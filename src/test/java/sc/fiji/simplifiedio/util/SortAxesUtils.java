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
package sc.fiji.simplifiedio.util;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SortAxesUtils
{
	public static < T > RandomAccessibleInterval< T > ensureXYCZT( final ImgPlus< T > imgPlus )
	{
		final int[] axes = getPermutation( getAxes( imgPlus ) );
		return permute( imgPlus, axes );
	}

	private static int[] getPermutation( final List< AxisType > axes )
	{
		return axes.stream().mapToInt( axis -> {
			final int index = imagePlusAxisOrder.indexOf( axis );
			if ( index < 0 )
				throw new IllegalArgumentException( "Unsupported axis type: " + axis );
			return index;
		} ).toArray();
	}

	private static List< AxisType > getAxes( final ImgPlus< ? > imgPlus )
	{
		return IntStream.range( 0, imgPlus.numDimensions() )
				.mapToObj( i -> imgPlus.axis( i ).type() )
				.collect( Collectors.toList() );
	}

	private static < T > RandomAccessibleInterval< T > permute( final ImgPlus< T > imgPlus, int[] axes )
	{
		boolean inNaturalOrder = true;
		final boolean[] matchedDimensions = new boolean[ 5 ];
		final long[] min = new long[ 5 ], max = new long[ 5 ];
		for ( int d = 0; d < axes.length; d++ )
		{
			final int index = axes[ d ];
			matchedDimensions[ index ] = true;
			min[ index ] = imgPlus.min( d );
			max[ index ] = imgPlus.max( d );
			if ( index != d )
				inNaturalOrder = false;
		}

		if ( imgPlus.numDimensions() != 5 )
			inNaturalOrder = false;
		if ( inNaturalOrder )
			return imgPlus;

		axes = Arrays.copyOf( axes, 5 );
		RandomAccessibleInterval< T > rai = imgPlus;
		// pad the image to at least 5D
		for ( int i = 0; i < 5; i++ )
		{
			if ( matchedDimensions[ i ] )
				continue;
			axes[ rai.numDimensions() ] = i;
			min[ i ] = 0;
			max[ i ] = 0;
			rai = Views.addDimension( rai, 0, 0 );
		}

		// permute the axis order to XYCZT...
		final MixedTransform t = new MixedTransform( rai.numDimensions(), 5 );
		t.setComponentMapping( axes );
		return Views.interval( new MixedTransformView<>( rai, t ), min, max );
	}

	private static final List< AxisType > imagePlusAxisOrder =
			Arrays.asList( Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME );
}
