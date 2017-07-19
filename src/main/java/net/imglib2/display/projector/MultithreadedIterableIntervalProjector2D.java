package net.imglib2.display.projector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;


public class MultithreadedIterableIntervalProjector2D<A, B> extends IterableIntervalProjector2D< A, B >
{

	final ExecutorService service;

	private final int dimX;

	private final int dimY;
	
	private final int nTasks;

	public MultithreadedIterableIntervalProjector2D(int dimX, int dimY, RandomAccessible< A > source,
			IterableInterval< B > target, Converter< ? super A, B > converter, ExecutorService service,
			int nTasks)
	{
		super( dimX, dimY, source, target, converter );

		this.service = service;
		this.dimX = dimX;
		this.dimY = dimY;
		this.nTasks = nTasks;
	}
	
	public MultithreadedIterableIntervalProjector2D(int dimX, int dimY, RandomAccessible< A > source,
			IterableInterval< B > target, Converter< ? super A, B > converter, ExecutorService service)
	{
		this(dimX, dimY, source, target, converter, service, Runtime.getRuntime().availableProcessors());
	}

	@Override
	public void map()
	{
		// fix interval for all dimensions
		for ( int d = 0; d < position.length; ++d )
			min[d] = max[d] = position[d];

		min[dimX] = target.min( 0 );
		min[dimY] = target.min( 1 );
		max[dimX] = target.max( 0 );
		max[dimY] = target.max( 1 );
		
		
		// we ignore a lot of optimizations in the original IterableIntervalProjector2D here
		// (e.g. when iteration orders are the same)
		//
		// hopefully, multithreading will still give us a well-performing solution
		
		final long portionSize = target.size() / nTasks;
		
		final List< Callable< Void > > tasks = new ArrayList<>();
		final AtomicInteger ai = new AtomicInteger();
		
		for (int t = 0; t < nTasks; ++t)
		{
			tasks.add( new Callable< Void >()
			{
				
				@Override
				public Void call() throws Exception
				{
					int i = ai.getAndIncrement();
					
					final Cursor< B > targetCursor = target.localizingCursor();
					final RandomAccess< A > sourceRandomAccess = source.randomAccess();
					
					targetCursor.jumpFwd( i * portionSize );
					
					long stepsTaken = 0;
					
					// either map a portion or (for the last portion) go until the end
					while ((i == nTasks - 1 && stepsTaken < portionSize) || targetCursor.hasNext())
					{
						stepsTaken++;

						final B b = targetCursor.next();
						sourceRandomAccess.setPosition( targetCursor.getLongPosition( 0 ), dimX );
						sourceRandomAccess.setPosition( targetCursor.getLongPosition( 1 ), dimY );

						converter.convert( sourceRandomAccess.get(), b );
					}
					
					return null;
				}
			} );
		}
		
		try
		{
			List< Future< Void > > futures = service.invokeAll( tasks );
			for (Future< Void > f : futures)
				f.get();
		}
		catch ( InterruptedException | ExecutionException e )
		{
			e.printStackTrace();
		}


	}

}
