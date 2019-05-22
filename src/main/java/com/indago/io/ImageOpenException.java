package com.indago.io;

import java.io.IOException;

public class ImageOpenException extends RuntimeException
{
	public ImageOpenException( Exception cause ) {
		super( cause );
	}

	public ImageOpenException( String message )
	{
		super( message );
	}
}
