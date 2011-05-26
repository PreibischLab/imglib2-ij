package net.imglib2.img.display.imagej;

import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.RandomAccessibleZeroMinIntervalCursor;

public class ImageJVirtualStackFloat< S > extends ImageJVirtualStack< S, FloatType >
{
	public ImageJVirtualStackFloat( RandomAccessibleInterval< S > source, Converter< S, FloatType > converter )
	{
		super( source, converter, new FloatType(), ImagePlus.GRAY32 );
		setMinMax( source, converter );
	}
	
	public void setMinMax ( final RandomAccessibleInterval< S > source, final Converter< S, FloatType > converter )
	{		
		final RandomAccessibleZeroMinIntervalCursor< S > cursor = new RandomAccessibleZeroMinIntervalCursor< S >( source );
		final FloatType t = new FloatType();
		
		if ( cursor.hasNext() ) 
		{
			converter.convert( cursor.next(), t );
			
			float min = t.get();
			float max = min; 
			
			while ( cursor.hasNext() ) 
			{
				converter.convert( cursor.next(), t );
				final float value = t.get();
				
				if ( value < min )
					min = value;

				if ( value > max )
					max = value;
			}

			System.out.println("fmax = " + max );
			System.out.println("fmin = " + min );
			imageProcessor.setMinAndMax( min, max );
		}
	}
}
