import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest;
import java.util.zip.ZipFile;

def felixOriginalHash = "3371237ec5b6bf185772e989bc11148f8072a40c";
def artifactNameWithoutExtension = "target/auto-load-distrib-1.0-SNAPSHOT"

// check the zip distribution
def dist = new File (basedir, artifactNameWithoutExtension + ".zip");
assert dist.exists();
assert dist.canRead();

MessageDigest md = MessageDigest.getInstance("SHA1");

// get inputStream of felix jar
def zipFile = new ZipFile(dist);
try {
    def felixIs = zipFile.getInputStream(zipFile.getEntry("felix.jar"));
    felixIs = new DigestInputStream(felixIs, md);
} finally {
    felixIs.close();
}
def digest = new String(md.digest());
assert digest.equals(felixOriginalHash);

assert zipFile.getEntry("load") != null;
assert zipFile.getEntry("load").isDirectory();
assert zipFile.getEntry("felix.jar") != null;