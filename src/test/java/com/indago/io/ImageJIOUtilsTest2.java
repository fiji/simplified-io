package com.indago.io;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ImageJIOUtilsTest2
{
	@Test( expected = ImageOpenException.class )
	public void testException() throws IOException
	{
		File file = File.createTempFile( "image-", ".dummy" );
		ImageJIOUtils.loadImage( file );
	}
}
