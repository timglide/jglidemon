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
package jgm.test;

import jgm.util.RingBuffer;

public class RingBufferTest {
	public static void main(String[] args) {
		RingBuffer<Integer> rb = new RingBuffer<Integer>(5);
		
		for (int i = 0; i < 5; i++) {
			System.out.printf("Inserting %s\n", i);
			rb.add(i);
			
			System.out.printf("Size is %s, expecting %s\n", rb.size(), i+1);
		}
		
		System.out.println("Iterating... ");

		for (Integer i : rb) {
			System.out.println(i);
		}
		
		System.out.println();
		
		for (int i = 5; i < 7; i++) {
			System.out.printf("Inserting %s\n", i);
			rb.add(i);
			
			System.out.printf("Size is %s, expecting 5\n", rb.size());
		}
		
		System.out.println("Iterating... ");
		for (Integer i : rb) {
			System.out.println(i);
		}
		
		System.out.println();
		
		for (int i = 7; i < 9; i++) {
			System.out.printf("Inserting %s\n", i);
			rb.add(i);
			
			System.out.printf("Size is %s, expecting 5\n", rb.size());
		}
		
		System.out.println("Iterating... ");
		for (Integer i : rb) {
			System.out.println(i);
		}
	}
}
