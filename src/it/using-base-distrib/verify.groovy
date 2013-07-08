/**
	This script must test that extending other distribution works right
*/
import java.io.*
import java.security.MessageDigest;
import java.util.zip.ZipFile;
import java.security.DigestInputStream;

def artifactNameWithoutExtension = "target/using-base-distrib-1.0-SNAPSHOT"
def distribDirectory = "target/using-base-distrib"

def dist = new File (basedir, distribDirectory);
assert dist.exists();
assert dist.canRead();

// bin, conf and bundle are directories of base distribution. load is added in the current distribution
def list = ["bin", "conf", "bundle", "load"]
def dirNumber = 0

dist.eachFile{file->
	if (list.contains(file.getName())) {
		dirNumber++
	}
  println file
}

// all directories in distribution
assert dirNumber == 4
