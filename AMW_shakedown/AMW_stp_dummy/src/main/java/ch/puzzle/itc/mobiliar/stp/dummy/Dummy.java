package ch.puzzle.itc.mobiliar.stp.dummy;

public class Dummy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length>1 && Boolean.parseBoolean(args[1])==false){
			System.err.println("FAILURE");
			System.exit(1);
		}
		else{
			System.out.println("SUCCESS");
			if(args.length>1) {
				System.out.println("@{"+args[1]+"}");
			}
			System.exit(0);
		}
	}

}
