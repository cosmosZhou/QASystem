package com.deeplearning.rnn.gru;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import com.deeplearning.utils.Activation;

import com.util.Utility;


/**
 * implimentation of Gated Recurrent Unit
 * @author Cosmos
 *
 */

public class GRUFloat implements Serializable {
	private static final long serialVersionUID = -1501734916541393551L;

	Activation recurrent_activation = Activation.hard_sigmoid;
	Activation hidden_activation = Activation.tanh;
	Activation softmax_activation = Activation.softmax;

	boolean bUseLambda = false;

	FloatMatrix charVector;
	Map<String, Integer> charID;

	private int xSize;
	private int hSize;
	private int ySize;

	private FloatMatrix Wxu;
	private FloatMatrix Whu;
	private FloatMatrix bu;

	private FloatMatrix Wxr;
	private FloatMatrix Whr;
	private FloatMatrix br;

	private FloatMatrix Wxh;
	private FloatMatrix Whh;
	private FloatMatrix bh;

	private FloatMatrix Why; // for the final softmax layer
	private FloatMatrix by; // for bias of the final softmax layer

	private int maximum_length;
	private boolean mask_zero;

	public static double toDouble(float x) {
		return x;
	}

	public static double[] toDouble(float x[]) {
		double[] y = new double[x.length];
		for (int i = 0; i < y.length; i++) {
			y[i] = x[i];
		}
		return y;
	}

	public static double[][] toDouble(float x[][]) {
		double[][] y = new double[x.length][];
		for (int i = 0; i < y.length; i++) {
			y[i] = toDouble(x[i]);
		}
		return y;
	}

	public int getxSize() {
		return xSize;
	}

	public int getySize() {
		return ySize;
	}

	public void active(int t, Map<String, FloatMatrix> acts) {
		FloatMatrix x = acts.get("x" + t);
		FloatMatrix h;
		if (t == 0) {
			FloatMatrix u = x.mmul(Wxu);
			if (bu != null)
				u = u.add(bu);
			u = recurrent_activation.activate(u);

			FloatMatrix gh = x.mmul(Wxh);
			if (bh != null)
				gh = gh.add(bh);
			gh = hidden_activation.activate(gh);

			h = FloatMatrix.ones(1, u.columns).sub(u).mul(gh);

		} else {
			FloatMatrix preH = acts.get("h" + (t - 1));

			FloatMatrix r = x.mmul(Wxr).add(preH.mmul(Whr));
			if (br != null)
				r = r.add(br);
			r = recurrent_activation.activate(r);

			FloatMatrix u = x.mmul(Wxu).add(preH.mmul(Whu));
			if (bu != null)
				u = u.add(bu);
			u = recurrent_activation.activate(u);

			FloatMatrix gh = x.mmul(Wxh).add(r.mul(preH).mmul(Whh));
			if (bh != null)
				gh = gh.add(bh);
			gh = hidden_activation.activate(gh);

			h = (FloatMatrix.ones(1, u.columns).sub(u)).mul(gh).add(u.mul(preH));
		}
		acts.put("h" + t, h);
	}

	public void bptt(Map<String, FloatMatrix> acts, int lastT, float lr) {
		for (int t = lastT; t > -1; t--) {
			FloatMatrix py = acts.get("py" + t);
			FloatMatrix y = acts.get("y" + t);
			FloatMatrix deltaY = py.sub(y);
			acts.put("dy" + t, deltaY);

			// cell output errors
			FloatMatrix h = acts.get("h" + t);
			FloatMatrix z = acts.get("z" + t);
			FloatMatrix r = acts.get("r" + t);
			FloatMatrix gh = acts.get("gh" + t);

			FloatMatrix deltaH = null;
			if (t == lastT) {
				deltaH = Why.mmul(deltaY.transpose()).transpose();
			} else {
				FloatMatrix lateDh = acts.get("dh" + (t + 1));
				FloatMatrix lateDgh = acts.get("dgh" + (t + 1));
				FloatMatrix lateDr = acts.get("dr" + (t + 1));
				FloatMatrix lateDz = acts.get("dz" + (t + 1));
				FloatMatrix lateR = acts.get("r" + (t + 1));
				FloatMatrix lateZ = acts.get("z" + (t + 1));
				deltaH = Why.mmul(deltaY.transpose()).transpose().add(Whu.mmul(lateDr.transpose()).transpose())
						.add(Whr.mmul(lateDz.transpose()).transpose())
						.add(Whh.mmul(lateDgh.mul(lateR).transpose()).transpose())
						.add(lateDh.mul(FloatMatrix.ones(1, lateZ.columns).sub(lateZ)));
			}
			acts.put("dh" + t, deltaH);

			// gh
			FloatMatrix deltaGh = deltaH.mul(z).mul(deriveTanh(gh));
			acts.put("dgh" + t, deltaGh);

			FloatMatrix preH = null;
			if (t > 0) {
				preH = acts.get("h" + (t - 1));
			} else {
				preH = FloatMatrix.zeros(1, h.length);
			}

			// reset gates
			FloatMatrix deltaR = (Whh.mmul(deltaGh.mul(preH).transpose()).transpose()).mul(deriveExp(r));
			acts.put("dr" + t, deltaR);

			// update gates
			FloatMatrix deltaZ = deltaH.mul(gh.sub(preH)).mul(deriveExp(z));
			acts.put("dz" + t, deltaZ);
		}

		updateParameters(acts, lastT, lr);
	}

	private void updateParameters(Map<String, FloatMatrix> acts, int lastT, float lr) {
		FloatMatrix gWxr = new FloatMatrix(Wxu.rows, Wxu.columns);
		FloatMatrix gWhr = new FloatMatrix(Whu.rows, Whu.columns);
		FloatMatrix gbr = new FloatMatrix(bu.rows, bu.columns);

		FloatMatrix gWxu = new FloatMatrix(Wxr.rows, Wxr.columns);
		FloatMatrix gWhu = new FloatMatrix(Whr.rows, Whr.columns);
		FloatMatrix gbu = new FloatMatrix(br.rows, br.columns);

		FloatMatrix gWxh = new FloatMatrix(Wxh.rows, Wxh.columns);
		FloatMatrix gWhh = new FloatMatrix(Whh.rows, Whh.columns);
		FloatMatrix gbh = new FloatMatrix(bh.rows, bh.columns);

		FloatMatrix gWhy = new FloatMatrix(Why.rows, Why.columns);
		FloatMatrix gby = new FloatMatrix(by.rows, by.columns);

		for (int t = 0; t < lastT + 1; t++) {
			FloatMatrix x = acts.get("x" + t).transpose();
			gWxr = gWxr.add(x.mmul(acts.get("dr" + t)));
			gWxu = gWxu.add(x.mmul(acts.get("dz" + t)));
			gWxh = gWxh.add(x.mmul(acts.get("dgh" + t)));

			if (t > 0) {
				FloatMatrix preH = acts.get("h" + (t - 1)).transpose();
				gWhr = gWhr.add(preH.mmul(acts.get("dr" + t)));
				gWhu = gWhu.add(preH.mmul(acts.get("dz" + t)));
				gWhh = gWhh.add(acts.get("r" + t).transpose().mul(preH).mmul(acts.get("dgh" + t)));
			}
			gWhy = gWhy.add(acts.get("h" + t).transpose().mmul(acts.get("dy" + t)));

			gbr = gbr.add(acts.get("dr" + t));
			gbu = gbu.add(acts.get("dz" + t));
			gbh = gbh.add(acts.get("dgh" + t));
			gby = gby.add(acts.get("dy" + t));
		}

		Wxu = Wxu.sub(clip(gWxr.div(lastT)).mul(lr));
		Whu = Whu.sub(clip(gWhr.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		bu = bu.sub(clip(gbr.div(lastT)).mul(lr));

		Wxr = Wxr.sub(clip(gWxu.div(lastT)).mul(lr));
		Whr = Whr.sub(clip(gWhu.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		br = br.sub(clip(gbu.div(lastT)).mul(lr));

		Wxh = Wxh.sub(clip(gWxh.div(lastT)).mul(lr));
		Whh = Whh.sub(clip(gWhh.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		bh = bh.sub(clip(gbh.div(lastT)).mul(lr));

		Why = Why.sub(clip(gWhy.div(lastT)).mul(lr));
		by = by.sub(clip(gby.div(lastT)).mul(lr));
	}

	private FloatMatrix deriveExp(FloatMatrix f) {
		return f.mul(FloatMatrix.ones(1, f.length).sub(f));
	}

	private FloatMatrix deriveTanh(FloatMatrix f) {
		return FloatMatrix.ones(1, f.length).sub(MatrixFunctions.pow(f, 2));
	}

	private FloatMatrix clip(FloatMatrix x) {
		//double v = 10;
		//return x.mul(x.ge(-v).mul(x.le(v)));
		return x;
	}

	public FloatMatrix decode(FloatMatrix ht) {
		if (bUseLambda) {
			for (int i = 0; i < ht.rows; ++i) {
				double sum = 0;
				for (int j = 0; j < ht.columns; ++j) {
					float x = ht.get(i, j);
					sum += x * x;
				}

				float sqrt = (float) Math.sqrt(sum);

				for (int j = 0; j < ht.columns; ++j) {
					int index = ht.index(i, j);
					ht.put(index, ht.get(index) / sqrt);
				}
			}
		}
		FloatMatrix y = ht;
		if (Why != null)
			y = ht.mmul(Why);
		if (by != null)
			y.add(by);

		if (softmax_activation != null)
			return softmax_activation.activate(y);
		return y;
	}

	// forward pass
	public FloatMatrix forward_propogate(String seq) {
		Map<String, FloatMatrix> acts = new HashMap<String, FloatMatrix>();
		int t = 0;
		int length = this.mask_zero ? seq.length() : this.maximum_length;
		for (; t < length; t++) {
			int index;
			if (t < seq.length()) {
				String ch = String.valueOf(seq.charAt(t));

				index = charID.containsKey(ch) ? charID.get(ch) : 1;
			} else
				index = 0;

			System.out.print(index + " ");
			FloatMatrix xt = charVector.getRow(index);
			acts.put("x" + t, xt);

			active(t, acts);
		}

		System.out.println();
		return decode(acts.get("h" + (t - 1)));
	}

}
