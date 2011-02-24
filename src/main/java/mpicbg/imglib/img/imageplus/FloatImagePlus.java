/**
 * Copyright (c) 2009--2010, Funke, Preibisch, Saalfeld & Schindelin
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
 */
package mpicbg.imglib.img.imageplus;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import mpicbg.imglib.img.basictypeaccess.array.FloatArray;
import mpicbg.imglib.exception.ImgLibException;
import mpicbg.imglib.type.NativeType;

/**
 * {@link ImagePlusImg} for float-stored data.
 * 
 * @author Jan Funke, Stephan Preibisch, Stephan Saalfeld, Johannes Schindelin
 */
public class FloatImagePlus< T extends NativeType< T > > extends ImagePlusImg< T, FloatArray >
{
	final ImagePlus imp;	
	
	public FloatImagePlus( final long[] dim, final int entitiesPerPixel ) 
	{
		super( dim, entitiesPerPixel );
		
		if ( entitiesPerPixel == 1 )
		{
			final ImageStack stack = new ImageStack( width, height );
			for ( int i = 0; i < numSlices; ++i )
				stack.addSlice( "", new FloatProcessor( width, height ) );
			imp = new ImagePlus( "image", stack );
			imp.setDimensions( channels, depth, frames );
			if ( numSlices > 1 )
				imp.setOpenAsHyperStack( true );
			
			mirror.clear();
			for ( int c = 0; c < channels; ++c )
				for ( int t = 0; t < frames; ++t )
					for ( int z = 0; z < depth; ++z )
						mirror.add( new FloatArray( ( float[] )imp.getStack().getProcessor( imp.getStackIndex( c + 1, z + 1 , t + 1 ) ).getPixels() ) );
		}
		else
		{
			imp = null;

			mirror.clear();
			for ( int i = 0; i < numSlices; ++i )
				mirror.add( new FloatArray( width * height * entitiesPerPixel ) );
		}
	}

	public FloatImagePlus( final ImagePlus imp ) 
	{
		super(
				imp.getWidth(),
				imp.getHeight(),
				imp.getNSlices(),
				imp.getNFrames(),
				imp.getNChannels(),
				1 );
		
		this.imp = imp;

		mirror.clear();		
		for ( int c = 0; c < channels; ++c )
			for ( int t = 0; t < frames; ++t )
				for ( int z = 0; z < depth; ++z )
					mirror.add( new FloatArray( ( float[] )imp.getStack().getProcessor( imp.getStackIndex( c + 1, z + 1 , t + 1 ) ).getPixels() ) );
	}

	@Override
	public void close() 
	{
		super.close();
		if ( imp != null )
			imp.close(); 
	}

	@Override
	public ImagePlus getImagePlus() throws ImgLibException 
	{
		if ( imp == null )
			throw new ImgLibException( this, "has no ImagePlus instance, it is not a standard type of ImagePlus (" + entitiesPerPixel + " entities per pixel)" ); 
		else
			return imp;
	}
}

