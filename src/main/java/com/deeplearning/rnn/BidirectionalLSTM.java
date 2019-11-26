package com.deeplearning.rnn;

import java.io.IOException;
import java.util.HashMap;

import org.jblas.DoubleMatrix;

import com.deeplearning.GRU;
import com.util.Utility;

import static com.util.Utility.toDouble;

/**
 * implimentation of Gated Recurrent Unit
 * 
 * @author Cosmos
 *
 */

public class BidirectionalLSTM extends Bidirectional {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BidirectionalLSTM(Utility.BinaryReader dis, merge_mode mode) throws IOException {
		this.forward = new LSTM(dis);
		this.backward = new LSTM(dis);
		this.mode = mode;
	}
	
//	// forward pass
//	public DoubleMatrix propogate(String seq) {
//
//		int length = this.mask_zero ? seq.length() : this.maximum_length;
//		int t = 0;
//		DoubleMatrix h = null;
//		for (; t < length; ++t) {
//			int index;
//			if (t < seq.length()) {
//				String ch = String.valueOf(seq.charAt(t));
//
//				index = charID.containsKey(ch) ? charID.get(ch) : 1;
//			} else
//				index = 0;
//
//			System.out.print(index + " ");
//			DoubleMatrix xt = charVector.getRow(index);
//
//			h = activate(xt, h);
//		}
//
//		DoubleMatrix _h = null;
//		for (t = length - 1; t >= 0; --t) {
//			int index;
//			if (t < seq.length()) {
//				String ch = String.valueOf(seq.charAt(t));
//
//				index = charID.containsKey(ch) ? charID.get(ch) : 1;
//			} else
//				index = 0;
//
//			System.out.print(index + " ");
//			DoubleMatrix xt = charVector.getRow(index);
//
//			_h = _lstm.activate(xt, _h);
//		}
//
//		System.out.println();
//
//		switch (this.merge_mode) {
//		case sum:
//			h = h.addi(_h);
//			break;
//		case mul:
//			h = h.muli(_h);
//			break;
//		case ave:
//			h = h.addi(_h);
//			h = h.divi(2);
//			break;
//		case concat:
//			h = DoubleMatrix.concatHorizontally(h, _h);
//			break;
//		default:
//			break;
//		}
//
//		return decode(h);
//	}
}
