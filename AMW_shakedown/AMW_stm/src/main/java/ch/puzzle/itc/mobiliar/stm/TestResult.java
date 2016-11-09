package ch.puzzle.itc.mobiliar.stm;

import java.util.List;

public class TestResult {

	private final String result;
	private final List<String> additionalResources;
	private final STP stp;
	
	public TestResult(String result, List<String> additionalResources, STP stp) {
		super();
		this.result = result;
		this.additionalResources = additionalResources;
		this.stp = stp;
	}

	public String getResult() {
		return result;
	}

	public List<String> getAdditionalResources() {
		return additionalResources;
	}

	public STP getStp() {
		return stp;
	}
	
	
	
}
