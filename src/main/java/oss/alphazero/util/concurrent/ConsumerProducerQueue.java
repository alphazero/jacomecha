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
