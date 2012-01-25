/*
 *   Copyright 2009-2012 Joubin Houshyar
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

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import oss.alphazero.util.support.AbstractNopCollection;

/**
 * This is pretty much based on the 2 Lock concurrent queueu algorithm described
 * in  Maged M. Michael, and Michael L. Scott's
 * "Simple, Fast, and Practical Non-Blocking and Blocking Concurrent Algorithms"
 * <br>
 * See: http://www.cs.rochester.edu/research/synchronization/pseudocode/queues.html
 * <p>
 * While this class implements {@link Queue}, not all operations are supported. Specifically,
 * none of the {@link Queue}'s inherited methods from {@link Collection} are implemented.
 * <p>
 * The queue is unbounded. <code>null</code> values are not permitted.
 * <p>
 * This class is declared as abstract and instantiation is restricted to 
 * prevent accidental misuse.
 *
 * @author  joubin (alphazero@sensesay.net)
 * @date    Dec 9, 2009
 * 
 */
abstract public class Concurrent2LockQueueBase<E> extends AbstractNopCollection<E> implements Queue<E>{

	// ========================================================================
	// Inner Class
	// ========================================================================
	
	private static final class Node<E> {
		private volatile E 			item;
		private volatile Node<E>	next;

		@SuppressWarnings("unchecked")
        private static final
		AtomicReferenceFieldUpdater<Node, Node>
		nextUpdater = 
			AtomicReferenceFieldUpdater.newUpdater
			(Node.class, Node.class, "next");

		@SuppressWarnings("unchecked")
        private static final
		AtomicReferenceFieldUpdater<Node, Object>
		itemUpdater = 
			AtomicReferenceFieldUpdater.newUpdater
			(Node.class, Object.class, "item");

		private Node(E x, Node<E> n) { item = x; next = n; }

		private final E getItem() { return item; }

		private final void setItem(E update) { itemUpdater.set(this, update); }

		private final void setNext(Node<E> update) { nextUpdater.set(this, update); }

		private final Node<E> getNext() { return next; }
	}

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	private transient volatile Node<E> head = new Node<E>(null, null);
	private transient volatile Node<E> tail = head;

	// ------------------------------------------------------------------------
	// constructor
	// ------------------------------------------------------------------------

    Concurrent2LockQueueBase () {
    	super();
    }

	// ------------------------------------------------------------------------
	// INTERFACE:													   Queue<E>
	// ------------------------------------------------------------------------
    
    /* ------------------------------------------------------queue items --- */
    
	/* (non-Javadoc) @see java.util.Queue#offer(java.lang.Object) */
	@Override 
    public boolean offer(E item) {
    	if(null == item) throw new NullPointerException("item");
    	Node<E> n = new Node<E>(item, null);
    	
		Node<E> t = tail;
		t.setNext(n);
		tail = n;
		
    	return true;
    }
    
	/* (non-Javadoc) @see java.util.Queue#add(java.lang.Object) */
	@Override final
	public boolean add(E e) {
		final boolean r = this.offer(e);
		if(!r) throw new IllegalStateException ();
		return r;
	}

    /* -----------------------------------------------------dequeue items --- */
	
	/* (non-Javadoc) @see java.util.Queue#poll() */
	@Override 
    public E poll () {
    	E item = null;
    	
		Node<E> h = head;
		Node<E> newhead = h.getNext();
		if(newhead != null) {
			item = newhead.getItem();
			head = newhead;
			newhead.setItem(null);
			h.setNext(null);
		} 
    	
    	final E e = item;
    	return e;
    }
    
	/* (non-Javadoc) @see java.util.Queue#peek() */
	@Override 
    public E peek () {
    	E item = null;
    	
		Node<E> h = head;
		Node<E> f = h.getNext();
		if(f != null) {
			item = f.getItem();
		}

    	final E e = item;
    	return e;
    }
	
	/* (non-Javadoc) @see java.util.Queue#element() */
	@Override final
	public E element() {
		final E e = this.peek();
		if(e == null) throw new NoSuchElementException();
		return e;
	}

	/* (non-Javadoc) @see java.util.Queue#remove() */
	@Override
	public E remove() {
		final E e = this.poll();
		if(e == null) throw new NoSuchElementException();
		return e;
	}
}
