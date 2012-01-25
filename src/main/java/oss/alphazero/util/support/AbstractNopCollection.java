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
