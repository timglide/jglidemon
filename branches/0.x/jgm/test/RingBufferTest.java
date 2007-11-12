package jgm.test;

import jgm.util.RingBuffer;
import java.util.*;

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
