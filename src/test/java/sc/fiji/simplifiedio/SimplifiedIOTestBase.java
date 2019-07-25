package sc.fiji.simplifiedio;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class SimplifiedIOTestBase {

	protected static final String TEST_IMAGES_DIR = System.getProperty( "user.dir" ) + File.separatorChar + "test-images";
	protected static final String TEST_IMAGES_URL = "https://samples.fiji.sc/sio-test-images.zip";
	protected static final String TEST_IMAGES_ZIP = "sio-test-images.zip";
	protected static final String TMP_DIR = System.getProperty( "java.io.tmpdir" );

	@BeforeAll
	public static void setup() {
		File testDir = new File( TEST_IMAGES_DIR );
		silenceNoisyLibraryLoggers();

		// Check if we already downloaded and unpacked the test images
		if ( testDir.exists() && testDir.isDirectory() && testDir.list().length > 0 ) {
			System.out.println( "A non empty \"test-images\" directory was found, assuming all test images are present" );
			return;
		}

		try {
			System.out.println( "No \"test-images\" directory found, downloading and installing test images" );
			downloadAndUnzipTestImages();
		} catch ( InterruptedException | ExecutionException | IOException | ZipException e ) {
			System.out.println( "Failed to source test images: " + e.getMessage() );
			fail();
		}

	}

	private static void downloadAndUnzipTestImages() throws InterruptedException, ExecutionException, IOException, ZipException {

		URL url = new URL( TEST_IMAGES_URL );
		String tmpPath = TMP_DIR + File.separator + TEST_IMAGES_ZIP;
		try (ReadableByteChannel readableByteChannel = Channels.newChannel( url.openStream() )) {
			try (FileOutputStream fileOutputStream = new FileOutputStream( tmpPath )) {
				fileOutputStream.getChannel().transferFrom( readableByteChannel, 0, Long.MAX_VALUE );
			}
			ZipFile zipFile = new ZipFile( tmpPath );
			zipFile.extractAll( System.getProperty( "user.dir" ) );
			unzipSingleImages();
		}
	}

	private static void unzipSingleImages() {
		Collection< File > files =
				FileUtils.listFiles( new File( TEST_IMAGES_DIR ), new String[] { "zip" }, false );
		System.out.println( "Unzipping " + files.size() + " images" );
		for ( File file : files ) {
			try {
				ZipFile zipFile = new ZipFile( file.getAbsolutePath() );
				zipFile.extractAll( TEST_IMAGES_DIR );
			} catch ( ZipException e ) {
				System.out.println( e.getMessage() );
			}
		}
	}

	private static void silenceNoisyLibraryLoggers() {
		LoggerContext loggerContext = ( LoggerContext ) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger( "loci.formats" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "loci.common" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "org.scijava.nativelib" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "ome.xml.model.enums.handlers" );
		rootLogger.setLevel( Level.OFF );
	}

}
