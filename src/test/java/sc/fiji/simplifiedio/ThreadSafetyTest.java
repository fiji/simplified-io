/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2019 - 2020 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package sc.fiji.simplifiedio;

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

import org.junit.jupiter.api.Test;

import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Write and read a thousand different images in parallel,
 * and check if the content is correct. This is done to
 * ensure that threads writing and reading at the same
 * time don't interfere with each other.
 */
public class ThreadSafetyTest {

	private Map< ImgPlus< ? >, File > imagesAndFiles = initImageAndFiles();

	@Test
	public void test() throws IOException, ExecutionException, InterruptedException {
		System.out.println( "Run write threads" );
		writeParallel();
		System.out.println( "Finished all writing threads, run read threads" );
		readParallel();
		System.out.println( "Finished all reading threads, test terminated successfully" );

	}

	private void writeParallel() {
		ExecutorService executor = Executors.newFixedThreadPool( 5 );
		for ( Map.Entry< ImgPlus< ? >, File > entry : imagesAndFiles.entrySet() ) {
			Runnable worker = new MultiImageWriter( entry.getKey(), entry.getValue() );
			executor.execute( worker );
		}
		executor.shutdown();
		// Wait until all tasks are finish
		while ( !executor.isTerminated() ) {}
	}

	private void readParallel() throws ExecutionException, InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool( 5 );
		List< Future< ? > > futures = new ArrayList<>();
		imagesAndFiles.forEach( ( image, file ) -> {
			Runnable worker = new MultiImageReader( image, file );
			futures.add( executor.submit( worker ) );
		} );
		// Wait until all tasks are finish
		for ( Future< ? > future : futures )
			future.get();
		executor.shutdown();
	}

	private Map< ImgPlus< ? >, File > initImageAndFiles() {
		try {
			Map< ImgPlus< ? >, File > result = new HashMap<>();
			for ( int i = 0; i < 1000; i++ ) {
				File outputFile = File.createTempFile( "testTreadSafety", ".tif" );
				outputFile.deleteOnExit();
				result.put( createImage( i ), outputFile );
			}
			return result;
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	private ImgPlus< ? > createImage( int i ) {Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 400, 320 );
		ImgPlus< UnsignedByteType > imgPlus = new ImgPlus<>( img );
		RandomImgs.seed( i ).randomize( imgPlus );
		return imgPlus;
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
			SimplifiedIO.saveImage( imgPlus, outputFilePath );
		}
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
		ImgPlus< ? > readImage = SimplifiedIO.openImage( file.getAbsolutePath() );
		ImgLib2Assert.assertImageEquals( ( Img ) readImage.getImg(), ( Img ) imgPlus.getImg() );
	}
}
