[![Build Status](https://github.com/fiji/simplified-io/actions/workflows/build.yml/badge.svg)](https://github.com/fiji/simplified-io/actions/workflows/build.yml)

Simplified-IO
=============
Read, write, type convert images using simple methods.

There are currently at least 3 ways to open an image in ImageJ: 

	- using the ij library (aka ImageJ1)
	- using the SCIFIO library (part of ImageJ2)
	- using the BioFormats library (for instrument specific images)
	
When opening images with Fiji this is handled behind the scene for the user, including having to switch between legacy
ImageJ1 ImagePlus objects and ImageJ2 ImgPlus objects.
However there are plenty of situations (like writing scripts or plug-ins) where one has to deal with image reading and writing directly.

This small library attempts to hide some of the complexities by standardizing on the use of ImgPlus objects as input and output to very
simple open and save methods. There are 4 public static methods available:

To read: 

`ImgPlus<?> readImage = SimplifiedIO.openImage( "/path/to/myimage.tif" );`

To read and convert data type in one step:

`ImgPlus< DoubleType > readImageDouble = SimplifiedIO.openImage( "/path/to/myimage.tif" , new DoubleType() );`

To save (the saved image type is dictated by the file name extension):

`SimplifiedIO.saveImage( readImage, "/path/to/mynewimage.tif" );`

Additionally, a type conversion method is also available:

`ImgPlus< DoubleType > readImageDouble = SimplifiedIO.convert( readImage, new DoubleType() );`

All methods throw a runtime SimplifiedIOException which covers fatal errors encountered while opening or saving an image (e.g. FileNotFoundException, IOException, unsupported format, etc).

A number of test images of various formats were used for testing. When you run the tests for the first time, a zipped file with all the test images will be downloaded from the web and
the images installed where expected by the tests. This may take some time.

How to use in your own code?
============================

Simplified-IO is intended for use in your SciJava projects, e.g. your Fiji Plugins.

If you don't know better, you should start with a <code>pom.xml</code> that uses <code>pom-scijava</code> as parent POM.

Within it you can then include the following dependency in order to enable Simplified-IO:
```
<dependency>
    <groupId>sc.fiji</groupId>
    <artifactId>simplified-io</artifactId>
    <version>1.0.1</version>
</dependency>
```

_Footnote:_ please determine the latest version via https://maven.scijava.org/#nexus-search;quick~simplified-io. At some point in the future we hope that the SciJava parent POM will take care of this and you can drop the version-tag altogether.

This all means little to you? You might want to check out https://imagej.net/Learnathon 
