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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ConcurrentBlockingQueueBase<E> extends Concurrent2LockQueueBase<E> implements BlockingQueue<E> {

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		// REVU: this is hokey ..
		long deadline = System.nanoTime() + unit.toNanos(timeout);
		boolean r = false;
		while(!(r = super.offer(e))) {
			if(Thread.interrupted())
				throw new InterruptedException();
			if(System.nanoTime() < deadline)
				LockSupport.parkNanos(1L);
			else
				break;
		}
		return r;
	}

	@Override
	public void put(E e) throws InterruptedException {
		// TODO blocking put
		// REVU: equally hokey ..
		while(!offer(e, 1L, TimeUnit.NANOSECONDS)) 
			LockSupport.parkNanos(1L); // for now just keep calling
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		long deadline = System.nanoTime() + unit.toNanos(timeout);
		E e = null;
		while((e = super.poll()) == null) {
			if(Thread.interrupted())
				throw new InterruptedException();
			if(System.nanoTime() < deadline)
				LockSupport.parkNanos(1L);
			else
				break;
		}
		return e;
	}

	@Override
	public E take() throws InterruptedException {
		E e = null;
		while((e = poll(1L, TimeUnit.NANOSECONDS)) == null)
			LockSupport.parkNanos(1L);
		return e;
	}
	
	@Override
	public int remainingCapacity() {
		throw new RuntimeException("not yet supported -- maybe TODO");
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		throw new RuntimeException("not supported");
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		throw new RuntimeException("not supported");
	}

}
