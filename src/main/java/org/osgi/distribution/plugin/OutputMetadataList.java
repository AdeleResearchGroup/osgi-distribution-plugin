package org.osgi.distribution.plugin;

import java.util.ArrayList;
import java.util.List;

public class OutputMetadataList {

	List<Output> lsOutput = null;
	
	public OutputMetadataList(){
		lsOutput = new ArrayList<Output>();
	}

	public List<Output> getLsOutput() {
		return lsOutput;
	}

	public void setLsOutput(List<Output> lsOutput) {
		this.lsOutput = lsOutput;
	}
	
}
