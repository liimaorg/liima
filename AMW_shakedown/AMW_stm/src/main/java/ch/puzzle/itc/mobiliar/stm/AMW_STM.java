package ch.puzzle.itc.mobiliar.stm;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AMW_STM {

    	private final static String SHELL = "sh";

	// arg 0
	protected String pathToSTPs;

	// arg 1
	protected boolean dependencyManagement;

	protected int numberOfThreads = 20;

	// arg 2
	protected long testTimeout = 30000;

	private static final String DEPENDENCY_MANAGEMENT_ARGUMENT_STRING = "dependencyManagement";

	private Housekeeping houseKeeping = new Housekeeping();

	private static AMW_STM stm;

	private STR str;

	private STS sts;

	/**
	 * @param args : First argument is the path to the STPs, second argument is the number of
	 *            threads (number of tests that should be executed in parallel), third argument is
	 *            the timeout of tests if second Argument is dependencyManagement
	 *            dependencyManagement is enabled The Timeout is only set if dependencyManagement is
	 *            false
	 */
	public static void main(String[] args) {
		// create STM based on Args
		stm = createSTMbasedOnArgs(args);

		try {
			stm.str = stm.getSTR();
		}
		catch (IOException e) {
			System.out.println("No STR found!");
			System.exit(1);
		}
		try {
			stm.sts = stm.getSTS(stm.str);
		}
		catch (IOException e) {
			System.out.println("No STS found!");
			System.exit(1);
		}

		// Clean up structure
		stm.getHousekeeping().cleanOldSTPsToDelete(stm.str, stm.pathToSTPs);

		if (stm.dependencyManagement) {
			checkAndLogMissingPackages(stm);
		}
		else {
			stm.processSTPs();
		}
	}

	/**
	 * checks and Logs missing Packages
	 */
	private static void checkAndLogMissingPackages(AMW_STM stm) {
		// Find missing packages
		List<String> missingPackages = stm.getHousekeeping().getMissingSTPs(stm.sts, stm.pathToSTPs);

		if (missingPackages.size() > 0) {
			for (String missing : missingPackages) {
				System.err.println("@{" + missing + "}");
			}
			System.exit(1);
		}
		else {
			System.exit(0);
		}
	}

	/**
	 * arg[0] = path to stp <br/>
	 * arg[1] = DEPENDENCY_MANAGEMENT_ARGUMENT_STRING or numberOfThreads as int<br/>
	 * arg[2] = testTimeout as int
	 * 
	 * @param args
	 * @return an AMW_STM based on the actual args
	 */
	protected static AMW_STM createSTMbasedOnArgs(String[] args) {
		AMW_STM stm = new AMW_STM();
		// Read configurations
		if (args.length > 0) {
			stm.pathToSTPs = args[0];
		}
		if (args.length > 1) {
			if (DEPENDENCY_MANAGEMENT_ARGUMENT_STRING.equals(args[1])) {
				stm.dependencyManagement = true;
			}
			else {
				stm.numberOfThreads = Integer.parseInt(args[1]);
			}
			// dependencyManagement false and arg[2] is set
			if (!stm.dependencyManagement && args.length > 2) {
				stm.testTimeout = Integer.parseInt(args[2]);
			}
		}
		return stm;
	}

	Housekeeping getHousekeeping() {
		return houseKeeping;
	}

	STR getSTR() throws IOException {
		return new STR(readFile("str.csv"));
	}

	STS getSTS(STR str) throws IOException {
		return new STS(readFile("sts.csv"), str, SHELL);
	}

	String readFile(String file) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(file)));
		String read = r.readLine();
		while (read != null) {
			sb.append(read).append('\n');
			read = r.readLine();
		}
		return sb.toString();
	}

	protected void processSTPs() {

		File stps = new File(pathToSTPs);
		File resultsDir = new File(stps.getParent() + File.separator + "results" + File.separator + sts.getId());
		// create ResultDir if it does not exist
		if (!resultsDir.exists()) {
			resultsDir.mkdirs();
		}

		StringBuffer resultLog = new StringBuffer();

		resultLog.append("<testset id=\"");
		resultLog.append(sts.getId());
		resultLog.append("\">");
		ExecutorService executors = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<TestResult>> futures = null;
		try {
			futures = executors.invokeAll(sts.getProcesses(pathToSTPs), testTimeout, TimeUnit.MILLISECONDS);
			executors.shutdown();
			executors.awaitTermination(testTimeout, TimeUnit.MILLISECONDS);

			processResults(futures, resultsDir, resultLog);

		}
		catch (Exception e) {
			resultLog.append("<failure>");
			resultLog.append("Exception: " + e.getMessage());
			resultLog.append("</failure>");
		}
		resultLog.append("</testset>");

		try {
			FileWriter logFile;
			logFile = new FileWriter(resultsDir.getAbsolutePath() + File.separator + "result.xml");
			logFile.write(resultLog.toString());
			logFile.flush();
			logFile.close();
		}
		catch (IOException e1) {
			System.err.println("Was not able to create file " + resultsDir.getAbsolutePath() + File.separator
					+ "result.xml:" + e1.getMessage());
			System.exit(1);
		}

		// create Zip
		createZip(resultsDir, sts.getId());

		// remove ResultDir
		System.out.println("Remove directory " + resultsDir.getAbsolutePath());
		try {
			Runtime.getRuntime().exec("rm -rf " + resultsDir.getAbsolutePath());
		}
		catch (IOException e) {
			System.err.println("Was not able to remove resultDir  " + resultsDir.getAbsolutePath() + " "
					+ e.getMessage());
			System.exit(1);
		}
	}

	protected void createZip(File resultsDir, String stsId) {

		String zipFilename = resultsDir.getParent() + File.separator + stsId + ".zip";
		ZipOutputStream os;
		try {
			os = new ZipOutputStream(new FileOutputStream(zipFilename));
			compressDirectory(resultsDir.getAbsolutePath(), "", os);
			os.flush();
			os.close();
		}
		catch (FileNotFoundException e1) {
			System.err.println("Was not able to create zip File " + zipFilename + " " + e1.getMessage());
			System.exit(1);
		}
		catch (IOException e) {
			System.err.println("Was not able to create zip File " + zipFilename + " " + e.getMessage());
			System.exit(1);
		}

	}

	private void processResults(List<Future<TestResult>> futures, File resultsDir, StringBuffer log)
			throws InterruptedException, ExecutionException, IOException {
		System.out.println("*********** RESULTS ************");
		for (Future<TestResult> future : futures) {

			if (future.isDone() && !future.isCancelled()) {
				TestResult testResult = future.get();

				String result = testResult.getResult();

				log.append(result);
				System.out.println(result);

				handleAdditionalResouceInResult(resultsDir, testResult);
			}
		}
	}

	private void handleAdditionalResouceInResult(File resultsDir, TestResult testResult) throws IOException {

		if (testResult != null) {
			for (String additionalResource : testResult.getAdditionalResources()) {
				File toCopy = new File(additionalResource);
				if (toCopy.exists()) {
					File destination = new File(resultsDir + File.separator + testResult.getStp().getName() + "-"
							+ testResult.getStp().getVersion() + File.separator);
					if (!destination.exists()) {
						destination.mkdirs();
					}
					// FIXME: tph 11.7.2013 refactor copy File use io
					FileChannel source = null;
					FileChannel dest = null;
					try {
						source = new FileInputStream(toCopy).getChannel();
						dest = new FileOutputStream(destination.getAbsolutePath() + File.separator
								+ toCopy.getName()).getChannel();
						dest.transferFrom(source, 0, source.size());
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						if(source != null){
							source.close();
						}
						if(dest != null){
							dest.close();
						}
					}
					
					try {
						dest = new FileOutputStream(destination.getAbsolutePath() + File.separator
								+ toCopy.getName()).getChannel();
					}
					catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Add resource: " + additionalResource);
				}
				else {
					System.err.println("AdditionalResource File " + additionalResource + " does not exist.");
				}
			}
		}
	}

	private void compressDirectory(String directory, String path, ZipOutputStream out) throws IOException {
		byte[] data = new byte[1024];
		File fileToCompress = new File(directory);
		// list contents.
		String[] contents = fileToCompress.list();
		// iterate through directory and compress files.
		for (int i = 0; i < contents.length; i++) {
			File f = new File(directory, contents[i]);
			System.out.println(f.getPath());
			// testing type. directories and files have to be treated
			// separately.
			if (f.isDirectory()) {
				// add empty directory
				out.putNextEntry(new ZipEntry(f.getName() + File.separator));
				// initiate recursive call
				compressDirectory(f.getPath(), path + File.separator + f.getName(), out);
				// continue the iteration
				continue;
			}
			else {
				// prepare stream to read file.
				FileInputStream in = new FileInputStream(f);
				// create ZipEntry and add to outputting stream.
				out.putNextEntry(new ZipEntry(path + File.separator + f.getName()));
				// write the data.
				int len;
				while ((len = in.read(data)) > 0) {
					out.write(data, 0, len);
				}
				out.flush();
				out.closeEntry();
				in.close();
			}
		}
	}
}
