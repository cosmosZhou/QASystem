package com.robot;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

import com.robot.Sentence.Protagonist;

import com.util.Utility;

public class QACouplet implements Serializable, Comparable<QACouplet> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1509472908902869616L;

	QACouplet(Sentence question, Sentence answer, double coherence, Date time, String respondent, Origin origin) {
		this.que = question;
		this.ans = answer;
		this.coherence = coherence;
		this.time = time;
		//the answerer for the question
		this.respondent = respondent;
		this.origin = origin;
		this.frequency = 1;
	}

	public QACouplet(String question, String answer, double coherence, Date time, String respondent, Origin origin) throws Exception {
		this(new Sentence(question, Protagonist.CUSTOMER), new Sentence(answer, Protagonist.OPERATOR), coherence, time, respondent, origin);
	}

	@Override
	public int hashCode() {
		return que.hashCode() + ans.hashCode() * 31;
	}

	@Override
	public boolean equals(Object anObject) {
		if (anObject instanceof QACouplet) {
			QACouplet couplet = (QACouplet) anObject;
			return que.equals(couplet.que) && ans.equals(couplet.ans);
		}

		return false;
	}

	/**
	 * precondition: weight is in the interval [-1, 1]
	 * 
	 * @param weight
	 */
	void improve_confidence(double weight) {
		coherence = Math.pow(coherence, 1 / (1 + weight));
		frequency += weight;
		if (frequency <= 0) {
			frequency = 0.5;
		}
	}

	double similarity(QACouplet y) throws Exception {
		double a = que.questionSimilarity(y.que);
		double b = ans.answerSimilarity(y.ans);
		if (a < b) {
			return Math.sqrt(0.5 * a * a + 0.5 * b * b);
		} else
			return Math.sqrt(0.7 * a * a + 0.3 * b * b);
	}

	// the question field, doute = question ; 
	public Sentence que;
	public Sentence ans;
	public double coherence;
	public double frequency;
	public Date time;
	public String respondent;

	public static enum Origin implements Serializable {
		ROBOT_RESERVOIR, SYSTEM_RESERVOIR, INDIVIDUAL_RESERVOIR, SUPERVISOR_RESERVOIR,
	}

	static public boolean isSupervised(Origin provenance) {
		switch (provenance) {
		case SYSTEM_RESERVOIR:
		case INDIVIDUAL_RESERVOIR:
		case SUPERVISOR_RESERVOIR:
			return true;
		default:
			return false;
		}
	}

	public static Origin parseIntToOrigin(int i) {
		switch (i) {
		case 0:
			return Origin.ROBOT_RESERVOIR;
		case 1:
			return Origin.SYSTEM_RESERVOIR;
		case 2:
			return Origin.INDIVIDUAL_RESERVOIR;
		case 3:
			return Origin.SUPERVISOR_RESERVOIR;
		default:
			return Origin.ROBOT_RESERVOIR;
		}
	}

	public static String toString(Origin provenance) {
		switch (provenance) {
		case SYSTEM_RESERVOIR:
			return "系统预存";
		case INDIVIDUAL_RESERVOIR:
			return "个人预存";
		case SUPERVISOR_RESERVOIR:
			return "标准库";
		default:
			return "机器人";
		}
	}

	public Origin origin = Origin.ROBOT_RESERVOIR;

	/**
	 * compare the distance of time between two QACouplets.
	 * 
	 * @param autre
	 * @return
	 */
	double efficacy(QACouplet epitome) {
		if (origin == Origin.ROBOT_RESERVOIR) {
			long dif = epitome.time.getTime() - time.getTime();
			dif /= 1000;
			if (dif <= 0)
				return 1;
			return Math.exp(-(dif / (DecadentHalfLife * frequency)) * Math.log(2));
		}
		return 1;
	}

	// public String toString() {
	// return "question :" + question + "" + "answer :" + answer
	// + "" + "confidence = " + confidence * 100 + "%";
	// }

	double similarityScore(HashSet<QACouplet> set) throws Exception {
		double sum = 0;
		for (QACouplet Sentence : set) {
			sum += similarity(Sentence);
		}
		sum /= set.size();
		return sum;
	}

	public String toString() {
		return "question = " + que + "\n" + "answer = " + ans + "\n";
	}

	/**
	 * half life of decay; length of a month = one fortnight;
	 */
	static final double DecadentHalfLife = 14 * 86400;

	@Override
	public int compareTo(QACouplet o) {
		if (que.equals(o.que) && ans.equals(o.ans))
			return 0;
		int cmp = origin.ordinal() - o.origin.ordinal();
		if (cmp != 0)
			return cmp;
		cmp = Double.compare(coherence, o.coherence);
		if (cmp != 0)
			return cmp;
		cmp = this.time.compareTo(o.time);
		if (cmp != 0)
			return -cmp;
		cmp = que.sentence.compareTo(o.que.sentence);
		if (cmp != 0)
			return cmp;
		cmp = ans.sentence.compareTo(o.ans.sentence);
		if (cmp != 0)
			return cmp;
		return cmp;
	}
}
