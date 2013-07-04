import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

def artifactNameWithoutExtension = "target/rights-distrib-1.0-SNAPSHOT"
def distribDirectory = "target/rights-distrib"

// check the zip distribution
def dist = new File (basedir, distribDirectory);
assert dist.exists();
assert dist.canRead();


def File titi = new File(dist, "titi.bat");
assert  titi != null;
assert titi.canExecute();

def File tutu = new File(dist, "tutu");
assert  tutu != null;
assert tutu.canExecute();

def File tete = new File(dist, "tete.sh");
assert  tete != null;
assert tete.canExecute();
