package com.deeplearning;

import java.io.IOException;
import java.io.Serializable;

import org.jblas.DoubleMatrix;

import com.deeplearning.utils.Activation;
import com.util.Utility;

public class Conv1D implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Conv1D(DoubleMatrix[] w, DoubleMatrix bias, Activation activate) {
		this.w = w;
		this.bias = bias;
		this.activate = activate;
	}

	DoubleMatrix[] w;
	DoubleMatrix bias;
	Activation activate = Activation.ReLU;

	public Conv1D(Utility.BinaryReader dis) throws IOException {
		double[][][] matrix = dis.readArray3();

		w = new DoubleMatrix[matrix.length];
		for (int i = 0; i < matrix.length; ++i) {
			w[i] = new DoubleMatrix(matrix[i]);
		}
		double[] bias = dis.readArray1();
		this.bias = new DoubleMatrix(bias);
	}

	static public int initial_offset(int xshape, int yshape, int wshape, int sshape) {
		if (yshape > 1) {
			int l = xshape + (wshape - sshape) * (yshape - 1);
			if (yshape * wshape < l)
				l = yshape * wshape;
			return wshape - (2 * wshape + l - (l + wshape - 1) / wshape * wshape + 1) / 2;
		} else
			return -((xshape - wshape) / 2);
	}

//	#stride=(1,1)
	public DoubleMatrix[] conv_same(DoubleMatrix[] x, int s) {
		int yshape0 = (x.length + s - 1) / s;
		DoubleMatrix[] y = new DoubleMatrix[yshape0];
		int d0 = initial_offset(x.length, y.length, w.length, s);
		for (int i = 0; i < yshape0; ++i) {
			y[i] = new DoubleMatrix(1, w[0].columns);
			int _i = s * i - d0;
			int di_end = Math.min(w.length, x.length - _i);
			for (int di = Math.max(0, -_i); di < di_end; ++di)
				y[i].addi(x[_i + di].mmul(w[di]));
			if (bias != null)
				y[i].addi(bias);
			if (activate != null)
				y[i] = activate.activate(y[i]);
		}
		return y;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
