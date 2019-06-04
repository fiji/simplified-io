package com.indago.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.imagej.ImgPlus;
import net.imagej.ops.create.imgPlus.DefaultCreateImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class ThreadSafetyTests {

	private static Collection< File > files = new ArrayList< File >( 1000 );

	@BeforeClass
	private static void init() throws IOException {
		File outputFile = File.createTempFile( "threadtest", ".tif" );
		outputFile.deleteOnExit();
		files.add( outputFile );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Test
	public void testCreateFiles() throws IOException {
		ArrayImgFactory< UnsignedByteType > imageFactory =
				new ArrayImgFactory( new UnsignedByteType() );
		Img< UnsignedByteType > img = imageFactory.create( new long[] { 400, 320 } );
		ImgPlus imgPlus = new DefaultCreateImgPlus().calculate( img );
		for ( File file : files ) {
			String outputFilePath = file.getPath();
			ImageJIOUtils.saveImage( imgPlus, outputFilePath );
		}
	}

	@Test
	public void testReadFiles() {
		for ( File file : files ) {
			ImageJIOUtils.loadImage( file);
		}
	}
}
