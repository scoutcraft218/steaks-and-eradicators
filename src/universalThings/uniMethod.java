package universalThings;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.*;


public class uniMethod {
	static int overCounter = 0; // used in overload method
	static Scanner myScanner = new Scanner(System.in);
	
	public static void line(int num, char symbol) { //print a line break
		for(int i = 0; i < num; i++) {
			System.out.print(symbol);
		}
		System.out.println();
	}
	
	//utility methods *************************************************************************
	
	public static void overload(int amount) { // used to stop infinite loops in case
		overCounter++;
		if (overCounter == amount) {
			
			throw new IllegalArgumentException("Overload activated " + amount + " times.");
//			System.out.println("this loop has overloaded, activated infinite while loop.");
//			while (true) {
//			}
		}
	}
	
	public static void overloadReset() {
		overCounter = 0;
	}
	
	public static void wipeArray(int[] array) { // replace int array with 0
		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}
	}
	
	public static void wipeArray(String[] array) { // replace string array with ""
		for (int i = 0; i < array.length; i++) {
			array[i] = "";
		}
	}
	
	public static void wipeArray(String[] array, String replace) { // replace string array with input string
		for (int i = 0; i < array.length; i++) {
			array[i] = replace;
		}
	}
	
	public static void wipeArray(boolean[] array) { // replace string array with input string
		for (int i = 0; i < array.length; i++) {
			array[i] = false;
		}
	}
	
	// print array
	public static void printArray(int[] array) { // smoother way to print arrays
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			if (i != array.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("]");
		System.out.println();
	}
	
	public static void printArray(double[] array) { // smoother way to print arrays
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			if (i != array.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("]");
		System.out.println();
	}
	
	public static void printArray(String[] array) { // smoother way to print arrays
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			if (i != array.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("]");
		System.out.println();
	}
	
	public static void printArray(boolean[] array) { // smoother way to print arrays
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			if (i != array.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("]");
		System.out.println();
	}
	
	public static void printArray(ArrayList array) { // smoother way to print arrays
		System.out.print("[");
		for (int i = 0; i < array.size(); i++) {
			System.out.print(array.get(i));
			if (i != array.size() - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("]");
		System.out.println();
	}
	
//	public static void printArray(ArrayList<String> array) { // smoother way to print arrays
//		System.out.print("[");
//		for (int i = 0; i < array.size(); i++) {
//			System.out.print(array.get(i));
//			if (i != array.size() - 1) {
//				System.out.print(", ");
//			}
//		}
//		System.out.print("]");
//		System.out.println();
//	}
//	
	// print array
	public static void printArray(File[] array) { // smoother way to print arrays
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			if (i != array.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("]");
		System.out.println();
	}
	
	public static void printArray(Object[] array) {
		System.out.print("[");
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			if (i != array.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("]");
		System.out.println();
	}
	
	
	// return array
	public static String returnArray(int[] array) {
		String holder = "";
		
		holder += "[";
		for (int i = 0; i < array.length; i++) {
			holder += array[i];
			if (i != array.length - 1) {
				holder += ", ";
			}
		}
		holder += "]";
		return holder;
	}
	
	public static String returnArray(String[] array) {
		String holder = "";
		
		holder += "[";
		for (int i = 0; i < array.length; i++) {
			holder += array[i];
			if (i != array.length - 1) {
				holder += ", ";
			}
		}
		holder += "]";
		return holder;
	}
	
	public static String returnArray(boolean[] array) {
		String holder = "";
		
		holder += "[";
		for (int i = 0; i < array.length; i++) {
			holder += array[i];
			if (i != array.length - 1) {
				holder += ", ";
			}
		}
		holder += "]";
		return holder;
	}
	
	public static void copyArray(int[] toReference, int[] toCopy) {
		for (int i = 0; i < toReference.length; i++) {
			toCopy[i] = toReference[i];
		}
	}
	
	public static int countDigits(int x) { // count the digits of a number
		return Integer.toString(x).length();
	}
	
		
		
		

		
		

		
		//print randomstuff
		//get an input
		//if (nextKey)
		//	change menuEncoder
		//	repeat
		//else if (validKey || 'q')
		//	break
		//	set parameters outside the loop
		//else
		//	loop
		//repeat for next menuInterface pos
		//pressing q sets current pos to 0 and loads next menu, -1 if on pos 1
	
	
}