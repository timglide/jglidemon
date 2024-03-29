/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 Tim
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * -----LICENSE END-----
 */
package jgm.util;

import java.util.*;

/**
 * A simple generic ringbuffer that implements
 * the List<T> interface
 * @author Tim
 *
 */
/*

 0 1 2 3 4 5 6
+-+-+-+-+-+-+-+
| | | | | | | |
+-+-+-+-+-+-+-+
          
 */

@SuppressWarnings("unchecked")
public class RingBuffer<T> implements List<T>, RandomAccess {
	T[] data;
	int first = 0, count = 0;
	int nextInsert = 0;
	int totalInserts = 0;
	
	public RingBuffer(int capacity) {
		if (capacity < 1)
			throw new IllegalArgumentException("Capacity must be >= 1");
		
		data = (T[]) new Object[capacity]; // stupid fucking java
	}
	
	public int size() {
		return count;
	}
	
	public int capacity() {
		return data.length;
	}
	
	/**
	 * @return how many times {@link #add(T)} has been called over
	 * the lifetime of this object
	 */
	// 
	public int totalInserts() {
		return totalInserts;
	}
	
	public T get(int position) {
		if (position > size())
			throw new ArrayIndexOutOfBoundsException(String.format("position: %s; size: %s;", position, size()));
		
		int index = first + position;
		
		if (index >= data.length) {
			index -= data.length;
		}
		
//		System.out.printf("Getting position %s from index %s\n", position, index);
		
		return data[index];
	}
	
	public boolean add(T value) {
		if (value == null)
			throw new NullPointerException("Cannot add null to RingBuffer");
		
		data[nextInsert] = value;
//		System.out.printf("Added %s at %s\n", data[nextInsert], nextInsert);
		
		// this insert necessitates overriting
		// the first value
		if (nextInsert == first && count > 0) {
			first++;
			
			if (first == data.length)
				first = 0;
		}
		
		nextInsert++;
		
		if (nextInsert == data.length)
			nextInsert = 0;
		
		if (count < data.length)
			count++;
		
		totalInserts++;
		
		return true;
	}
	
	public void add(int index, T value) {
		throw new UnsupportedOperationException();
	}
	
	public boolean addAll(Collection<? extends T> t) {
		throw new UnsupportedOperationException();
	}
	
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}
	
	public T set(int i, T t) {
		throw new UnsupportedOperationException();
	}
	
	public T remove(int i) {
		throw new UnsupportedOperationException();
	}
	
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}
	
	public void clear() {
		first = 0;
		count = 0;
		nextInsert = 0;
		Arrays.fill(data, null);
	}
	
	public T[] toArray() {
		T[] ret = (T[]) new Object[size()];
		
		int i = 0;
		for (T t : this) {
			ret[i++] = t;
		}
		
		return ret;
	}
	
	public <E> E[] toArray(E[] arr) {
		if (arr.length != size())
			arr = (E[]) new Object[size()];
		
		int i = 0;
		for (T t : this) {
			arr[i++] = (E) t;
		}
		
		return arr;
	}
	
	public List<T> subList(int from, int to) {
		RingBuffer<T> ret = new RingBuffer<T>(to - from);
		
		for (int i = from; i < to; i++) {
			ret.add(get(i));
		}
		
		return ret;
	}
	
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}
	
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) return false;
		}
		
		return true;
	}
	
	public int indexOf(Object o) {	
		int i = 0;
		
		for (T t : this) {
			if (t.equals(o)) {
				return i;
			}
			
			i++;
		}
		
		return -1;
	}
	
	public int lastIndexOf(Object o) {
		int ret = -1;

		int i = 0;
		for (T t : this) {
			if (t.equals(o)) {
				ret = i++;
			}
		}
		
		return ret;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	public Iterator<T> iterator() {
		return listIterator();
	}
	
	public ListIterator<T> listIterator() {
		return listIterator(0);
	}
	
	public ListIterator<T> listIterator(final int start) {
		return new ListIterator<T>() {
			int cur = start;
			
			public boolean hasNext() {
				return data.length > 0 && cur < size();
			}
			
			public int nextIndex() {
				return cur;
			}
			
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				
				return get(cur++);
			}
			
			public boolean hasPrevious() {
				return data.length > 0 && cur > 0;
			}
			
			public int previousIndex() {
				return cur - 1;
			}
			
			public T previous() {
				if (!hasPrevious())
					throw new NoSuchElementException();
				
				return get(--cur);
			}
			
			public void add(T t) {
				throw new UnsupportedOperationException();
			}
			
			public void set(T t) {
				throw new UnsupportedOperationException();
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
