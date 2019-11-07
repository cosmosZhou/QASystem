package com.util;

import java.io.File;
import java.nio.file.Path;

public class Native {
	public native void displayHelloWorld();

	// native method that prints a prompt and reads a line
	public native static String reverse(String prompt);

	static {		
//		System.out.println("initially, java.library.path = " + System.getProperty("java.library.path"));
//		File path = new File(Utility.workingDirectory + "lib");		
//		System.setProperty("java.library.path", path.getAbsolutePath());
//		System.out.println("java.library.path = " + System.getProperty("java.library.path"));
//		System.loadLibrary("std");
//		System.loadLibrary("Native");
		System.loadLibrary(Utility.workingDirectory + "lib/std");
		System.loadLibrary(Utility.workingDirectory + "lib/Native");
	}
	
	public static void main(String[] args) {
		Native Native = new Native();
		// Native.displayHelloWorld();
		String input = Native.reverse("123456");
		System.out.println("reverse what the user typed: \n" + input);
	}
}
