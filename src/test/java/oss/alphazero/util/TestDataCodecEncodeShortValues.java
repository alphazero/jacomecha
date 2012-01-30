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

package oss.alphazero.util;

import static org.testng.Assert.*;

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
	
	public TestDataCodecEncodeShortValues() {
		Log.log("TEST - %s - Short values", DataCodec.class.getSimpleName());
	}
	
	// ------------------------------------------------------------------------
	//	TESTS
	// ------------------------------------------------------------------------
	
	@Test(dataProvider=RANDOM_SHORTS)
	public void testConversion(short data) {
		byte[] b = null;
		try {
			String dataStr = String.format("{ 0x%04X | %+06d }", data, data).toString();
//			Log.log("TEST - convert short to byte and back - provided %s", dataStr);
			String assertStr = String.format("DataCodec writeShort() -> readShort() with data %s", dataStr).toString(); 
			

			b = newPrimitiveByteArray(DataCodec.SHORT_BYTES);
			DataCodec.writeShort(data, b);
			short v = DataCodec.readShort(b);
			assertEquals(v, data, assertStr);
			
		} catch (NullPointerException e) {
			fail("writeShort with non-null buffer should not throw exception", e);
		} catch (IllegalArgumentException e) {
			String msg = String.format("writeShort with %d length buffer should not throw exception", b.length).toString();
			fail(msg, e);
		}
	}
	
	@Test(dataProvider=RANDOM_BYTEARRAY_SHORT_ARGS)
	public void testConversion2(Byte[] barr, short data) {
		byte[] b = null;
		try {
			String dataStr = String.format("{ 0x%04X | %+06d }", data, data).toString();
//			Log.log("TEST - convert short to byte and back - provided %s", dataStr);
			String assertStr = String.format("DataCodec writeShort() -> readShort() with data %s", dataStr).toString(); 
			
			b = toPrimitiveByteArray(barr);
			
			DataCodec.writeShort(data, b);
			short v = DataCodec.readShort(b);
			assertEquals(v, data, assertStr);
			
		} catch (NullPointerException e) {
			fail("writeShort with non-null buffer should not throw exception", e);
		} catch (IllegalArgumentException e) {
			String msg = String.format("writeShort with %d length buffer should not throw exception", b.length).toString();
			fail(msg, e);
		}
	}
	
//	public void testInvalidArg (Byte[] barr, short data){
//		
//	}
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
		
		Short[] shorts = SpecDataProvider.rangeOfShorts();
		final int cnt = shorts.length;
		Object[][] data = new Object[cnt][];
		try {
			for(int i=0; i<cnt; i++){
				data[i] = new Object[1];
				data[i][0] = shorts[i];
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
		
		Short[] shorts = SpecDataProvider.rangeOfShorts();
		final int cnt = shorts.length;
		
		Object[][] data = new Object[cnt][];
		try {
			for(int i=0; i<cnt; i++){
				data[i] = new Object[2];
				data[i][0] = newByteArray(DataCodec.SHORT_BYTES);
				data[i][1] = shorts[i];
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
//		TestDataCodecEncodeShortValues t = new TestDataCodecEncodeShortValues();
//		Byte[] barray = newByteArray(DataCodec.SHORT_BYTES);
//		assertNotNull(barray);
//		for(int i=0; i<barray.length; i++)
//			assertNotNull(barray[i]);
//		Object[][] argarray2 = t.randomValidArgs();
//		Object[][] argarray = t.randomShorts();
//		assertNotNull(argarray);
//		Log.log("argarray.length: %d", argarray.length);
//		for(int i=0; i<argarray.length; i++){
//			assertNotNull(argarray[i]);
//			Byte[] ba = (Byte[]) argarray2[i][0];
//			Short sh = (Short) argarray2[i][1];
//			t.testConversion((Short)argarray[i][0]);
//			t.testConversion2((Byte[])argarray2[i][0], (Short)argarray2[i][1]);
//		}
//	}
}
