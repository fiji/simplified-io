package com.indago.io;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.StopWatch;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(Parameterized.class)
public class LoadImageTests
{

	private final File imageFile;

	private static String imagesPath = System.getProperty("user.dir") + File.separatorChar + "test-images";

	@Parameterized.Parameters( name = "{0}" )
	public static Collection<File> data() {
		return listImageFiles();
	}

	@BeforeClass
	public static void init() throws URISyntaxException {
		silenceNoisyLibraryLoggers();
		unzipImages();
	}

	private static void silenceNoisyLibraryLoggers()
	{
		LoggerContext loggerContext = ( LoggerContext ) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger( "loci.formats" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "loci.common" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "org.scijava.nativelib" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "ome.xml.model.enums.handlers" );
		rootLogger.setLevel( Level.OFF );
	}

	private static void unzipImages()
	{
		Collection< File > files =
				FileUtils.listFiles( new File( imagesPath ), new String[] { "zip" }, false );
		System.out.println( "Unzipping " + files.size() + " images" );
		for ( File file : files ) {
			try {
				ZipFile zipFile = new ZipFile( file.getAbsolutePath() );
				zipFile.extractAll( imagesPath );
			} catch ( ZipException e ) {
				System.out.println( e.getMessage() );
			}
		}
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

	public LoadImageTests( File imageFile ) {
		this.imageFile = imageFile;
	}

	@Ignore( "This is not expected to work for all images. ")
	@Test
	public void testLoadImageWithIJ1() {
		testLoadImage( "testLoadImagesWithIJ1", file -> ImageJIOUtils.loadImageWithIJ1( file ) );
	}

	@Ignore( "This is not expected to work for all images. ")
	@Test
	public void testLoadImageWithSCIFIO() {
		testLoadImage( "testLoadImagesWithSCIFIO", file -> ImageJIOUtils.loadImageWithSCIFIO( file ) );
	}

	@Ignore( "This is not expected to work for all images. ")
	@Test
	public void testLoadImageWithBioFormats() {
		testLoadImage( "testLoadImagesWithBioFormats", file -> ImageJIOUtils.loadImageWithBioFormats( file ) );
	}

	@Test
	public void testLoadImage() {
		testLoadImage( "testLoadImages", file -> ImageJIOUtils.loadImage( file ) );
	}

	@Test
	public void testLoadImageWithRealType() {
		ImgPlus< DoubleType > image = ImageJIOUtils.loadImage( imageFile , new DoubleType() );
		assertEquals( true, image.firstElement() instanceof DoubleType );
	}

	@Test
	public void testLoadImageWithARGBType() {
		ImgPlus< ARGBType > image = ImageJIOUtils.loadImage( imageFile , new ARGBType() );
		assertEquals( true, image.firstElement() instanceof ARGBType );
	}

	@SuppressWarnings( "rawtypes" )
	private void testLoadImage( String title, Function< File, ImgPlus > loadImageFunction )
	{
		System.out.println( "******* " + title );
		System.out.println( "Reading:" + imageFile.getName() );
		StopWatch watch = StopWatch.createAndStart();
		ImgPlus img2 = loadImageFunction.apply( imageFile );
		System.out.println( "Time elapsed " + watch.toString() );
		assertNotNull( img2 );
	}

}
