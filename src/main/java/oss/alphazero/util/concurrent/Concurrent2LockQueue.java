/*
 *   Copyright 2012 Joubin Houshyar
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *    
 *   http://www.apache.org/licenses/LICENSE-2.0
 *    
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
