package com.robot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.robot.QACouplet.Origin;
import com.util.Utility;
import com.util.Utility.PriorityQueue;

public class FAQ extends ArrayList<QACouplet> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8237697587239652376L;

	FAQ(int id, QACouplet couplet) {
		super();
		add(couplet);
		this.id = id;
	}

	FAQ(int id) {
		super();
		this.id = id;
	}

	public int id;
	public boolean isChanged = false;

	/**
	 * when similarity = 0, couplet.coherence does not improve at all; when
	 * similarity = 1, couplet.coherence will improve to its square root;
	 */

	void merge(FAQ faq) throws Exception {
		for (QACouplet coupletNew : faq) {
			for (QACouplet coupletOld : this) {
				double similarity;
				if (coupletNew.equals(coupletOld)) {
					if (coupletNew.coherence > coupletOld.coherence) {
						coupletOld.coherence = coupletNew.coherence;
					}

					if (QACouplet.isSupervised(coupletNew.origin)) {
						coupletOld.origin = coupletNew.origin;
						coupletOld.time = coupletNew.time;
						similarity = 0;
					} else
						similarity = 1;
				} else {
					similarity = coupletOld.ans.answerSimilarity(coupletNew.ans);
				}
				coupletOld.improve_confidence(similarity);
			}
			// similarity might be negative;
			// couplet.improve_confidence(similarity);
		}

		for (QACouplet qa : faq) {
			int i = this.size() - 1;
			for (; i >= 0; --i) {
				int cmp = qa.compareTo(this.get(i));
				if (cmp == 0) {
					break;
				}
				if (cmp < 0) {
					this.add(i + 1, qa);
					break;
				}
			}
			if (i < 0) {
				this.add(0, qa);
			}
		}

		if (this.size() > 20) {
			this.removeRange(20, this.size());
		}

		this.isChanged = true;
	}

	public String toString() {
		return Utility.toString(this, "\n", null);
	}

	public QACouplet epitome() {
		QACouplet epitome = supervisedEpitome();
		if (epitome != null) {
			return epitome;
		}

		QACouplet newest = newest();
		double confidence = 0;

		for (QACouplet couplet : this) {
			double conf = couplet.coherence * couplet.efficacy(newest);
			if (conf > confidence) {
				epitome = couplet;
			}
		}
		return epitome;
	}

	public QACouplet supervisedEpitome() {
		QACouplet epitome = supervisedEpitome(Origin.SUPERVISOR_RESERVOIR);
		if (epitome != null) {
			return epitome;
		}

		epitome = supervisedEpitome(Origin.SYSTEM_RESERVOIR);
		if (epitome != null) {
			return epitome;
		}

		epitome = supervisedEpitome(Origin.INDIVIDUAL_RESERVOIR);
		if (epitome != null) {
			return epitome;
		}

		return null;
	}

	public QACouplet supervisedEpitome(Origin provenance) {
		for (QACouplet couplet : this) {
			if (couplet.origin == provenance) {
				return couplet;
			}
		}
		return null;
	}

	double similarity(QACouplet epitome) throws Exception {
		double maximum = 0;
		for (QACouplet Sentence : this) {
			double similarity = Sentence.similarity(epitome);
			if (similarity > maximum) {
				maximum = similarity;
			}
		}
		return maximum;
	}

	/**
	 * 
	 * @return the latest version of answer for this FAQ with the passage of
	 *         time;
	 */
	QACouplet newest() {
		QACouplet newest = null;
		Date when = null;
		for (QACouplet couplet : this) {
			if (when == null || couplet.time.after(when)) {
				when = couplet.time;
				newest = couplet;
			}
		}
		return newest;

	}

	/**
	 * the similarity between two knowledge points depends on the similarity
	 * between the questions and the similarity between answers. so two
	 * knowledge points with the similar questions but different answers may not
	 * be similar, and hence they may be classified as two different knowledge
	 * points. for example: 婴儿机票怎么卖？----儿童小于两岁免费，小于12周岁是成人机票的半价。
	 * 婴儿机票怎么买？----凭婴儿出生证到服务窗口去购买。 these two pairs of question and answer may be
	 * classified as two different knowledge points. Another example:
	 * 斯柯达汽车怎么卖？----斯柯达汽车市场价为10万元。 斯柯达汽车怎么买？----斯柯达汽车市场价为20万元。 these two pairs
	 * of question and answer may be classified as the same knowledge point.
	 * 
	 * 
	 * @param faq
	 * @return
	 * @throws Exception
	 */
	double similarity(FAQ faq) throws Exception {
		PriorityQueue<Double> priorityQueue = new PriorityQueue<Double>();

		for (QACouplet sentence : this)
			for (QACouplet _sentence : faq) {
				priorityQueue.add(_sentence.similarity(sentence));
			}
		int size = (int) Math.sqrt(Math.min(this.size(), faq.size()));
		if (size <= 0) {
			return 0;
		}

		double similarity = 0;
		for (int i = 0; i < size; ++i) {
			similarity += priorityQueue.poll();
		}
		return similarity / size;
	}

	public static Logger log = Logger.getLogger(FAQ.class);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
