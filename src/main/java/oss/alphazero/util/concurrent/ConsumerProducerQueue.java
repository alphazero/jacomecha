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

/**
 * This class simply exposes a public constructor, but otherwise does not
 * modify the superclass {@link Concurrent2LockQueueBase}.
 * <p> 
 * The use-case for this class is restricted to 2 concurrent threads, one 
 * acting as a producer and the other a consumer.  Note that this does not
 * mean the same thread must always be in role of producer or consumer. It
 * simply means that this construct provides a thread-safe concurrent 1:1
 * activity.     
 * 
 * @see Concurrent2LockQueueBase
 * @author Joubin <alphazero@sensesay.net>
 *
 * @param <E>
 */
public class ConsumerProducerQueue<E> extends Concurrent2LockQueueBase<E> {
	public ConsumerProducerQueue() {
		super();
	}
}
