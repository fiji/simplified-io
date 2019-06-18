package com.indago.io;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;

import org.scijava.util.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.io.Opener;
import ij.plugin.ImagesToStack;
import io.scif.SCIFIO;
import loci.formats.FormatException;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

public class ImageJIOUtils {

	private static SCIFIO scifio;

	/**
	 * Loads an image using ImageJ1, then wraps it into an ImgPlus object
	 * Returns null if the image is not in a supported format.
	 * Quick and dirty it either works or does not
	 *
	 * @see net.imagej.ImgPlus
	 * @see ij.IJ#openImage(String)
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	static ImgPlus openImageWithIJ1( final String path ) {
		// package private to allow testing
		final Opener opnr = new Opener();
		ImagePlus image = opnr.openImage( path );
		if ( image == null )
			throw new ImageOpenException( "new ij.io.Opener().openImage() returned null." );
		return ImagePlusAdapter.wrapImgPlus( image );
	}

	/**
	 *
	 * Loads an image using SCIFIO
	 *
	 * @return ImgPlus object
	 * @see net.imagej.ImgPlus
	 * @see org.scijava.Context
	 */
	@SuppressWarnings( "rawtypes" )
	static ImgPlus openImageWithSCIFIO( final String path ) {
		// package private to allow testing
		try {
			SCIFIO scifio = getScifio();
			Dataset dataset = scifio.datasetIO().open( path );
			return dataset.getImgPlus();
		} catch ( IOException e ) {
			throw new ImageOpenException( e );
		}
	}

	/**
	 * Loads an image using BioFormats
	 *
	 * @see net.imagej.ImgPlus
	 */
	@SuppressWarnings( "rawtypes" )
	static ImgPlus openImageWithBioFormats( final String path ) {
		// package private to allow testing
		try {
			ImporterOptions options = new ImporterOptions();
			if ( Macro.getOptions() == null ) {
				options.loadOptions();
			}
			options.parseArg( path );
			options.checkObsoleteOptions();
			ImportProcess process = new ImportProcess( options );
			process.execute();
			ImagePlusReader reader = new ImagePlusReader( process );
			ImagePlus[] imps = reader.openImagePlus();
			ImagePlus finalImage = ImagesToStack.run( imps );
			return ImagePlusAdapter.wrapImgPlus( finalImage );
		} catch ( FormatException | IOException e ) {
			throw new ImageOpenException( e );
		}
	}

	/**
	 * Loads an image into an ImgPlus object
	 *
	 * @see net.imagej.ImgPlus
	 */
	@SuppressWarnings( "rawtypes" )
	public static ImgPlus openImage( final String path ) {

		StringJoiner messages = new StringJoiner( "\n" );

		try {
			return ImageJIOUtils.openImageWithIJ1( path );
		} catch ( Exception e ) {
			messages.add( "ImageJ1 Exception: " + e.getMessage() );
		}

		try {
			return ImageJIOUtils.openImageWithSCIFIO( path );
		} catch ( Exception e ) {
			messages.add( "SCIFIO Exception: " + e.getMessage() );
		}

		try {
			return ImageJIOUtils.openImageWithBioFormats( path );
		} catch ( Exception e ) {
			messages.add( "BioFormats Exception: " + e.getMessage() );
		}

		if ( !new File(path).exists() )
			throw new ImageOpenException( "Image file doesn't exist: " + path );

		throw new ImageOpenException( "Couldn't open image file: \"" + path + "\"\n" + "Exceptions:\n" + messages );
	}

	public static < T extends NativeType< T > > ImgPlus< T > openImage( String path, T type ) {
		return convert( openImage( path ), type );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static < T extends NativeType< T > > ImgPlus< T > convert( ImgPlus image, T type ) {
		Object imageType = Util.getTypeFromInterval( image );
		if ( imageType.getClass().equals( type.getClass() ) ) {
			return image;
		} else if ( imageType instanceof RealType && type instanceof RealType ) {
			return convertBetweenRealType( image, ( RealType ) type );
		} else if ( imageType instanceof UnsignedByteType && type instanceof ARGBType ) {
			return convertUnsignedByteTypeToARGBType( image );
		} else if ( imageType instanceof ARGBType && type instanceof RealType ) {
			return convertARGBTypeToRealType( image, ( RealType ) type );
		}
		throw new IllegalStateException( "Cannot convert between given pixel types: " + imageType.getClass().getSimpleName() + ", " + type.getClass().getSimpleName() );
	}

	/**
	 * Saves the specified image to the specified file path.
	 * The specified image is saved as a "tif" if there is no extension.
	 * <p>
	 * The method also accepts {@link ImgPlus} and stores the metadata.
	 **/
	public static void saveImage( RandomAccessibleInterval< ? > img, String path ) {
		path = addTifAsDefaultExtension( path );
		IJ.save( ImgToVirtualStack.wrap( toImgPlus( img ) ), path );
	}

	private static <T> ImgPlus<T> toImgPlus( RandomAccessibleInterval<T> image )
	{
		if( image instanceof ImgPlus )
			return ( ImgPlus<T> ) image;
		if( image instanceof Img )
			return new ImgPlus<>( ( Img< T > ) image );
		return new ImgPlus<>( ImgView.wrap( (RandomAccessibleInterval) image, null ));
	}

	private static String addTifAsDefaultExtension( String path )
	{
		String ext = FileUtils.getExtension( path ).toLowerCase();

		if ( ext.isEmpty() ) {
			path += ".tif";
		}
		return path;
	}

	private static ImgPlus< ARGBType >
			convertUnsignedByteTypeToARGBType( ImgPlus< UnsignedByteType > image ) {
		if ( ImgPlusViews.canFuseColor( image ) )
			return ImgPlusViews.fuseColor( image );
		RandomAccessibleInterval< ARGBType > convertedRAI = Converters.convertRAI(
				image.getImg(),
				( i, o ) -> {
					int value = i.get();
					o.set( ARGBType.rgba( value, value, value, 255 ) );
				},
				new ARGBType() );
		Img< ARGBType > convertedImg = ImgView.wrap( convertedRAI, null );
		return new ImgPlus<>( convertedImg, image );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static < T extends NativeType< T > > ImgPlus< T >
			convertBetweenRealType( ImgPlus image, RealType type ) {
		RandomAccessibleInterval< T > convertedRAI = RealTypeConverters.convert( image, type );
		Img< T > convertedImg = ImgView.wrap( convertedRAI, null );
		return new ImgPlus<>( convertedImg, image );
	}

	private static < T extends RealType< T > > ImgPlus< T >
			convertARGBTypeToRealType( ImgPlus< ARGBType > image, T type ) {
		RandomAccessibleInterval< T > convertedRAI =
				RealTypeConverters.convert( Converters.argbChannels( image ), type );
		Img< T > convertedImg = ImgView.wrap( convertedRAI, null );
		int n = image.numDimensions();
		CalibratedAxis[] axis = new CalibratedAxis[ n + 1 ];
		for ( int i = 0; i < n; i++ ) {
			axis[ i ] = image.axis( i );
		}
		axis[ n ] = new DefaultLinearAxis( Axes.CHANNEL );
		return new ImgPlus<>( convertedImg, image.getName(), axis );
	}

	private static SCIFIO getScifio() {
		if ( scifio == null )
			scifio = new SCIFIO();
		return scifio;
	}
}
