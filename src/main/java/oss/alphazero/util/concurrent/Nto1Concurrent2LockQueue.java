package oss.alphazero.util.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Nto1Concurrent2LockQueue<E> extends Concurrent2LockQueueBase<E> {
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	private final transient Lock Lt;

	// ------------------------------------------------------------------------
	// constructor
	// ------------------------------------------------------------------------

	Nto1Concurrent2LockQueue () {
    	super();
    	Lt = new ReentrantLock(false);
    }

	// ------------------------------------------------------------------------
	// INTERFACE:													   Queue<E>
	// ------------------------------------------------------------------------
    
    /* ------------------------------------------------------queue items --- */
    
	/* (non-Javadoc) @see java.util.Queue#offer(java.lang.Object) */
	@Override final
    public boolean offer(E item) {
		boolean r = false;
    	Lt.lock();
    	try{
    		r = super.offer(item);
    	} 
    	finally { Lt.unlock(); }
    	
    	return r;
    }
}
