package com.indago.io;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.function.BiConsumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.StopWatch;

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

		testSaveImage( "tif", "testSaveTifImageWithIJ1", ImageJIOUtils::saveImageWithIJ1 );
	}
	
	@Ignore
	@Test
	public void testSaveTifImageWithSCIFIO() throws IOException {

		testSaveImage( "tif", "testSaveTifImageWithSCIFIO", ImageJIOUtils::saveImageWithSCIFIO );
	}

	@Ignore
	@Test
	public void testSaveTifToJpgImageWithJ1() throws IOException {

		testSaveImage( "tif", "jpg", "testSaveImageTifToJpgWithJ1", ImageJIOUtils::saveImageWithIJ1 );
	}
	
	@Test
	public void testSaveTifToPngImageWithJ1() throws IOException {

		testSaveImage( "tif", "png", "testSaveImageTifToPngWithJ1", ImageJIOUtils::saveImageWithIJ1 );
	}

	@Test
	public void testSaveJpgImageWithJ1() throws IOException {

		testSaveImage( "jpg", "testSaveJpgImageWithSCFIO", ImageJIOUtils::saveImageWithIJ1 );
	}
	
	@Test
	@Ignore
	public void testSaveJpgImageWithSCFIO() throws IOException {

		testSaveImage( "jpg", "testSaveJpgImageWithSCIFIO", ImageJIOUtils::saveImageWithSCIFIO );
	}


	@Test
	public void testSaveJpgToTifImageWithJ1() throws IOException {
	
		testSaveImage( "jpg", "tif", "testSaveImageJpgToTifWithJ1", ImageJIOUtils::saveImageWithIJ1 );
	}
	
	@Test
	public void testSaveJpgToPngImageWithJ1() throws IOException {
	
		testSaveImage( "jpg", "png", "testSaveImageJpgToPngWithJ1", ImageJIOUtils::saveImageWithIJ1 );
	}

	private void testSaveImage( String ext, String title, BiConsumer< ImgPlus< ? >, String > saveImageFunction) throws IOException {
		
		testSaveImage( ext, ext, title, saveImageFunction);
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private void testSaveImage( String inExt, String outExt, String title, BiConsumer< ImgPlus< ? >, String > saveImageFunction) throws IOException {

		System.out.println( "****** " + title );
		Collection< File > images = listFiles( inExt );
		for ( File image : images ) {
			File outputFile = File.createTempFile( "test", "." + outExt );
			outputFile.deleteOnExit();
			String outputFilePath = outputFile.getPath();
			ImgPlus< ? > originalImage = ImageJIOUtils.loadImage( image);
			System.out.println( "Writing:" + image.getName() );
			StopWatch watch = StopWatch.createAndStart();
			saveImageFunction.accept( originalImage, outputFilePath );
			ImgPlus< ? > savedImage = ImageJIOUtils.loadImage( outputFile );
			System.out.println( "Time elapsed " + watch.toString() );
			try {
			ImgLib2Assert.assertImageEquals(
					( Img ) savedImage.getImg(),
					( Img ) originalImage.getImg() );
			} catch (ImageWriteException | AssertionError e) {
				System.out.println( e.getMessage() );
			}
		}
	}

	private Collection< File > listFiles( String ext ) {
		final IOFileFilter zips = new IOFileFilter() {

			@Override
			public boolean accept( File file ) {
				return file.getName().endsWith( ext );
			}

			@Override
			public boolean accept( File dir, String name ) {
				return name.endsWith( ext );
			}
		};

		return FileUtils.listFiles( new File( imagesPath ), zips, null );
	}
}
