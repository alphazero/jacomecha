package oss.alphazero.util.buffer;


/**
 * Convert to and from language primitives from provided buffers.
 * @author Joubin <alphazero@sensesay.net>
 * @date:  Jan 28, 2012
 */
public class DataCodec {
	static public final int LONG_BYTES = Long.SIZE / Byte.SIZE;
	static public final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;
	static public final byte[] writeLong(final long v, final byte[] b) {
	    return writeLong(v, b, 0);
	}
	static public final byte[] writeLong(final long v, final byte[] b, final int off) {
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < LONG_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());
	    b[off]   = (byte)(v >>> 56);
	    b[off+1] = (byte)(v >>> 48);
	    b[off+2] = (byte)(v >>> 40);
	    b[off+3] = (byte)(v >>> 32);
	    b[off+4] = (byte)(v >>> 24);
	    b[off+5] = (byte)(v >>> 16);
	    b[off+6] = (byte)(v >>>  8);
	    b[off+7] = (byte)(v >>>  0);
	    return b;
	}
	
	static public final byte[] writeInt(final int v, final byte[] b){
		return writeInt(v, b, 0);
	}
	static public final byte[] writeInt(final int v, final byte[] b, final int off){
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < INTEGER_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());
        b[off]   = (byte) ((v >>> 24) & 0xFF);
        b[off+1] = (byte) ((v >>> 16) & 0xFF);
        b[off+2] = (byte) ((v >>>  8) & 0xFF);
        b[off+3] = (byte) ((v >>>  0) & 0xFF);
        return b;
	}
	
	static public final long readLong(byte[] b){
		return readLong(b, 0);
	}
	static public final long readLong(byte[] b, int off){
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < LONG_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());
		return ( 
			((long)b[0] << 56) +
			((long)(b[1] & 255) << 48) +
			((long)(b[2] & 255) << 40) +
			((long)(b[3] & 255) << 32) +
			((long)(b[4] & 255) << 24) +
			((b[5] & 255) << 16) +
			((b[6] & 255) <<  8) +
			((b[7] & 255) <<  0)
		);
	}
	
    public static final byte[] writeShort(final int v, final byte[] out) {
    	return writeShort(v, out, 0);
    }
    
    public static final byte[] writeShort(final int v, final byte[] out, final int off) {
        out[off] = (byte) ((v >>> 8) & 0xFF);
        out[off+1] = (byte) ((v >>> 0) & 0xFF);
        return out;
    }
}
