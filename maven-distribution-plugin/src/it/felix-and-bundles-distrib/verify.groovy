import java.io.*
import java.security.MessageDigest;
import java.util.zip.ZipFile;
import java.security.DigestInputStream;

def felixOriginalHash = "3371237ec5b6bf185772e989bc11148f8072a40c";
def fileInstallOriginalHash = "2f977393f724ac5b3c9e56eaf7d40ee34b8ea630";
def artifactNameWithoutExtension = "target/felix-and-bundles-distrib-1.0-SNAPSHOT"

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

assert zipFile.getEntry("bin").isDirectory();
assert zipFile.getEntry("felix.jar") != null;

// get inputStream of fileinstall
try{
    def fileInstallIs = zipFile.getInputStream(zipFile.getEntry("fileinstall-3.2.6.jar"));
    fileInstallIs = new DigestInputStream(fileInstallIs, md);
}   finally {
    fileInstallIs.close();
}
def fileinstallDigest = new String(md.digest());
assert fileinstallDigest.equals(fileInstallOriginalHash);

assert zipFile.getEntry("bundles").isDirectory();
assert zipFile.getEntry("fileinstall-3.2.6.jar") != null;
