package sc.fiji.simplifiedio;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImageOpenExceptionTest {

	@Test
	public void testMissingFile() throws IOException {
		Assertions.assertThrows( SimplifiedIOException.class, () -> {
			File file = File.createTempFile( "image-", ".tif" );
			SimplifiedIO.openImage( file.getAbsolutePath() );
		} );
	}

	@Test
	public void testEmptyFile() throws IOException {
		Assertions.assertThrows( SimplifiedIOException.class, () -> {
			File file = File.createTempFile( "image-", ".tif" );
			file.createNewFile();
			file.deleteOnExit();
			SimplifiedIO.openImage( file.getAbsolutePath() );
		} );
	}
}
