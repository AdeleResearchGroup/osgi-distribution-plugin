package org.osgi.distribution.plugin;

public class Output {
	
	private String includesArtifactId = null;
	private String directory = null;
	private String outputFileName = null;
	
	public Output(){
	}

	public String getIncludesArtifactId() {
		return includesArtifactId;
	}

	public void setIncludesArtifactId(String includesArtifactId) {
		this.includesArtifactId = includesArtifactId;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

}
