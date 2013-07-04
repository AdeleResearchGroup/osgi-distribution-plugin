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
def zipFile = new ZipFile(dist)

// Calculate the digest from the felix zipped file
def felixIs = zipFile.getInputStream(zipFile.getEntry("felix-and-bundles-distrib/bin/felix.jar"));	
felixIs.eachByte(1024) { byte[] buf, int bytesRead ->
	md.update(buf, 0, bytesRead);
}
felixIs.close();	

def byteData =  md.digest()

// Translate byteData checksum from byte[] to String
def sb = new StringBuffer()
byteData.each {
	sb.append(Integer.toString((it & 0xff) + 0x100, 16).substring(1))
}
def digest = sb.toString()

assert digest.equals(felixOriginalHash);

assert zipFile.getEntry("felix-and-bundles-distrib/bin/").isDirectory();
assert zipFile.getEntry("felix-and-bundles-distrib/bin/felix.jar") != null;
assert zipFile.getEntry("felix-and-bundles-distrib/bundles/").isDirectory();
assert zipFile.getEntry("felix-and-bundles-distrib/bundles/org.apache.felix.fileinstall-3.2.6.jar") != null;
