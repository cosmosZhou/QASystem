package com.deeplearning.utils;

import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

public class Activator {

	public static double logistic(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	public static double tanh(double x) {
		double e = Math.exp(2 * x);
		return (e - 1) / (e + 1);
	}

	// public static double tanh(double x) {
	// return 2 * logistic(2 * x) - 1;
	// }

	public static double hard_sigmoid(double x) {
		if (x < -2.5)
			return 0;
		if (x > 2.5)
			return 1;
		return 0.2 * x + 0.5;
	}

	public static double hard_sigmoidDerivative(double y) {
		if (y < 0 || y > 1)
			return 0;
		return 0.2;
	}

	public static float hard_sigmoid(float x) {
		if (x < -2.5)
			return 0;
		if (x > 2.5)
			return 1;
		return 0.2f * x + 0.5f;
	}

	public static DoubleMatrix hard_sigmoid(DoubleMatrix X) {
		DoubleMatrix _X = new DoubleMatrix(X.rows, X.columns);
		for (int i = 0; i < _X.length; ++i) {
			_X.put(i, hard_sigmoid(X.get(i)));
		}
		return _X;
	}

	public static DoubleMatrix hard_sigmoidDerivative(DoubleMatrix y) {
		DoubleMatrix dy = new DoubleMatrix(y.rows, y.columns);
		for (int i = 0; i < dy.length; ++i) {
			dy.put(i, hard_sigmoidDerivative(y.get(i)));
		}
		return dy;
	}

	public static FloatMatrix hard_sigmoid(FloatMatrix X) {
		FloatMatrix _X = new FloatMatrix(X.rows, X.columns);
		for (int i = 0; i < _X.length; ++i) {
			_X.put(i, hard_sigmoid(X.get(i)));
		}
		return _X;
	}

	public static DoubleMatrix logistic(DoubleMatrix X) {
		return MatrixFunctions.pow(MatrixFunctions.exp(X.mul(-1)).add(1), -1);
	}

	public static DoubleMatrix logisticDerivative(DoubleMatrix input) {
		DoubleMatrix output = new DoubleMatrix(input.rows, input.columns);
		for (int i = 0; i < output.length; ++i) {
			double a = input.get(i);
			output.put(i, a * (1 - a));
		}
		return output;
	}

	public static FloatMatrix logistic(FloatMatrix X) {
		return MatrixFunctions.pow(MatrixFunctions.exp(X.mul(-1)).add(1), -1);
	}

	public static DoubleMatrix tanh(DoubleMatrix X) {
		return MatrixFunctions.tanh(X);
	}

	public static FloatMatrix tanh(FloatMatrix X) {
		return MatrixFunctions.tanh(X);
	}

	public static DoubleMatrix ReLU(DoubleMatrix X) {
		DoubleMatrix pIndex = X.gt(0);
		return X.mul(pIndex);
	}

	public static DoubleMatrix ReLUDerivative(DoubleMatrix X) {
		return X.gt(0);
	}

	public static FloatMatrix ReLU(FloatMatrix X) {
		FloatMatrix pIndex = X.gt(0);
		return X.mul(pIndex);
	}

	// rows: samples
	public static DoubleMatrix softmax(DoubleMatrix X) {
		DoubleMatrix expM = MatrixFunctions.exp(X);
		for (int i = 0; i < X.rows; i++) {
			DoubleMatrix expMi = expM.getRow(i);
			expM.putRow(i, expMi.div(expMi.sum()));
		}
		return expM;
	}

	public static FloatMatrix softmax(FloatMatrix X) {
		FloatMatrix expM = MatrixFunctions.exp(X);
		for (int i = 0; i < X.rows; i++) {
			FloatMatrix expMi = expM.getRow(i);
			expM.putRow(i, expMi.div(expMi.sum()));
		}
		return expM;
	}

	static public DoubleMatrix deriveExp(DoubleMatrix f) {
		return f.mul(DoubleMatrix.ones(1, f.length).sub(f));
	}

	static public DoubleMatrix tanhDerivative(DoubleMatrix f) {
		return DoubleMatrix.ones(1, f.length).sub(MatrixFunctions.pow(f, 2));
	}

	static public DoubleMatrix l2_normalize(DoubleMatrix f) {
		double norm2 = f.norm2();
		return f.div(norm2);
	}
}
