package com.deeplearning;

import java.io.IOException;
import java.io.Serializable;

import org.jblas.DoubleMatrix;

import com.deeplearning.utils.Activation;
import com.util.Utility;

public class Dense implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DoubleMatrix wDense;
	DoubleMatrix bDense;

	public Dense(DoubleMatrix wDense, DoubleMatrix bDense) {
		this.wDense = wDense;
		this.bDense = bDense;
	}

	public DoubleMatrix call(DoubleMatrix x) {
		DoubleMatrix y = x.mmul(wDense);
		if (bDense != null) {
			y = y.addi(bDense);
		}

		if (activation != null)
			return activation.activate(y);
		return y;
	}

	public DoubleMatrix[] call(DoubleMatrix X[]) {
		DoubleMatrix[] lDense = new DoubleMatrix[X.length];
		for (int i = 0; i < lDense.length; ++i)
			lDense[i] = call(X[i]);

		return lDense;
	}

	Activation activation = null;// Activation.softmax;

	public Dense(Utility.BinaryReader dis) throws IOException {
		this(dis, true);
	}

	public Dense(Utility.BinaryReader dis, boolean bias) throws IOException {
		wDense = new DoubleMatrix(dis.readArray2());
		if (bias)
			bDense = new DoubleMatrix(dis.readArray1());
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
