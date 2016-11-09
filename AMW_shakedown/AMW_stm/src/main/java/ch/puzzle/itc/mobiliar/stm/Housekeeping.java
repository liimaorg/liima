package ch.puzzle.itc.mobiliar.stm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Housekeeping {
	
	List<String> getOldSTPsToDelete(STR repo, String pathToSTPs){
		List<String> foundSTPs = getListOfSTPs(pathToSTPs);
		List<String> stprepo = repo.getStpNames();
		List<String> toDelete = new ArrayList<String>();
		for(String stp : foundSTPs){
			if(!stprepo.contains(stp)){
				toDelete.add(stp);
			}
		}		
		return toDelete;
	}
	
	
	List<String> getListOfSTPs(String pathToSTPs){
		File f = new File(pathToSTPs);
		//If the directory does not exist, create it
		if(!f.exists()){
			f.mkdir();
		}
		if(f.isDirectory()){
			return Arrays.asList(f.list());			
		}
		else{
			System.out.println("The defined path to the STPs is not a directory!");
			System.exit(1);
			return null;
		}
	}
	
	public List<String> getMissingSTPs(STS testSuite, String pathToSTPs){
		List<String> foundSTPs = getListOfSTPs(pathToSTPs);
		List<String> result = new ArrayList<String>();
		for(String stp : testSuite.getStpNames()){
			if(!foundSTPs.contains(stp)){
				result.add(stp);
			}
		}
		return result;		
	}
	
	
	public void cleanOldSTPsToDelete(STR repo, String pathToSTPs){
		for(String s : getOldSTPsToDelete(repo, pathToSTPs)){
			File f = new File(pathToSTPs+File.separator+s);
			if(f.exists()) {
				f.delete();
			}
		}
	}
}
