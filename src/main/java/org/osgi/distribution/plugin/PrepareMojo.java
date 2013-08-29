package org.osgi.distribution.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * Goal which prepares a distribution for the zip mojo.
 * 
 * @goal prepare
 * 
 * @phase prepare-package
 * @requiresDependencyResolution
 */
public class PrepareMojo extends AbstractMojo {

	/**
	 * @parameter expression="${project.artifactId}"
	 */
	protected String defaultDistribDirectoryName;

	public String defaultDistribDirectoryPath;

	/**
	 * The Maven Project Object
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The Maven Session Object
	 * 
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	protected MavenSession session;

	/**
	 * The Maven PluginManager Object
	 * 
	 * @component
	 * @required
	 */
	protected BuildPluginManager pluginManager;

	/**
	 * Maven plugin output metadata
	 * 
	 * @parameter alias="outputs"
	 * @readonly
	 */
	private List<Output> outputs;

	/**
	 * Default output directory
	 * 
	 * @parameter alias="defaultOutputDirectory" default-value="load"
	 * 
	 */
	private String defaultOutputDirectory;

	/**
	 * Generate or not starter scripts
	 * 
	 * @parameter alias="generateScripts" default-value="false"
	 * 
	 */
	private boolean generateScripts;

	/**
	 * Unzip Deployment packages into the output directory. The structure is flattened into the output directory.
	 * 
	 * @parameter alias="flattenDP" default-value="false"
	 * 
	 */
	private boolean flattenDP;

	/**
	 * Execute that mojo.
	 */
	public void execute() throws MojoExecutionException {

		defaultDistribDirectoryPath = this.project.getBuild().getDirectory() + File.separator
		      + defaultDistribDirectoryName;

		try {
			manageDependencies();
			if (flattenDP) {
				unzipDeploymentPackages();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		manageResources();
		eliminateDuplicateFiles();
		if (generateScripts) {
			generateScripts();
		}
		giveRights();

	}

	/**
	 * Eliminates the duplicated jar and dp files in "Felix" distributions. Duplicates are only eliminated in fileinstall
	 * directories, felix configuration is read to charge this information.
	 * 
	 */
	private void eliminateDuplicateFiles() {

		Properties prop = new Properties();
		List<File> directoryList = new ArrayList<File>();
		try {
			prop.load(new FileInputStream(defaultDistribDirectoryPath + File.separator + "conf" + File.separator
			      + "config.properties"));
			String deployDir = prop.getProperty("felix.auto.deploy.dir", "bundle");


			directoryList.add(new File(defaultDistribDirectoryPath, deployDir));

			// Directories in file install
			String dirsProperty = (String) prop.getProperty("felix.fileinstall.dir", "./load");

			// Multiple directories in property "felix.fileinstall.dir"
			if (dirsProperty != null && dirsProperty.indexOf(',') != -1) {
				StringTokenizer st = new StringTokenizer(dirsProperty, ",");
				while (st.hasMoreTokens()) {
					final String dir = st.nextToken().trim();
					if (dir.startsWith("./")) {
						// Taking in account only local directories
						directoryList.add(new File(defaultDistribDirectoryPath, dir.substring(2)));
					}
				}
			} else { // Only one directory in property "felix.fileinstall.dir"
				if (dirsProperty.startsWith("./")) {
					// Taking in account only local directories
					directoryList.add(new File(defaultDistribDirectoryPath, dirsProperty.substring(2)));
				}
			}

			Set<String> checkSums = new HashSet<String>();
			List<File> toDelete = new ArrayList<File>();

			for (File dir : directoryList) {
				if (dir.exists()) {
					File[] filesInDir = dir.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".jar") || name.endsWith(".dp");
						}
					});
					
					// There are .jar and .dp files in the directory?
					if (filesInDir!=null && filesInDir.length>0) {
						for (File file : filesInDir) {
							try {
								String md5 = getMD5Checksum(file);
								// The file is in a prevoius directory, it must be deleted
								if (checkSums.contains(md5)) {
									toDelete.add(file);
								} else {
									checkSums.add(md5);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			for (File file : toDelete) {
				file.delete();
			}

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	/**
	 * creates a byte checksum of a file
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private byte[] createChecksum(File file) throws Exception {

		InputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}

	/**
	 * creates a string md5 checksum of a file
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private String getMD5Checksum(File file) throws Exception {
		byte[] b = createChecksum(file);
		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	/**
	 * Give executable rights to bat and sh files in distrib directory.
	 */
	private void giveRights() {
		File[] filesInDistrib = new File(defaultDistribDirectoryPath).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".bat") || name.endsWith(".sh") || !name.contains("."));
			}
		});
		for (File file : filesInDistrib) {
			file.setExecutable(true);
		}
	}

	private void generateScripts() {
		String winpath = this.project.getBasedir() + File.separator + "start.bat";
		String unixpath = this.project.getBasedir() + File.separator + "start.sh";
		generateScriptFile(generateWinScriptContent(), winpath);
		generateScriptFile(generateUnixScriptContent(), unixpath);
	}

	private String generateWinScriptContent() {
		StringBuilder content = new StringBuilder();
		content.append("cd \"");
		content.append(this.project.getBuild().getDirectory()).append(File.separator).append(defaultDistribDirectoryName);
		content.append("\"\n");
		content.append("java %* -jar bin\\felix.jar");
		return content.toString();
	}

	/**
	 * Generate unix script files, useful on build.
	 */
	private String generateUnixScriptContent() {
		StringBuilder content = new StringBuilder("#!/usr/bin/env sh\n");
		content.append("cd \"");
		content.append(this.project.getBuild().getDirectory()).append(File.separator).append(defaultDistribDirectoryName);
		content.append("\"\n");
		content.append("exec java $@ -jar bin/felix.jar");
		return content.toString();
	}

	private void generateScriptFile(String content, String filename) {
		try {
			File file = new File(filename);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
				file.setExecutable(true);
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void manageResources() throws MojoExecutionException {

		Xpp3Dom config = MojoExecutor.configuration(MojoExecutor.element("outputDirectory", defaultDistribDirectoryPath),
		      MojoExecutor.element("overwrite", "true"));

		MojoExecutor.executeMojo(MojoExecutor.plugin("org.apache.maven.plugins", "maven-resources-plugin", "2.6"),
		      MojoExecutor.goal("resources"), config, MojoExecutor.executionEnvironment(project, session, pluginManager));

	}

	/**
	 * gather dependencies as dom element.
	 * 
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	private void manageDependencies() throws MojoExecutionException, IOException {
		List<Dependency> dependencies = project.getDependencies();

		for (Dependency dependency : dependencies) {
			if (dependency.getType().equals("play2")) {
				unzipPlay2WithDependencyPlugin(dependency);
			} else if (dependency.getType().equals("osgi-distribution")) {
				unzipOsgiDistributionWithDependencyPlugin(dependency);
			} else {
				copyDependency(dependency);
			}
		}
	}

	private void unzipDeploymentPackages() throws IOException {

		File tempDistribDir = new File(defaultDistribDirectoryPath);

		File[] firstLevelDirectories = tempDistribDir.listFiles(new FileFilter() {
			public boolean accept(File dir) {
				return dir.isDirectory();
			}
		});

		for (File dir : firstLevelDirectories) {
			File[] files = dir.listFiles();
			getLog().info("First level directory " + dir.getName());
			for (File dpFile : files) {
				if (dpFile.getName().endsWith(".dp")) {
					unZipFile(dpFile, dir);
					dpFile.delete();
				}
			}
						
			// Copy all files in $distribution/$dir/bundles into $distribution/$dir
			File tempBundlesDir = new File(dir, "bundles");
			if (tempBundlesDir.exists()) {
				// Copy the files to the upper level
				FileUtils.copyDirectoryStructure(tempBundlesDir, dir);
				// Delete the $distribution/$outputDirectory/bundles directory
				FileUtils.deleteDirectory(tempBundlesDir);
			}
			
			File tempMetaInfDir = new File(dir, "META-INF");
			if (tempBundlesDir.exists()) {
				// Delete the $distribution/$dir/META-INF
				FileUtils.deleteDirectory(tempMetaInfDir);
			}

		}
	}

	private void unzipOsgiDistributionWithDependencyPlugin(Dependency dep) throws MojoExecutionException, IOException {
		String temporalDependencyPath = this.project.getBuild().getDirectory() + File.separator + "dependencies";
		Xpp3Dom config = MojoExecutor.configuration(MojoExecutor.element("includeGroupIds", dep.getGroupId()),
		      MojoExecutor.element("includeArtifactIds", dep.getArtifactId()),
		      MojoExecutor.element("outputDirectory", temporalDependencyPath));

		MojoExecutor.executeMojo(MojoExecutor.plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.5.1"),
		      MojoExecutor.goal("unpack-dependencies"), config,
		      MojoExecutor.executionEnvironment(project, session, pluginManager));
		File temporal = new File(temporalDependencyPath + File.separator + dep.getArtifactId());
		FileUtils.copyDirectoryStructure(temporal, new File(defaultDistribDirectoryPath));
		// temporal.delete();
	}

	private void unzipPlay2WithDependencyPlugin(Dependency dep) throws MojoExecutionException, IOException {
		String temporalDependencyPath = this.project.getBuild().getDirectory() + File.separator + "dependencies";
        //Get directory destination
        String destinationDirectory = getDirectoryDestinationForDependency(dep);
		// prepare dependency plugin config
		Xpp3Dom items = null;
		items = new Xpp3Dom("artifactItems");
		Xpp3Dom itemAsDom = new Xpp3Dom("artifactItem");
		itemAsDom.addChild(MojoExecutor.element("groupId", dep.getGroupId()).toDom());
		itemAsDom.addChild(MojoExecutor.element("artifactId", dep.getArtifactId()).toDom());
		itemAsDom.addChild(MojoExecutor.element("version", dep.getVersion()).toDom());
		itemAsDom.addChild(MojoExecutor.element("type", "zip").toDom());
		itemAsDom.addChild(MojoExecutor.element("overWrite", "true").toDom());
		itemAsDom.addChild(MojoExecutor.element("excludes", "**/README,**/start,,**/start.bat").toDom());

		Xpp3Dom config = MojoExecutor.configuration(MojoExecutor.element("overWriteSnapshots", "true"),
		      MojoExecutor.element("overWriteIfNewer", "true"));

		config.addChild(MojoExecutor.element("outputDirectory",
		      temporalDependencyPath + File.separator + dep.getArtifactId()).toDom());

		items.addChild(itemAsDom);
		config.addChild(items);

		MojoExecutor.executeMojo(MojoExecutor.plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.5.1"),
		      MojoExecutor.goal("unpack"), config, MojoExecutor.executionEnvironment(project, session, pluginManager));
		File temporal = new File(temporalDependencyPath + File.separator + dep.getArtifactId());
		for (File temporalFile : temporal.listFiles()) {
			if (temporalFile.isDirectory() && temporalFile.getName().endsWith(dep.getVersion()))
				FileUtils.copyDirectoryStructure(temporalFile, new File(destinationDirectory));
		}

		// temporal.delete();
	}


	/**
	 * Copy the dependency given as parameter into default folder, or given output folder.
	 * 
	 * @param dep
	 * @throws MojoExecutionException
	 */
	private void copyDependency(Dependency dep) throws MojoExecutionException {

		// prepare dependency plugin config
		boolean foundMatching = false;
		Xpp3Dom items = null;
		items = new Xpp3Dom("artifactItems");
		Xpp3Dom itemAsDom = new Xpp3Dom("artifactItem");
		itemAsDom.addChild(MojoExecutor.element("groupId", dep.getGroupId()).toDom());
		itemAsDom.addChild(MojoExecutor.element("artifactId", dep.getArtifactId()).toDom());
		itemAsDom.addChild(MojoExecutor.element("version", dep.getVersion()).toDom());
		itemAsDom.addChild(MojoExecutor.element("type", dep.getType()).toDom());

		Xpp3Dom config = MojoExecutor.configuration(MojoExecutor.element("overWriteSnapshots", "true"),
		      MojoExecutor.element("overWriteIfNewer", "true"));

		if (dep.getType().equals("osgi-distribution")) {
			config.addChild(MojoExecutor.element("outputDirectory", defaultDistribDirectoryPath).toDom());
		} else {
			if (outputs != null) {
				// check if there is an output entry for that dependency
				for (Output output : outputs) {
					if (dep.getArtifactId().equals(output.getIncludesArtifactId())) {
						// found a matching
						foundMatching = true;
						if (output.getOutputFileName() != null) {
							itemAsDom.addChild(MojoExecutor.element("destFileName", output.getOutputFileName()).toDom());
						}
						if (output.getDirectory() != null) {
							config.addChild(MojoExecutor.element("outputDirectory",
							      defaultDistribDirectoryPath + File.separator + output.getDirectory()).toDom());
						}
						// since we cant have 2 outputs for the same dep, break
						// out of the loop
						break;
					}
				}
				if (!foundMatching) {
					config.addChild(MojoExecutor.element("outputDirectory",
					      defaultDistribDirectoryPath + File.separator + defaultOutputDirectory).toDom());
				}
			} else {
				// default : send all dependencies to <defaultOutputDirectory>
				// folder (default value is "load").
				config.addChild(MojoExecutor.element("outputDirectory",
				      defaultDistribDirectoryPath + File.separator + defaultOutputDirectory).toDom());
			}
		}
		items.addChild(itemAsDom);
		config.addChild(items);

		MojoExecutor.executeMojo(MojoExecutor.plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.5.1"),
		      MojoExecutor.goal("copy"), config, MojoExecutor.executionEnvironment(project, session, pluginManager));
	}

    private String getDirectoryDestinationForDependency(Dependency dep){
        String directory = defaultDistribDirectoryPath;
        if (outputs != null) {
            // check if there is an output entry for that dependency
            for (Output output : outputs) {
                if (dep.getArtifactId().equals(output.getIncludesArtifactId())) {
                    // found a matching
                    if (output.getDirectory() != null) {
                        directory = defaultDistribDirectoryPath + File.separator + output.getDirectory();
                    }
                    // since we cant have 2 outputs for the same dep, break
                    // out of the loop
                    break;
                }
            }
        }
        return directory;
    }

	private void unZipFile(File zipFile, File outputFolder) {

		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			if (!outputFolder.exists()) {
				outputFolder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder.getAbsolutePath() + File.separator + fileName);

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
