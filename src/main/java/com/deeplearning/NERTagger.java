package com.deeplearning;

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.jblas.DoubleMatrix;

import com.deeplearning.rnn.Bidirectional.merge_mode;
import com.deeplearning.rnn.BidirectionalLSTM;
import com.util.Utility;

public class NERTagger implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Embedding embedding;
	public Embedding repertoire_embedding;

	Conv1D con1D[] = new Conv1D[3];

	public BidirectionalLSTM lstm;
//	Dense dense;

	public CRF wCRF;

	public int[] predict(String predict_text, int[] repertoire_code) {
		DoubleMatrix[] lEmbedding = embedding.call(predict_text);
		DoubleMatrix[] lRepertoire = repertoire_embedding.call(repertoire_code);
// never use the following code, it will change the initial embeddings!
//		lEmbedding = Utility.addi(lEmbedding, lRepertoire);
		lEmbedding = Utility.add(lEmbedding, lRepertoire);

		DoubleMatrix[] lLSTM = lstm.call_return_sequences(lEmbedding);

		DoubleMatrix[] lCNN = con1D[0].conv_same(lEmbedding, 1);// , Activation.ReLU
		lCNN = con1D[1].conv_same(lCNN, 1);// , Activation.ReLU
		lCNN = con1D[2].conv_same(lCNN, 1);// , Activation.ReLU

		DoubleMatrix[] lConcatenate = new DoubleMatrix[lLSTM.length];
		for (int i = 0; i < lLSTM.length; ++i)
			lConcatenate[i] = DoubleMatrix.concatHorizontally(lLSTM[i], lCNN[i]);

		return wCRF.call(lConcatenate);
	}

	static double[][] to_double(DoubleMatrix[] matrix) {
		int n = matrix[0].columns;
		double[][] arr = new double[matrix.length][n];

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < n; j++) {
				arr[i][j] = matrix[i].get(j);
			}

		}
		return arr;
	}

	public double[][][] _predict(String predict_text, int[] repertoire_code) {
		double[][][] arr = new double[9][][];
		int index = 0;
		DoubleMatrix[] lEmbedding = embedding._call(predict_text);
		arr[index++] = to_double(lEmbedding);// i = 0

		DoubleMatrix[] lRepertoire = repertoire_embedding.call(repertoire_code);
		arr[index++] = to_double(lRepertoire);// i = 1

		DoubleMatrix[] x = Utility.add(lEmbedding, lRepertoire);
		arr[index++] = to_double(x);// i = 2

		DoubleMatrix[] lLSTM = lstm.call_return_sequences(x);
		arr[index++] = to_double(lLSTM);// i = 3

		DoubleMatrix[] lCNN = con1D[0].conv_same(x, 1);
		arr[index++] = to_double(lCNN);// i = 4

		lCNN = con1D[1].conv_same(lCNN, 1);
		arr[index++] = to_double(lCNN);// i = 5

		lCNN = con1D[2].conv_same(lCNN, 1);
		arr[index++] = to_double(lCNN);// i = 6

		DoubleMatrix[] lConcatenate = new DoubleMatrix[lLSTM.length];
		for (int i = 0; i < lConcatenate.length; ++i)
			lConcatenate[i] = DoubleMatrix.concatHorizontally(lLSTM[i], lCNN[i]);
		arr[index++] = to_double(lConcatenate);// i = 7

		DoubleMatrix[] label = wCRF.viterbi_one_hot(lConcatenate);
		arr[index++] = to_double(label);// i = 8

		return arr;
	}

	public NERTagger(Utility.BinaryReader dis) throws IOException {

		this.embedding = new Embedding(dis);
		this.repertoire_embedding = new Embedding(dis, false);

		this.lstm = new BidirectionalLSTM(dis, merge_mode.sum);

		con1D[0] = new Conv1D(dis);
		con1D[1] = new Conv1D(dis);
		con1D[2] = new Conv1D(dis);

		this.wCRF = new CRF(dis);

		assert dis.dis.available() == 0;
		dis.dis.close();
	}

	public static Logger log = Logger.getLogger(NERTagger.class);
}
