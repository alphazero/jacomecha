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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import oss.alphazero.util.Log;

@SuppressWarnings("unused")
public class AdHocTestConcurrentQueue {
	
	static final long NANOS_PER_SEC = 1000 * 1000 * 1000;
	static final long BITS_PER_BYTE = 8;
	protected static final long BITS_PER_LONG_WORD = BITS_PER_BYTE * Long.SIZE;
	
	public static void main(String[] args) {
		new AdHocTestConcurrentQueue().run();
	}
	private final void run () {
		Queue<byte[]> q = new TcpQueueBase();
//		Queue<byte[]> q = new TcpNioQueueBase();
//		Queue<byte[]> q = new ConsumerProducerQueue<byte[]>();
//		Queue<byte[]> q = new Nto1Concurrent2LockQueue<byte[]>();
//		Queue<byte[]> q = new LinkedBlockingQueue<byte[]>();
//		Queue<byte[]> q = new ConcurrentLinkedQueue<byte[]>();
		final Thread tproducer = new Thread(newProducerTask(q), "producer-1");
//		final Thread tproducer2 = new Thread(newProducerTask(q), "producer-2");
//		final Thread tproducer3 = new Thread(newProducerTask(q), "producer-3");
		final Thread tconsumer = new Thread(newConsumerTask(q), "consumer");

		try {
			Log.log("start consumer(s)");
			tconsumer.start();
			Log.log("start producer(s)");
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
			b[i] = (byte) i;
//		b[0] = (byte) System.nanoTime();
		return b;
	}
	
	private final Runnable newProducerTask (final Queue<byte[]> q) {
		return new Runnable() {
			@Override final public void run() {
				byte[] data = getBlock(4096);
				byte[] buff = new byte[1024 * 4];
				int off = 0;
				final int iters = 4096 * 12;
				Log.log("producer task started");
				for(;;){
//					long start = System.nanoTime();
					for(int i=0; i<iters; i++){
//						for(int o=0; o<buff.length; o+=8) {
////							long addr = System.nanoTime();
//							//							System.arraycopy(getBlock(8), 0, buff, o, 8);
//							System.arraycopy(getBlock(8), 0, buff, o, 8);
//						}
//						q.offer(buff);
//						q.offer(data);
						q.offer(getBlock(1024));
					}
//					qitem = qitem.longValue() + 1;
//					long delta = System.nanoTime() - start;
//					System.out.format("\t[%8s]--enqueue:%5d delta:%8d usec\n", Thread.currentThread().getName(), iters, TimeUnit.NANOSECONDS.toMicros(delta));
//					LockSupport.parkNanos(1L);
				}
			}
		};
	}
	private final Runnable newConsumerTask (final Queue<byte[]> q) {
		return new Runnable() {
			@Override final public void run() {
				int n = 0;
//				int lim = Integer.MAX_VALUE;
				int lim = 1000;
				long totb = 0;
//				int blim = 1024 * 1024 * 8 * 10;
//				int blim = 83886080; // 10MBits
				int blim = 12500000; // 1 MBits
				Log.log("consumer task started");
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
//					long delta = System.nanoTime() - start;
					n++;
//					try {
//						long bps = rlen * BITS_PER_BYTE * NANOS_PER_SEC / delta;
//						long wps = bps / BITS_PER_LONG_WORD;
//						System.out.format("[%08d] bytes:%010d - delta:%8d usec - bps:%16d - wps:%12d\n", n, rlen, TimeUnit.NANOSECONDS.toMicros(delta), bps, wps);
//					} catch (Exception e) {
//						e.printStackTrace();
//						Log.error("divide by zero - bytes:%d - delta: %d",rlen,  delta);
//					}
					totb += rlen;
				}
				long delta = System.nanoTime() - start0;
				Log.log("consumer stopping for summation");
				double rlen = totb;
				double bps = rlen * BITS_PER_BYTE * NANOS_PER_SEC / delta;
				double wps = bps / BITS_PER_LONG_WORD;
				System.out.println();
				System.out.format("[TOTAL] bytes:%f - delta:%d msec - bps:%16f - wps:%f {%s}\n", rlen, TimeUnit.NANOSECONDS.toMillis(delta), bps, wps, q.getClass().getSimpleName());
				System.exit(1);
			}
		};
	}
}
