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

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import oss.alphazero.util.Log;

@SuppressWarnings("unused")
public class AdHocTestConcurrentQueue {
	public static void main(String[] args) {
		new AdHocTestConcurrentQueue().run();
	}
	private final void run () {
//		Queue<byte[]> q = new ConcurrentTcpQueueBase();
		Queue<byte[]> q = new ConsumerProducerQueue<byte[]>();
//		Queue<byte[]> q = new Nto1Concurrent2LockQueue<byte[]>();
//		Queue<byte[]> q = new LinkedBlockingQueue<byte[]>();
		final Thread tproducer = new Thread(newProducerTask(q), "producer-1");
//		final Thread tproducer2 = new Thread(newProducerTask(q), "producer-2");
//		final Thread tproducer3 = new Thread(newProducerTask(q), "producer-3");
		final Thread tconsumer = new Thread(newConsumerTask(q), "consumer");

		try {
			tconsumer.start();
			tproducer.start();
//			tproducer2.start();
//			tproducer3.start();
		} catch (Throwable e) { e.printStackTrace(); System.exit(1); }

		try {
			tproducer.join();
			tconsumer.join();
		} catch (Throwable e) { e.printStackTrace(); System.exit(1); }

	}
	private final byte[] getBlock(int size){
		
		byte[] b = new byte[size];
		for(int i=0; i<size; i++)
			b[i] = 100;
		b[0] = (byte) System.nanoTime();
		return b;
	}
	private final Runnable newProducerTask (final Queue<byte[]> q) {
		return new Runnable() {
			@Override final public void run() {
				byte[] data = getBlock(8);
//				byte[] data = {100,100,100,100,100,100,100,100,100,100,100,100,100,100,10,13 };
				final int iters = 1024 * 48;
				for(;;){
//					long start = System.nanoTime();
					for(int i=0; i<iters; i++){
						q.offer(data);
					}
//					qitem = qitem.longValue() + 1;
//					long delta = System.nanoTime() - start;
//					System.out.format("\t[%8s]--enqueue:%5d delta:%8d usec\n", Thread.currentThread().getName(), iters, TimeUnit.NANOSECONDS.toMicros(delta));
					LockSupport.parkNanos(100L);
				}
			}
		};
	}
	private final Runnable newConsumerTask (final Queue<byte[]> q) {
		return new Runnable() {
			@Override final public void run() {
				int n = 0;
				int lim = Integer.MAX_VALUE;
				int blim = 1024 * 1024 * 8;
				long start0 = System.nanoTime();
//				while(true) {
				for(int i=0; i<lim; i++){
					long start = System.nanoTime();
					long rlen = 0;
					while(rlen < blim){ 
						final byte[] data = q.poll();
						if(data == null){
							LockSupport.parkNanos(10L);
						}
						else {
							rlen += data.length;
						}
					}
					long delta = System.nanoTime() - start;
					n++;
					try {
						long bps = rlen*8*1000000000 / delta;
						long wps = (rlen*1000000000/8) / delta;
						System.out.format("[%08d] bytes:%d - delta:%8d usec - bps:%16d - wps:%12d\n", n, rlen, TimeUnit.NANOSECONDS.toMicros(delta), bps, wps);
					} catch (Exception e) {
						e.printStackTrace();
						Log.error("divide by zero - bytes:%d - delta: %d",rlen,  delta);
					}
				}
//				long delta = System.nanoTime() - start0;
//				long rlen = lim * blim;
//				long bps = rlen*8 / TimeUnit.NANOSECONDS.toSeconds(delta);
//				long wps = (rlen/8) / TimeUnit.NANOSECONDS.toSeconds(delta);
//				System.out.println();
//				System.out.format("[TOTAL] bytes:%d - delta:%8d msec - bps:%16d - wps:%12d {%s}\n", rlen, TimeUnit.NANOSECONDS.toMillis(delta), bps, wps, q.getClass().getSimpleName());
//				System.exit(1);
			}
		};
	}
}
