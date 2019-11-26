package com.deeplearning.rnn;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import com.deeplearning.utils.Activation;
import com.deeplearning.utils.Activator;
import com.util.Utility;

public class LSTM extends RNN implements Serializable {
	private static final long serialVersionUID = -7059290852389115565L;

	private DoubleMatrix Wxi;
	private DoubleMatrix Whi;
	private DoubleMatrix Wci;
	private DoubleMatrix bi;

	private DoubleMatrix Wxf;
	private DoubleMatrix Whf;
	private DoubleMatrix Wcf;
	private DoubleMatrix bf;

	private DoubleMatrix Wxc;
	private DoubleMatrix Whc;
	private DoubleMatrix bc;

	private DoubleMatrix Wxo;
	private DoubleMatrix Who;
	private DoubleMatrix Wco;
	private DoubleMatrix bo;

	private DoubleMatrix Why;
	private DoubleMatrix by;

	Activation sigmoid = Activation.hard_sigmoid;
	Activation tanh = Activation.tanh;

	public LSTM(DoubleMatrix Wxi, DoubleMatrix Wxf, DoubleMatrix Wxc, DoubleMatrix Wxo, DoubleMatrix Whi,
			DoubleMatrix Whf, DoubleMatrix Whc, DoubleMatrix Who, DoubleMatrix bi, DoubleMatrix bf, DoubleMatrix bc,
			DoubleMatrix bo) {
		this.Wxi = Wxi;
		this.Wxf = Wxf;
		this.Wxc = Wxc;
		this.Wxo = Wxo;

		this.Whi = Whi;
		this.Whf = Whf;
		this.Whc = Whc;
		this.Who = Who;

		this.bi = bi;
		this.bf = bf;
		this.bc = bc;
		this.bo = bo;

		this.sigmoid = Activation.hard_sigmoid;
		this.tanh = Activation.tanh;
	}

	public DoubleMatrix call(DoubleMatrix[] x) {
		DoubleMatrix[] hc = { null, null };

		for (int t = 0; t < x.length; ++t) {
			activate(x[t], hc);
		}

		return hc[0];
	}

	public void activate(DoubleMatrix x, DoubleMatrix[] hc) {
		DoubleMatrix h = hc[0];
		DoubleMatrix c = hc[1];
		DoubleMatrix o;
		if (h == null) {
			DoubleMatrix i = sigmoid.activate(x.mmul(Wxi).addi(bi));
			c = i.mul(tanh.activate(x.mmul(Wxc).addi(bc)));
			o = sigmoid.activate(x.mmul(Wxo).addi(bo));
		} else {
			DoubleMatrix i = sigmoid.activate(x.mmul(Wxi).addi(h.mmul(Whi)).addi(bi));
			DoubleMatrix f = sigmoid.activate(x.mmul(Wxf).addi(h.mmul(Whf)).addi(bf));
			c = f.mul(c).addi(i.mul(tanh.activate(x.mmul(Wxc).addi(h.mmul(Whc)).addi(bc))));
			o = sigmoid.activate(x.mmul(Wxo).addi(h.mmul(Who)).addi(bo));
		}

		hc[0] = o.mul(tanh.activate(c));
		hc[1] = c;
	}

	public void bptt(Map<String, DoubleMatrix> acts, int lastT, double lr) {
		for (int t = lastT; t > -1; t--) {
			DoubleMatrix py = acts.get("py" + t);
			DoubleMatrix y = acts.get("y" + t);
			DoubleMatrix deltaY = py.sub(y);
			acts.put("dy" + t, deltaY);

			// cell output errors
			DoubleMatrix h = acts.get("h" + t);
			DoubleMatrix deltaH = null;
			if (t == lastT) {
				deltaH = Why.mmul(deltaY.transpose()).transpose();
			} else {
				DoubleMatrix lateDgc = acts.get("dgc" + (t + 1));
				DoubleMatrix lateDf = acts.get("df" + (t + 1));
				DoubleMatrix lateDo = acts.get("do" + (t + 1));
				DoubleMatrix lateDi = acts.get("di" + (t + 1));
				deltaH = Why.mmul(deltaY.transpose()).transpose().add(Whc.mmul(lateDgc.transpose()).transpose())
						.add(Whi.mmul(lateDi.transpose()).transpose()).add(Who.mmul(lateDo.transpose()).transpose())
						.add(Whf.mmul(lateDf.transpose()).transpose());
			}
			acts.put("dh" + t, deltaH);

			// output gates
			DoubleMatrix gh = acts.get("gh" + t);
			DoubleMatrix o = acts.get("o" + t);
			DoubleMatrix deltaO = deltaH.mul(gh).mul(deriveExp(o));
			acts.put("do" + t, deltaO);

			// status
			DoubleMatrix deltaC = null;
			if (t == lastT) {
				deltaC = deltaH.mul(o).mul(deriveTanh(gh)).add(Wco.mmul(deltaO.transpose()).transpose());
			} else {
				DoubleMatrix lateDc = acts.get("dc" + (t + 1));
				DoubleMatrix lateDf = acts.get("df" + (t + 1));
				DoubleMatrix lateF = acts.get("f" + (t + 1));
				DoubleMatrix lateDi = acts.get("di" + (t + 1));
				deltaC = deltaH.mul(o).mul(deriveTanh(gh)).add(Wco.mmul(deltaO.transpose()).transpose())
						.add(lateF.mul(lateDc)).add(Wcf.mmul(lateDf.transpose()).transpose())
						.add(Wci.mmul(lateDi.transpose()).transpose());
			}
			acts.put("dc" + t, deltaC);

			// cells
			DoubleMatrix gc = acts.get("gc" + t);
			DoubleMatrix i = acts.get("i" + t);
			DoubleMatrix deltaGc = deltaC.mul(i).mul(deriveTanh(gc));
			acts.put("dgc" + t, deltaGc);

			DoubleMatrix preC = null;
			if (t > 0) {
				preC = acts.get("c" + (t - 1));
			} else {
				preC = DoubleMatrix.zeros(1, h.length);
			}
			// forget gates
			DoubleMatrix f = acts.get("f" + t);
			DoubleMatrix deltaF = deltaC.mul(preC).mul(deriveExp(f));
			acts.put("df" + t, deltaF);

			// input gates
			DoubleMatrix deltaI = deltaC.mul(gc).mul(deriveExp(i));
			acts.put("di" + t, deltaI);
		}
		updateParameters(acts, lastT, lr);
	}

	private void updateParameters(Map<String, DoubleMatrix> acts, int lastT, double lr) {
		DoubleMatrix gWxi = new DoubleMatrix(Wxi.rows, Wxi.columns);
		DoubleMatrix gWhi = new DoubleMatrix(Whi.rows, Whi.columns);
		DoubleMatrix gWci = new DoubleMatrix(Wci.rows, Wci.columns);
		DoubleMatrix gbi = new DoubleMatrix(bi.rows, bi.columns);

		DoubleMatrix gWxf = new DoubleMatrix(Wxf.rows, Wxf.columns);
		DoubleMatrix gWhf = new DoubleMatrix(Whf.rows, Whf.columns);
		DoubleMatrix gWcf = new DoubleMatrix(Wcf.rows, Wcf.columns);
		DoubleMatrix gbf = new DoubleMatrix(bf.rows, bf.columns);

		DoubleMatrix gWxc = new DoubleMatrix(Wxc.rows, Wxc.columns);
		DoubleMatrix gWhc = new DoubleMatrix(Whc.rows, Whc.columns);
		DoubleMatrix gbc = new DoubleMatrix(bc.rows, bc.columns);

		DoubleMatrix gWxo = new DoubleMatrix(Wxo.rows, Wxo.columns);
		DoubleMatrix gWho = new DoubleMatrix(Who.rows, Who.columns);
		DoubleMatrix gWco = new DoubleMatrix(Wco.rows, Wco.columns);
		DoubleMatrix gbo = new DoubleMatrix(bo.rows, bo.columns);

		DoubleMatrix gWhy = new DoubleMatrix(Why.rows, Why.columns);
		DoubleMatrix gby = new DoubleMatrix(by.rows, by.columns);

		for (int t = 0; t < lastT + 1; t++) {
			DoubleMatrix x = acts.get("x" + t).transpose();
			gWxi = gWxi.add(x.mmul(acts.get("di" + t)));
			gWxf = gWxf.add(x.mmul(acts.get("df" + t)));
			gWxc = gWxc.add(x.mmul(acts.get("dgc" + t)));
			gWxo = gWxo.add(x.mmul(acts.get("do" + t)));

			if (t > 0) {
				DoubleMatrix preH = acts.get("h" + (t - 1)).transpose();
				DoubleMatrix preC = acts.get("c" + (t - 1)).transpose();
				gWhi = gWhi.add(preH.mmul(acts.get("di" + t)));
				gWhf = gWhf.add(preH.mmul(acts.get("df" + t)));
				gWhc = gWhc.add(preH.mmul(acts.get("dgc" + t)));
				gWho = gWho.add(preH.mmul(acts.get("do" + t)));
				gWci = gWci.add(preC.mmul(acts.get("di" + t)));
				gWcf = gWcf.add(preC.mmul(acts.get("df" + t)));
			}
			gWco = gWco.add(acts.get("c" + t).transpose().mmul(acts.get("do" + t)));
			gWhy = gWhy.add(acts.get("h" + t).transpose().mmul(acts.get("dy" + t)));

			gbi = gbi.add(acts.get("di" + t));
			gbf = gbf.add(acts.get("df" + t));
			gbc = gbc.add(acts.get("dgc" + t));
			gbo = gbo.add(acts.get("do" + t));
			gby = gby.add(acts.get("dy" + t));
		}

		Wxi = Wxi.sub(clip(gWxi.div(lastT)).mul(lr));
		Whi = Whi.sub(clip(gWhi.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		Wci = Wci.sub(clip(gWci.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		bi = bi.sub(clip(gbi.div(lastT)).mul(lr));

		Wxf = Wxf.sub(clip(gWxf.div(lastT)).mul(lr));
		Whf = Whf.sub(clip(gWhf.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		Wcf = Wcf.sub(clip(gWcf.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		bf = bf.sub(clip(gbf.div(lastT)).mul(lr));

		Wxc = Wxc.sub(clip(gWxc.div(lastT)).mul(lr));
		Whc = Whc.sub(clip(gWhc.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		bc = bc.sub(clip(gbc.div(lastT)).mul(lr));

		Wxo = Wxo.sub(clip(gWxo.div(lastT)).mul(lr));
		Who = Who.sub(clip(gWho.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
		Wco = Wco.sub(clip(gWco.div(lastT)).mul(lr));
		bo = bo.sub(clip(gbo.div(lastT)).mul(lr));

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
		return Activator.softmax(ht.mmul(Why).add(by));
	}

	public LSTM(Utility.BinaryReader dis) throws IOException {
		this.Wxi = new DoubleMatrix(dis.readArray2());
		this.Wxf = new DoubleMatrix(dis.readArray2());
		this.Wxc = new DoubleMatrix(dis.readArray2());
		this.Wxo = new DoubleMatrix(dis.readArray2());

		this.Whi = new DoubleMatrix(dis.readArray2());
		this.Whf = new DoubleMatrix(dis.readArray2());
		this.Whc = new DoubleMatrix(dis.readArray2());
		this.Who = new DoubleMatrix(dis.readArray2());

		this.bi = new DoubleMatrix(dis.readArray1());
		this.bf = new DoubleMatrix(dis.readArray1());
		this.bc = new DoubleMatrix(dis.readArray1());
		this.bo = new DoubleMatrix(dis.readArray1());
	}

	@Override
	public DoubleMatrix[] call_return_sequences(DoubleMatrix[] x) {
		DoubleMatrix[] arr = new DoubleMatrix[x.length];
		DoubleMatrix[] hc = { null, null };

		for (int t = 0, length = x.length; t < length; ++t) {
			activate(x[t], hc);
			arr[t] = hc[0];
		}

		return arr;
	}

	@Override
	public DoubleMatrix[] call_return_sequences_reverse(DoubleMatrix[] x) {
		DoubleMatrix[] arr = new DoubleMatrix[x.length];
		DoubleMatrix[] hc = { null, null };

		for (int t = x.length - 1; t >= 0; --t) {
			activate(x[t], hc);
			arr[t] = hc[0];
		}

		return arr;
	}

	@Override
	public DoubleMatrix call_reverse(DoubleMatrix[] x) {
		// TODO Auto-generated method stub
		return null;
	}
}