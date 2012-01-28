/*
 *   Copyright 2012 Joubin Houshyar
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package oss.alphazero.util.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Fully concurrent extension of {@link Concurrent2LockQueueBase}. 
 * 
 * @see Concurrent2LockQueueBase
 * @author Joubin <alphazero@sensesay.net>
 *
 * @param <E>
 */
public class Concurrent2LockQueue<E> extends Concurrent2LockQueueBase<E> {
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	private final transient Lock Lh;
	private final transient Lock Lt;

	// ------------------------------------------------------------------------
	// constructor
	// ------------------------------------------------------------------------

    Concurrent2LockQueue () {
    	super();
    	Lh = new ReentrantLock(false);
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
	
	/* (non-Javadoc) @see java.util.Queue#poll() */
	@Override final
    public E poll () {
    	E item = null;
    	
    	Lh.lock();
    	try{
    		item = super.poll();
    	} 
    	finally { Lh.unlock(); }
    	
    	final E e = item;
    	return e;
    }
    
	/* (non-Javadoc) @see java.util.Queue#peek() */
	@Override final
    public E peek () {
    	E item = null;
    	
    	Lh.lock();
    	try{
    		item = super.peek();
    	} 
    	finally { Lh.unlock(); }
    	
    	final E e = item;
    	return e;
    }
}
