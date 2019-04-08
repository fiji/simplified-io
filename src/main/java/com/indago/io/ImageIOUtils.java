package com.indago.io;

import java.io.File;
import java.io.IOException;

import org.scijava.Context;

import ij.ImagePlus;
import ij.Macro;
import ij.io.Opener;
import ij.plugin.ImagesToStack;
import io.scif.SCIFIO;
import io.scif.bf.BioFormatsFormat;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import net.imagej.Dataset;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;

public class ImageIOUtils {

	private static ImageReader imgReader = null;

	/**
	 * Loads an image using ImageJ1, then wraps it into an ImgPlus object
	 * Returns null if the image is not in a supported format.
	 * Quick and dirty it either works or does not
	 * 
	 * @param input
	 *            File
	 * @see ij.IJ#openImage(String)
	 */
	@SuppressWarnings( "unchecked" )
	public static < T > Img< T > loadImageWithIJ1( final File imgFile) {
		final Opener opnr = new Opener();
		ImagePlus image = opnr.openImage( imgFile.getAbsolutePath() );
		if ( image == null ) return null;
		return ( Img< T > ) ImagePlusAdapter.wrapImgPlus( image );
	}

	/**
	 * 
	 * Loads an image using ImageJ (actually scifio)
	 * 
	 * @param input
	 *            File
	 */
	@SuppressWarnings( "unchecked" )
	public static < T > Img< T > loadImageWithIJ( final File imgFile, Context context ) {
		final SCIFIO scifio = new SCIFIO(context);
		Dataset dataset = null;
		try {
			dataset = scifio.datasetIO().open( imgFile.getAbsolutePath() );
			return ( Img< T > ) dataset.getImgPlus();
		} catch ( IOException e ) {
			System.out.println( e.getMessage() );
			return null;
		}
	}

	/**
	 * Loads an image using BioFormats
	 * 
	 * @param input
	 *            File
	 * @return appropriate reader for the input file's format
	 */
	@SuppressWarnings( "unchecked" )
	public static < T > Img<T> loadImageWithBioFormats( final File imgFile ) {
		if ( imgReader == null ) { 
			BioFormatsFormat bff = new BioFormatsFormat();
			imgReader = bff.createImageReader();
		}
		try {
			imgReader.setId( imgFile.getAbsolutePath() );
			ImporterOptions options = new ImporterOptions();
		    if (Macro.getOptions() == null) {
		        options.loadOptions();
		      }
		    options.parseArg(imgFile.getAbsolutePath());
		    options.checkObsoleteOptions();
		    ImportProcess process = new ImportProcess(options);
		    process.execute();
		    ImagePlusReader reader = new ImagePlusReader(process);
		    ImagePlus[] imps = reader.openImagePlus();
		    ImagePlus finalImage = ImagesToStack.run(imps);
		    return ( Img< T > ) ImagePlusAdapter.wrapImgPlus( finalImage );
		} catch ( FormatException | IOException e ) {
			System.out.println( e.getMessage() );
			return null;
		}
	}
	
	public static <T> Img<T> loadImage( final File imgFile, boolean useij1) {
		Img< T > img = null;
		if (useij1)
			img = ImageIOUtils.loadImageWithIJ1(imgFile);
		else
			img = ImageIOUtils.loadImageWithIJ(imgFile, null);
		
		if (img == null)
			img =  ImageIOUtils.loadImageWithBioFormats( imgFile );
		return img;
	}
}
