package com.deeplearning.rnn;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import com.deeplearning.utils.Activation;

/**
 * implimentation of Gated Recurrent Unit
 * 
 * @author Cosmos
 *
 */

public class GRU extends RNN implements Serializable {
	private static final long serialVersionUID = -1501734916541393551L;

	Activation recurrent_activation = Activation.hard_sigmoid;
	Activation hidden_activation = Activation.tanh;
	Activation softmax_activation = Activation.softmax;

	boolean bUseLambda = false;

	DoubleMatrix charVector;
	Map<String, Integer> charID;

	private int xSize;
	private int hSize;
	private int ySize;

	private DoubleMatrix Wxu;
	private DoubleMatrix Whu;
	private DoubleMatrix bu;

	private DoubleMatrix Wxr;
	private DoubleMatrix Whr;
	private DoubleMatrix br;

	private DoubleMatrix Wxh;
	private DoubleMatrix Whh;
	private DoubleMatrix bh;

	private DoubleMatrix Why; // for the final softmax layer
	private DoubleMatrix by; // for bias of the final softmax layer

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

	public void active(int t, Map<String, DoubleMatrix> acts) {
		DoubleMatrix x = acts.get("x" + t);
		DoubleMatrix h;
		if (t == 0) {
			DoubleMatrix u = x.mmul(Wxu);
			if (bu != null)
				u = u.add(bu);
			u = recurrent_activation.activate(u);

			DoubleMatrix gh = x.mmul(Wxh);
			if (bh != null)
				gh = gh.add(bh);
			gh = hidden_activation.activate(gh);

			h = DoubleMatrix.ones(1, u.columns).sub(u).mul(gh);

		} else {
			DoubleMatrix preH = acts.get("h" + (t - 1));

			DoubleMatrix r = x.mmul(Wxr).add(preH.mmul(Whr));
			if (br != null)
				r = r.add(br);
			r = recurrent_activation.activate(r);

			DoubleMatrix u = x.mmul(Wxu).add(preH.mmul(Whu));
			if (bu != null)
				u = u.add(bu);
			u = recurrent_activation.activate(u);

			DoubleMatrix gh = x.mmul(Wxh).add(r.mul(preH).mmul(Whh));
			if (bh != null)
				gh = gh.add(bh);
			gh = hidden_activation.activate(gh);

			h = (DoubleMatrix.ones(1, u.columns).sub(u)).mul(gh).add(u.mul(preH));
		}
		acts.put("h" + t, h);
	}

	public void bptt(Map<String, DoubleMatrix> acts, int lastT, float lr) {
		for (int t = lastT; t > -1; t--) {
			DoubleMatrix py = acts.get("py" + t);
			DoubleMatrix y = acts.get("y" + t);
			DoubleMatrix deltaY = py.sub(y);
			acts.put("dy" + t, deltaY);

			// cell output errors
			DoubleMatrix h = acts.get("h" + t);
			DoubleMatrix z = acts.get("z" + t);
			DoubleMatrix r = acts.get("r" + t);
			DoubleMatrix gh = acts.get("gh" + t);

			DoubleMatrix deltaH = null;
			if (t == lastT) {
				deltaH = Why.mmul(deltaY.transpose()).transpose();
			} else {
				DoubleMatrix lateDh = acts.get("dh" + (t + 1));
				DoubleMatrix lateDgh = acts.get("dgh" + (t + 1));
				DoubleMatrix lateDr = acts.get("dr" + (t + 1));
				DoubleMatrix lateDz = acts.get("dz" + (t + 1));
				DoubleMatrix lateR = acts.get("r" + (t + 1));
				DoubleMatrix lateZ = acts.get("z" + (t + 1));
				deltaH = Why.mmul(deltaY.transpose()).transpose().add(Whu.mmul(lateDr.transpose()).transpose())
						.add(Whr.mmul(lateDz.transpose()).transpose())
						.add(Whh.mmul(lateDgh.mul(lateR).transpose()).transpose())
						.add(lateDh.mul(DoubleMatrix.ones(1, lateZ.columns).sub(lateZ)));
			}
			acts.put("dh" + t, deltaH);

			// gh
			DoubleMatrix deltaGh = deltaH.mul(z).mul(deriveTanh(gh));
			acts.put("dgh" + t, deltaGh);

			DoubleMatrix preH = null;
			if (t > 0) {
				preH = acts.get("h" + (t - 1));
			} else {
				preH = DoubleMatrix.zeros(1, h.length);
			}

			// reset gates
			DoubleMatrix deltaR = (Whh.mmul(deltaGh.mul(preH).transpose()).transpose()).mul(deriveExp(r));
			acts.put("dr" + t, deltaR);

			// update gates
			DoubleMatrix deltaZ = deltaH.mul(gh.sub(preH)).mul(deriveExp(z));
			acts.put("dz" + t, deltaZ);
		}

		updateParameters(acts, lastT, lr);
	}

	private void updateParameters(Map<String, DoubleMatrix> acts, int lastT, float lr) {
		DoubleMatrix gWxr = new DoubleMatrix(Wxu.rows, Wxu.columns);
		DoubleMatrix gWhr = new DoubleMatrix(Whu.rows, Whu.columns);
		DoubleMatrix gbr = new DoubleMatrix(bu.rows, bu.columns);

		DoubleMatrix gWxu = new DoubleMatrix(Wxr.rows, Wxr.columns);
		DoubleMatrix gWhu = new DoubleMatrix(Whr.rows, Whr.columns);
		DoubleMatrix gbu = new DoubleMatrix(br.rows, br.columns);

		DoubleMatrix gWxh = new DoubleMatrix(Wxh.rows, Wxh.columns);
		DoubleMatrix gWhh = new DoubleMatrix(Whh.rows, Whh.columns);
		DoubleMatrix gbh = new DoubleMatrix(bh.rows, bh.columns);

		DoubleMatrix gWhy = new DoubleMatrix(Why.rows, Why.columns);
		DoubleMatrix gby = new DoubleMatrix(by.rows, by.columns);

		for (int t = 0; t < lastT + 1; t++) {
			DoubleMatrix x = acts.get("x" + t).transpose();
			gWxr = gWxr.add(x.mmul(acts.get("dr" + t)));
			gWxu = gWxu.add(x.mmul(acts.get("dz" + t)));
			gWxh = gWxh.add(x.mmul(acts.get("dgh" + t)));

			if (t > 0) {
				DoubleMatrix preH = acts.get("h" + (t - 1)).transpose();
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

	private DoubleMatrix deriveExp(DoubleMatrix f) {
		return f.mul(DoubleMatrix.ones(1, f.length).sub(f));
	}

	private DoubleMatrix deriveTanh(DoubleMatrix f) {
		return DoubleMatrix.ones(1, f.length).sub(MatrixFunctions.pow(f, 2));
	}

	private DoubleMatrix clip(DoubleMatrix x) {
		// double v = 10;
		// return x.mul(x.ge(-v).mul(x.le(v)));
		return x;
	}

	public DoubleMatrix decode(DoubleMatrix ht) {
		if (bUseLambda) {
			for (int i = 0; i < ht.rows; ++i) {
				double sum = 0;
				for (int j = 0; j < ht.columns; ++j) {
					double x = ht.get(i, j);
					sum += x * x;
				}

				float sqrt = (float) Math.sqrt(sum);

				for (int j = 0; j < ht.columns; ++j) {
					int index = ht.index(i, j);
					ht.put(index, ht.get(index) / sqrt);
				}
			}
		}
		DoubleMatrix y = ht;
		if (Why != null)
			y = ht.mmul(Why);
		if (by != null)
			y.add(by);

		if (softmax_activation != null)
			return softmax_activation.activate(y);
		return y;
	}

	// forward pass
	public DoubleMatrix forward_propogate(String seq) {
		Map<String, DoubleMatrix> acts = new HashMap<String, DoubleMatrix>();
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
			DoubleMatrix xt = charVector.getRow(index);
			acts.put("x" + t, xt);

			active(t, acts);
		}

		System.out.println();
		return decode(acts.get("h" + (t - 1)));
	}

	@Override
	public DoubleMatrix call(DoubleMatrix[] x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleMatrix call_reverse(DoubleMatrix[] x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleMatrix[] call_return_sequences(DoubleMatrix[] x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleMatrix[] call_return_sequences_reverse(DoubleMatrix[] x) {
		// TODO Auto-generated method stub
		return null;
	}

}
