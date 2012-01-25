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
