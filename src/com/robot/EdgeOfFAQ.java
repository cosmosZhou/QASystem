package com.robot;

import org.apache.log4j.Logger;

import com.util.Utility.Couplet;

class EdgeOfFAQ extends Couplet<FAQ, FAQ> implements Comparable<EdgeOfFAQ> {
	EdgeOfFAQ(FAQ x, FAQ y) throws Exception {
		super(x, y);

		if (x == y) {
			throw new Exception("error detected: x == y");
		}
		if (x.id == y.id) {
			throw new Exception("error detected: x.id == y.id");
		}

		this.similarity = x.similarity(y);
	}

	double similarity;

	@Override
	public int compareTo(EdgeOfFAQ o) {
		int cmp = java.lang.Double.compare(similarity, o.similarity);
		if (cmp != 0)
			return cmp;
		// cmp = Integer.compare(x, o.x);
		// if (cmp != 0)
		// return cmp;
		// cmp = Integer.compare(y, o.y);
		// if (cmp != 0)
		// return cmp;
		return 0;
	}

	public static Logger log = Logger.getLogger(EdgeOfFAQ.class);
	}
