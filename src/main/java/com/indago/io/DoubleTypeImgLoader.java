/**
 *
 */
package com.indago.io;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

/**
 * @author jug
 *
 */
public class DoubleTypeImgLoader {

	public static Img< DoubleType > loadTiff( final File file ) {
//	    ALERT: THOSE FOLLOWING TWO LINES CAUSE THREAD LEAK!!!!
//		final ImgFactory< DoubleType > imgFactory = new ArrayImgFactory< DoubleType >();
//		final ImgOpener imageOpener = new ImgOpener();

		IndagoLog.log.info( "Loading file '" + file.getName() + "' ..." );
//		final List< SCIFIOImgPlus< FloatType >> imgs = imageOpener.openImgs( file.getAbsolutePath(), imgFactory, new DoubleType() );
//		final Img< RealType > img = imgs.get( 0 ).getImg();
		final Img< DoubleType > img =
				ImagePlusAdapter.wrapReal( IJ.openImage( file.getAbsolutePath() ) );

		return img;
	}

	public static RandomAccessibleInterval< DoubleType > loadTiffEnsureType( final File file ) {
		final Img< DoubleType > img = loadTiff( file );

		final RandomAccessibleInterval< DoubleType > ret = copyToDouble( img );
		return ret;
	}

	public static < T extends NumericType< T > > ImgPlus< DoubleType > wrapEnsureType( final ImgPlus< T > imgPlus ) {
		final Img< DoubleType > newImg = copyToDouble( imgPlus.getImg() );
		final ImgPlus< DoubleType > result = new ImgPlus<>( newImg, imgPlus.getName() );
		for ( int i = 0; i < result.numDimensions(); i++ )
			result.setAxis( imgPlus.axis( i ), i );
		return result;
	}

	public static RandomAccessibleInterval< DoubleType > wrapEnsureType( final ImagePlus imagePlus ) {
		final Img< DoubleType > img =
				ImagePlusAdapter.wrapReal( imagePlus );

		final RandomAccessibleInterval< DoubleType > ret = copyToDouble( img );
		return ret;
	}

	/**
	 * @param img
	 * @return
	 */
	private static < T extends NumericType< T > > Img< DoubleType > copyToDouble( final Img< T > img ) {
		final long dims[] = new long[ img.numDimensions() ];
		img.dimensions( dims );
		final Img< DoubleType > ret =
				new ArrayImgFactory< DoubleType >().create( dims, new DoubleType() );
		final IterableInterval< DoubleType > iterRet = Views.iterable( ret );
		try {
			DataMover.convertAndCopy( Views.extendZero( img ), iterRet );
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
		return ret;
	}

}
