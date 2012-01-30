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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import oss.alphazero.util.Log;
import oss.alphazero.util.buffer.DataCodec;
import oss.alphazero.util.support.AbstractNopCollection;

/**
 * A TCP/IP based in-process hand-off queue, designed to bypass
 * the following issues:
 * 
 * <li>multi-core memory coherence in terms of process memory</li>
 * <li>garbage generated by queue data structures </li>
 * 
 * by passing these concerns to the underlying operating system. 
 * 
 * <li>using the TCP/IP localhost loop back, the sender and receiver
 * threads are isolated at the process level</li>
 * 
 * <li>we do incur memory copy costs and certainly lots of garbage
 * is generated at the virtual memory level, but unlike object based
 * queues, the data has excellent locality and presumably the OS VM
 * is optimized for handling garbage and dealing with paging issues.</li>
 * 
 * <li>Final benefit is that the TCP of TCP/IP is designed for dealing
 * with congestion, so no specific in-process mechanisms are really
 * necessary to deal with queue getting full, e.g. throttling is another
 * nice 'free' benefit.</li>
 * 
 * Here TCP/IP is used, but naturally domain sockets (where available) 
 * can also be utilized.
 * <p>  
 * This class implements (as of now) basic {@link Queue} ops.  
 * REVU: wrong:(It is effectively an 'unbounded' queue.) 
 * TODO: may need to add timeouts on send.
 *    
 * @author Joubin <alphazero@sensesay.net>
 * @date:  Jan 27, 2012
 */

public class TcpQueueBase extends AbstractNopCollection<byte[]> implements Queue<byte[]>{

	// ----------------------------------------------------------------
	// Properties 
	// ----------------------------------------------------------------
	
	private static final int INIT_PORT = 0;
	
	final AtomicReference<Socket> rcvsocket_ref;
	final AtomicReference<Socket> sndsocket_ref;
	final AtomicInteger port_ref;
	
	volatile int port = INIT_PORT;
	
	static final int RECIEVER 	= 0;
	static final int SENDER 	= 1;
	
	private final Socket 		rcvsocket;
	private final Socket 		sndsocket;
	
	private final InputStream 	rcvin;
	private final OutputStream 	sndout;
	
	/* may use in future for meta-comm */
	@SuppressWarnings("unused")
	private final InputStream 	sndin;
	@SuppressWarnings("unused")
	private final OutputStream 	rcvout;
	
	private static final int SND_BUFF_SIZE = 64 * 8; // affects latency as of now
	private static final int RCV_BUFF_SIZE = 1024 * 24;
	
	private static final int SO_RCV_BUFF_SIZE = RCV_BUFF_SIZE;
	private static final int SO_SND_BUFF_SIZE = SND_BUFF_SIZE * 200;

	private static final int DATA_BUFF_SIZE = 1024 * 4;
	private static final int DATA_BUFF_CNT = 256;
	
	private final ByteBuffer[] wbuffers;
	private int wbuffidx;
	
	private final ByteBuffer[] rbuffers;
	private int rbuffidx;
	
	private final byte[]   sndbuffer;
	private int sndbuffoff;
	
	// ----------------------------------------------------------------
	// Constructor 
	// ----------------------------------------------------------------
    
	/**  */
	public TcpQueueBase() throws RuntimeException {
		this.rcvsocket_ref = new AtomicReference<Socket>();
		this.sndsocket_ref = new AtomicReference<Socket>();
		this.port_ref = new AtomicInteger(INIT_PORT);

		Socket[] endpoints = initializeEndpoints();
		this.sndsocket = endpoints[SENDER];
		this.rcvsocket = endpoints[RECIEVER];
		
		InputStream[] inputs = getInputStreams();
		this.sndin = inputs[SENDER];
		this.rcvin = inputs[RECIEVER];
		
		OutputStream[] outputs = getOutputStreams();
		this.sndout = outputs[SENDER];
		this.rcvout = outputs[RECIEVER];
		
		wbuffers = allocateWriterBuffers();
		wbuffidx = 0;
		
		rbuffers = getReaderBuffers (wbuffers);
		rbuffidx = 0;
		
		sndbuffer = allocateSendBuffer();
		sndbuffoff = 0;
	}
	
	// ----------------------------------------------------------------
	// INTERFACE:												  Queue
	// ----------------------------------------------------------------
	
	/* (non-Javadoc) @see java.util.Queue#offer(java.lang.Object) */
	@Override
	public final boolean offer(final byte[] b) {
		int curridx = wbuffidx;
		ByteBuffer currbuff = wbuffers[wbuffidx];
		final int currcap = currbuff.remaining();
		if(b.length > currcap) {
			currbuff.clear();
			// REVU: not handling sender overtaking receiver
			// TODO: place to READ a (TODO) virtual rbuffidx
			wbuffidx = (curridx+1)%DATA_BUFF_CNT;
			currbuff = wbuffers[wbuffidx];
		}
		// REVU: doff is redundant if code is correct ..
//		final int doff = currbuff.position();
//		final int dlen = b.length;
		currbuff.put(b);
		
		if(sndbuffoff + 12 > sndbuffer.length) {
			/* flush */
			try {
// DEBUG				
//				byte[] bb = sndbuffer;
//				int cnt = 0;
//				for(int off=0; off<sndbuffer.length-12; off+=12){
//					cnt++;
//			        Log.log("-----------------------------------------------------------------------------");
//			        Log.log("FLUSH: --- %02X %02X %02X %02X ---", bb[off],   bb[off+1], bb[off+2],  bb[off+3]);
//			        Log.log("FLUSH: --- %02X %02X %02X %02X ---", bb[off+4], bb[off+5], bb[off+6],  bb[off+7]);
//			        Log.log("FLUSH: --- %02X %02X %02X %02X ---", bb[off+8], bb[off+9], bb[off+10], bb[off+11]);
//			        Log.log("-----------------------------------------------------------------------------");
//				}
//				Log.log("**********************flush msg cnt:%d", cnt);
				this.sndout.write(sndbuffer);
				this.sndout.flush();
				sndbuffoff = 0;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		// REVU: 
		//	1 - switch to short for dlen
		//  2 - don't need doff (see REVU in poll())
		// msg is 12 bytes:
		// (int:bidx) (int:doff) (int:dlen)
		DataCodec.writeInt(wbuffidx, sndbuffer, sndbuffoff); sndbuffoff += DataCodec.INTEGER_BYTES;
//		DataCodec.writeInt(doff, sndbuffer, sndbuffoff);    sndbuffoff += DataCodec.INTEGER_BYTES;
		DataCodec.writeInt(b.length, sndbuffer, sndbuffoff);    sndbuffoff += DataCodec.INTEGER_BYTES;

		return true;
	}

	/* (non-Javadoc) @see java.util.Queue#poll() */
	@Override
	public byte[] poll() {
		byte[] data = null;
		// REVU: clearly net io is a bottle neck.
		// TODO: cache this and get rid of general purpose bufferedInput
		DataInputStream in = (DataInputStream) rcvin;
		try {
			final int buffidx = in.readInt();
			if(buffidx != rbuffidx) {
				rbuffers[rbuffidx].clear();
				rbuffidx = buffidx;
			}
			// REVU: note that doff is redundant as ByteBuffer will keep track
			// if there are no bugs in code (haha)
			// TODO: that's 4 bytes per message -- remove it.
//			final int doff = in.readInt();
			final int dlen = in.readInt();
//			Log.log("READ: %2d | %6d | %6d", buffidx, doff, dlen);
			data = new byte[dlen];
			rbuffers[buffidx].get(data);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
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
		throw new RuntimeException("Not (yet) supported.");
	}
	
	/* (non-Javadoc) @see java.util.Queue#element() */
	@Override
	public byte[] element() {
		final byte[] e = this.peek();
		if(e == null) throw new NoSuchElementException();
		return e;
	}
	
	// ----------------------------------------------------------------
	// Inner Ops 
	// ----------------------------------------------------------------
	private final byte[] allocateSendBuffer() {
		return new byte[SND_BUFF_SIZE];
	}
	private final ByteBuffer[] allocateWriterBuffers() {
		ByteBuffer[] buffers = new ByteBuffer[DATA_BUFF_CNT];
		for(int i=0; i< DATA_BUFF_CNT; i++) {
			buffers[i] = ByteBuffer.allocateDirect(DATA_BUFF_SIZE);
			buffers[i].clear();
			Log.log("buffer[%02d] allocated with cap: 0x%X %d", i, buffers[i].capacity(), buffers[i].capacity());
		}
		return buffers;
	}
	private final ByteBuffer[] getReaderBuffers (ByteBuffer[] srcbuffers) {
		ByteBuffer[] buffers = new ByteBuffer[DATA_BUFF_CNT];
		for(int i=0; i<srcbuffers.length; i++){
			buffers[i] = srcbuffers[i].asReadOnlyBuffer();
		}
		return buffers;
	}

	private final InputStream[] getInputStreams() throws RuntimeException {
		InputStream[] inputs = new InputStream[2];
		
		try {
			inputs[RECIEVER] = new DataInputStream(new BufferedInputStream(this.rcvsocket.getInputStream(), RCV_BUFF_SIZE));
//			inputs[RECIEVER] = new FastBufferedInputStream(this.rcvsocket.getInputStream(), RCV_BUFF_SIZE);
//			inputs[RECIEVER] = this.rcvsocket.getInputStream();
		} catch (Exception e) {
			String errmsg = String.format("%s exception on get InputStream for RCV ", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg, e);
		}
		
		try {
			inputs[SENDER] = this.sndsocket.getInputStream();
		} catch (Exception e) {
			String errmsg = String.format("%s exception on get InputStream for SND ", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg, e);
		}
		
		return inputs;
	}
	private final OutputStream[] getOutputStreams() throws RuntimeException {
		OutputStream[] outputs = new OutputStream[2];
		
		try {
			outputs[RECIEVER] = this.rcvsocket.getOutputStream();
		} catch (Exception e) {
			String errmsg = String.format("%s exception on get OutputStream for RCV ", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg, e);
		}
		
		try {
			outputs[SENDER] = this.sndsocket.getOutputStream();
//			outputs[SENDER] = new BufferedOutputStream(this.sndsocket.getOutputStream(), SND_BUFF_SIZE*2);
		} catch (Exception e) {
			String errmsg = String.format("%s exception on get OutputStream for SND ", e);
			Log.error(errmsg, e);
			throw new RuntimeException(errmsg, e);
		}
		
		return outputs;
	}

	private final Socket[] initializeEndpoints() throws IllegalStateException{
		
		/* bootstrap the end-points' respective IO Streams */
		Thread t_recv_bootstrap;
		Thread t_send_bootstrap;
		
		try {
			final CountDownLatch latch = new CountDownLatch(1);
			Runnable rcv_bootstrap_task = this.new EndpointRecvBootstrap(latch);
			t_recv_bootstrap = new Thread(rcv_bootstrap_task);
			
			Log.log("startup recv endpoint ...");
			t_recv_bootstrap.start();
			latch.await();
				
			Log.log("startup send endpoint ...");
			Runnable snd_bootstrap_task = this.new EndpointSendBootstrap(port_ref.get());
			t_send_bootstrap = new Thread(snd_bootstrap_task);
			t_send_bootstrap.start();
		} catch (Throwable e) {
			Log.error("failed to start connection establishment threads", e);
			throw new RuntimeException(e);
		}
		
		Socket[] sockets = new Socket[2];
		
		try {
			t_recv_bootstrap.join();
			t_send_bootstrap.join();
			
			/* assert */
			if((sockets[RECIEVER]=rcvsocket_ref.get()) == null)
				throw new IllegalStateException("BUG - rcvsocket is null");
			
			/* assert */
			if((sockets[SENDER]=sndsocket_ref.get()) == null)
				throw new IllegalStateException("BUG - rcvsocket is null");

		} catch (InterruptedException e) {
			Log.error("interrupted", e);
			throw new RuntimeException(e);
		}
		
		Log.log("-- RCV endpoint: %s", sockets[RECIEVER]);
		Log.log("-- SND endpoint: %s", sockets[SENDER]);
		
		Log.log("endpoints' connection established");
		
		return sockets;
	}

	private static final int SO_BANDWIDTH_PREF = 1;
	private static final int SO_LATENCY_PREF = 2;
	private static final int SO_CONNTIME_PREF = 0;
	
	// ========================================================================
	// Inner Class
	// ========================================================================
	
	public class EndpointRecvBootstrap implements Runnable {
		final CountDownLatch latch;
		
		EndpointRecvBootstrap (CountDownLatch latch){ this.latch = latch;}

		@Override final
		public void run() {
			try {
				final TcpQueueBase enclosing = TcpQueueBase.this; 
				Log.log("startup reciever endpoint for %s", enclosing);
				
				ServerSocket server = new ServerSocket(0);
				server.setPerformancePreferences(SO_CONNTIME_PREF, SO_LATENCY_PREF, SO_BANDWIDTH_PREF);
				server.setReceiveBufferSize(SO_RCV_BUFF_SIZE);
				
				int soport = server.getLocalPort();
				
				Log.log("-- RCV endpoint server socket opened on port %d", soport);

				if(!port_ref.compareAndSet(INIT_PORT, soport))
					throw new IllegalStateException("portUpdater");
				
				Log.log("-- RCV endpoint port set to %d", port_ref.get());
				
				Log.log("-- RCV endpoint now accepting connection ..");
				latch.countDown();
				Socket socket = server.accept();
				socket.setKeepAlive(true);
				socket.setPerformancePreferences(SO_CONNTIME_PREF, SO_LATENCY_PREF, SO_BANDWIDTH_PREF);
				socket.setTcpNoDelay(true);
				socket.setSendBufferSize(SO_RCV_BUFF_SIZE);
				SocketAddress remsoaddr = socket.getRemoteSocketAddress();

				Log.log("-- RCV endpoint accepted connection from %s", remsoaddr);
				
				if(!rcvsocket_ref.compareAndSet(null, socket))
					throw new IllegalStateException("recvInUpdater");
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				Log.log("server endpoint established");
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
				final TcpQueueBase enclosing = TcpQueueBase.this; 
				Log.log("startup send endpoint for %s @ ", enclosing, soport);
				
				Socket so = new Socket();
				so.setKeepAlive(true);
				so.setPerformancePreferences(SO_CONNTIME_PREF, SO_LATENCY_PREF, SO_BANDWIDTH_PREF);
				so.setTcpNoDelay(true);
				so.setSendBufferSize(SO_SND_BUFF_SIZE);
				
				Log.log("... connecting to port %d", soport);
				final InetAddress localhost = InetAddress.getLocalHost();
				SocketAddress endpoint = new InetSocketAddress(localhost, soport);
				
				so.connect(endpoint);
				Log.log("client connected to %s", so.getRemoteSocketAddress());
				
				if(!sndsocket_ref.compareAndSet(null, so))
					throw new IllegalStateException("sendInUpdater");
				
				Log.log("-- SND endpoint connected to remote endpoint %s", so.getRemoteSocketAddress());
				
			} catch (Exception e) {
				throw new RuntimeException("SND bootstrap failed", e);
			} finally {
				Log.log("SND endpoint established");
			}
		}
	}
	
	// ========================================================================
	// Temp Tests // REMOVE AT WILL
	// ========================================================================
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Queue<byte[]> pipe = new TcpQueueBase();
		pipe.offer(new byte[1024]);
		Log.log("OK, bye!");
	}
}
