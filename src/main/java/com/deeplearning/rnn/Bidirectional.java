package com.deeplearning.rnn;

import org.jblas.DoubleMatrix;

import com.util.Utility;

public class Bidirectional {
	public RNN forward, backward;

	public enum merge_mode {
		sum, mul, ave, concat
	}

	public merge_mode mode;

	public DoubleMatrix[] call_return_sequences(DoubleMatrix[] x) {
		DoubleMatrix[] forward = this.forward.call_return_sequences(x);
		DoubleMatrix[] backward = this.backward.call_return_sequences_reverse(x);

		switch (mode) {
		case sum:
			return Utility.addi(forward, backward);
		case ave:
			return Utility.divi(Utility.addi(forward, backward), 2);
		case mul:
			return Utility.muli(forward, backward);
		case concat:
			return Utility.concatHorizontally(forward, backward);
		}
		return null;
	}

	public DoubleMatrix call(DoubleMatrix[] x) {
		DoubleMatrix forward = this.forward.call(x);
		DoubleMatrix backward = this.backward.call_reverse(x);

		switch (mode) {
		case sum:
			return forward.addi(backward);
		case ave:
			return forward.addi(backward).divi(2);
		case mul:
			return forward.muli(backward);
		case concat:			
			return DoubleMatrix.concatHorizontally(forward, backward);
		}
		return null;
	}
}
