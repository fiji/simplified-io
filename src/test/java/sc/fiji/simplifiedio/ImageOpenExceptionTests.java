package sc.fiji.simplifiedio;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import sc.fiji.simplifiedio.SimplifiedIO;
import sc.fiji.simplifiedio.SimplifiedIOException;

public class ImageOpenExceptionTests {

	@Test( expected = SimplifiedIOException.class )
	public void testMissingFile() throws IOException {
		File file = File.createTempFile( "image-", ".tif" );
		SimplifiedIO.openImage( file.getAbsolutePath() );
	}

	@Test( expected = SimplifiedIOException.class )
	public void testEmptyFile() throws IOException {
		File file = File.createTempFile( "image-", ".tif" );
		file.createNewFile();
		file.deleteOnExit();
		SimplifiedIO.openImage( file.getAbsolutePath() );
	}
}
