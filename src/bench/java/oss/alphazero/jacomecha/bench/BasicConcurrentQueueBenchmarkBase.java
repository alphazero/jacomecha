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
