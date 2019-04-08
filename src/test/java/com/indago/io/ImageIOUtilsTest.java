package com.indago.io;

import java.io.File;

import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import net.imglib2.img.Img;

public class ImageIOUtilsTest {
	
	@Test
	public void testLoadImages() {
		Collection< File > files = FileUtils.listFiles( new File("/Users/turek/Downloads/bioimages/misc"), null, false);
		for (File file: files) {
			System.out.println("Reading:" + file.getName());
			long start = System.nanoTime();
			Img<?> img1 = ImageIOUtils.loadImageWithIJ1( file );
			long finish = System.nanoTime();
			long timeElapsed = finish - start;
			System.out.println("Time elapsed ij1 " + timeElapsed );
			if (img1 == null) System.out.println("null image"); 
			start = System.nanoTime();
			Img<?> img2 = ImageIOUtils.loadImageWithIJ( file, null );
			finish = System.nanoTime();
			timeElapsed = finish - start;
			System.out.println("Time elapsed scifio " + timeElapsed );
			if (img2 == null) System.out.println("null image"); 
			else System.out.println( img2.getClass().toGenericString() );
		}
	}
	
	@Test
	public void testLoadImagesWithBioFormats() {
		Collection< File > files = FileUtils.listFiles( new File("/Users/turek/Downloads/bioimages/bioformats"), null, false);
		for (File file: files) {
			System.out.println("Reading:" + file.getName());
			long start = System.nanoTime();
			Img<?> img2 = ImageIOUtils.loadImageWithBioFormats( file );
			long finish = System.nanoTime();
			long timeElapsed = finish - start;
			System.out.println("Time elapsed " + timeElapsed );
			if (img2 == null) System.out.println("null image"); 
			else System.out.println( img2.getClass().toGenericString() );
		}
	}
	
}
