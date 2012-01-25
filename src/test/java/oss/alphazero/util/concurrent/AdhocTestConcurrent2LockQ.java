package oss.alphazero.util.concurrent; 

import java.util.Queue;
import java.util.concurrent.locks.LockSupport;

public class AdhocTestConcurrent2LockQ {
	public static void main(String[] args) {
		new AdhocTestConcurrent2LockQ().run();
	}
	private final void run () {
		Queue<Object> q = new ConsumerProducerQueue<Object>();
		final Thread tproducer = new Thread(newProducerTask(q), "producer");
		final Thread tconsumer = new Thread(newConsumerTask(q), "consumer");

		try {
			tconsumer.start();
			tproducer.start();
		} catch (Throwable e) { e.printStackTrace(); System.exit(1); }

		//		try {
		//			tproducer.start();
		//			tconsumer.start();
		//		} catch (Throwable e) { e.printStackTrace(); System.exit(1); }
		//		
		try {
			tproducer.join();
			tconsumer.join();
		} catch (Throwable e) { e.printStackTrace(); System.exit(1); }

	}
	private final Runnable newProducerTask (final Queue<Object> q) {
		return new Runnable() {
			@Override final public void run() {
				Long qitem = new Long(0);
				final int iters = 1000;
				for(;;){
					for(int i=0; i<iters; i++){
						q.offer(qitem);
					}
					LockSupport.parkNanos(1L);
//					qitem = qitem.longValue() + 1;
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
					System.out.format("%d msec\n", delta/1000000);
				}
			}
		};
	}
}