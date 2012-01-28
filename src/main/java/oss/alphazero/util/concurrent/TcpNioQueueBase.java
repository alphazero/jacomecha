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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import oss.alphazero.util.Log;
import oss.alphazero.util.support.AbstractNopCollection;

/**
 * @author Joubin <alphazero@sensesay.net>
 * @date:  Jan 27, 2012
 */
public class TcpNioQueueBase extends AbstractNopCollection<byte[]> implements Queue<byte[]>{

	// ----------------------------------------------------------------
	// Properties 
	// ----------------------------------------------------------------
	
	/**  */
	private static final int SND_BUFF_SIZE = 4096;
	private static final int RCV_BUFF_SIZE = 4096;
	private static final int SO_RCV_BUFF_SIZE = RCV_BUFF_SIZE * 2;
	private static final int SO_SND_BUFF_SIZE = SND_BUFF_SIZE * 2;
	/**  */
	private static final int INIT_PORT = 0;
	
	final AtomicReference<SocketChannel> recvchan_ref;
	final AtomicReference<SocketChannel> sendchan_ref;
	final AtomicInteger port_ref;
	
	/**  */
	volatile InputStream recv_in;
	/**  */
	volatile InputStream send_in;
	/**  */
	volatile OutputStream recv_out;
	/**  */
	volatile OutputStream send_out;
	
	static final int RECIEVER 	= 0;
	static final int SENDER 	= 1;
	private final ByteBuffer sndbuff;
	private final ByteBuffer rcvbuff;
	private final SocketChannel rcvchan;
	private final SocketChannel sndchan;

	// ----------------------------------------------------------------
	// Constructor 
	// ----------------------------------------------------------------
    
	/**  */
	public TcpNioQueueBase() {
		
		this.recvchan_ref = new AtomicReference<SocketChannel>();
		this.sendchan_ref = new AtomicReference<SocketChannel>();
		this.port_ref = new AtomicInteger(INIT_PORT);
		
		SocketChannel[] endpoints = initializeEndpoints();
		this.sndchan = endpoints[SENDER];
		this.rcvchan = endpoints[RECIEVER];
		
		ByteBuffer[] endpoint_buffers = initializeBuffers();

		this.sndbuff = endpoint_buffers[SENDER];
		this.rcvbuff = endpoint_buffers[RECIEVER];
	}
	
	// ----------------------------------------------------------------
	// Inner Ops 
	// ----------------------------------------------------------------
	/**
	 * REVU: just use a latch ..
	 * @throws RuntimeException
	 */
	final private SocketChannel[] initializeEndpoints() throws RuntimeException{
		
		/* bootstrap the end-points' respective IO Streams */
		Thread t_recv_bootstrap;
		Thread t_send_bootstrap;
		
		try {
			Runnable rcv_bootstrap_task = this.new EndpointRecvBootstrap();
			t_recv_bootstrap = new Thread(rcv_bootstrap_task);
			
			Log.log("startup recv endpoint ...");
			t_recv_bootstrap.start();
			
			Thread.sleep(1);
			while(port_ref.get() == INIT_PORT) {
				Thread.sleep(1000L);
			}
				
			Log.log("startup send endpoint ...");
			Runnable snd_bootstrap_task = this.new EndpointSendBootstrap(port_ref.get());
			t_send_bootstrap = new Thread(snd_bootstrap_task);
			t_send_bootstrap.start();
			
			// TODO: REVU: unreliable -- latch it
//			while(recvchan_ref.get() == null) {
//				Thread.sleep(1000L);
//			}
		} catch (Throwable e) {
			Log.error("failed to start connection establishment threads", e);
			throw new RuntimeException(e);
		}
		
		SocketChannel[] schans = new SocketChannel[2];
		
		try {
			t_recv_bootstrap.join();
			t_send_bootstrap.join();
			
			/* assert */
			if((schans[RECIEVER]=recvchan_ref.get()) == null)
				throw new IllegalStateException("BUG - recvchan is null");
			
			/* assert */
			if((schans[SENDER]=sendchan_ref.get()) == null)
				throw new IllegalStateException("BUG - sendchan is null");

		} catch (InterruptedException e) {
			Log.error("interrupted", e);
			throw new RuntimeException(e);
		}
		
		Log.log("-- RCV endpoint: %s", schans[RECIEVER]);
		Log.log("-- SND endpoint: %s", schans[SENDER]);
		
		Log.log("endpoints' connection established");
		
		return schans;
	}
	/**
	 * REVU: just use a latch ..
	 * @throws RuntimeException
	 */
	final private ByteBuffer[] initializeBuffers() throws RuntimeException{
		
		ByteBuffer[] endpoint_buffers = new ByteBuffer[2];
		
		try {
			endpoint_buffers[SENDER] = ByteBuffer.allocateDirect(SND_BUFF_SIZE);
			if(endpoint_buffers[SENDER] == null)
				throw new IllegalStateException(String.format("send buffer direct allocate of %d failed", SND_BUFF_SIZE));
				
			endpoint_buffers[RECIEVER] = ByteBuffer.allocateDirect(SND_BUFF_SIZE);
			if(endpoint_buffers[SENDER] == null)
				throw new IllegalStateException(String.format("send buffer direct allocate of %d failed", RCV_BUFF_SIZE));
		} catch (Throwable e) {
			String errmsg = "buffer allocation";
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg, e);
		}
		
		Log.log("-- RCV endpoint buffer: %s", endpoint_buffers[RECIEVER]);
		Log.log("-- SND endpoint buffer: %s", endpoint_buffers[SENDER]);
		
		Log.log("endpoint buffers allocated");
		
		return endpoint_buffers;
	}
	
	// ========================================================================
	// Inner Class
	// ========================================================================
	public class EndpointRecvBootstrap implements Runnable {

		@Override final
		public void run() {
			try {
				final TcpNioQueueBase enclosing = TcpNioQueueBase.this; 
				Log.log("startup reciever endpoint for %s", enclosing);
				
				ServerSocketChannel ssc = ServerSocketChannel.open();
				ssc.configureBlocking(true);
				
				ServerSocket server = ssc.socket();
				server.setPerformancePreferences(0, 2, 1);
				server.setReceiveBufferSize(SO_RCV_BUFF_SIZE);
				
				final SocketAddress localhostanyport = null;
				server.bind(localhostanyport);
				int soport = server.getLocalPort();
				
				Log.log("-- RCV endpoint server socket opened on port %d", soport);

				if(!port_ref.compareAndSet(INIT_PORT, soport))
					throw new IllegalStateException("portUpdater");
				
				Log.log("-- RCV endpoint port set to %d", port_ref.get());
				
				Log.log("-- RCV endpoint now accepting connection ..");
				Socket socket = server.accept();
				SocketAddress remsoaddr = socket.getRemoteSocketAddress();
				
				Log.log("-- RCV endpoint accepted connection from %s", remsoaddr);
				
				SocketChannel sc = socket.getChannel();
				boolean rup = recvchan_ref.compareAndSet(null, sc);
				if(!rup)
					throw new RuntimeException("recvchan_ref CAS failed");
				
			} catch (IOException e) {
				throw new RuntimeException("RCV bootstrap failed", e);
			} finally {
				Log.log("RCV endpoint established");
			}
		}
	}
	
	// ========================================================================
	// Inner Class
	// ========================================================================
	public class EndpointSendBootstrap implements Runnable{
		final int soport;
		
		EndpointSendBootstrap (int soport){ this.soport = soport; }
		
		@Override final
		public void run() {
			try {
				final TcpNioQueueBase enclosing = TcpNioQueueBase.this; 
				Log.log("startup send endpoint for %s @ ", enclosing, soport);
				
				final InetAddress localhost = InetAddress.getLocalHost();
				SocketAddress endpoint = new InetSocketAddress(localhost, soport);
				SocketChannel sc = SocketChannel.open();
				Socket so = sc.socket();
				so.setKeepAlive(true);
				so.setPerformancePreferences(0, 2, 1);
				so.setTcpNoDelay(true);
				so.setSendBufferSize(SO_SND_BUFF_SIZE);
				
				Log.log("-- SND endpoint connecting to remote endpoint %s", so.getRemoteSocketAddress());
				
				boolean rc = sc.connect(endpoint);
				if(rc != true)
					throw new RuntimeException("failed to connect");
				
				boolean rup = sendchan_ref.compareAndSet(null, sc);
				if(!rup)
					throw new RuntimeException("BUG -- cas(sendchan) failed");
				
				Log.log("-- SND endpoint connected to remote endpoint %s", so.getRemoteSocketAddress());
				
			} catch (Exception e) {
				throw new RuntimeException("SND bootstrap failed", e);
			} finally {
				Log.log("SND endpoint established");
			}
		}
	}
	
	// ----------------------------------------------------------------
	// INTERFACE:												Queue
	// ----------------------------------------------------------------
	
	/* (non-Javadoc) @see java.util.Queue#offer(java.lang.Object) */
	@Override
	public boolean offer(final byte[] b) {
		boolean res = true;
		try {
			sndbuff.put(b);
			sndbuff.flip();
//			Log.log("sndbuff.pos: %d", sndbuff.position());
//			Log.log("sndbuff.lim: %d", sndbuff.limit());
			int wlen = sndchan.write(sndbuff);
//			Log.log("wrote %d of %d", wlen, b.length);
			if(wlen == 0 || wlen < b.length){
				Log.log("\t -- isConnected %b", sndchan.isConnected());
				Log.log("\t -- isBlocking %b", sndchan.isBlocking());
			}

			sndbuff.clear();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return res;
	}

	/* (non-Javadoc) @see java.util.Queue#poll() */
	@Override
	public byte[] poll() {
		byte[] data = null;
		try {
			int rlen =  rcvchan.read(rcvbuff);
			data = new byte[rlen];
			rcvbuff.flip();
			rcvbuff.get(data);
//			Log.log("read %d", rlen);
			rcvbuff.clear();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	/* (non-Javadoc) @see java.util.Queue#peek() */
	@Override final
	public byte[] peek() {
		throw new RuntimeException("Not (yet) supported.");
	}

	/* (non-Javadoc) @see java.util.Queue#remove() */
	@Override
	public byte[] remove() {
		return null;
	}
	
	/* (non-Javadoc) @see java.util.Queue#element() */
	@Override
	public byte[] element() {
		final byte[] e = this.peek();
		if(e == null) throw new NoSuchElementException();
		return e;
	}

	// ========================================================================
	// Temp Tests // REMOVE AT WILL
	// ========================================================================
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Queue<byte[]> pipe = new TcpNioQueueBase();
		Log.log("OK, bye!");
	}
}
