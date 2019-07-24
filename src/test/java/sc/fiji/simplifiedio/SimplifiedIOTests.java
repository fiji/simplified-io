package sc.fiji.simplifiedio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.StopWatch;
import net.imglib2.util.Util;
import sc.fiji.simplifiedio.util.SortAxesUtils;

@RunWith( Parameterized.class )
public class SimplifiedIOTests extends SimplifiedIOTestBase {

	private final File imageFile;

	@Parameterized.Parameters
	public static Collection< File > data() {
		return listImageFiles();
	}

	private static Collection< File > listImageFiles() {
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

		return FileUtils.listFiles( new File( TEST_IMAGES_DIR ), zips, null );
	}

	public SimplifiedIOTests( File imageFile ) {
		this.imageFile = imageFile;
	}

	@Ignore( "This is not expected to work for all images. " )
	@Test
	public void testLoadImageWithIJ1() {
		testLoadImage( "testLoadImagesWithIJ1", SimplifiedIO::openImageWithIJ1 );
	}

	@Ignore( "This is not expected to work for all images. " )
	@Test
	public void testLoadImageWithSCIFIO() {
		testLoadImage( "testLoadImagesWithSCIFIO", SimplifiedIO::openImageWithSCIFIO );
	}

	@Ignore( "This is not expected to work for all images. " )
	@Test
	public void testLoadImageWithBioFormats() {
		testLoadImage( "testLoadImagesWithBioFormats", SimplifiedIO::openImageWithBioFormats );
	}

	@Test
	public void testLoadImage() {
		testLoadImage( "testLoadImages", SimplifiedIO::openImage );
	}

	@Test
	public void testLoadImageWithRealType() {
		ImgPlus< DoubleType > image = SimplifiedIO.openImage( imageFile.getAbsolutePath(), new DoubleType() );
		assertEquals( true, image.firstElement() instanceof DoubleType );
	}

	@Test
	@Ignore
	public void testLoadImageWithARGBType() {
		ImgPlus< ARGBType > image = SimplifiedIO.openImage( imageFile.getAbsolutePath(), new ARGBType() );
		assertEquals( true, image.firstElement() instanceof ARGBType );
	}

	@SuppressWarnings( "rawtypes" )
	private void testLoadImage( String title, Function< String, ImgPlus > loadImageFunction ) {
		System.out.println( "******* " + title );
		System.out.println( "Reading:" + imageFile.getName() );
		StopWatch watch = StopWatch.createAndStart();
		ImgPlus img2 = loadImageFunction.apply( imageFile.getAbsolutePath() );
		System.out.println( "Time elapsed " + watch.toString() );
		assertNotNull( img2 );
		assertNotNull( img2.factory() );
	}

	@Test
	public void testSaveTifImage() throws IOException {
		testSaveImage( imageFile, "tif", SimplifiedIO::saveImage );
	}

	@Ignore
	@Test
	public void testSaveJpgImage() throws IOException {
		testSaveImage( imageFile, "jpg", SimplifiedIO::saveImage );
	}

	@Ignore
	@Test
	public void testSavePngImage() throws IOException {
		testSaveImage( imageFile, "png", SimplifiedIO::saveImage );
	}

	private void testSaveImage( File image, String outExt, BiConsumer< ImgPlus< ? >, String > saveImageFunction ) throws IOException {
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
