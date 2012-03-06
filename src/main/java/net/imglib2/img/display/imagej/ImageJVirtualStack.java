package net.imglib2.img.display.imagej;

import ij.ImagePlus;
import ij.VirtualStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.display.XYProjector;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.NativeType;
import net.imglib2.view.Views;

public abstract class ImageJVirtualStack< S, T extends NativeType< T > > extends VirtualStack
{
	final private XYProjector< S, T > projector;

	final private int size;
	final private int numDimensions;

	final private int bitDepth;

	final protected ImageProcessor imageProcessor;

	protected ImageJVirtualStack( final RandomAccessibleInterval< S > source, final Converter< S, T > converter, final T type, final int ijtype )
	{
		super( ( int ) source.dimension( 0 ), ( int ) source.dimension( 1 ), null, null );

		assert source.numDimensions() > 1;

		this.size = ( source.numDimensions() > 2 ) ? ( int ) source.dimension( 2 ) : 1;

		final int sizeX = ( int ) source.dimension( 0 );
		final int sizeY = ( int ) source.dimension( 1 );

		final ArrayImg< T, ? > img = new ArrayImgFactory< T >().create( new long[] { sizeX, sizeY }, type );

		this.numDimensions = source.numDimensions();

		// test whether the source interval starts at (0,0,...,0)
		boolean isZeroMin = true;
		for ( int d = 0; d < numDimensions; ++d )
		{
			if ( source.min(d) != 0 )
			{
				isZeroMin = false;
				break;
			}
		}
		// if the source interval is not zero-min, we wrap it into a view that translates it to the origin
		this.projector = new XYProjector< S, T >( isZeroMin ? source : Views.zeroMin( source ), img, converter );

		switch ( ijtype )
		{
		case ImagePlus.GRAY8:
			this.bitDepth = 8;
			imageProcessor = new ByteProcessor( sizeX, sizeY, ( byte[] ) ( ( ArrayDataAccess< ? > ) img.update( null ) ).getCurrentStorageArray(), null );
			break;
		case ImagePlus.GRAY16:
			this.bitDepth = 16;
			imageProcessor = new ShortProcessor( sizeX, sizeY, ( short[] ) ( ( ArrayDataAccess< ? > ) img.update( null ) ).getCurrentStorageArray(), null );
			break;
		case ImagePlus.COLOR_RGB:
			this.bitDepth = 24;
			imageProcessor = new ColorProcessor( sizeX, sizeY, ( int[] ) ( ( ArrayDataAccess< ? > ) img.update( null ) ).getCurrentStorageArray() );
			break;
		case ImagePlus.GRAY32:
			this.bitDepth = 32;
			imageProcessor = new FloatProcessor( sizeX, sizeY, ( float[] ) ( ( ArrayDataAccess< ? > ) img.update( null ) ).getCurrentStorageArray(), null );
			// ip.setMinAndMax( display.getMin(), display.getMax() );
			break;
		default:
			throw new IllegalArgumentException( "unsupported color type " + ijtype );
		}
	}

	/**
	 * Returns an ImageProcessor for the specified slice, were 1<=n<=nslices.
	 * Returns null if the stack is empty.
	 */
	@Override
	public ImageProcessor getProcessor( final int n )
	{
		if ( numDimensions > 2 )
			projector.setPosition( n - 1, 2 );

		projector.map();
		return imageProcessor;
	}

	@Override
	public int getBitDepth()
	{
		return bitDepth;
	}

	/** Obsolete. Short images are always unsigned. */
	@Override
	public void addUnsignedShortSlice( final String sliceLabel, final Object pixels )
	{}

	/** Adds the image in 'ip' to the end of the stack. */
	@Override
	public void addSlice( final String sliceLabel, final ImageProcessor ip )
	{}

	/**
	 * Adds the image in 'ip' to the stack following slice 'n'. Adds the slice
	 * to the beginning of the stack if 'n' is zero.
	 */
	@Override
	public void addSlice( final String sliceLabel, final ImageProcessor ip, final int n )
	{}

	/** Deletes the specified slice, were 1<=n<=nslices. */
	@Override
	public void deleteSlice( final int n )
	{}

	/** Deletes the last slice in the stack. */
	@Override
	public void deleteLastSlice()
	{}

	/**
	 * Updates this stack so its attributes, such as min, max, calibration table
	 * and color model, are the same as 'ip'.
	 */
	@Override
	public void update( final ImageProcessor ip )
	{}

	/** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
	@Override
	public Object getPixels( final int n )
	{
		return getProcessor( n ).getPixels();
	}

	/**
	 * Assigns a pixel array to the specified slice, were 1<=n<=nslices.
	 */
	@Override
	public void setPixels( final Object pixels, final int n )
	{}

	/**
	 * Returns the stack as an array of 1D pixel arrays. Note that the size of
	 * the returned array may be greater than the number of slices currently in
	 * the stack, with unused elements set to null.
	 */
	@Override
	public Object[] getImageArray()
	{
		return null;
	}

	/**
	 * Returns the slice labels as an array of Strings. Note that the size of
	 * the returned array may be greater than the number of slices currently in
	 * the stack. Returns null if the stack is empty or the label of the first
	 * slice is null.
	 */
	@Override
	public String[] getSliceLabels()
	{
		return null;
	}

	/**
	 * Returns the label of the specified slice, were 1<=n<=nslices. Returns
	 * null if the slice does not have a label. For DICOM and FITS stacks,
	 * labels may contain header information.
	 */
	@Override
	public String getSliceLabel( final int n )
	{
		return "" + n;
	}

	/**
	 * Returns a shortened version (up to the first 60 characters or first
	 * newline and suffix removed) of the label of the specified slice. Returns
	 * null if the slice does not have a label.
	 */
	@Override
	public String getShortSliceLabel( final int n )
	{
		return getSliceLabel( n );
	}

	/** Sets the label of the specified slice, were 1<=n<=nslices. */
	@Override
	public void setSliceLabel( final String label, final int n )
	{}

	/** Returns true if this is a 3-slice RGB stack. */
	@Override
	public boolean isRGB()
	{
		return false;
	}

	/** Returns true if this is a 3-slice HSB stack. */
	@Override
	public boolean isHSB()
	{
		return false;
	}

	/**
	 * Returns true if this is a virtual (disk resident) stack. This method is
	 * overridden by the VirtualStack subclass.
	 */
	@Override
	public boolean isVirtual()
	{
		return true;
	}

	/** Frees memory by deleting a few slices from the end of the stack. */
	@Override
	public void trim()
	{}

	@Override
	public int getSize()
	{
		return size;
	}

	@Override
	public void setBitDepth( final int bitDepth )
	{}

	@Override
	public String getDirectory()
	{
		return null;
	}

	@Override
	public String getFileName( final int n )
	{
		return null;
	}
}
