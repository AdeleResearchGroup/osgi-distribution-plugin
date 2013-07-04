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

zipFile.entries().each {
   println it
}

// Calculate the digest from the felix zipped file
def felixIs = zipFile.getInputStream(zipFile.getEntry("auto-load-distrib/load/org.apache.felix.main-4.0.3.jar"));	
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

assert zipFile.getEntry("auto-load-distrib/load/") != null;
assert zipFile.getEntry("auto-load-distrib/load/").isDirectory();
assert zipFile.getEntry("auto-load-distrib/load/org.apache.felix.main-4.0.3.jar") != null;

