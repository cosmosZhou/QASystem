package com.deeplearning;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jblas.DoubleMatrix;

import com.deeplearning.rnn.RNN;
import com.deeplearning.utils.Activation;
import com.util.Utility;
import com.util.Utility.BinaryReader;

/**
 * implimentation of Gated Recurrent Unit
 * 
 * @author Cosmos
 *
 */

public class GRU extends RNN implements Serializable {
	private static final long serialVersionUID = -1501734916541393551L;

	Activation sigmoid = Activation.hard_sigmoid;
	Activation tanh = Activation.tanh;
	Activation softmax = Activation.softmax;

	boolean bUseLambda = false;
	DoubleMatrix charVector;
	Map<String, Integer> charID;
	Map<Character, DoubleMatrix> charEmbedding;

	protected int maximum_length;
	protected boolean mask_zero;

	private int xSize;
	private int hSize;
	private int ySize;

	public DoubleMatrix Wxu, Wxr, Wxh, Whu, Whr, Whh, bu, br, bh;

	public GRU(DoubleMatrix Wxu, DoubleMatrix Wxr, DoubleMatrix Wxh, DoubleMatrix Whu, DoubleMatrix Whr,
			DoubleMatrix Whh, DoubleMatrix bu, DoubleMatrix br, DoubleMatrix bh) {
		this.Wxu = Wxu;
		this.Wxr = Wxr;
		this.Wxh = Wxh;

		this.Whu = Whu;
		this.Whr = Whr;
		this.Whh = Whh;

		this.bu = bu;
		this.br = br;
		this.bh = bh;
	}

	public GRU() {
	}

	public GRU(Utility.BinaryReader dis) throws IOException {
		this.Wxu = new DoubleMatrix(dis.readArray2());
		this.Wxr = new DoubleMatrix(dis.readArray2());
		this.Wxh = new DoubleMatrix(dis.readArray2());

		this.Whu = new DoubleMatrix(dis.readArray2());
		this.Whr = new DoubleMatrix(dis.readArray2());
		this.Whh = new DoubleMatrix(dis.readArray2());

		this.bu = new DoubleMatrix(dis.readArray1());
		this.br = new DoubleMatrix(dis.readArray1());
		this.bh = new DoubleMatrix(dis.readArray1());
	}

	public int getxSize() {
		return xSize;
	}

	private int gethSize() {
		return hSize;
	}

	public int getySize() {
		return ySize;
	}

	public static class HidenLayerGradient {
		public DoubleMatrix Wxr;
		public DoubleMatrix Whr;
		public DoubleMatrix br;

		public DoubleMatrix Wxu;
		public DoubleMatrix Whu;
		public DoubleMatrix bu;

		public DoubleMatrix Wxh;
		public DoubleMatrix Whh;
		public DoubleMatrix bh;

		public DoubleMatrix x, h, dy, y;
	}

	public static class GRUGradient {
		public DoubleMatrix Wxr;
		public DoubleMatrix Whr;
		public DoubleMatrix br;

		public DoubleMatrix Wxu;
		public DoubleMatrix Whu;
		public DoubleMatrix bu;

		public DoubleMatrix Wxh;
		public DoubleMatrix Whh;
		public DoubleMatrix bh;

		public DoubleMatrix Why; // for the final softmax layer
		public DoubleMatrix by; // for bias of the final softmax layer

		public DoubleMatrix dy, y;
		public Map<Character, DoubleMatrix> charEmbedding = new HashMap<Character, DoubleMatrix>();

		public void add(OutputLayerGradient outputLayerGradient) {
			Why = Utility.addi(Why, outputLayerGradient.Why);
			by = Utility.addi(by, outputLayerGradient.by);
		}

		public void add(HidenLayerGradient hidenLayerGradient, char ch) {

			Wxr = Utility.addi(Wxr, hidenLayerGradient.Wxr);
			Whr = Utility.addi(Whr, hidenLayerGradient.Whr);
			br = Utility.addi(br, hidenLayerGradient.br);

			Wxu = Utility.addi(Wxu, hidenLayerGradient.Wxu);
			Whu = Utility.addi(Whu, hidenLayerGradient.Whu);

			bu = Utility.addi(bu, hidenLayerGradient.bu);
			Wxh = Utility.addi(Wxh, hidenLayerGradient.Wxh);
			Whh = Utility.addi(Whh, hidenLayerGradient.Whh);
			bh = Utility.addi(bh, hidenLayerGradient.bh);

			if (charEmbedding.containsKey(ch))
				charEmbedding.get(ch).addi(hidenLayerGradient.x);
			else
				charEmbedding.put(ch, hidenLayerGradient.x);
		}

	}

	public static class OutputLayerGradient {
		public DoubleMatrix Why; // for the final softmax layer
		public DoubleMatrix by; // for bias of the final softmax layer

		public DoubleMatrix h, dy, y;
	}

	public DoubleMatrix active(DoubleMatrix x, DoubleMatrix h, HidenLayerGradient gradient) {
		assert gradient.y != null;

		if (h == null) {
			DoubleMatrix u = x.mmul(Wxu);
			if (bu != null)
				u = u.add(bu);
			u = sigmoid.activate(u);

			DoubleMatrix gh = x.mmul(Wxh);
			if (bh != null)
				gh = gh.add(bh);
			gh = tanh.activate(gh);

			// (1- u) * gh

			DoubleMatrix dgh = gradient.dy.mul(DoubleMatrix.ones(1, u.columns).sub(u));
			dgh = tanh.activateDerivative(gh).mul(dgh);
			if (bh != null)
				gradient.bh = dgh;
			gradient.Wxh = x.transpose().mmul(dgh);
			gradient.x = dgh.mul(Wxh.transpose());

			DoubleMatrix du = gradient.dy.mul(gh).neg();
			du = sigmoid.activateDerivative(u).mul(du);
			if (bu != null)
				gradient.bu = du;
			gradient.Wxu = x.transpose().mmul(du);

			gradient.x.addi(du.mmul(Wxu.transpose()));

//			return (DoubleMatrix.ones(1, u.columns).sub(u)).mul(gh);
		} else {

			DoubleMatrix r = x.mmul(Wxr).add(h.mmul(Whr));
			if (br != null)
				r = r.add(br);
			r = sigmoid.activate(r);

			DoubleMatrix u = x.mmul(Wxu).add(h.mmul(Whu));
			if (bu != null)
				u = u.add(bu);
			u = sigmoid.activate(u);

			DoubleMatrix gh = x.mmul(Wxh).add(r.mul(h).mmul(Whh));
			if (bh != null)
				gh = gh.add(bh);
			gh = tanh.activate(gh);

			// (1- u) * gh + u * h

			DoubleMatrix dgh = gradient.dy.mul(DoubleMatrix.ones(1, u.columns).sub(u));
			dgh = tanh.activateDerivative(gh).mul(dgh);
			if (bh != null)
				gradient.bh = dgh;
			gradient.Wxh = x.transpose().mmul(dgh);
			gradient.x = dgh.mmul(Wxh.transpose());
//			gh = x * Wxh + r * h * Whh

			DoubleMatrix drh = dgh.mmul(Whh.transpose());

			DoubleMatrix dr = drh.mul(h);
			gradient.h = drh.mul(r);

			gradient.Whh = r.mul(h).transpose().mmul(dgh);

			DoubleMatrix du = gradient.dy.mul(gh).neg().add(h);
			du = sigmoid.activateDerivative(u).mul(du);
			if (bu != null)
				gradient.bu = du;
//			u = x * Wxu + h * Whu
			gradient.Wxu = x.transpose().mmul(du);
			gradient.x.addi(du.mmul(Wxu.transpose()));
			gradient.h.addi(du.mmul(Whu.transpose()));
			gradient.Wxu = h.transpose().mmul(du);

//			r = x * Wxr + h * Whr;
			dr = sigmoid.activateDerivative(dr).mul(dr);
			if (br != null)
				gradient.br = dr;
			gradient.Wxr = x.transpose().mmul(dr);
			gradient.x.addi(dr.mmul(Wxr.transpose()));
			gradient.h.addi(dr.mmul(Whr.transpose()));
			gradient.Whr = h.transpose().mmul(dr);

//			return (DoubleMatrix.ones(1, u.columns).sub(u)).mul(gh).add(u.mul(h));
		}
		return gradient.y;
	}

	public DoubleMatrix activate(DoubleMatrix x, DoubleMatrix h) {
		if (h == null) {
			DoubleMatrix u = x.mmul(Wxu);
			if (bu != null)
				u.addi(bu);
			u = sigmoid.activate(u);

			DoubleMatrix gh = x.mmul(Wxh);
			if (bh != null)
				gh.addi(bh);
			gh = tanh.activate(gh);

			return (DoubleMatrix.ones(1, u.columns).sub(u)).mul(gh);
		} else {

			DoubleMatrix r = x.mmul(Wxr).add(h.mmul(Whr));
			if (br != null)
				r.addi(br);
			r = sigmoid.activate(r);

			DoubleMatrix u = x.mmul(Wxu).add(h.mmul(Whu));
			if (bu != null)
				u.addi(bu);
			u = sigmoid.activate(u);

			DoubleMatrix gh = x.mmul(Wxh).add(r.mul(h).mmul(Whh));
			if (bh != null)
				gh.addi(bh);
			gh = tanh.activate(gh);

			return (DoubleMatrix.ones(1, u.columns).sub(u)).mul(gh).add(u.mul(h));
		}
	}

	public static GRU initialize(Utility.BinaryReader dis) throws IOException {
		DoubleMatrix Wxu = new DoubleMatrix(dis.readArray2());
		DoubleMatrix Wxr = new DoubleMatrix(dis.readArray2());
		DoubleMatrix Wxh = new DoubleMatrix(dis.readArray2());
		DoubleMatrix Whu = new DoubleMatrix(dis.readArray2());
		DoubleMatrix Whr = new DoubleMatrix(dis.readArray2());
		DoubleMatrix Whh = new DoubleMatrix(dis.readArray2());
		DoubleMatrix bu = new DoubleMatrix(dis.readArray1());
		DoubleMatrix br = new DoubleMatrix(dis.readArray1());
		DoubleMatrix bh = new DoubleMatrix(dis.readArray1());
		return new GRU(Wxu, Wxr, Wxh, Whu, Whr, Whh, bu, br, bh);
	}

	private DoubleMatrix clip(DoubleMatrix x) {
		// double v = 10;
		// return x.mul(x.ge(-v).mul(x.le(v)));
		return x;
	}

	// forward pass
	public DoubleMatrix propogate(String seq) {
		DoubleMatrix h = null;

		int length = this.mask_zero ? seq.length() : this.maximum_length;
		int t = 0;
		for (; t < length; ++t) {
			int index;
			if (t < seq.length()) {
				String ch = String.valueOf(seq.charAt(t));

				index = charID.containsKey(ch) ? charID.get(ch) : 1;
			} else
				index = 0;

			System.out.print(index + " ");
			DoubleMatrix xt = charVector.getRow(index);

			h = activate(xt, h);
		}

		System.out.println();
		return h;
	}

	public DoubleMatrix call(DoubleMatrix[] x) {
		DoubleMatrix h = null;

		for (int t = 0; t < x.length; ++t) {
			h = activate(x[t], h);
		}

		return h;
	}

	// forward pass
	public DoubleMatrix propogate(String seq, GRUGradient gruGradient) {
		DoubleMatrix hHistory[] = new DoubleMatrix[seq.length() + 1];
		for (int i = 0; i < seq.length(); ++i) {

			char ch = seq.charAt(i);
			DoubleMatrix xt = charEmbedding.get(ch);

			hHistory[i + 1] = activate(xt, hHistory[i]);
		}

		System.out.println();

		DoubleMatrix h = hHistory[seq.length()];

		assert softmax == Activation.softmax;

		DoubleMatrix dy = gruGradient.dy;
		for (int i = seq.length() - 1; i >= 0; --i) {
			HidenLayerGradient hidenLayerGradient = new HidenLayerGradient();
			hidenLayerGradient.dy = dy;
			hidenLayerGradient.y = hHistory[i + 1];

			char ch = seq.charAt(i);
			DoubleMatrix xt = charEmbedding.get(ch);

			active(xt, hHistory[i], hidenLayerGradient);
			gruGradient.add(hidenLayerGradient, ch);
			dy = hidenLayerGradient.h;
		}

		return gruGradient.y;
	}

	@Override
	public DoubleMatrix call_reverse(DoubleMatrix[] x) {
		DoubleMatrix h = null;

		for (int t = x.length - 1; t >= 0; --t) {
			h = activate(x[t], h);
		}

		return h;
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
