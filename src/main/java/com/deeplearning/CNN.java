package com.deeplearning;

import org.jblas.DoubleMatrix;
import com.deeplearning.utils.Activation;

/**
 * implimentation Convolutional Neural Networks
 * 
 * @author Cosmos
 *
 */

public class CNN {

	static public DoubleMatrix[][] conv2d_same(DoubleMatrix[][] x, DoubleMatrix[][] w, DoubleMatrix bias, int[] stride,
			Activation activation) {

		int[] xshape = { x.length, x[0].length };
		int[] wshape = { w.length, w[0].length, w[0][0].rows, w[0][0].columns };
		int yshape0 = xshape[0];
		int yshape1 = xshape[1];

		yshape0 /= stride[0];
		yshape1 /= stride[1];

		DoubleMatrix[][] y = new DoubleMatrix[yshape0][yshape1];

		for (int i = 0; i < yshape0; ++i)
			for (int j = 0; j < yshape1; ++j) {
				y[i][j] = new DoubleMatrix(1, wshape[3]);

				for (int di = 0; di < wshape[0]; ++di)
					for (int dj = 0; dj < wshape[1]; ++dj)
						y[i][j].addi(x[stride[0] * i + di][stride[1] * j + dj].mmul(w[di][dj]));

				if (bias != null)
					y[i][j].addi(bias);
				if (activation != null)
					y[i][j] = activation.activate(y[i][j]);
			}
		return y;
	}

	static public DoubleMatrix[][] conv2d(DoubleMatrix[][] x, DoubleMatrix[][] w, DoubleMatrix bias, int[] stride,
			boolean samePadding, Activation activation) {
		if (samePadding)
			return conv2d_same(x, w, bias, stride, activation);

		int[] xshape = { x.length, x[0].length };
		int[] wshape = { w.length, w[0].length, w[0][0].rows, w[0][0].columns };

		int yshape0 = (xshape[0] + stride[0] - wshape[0]) / stride[0];
		int yshape1 = (xshape[1] + stride[1] - wshape[1]) / stride[1];

		DoubleMatrix[][] y = new DoubleMatrix[yshape0][yshape1];

		for (int i = 0; i < yshape0; ++i)
			for (int j = 0; j < yshape1; ++j) {
				y[i][j] = new DoubleMatrix(1, wshape[3]);

				for (int di = 0; di < wshape[0]; ++di)
					for (int dj = 0; dj < wshape[1]; ++dj)
						y[i][j].addi(x[stride[0] * i + di][stride[1] * j + dj].mmul(w[di][dj]));

				if (bias != null)
					y[i][j].addi(bias);
				if (activation != null)
					y[i][j] = activation.activate(y[i][j]);
			}
		return y;
	}

//	# padding='VALID' or 'SAME'
	static public DoubleMatrix[][] max_pooling(DoubleMatrix[][] x, int[] size, int[] stride, boolean samePadding) {
//			    # Preparing the output of the pooling operation.
		int[] xshape = { x.length, x[0].length, };
		int yshape0 = xshape[0];
		int yshape1 = xshape[1];

		if (samePadding) {
			yshape0 += 1;
			yshape1 += 1;
		}

		yshape0 /= stride[0];
		yshape1 /= stride[1];
		int yshape2 = x[0][0].length;

		DoubleMatrix[][] y = new DoubleMatrix[yshape0][yshape1];

		for (int i = 0; i < yshape0; ++i)
			for (int j = 0; j < yshape1; ++j) {
				y[i][j] = new DoubleMatrix(1, yshape2);

				for (int k = 0; k < yshape2; ++k) {
					int _i = stride[0] * i;
					int _j = stride[1] * j;

					int _I = Math.min(xshape[0], _i + size[0]);
					int _J = Math.min(xshape[1], _j + size[1]);

					double max = Double.NEGATIVE_INFINITY;

					for (int ii = _I - 1; ii >= _i; --ii)
						for (int jj = _J - 1; jj >= _j; --jj)
							max = Math.max(max, x[ii][jj].get(k));

					y[i][j].put(k, max);
				}
			}
		return y;
	}

}
