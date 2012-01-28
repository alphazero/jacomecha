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
