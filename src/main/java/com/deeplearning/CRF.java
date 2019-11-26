package com.deeplearning;

import java.io.IOException;
import java.io.Serializable;

import org.jblas.DoubleMatrix;

import com.deeplearning.utils.Activation;
import com.util.Utility;

public class CRF implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DoubleMatrix bias;
	private DoubleMatrix G;
	private DoubleMatrix kernel;
	private DoubleMatrix left_boundary;
	private DoubleMatrix right_boundary;
	private Activation activation = Activation.linear;

	public CRF(DoubleMatrix kernel, DoubleMatrix G, DoubleMatrix bias, DoubleMatrix left_boundary,
			DoubleMatrix right_boundary) {
		this.bias = bias;

		this.G = G;

		this.kernel = kernel;

		this.left_boundary = left_boundary;

		this.right_boundary = right_boundary;
	}

	public DoubleMatrix[] viterbi_one_hot(DoubleMatrix[] X) {
		int[] label = call(X);
		DoubleMatrix eye = DoubleMatrix.eye(bias.length);
		DoubleMatrix[] oneHot = new DoubleMatrix[label.length];
		for (int i = 0; i < oneHot.length; ++i) {
			oneHot[i] = eye.getRow(label[i]);
		}
		return oneHot;
	}

	public int[] call(DoubleMatrix[] X) {
		DoubleMatrix[] x = new DoubleMatrix[X.length];
		for (int j = 0; j < x.length; ++j)
//			x[j] = activation.activate(X[j].mmul(kernel).addi(bias));
			x[j] = X[j].mmul(kernel).addi(bias);

		x[0].addi(left_boundary);
		x[x.length - 1].addi(right_boundary);

		int i = 0;
		DoubleMatrix min_energy = x[i++];
		int length = x.length;
		int[][] argmin_tables = new int[length][];

		while (i < length) {
			DoubleMatrix energy = min_energy.repmat(bias.length, 1).addi(G);
			argmin_tables[i - 1] = Utility.argmin(energy);
			min_energy = Utility.min(energy);
			min_energy.addi(x[i++]);
		}

		int argmin = min_energy.argmin();

		assert i == length;

		int[] best_paths = new int[length];
		best_paths[--i] = argmin;

		for (--i; i >= 0; --i) {
			argmin = argmin_tables[i][argmin];
			best_paths[i] = argmin;
		}
		return best_paths;
	}

	public CRF(Utility.BinaryReader dis) throws IOException {
		kernel = new DoubleMatrix(dis.readArray2());
		G = new DoubleMatrix(dis.readArray2());
		bias = new DoubleMatrix(dis.readArray1());
		left_boundary = new DoubleMatrix(dis.readArray1());
		right_boundary = new DoubleMatrix(dis.readArray1());
	}

}