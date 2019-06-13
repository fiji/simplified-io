package com.indago.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.BeforeClass;
import org.junit.Test;

import net.imagej.ImgPlus;
import net.imagej.ops.create.imgPlus.DefaultCreateImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class ThreadSafetyTests {

	private static ImgPlus< ? > imgPlus = null;
	private static List< File > files;

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@BeforeClass
	public static void init() {
		ArrayImgFactory< UnsignedByteType > imageFactory =
				new ArrayImgFactory( new UnsignedByteType() );
		Img< UnsignedByteType > img = imageFactory.create( new long[] { 400, 320 } );
		imgPlus = new DefaultCreateImgPlus().calculate( img );

		files = new ArrayList< File >( 1000 );
		for ( int i = 1; i < 6; i++ ) {
			for ( int j = 1; j < 201; j++ ) {
				File outputFile = new File( "/tmp/threadtest" + i + j + ".tif" );
				outputFile.deleteOnExit();
				files.add( outputFile );
			}
		}
	}

	@Test
	public void test() throws IOException {
		System.out.println( "Run write threads" );
		ExecutorService executor = Executors.newFixedThreadPool( 5 );
		for ( int i = 0; i < 5; i++ ) {
			Runnable worker = new MultiImageWriter( imgPlus, files.subList(i*200, (i + 1)*200 - 1  ));
			executor.execute( worker );
		}
		executor.shutdown();
		// Wait until all threads are finish
		while ( !executor.isTerminated() ) {}
		System.out.println( "Finished all writing threads, run read threads" );

		executor = Executors.newFixedThreadPool( 5 );
		for ( int i = 0; i < 5; i++ ) {
			Runnable worker = new MultiImageReader( imgPlus, files.subList(i*200, (i + 1)*200 - 1  ) );
			executor.execute( worker );
		}
		executor.shutdown();
		// Wait until all threads are finish
		while ( !executor.isTerminated() ) {}
		System.out.println( "Finished all reading threads, test terminated successfully" );

	}
}

class MultiImageWriter implements Runnable {

	private Collection< File > files = null;
	private ImgPlus< ? > imgPlus = null;

	public MultiImageWriter( ImgPlus< ? > imgPlus, List<File> files ) {
		this.imgPlus = imgPlus;
		this.files = files;
	}

	public void run() {
		for ( File file : files ) {
			String outputFilePath = file.getPath();
			ImageJIOUtils.saveImage( imgPlus, outputFilePath );
		}
	}
}

class MultiImageReader implements Runnable {

	private Collection< File > files = null;
	private ImgPlus< ? > imgPlus = null;

	public MultiImageReader( ImgPlus< ? > imgPlus, List<File> files) {
		this.imgPlus = imgPlus;
		this.files = files;
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public void run() {
		for ( File file : files ) {
			ImgPlus< ? > readImage = ImageJIOUtils.loadImage( file );
			try {
				ImgLib2Assert.assertImageEquals( ( Img ) readImage.getImg(), ( Img ) imgPlus.getImg() );
			} catch ( ImageWriteException | AssertionError e ) {
				System.out.println( e.getMessage() );
			}
		}
	}
}
