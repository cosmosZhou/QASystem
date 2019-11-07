package com.util;

import java.util.Arrays;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class SingularValueDecomposition {
	/**
	 * document dimensionality is 6; lexeme dimensionality is 5; that a document
	 * is interpreted as a 5-dimensional vector, the goal is to reduce the
	 * document dimensionality by SVD algorithm. A = USV, where U and V are
	 * orthonormal, ie, U`U = V`V = I;
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		test2();
		test1();
		// test();
	}

	static void test2() throws Exception{
		int w = 5, d = 2;
		double matrixA[][] = {{1,   0,   1,   1,   0},
		   {0,   1,   1,   0,   0},
		   {1,   0,   0,   0,   0},
		   {0,   0,   0,   1,   1},
		   {0,   0,   0,   1,   0},
		   {0,   0,   0,   0,   1}};
		
	
		double [][] res = SVD(matrixA, 2);
		Matrix B = new Matrix(res);
		B.print(w, d);
		System.out.println("correlation matrix = ");
		B.times(B.transpose()).print(w, d);
		System.out.println("res = " + Arrays.toString(res));
		
		Matrix A = new Matrix(matrixA);
		normalize(A);
		System.out.println("correlation matrix = ");
		A.times(A.transpose()).print(w, d);

	}
	
	static public double [][] SVD(double matrixA[][], int dimensionDesired) throws Exception {
		Matrix A = new Matrix(matrixA);
		if (A.getRowDimension() < A.getColumnDimension())
			throw new Exception("A.getRowDimension() < A.getColumnDimension()");

		if (A.getColumnDimension() < dimensionDesired)
			throw new Exception("A.getColumnDimension() < dimensionDesired");

		Jama.SingularValueDecomposition svd = A.svd();
//		System.out.println("U = ");
		Matrix U = svd.getU();

//		int w = 5, d = 2;
//		U.print(w, d);

//		System.out.println("S = ");
		Matrix S = svd.getS();
//		S.print(w, d);

//		System.out.println("V = ");
//		Matrix V = svd.getV();
//		V.print(w, d);
//		System.out.println("A - USV` = ");
//		A.minus(U.times(S).times(V.transpose())).print(w, d);
		// System.out.println("VV` = ");
		// V.times(V.transpose()).print(w, d);
		//
		// System.out.println("V`V = ");
		// V.transpose().times(V).print(w, d);

		Matrix B = U.getMatrix(0, U.getRowDimension() - 1, 0, dimensionDesired - 1)
				.times(S.getMatrix(0, dimensionDesired - 1, 0, dimensionDesired - 1));
//		System.out.println("B = ");
//		B.print(w, d);
//		B.transpose().print(w, d);
		// U.times(U.transpose()).print(w, d);

		// Matrix B = S.times(D);
		normalize(B);
//		System.out.println("B = ");
//		B.print(w, d);
		return B.getArray();
	}

	static void normalize(Matrix B){
		for (int i = 0; i < B.getRowDimension(); ++i) {
			double sum = 0;
			for (int j = 0; j < B.getColumnDimension(); ++j) {
				sum += B.get(i, j) * B.get(i, j);
			}
			sum = Math.sqrt(sum);
			for (int j = 0; j < B.getColumnDimension(); ++j) {
				B.set(i, j, B.get(i, j) / sum);
			}
		}
	}
	
	static void test1() {
		int w = 5, d = 2;
		double matrixA[][] = { { 1, 0, 1, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0 }, { 1, 0, 0, 1, 1, 0 },
				{ 0, 0, 0, 1, 0, 1 } };

		double matrixT[][] = { { -0.44, -0.13, -0.48, -0.70, -0.26 }, { -0.30, -0.33, -0.51, 0.35, 0.65 },
				{ 0.57, -0.59, -0.37, 0.15, -0.41 }, { 0.58, 0.00, 0.00, -0.58, 0.58 },
				{ 0.25, 0.73, -0.61, 0.16, -0.09 } };

		double matrixS[][] = { { 2.16, 0.00, 0.00, 0.00, 0.00 }, { 0.00, 1.59, 0.00, 0.00, 0.00 },
				{ 0.00, 0.00, 1.28, 0.00, 0.00 }, { 0.00, 0.00, 0.00, 1.00, 0.00 }, { 0.00, 0.00, 0.00, 0.00, 0.39 } };

		double matrixD[][] = { { -0.75, -0.28, -0.20, -0.45, -0.33, -0.12 }, { -0.29, -0.53, -0.19, 0.63, 0.22, 0.41 },
				{ 0.28, -0.75, 0.45, -0.20, 0.12, -0.33 }, { 0.00, 0.00, 0.58, 0.00, -0.58, 0.58 },
				{ -0.53, 0.29, 0.63, 0.19, 0.41, -0.22 } };

		Matrix A = new Matrix(matrixA);
		Matrix T = new Matrix(matrixT);
		Matrix S = new Matrix(matrixS);
		Matrix D = new Matrix(matrixD);
//		EigenvalueDecomposition eig = A.eig();
//		eig.

		// // SMatrix.svd();
		// System.out.println("A = ");
		// A.print(w, d);
		//
		// System.out.println("T = ");
		// T.print(w, d);
		//
		// System.out.println("S = ");
		// S.print(w, d);
		//
		// System.out.println("D = ");
		// D.print(w, d);

		// A - T`S D
		// Matrix dif = A.minus(T.transpose().times(S).times(D));
		// System.out.println("dif = ");
		// dif.print(w, d);

		A = A.transpose();
		System.out.println("A = ");
		A.print(w, d);
		// System.out.println("A`A = ");
		//
		//
		// A.transpose().times(A).print(w, d);
		// System.out.println("AA` = ");
		// A.times(A.transpose()).print(w, d);

		Jama.SingularValueDecomposition svd = A.svd();
		System.out.println("U = ");
		Matrix U = svd.getU();
		U.print(w, d);
		// System.out.println("U`U = ");
		// //the column vector of U are orthogonal
		// U.transpose().times(U).print(w, d);
		// 0.7 -0.3 0.3 -0.0 0.5
		// 0.3 -0.5 -0.7 0.0 -0.3
		// 0.2 -0.2 0.4 -0.6 -0.6
		// 0.4 0.6 -0.2 0.0 -0.2
		// 0.3 0.2 0.1 0.6 -0.4
		// 0.1 0.4 -0.3 -0.6 0.2

		System.out.println("S = ");
		S = svd.getS();
		S.print(w, d);

		System.out.println("V = ");
		Matrix V = svd.getV();
		V.print(w, d);
		System.out.println("A - USV` = ");
		A.minus(U.times(S).times(V.transpose())).print(w, d);
		// System.out.println("VV` = ");
		// V.times(V.transpose()).print(w, d);
		//
		// System.out.println("V`V = ");
		// V.transpose().times(V).print(w, d);

		Matrix B = U.getMatrix(0, U.getRowDimension() - 1, 0, 1).times(S.getMatrix(0, 1, 0, 1));
		System.out.println("B = ");
		B.print(w, d);
		B.transpose().print(w, d);
		// U.times(U.transpose()).print(w, d);

		// Matrix B = S.times(D);

		for (int i = 0; i < B.getRowDimension(); ++i) {
			double sum = 0;
			for (int j = 0; j < B.getColumnDimension(); ++j) {
				sum += B.get(i, j) * B.get(i, j);
			}
			sum = Math.sqrt(sum);
			for (int j = 0; j < B.getColumnDimension(); ++j) {
				B.set(i, j, B.get(i, j) / sum);
			}
		}
		System.out.println("B = ");
		B.transpose().print(w, d);
		//
		B.times(B.transpose()).print(w, d);
		// B.transpose().times(B).print(w, d);
	}

	static void test() {
		int w = 5, d = 2;
		double SMatrix[][] = { { 2.16, 0.00, 0.00, 0.00, 0.00 }, { 0.00, 1.59, 0.00, 0.00, 0.00 },
				{ 0.00, 0.00, 1.28, 0.00, 0.00 }, { 0.00, 0.00, 0.00, 1.00, 0.00 }, { 0.00, 0.00, 0.00, 0.00, 0.39 } };

		double DMatrix[][] = { { -0.75, -0.28, -0.20, -0.45, -0.33, -0.12 }, { -0.29, -0.53, -0.19, 0.63, 0.22, 0.41 },
				{ 0.28, -0.75, 0.45, -0.20, 0.12, -0.33 }, { 0.00, 0.00, 0.58, 0.00, -0.58, 0.58 },
				{ -0.53, 0.29, 0.63, 0.19, 0.41, -0.22 } };

		// Matrix B = new Matrix(BMatrix);
		Matrix D = new Matrix(DMatrix);
		Matrix S = new Matrix(SMatrix);

		// // D.transpose().times(D).print(w, d);
		D = D.getMatrix(0, 1, 0, 5);
		S = S.getMatrix(0, 1, 0, 1);
		Matrix B = S.times(D);
		B.print(w, d);

		for (int j = 0; j < B.getColumnDimension(); ++j) {
			double sum = 0;
			for (int i = 0; i < B.getRowDimension(); ++i) {
				sum += B.get(i, j) * B.get(i, j);
			}
			sum = Math.sqrt(sum);
			for (int i = 0; i < B.getRowDimension(); ++i) {
				B.set(i, j, B.get(i, j) / sum);
			}
		}
		B.print(w, d);
		Matrix Correlation = B.transpose().times(B);

		// double diagonal[] = new double[Correlation.getRowDimension()];
		//
		// for (int i = 0; i < diagonal.length; ++i) {
		// diagonal[i] = Math.sqrt(Correlation.get(i, i));
		// }
		//
		// for (int j = 0; j < Correlation.getColumnDimension(); ++j) {
		// for (int i = 0; i < Correlation.getRowDimension(); ++i) {
		// Correlation.set(i, j, Correlation.get(i, j) / diagonal[i]
		// / diagonal[j]);
		// }
		// }
		//
		Correlation.print(w, d);
	}
}