package com.deeplearning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jblas.DoubleMatrix;

import com.deeplearning.rnn.Bidirectional.merge_mode;
import com.deeplearning.rnn.BidirectionalGRU;
import com.deeplearning.utils.Activator;
import com.util.HttpClientWebApp4NER;
import com.util.MySQL;
import com.util.Native;
import com.util.Utility;

public class Service implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Service instance;
	public static Logger log = Logger.getLogger(Service.class);
	static {
		synchronized (Service.class) {
			try {
				System.out.println("initializing Service in Java");
				Utility.BinaryReader dis = new Utility.BinaryReader(
						Utility.modelsDirectory() + "cn/gru_data/service.bin");
				instance = new Service(dis);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Embedding embedding;

	public BidirectionalGRU gru;
	Dense dense_mean;
//	Dense dense_log_var;
	Dense dense_pred;
	public String label[];

	int max_length = 30;

	public String predict(String predict_text) {
		DoubleMatrix[] lEmbedding = embedding.call(predict_text, max_length);

		DoubleMatrix x = gru.call(lEmbedding);

		DoubleMatrix z_mean = dense_mean.call(x);
//		DoubleMatrix z_log_var = dense_log_var.call(x);
		x = Activator.l2_normalize(z_mean);

		DoubleMatrix pred = dense_pred.call(x);

		return label[pred.argmax()];
	}

	public String cpp_predict(String text) {
		return Service.instance.label[Native.service(text)];
	}

	public ArrayList<DoubleMatrix> _predict(String predict_text) {
		ArrayList<DoubleMatrix> result = new ArrayList<DoubleMatrix>();

		DoubleMatrix[] lEmbedding = embedding.call(predict_text, max_length);
		result.add(lEmbedding[0]);
		result.add(lEmbedding[lEmbedding.length - 1]);

		DoubleMatrix x = gru.call(lEmbedding);
		result.add(x);
		DoubleMatrix z_mean = dense_mean.call(x);
		result.add(z_mean);
//		DoubleMatrix z_log_var = dense_log_var.call(x);
		x = Activator.l2_normalize(z_mean);
		result.add(x);
		DoubleMatrix pred = dense_pred.call(x);
		result.add(pred);

		return result;
	}

	public Service(Utility.BinaryReader dis) throws Exception {

		this.embedding = new Embedding(dis);

		this.gru = new BidirectionalGRU(dis, merge_mode.concat);

		dense_mean = new Dense(dis);
		dense_pred = new Dense(dis, false);

		assert dis.dis.available() == 0;
		dis.dis.close();
		label = MySQL.instance.tbl_service_distinct_category();
		if (label.length != dense_pred.wDense.columns) {
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("label", "true");
			String json = HttpClientWebApp4NER.instance.postMethod("_service", data);

			label = Utility.dejsonify(json, String[].class);
//			log.info(label);
			assert label.length == dense_pred.wDense.columns;
		}
	}

}
