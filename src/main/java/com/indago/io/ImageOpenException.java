package com.indago.io;

/**
 * Runtime exception for fatal errors encountered while
 * opening an image (e.g. IOException, FileNotFoundException, etc)
 */
public class ImageOpenException extends RuntimeException
{
	private static final long serialVersionUID = 349763419228666057L;

	public ImageOpenException( Exception cause ) {
		super( cause );
	}

	public ImageOpenException( String message )
	{
		super( message );
	}
}
