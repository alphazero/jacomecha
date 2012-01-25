package oss.alphazero.util.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Nto1Concurrent2LockQueue<E> extends Concurrent2LockQueueBase<E> {
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
//	private final transient Lock Lh;
	private final transient Lock Lt;

	// ------------------------------------------------------------------------
	// constructor
	// ------------------------------------------------------------------------

	Nto1Concurrent2LockQueue () {
    	super();
//    	Lh = new ReentrantLock(false);
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
    
    /* -----------------------------------------------------dequeue items --- */
	
//	/* (non-Javadoc) @see java.util.Queue#poll() */
//	@Override final
//    public E poll () {
//    	E item = null;
//    	
//    	Lh.lock();
//    	try{
//    		item = super.poll();
//    	} 
//    	finally { Lh.unlock(); }
//    	
//    	final E e = item;
//    	return e;
//    }
//    
//	/* (non-Javadoc) @see java.util.Queue#peek() */
//	@Override final
//    public E peek () {
//    	E item = null;
//    	
//    	Lh.lock();
//    	try{
//    		item = super.peek();
//    	} 
//    	finally { Lh.unlock(); }
//    	
//    	final E e = item;
//    	return e;
//    }
}
