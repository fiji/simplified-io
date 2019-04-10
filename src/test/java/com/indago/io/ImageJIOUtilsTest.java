package com.indago.io;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.imagej.ImgPlus;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
 public class ImageJIOUtilsTest { 
	private static String imagesPath = System.getProperty("user.dir") + File.separatorChar + "test-images";
	private static Collection< File > tbp = null;
	private static int ntot = 0;

	@BeforeClass
	public static void init() throws URISyntaxException {
		// Silence noisy library loggers
		LoggerContext loggerContext = ( LoggerContext ) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger( "loci.formats" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "loci.common" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "org.scijava.nativelib" );
		rootLogger.setLevel( Level.OFF );
		rootLogger = loggerContext.getLogger( "ome.xml.model.enums.handlers" );
		rootLogger.setLevel( Level.OFF );
		// Unzip zipped images
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
		
		// Get a list of all the images to be processed. Yes, zips can be processed but take too long
		final IOFileFilter zips = new IOFileFilter() {
		    public boolean accept(File file) {
		        return !file.getName().endsWith( "zip" ) && !file.isDirectory() && !file.getName().startsWith( "." );
		    }

			@Override
			public boolean accept( File dir, String name ) {
		        return !name.endsWith( "zip" );
			}
		};

		tbp = FileUtils.listFiles( new File( imagesPath ), zips, null );
		ntot = tbp.size();
	}

	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesWithIJ1() {
		System.out.println( "******* testLoadImagesWithIJ1" );
		int cntr = 0;
		for ( File file : tbp ) {
			System.out.println( "Reading:" + file.getName() );
			long start = System.nanoTime();
			ImgPlus img1 = ImageJIOUtils.loadImageWithIJ1( file );
			long finish = System.nanoTime();
			long timeElapsed = finish - start;
			if ( img1 != null ) {
				System.out.println( "Time elapsed ij1 " + timeElapsed );
				cntr++;
				//if (!GraphicsEnvironment.isHeadless())ImageJFunctions.show((RandomAccessibleInterval) img1 );
			}
		}
		System.out.println( ntot + " file found, " + cntr + " processed" );
	}

	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesWithIJ() {
		System.out.println( "******* testLoadImagesWithIJ" );
		int cntr = 0;
		for ( File file : tbp ) {
			System.out.println( "Reading:" + file.getName() );
			long start = System.nanoTime();
			ImgPlus img2 = ImageJIOUtils.loadImageWithIJ( file, null );
			long finish = System.nanoTime();
			long timeElapsed = finish - start;
			if ( img2 != null ) {
				System.out.println( "Time elapsed scifio " + timeElapsed );
				cntr++;
				//if (!GraphicsEnvironment.isHeadless())ImageJFunctions.show((RandomAccessibleInterval) img2 );
			}
		}
		System.out.println( ntot + " file found, " + cntr + " processed" );
	}

	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesWithBioFormats() {
		System.out.println( "******* testLoadImagesWithBioFormats" );
		int cntr = 0;
		for ( File file : tbp ) {
			System.out.println( "Reading:" + file.getName() );
			long start = System.nanoTime();
			ImgPlus img2 = ImageJIOUtils.loadImageWithBioFormats( file );
			long finish = System.nanoTime();
			long timeElapsed = finish - start;
			if ( img2 != null ) {
				System.out.println( "Time elapsed " + timeElapsed );
				cntr++;
				//if (!GraphicsEnvironment.isHeadless()) ImageJFunctions.show((RandomAccessibleInterval) img2 );
			}
		}
		System.out.println( ntot + " file found, " + cntr + " processed" );
	}

	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesChooseIJ1() {
		System.out.println( "******* testLoadImagesChooseIJ1" );
		int cntr = 0;
		for ( File file : tbp ) {
			System.out.println( "Reading:" + file.getName() );
			long start = System.nanoTime();
			ImgPlus img2 = ImageJIOUtils.loadImage( file, true );
			long finish = System.nanoTime();
			long timeElapsed = finish - start;
			if ( img2 != null ) {
				System.out.println( "Time elapsed " + timeElapsed );
				cntr++;
				//if (!GraphicsEnvironment.isHeadless()) ImageJFunctions.show((RandomAccessibleInterval) img2 );
			}
		}
		System.out.println( ntot + " file found, " + cntr + " processed" );
	}

	@SuppressWarnings( { "rawtypes" } )
	@Test
	public void testLoadImagesChooseIJ() {
		System.out.println( "******* testLoadImagesChooseIJ" );
		int cntr = 0;
		for ( File file : tbp ) {
			System.out.println( "Reading:" + file.getName() );
			long start = System.nanoTime();
			ImgPlus img2 = ImageJIOUtils.loadImage( file, false );
			long finish = System.nanoTime();
			long timeElapsed = finish - start;
			if ( img2 != null ) {
				System.out.println( "Time elapsed " + timeElapsed );
				cntr++;
				//if (!GraphicsEnvironment.isHeadless()) ImageJFunctions.show((RandomAccessibleInterval) img2 );
			}
		}
		System.out.println( ntot + " file found, " + cntr + " processed" );
	}

}
