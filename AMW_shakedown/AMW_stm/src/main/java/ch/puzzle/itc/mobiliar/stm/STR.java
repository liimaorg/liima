package ch.puzzle.itc.mobiliar.stm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class STR {

	protected static final String STR_CSV_ROW_DELIM = "\t";
	protected static final String STR_CSV_LINE_DELIM = "\n\r";
	
	private final List<STP> stps;
	
	private List<String> stpnames;
	
	public STR(String stpCsv){
		if(stpCsv == null){
			throw new IllegalArgumentException("stpCsv must not be null");
		}
		stps = parseSTR(stpCsv);
	}

	public List<STP> getStps() {
		return stps;
	}
	
	public STP createSTPByName(String stpName){		
		for(STP stp : stps){
			if(stp.getName().equals(stpName)) {
				return new STP(stp.getName(), stp.getVersion());
			}
		}
		return null;
	}
	
	public STP getSTPByName(String stpName){
		for(STP stp : stps){
			if(stp.getName().equals(stpName)) {
				return stp;
			}
		}
		return null;
	}
	
	protected List<STP> parseSTR(String csv){
		StringTokenizer t = new StringTokenizer(csv, STR_CSV_LINE_DELIM);
		List<STP> result = new ArrayList<STP>();
		while(t.hasMoreTokens()){
			String row = t.nextToken();
			String[] cols = row.split(STR_CSV_ROW_DELIM);
			if(cols.length==2){
				STP s = new STP(cols[0], cols[1]);
				result.add(s);
			}
		}
		return Collections.unmodifiableList(result);		
	}
	

	
	public List<String> getStpNames(){
		if(stpnames==null){
			stpnames = new ArrayList<String>();
			for(STP s : stps){
				stpnames.add(s.getCombinedFileName());
			}			
		}
		return stpnames;
	}
	
}
