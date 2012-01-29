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
import oss.alphazero.util.buffer.DataCodec;

@SuppressWarnings("unused")
public class AdHocTestConcurrentQueue {
	
	
	public static void main(String[] args) {
		new AdHocTestConcurrentQueue().run();
	}
	private final void run () {
		Queue<byte[]> q = new TcpQueueBase();
//		Queue<byte[]> q = new TcpNioQueueBase();
//		Queue<byte[]> q = new ConsumerProducerQueue<byte[]>();
//		Queue<byte[]> q = new Nto1Concurrent2LockQueue<byte[]>();
//		Queue<byte[]> q = new LinkedBlockingQueue<byte[]>(1024 * 48);
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
		if(size%8!=0) throw new IllegalArgumentException("size is not multiple of 8: " + size);
		byte[] b = new byte[size];
		for(int off=0; off<size; off+=DataCodec.LONG_BYTES)
			DataCodec.writeLong(System.currentTimeMillis(), b, off);
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
					for(int i=0; i<iters; i++){
						q.offer(getBlock(1024));
					}
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
				final String qclass = q.getClass().getSimpleName();
				long totbytes = 0L;
				long totnanos = 0L;
				int n = 0;
				
				final int lim = 1000;
				final int blim = 12500000;
				
				Log.log("consumer task started");
				final long start0 = System.nanoTime();
				
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
					
//					report(String.valueOf(n), rlen, delta, qclass);
					
					totbytes += rlen;
				}
				totnanos = System.nanoTime() - start0;
				Log.log("---");
				Log.log("consumer stopping for summation");
				Log.log("---");
				report("TOTAL", totbytes, totnanos, qclass);
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.exit(1);
			}
		};
	}
	/* --------- metrics -----------------*/
	// TODO: move this to utils
	public static final long NANOS_PER_SEC = TimeUnit.SECONDS.toNanos(1);
	public static final long BITS_PER_BYTE = 8;
	public  static final long BITS_PER_LONG_WORD = Long.SIZE;
	public  static final long BYTES_PER_LONG_WORD = Long.SIZE / Byte.SIZE;

	public final static void report (final String label, final long totbytes, final long nanos, final String subjectclass ) {
		double rlen = totbytes;
		double Bps = rlen * NANOS_PER_SEC / nanos;
//		double bps = rlen * BITS_PER_BYTE * NANOS_PER_SEC / nanos;
		double wps = Bps / BYTES_PER_LONG_WORD;
		Log.log("[%8s] bytes:%12.0f | delta:%12d nsec | Bps:%12.0f | wps:%12.0f {%s}", label, rlen, nanos, Bps, wps, subjectclass);
	}
}
