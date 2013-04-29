import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

def artifactNameWithoutExtension = "target/rights-distrib-1.0-SNAPSHOT"
def distribDirectory = "target/osgi-distribution"

// check the zip distribution
def dist = new File (basedir, distribDirectory);
assert dist.exists();
assert dist.canRead();


def File toto = new File(dist, "toto.txt"); 
assert  toto != null;
assert !toto.canExecute();

def File titi = new File(dist, "titi.bat");
assert  titi != null;
assert titi.canExecute();

def File tutu = new File(dist, "tutu");
assert  tutu != null;
assert tutu.canExecute();