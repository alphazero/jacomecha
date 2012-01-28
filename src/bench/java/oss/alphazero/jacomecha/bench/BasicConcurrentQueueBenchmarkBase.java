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

package oss.alphazero.jacomecha.bench;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import oss.alphazero.util.Log;
import oss.alphazero.util.concurrent.ConsumerProducerQueue;

@SuppressWarnings("unused")
abstract public class BasicConcurrentQueueBenchmarkBase <E>{

	/**  */
	final private Class<E> vclass;
	/** TODO: use a real spec class  */
	final Object[] spec;
	
	/**
	 * @param vclass
	 * @param spec
	 */
	BasicConcurrentQueueBenchmarkBase(Class<E> vclass, Object...spec) {
		assert vclass != null : "vclass is null";
		this.vclass = vclass;
		this.spec = spec;
	}
	abstract protected Queue<E> newQueue(Class<E> vclass, Object...spec) throws IllegalArgumentException;
	abstract protected E 		newQueueItme(Class<E> vclass, Object...spec);
	abstract protected Thread[] createProducers(Queue<E> queue, Object...spec); 
	abstract protected Thread[] createConsumers(Queue<E> queue, Object...spec);
	abstract protected long	 	signalAndMarkRunStart();
	abstract protected long	 	awaitAndMarkRunEnd();


	private final void run () {
		
		Queue<E> q = newQueue(vclass);
		
		Log.log("Benchmarking %s", q.getClass().getName());
		
		/* ---- setup -----------------------------------------------*/
		
		Thread[] consumers;
		Thread[] producers;
		try {
			consumers = createConsumers(q, spec);
		} catch (Throwable e){
			String errmsg = String.format("%s on createConsumers()", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg);
		}
		try {
			producers = createProducers(q, spec);
		} catch (Throwable e){
			String errmsg = String.format("%s on createProducers()", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg);
		}
		
		/* ---- start them -----------------------------------------------*/
		Log.log("Using %d producers and %d consumers", producers.length, consumers.length);
		try {
			for(Thread tconsumers : consumers)
				tconsumers.start();
			
		} catch (Throwable e) { 
			String errmsg = String.format("%s on consumers start", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg);
		}
		try {
			for(Thread tproducer : producers)
				tproducer.start();
			
		} catch (Throwable e) { 
			String errmsg = String.format("%s on producers start", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg);
		}
		
		// TODO: use a latch ...
		long start = signalAndMarkRunStart();

		/* ---- wait for run end----------------------------------------*/
		// TODO: reuse latch mechanism from JRedis benchmarkers
		long end = awaitAndMarkRunEnd();

		try {
			for(Thread tconsumer : consumers)
				tconsumer.join();
			
			for(Thread tproducer : producers)
				tproducer.join();
			
		} catch (Exception e) {
			String errmsg = String.format("%s on run end joins", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg);
		}
	}
	
	/* TODO:
	 * below need to be cleanly instrumented and use the spec
	 */
	
	private final Runnable newProducerTask (final Queue<E> q) {
		return new Runnable() {
			@Override final public void run() {
				E data = newQueueItme(vclass, "size", 8);
				final int iters = 1024 * 48; 
				for(;;){
					for(int i=0; i<iters; i++){
						q.offer(data);
					}
				}
			}
		};
	}
	private final Runnable newConsumerTask (final Queue<E> q) {
		return new Runnable() {
			@Override final public void run() {
				for(;;){
					long start = System.nanoTime();
					while(true){ 
						final E data = q.poll();
						if(data == null){
							LockSupport.parkNanos(10L);
						}
					}
				}
			}
		};
	}
}
