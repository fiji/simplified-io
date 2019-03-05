/**
 *
 */
package com.indago.io;

import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgView;
import net.imglib2.type.numeric.RealType;

/**
 * Since loading and saving is still a hard thing to do right and the way to do
 * it using IJ(1/2)/Fiji changes constantly (plus there are shitloads of bugs),
 * I need to make this additional layer of abstraction. Tadaaa!
 *
 * @author jug
 */
public class ImageSaver {

	// Needed for current workaround... sorry!
//	public static Context context = new Context( OpService.class, OpMatchingService.class,
//			IOService.class, DatasetIOService.class, LocationService.class,
//			DatasetService.class, ImgUtilityService.class, StatusService.class,
//			TranslatorService.class, QTJavaService.class, TiffService.class,
//			CodecService.class, JAIIIOService.class );

	/**
	 *
	 * @param filename
	 * @param rai
	 */
	public static < T extends RealType< T > > void saveAsTiff(
			final String filename,
			final RandomAccessibleInterval< T > rai ) {

		// What I would like to do but breaks currently as soon as you are within Fiji/Imagej2 (e.g. as Command)
//		IO.saveImg( filename, img );

		try {
			new ImgSaver().saveImg( filename, ImgView.wrap( rai, null ) );
		} catch ( ImgIOException | IncompatibleTypeException e ) {
			e.printStackTrace();
		}

		// The only workaround I know works at the moment (2016-08-05)
//		if ( context == null ) IndagoLog.log.error( "Static field 'context' was not set before using ImageSaver..." );
//		final DatasetService datasetService = context.getService( DatasetService.class );
//		final Dataset dataset = datasetService.create( rai );
//		final DatasetIOService service = context.getService( DatasetIOService.class );
//		try {
//			service.save( dataset, filename );
//		} catch ( final IOException exc ) {
//			exc.printStackTrace();
//		}
	}
}
