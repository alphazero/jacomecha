
package oss.alphazero.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Random;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import oss.alphazero.util.buffer.DataCodec;

/**
 * @author Joubin <alphazero@sensesay.net>
 * @date:  Jan 30, 2012
 */
@Test(groups={"utils-datacodec-encode"})
public class TestDataCodecEncodeShortValues {

	public final static String RANDOM_SHORTS = "random-shorts";
	public final static String RANDOM_BYTEARRAY_SHORT_ARGS = "random-byte[]-short-args";
	
	private final Random random;
	
	public TestDataCodecEncodeShortValues() {
		random = new Random(System.nanoTime());
		Log.log("TEST - %s - Short values", DataCodec.class.getSimpleName());
	}
	
	// ------------------------------------------------------------------------
	//	TESTS
	// ------------------------------------------------------------------------
	
	@Test(dataProvider=RANDOM_SHORTS)
//	public void testConversion(Byte[] barr, short data) {
	public void testConversion(short data) {
		byte[] b = null;
		try {
			String dataStr = String.format("{ 0x%04X | %+06d }", data, data).toString();
//			Log.log("TEST - convert short to byte and back - provided %s", dataStr);
			String assertStr = String.format("DataCodec writeShort() -> readShort() with data %s", dataStr).toString(); 
			
//			b = toPrimitiveByteArray(barr);
			b = newPrimitiveByteArray(DataCodec.SHORT_BYTES);
			DataCodec.writeShort(data, b);
			short v = DataCodec.readShort(b);
			assertEquals(v, data, assertStr);
			
		} catch (NullPointerException e) {
			fail("writeShort with non-null buffer should not throw exception", e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			String msg = String.format("writeShort with %d length buffer should not throw exception", b.length).toString();
			fail(msg, e);
		}
	}
	
	public void testInvalidArg (Byte[] barr, short data){
		
	}
	// ------------------------------------------------------------------------
	//	DATA PROVIDERS
	// ------------------------------------------------------------------------
	
	static final public Byte[] newByteArray(int size) {
		Byte[] barray = new Byte[size];
		for(int i=0; i<size; i++)
			barray[i] = new Byte((byte) 0);
		return barray;
	}
	static final public byte[] newPrimitiveByteArray(int size) {
		return new byte[size];
	}
	static final public byte[] toPrimitiveByteArray(Byte[] barray){
		if(barray == null) 
			Log.error("barray is null: %s", (Object)barray);
		byte[] b = new byte[barray.length];
		for(int i=0;i<b.length;i++)
			b[i] = barray[i].byteValue();
		return b;
	}
	
	static final public byte[] nullByteArray() {
		return null;
	}
	@DataProvider(name=RANDOM_SHORTS)
	public Object[][] randomShorts() {
		
		final int cnt = Byte.MAX_VALUE * 2;
		
		Object[][] data = new Short[cnt][];
		try {
			for(int i=0; i<cnt; i++){
				data[i] = new Short[1];
			}
			int j = 0;
			data[j][0] = new Short((short) 0); j++;
			data[j][0] = Short.MAX_VALUE; j++;
			data[j][0] = Short.MIN_VALUE; j++;
			
			data[j][0] = new Short((short) 0x000F); j++;
			data[j][0] = new Short((short) 0x00FF); j++;
			data[j][0] = new Short((short) 0x0FFF); j++;
			data[j][0] = new Short((short) 0xFFFF); j++;
			
			data[j][0] = new Short((short) 0xF00F); j++;
			data[j][0] = new Short((short) 0x00F0); j++;
			data[j][0] = new Short((short) 0xF000); j++;
			data[j][0] = new Short((short) 0xF0F0); j++;
			
			data[j][0] = new Short((short) 0x0008); j++;
			data[j][0] = new Short((short) 0x0088); j++;
			data[j][0] = new Short((short) 0x0888); j++;
			data[j][0] = new Short((short) 0x8888); j++;
			
			data[j][0] = new Short((short) 0x8008); j++;
			data[j][0] = new Short((short) 0x0080); j++;
			data[j][0] = new Short((short) 0x8000); j++;
			data[j][0] = new Short((short) 0x8080); j++;
			
			// Random positive values
			for(int i=j; i<Byte.MAX_VALUE; i++){
				data[i][0] = new Short((short)random.nextInt(Short.MAX_VALUE));
			}
			// Random negative values
			for(int i=Byte.MAX_VALUE; i<cnt; i++){
				data[i][0] = new Short((short) (0 - random.nextInt(Short.MAX_VALUE)));
			}
		}
		catch (Throwable e){
			if(e.getCause() != null) {
				e.getCause().printStackTrace();
			}
			e.printStackTrace();
			fail("DATA PROVIDER ERROR");
		}
		return data;
	}

	@DataProvider(name=RANDOM_BYTEARRAY_SHORT_ARGS)
	public Object[][] randomValidArgs() {
		
		final int cnt = Byte.MAX_VALUE * 2;
		
		Object[][] data = new Object[cnt][];
		try {
			for(int i=0; i<cnt; i++){
				data[i] = new Object[2];
				data[i][0] = newByteArray(DataCodec.SHORT_BYTES);
			}
			int j = 0;
			data[j][1] = new Short((short) 0); j++;
			data[j][1] = Short.MAX_VALUE; j++;
			data[j][1] = Short.MIN_VALUE; j++;
			
			data[j][1] = new Short((short) 0x000F); j++;
			data[j][1] = new Short((short) 0x00FF); j++;
			data[j][1] = new Short((short) 0x0FFF); j++;
			data[j][1] = new Short((short) 0xFFFF); j++;
			
			data[j][1] = new Short((short) 0xF00F); j++;
			data[j][1] = new Short((short) 0x00F0); j++;
			data[j][1] = new Short((short) 0xF000); j++;
			data[j][1] = new Short((short) 0xF0F0); j++;
			
			data[j][1] = new Short((short) 0x0008); j++;
			data[j][1] = new Short((short) 0x0088); j++;
			data[j][1] = new Short((short) 0x0888); j++;
			data[j][1] = new Short((short) 0x8888); j++;
			
			data[j][1] = new Short((short) 0x8008); j++;
			data[j][1] = new Short((short) 0x0080); j++;
			data[j][1] = new Short((short) 0x8000); j++;
			data[j][1] = new Short((short) 0x8080); j++;
			
			// Random positive values
			for(int i=j; i<Byte.MAX_VALUE; i++){
				data[i][1] = new Short((short)random.nextInt(Short.MAX_VALUE));
			}
			// Random negative values
			for(int i=Byte.MAX_VALUE; i<cnt; i++){
				data[i][1] = new Short((short) (0 - random.nextInt(Short.MAX_VALUE)));
			}
		}
		catch (Throwable e){
			if(e.getCause() != null) {
				e.getCause().printStackTrace();
			}
			e.printStackTrace();
			fail("DATA PROVIDER ERROR");
		}
		return data;
	}
//	public static void main(String[] args) {
//		TestDataCodecShortValues t = new TestDataCodecShortValues();
//		Byte[] barray = newByteArray(DataCodec.SHORT_BYTES);
//		assertNotNull(barray);
//		for(int i=0; i<barray.length; i++)
//			assertNotNull(barray[i]);
//		Object[][] argarray = t.randomValidArgs();
//		assertNotNull(argarray);
//		Log.log("argarray.length: %d", argarray.length);
//		for(int i=0; i<argarray.length; i++){
//			assertNotNull(argarray[i]);
//			Byte[] ba = (Byte[]) argarray[i][0];
//			Short sh = (Short) argarray[i][1];
//			t.testConversion((Byte[])argarray[i][0], (Short)argarray[i][1]);
//		}
//	}
}
