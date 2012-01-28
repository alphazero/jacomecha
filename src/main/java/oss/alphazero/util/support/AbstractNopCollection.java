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

package oss.alphazero.util.support;

import java.util.Collection;
import java.util.Iterator;

/**
 * This is a ~ non-functional support class that implements {@link Collection}.
 * All methods will throw a {@link RuntimeException}. Extensions may choose to
 * implement whatever subset of {@link Collection} API (if any) that they wish
 * to support.
 * 
 * @author Joubin <alphazero@sensesay.net>
 *
 * @param <E>
 */
public class AbstractNopCollection<E> implements Collection<E> {
	
	/** disallow instantiation */
	protected AbstractNopCollection() {}
	
	/* (non-Javadoc) @see java.util.Collection#add(java.lang.Object) */
	@Override
	public boolean add(E e) {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#addAll(java.util.Collection) */
	@Override 
	public boolean addAll(Collection<? extends E> c) {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#clear() */
	@Override 
	public void clear() {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#contains(java.lang.Object) */
	@Override 
	public boolean contains(Object o) {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#containsAll(java.util.Collection) */
	@Override 
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#isEmpty() */
	@Override  public boolean isEmpty() {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#iterator() */
	@Override 
	public Iterator<E> iterator() {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#remove(java.lang.Object) */
	@Override 
	public boolean remove(Object o) {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#removeAll(java.util.Collection) */
	@Override 
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#retainAll(java.util.Collection) */
	@Override 
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#size() */
	@Override 
	public int size() {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#toArray() */
	@Override 
	public Object[] toArray() {
		throw new RuntimeException("not supported");
	}
	/* (non-Javadoc) @see java.util.Collection#toArray(T[]) */
	@Override 
	public <T> T[] toArray(T[] a) {
		throw new RuntimeException("not supported");
	}
}
