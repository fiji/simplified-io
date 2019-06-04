package com.indago.io;

/**
 * Runtime exception for fatal errors encountered while
 * saving an image (e.g. IOException, unsupported format, etc)
 */
public class ImageWriteException extends RuntimeException
{
	private static final long serialVersionUID = 349763419228666057L;

	public ImageWriteException( Exception cause ) {
		super( cause );
	}

	public ImageWriteException( String message )
	{
		super( message );
	}
}
