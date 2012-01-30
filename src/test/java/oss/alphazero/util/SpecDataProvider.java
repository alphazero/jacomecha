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

import static org.testng.Assert.fail;

import java.util.Random;

/**
 * @author Joubin <alphazero@sensesay.net>
 * @date:  Jan 30, 2012
 */
public class SpecDataProvider {
	private static final Random random = new Random(System.nanoTime());
	
	public static final Short[] rangeOfShorts () {
		final int cnt = Byte.MAX_VALUE * 2;
		
		Short[] data = new Short[cnt];
		try {
			int j = 0;
			data[j] = new Short((short) 0); j++;
			data[j] = Short.MAX_VALUE; j++;
			data[j] = Short.MIN_VALUE; j++;
			
			data[j] = new Short((short) 0x000F); j++;
			data[j] = new Short((short) 0x00FF); j++;
			data[j] = new Short((short) 0x0FFF); j++;
			data[j] = new Short((short) 0xFFFF); j++;
			
			data[j] = new Short((short) 0xF000); j++;
			data[j] = new Short((short) 0xFF00); j++;
			data[j] = new Short((short) 0xFFF0); j++;
			
			data[j] = new Short((short) 0xF888); j++;
			data[j] = new Short((short) 0xFF88); j++;
			data[j] = new Short((short) 0xFFF8); j++;
			
			data[j] = new Short((short) 0xF777); j++;
			data[j] = new Short((short) 0xFF77); j++;
			data[j] = new Short((short) 0xFFF7); j++;
			
			data[j] = new Short((short) 0x0007); j++;
			data[j] = new Short((short) 0x007F); j++;
			data[j] = new Short((short) 0x07FF); j++;
			data[j] = new Short((short) 0x7FFF); j++;

			data[j] = new Short((short) 0x7000); j++;
			data[j] = new Short((short) 0x7700); j++;
			data[j] = new Short((short) 0x7770); j++;
			
			data[j] = new Short((short) 0xFEEE); j++;
			data[j] = new Short((short) 0xFFEE); j++;
			data[j] = new Short((short) 0xFFFE); j++;
			
			data[j] = new Short((short) 0xF888); j++;
			data[j] = new Short((short) 0xFF88); j++;
			data[j] = new Short((short) 0xFFF8); j++;
			
			data[j] = new Short((short) 0xF777); j++;
			data[j] = new Short((short) 0xFF77); j++;
			data[j] = new Short((short) 0xFFF7); j++;
			
			data[j] = new Short((short) 0x0008); j++;
			data[j] = new Short((short) 0x008F); j++;
			data[j] = new Short((short) 0x08FF); j++;
			data[j] = new Short((short) 0x8FFF); j++;
			
			data[j] = new Short((short) 0x0008); j++;
			data[j] = new Short((short) 0x0088); j++;
			data[j] = new Short((short) 0x0888); j++;
			data[j] = new Short((short) 0x8888); j++;
			
			data[j] = new Short((short) 0x00F0); j++;
			data[j] = new Short((short) 0x0080); j++;
			data[j] = new Short((short) 0x0070); j++;
			
			// Random positive values
			for(int i=j; i<cnt/2; i++){
				data[i] = new Short((short)random.nextInt(Short.MAX_VALUE));
			}
			// Random negative values
			for(int i=Byte.MAX_VALUE; i<cnt; i++){
				data[i] = new Short((short) (0 - random.nextInt(Short.MAX_VALUE)));
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

	public static final Integer[] rangeOfIntegers () {
		final int cnt = Byte.MAX_VALUE * 2;
		
		Integer[] data = new Integer[cnt];
		try {
			int j = 0;
			data[j] = new Integer(0); j++;
			data[j] = Integer.MAX_VALUE; j++;
			data[j] = Integer.MIN_VALUE; j++;
			
			data[j] = new Integer(0x0000000F); j++;
			data[j] = new Integer(0x000000FF); j++;
			data[j] = new Integer(0x00000FFF); j++;
			data[j] = new Integer(0x0000FFFF); j++;
			data[j] = new Integer(0x000FFFFF); j++;
			data[j] = new Integer(0x00FFFFFF); j++;
			data[j] = new Integer(0x0FFFFFFF); j++;
			data[j] = new Integer(0xFFFFFFFF); j++;
			
			data[j] = new Integer(0x00000007); j++;
			data[j] = new Integer(0x0000007F); j++;
			data[j] = new Integer(0x000007FF); j++;
			data[j] = new Integer(0x00007FFF); j++;
			data[j] = new Integer(0x0007FFFF); j++;
			data[j] = new Integer(0x007FFFFF); j++;
			data[j] = new Integer(0x07FFFFFF); j++;
			data[j] = new Integer(0x7FFFFFFF); j++;

			data[j] = new Integer(0x00000008); j++;
			data[j] = new Integer(0x00000080); j++;
			data[j] = new Integer(0x00000800); j++;
			data[j] = new Integer(0x00008000); j++;
			data[j] = new Integer(0x00080000); j++;
			data[j] = new Integer(0x00800000); j++;
			data[j] = new Integer(0x08000000); j++;
			data[j] = new Integer(0x80000000); j++;
			
			data[j] = new Integer(0x00000008); j++;
			data[j] = new Integer(0x0000008F); j++;
			data[j] = new Integer(0x000008FF); j++;
			data[j] = new Integer(0x00008FFF); j++;
			data[j] = new Integer(0x0008FFFF); j++;
			data[j] = new Integer(0x008FFFFF); j++;
			data[j] = new Integer(0x08FFFFFF); j++;
			data[j] = new Integer(0x8FFFFFFF); j++;
			
			data[j] = new Integer(0xF0000000); j++;
			data[j] = new Integer(0xFF000000); j++;
			data[j] = new Integer(0xFFF00000); j++;
			data[j] = new Integer(0xFFFF0000); j++;
			data[j] = new Integer(0xFFFFF000); j++;
			data[j] = new Integer(0xFFFFFF00); j++;
			data[j] = new Integer(0xFFFFFFF0); j++;
			
			data[j] = new Integer(0x70000000); j++;
			data[j] = new Integer(0x7F000000); j++;
			data[j] = new Integer(0x7F000000); j++;
			data[j] = new Integer(0x7FF00000); j++;
			data[j] = new Integer(0x7FFF0000); j++;
			data[j] = new Integer(0x7FFFF000); j++;
			data[j] = new Integer(0x7FFFFF00); j++;
			data[j] = new Integer(0x7FFFFFF0); j++;
			
			data[j] = new Integer(0x80000000); j++;
			data[j] = new Integer(0x8F000000); j++;
			data[j] = new Integer(0x8F000000); j++;
			data[j] = new Integer(0x8FF00000); j++;
			data[j] = new Integer(0x8FFF0000); j++;
			data[j] = new Integer(0x8FFFF000); j++;
			data[j] = new Integer(0x8FFFFF00); j++;
			data[j] = new Integer(0x8FFFFFF0); j++;
			
			data[j] = new Integer(0x000000F0); j++;
			data[j] = new Integer(0x00000080); j++;
			
			// Random positive values
			for(int i=j; i<cnt/2; i++){
				data[i] = new Integer(random.nextInt(Integer.MAX_VALUE));
			}
			// Random negative values
			for(int i=Byte.MAX_VALUE; i<cnt; i++){
				data[i] = new Integer((0 - random.nextInt(Integer.MAX_VALUE)));
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
	public static void main(String[] args) {
		Integer[] range = rangeOfIntegers();
		for(Integer data : range){
			String dataStr = String.format("{ 0x%08X | %+013d }", data, data).toString();
			Log.log("TEST - convert int to byte and back - provided %s", dataStr);
		}

	}
}
