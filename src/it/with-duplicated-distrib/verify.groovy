/**
	This script must test that duplicatre bundles must be keet if they are not in file install directories (load2).
*/
import java.io.*
import java.security.MessageDigest;
import java.util.zip.ZipFile;
import java.security.DigestInputStream;

def artifactNameWithoutExtension = "target/with-duplicated-distrib-1.0-SNAPSHOT"
def distribDirectory = "target/with-duplicated-distrib"

// check the zip distribution
def dist = new File (basedir, distribDirectory);
assert dist.exists();
assert dist.canRead();

def list = ["bin", "conf", "bundle", "load2"]
def dirNumber = 0

def fileInstallNumber = 0

dist.eachFile{dir->
	if (list.contains(dir.getName()) && dir.isDirectory()) {
		dirNumber++
		println "Directory " + dir.getName()
		dir.eachFile{file->
			println "---> " + file.getName()
			if (file.getName().equals("org.apache.felix.fileinstall-3.1.4.jar")) {
				fileInstallNumber++
			}
		}		
	}
}

// all directories in base distribution (3) + load directory must be found
assert dirNumber == 4

// file install bundle must be found twice
assert fileInstallNumber == 2

