package com.deeplearning;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.jblas.DoubleMatrix;

import com.util.Utility;

public class Embedding implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */

	public Embedding(HashMap<Character, Integer> char2id, DoubleMatrix[] wEmbedding) {
		this.char2id = char2id;
		this.wEmbedding = wEmbedding;
	}

	public HashMap<Character, Integer> char2id = new HashMap<Character, Integer>();
	public DoubleMatrix[] wEmbedding;

	public DoubleMatrix[] call(String word) {

		int length = word.length();
		DoubleMatrix[] charEmbedding = new DoubleMatrix[length];

		for (int j = 0; j < charEmbedding.length; ++j) {
			int index = 1;
			char ch = word.charAt(j);

			if (char2id.containsKey(ch))
				index = char2id.get(ch);

			charEmbedding[j] = this.wEmbedding[index];
		}
		return charEmbedding;
	}

	public DoubleMatrix[] call(String word, int max_length) {

		if (word.length() > max_length) {
			word = word.substring(0, max_length);
		}
		
		return this.call(word);
	}

	public DoubleMatrix[] _call(String word) {

		int length = word.length();
		DoubleMatrix[] charEmbedding = new DoubleMatrix[length];

		for (int j = 0; j < charEmbedding.length; ++j) {
			int index = 1;
			char ch = word.charAt(j);

			if (char2id.containsKey(ch))
				index = char2id.get(ch);

			charEmbedding[j] = this.wEmbedding[index];
		}

		return charEmbedding;
	}

	public DoubleMatrix[] call(int[] word) {

		int length = word.length;
		DoubleMatrix[] charEmbedding = new DoubleMatrix[length];

		for (int j = 0; j < charEmbedding.length; ++j) {
			int index = word[j];

			charEmbedding[j] = this.wEmbedding[index];
		}
		return charEmbedding;
	}

	public Embedding(Utility.BinaryReader dis) throws IOException {
		initialize(dis, true);
	}

	void initialize(Utility.BinaryReader dis, boolean dic) throws IOException {
		if (dic) {
			char2id = new HashMap<Character, Integer>();
			dis.readCharMap(char2id);
		}

		double[][] wEmbedding = dis.readArray2();

		this.wEmbedding = new DoubleMatrix[wEmbedding.length];
		for (int i = 0; i < this.wEmbedding.length; ++i) {
			this.wEmbedding[i] = new DoubleMatrix(1, wEmbedding[i].length, wEmbedding[i]);
		}

	}

	public Embedding(Utility.BinaryReader dis, boolean dic) throws IOException {
		initialize(dis, dic);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
