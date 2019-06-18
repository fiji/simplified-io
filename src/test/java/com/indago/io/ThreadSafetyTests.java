package com.indago.io;

import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Write and read a thousand different images in parallel,
 * and check if the content is correct. This is done to
 * ensure that threads writing and reading at the same
 * time don't interfere with each other.
 */
public class ThreadSafetyTests {

	private Map< ImgPlus< ? >, File > imagesAndFiles = initImageAndFiles();

	private Map<ImgPlus<?>,File> initImageAndFiles()
	{
		try
		{
			Map< ImgPlus< ? >, File > result = new HashMap<>();
			for ( int i = 0; i < 1000; i++ ) {
				File outputFile = File.createTempFile( "testTreadSafety",".tif" );
				outputFile.deleteOnExit();
				result.put( createImage(i), outputFile );
			}
			return result;
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private ImgPlus< ? > createImage( int i )
	{
		Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 400, 320 );
		ImgPlus< UnsignedByteType > imgPlus = new ImgPlus<>( img );
		RandomImgs.seed( i ).randomize( imgPlus );
		return imgPlus;
	}

	@Test
	public void test() throws IOException, ExecutionException, InterruptedException
	{
		System.out.println( "Run write threads" );
		writeParallel();
		System.out.println( "Finished all writing threads, run read threads" );
		readParallel();
		System.out.println( "Finished all reading threads, test terminated successfully" );

	}

	private void writeParallel()
	{
		ExecutorService executor = Executors.newFixedThreadPool( 5 );
		for ( Map.Entry< ImgPlus< ? >, File > entry : imagesAndFiles.entrySet() ) {
			Runnable worker = new MultiImageWriter( entry.getKey(), entry.getValue() );
			executor.execute( worker );
		}
		executor.shutdown();
		// Wait until all tasks are finish
		while ( !executor.isTerminated() ) {}
	}

	private void readParallel() throws ExecutionException, InterruptedException
	{
		ExecutorService executor = Executors.newFixedThreadPool( 5 );
		List< Future< ? >> futures = new ArrayList<>(  );
		imagesAndFiles.forEach( ( image, file ) -> {
			Runnable worker = new MultiImageReader( image, file );
			futures.add( executor.submit( worker ) );
		} );
		// Wait until all tasks are finish
		for( Future< ? > future : futures )
			future.get();
		executor.shutdown();
	}
}

class MultiImageWriter implements Runnable {

	private final File file;
	private final ImgPlus< ? > imgPlus;

	public MultiImageWriter( ImgPlus< ? > imgPlus, File files ) {
		this.imgPlus = imgPlus;
		this.file = files;
	}

	public void run() {
		String outputFilePath = file.getPath();
		ImageJIOUtils.saveImage( imgPlus, outputFilePath );
	}
}

class MultiImageReader implements Runnable {

	private final File file;
	private final ImgPlus< ? > imgPlus;

	public MultiImageReader( ImgPlus< ? > imgPlus, File file ) {
		this.imgPlus = imgPlus;
		this.file = file;
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public void run() {
		ImgPlus< ? > readImage = ImageJIOUtils.loadImage( file );
		ImgLib2Assert.assertImageEquals( ( Img ) readImage.getImg(), ( Img ) imgPlus.getImg() );
	}
}
