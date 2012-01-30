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
			
			data[j] = new Short((short) 0xF00F); j++;
			data[j] = new Short((short) 0x00F0); j++;
			data[j] = new Short((short) 0xF000); j++;
			data[j] = new Short((short) 0xF0F0); j++;
			
			data[j] = new Short((short) 0x0008); j++;
			data[j] = new Short((short) 0x0088); j++;
			data[j] = new Short((short) 0x0888); j++;
			data[j] = new Short((short) 0x8888); j++;
			
			data[j] = new Short((short) 0x8008); j++;
			data[j] = new Short((short) 0x0080); j++;
			data[j] = new Short((short) 0x8000); j++;
			data[j] = new Short((short) 0x8080); j++;
			
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
}
