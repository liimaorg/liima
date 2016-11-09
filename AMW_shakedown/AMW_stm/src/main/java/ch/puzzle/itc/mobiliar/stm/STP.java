package ch.puzzle.itc.mobiliar.stm;

public class STP {
	
	private static final String NAME_VERSION_DELIMITER = "-";

	private static final String FILENAME_POSTFIX = ".zip";
	
	private final String name;
	private final String version;
	private String[] params;
	
	public STP(String name, String version){
		this.name = name;
		this.version = version;
	}
	
	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}	
	
	public String[] getParams(){
		return params;
	}
	
	public void setParams(String... params){
		this.params = params;
	}
	
	public String getCombinedFileName(){
		return name+NAME_VERSION_DELIMITER+version+FILENAME_POSTFIX;
	}
	
}
