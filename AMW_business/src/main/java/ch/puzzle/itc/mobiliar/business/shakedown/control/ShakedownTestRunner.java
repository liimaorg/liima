/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.shakedown.control;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXB;

import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.STS;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.Test;
import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.TestSet;
import org.apache.commons.io.IOUtils;

import ch.puzzle.itc.mobiliar.business.shakedown.xmlmodel.TestSet.OverallStatus;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownStpEntity;
import ch.puzzle.itc.mobiliar.common.exception.ShakedownTestException;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;

@Stateless
public class ShakedownTestRunner {

	@Inject
	protected EntityManager entityManager;
	@Inject
	protected Logger log;

	/**
	 * @param sts
	 * @return
	 */
	public TestSet executeShakedownTest(STS sts) {
		TestSet resultSet;
		String strcsv = getStrCsv();
		ArrayList<Test> otherTests = new ArrayList<Test>();
		String bundle;
		try {
			bundle = bundleSTM(strcsv, sts.getShakedowntestsAsCSV(), sts);
			try {
				List<String> missingSTPs = copySTMtoRemoteServerAndGetMissingSTPs(bundle, sts);
				if (missingSTPs != null && !missingSTPs.isEmpty()) {
					for (String missingSTP : missingSTPs) {
						try {
							copySTPtoRemoteServer(missingSTP, sts);
						} catch (ShakedownTestException e) {
							Test t = new Test();
							t.setName(missingSTP);
							t.setTestStatus(OverallStatus.failed.name());
							t.setStdErr(e.getMessage());
							log.log(Level.WARNING, "Was not able to copy STP " + missingSTP + " to remote server", e);
							otherTests.add(t);
						}
					}
				}
				try {
					launchTests(bundle, sts);
				} catch (ShakedownTestException e) {
					Test launchTestFailure = new Test();
					launchTestFailure.setName("General Test Execution");
					launchTestFailure.setTestStatus(OverallStatus.failed.name());
					launchTestFailure.setStdErr(e.getMessage());
					log.log(Level.WARNING, "General Test Execution failed", e);
					otherTests.add(launchTestFailure);
				}
			} catch (ShakedownTestException e1) {
				Test copySTMFailure = new Test();
				copySTMFailure.setName("General Test Execution");
				copySTMFailure.setTestStatus(OverallStatus.failed.name());
				copySTMFailure.setStdErr(e1.getMessage());
				log.log(Level.WARNING, "Was not able to copy STM to remote server", e1);
				otherTests.add(copySTMFailure);
			}

		} catch (ShakedownTestException e) {
			Test bundleFailure = new Test();
			bundleFailure.setName("General Test Execution");
			bundleFailure.setTestStatus(OverallStatus.failed.name());
			bundleFailure.setStdErr(e.getMessage());
			otherTests.add(bundleFailure);
			log.log(Level.WARNING, "Was not able to bundle STM", e);
		}
		String result = null;
		try {
			result = analyzeResult(sts);
		} catch (ShakedownTestException e) {
			Test resultAnalyzeFailure = new Test();
			resultAnalyzeFailure.setName("General Test Execution");
			resultAnalyzeFailure.setTestStatus(OverallStatus.failed.name());
			resultAnalyzeFailure.setStdErr(e.getMessage());
			otherTests.add(resultAnalyzeFailure);
			log.log(Level.WARNING, "Was not able to analyze results", e);
		}
		if (result != null) {
			resultSet = JAXB.unmarshal(new StringReader(result), TestSet.class);
		} else {
			resultSet = new TestSet();
		}
		if (resultSet.getTests() == null) {
			resultSet.setTests(otherTests);
		} else {
			resultSet.getTests().addAll(otherTests);
		}
		return resultSet;

	}

	private String analyzeResult(STS sts) throws ShakedownTestException {
		String testResultPath = ConfigurationService.getProperty(ConfigKey.TEST_RESULT_PATH);
		try {
			StringBuilder sb = new StringBuilder();
			ZipInputStream zis = null;
			try {
				zis = new ZipInputStream(new FileInputStream(testResultPath + File.separator + sts.getTestId() + ".zip"));
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					if (entry.getName().equals("/result.xml")) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
						String s;
						while ((s = reader.readLine()) != null) {
							sb.append(s);
						}
					}
				}
			} finally {
				if (zis != null) {
					zis.close();
				}
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			throw new ShakedownTestException("Was not able to analyze the result - no result file found!", e);
		} catch (IOException e) {
			throw new ShakedownTestException("Was not able to analyze the result", e);
		}

	}

	private void launchTests(String bundle, STS sts) throws ShakedownTestException {
		File f = new File(bundle);
		// TODO set other parameters to ssh call
		String resultPath = ConfigurationService.getProperty(ConfigKey.TEST_RESULT_PATH);
		File resPath = new File(resultPath);
		if (resultPath != null && resPath.exists() && resPath.isDirectory()) {
			String launchSTM = "ssh " + sts.getUser() + "@" + sts.getRemoteHost() + " java -jar " + sts.getRemoteSTPPath() + File.separator + "stms" + File.separator + f.getName() + ' '
					+ sts.getRemoteSTPPath();
			log.info("EXECUTE: " + launchSTM);
			try {
				Process launcher = Runtime.getRuntime().exec(launchSTM);
				if (launcher.waitFor() == 0) {
					File stp = new File(sts.getRemoteSTPPath());
					String fetchResultCommand = "scp " + sts.getUser() + "@" + sts.getRemoteHost() + ":" + stp.getParent() + File.separator + "results" + File.separator + sts.getTestId() + ".zip "
							+ resultPath + File.separator + sts.getTestId() + ".zip";
					// TODO save results to configurable folder
					log.info("EXECUTE: " + fetchResultCommand);
					Process fetchResult = Runtime.getRuntime().exec(fetchResultCommand);
					if (fetchResult.waitFor() == 0) {
						log.info("Copied results from to host " + sts.getRemoteHost());
					} else {
						throw new ShakedownTestException("Was not able to copy results from host " + sts.getRemoteHost());
					}
				} else {
					throw new ShakedownTestException("The test manager has exited anormally (exit code != 0)");
				}
			} catch (IOException e) {
				throw new ShakedownTestException("Was not able to launch tests on remote server", e);
			} catch (InterruptedException e) {
				throw new ShakedownTestException("Was not able to launch tests on remote server", e);
			}
		} else {
			throw new ShakedownTestException("amw.testResultPath not set or value is not a directory");
		}
	}

	private void copyThroughSCP(String filepath, STS sts) throws IOException, InterruptedException, ShakedownTestException {
		Runtime r = Runtime.getRuntime();
		File f = new File(filepath);
		String copyCommand = "scp " + filepath + " " + sts.getUser() + "@" + sts.getRemoteHost() + ":" + sts.getRemoteSTPPath() + File.separator + f.getName();
		log.info("EXECUTE: " + copyCommand);
		Process p = r.exec(copyCommand);
		if (p.waitFor() == 0) {
			log.info("Copied STP " + filepath + " to host " + sts.getRemoteHost());
		} else {
			throw new ShakedownTestException("Was not able to copy STP " + filepath + " to host " + sts.getRemoteHost());
		}
	}

	private void copySTPtoRemoteServer(String missingSTP, STS sts) throws ShakedownTestException {
		String stmrepo = ConfigurationService.getProperty(ConfigKey.STM_REPO);
		File f = new File(stmrepo + File.separator + missingSTP);
		if (f.exists()) {
			try {
				copyThroughSCP(f.getAbsolutePath(), sts);
			} catch (IOException e) {
				throw new ShakedownTestException(e);
			} catch (InterruptedException e) {
				throw new ShakedownTestException(e);
			}
		} else {
			throw new ShakedownTestException("STP not found in repository!");
		}
	}

	private List<String> copySTMtoRemoteServerAndGetMissingSTPs(String bundle, STS sts) throws ShakedownTestException {
		List<String> result = new ArrayList<String>();
		Runtime r = Runtime.getRuntime();
		File f = new File(bundle);
		if (!f.exists()) {
			throw new ShakedownTestException("The STM bundle file " + f.getAbsolutePath() + " does not exist!");
		}
		// Create folder if it does not yet exist
		String createRemoteSTMFolderIfNotExists = "ssh " + sts.getUser() + "@" + sts.getRemoteHost() + " mkdir -p " + sts.getRemoteSTPPath() + File.separator + "stms";
		log.info("EXECUTE: " + createRemoteSTMFolderIfNotExists);
		try {
			Process createRemoteSTMFolder = r.exec(createRemoteSTMFolderIfNotExists);
			if (createRemoteSTMFolder.waitFor() != 0) {
				throw new ShakedownTestException("Was not able to create STM folder on remote site: " + sts.getRemoteHost() + File.separator + "stms with user " + sts.getUser());
			}
			String copySTMcommand = "scp " + bundle + " " + sts.getUser() + "@" + sts.getRemoteHost() + ":" + sts.getRemoteSTPPath() + File.separator + "stms" + File.separator + f.getName();
			log.info("EXECUTE: " + copySTMcommand);
			Process p = r.exec(copySTMcommand);
			if (p.waitFor() == 0) {
				log.info("Copied STM bundle " + bundle + " to host " + sts.getRemoteHost());
				String launchDepMgmtCommand = "ssh " + sts.getUser() + "@" + sts.getRemoteHost() + " java -jar " + sts.getRemoteSTPPath() + File.separator + "stms" + File.separator + f.getName()
						+ " " + sts.getRemoteSTPPath() + " dependencyManagement";
				log.info("EXECUTE: " + launchDepMgmtCommand);
				Process dependencyMgmt = r.exec(launchDepMgmtCommand);
				BufferedReader bufferedreader = null;
				try {
					bufferedreader = new BufferedReader(new InputStreamReader(new BufferedInputStream(dependencyMgmt.getErrorStream())));
					String errorline;
					while ((errorline = bufferedreader.readLine()) != null) {
						if (errorline.startsWith("@{") && errorline.trim().endsWith("}")) {
							result.add(errorline.trim().substring(2, errorline.trim().length() - 1));
						}
					}
				} finally {
					if (bufferedreader != null) {
						bufferedreader.close();
					}
				}
			} else {
				throw new ShakedownTestException("Was not able to copy STP " + bundle + " to host " + sts.getRemoteHost() + " with ssh user " + sts.getUser());
			}
		} catch (IOException e) {
			throw new ShakedownTestException("Was not able to copy STM to remote server ", e);
		} catch (InterruptedException e) {
			throw new ShakedownTestException("Was not able to copy STM to remote server ", e);
		}
		return result;
	}

	String bundleSTM(String strcsv, String stscsv, STS sts) throws ShakedownTestException {
		String tmpdir = System.getProperty("java.io.tmpdir");
		String stmpath = ConfigurationService.getProperty(ConfigKey.STM_PATH);
		if (tmpdir != null && !tmpdir.trim().isEmpty()) {
			if (stmpath != null && new File(stmpath).exists()) {
				try {
					File bundle = new File(tmpdir + File.separator + sts.getTestId() + "stm.jar");
					ZipOutputStream bundleZOS = new ZipOutputStream(new FileOutputStream(bundle));
					bundleZOS.putNextEntry(new ZipEntry("str.csv"));
					IOUtils.write(strcsv, bundleZOS);
					bundleZOS.putNextEntry(new ZipEntry("sts.csv"));
					IOUtils.write(stscsv, bundleZOS);
					ZipInputStream zin = new ZipInputStream(new FileInputStream(stmpath));
					ZipEntry ze;
					while ((ze = zin.getNextEntry()) != null) {
						bundleZOS.putNextEntry(ze);
						IOUtils.copy(zin, bundleZOS);
					}
					bundleZOS.close();
					log.info("Successfully bundled STM and stored to " + bundle.getAbsolutePath());
				} catch (IOException e) {
					throw new ShakedownTestException("Was not able to bundle STM", e);
				}
			}
			else{
				throw new ShakedownTestException("No STM found at location configured with system property "+ConfigKey.STM_PATH+".");
			}
		} else {
			throw new ShakedownTestException("No temporary folder found - is java.io.tmpdir defined?");
		}
		return tmpdir + File.separator + sts.getTestId() + "stm.jar";
	}

	public String getStrCsv() {
		StringBuilder sb = new StringBuilder();
		TypedQuery<ShakedownStpEntity> q = entityManager.createQuery("from ShakedownStpEntity", ShakedownStpEntity.class);
		List<ShakedownStpEntity> stps = q.getResultList();
		for (ShakedownStpEntity s : stps) {
			sb.append(s.getStpName()).append('\t').append(s.getVersion()).append('\n');
		}
		return sb.toString();
	}

}
