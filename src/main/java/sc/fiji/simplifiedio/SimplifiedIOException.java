package sc.fiji.simplifiedio;

/**
 * Runtime exception for fatal errors encountered while
 * opening or saving an image (e.g. FileNotFoundException, IOException, unsupported format, etc)
 */
public class SimplifiedIOException extends RuntimeException
{
	private static final long serialVersionUID = 349763419228666057L;

	public SimplifiedIOException( Exception cause ) {
		super( cause );
	}

	public SimplifiedIOException( String message )
	{
		super( message );
	}
}
