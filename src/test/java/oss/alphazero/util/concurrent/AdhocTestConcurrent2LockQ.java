package oss.alphazero.util.concurrent; 

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import bsh.commands.dir;

public class AdhocTestConcurrent2LockQ {
	public static void main(String[] args) {
		new AdhocTestConcurrent2LockQ().run();
	}
	private final void run () {
//		Queue<Object> q = new ConsumerProducerQueue<Object>();
//		Queue<Object> q = new Concurrent2LockQueue<Object>();
		Queue<Object> q = new Nto1Concurrent2LockQueue<Object>();
		final Thread tproducer = new Thread(newProducerTask(q), "producer");
		final Thread tconsumer = new Thread(newConsumerTask(q), "consumer");

		try {
			tconsumer.start();
			tproducer.start();
		} catch (Throwable e) { e.printStackTrace(); System.exit(1); }

		try {
			tproducer.join();
			tconsumer.join();
		} catch (Throwable e) { e.printStackTrace(); System.exit(1); }

	}
	private final Runnable newProducerTask (final Queue<Object> q) {
		return new Runnable() {
			@Override final public void run() {
				Long qitem = new Long(0);
				final int iters = 4000;
				for(;;){
//					long start = System.nanoTime();
					for(int i=0; i<iters; i++){
						q.offer(qitem);
					}
					LockSupport.parkNanos(100L);
//					qitem = qitem.longValue() + 1;
//					long delta = System.nanoTime() - start;
//					System.out.format("enqueue:%d delta:%d msec\n", iters, delta/1000000);
				}
			}
		};
	}
	private final Runnable newConsumerTask (final Queue<Object> q) {
		return new Runnable() {
			@Override final public void run() {
				int iters = 100000;
				for(;;){
					long start = System.nanoTime();
					for(int i=0; i<iters; i++){
						final Object qitem = q.poll();
						if(qitem == null){
//							System.out.format(".");
							LockSupport.parkNanos(1L);
						}
					}
					long delta = System.nanoTime() - start;
					long dqpusec = iters / (delta/1000);
					System.out.format("dequeue:%d - delta:%d usec - dequeues/usec:%d\n", iters, TimeUnit.NANOSECONDS.toMicros(delta), dqpusec);
				}
			}
		};
	}
}