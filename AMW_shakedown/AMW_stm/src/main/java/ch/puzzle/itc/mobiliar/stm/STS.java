package ch.puzzle.itc.mobiliar.stm;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class STS {

	private final String shell;

	private String id;

	private final List<STP> stps;

	public STS(String stsCsv, STR str, String shell) {
		this.stps = parseSTS(stsCsv, str);
		this.shell = shell;
	}

	public String getId() {
		return id;
	}

	List<STP> parseSTS(String csv, STR str) {
		String[] rows = csv.split("\n");
		List<STP> result = new ArrayList<STP>();
		boolean firstLine = true;
		for (String row : rows) {
			if (firstLine) {
				id = row.trim();
				firstLine = false;
			}
			else {
				String[] cols = row.split("\t");
				if (cols.length > 0) {
					String[] additionalParams = null;
					if (cols.length > 1) {
						additionalParams = new String[cols.length - 1];
						int p = 0;
						for (int i = 1; i < cols.length; i++) {
							additionalParams[p++] = cols[i];
						}
					}
					STP stp = str.createSTPByName(cols[0]);
					if (stp != null) {
						stp.setParams(additionalParams);
						result.add(stp);
					}
					else {
						result.add(new STP(cols[0], null));
					}
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<STP> getStps() {
		return stps;
	}

	private List<String> stpnames;

	public List<String> getStpNames() {
		if (stpnames == null) {
			stpnames = new ArrayList<String>();
			for (STP s : stps) {
				stpnames.add(s.getCombinedFileName());
			}
		}
		return stpnames;
	}

	File unzip(String filename) throws IOException {
		byte[] buffer = new byte[1024];
		File tempDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + Math.random());
		if (!tempDirectory.exists()) {
			tempDirectory.mkdirs();
		}

		// get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(filename));
		// get the zipped file list entry
		ZipEntry ze = zis.getNextEntry();

		while (ze != null) {
			if (!ze.isDirectory()) {
				String fileName = ze.getName();
				File newFile = new File(tempDirectory + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
			}
			ze = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
		return tempDirectory;
	}

	public List<Callable<TestResult>> getProcesses(final String pathToSTPs) {
		List<Callable<TestResult>> processes = new ArrayList<Callable<TestResult>>();
		for (STP s : stps) {
			final STP stp = s;
			Callable<TestResult> c = new Callable<TestResult>() {

				public TestResult call() throws Exception {

					StringBuilder result = new StringBuilder();
					List<String> additionalResources = new ArrayList<String>();
					result.append("<test name=\"").append(stp.getName()).append("\" version=\"")
							.append(stp.getVersion()).append("\">");
					// UNZIP THE STP IF IT EXISTS
					String filePath = pathToSTPs + File.separator + stp.getCombinedFileName();
					if (new File(filePath).exists()) {
						File tempDirectory = unzip(filePath);

						// EXECUTE RUNTEST.SH
						File runscript = new File(tempDirectory + File.separator + "runtest.sh");
						runscript.setExecutable(true);
						StringBuilder command = new StringBuilder();
						command.append(runscript.getAbsolutePath());
						for (String param : stp.getParams()) {
							command.append(' ').append(param);
						}
						Runtime r = Runtime.getRuntime();
						result.append('\n').append("<command").append(" executionTime=\"")
								.append(new Date().getTime()).append("\">")
								.append(command.toString()).append("</command>\n");
						Process p = r.exec(new String[] { shell, "-c", command.toString() });
						InputStream errorin = p.getErrorStream();
						InputStream in = p.getInputStream();
						BufferedInputStream buf = new BufferedInputStream(in);
						BufferedInputStream errbuf = new BufferedInputStream(errorin);
						InputStreamReader inread = new InputStreamReader(buf);
						InputStreamReader errinread = new InputStreamReader(errbuf);
						BufferedReader bufferedreader = new BufferedReader(inread);
						BufferedReader bufferederrreader = new BufferedReader(errinread);
						StringBuilder sbError = new StringBuilder();
						StringBuilder sb = new StringBuilder();
						String errorline;
						while ((errorline = bufferederrreader.readLine()) != null) {
							sbError.append(errorline).append('\n');
						}
						result.append("<stderr><![CDATA[\n").append(sbError.toString())
								.append("\n]]></stderr>");
						String line;
						while ((line = bufferedreader.readLine()) != null) {
							if (line.trim().startsWith("@{") && line.trim().endsWith("}")) {
								additionalResources.add(line.trim().substring(2,
										line.trim().length() - 1));
							}
							else {
								sb.append(line).append('\n');
							}
						}
						result.append("\n<stdout><![CDATA[").append(sb.toString())
								.append("\n]]></stdout>");

						result.append("\n<testStatus>");
						try {
							if (p.waitFor() != 0) {
								result.append("failed");
							}
							else {
								result.append("successful");
							}
						}
						catch (InterruptedException e) {
							result.append("interrupted");
						}
						finally {
							// Close the InputStream
							bufferedreader.close();
							inread.close();
							buf.close();
							in.close();
						}
						result.append("</testStatus>");
						System.out.println("Remove directory: " + tempDirectory);
						deleteDirectoryRecursively(tempDirectory.getAbsolutePath());
					}
					else {
						result.append("<command/><stderr/><stdout/><testStatus>missing</testStatus>");
					}
					result.append("\n</test>");
					return new TestResult(result.toString(), additionalResources, stp);
				}
			};
			processes.add(c);
		}
		return processes;
	}

	public void deleteDirectoryRecursively(String dir) throws IOException {
		Path directory = Paths.get(dir);
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
