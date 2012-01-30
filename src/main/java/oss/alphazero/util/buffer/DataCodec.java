package oss.alphazero.util.buffer;

/**
 * Convert to and from language primitives from provided buffers.
 * @author Joubin <alphazero@sensesay.net>
 * @date:  Jan 28, 2012
 */
public class DataCodec {
	static public final int LONG_BYTES = Long.SIZE / Byte.SIZE;
	static public final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;
	static public final int SHORT_BYTES = Short.SIZE / Byte.SIZE;
	
	static public final void writeLong(final long v, final byte[] b) throws NullPointerException, IllegalArgumentException {
	    writeLong(v, b, 0);
	}
	static public final void writeLong(final long v, final byte[] b, final int off) throws NullPointerException, IllegalArgumentException {
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
	
	static public final void writeInt(final int v, final byte[] b){
		writeInt(v, b, 0);
	}
	static public final void writeInt(final int v, final byte[] b, final int off){
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < INTEGER_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());
        b[off]   = (byte) ((v >>> 24) & 0xFF);
        b[off+1] = (byte) ((v >>> 16) & 0xFF);
        b[off+2] = (byte) ((v >>>  8) & 0xFF);
        b[off+3] = (byte) ((v >>>  0) & 0xFF);
 	}
	
    public static final int readInt(final byte[] b) throws NullPointerException, IllegalArgumentException {
    	return readInt(b, 0);
    }
    public static final int readInt(final byte[] b, final int off) throws NullPointerException, IllegalArgumentException {
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < INTEGER_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());
        int b1 = b[off] & 0xFF;
        int b2 = b[off+1] & 0xFF;
        int b3 = b[off+2] & 0xFF;
        int b4 = b[off+3] & 0xFF;
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
    }

    public static final byte[] writeShort(final int v, final byte[] b) throws NullPointerException, IllegalArgumentException {
    	return writeShort(v, b, 0);
    }
    public static final byte[] writeShort(final int v, final byte[] b, final int off) throws NullPointerException, IllegalArgumentException {
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < SHORT_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());
		
        b[off] = (byte) ((v >>> 8) & 0xFF);
        b[off+1] = (byte) ((v >>> 0) & 0xFF);
        return b;
    }
    
    public static final short readShort(final byte[] b) throws NullPointerException, IllegalArgumentException {
    	return readShort(b, 0);
    }

    public static final short readShort(final byte[] b, final int off) throws NullPointerException, IllegalArgumentException {
		if(b==null)
			throw new NullPointerException("b");
		if(b.length - off < SHORT_BYTES) 
			throw new IllegalArgumentException(String.format("(b.len:%d, off:%d)", b.length, off).toString());

        int b1 = b[off] & 0xFF;
        int b2 = b[off+1] & 0xFF;
        return (short)((b1 << 8) + (b2 << 0));
    }

}
