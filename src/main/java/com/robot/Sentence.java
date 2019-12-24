package com.robot;

import java.io.Serializable;

import com.util.Native;
import com.util.Utility;

public class Sentence implements Serializable {

	public enum Protagonist {
		CUSTOMER, OPERATOR, ROBOT;
	}

	public enum QATYPE {
		QUERY, REPLY, NEUTRAL;
		public boolean isNeutral() {
			return this == NEUTRAL;
		}

		public boolean isQuery() {
			return this == QUERY;
		}

		public boolean isReply() {
			return this == REPLY;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5755811681194797169L;
	static final int INDEX_INTEGRITY = 0;
	static final int INDEX_INCOMPLETE = 1;
	static final int INDEX_ANAPHORA = 2;
	static final int INDEX_MULTIPLE = 3;

	public static void main(String[] args) throws Exception {
	}

	@Override
	public int hashCode() {
		return sentence.hashCode();
	}

	@Override
	public boolean equals(Object anObject) {
		if (anObject instanceof Sentence) {
			Sentence sent = (Sentence) anObject;
			return sentence.equals(sent.sentence);
		}

		return false;
	}

	public Sentence(String sentence) throws Exception {
		this.sentence = sentence;
	}

	public Sentence(String sentence, Protagonist protagonist) throws Exception {
		this.sentence = sentence;
		this.protagonist = protagonist;
	}

	public Sentence(String sentence, Protagonist protagonist, String speaker) throws Exception {
		this.sentence = sentence;
		this.protagonist = protagonist;
		this.speaker = speaker;
	}

	// int id = -1;
	public String sentence;
	Protagonist protagonist;
	String speaker;

	/**
	 * indicate whether it is a question or an answer;
	 */
	transient QATYPE qatype;
	// int anomaly;
	/**
	 * confidence of judgment of QA type;
	 */
	transient double confidence;

	public QATYPE qatype() throws Exception {
		if (qatype == null) {
			double score = Native.phatic(sentence);
			if (score < 0.5) {
				qatype = QATYPE.NEUTRAL;
				this.confidence = 1 - score;
			} else {// score >= RNNPhaticsClassifier.threshold
				this.confidence = Native.qatype(this.sentence);
				if (confidence > 0.5) {
					qatype = QATYPE.QUERY;
					confidence = Math.sqrt(score * confidence);
				} else {// if (confidence <= 0.5) {
					qatype = QATYPE.REPLY;
					confidence = Math.sqrt(score * (1 - confidence));
				}
			}
		}
		return qatype;
	}

	// public String anomaly() throws Exception {
	// switch (this.anomaly) {
	// case INDEX_INTEGRITY:
	// return "INTEGRITY";
	// case INDEX_INCOMPLETE:
	// return "INCOMPLETE";
	// case INDEX_ANAPHORA:
	// return "ANAPHORA";
	// case INDEX_MULTIPLE:
	// return "MULTIPLE";
	// default:
	// throw new Exception("unknown index for anomaly.");
	// }
	// }

	public String toString() {
		return sentence;
	}

	double entropyInformation = Double.NaN;

	/**
	 * the semantic similarity of two sentences can be negative;
	 * 
	 * @param sentence
	 * @return
	 * @throws Exception
	 */

	public double similarity(Sentence sentence) throws Exception {
		try {
			return Native.similarity(this.sentence, sentence.sentence);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0.1;
		}

	}

	static public int getIndexOfAnomaly(String anomaly) throws Exception {
		if (anomaly.equals("INTEGRITY"))
			return INDEX_INTEGRITY;
		if (anomaly.equals("INCOMPLETE"))
			return INDEX_INCOMPLETE;
		if (anomaly.equals("ANAPHORA"))
			return INDEX_ANAPHORA;
		if (anomaly.equals("MULTIPLE"))
			return INDEX_MULTIPLE;
		return -1;
	}

	static int find_question_sequential(Sentence[] history, int i) {
		for (; i < history.length; ++i) {
			if (history[i].protagonist == Protagonist.OPERATOR)
				continue;
			// if (Utility.contains(history[i].feature[1].x, "BONJOUR"))
			// continue;
			// if (history[i].anomaly == INDEX_INCOMPLETE)
			// continue;

			if (history[i].qatype != QATYPE.QUERY)
				continue;

			// find a pertinent question sentence from the visitor's speech
			return i;
		}

		return -1;
	}

	static Utility.Couplet<Integer, Double> find_answer_sequential(Sentence[] history, int i) throws Exception {
		if ((history[i].protagonist != Protagonist.CUSTOMER)) {
			throw new Exception("(history[i].protagonist != INDEX_CUSTOMER)");
		}

		if (history[i].qatype != QATYPE.QUERY) {
			throw new Exception("history[i].qatype != INDEX_QUE");
		}

		double confidence = 1;
		for (++i; i < history.length; ++i) {
			// if it is a visitor, to see whether the old question is
			// overwhelmed by the new question proposed by the visitor
			confidence *= history[i].confidence;
			if (history[i].qatype.isNeutral())
				continue;

			if (history[i].protagonist == Protagonist.CUSTOMER) {
				if (history[i].qatype == QATYPE.QUERY)
					break;
				else
					continue;
			}

			if (history[i].qatype == QATYPE.REPLY) {
				// a pertinent DECLARATIVE sentence from the operator has been
				// detected.
				return new Utility.Couplet<Integer, Double>(i, confidence);
			}
		}

		return null;
	}

	public String decompile() throws Exception {
		return Conversation.decompile(sentence, protagonist.ordinal());
	}

	// we should determine the parents of the interrogative, whether there are
	// multiple verbs above.

	boolean isIncompleteQuestion() {
		return this.sentence.length() <= 3;
	}

}
