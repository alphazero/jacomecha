/*
 *   Copyright 2009-2012 Joubin Houshyar
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

package oss.alphazero.util.buffer;

import java.io.IOException;

/**
 * Convert to and from language primitives from provided buffers.
 * @author Joubin <alphazero@sensesay.net>
 * @date:  Jan 28, 2012
 */
public class DataCodec {
	static final int LONG_BYTES = Long.SIZE / Byte.SIZE;
	public static final byte[] writeLong(final long v, byte[] b) {
	    return writeLong(v, b, 0);
	}
	public static final byte[] writeLong(final long v, byte[] b, int off) {
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < LONG_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());
	    b[0] = (byte)(v >>> 56);
	    b[1] = (byte)(v >>> 48);
	    b[2] = (byte)(v >>> 40);
	    b[3] = (byte)(v >>> 32);
	    b[4] = (byte)(v >>> 24);
	    b[5] = (byte)(v >>> 16);
	    b[6] = (byte)(v >>>  8);
	    b[7] = (byte)(v >>>  0);
	    return b;
	}
	public static final long readLong(byte[] b) throws IOException {
		return readLong(b, 0);
	}
	public static final long readLong(byte[] b, int off) throws IOException {
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < LONG_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());
		return ( ((long)b[0] << 56) +
				((long)(b[1] & 255) << 48) +
				((long)(b[2] & 255) << 40) +
				((long)(b[3] & 255) << 32) +
				((long)(b[4] & 255) << 24) +
				((b[5] & 255) << 16) +
				((b[6] & 255) <<  8) +
				((b[7] & 255) <<  0));
	}
}
