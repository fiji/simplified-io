package com.indago.io;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.StopWatch;
import net.imglib2.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.function.BiConsumer;

public class SaveImageTests {

	private static String imagesPath =
			System.getProperty( "user.dir" ) + File.separatorChar + "test-images";

	@BeforeClass
	public static void init() throws URISyntaxException {
		silenceNoisyLibraryLoggers();
	}

	private static void silenceNoisyLibraryLoggers() {
		LoggerContext loggerContext = ( LoggerContext ) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger( "loci.formats" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "loci.common" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "org.scijava.nativelib" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "ome.xml.model.enums.handlers" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "org.bushe.swing" );
		rootLogger.setLevel( Level.OFF );
	}

	@Test
	public void testSaveTifImageWithJ1() throws IOException {

		testSaveImage( "tif", ImageJIOUtils::saveImageWithIJ1 );
	}

	@Test
	public void testSaveJpgImageWithJ1() throws IOException {

		testSaveImage( "jpg", ImageJIOUtils::saveImageWithIJ1 );
	}

	@Test
	public void testSavePngImageWithJ1() throws IOException {

		testSaveImage( "png", ImageJIOUtils::saveImageWithIJ1 );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private void testSaveImage( String outExt, BiConsumer< ImgPlus< ? >, String > saveImageFunction ) throws IOException {

		Collection< File > images = listImageFiles();
		for ( File image : images ) {
			testSaveImage( image, outExt, saveImageFunction );
		}
	}

	private void testSaveImage( File image, String outExt, BiConsumer< ImgPlus< ? >, String > saveImageFunction ) throws IOException
	{
		File outputFile = File.createTempFile( "test", "." + outExt );
		outputFile.deleteOnExit();
		String outputFilePath = outputFile.getPath();
		ImgPlus< ? > originalImage = ImageJIOUtils.openImage( image);
		System.out.println( "Writing:" + image.getName() );
		StopWatch watch = StopWatch.createAndStart();
		saveImageFunction.accept( originalImage, outputFilePath );
		ImgPlus< ? > savedImage = ImageJIOUtils.openImage( outputFile );
		System.out.println( "Time elapsed " + watch.toString() );
		assertImageEquals( originalImage, savedImage );
	}

	private void assertImageEquals( ImgPlus< ? > expected, ImgPlus< ? > actual )
	{
		if ( isRealType( expected ) && isRealType( actual ) )
			ImgLib2Assert.assertImageEqualsRealType( ( RandomAccessibleInterval ) Utils.ensureXYCZT( expected ), ( RandomAccessibleInterval ) Utils.ensureXYCZT( actual ), 0.0 );
		else
			ImgLib2Assert.assertImageEquals( Utils.ensureXYCZT( expected ), Utils.ensureXYCZT( actual ), Object::equals );
	}

	private boolean isRealType( ImgPlus<?> expected )
	{
		return Util.getTypeFromInterval(expected) instanceof RealType;
	}

	private static Collection< File > listImageFiles()
	{
		final IOFileFilter zips = new IOFileFilter() {

			@Override
			public boolean accept(File file) {
				return !file.getName().endsWith( "zip" ) && !file.isDirectory() && !file.getName().startsWith( "." );
			}

			@Override
			public boolean accept( File dir, String name ) {
				return !name.endsWith( "zip" );
			}
		};

		return FileUtils.listFiles( new File( imagesPath ), zips, null );
	}
}
