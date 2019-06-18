package com.indago.io;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ImageOpenExceptionTests {

	@Test( expected = ImageOpenException.class )
	public void testMissingFile() throws IOException {
		File file = File.createTempFile( "image-", ".tif" );
		ImageJIOUtils.openImage( file );
	}

	@Test( expected = ImageOpenException.class )
	public void testEmptyFile() throws IOException {
		File file = File.createTempFile( "image-", ".tif" );
		file.createNewFile();
		file.deleteOnExit();
		ImageJIOUtils.openImage( file );
	}
}
