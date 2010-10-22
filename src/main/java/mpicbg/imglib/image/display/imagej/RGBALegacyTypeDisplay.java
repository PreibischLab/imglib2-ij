/**
 * Copyright (c) 2009--2010, Stephan Preibisch & Stephan Saalfeld
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.  Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.  Neither the name of the Fiji project nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Stephan Preibisch & Stephan Saalfeld
 */
package mpicbg.imglib.image.display.imagej;

import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.display.Display;
import mpicbg.imglib.type.numeric.RGBALegacyType;

public class RGBALegacyTypeDisplay extends Display<RGBALegacyType>
{
	public RGBALegacyTypeDisplay( final Image<RGBALegacyType> img)
	{
		super(img);
		this.min = 0;
		this.max = 255;
	}	

	final protected float avg( final int col )
	{
		final int r = RGBALegacyType.red( col );
		final int g = RGBALegacyType.green( col );
		final int b = RGBALegacyType.blue( col );
		
		return ( 0.3f * r + 0.6f * g + 0.1f * b );
	}	
	
	final protected int max( final int col )
	{
		final int r = RGBALegacyType.red( col );
		final int g = RGBALegacyType.green( col );
		final int b = RGBALegacyType.blue( col );
		
		return Math.max( Math.max ( r,g ), b);
	}

	final protected int min( final int col )
	{
		final int r = RGBALegacyType.red( col );
		final int g = RGBALegacyType.green( col );
		final int b = RGBALegacyType.blue( col );

		return Math.min( Math.min( r,g ), b);
	}
	
	@Override
	public void setMinMax()
	{
		final Cursor<RGBALegacyType> c = img.createCursor();
		final RGBALegacyType t = c.getType();
		
		if ( !c.hasNext() )
		{
			min = 0;
			max = 255;
			return;
		}
		
		c.fwd();
		
		min = min( t.get() );
		max = max( t.get() );

		while ( c.hasNext() )
		{
			c.fwd();
			
			final int value = t.get();
			final int minValue = min( value );
			final int maxValue = max( value );

			if ( maxValue > max )
				max = maxValue;

			if ( value < minValue )
				min = minValue;
		}
		
		c.close();
	}
	
	@Override
	public float get32Bit( RGBALegacyType c ) { return avg( c.get() ); }
	@Override
	public float get32BitNormed( RGBALegacyType c ) { return normFloat( avg( c.get() ) ); }
	
	@Override
	public byte get8BitSigned( final RGBALegacyType c) { return (byte)Math.round( normFloat( avg( c.get() ) ) * 255 ); }
	@Override
	public short get8BitUnsigned( final RGBALegacyType c) { return (short)Math.round( normFloat( avg( c.get() ) ) * 255 ); }		
	
	@Override
	public int get8BitARGB( final RGBALegacyType c) { return c.get(); }	
}
