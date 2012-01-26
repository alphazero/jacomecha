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
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
//import java.util.concurrent.locks.LockSupport;

import oss.alphazero.util.Log;
import oss.alphazero.util.support.AbstractNopCollection;

/**
 * WIP - move along folks ...
 * @author Joubin <alphazero@sensesay.net>
 *
 */
public class ConcurrentTcpQueueBase extends AbstractNopCollection<byte[]> implements Queue<byte[]>{

	// ----------------------------------------------------------------
	// INTERFACE:												Queue
	// ----------------------------------------------------------------
	
	/* (non-Javadoc) @see java.util.Queue#element() */
	@Override
	public byte[] element() {
		final byte[] e = this.peek();
		if(e == null) throw new NoSuchElementException();
		return e;
	}

	/* (non-Javadoc) @see java.util.Queue#offer(java.lang.Object) */
	@Override
	public boolean offer(byte[] e) {
		boolean res = false;
//		OutputStream out = sendOutUpdater.get(this);
		final OutputStream out = this.send_out;
//		Log.log("out: %s", out);
		try {
			out.write(e);
			out.flush();
			res = true;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return res;
	}

	/* (non-Javadoc) @see java.util.Queue#peek() */
	@Override
	public byte[] peek() {
		return null;
	}

	/* (non-Javadoc) @see java.util.Queue#poll() */
	@Override
	public byte[] poll() {
		byte[] data = null;
//		InputStream in = recvInUpdater.get(this);
		final InputStream in = this.recv_in;
//		Log.log("in: %s", in);
		try {
			int alen = 0;
			alen = in.available();
//			Log.log("READ alen:%d", alen);
			if(alen == 0) return null;
			data = new byte[alen];
			int rlen = in.read(data);
//			Log.log("READ alen:%d - rlen:%d", alen, rlen);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	/* (non-Javadoc) @see java.util.Queue#remove() */
	@Override
	public byte[] remove() {
		return null;
	}

	// ----------------------------------------------------------------
	// Properties 
	// ----------------------------------------------------------------
	
	/**  */
	private static final int RCV_BUFF_SIZE = 1024 * 1;
	/**  */
	private static final int SND_BUFF_SIZE = 1024 * 1;
	
	/**  */
	private static final int INIT_PORT = 0;
	/**  */
	volatile InputStream recv_in;
	/**  */
	volatile InputStream send_in;
	/**  */
	volatile OutputStream recv_out;
	/**  */
	volatile OutputStream send_out;
	/**  */
	volatile int port = INIT_PORT;
	
	/**  */
	AtomicIntegerFieldUpdater<ConcurrentTcpQueueBase>
	portUpdater = 
		AtomicIntegerFieldUpdater.newUpdater
		(ConcurrentTcpQueueBase.class, "port");
	
    /**  */
    private static final
	AtomicReferenceFieldUpdater<ConcurrentTcpQueueBase, InputStream>
	recvInUpdater = 
		AtomicReferenceFieldUpdater.newUpdater
		(ConcurrentTcpQueueBase.class, InputStream.class, "recv_in");

    /**  */
    private static final
	AtomicReferenceFieldUpdater<ConcurrentTcpQueueBase, InputStream>
	sendInUpdater = 
		AtomicReferenceFieldUpdater.newUpdater
		(ConcurrentTcpQueueBase.class, InputStream.class, "send_in");

    /**  */
    private static final
	AtomicReferenceFieldUpdater<ConcurrentTcpQueueBase, OutputStream>
	recvOutUpdater = 
		AtomicReferenceFieldUpdater.newUpdater
		(ConcurrentTcpQueueBase.class, OutputStream.class, "recv_out");

    /**  */
    private static final
	AtomicReferenceFieldUpdater<ConcurrentTcpQueueBase, OutputStream>
	sendOutUpdater = 
		AtomicReferenceFieldUpdater.newUpdater
		(ConcurrentTcpQueueBase.class, OutputStream.class, "send_out");

	
	// ----------------------------------------------------------------
	// Constructor 
	// ----------------------------------------------------------------
    
	/**
	 * 
	 */
	public ConcurrentTcpQueueBase() {
		initialize();
	}
	// ----------------------------------------------------------------
	// Inner Ops 
	// ----------------------------------------------------------------
	/**
	 * @throws RuntimeException
	 */
	final private void initialize() throws RuntimeException{
		portUpdater.set(this, INIT_PORT);
		
		Thread t_server;
		Thread t_client;
		
		try {
			Runnable server = this.new Server();
			t_server = new Thread(server);
			
			Log.log("startup server endpoint ...");
			t_server.start();
			
			Thread.sleep(1);
			Log.log("startup server endpoint ...");
			while(portUpdater.get(this) == INIT_PORT) {
				Thread.sleep(1);
			}
			Log.log("OK - port is set to %d", port);
	
			Log.log("startup client endpoint ...");
			Runnable client = this.new Client(port);
			t_client = new Thread(client);
			t_client.start();
			
		} catch (Throwable e) {
			Log.error("failed to start connection establishment threads", e);
			throw new RuntimeException(e);
		}
		
		try {
			t_server.join();
			t_client.join();
		} catch (InterruptedException e) {
			Log.error("interrupted", e);
			throw new RuntimeException(e);
		}
		
		Log.log("endpoint connections established");
	}
	// ========================================================================
	// Inner Class
	// ========================================================================
	
	public class Server implements Runnable {

		@Override final
		public void run() {
			try {
				final ConcurrentTcpQueueBase enclosing = ConcurrentTcpQueueBase.this; 
				Log.log("startup recv endpoint for %s", enclosing);
				
				ServerSocket server = new ServerSocket(0);
				server.setPerformancePreferences(0, 2, 1);
				server.setReceiveBufferSize(RCV_BUFF_SIZE);
				int soport = server.getLocalPort();
				
				Log.log("server socket opened on port %d", soport);
				if(!portUpdater.compareAndSet(enclosing, INIT_PORT, soport))
					throw new IllegalStateException("portUpdater");
				
				Log.log("port set to %d", portUpdater.get(enclosing));
				
				Log.log("server socket now accepting connection ..");
				Socket so = server.accept();
				SocketAddress remsoaddr = so.getRemoteSocketAddress();
				
				Log.log("server connected to %s", remsoaddr);
//				BufferedInputStream bif = new BufferedInputStream(so.getInputStream(), 1024);
				final InputStream in = so.getInputStream();
				if(!recvInUpdater.compareAndSet(enclosing, null, in))
					throw new IllegalStateException("recvInUpdater");
				
				if(!recvOutUpdater.compareAndSet(enclosing, null, so.getOutputStream()))
					throw new IllegalStateException("recvOutUpdater");
				
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
	public class Client implements Runnable{
		final int soport;
		
		Client (int soport){ this.soport = soport; }
		
		@Override final
		public void run() {
			try {
				final ConcurrentTcpQueueBase enclosing = ConcurrentTcpQueueBase.this; 
				Log.log("startup send endpoint for %s @ ", enclosing, soport);
				
				Socket so = new Socket();
				so.setKeepAlive(true);
				so.setPerformancePreferences(0, 2, 1);
				so.setTcpNoDelay(true);
				so.setSendBufferSize(SND_BUFF_SIZE);
				
				Log.log("... connecting to port %d", soport);
				final InetAddress localhost = InetAddress.getLocalHost();
				SocketAddress endpoint = new InetSocketAddress(localhost, soport);
				
				so.connect(endpoint);
				Log.log("client connected to %s", so.getRemoteSocketAddress());
				
				if(!sendInUpdater.compareAndSet(enclosing, null, so.getInputStream()))
					throw new IllegalStateException("sendInUpdater");
				
				if(!sendOutUpdater.compareAndSet(enclosing, null, so.getOutputStream()))
					throw new IllegalStateException("sendOutUpdater");
				
				Log.log("client endpoint established");
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	// ========================================================================
	// Temp Tests // REMOVE AT WILL
	// ========================================================================
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Queue<byte[]> pipe = new ConcurrentTcpQueueBase();
		Log.log("OK, bye!");
	}
}
