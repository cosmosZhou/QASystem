package com.deeplearning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.jblas.DoubleMatrix;

import com.deeplearning.rnn.BidirectionalLSTM;
import com.deeplearning.rnn.Bidirectional.merge_mode;
import com.util.Utility;
import com.util.Utility.Text;

public class CWSTagger implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static CWSTagger instance;
	static {
		synchronized (CWSTagger.class) {
			try {
				Utility.BinaryReader dis = new Utility.BinaryReader(Utility.modelsDirectory() + "cn/cws.bin");
				instance = new CWSTagger(dis);

			} catch (NullPointerException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Embedding embedding;
	Conv1D con1D[] = new Conv1D[3];

	public BidirectionalLSTM lstm;
//	Dense dense;

	public CRF wCRF;

	public ArrayList<String> cut(String predict_text) {
		int[] result = predict(predict_text);
		ArrayList<String> arr = new ArrayList<String>();
		String sstr = "";
		for (int i = 0; i < result.length; ++i) {
			char ch = predict_text.charAt(i);
			if (!Character.isWhitespace(ch)) {
				sstr += ch;
			}

			if ((result[i] & 1) != 0 && !sstr.isEmpty()) {
				arr.add(sstr);
				sstr = "";
			}
		}

		if (!sstr.isEmpty())
			arr.add(sstr);

		return arr;
	}

	public String[] tag(String predict_text) {
		return Utility.toArray(this.cut(predict_text));
	}

	public int[] predict(String predict_text) {
		DoubleMatrix[] lEmbedding = embedding.call(predict_text);
		DoubleMatrix[] lLSTM = lstm.call_return_sequences(lEmbedding);

		DoubleMatrix[] lCNN = con1D[0].conv_same(lEmbedding, 1);// , Activation.ReLU
		lCNN = con1D[1].conv_same(lCNN, 1);// , Activation.ReLU
		lCNN = con1D[2].conv_same(lCNN, 1);// , Activation.ReLU

		DoubleMatrix[] lConcatenate = new DoubleMatrix[lLSTM.length];
		for (int i = 0; i < lLSTM.length; ++i)
			lConcatenate[i] = DoubleMatrix.concatHorizontally(lLSTM[i], lCNN[i]);

//		DoubleMatrix[] lDense = dense.call(lConcatenate);
		return wCRF.call(lConcatenate);
	}

	public ArrayList<Double> predictDebug(String predict_text) {
		ArrayList<Double> result = new ArrayList<Double>();
		DoubleMatrix[] lEmbedding = embedding.call(predict_text);
		for (int i = 0; i < lEmbedding.length; ++i)
			for (double x : lEmbedding[i].data)
				result.add(x); // i = 0
		DoubleMatrix[] lLSTM = lstm.call_return_sequences(lEmbedding);
		for (int i = 0; i < lLSTM.length; ++i)
			for (double x : lLSTM[i].data)
				result.add(x);// i = 1

		DoubleMatrix[] lCNN = con1D[0].conv_same(lEmbedding, 1);

		for (int i = 0; i < lCNN.length; ++i)
			for (double x : lCNN[i].data)
				result.add(x);// i = 2

		lCNN = con1D[1].conv_same(lCNN, 1);

		for (int i = 0; i < lCNN.length; ++i)
			for (double x : lCNN[i].data)
				result.add(x);// i = 3

		DoubleMatrix[] lConcatenate = new DoubleMatrix[lLSTM.length];
		for (int i = 0; i < lConcatenate.length; ++i)
			lConcatenate[i] = DoubleMatrix.concatHorizontally(lLSTM[i], lCNN[i]);

		for (int i = 0; i < lConcatenate.length; ++i)
			for (double x : lConcatenate[i].data)
				result.add(x);// i = 4

//		DoubleMatrix[] lDense = dense.call(lConcatenate);

//		for (int i = 0; i < lDense.length; ++i)
//			for (double x : lDense[i].data)
//				result.add(x);// i = 5

		DoubleMatrix[] label = wCRF.viterbi_one_hot(lConcatenate);
		for (int i = 0; i < label.length; ++i)
			for (double x : label[i].data)
				result.add(x);// i = 6

		return result;
	}

	public CWSTagger(Utility.BinaryReader dis) throws IOException {

		this.embedding = new Embedding(dis);

		this.lstm = new BidirectionalLSTM(dis, merge_mode.sum);

		con1D[0] = new Conv1D(dis);
		con1D[1] = new Conv1D(dis);
		con1D[2] = new Conv1D(dis);

//		dense = new Dense(dis);
//		dense.activation = Activation.tanh;

		this.wCRF = new CRF(dis);

		assert dis.dis.available() == 0;
		dis.dis.close();
	}

	public void save() throws IOException {
		Utility.saveTo("../models/CWSTagger.gz", this);
	}

	public static void test() throws UnsupportedEncodingException, FileNotFoundException {
		ArrayList<String> arr = new Text("../corpus/seg.txt").collect(new ArrayList<String>());
		Collections.shuffle(arr);

		int err = 0;
		int sgm = 0;
		Utility.Timer timer = new Utility.Timer();
		timer.start();
		for (String str : arr.subList(0, 1000)) {
			String strOriginal = Utility.convertSegmentationToOriginal(str.split("\\s+"));
			ArrayList<String> predRes = instance.cut(strOriginal);
			String[] goldRes = Utility.convertToSegmentation(str);
			if (!Utility.equals(Utility.toArray(predRes), goldRes)) {
//				System.out.println("training failed for ");
//				System.out.println(str);
//				System.out.println("strOriginal = \n" + strOriginal);
//				System.out.println(Utility.toString(goldRes, "  ", null, goldRes.length));
//				System.out.println(Utility.toString(predRes, "  ", null, predRes.length));

				++err;
			}
		}

		System.out.println("err = " + err);
		System.out.println("sgm = " + sgm);
		System.out.println("acc = " + (sgm - err) * 1.0 / sgm);
		System.out.println("totally cost " + timer.lapsedSeconds());
	}

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
		test();
	}

	public static Logger log = Logger.getLogger(CWSTagger.class);
}
