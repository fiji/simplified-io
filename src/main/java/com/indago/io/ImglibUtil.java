package com.indago.io;

import java.util.Iterator;

import org.scijava.log.Logger;

import net.imagej.ImgPlus;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;

/**
* @author jug
*/
public class ImglibUtil {

	/**
	 * Compute the min and max for any {@link Iterable}, like an {@link Img}.
	 *
	 * The only functionality we need for that is to iterate. Therefore we need
	 * no {@link net.imglib2.Cursor} that can localize itself, neither do we
	 * need a {@link net.imglib2.RandomAccess}. So we simply use the
	 * most simple interface in the hierarchy.
	 *
	 * @param iterableInterval
	 *            the input that has to just be {@link Iterable}
	 * @param min
	 *            the type that will have min
	 * @param max
	 *            the type that will have max
	 * @param <T>
	 *            pixel type
	 */
	public static < T extends RealType< T > & NativeType< T > > void computeMinMax(
			final IterableInterval< T > iterableInterval,
			final T min,
			final T max ) {
		if ( iterableInterval == null ) { return; }

		// create a cursor for the image (the order does not matter)
		final Iterator< T > iterator = iterableInterval.iterator();

		// initialize min and max with the first image value
		T type = iterator.next();

		min.set( type );
		max.set( type );

		// loop over the rest of the data and determine min and max value
		while ( iterator.hasNext() ) {
			// we need this type more than once
			type = iterator.next();

			if ( type.compareTo( min ) < 0 ) min.set( type );

			if ( type.compareTo( max ) > 0 ) max.set( type );
		}
	}

	/**
	 * Determines and outputs some metadata-info about a given ImgPlus.
	 *
	 * @param log
	 *            the Logger to use to output the information about the giben
	 *            ImgPlus
	 * @param imgPlus
	 *            the ImgPlus you want to know more about
	 */
	public static < T extends Type< T > > void logImgPlusFacts( final Logger log, final ImgPlus< T > imgPlus ) {
		log.info( String.format( "Image name:      %s", imgPlus.getName() ) );
		log.info( String.format( "Pixel type:      %s", imgPlus.firstElement().getClass().getName() ) );

		log.info( String.format( "Num dimesnsions: %s", imgPlus.numDimensions() ) );
		final long[] dims = new long[ imgPlus.numDimensions() ];
		imgPlus.dimensions( dims );
		String dimsString = "[";
		for ( int i = 0; i < imgPlus.numDimensions() - 1; i++ )
			dimsString += dims[ i ] + ",";
		dimsString += dims[ dims.length - 1 ] + "]";
		log.info( String.format( "Total size:      %s", dimsString ) );
		for ( int i = 0; i < imgPlus.numDimensions(); i++ ) {
			log.info( String.format( "Dimension %d is: %s", i, ( imgPlus.axis( i ).type().isSpatial() ) ? "spatial" : "other" ) );
			log.info( String.format( "            in: %s", imgPlus.axis( i ).unit() ) );
			log.info( String.format( "       labeled: %s", imgPlus.axis( i ).type().getLabel() ) );
			log.info( String.format( "        length: %d", dims[ i ] ) );
			log.info( String.format( "       min_idx: %d", imgPlus.min( i ) ) );
			log.info( String.format( "       max_idx: %d", imgPlus.max( i ) ) );
		}
	}

	public static < T extends Type< T > > int getNumberOfSpatialDimensions( final ImgPlus< T > imgPlus ) {
		int spatialDims = 0;
		for ( int i = 0; i < imgPlus.numDimensions(); i++ ) {
			spatialDims += imgPlus.axis( i ).type().isSpatial() ? 1 : 0;
		}
		return spatialDims;
	}

	public static < T extends Type< T > > int getTimeDimensionIndex( final ImgPlus< T > imgPlus ) {
		for ( int i = 0; i < imgPlus.numDimensions(); i++ ) {
			if ( imgPlus.axis( i ).type().getLabel().equals( "Time" ) ) { return i; }
		}
		return -1;
	}

	public static < T extends Type< T > > boolean hasTimeDimension( final ImgPlus< T > imgPlus ) {
		return ( getTimeDimensionIndex( imgPlus ) == -1 ) ? false : true;
	}

	public static < T extends Type< T > > long getNumFrames( final ImgPlus< T > imgPlus ) {
		if ( hasTimeDimension( imgPlus ) ) {
			return imgPlus.dimension( getTimeDimensionIndex( imgPlus ) );
		} else {
			return -1;
		}
	}
	public static < T extends Type< T > > int getChannelDimensionIndex( final ImgPlus< T > imgPlus ) {
		for ( int i = 0; i < imgPlus.numDimensions(); i++ ) {
			if ( imgPlus.axis( i ).type().getLabel().equals( "Channel" ) ) { return i; }
		}
		return -1;
	}

	public static < T extends Type< T > > boolean hasChannelDimension( final ImgPlus< T > imgPlus ) {
		return ( getTimeDimensionIndex( imgPlus ) == -1 ) ? false : true;
	}

	public static < T extends Type< T > > long getNumChannels( final ImgPlus< T > imgPlus ) {
		if ( hasChannelDimension( imgPlus ) ) {
			return imgPlus.dimension( getChannelDimensionIndex( imgPlus ) );
		} else {
			return -1;
		}
	}

}
