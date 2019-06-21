[![](https://travis-ci.com/fiji/simplified-io.svg?branch=master)](https://travis-ci.com/fiji/simplified-io)

Simplified-IO
=============
Read, write, type convert images using simple methods.

There are currently at least 3 ways to open an image in ImageJ: 

	- using the ij library (aka ImageJ1)
	- using the SCIFIO library (part of ImageJ2)
	- using the BioFormats library (for instrument specific images)
	
When opening images with Fiji this is handled behind the scene for the user, including having to switch between legacy
ImageJ1 ImagePlus objects and ImageJ2 ImgPlus objects.
However there are plenty of situations (like writing scripts or plugins) where one has to deal with image reading and writing directly.

This small library attempts to hide some of the complexities by standardizing on the use of ImgPlus objects as input and output to very
simple open and save methods:

To read: 

`ImgPlus<?> readImage = SimplifiedIO.openImage( "/path/to/myimage.tif" );`

To read and convert data type in one step:

`ImgPlus< DoubleType > readImageDouble = SimplifiedIO.openImage( "/path/to/myimage.tif" , new DoubleType() );`

To save (the saved image type is dictated by the file name extension):

`SimplifiedIO.saveImage( readImage, "/path/to/mynewimage.tif" );`

Additionally, a type conversion method is also available:

`ImgPlus< DoubleType > readImageDouble = SimplifiedIO.convert( readImage, new DoubleType() );`

A number of test images of various formats were used for testing. For convenience, these can be found in the test-images directory.
