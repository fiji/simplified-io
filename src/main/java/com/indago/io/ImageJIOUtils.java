package com.indago.io;

import ij.ImagePlus;
import ij.Macro;
import ij.io.Opener;
import ij.plugin.ImagesToStack;
import io.scif.SCIFIO;
import io.scif.bf.BioFormatsFormat;
import io.scif.services.DatasetIOService;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.img.ImagePlusAdapter;
import org.scijava.Context;
import org.scijava.log.DefaultLogger;
import org.scijava.log.LogLevel;
import org.scijava.log.LogSource;
import org.scijava.log.Logger;

import java.io.File;
import java.io.IOException;

public class ImageJIOUtils {

	private static Logger log =
			new DefaultLogger( System.out::print, LogSource.newRoot(), LogLevel.INFO ).subLogger(
					"IndagoIO" );

	private static ImageReader imgReader = null;

	private static SCIFIO scifio;

	private static SCIFIO getScifio() {
		if( scifio == null )
			scifio = new SCIFIO();
		return scifio;
	}

	/**
	 * Loads an image using ImageJ1, then wraps it into an ImgPlus object
	 * Returns null if the image is not in a supported format.
	 * Quick and dirty it either works or does not
	 *
	 * @see net.imagej.ImgPlus
	 * @see ij.IJ#openImage(String)
	 */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public static ImgPlus loadImageWithIJ1( final File imgFile ) {
		final Opener opnr = new Opener();
		ImagePlus image = opnr.openImage( imgFile.getAbsolutePath() );
		if ( image == null ) return null;
		return new ImgPlus( ImagePlusAdapter.wrapImgPlus( image ) );
	}

	/**
	 * 
	 * Loads an image using SCIFIO
	 * 
	 * @return ImgPlus object
	 * @see net.imagej.ImgPlus
	 * @see org.scijava.Context
	 */
	@SuppressWarnings( "rawtypes" )
	public static ImgPlus loadImageWithSCIFIO( final File imgFile ) {
		try {
			SCIFIO scifio = getScifio();
			Dataset dataset = scifio.datasetIO().open( imgFile.getAbsolutePath() );
			return dataset.getImgPlus();
		} catch ( IOException e ) {
			log.info( "Error loading file " + imgFile.getName() + ": " + e.getMessage() );
			return null;
		}
	}

	/**
	 * Loads an image using BioFormats
	 * 
	 * @see net.imagej.ImgPlus
	 */
	@SuppressWarnings( "rawtypes" )
	public static ImgPlus loadImageWithBioFormats( final File imgFile ) {
		if ( imgReader == null ) {
			BioFormatsFormat bff = new BioFormatsFormat();
			imgReader = bff.createImageReader();
		}
		try {
			imgReader.setId( imgFile.getAbsolutePath() );
			ImporterOptions options = new ImporterOptions();
			if ( Macro.getOptions() == null ) {
				options.loadOptions();
			}
			options.parseArg( imgFile.getAbsolutePath() );
			options.checkObsoleteOptions();
			ImportProcess process = new ImportProcess( options );
			process.execute();
			ImagePlusReader reader = new ImagePlusReader( process );
			ImagePlus[] imps = reader.openImagePlus();
			ImagePlus finalImage = ImagesToStack.run( imps );
			return ImagePlusAdapter.wrapImgPlus( finalImage );
		} catch ( FormatException | IOException e ) {
			log.info( "Error loading file " + imgFile.getName() + ": " + e.getMessage() );
			return null;
		}
	}

	/**
	 * Loads an image into an ImgPlus object
	 * 
	 * @see net.imagej.ImgPlus
	 */
	@SuppressWarnings( "rawtypes" )
	public static ImgPlus loadImage( final File imgFile ) {
		ImgPlus img = ImageJIOUtils.loadImageWithIJ1( imgFile );
		if ( img == null )
			img = ImageJIOUtils.loadImageWithSCIFIO( imgFile );

		if ( img == null )
			img = ImageJIOUtils.loadImageWithBioFormats( imgFile );
		return img;
	}
}
