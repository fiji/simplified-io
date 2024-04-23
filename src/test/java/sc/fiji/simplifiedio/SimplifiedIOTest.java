/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2019 - 2024 Fiji developers.
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.StopWatch;
import net.imglib2.util.Util;
import sc.fiji.simplifiedio.util.SortAxesUtils;

public class SimplifiedIOTest extends SimplifiedIOTestBase {

	private static Stream< File> listImageFiles() {
		final IOFileFilter zips = new IOFileFilter() {

			@Override
			public boolean accept( File file ) {
				return !file.getName().endsWith( "zip" ) && !file.isDirectory() && !file.getName().startsWith( "." );
			}

			@Override
			public boolean accept( File dir, String name ) {
				return !name.endsWith( "zip" );
			}
		};

		return FileUtils.listFiles( new File( TEST_IMAGES_DIR ), zips, null ).stream();
	}

	@Disabled( "This is not expected to work for all images. " )
	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testLoadImageWithIJ1( File imageFile ) {
		loadImage( imageFile, "testLoadImagesWithIJ1", SimplifiedIO::openImageWithIJ1 );
	}

	@Disabled( "This is not expected to work for all images. " )
	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testLoadImageWithSCIFIO( File imageFile ) {
		loadImage( imageFile, "testLoadImagesWithSCIFIO", SimplifiedIO::openImageWithSCIFIO );
	}

	@Disabled( "This is not expected to work for all images. " )
	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testLoadImageWithBioFormats( File imageFile ) {
		loadImage( imageFile, "testLoadImagesWithBioFormats", SimplifiedIO::openImageWithBioFormats );
	}

	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testLoadImage( File imageFile ) {
		loadImage( imageFile, "testLoadImages", SimplifiedIO::openImage );
	}

	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testLoadImageWithRealType( File imageFile ) {
		ImgPlus< DoubleType > image = SimplifiedIO.openImage( imageFile.getAbsolutePath(), new DoubleType() );
		assertEquals( true, image.firstElement() instanceof DoubleType );
	}

	@Disabled( "This is not expected to work for all images. " )
	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testLoadImageWithARGBType( File imageFile ) {
		ImgPlus< ARGBType > image = SimplifiedIO.openImage( imageFile.getAbsolutePath(), new ARGBType() );
		assertEquals( true, image.firstElement() instanceof ARGBType );
	}

	@SuppressWarnings( "rawtypes" )
	private void loadImage( File imageFile, String title, Function< String, ImgPlus > loadImageFunction ) {
		System.out.println( "******* " + title );
		System.out.println( "Reading:" + imageFile.getName() );
		StopWatch watch = StopWatch.createAndStart();
		ImgPlus img2 = loadImageFunction.apply( imageFile.getAbsolutePath() );
		System.out.println( "Time elapsed " + watch.toString() );
		assertNotNull( img2 );
		assertNotNull( img2.factory() );
	}

	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testSaveTifImage( File imageFile ) throws IOException {
		saveImage( imageFile, "tif", SimplifiedIO::saveImage );
	}

	@Disabled( "This is not expected to work for all images. " )
	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testSaveJpgImage( File imageFile ) throws IOException {
		saveImage( imageFile, "jpg", SimplifiedIO::saveImage );
	}

	@Disabled( "This is not expected to work for all images. " )
	@ParameterizedTest
	@MethodSource( "listImageFiles" )
	public void testSavePngImage( File imageFile ) throws IOException {
		saveImage( imageFile, "png", SimplifiedIO::saveImage );
	}

	private void saveImage( File image, String outExt, BiConsumer< ImgPlus< ? >, String > saveImageFunction ) throws IOException {
		File outputFile = File.createTempFile( "test", "." + outExt );
		outputFile.deleteOnExit();
		String outputFilePath = outputFile.getPath();
		ImgPlus< ? > originalImage = SimplifiedIO.openImage( image.getAbsolutePath() );
		System.out.println( "Writing:" + image.getName() );
		StopWatch watch = StopWatch.createAndStart();
		saveImageFunction.accept( originalImage, outputFilePath );
		ImgPlus< ? > savedImage = SimplifiedIO.openImage( outputFile.getAbsolutePath() );
		System.out.println( "Time elapsed " + watch.toString() );
		assertImageEquals( originalImage, savedImage );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private void assertImageEquals( ImgPlus< ? > expected, ImgPlus< ? > actual ) {
		if ( isRealType( expected ) && isRealType( actual ) )
			ImgLib2Assert.assertImageEqualsRealType( ( RandomAccessibleInterval ) SortAxesUtils.ensureXYCZT( expected ), ( RandomAccessibleInterval ) SortAxesUtils.ensureXYCZT( actual ), 0.0 );
		else
			ImgLib2Assert.assertImageEquals( SortAxesUtils.ensureXYCZT( expected ), SortAxesUtils.ensureXYCZT( actual ), Object::equals );
	}

	private boolean isRealType( ImgPlus< ? > expected ) {
		return Util.getTypeFromInterval( expected ) instanceof RealType;
	}

}
