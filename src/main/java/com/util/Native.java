package com.util;

public class Native {
	public native static void displayHelloWorld();

	// native method that prints a prompt and reads a line
	public native static String reverse(String prompt);

	public native static int gcdint(int ecx, int edx);

	public native static long gcdlong(long ecx, long edx);

	public native static int gcdinttemplate(int ecx, int edx);

	public native static long gcdlongtemplate(long ecx, long edx);
	
	public native static int main();

	public native static int service(String text);

	public native static double[][] SERVICE(String text);

	public native static int[] ner(String service, String text, int[] repertoire);

	public native static double[][][] NER(String service, String text, int[] repertoire);

	public native static double qatype(String text);
	
	public native static double phatic(String text);
	
	public native static double similarity(String x, String y);

	public native static double[] batch_similarity(String[] x, String[] y);

	public native static int asm6args(int rcx, int rdx, int r8, int r9, int fifthArg, int sixthArg);

	public native static double relu(double rcx);

	static {
		String LD_LIBRARY_PATH = System.getProperty("java.library.path");

		System.out.println("java.library.path = " + LD_LIBRARY_PATH);

		try {

			System.loadLibrary("eigen");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String input = "导航到播放下一站下一站传奇城乘风破浪";
		System.out.println("reverse what the user typed: \n" + input);
		String _input = Native.reverse(input);
		System.out.println(_input);

		int sum = Native.asm6args(1, 2, 3, 4, 5, 6);
		System.out.println("asm_6args, sum = " + sum);

		Native.displayHelloWorld();

		System.out.println("input = " + input);
		System.out.println("input.length = " + input.length());
//		double[][] arr = SERVICE(input);
//
//		System.out.println("index = " + arr);

		double x = 9.1;
		System.out.println("x = " + x);
		System.out.println("relu(x) = " + relu(x));

		x = -9.5;
		System.out.println("x = " + x);
		System.out.println("relu(x) = " + relu(x));

		System.out.println(
				"gcdlong(-10011001100110011001100110011001, -1171171171171171179) = " + gcdlong(-10011001, -117));
		Utility.Timer timer = new Utility.Timer();
		long a = -1001100118999999999l, b = -1171171171888888888l;
		int size = 10000000;
		timer.start();
		for (int i = 0; i < size; ++i) {
			gcdlong(a, b);
		}
		timer.report();

		System.out.println("gcdlongtemplate(-10011001, -117) = " + gcdlongtemplate(-10011001, -117));
		timer.start();
		for (int i = 0; i < size; ++i) {
			gcdlongtemplate(a, b);
		}
		timer.report();

		int _a = -1001100118, _b = -1171171171;
		System.out.println("gcdint(-10011001, -117) = " + gcdint(-10011001, -117));
		timer.start();
		for (int i = 0; i < size; ++i) {
			gcdint(_a, _b);
		}
		timer.report();

		System.out.println("gcdinttemplate(-10011001, -117) = " + gcdinttemplate(-10011001, -117));
		timer.start();
		for (int i = 0; i < size; ++i) {
			gcdinttemplate(_a, _b);
		}
		timer.report();

	}
}
