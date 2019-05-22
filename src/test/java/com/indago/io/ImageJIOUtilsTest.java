package com.indago.io;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.imagej.ImgPlus;
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

import static org.testng.AssertJUnit.assertNotNull;

@RunWith(Parameterized.class)
public class ImageJIOUtilsTest {

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

	public ImageJIOUtilsTest( File imageFile ) {
		this.imageFile = imageFile;
	}

	@Ignore( "This is not expected to work for all images. ")
	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesWithIJ1() {
		testLoadImages( "testLoadImagesWithIJ1", file -> ImageJIOUtils.loadImageWithIJ1( file ) );
	}

	@Ignore( "This is not expected to work for all images. ")
	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesWithIJ() {
		testLoadImages( "testLoadImagesWithIJ", file -> ImageJIOUtils.loadImageWithIJ( file, null ) );
	}

	@Ignore( "This is not expected to work for all images. ")
	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesWithBioFormats() {
		testLoadImages( "testLoadImagesWithBioFormats", file -> ImageJIOUtils.loadImageWithBioFormats( file ) );
	}

	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesChooseIJ1() {
		testLoadImages( "testLoadImagesChooseIJ1", file -> ImageJIOUtils.loadImage( file, true ) );
	}

	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesChooseIJ() {
		testLoadImages( "testLoadImagesChooseIJ", file -> ImageJIOUtils.loadImage( file, false ) );
	}

	private void testLoadImages( String title, Function< File, ImgPlus > loadImageFunction )
	{
		System.out.println( "******* " + title );
		System.out.println( "Reading:" + imageFile.getName() );
		long start = System.nanoTime();
		ImgPlus img2 = loadImageFunction.apply( imageFile );
		long finish = System.nanoTime();
		long timeElapsed = finish - start;
		assertNotNull( img2 );
		System.out.println( "Time elapsed " + timeElapsed );
	}

}
