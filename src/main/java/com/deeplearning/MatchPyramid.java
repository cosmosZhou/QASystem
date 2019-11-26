package com.deeplearning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jblas.DoubleMatrix;

import com.deeplearning.CNN;
import com.deeplearning.GRU;
import com.deeplearning.utils.Activation;

import com.util.Utility;
import com.util.Utility.Timer;

public class MatchPyramid implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap<String, Integer> word2id = new HashMap<String, Integer>();
	HashMap<String, DoubleMatrix> wordEmbedding = new HashMap<String, DoubleMatrix>();
	Embedding embedding;
	GRU gru;

	DoubleMatrix[][][] wCNN;
	DoubleMatrix[] bCNN;

	Dense dense;

	static Activation activation = Activation.ReLU;
	static int[] convolveStride = { 1, 1 };
	static int[] poolingStride = { 2, 2 };

	int[] string2id(String s[]) {
		int[] arr = new int[s.length];

		for (int i = 0; i < arr.length; ++i) {
			if (word2id.containsKey(s[i]))
				arr[i] = word2id.get(s[i]);
			else
				arr[i] = 0;
		}

		return arr;
	}

	DoubleMatrix[] string2matrix(String s[]) {
		DoubleMatrix[] embedding = new DoubleMatrix[s.length];

		for (int i = 0; i < embedding.length; ++i) {

			String word = s[i];
			if (wordEmbedding.containsKey(word)) {
				embedding[i] = wordEmbedding.get(word);
				continue;
			}

			DoubleMatrix vector = this.gru.call(this.embedding.call(word));
			this.wordEmbedding.put(word, vector);
			embedding[i] = vector;
		}

		return embedding;
	}

	public double predict(String s1, String s2) {
		String[] s1Arr = CWSTagger.instance.tag(s1);
		String[] s2Arr = CWSTagger.instance.tag(s2);

		int queLength = s1Arr.length;
		int docLength = s2Arr.length;

		DoubleMatrix[] queEmbedding = string2matrix(s1Arr);
		DoubleMatrix[] docEmbedding = string2matrix(s2Arr);

		DoubleMatrix[][] similarityMatrix = new DoubleMatrix[queLength][docLength];
		for (int i = 0; i < queLength; ++i)
			for (int j = 0; j < docLength; ++j)
				similarityMatrix[i][j] = queEmbedding[i].mul(docEmbedding[j]);

		int layer = 0;
		while (queLength >= 2 && docLength >= 2) {
			if (layer >= wCNN.length)
				break;

			similarityMatrix = CNN.conv2d(similarityMatrix, wCNN[layer], bCNN[layer], convolveStride, false,
					activation);

			++layer;

//			if (queLength > 2 && docLength > 2) {
			similarityMatrix = CNN.max_pooling(similarityMatrix, poolingStride, poolingStride, true);
			queLength /= 2;
			docLength /= 2;
//			} else {
//				--queLength;
//				--docLength;
//			}
		}

		assert queLength == similarityMatrix.length;
		assert docLength == similarityMatrix[0].length;
		DoubleMatrix vector = new DoubleMatrix(1, similarityMatrix[0][0].length);
		for (int i = 0; i < queLength; ++i)
			for (int j = 0; j < docLength; ++j)
				vector.addi(similarityMatrix[i][j]);
		vector.divi(queLength * docLength);

		DoubleMatrix prob = dense.call(vector);
		return prob.get(0);
	}

	public ArrayList<Double> predictDebug(String s1, String s2) {
		ArrayList<Double> result = new ArrayList<Double>();
		String[] s1Arr = CWSTagger.instance.tag(s1);
		String[] s2Arr = CWSTagger.instance.tag(s2);

		int queLength = s1Arr.length;
		int docLength = s2Arr.length;

		DoubleMatrix[] queEmbedding = string2matrix(s1Arr);
		DoubleMatrix[] docEmbedding = string2matrix(s2Arr);

		for (int i = 0; i < queEmbedding.length; ++i)
			for (double x : queEmbedding[i].data)
				result.add(x);

		for (int i = 0; i < docEmbedding.length; ++i)
			for (double x : docEmbedding[i].data)
				result.add(x);

		DoubleMatrix[][] similarityMatrix = new DoubleMatrix[queLength][docLength];
		for (int i = 0; i < queLength; ++i)
			for (int j = 0; j < docLength; ++j)
				similarityMatrix[i][j] = queEmbedding[i].mul(docEmbedding[j]);

		for (int i = 0; i < similarityMatrix.length; ++i)
			for (int j = 0; j < similarityMatrix[i].length; ++j)
				for (double x : similarityMatrix[i][j].data)
					result.add(x);

		int layer = 0;
		while (queLength >= 2 && docLength >= 2) {
			if (layer >= wCNN.length)
				break;

			similarityMatrix = CNN.conv2d(similarityMatrix, wCNN[layer], bCNN[layer], convolveStride, false,
					activation);

			for (int i = 0; i < similarityMatrix.length; ++i)
				for (int j = 0; j < similarityMatrix[i].length; ++j)
					for (double x : similarityMatrix[i][j].data)
						result.add(x);

			++layer;

			if (queLength > 2 && docLength > 2) {
				similarityMatrix = CNN.max_pooling(similarityMatrix, poolingStride, poolingStride, true);
				for (int i = 0; i < similarityMatrix.length; ++i)
					for (int j = 0; j < similarityMatrix[i].length; ++j)
						for (double x : similarityMatrix[i][j].data)
							result.add(x);
				queLength /= 2;
				docLength /= 2;
			} else {
				--queLength;
				--docLength;
			}

		}

		assert queLength == similarityMatrix.length;
		assert docLength == similarityMatrix[0].length;

		DoubleMatrix vector = new DoubleMatrix(1, similarityMatrix[0][0].length);
		for (int i = 0; i < queLength; ++i)
			for (int j = 0; j < docLength; ++j)
				vector.addi(similarityMatrix[i][j]);
		vector.divi(queLength * docLength);

		for (double x : vector.data)
			result.add(x);

		DoubleMatrix prob = this.dense.call(vector);
		for (double x : prob.data)
			result.add(x);

		return result;
	}

	public void save() throws IOException {
		Utility.saveTo("../models/matchPyramid.gz", this);
	}

	public static MatchPyramid instance;
	static {
		synchronized (MatchPyramid.class) {

			try {
				instance = (MatchPyramid) Utility.loadFrom(Utility.workingDirectory + "models/matchPyramid.gz");
				if (instance.wordEmbedding == null)
					instance.wordEmbedding = new HashMap<String, DoubleMatrix>();

			} catch (NullPointerException | IOException | ClassNotFoundException e) {
				instance = new MatchPyramid();
			}
		}
	}

	public void clearCache() {
		wordEmbedding = new HashMap<String, DoubleMatrix>();
	}

	public void verwahrenVonPython() throws IOException {

		Utility.BinaryReader dis = new Utility.BinaryReader("../models/paraphrase.bin");
		this.embedding = new Embedding(dis);

//		Wxu, Wxr, Wxh, Whu, Whr, Whh, bu, br, bh;
		this.gru = GRU.initialize(dis);
		log.info("gru saved successfully");

		double[][][][][] wCNN = dis.readArray5();
		double[][] bCNN = dis.readArray2();
		this.wCNN = new DoubleMatrix[wCNN.length][][];
		this.bCNN = new DoubleMatrix[wCNN.length];

		for (int l = 0; l < this.wCNN.length; ++l) {

			int strideX = wCNN[l].length;
			int strideY = wCNN[l][0].length;

			this.wCNN[l] = new DoubleMatrix[strideX][strideY];
			for (int i = 0; i < strideX; ++i) {
				for (int j = 0; j < strideY; ++j) {
					this.wCNN[l][i][j] = new DoubleMatrix(wCNN[l][i][j]);
				}
			}

			this.bCNN[l] = new DoubleMatrix(1, bCNN[l].length, bCNN[l]);
		}

		log.info("wCNN and bCNN saved successfully");

		double[][] wDense = dis.readArray2();
		double[] bDense = dis.readArray1();
		this.dense = new Dense(new DoubleMatrix(wDense), new DoubleMatrix(bDense));

		log.info("wDense and bDense saved successfully");

		assert dis.dis.available() == 0;
		dis.dis.close();
		this.word2id = null;
		this.wordEmbedding = new HashMap<String, DoubleMatrix>();
		save();
		log.info("model saved successfully");

//		test();
	}

	public void test() throws UnsupportedEncodingException, FileNotFoundException {
		Timer timer = new Timer();
		timer.start();

		for (String pair : new Utility.Text("../corpus/paraphrase/0.99.data")) {
			String[] arr = pair.split("\\s+/\\s+");

			double similarity = predict(arr[0], arr[1]);
			System.out.printf("%s / %s = %f\n", arr[0], arr[1], similarity);
//			log.info("similarity =" + similarity);
		}
		timer.report();

	}

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
		Timer timer = new Timer();
		for (String arg : args)
			log.info(arg);

		if (args.length > 0) {
			Utility.workingDirectory = args[0];
			log.info("Utility.workingDirectory = " + Utility.workingDirectory);
		}

//		CWSTagger.instance = CWSTagger.instance;
		timer.start();

		for (String pair : new Utility.Text(Utility.workingDirectory + "corpus/paraphrase/0.99.data")) {
			String[] arr = pair.split("\\s+/\\s+");

			double similarity = MatchPyramid.instance.predict(arr[0], arr[1]);
			System.out.printf("%s / %s = %f\n", arr[0], arr[1], similarity);
//			log.info("similarity =" + similarity);
		}
		timer.report();

	}

	public static Logger log = Logger.getLogger(MatchPyramid.class);
}
