
package org.osgi.distribution.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;

/**
 * Zip all pom resources content in a .zip file
 * 
 * @goal zip
 * @execute phase="prepare-package"
 * @phase package
 */
public class ZipMojo
    extends AbstractMojo
{

	/** 
	 * @parameter expression="${project.artifactId}"
	 */
	protected String defaultDistribDirectoryName;
	
	public String defaultDistribDirectoryPath;
	
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    
    public static final String[] DEFAULT_EXCLUDES = { 
        // Miscellaneous typical temporary files 
        "**/*~", 
        "**/#*#", 
        "**/.#*", 
        "**/%*%", 
        "**/._*", 

        // CVS 
        "**/CVS", 
        "**/CVS/**", 
        "**/.cvsignore", 

        // SCCS 
        "**/SCCS", 
        "**/SCCS/**", 

        // Visual SourceSafe 
        "**/vssver.scc", 

        // Subversion 
        "**/.svn", 
        "**/.svn/**", 

        // Arch/Bazaar 
        "**/.arch-ids", "**/.arch-ids/**", 

        // Mac 
        "**/.DS_Store", 
        
        // Zip and rar and tmp
        "**/*.zip", "**/*.rar", "**/*.tmp"
    }; 

    private static final String[] DEFAULT_INCLUDES = new String[]{"**/**"}; 

    

    /** 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
    	defaultDistribDirectoryPath = this.project.getBuild().getDirectory() + File.separator + defaultDistribDirectoryName;
    	
        if ( this.getLog().isDebugEnabled() )
        {
            this.getLog()
                .debug( "this.getProject().getResources().size() : " + this.getProject().getResources().size() );
        }
        if ( this.getProject().getResources().size() < 1 )
        {
            this.getLog().error( "No resources provided nothing is made" );
            return;
        }
        try
        {
            this.performArchive();
        }
        catch ( Exception e )
        {
            this.getLog().error( "trouble during performingArchive", e );
        }
    }

    /**
     * @throws Exception
     */
    private void performArchive()
        throws ArchiverException, IOException
    {
        File zipFile = new File( this.getProject().getBuild().getDirectory(), this.getProject().getBuild().getFinalName()
            + ".zip" );
        ZipArchiver archiver = new ZipArchiver();
        
        archiver.setDestFile( zipFile );
        archiver.setIncludeEmptyDirs( true );
        archiver.setCompress( true );
        File distribDirectory = createTemporalDirectory() ;
        //createTemporalDirectory() ;
        if (distribDirectory.exists() && distribDirectory.isDirectory()){
        	this.getLog().info("adding the directory : " + distribDirectory.getAbsolutePath());
        	
        	archiver.addDirectory(distribDirectory, DEFAULT_INCLUDES, DEFAULT_EXCLUDES);
        }
        archiver.createArchive();
        getProject().getArtifact().setFile( zipFile );
    }

    private File createTemporalDirectory() throws IOException{
    	String distrib_temp = this.project.getBuild().getDirectory() + File.separator + "distrib-temp" ;
    	String distrib_temp2 = distrib_temp +  File.separator + defaultDistribDirectoryName;
    	
    	FileUtils.mkdir(distrib_temp);
    	FileUtils.mkdir(distrib_temp2);
    	File distrib = new File(defaultDistribDirectoryPath);
    	File tempDistrib = new File(distrib_temp2);
		FileUtils.copyDirectoryStructure(distrib, tempDistrib);
		return new File (distrib_temp);
    }
    
    public MavenProject getProject()
    {
        return project;
    }

    public void setProject( MavenProject project )
    {
        this.project = project;
    }

}
