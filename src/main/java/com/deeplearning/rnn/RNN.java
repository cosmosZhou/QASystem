package com.deeplearning.rnn;

import org.jblas.DoubleMatrix;

public abstract class RNN {
	public abstract DoubleMatrix call(DoubleMatrix[] x);
	
	public abstract DoubleMatrix call_reverse(DoubleMatrix[] x);

	public abstract DoubleMatrix[] call_return_sequences(DoubleMatrix[] x);

	public abstract DoubleMatrix[] call_return_sequences_reverse(DoubleMatrix[] x);

}
