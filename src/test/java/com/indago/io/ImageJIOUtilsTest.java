package com.indago.io;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ImageJIOUtilsTest
{
	@Test( expected = ImageOpenException.class )
	public void testMissingFile() throws IOException
	{
		File file = File.createTempFile( "image-", ".tif" );
		ImageJIOUtils.loadImage( file );
	}

	@Test( expected = ImageOpenException.class )
	public void testEmptyFile() throws IOException
	{
		File file = File.createTempFile( "image-", ".tif" );
		file.createNewFile();
		file.deleteOnExit();
		ImageJIOUtils.loadImage( file );
	}
}
