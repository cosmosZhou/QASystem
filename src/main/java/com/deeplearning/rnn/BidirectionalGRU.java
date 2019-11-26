package com.deeplearning.rnn;

import java.io.IOException;

import com.deeplearning.GRU;
import com.util.Utility;

/**
 * implimentation of Gated Recurrent Unit
 * 
 * @author Cosmos
 *
 */

public class BidirectionalGRU extends Bidirectional {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BidirectionalGRU(Utility.BinaryReader dis, merge_mode mode) throws IOException {
		this.forward = new GRU(dis);
		this.backward = new GRU(dis);
		this.mode = mode;
	}
	
}
