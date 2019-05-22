package com.indago.io;

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
import net.imagej.ImgPlus;
import net.imglib2.img.ImagePlusAdapter;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;

public class ImageJIOUtils {

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
		if ( image == null )
			throw new ImageOpenException( "new ij.io.Opener().openImage() returned null." );
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
			throw new ImageOpenException( e );
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
			throw new ImageOpenException( e );
		}
	}

	/**
	 * Loads an image into an ImgPlus object
	 * 
	 * @see net.imagej.ImgPlus
	 */
	@SuppressWarnings( "rawtypes" )
	public static ImgPlus loadImage( final File imgFile ) {

		if( ! imgFile.exists() )
			throw new ImageOpenException( "Image file doesn't exist: " + imgFile );

		StringJoiner messages = new StringJoiner( "\n" );

		try {
			return ImageJIOUtils.loadImageWithIJ1( imgFile );
		}
		catch ( Exception e ) {
			messages.add( "ImageJ1 Exception: " + e.getMessage() );
		}

		try {
			return ImageJIOUtils.loadImageWithSCIFIO( imgFile );
		}
		catch ( Exception e ) {
			messages.add( "SCIFIO Exception: " + e.getMessage() );
		}

		try {
			return ImageJIOUtils.loadImageWithBioFormats( imgFile );
		}
		catch ( Exception e ) {
			messages.add( "BioFormats Exception: " + e.getMessage() );
		}

		throw new ImageOpenException( "Couldn't open image file: \"" + imgFile + "\"\n" +
				"Exceptions:\n" + messages );
	}
}
