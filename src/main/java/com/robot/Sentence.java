package com.robot;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.flexible.core.parser.SyntaxParser;

import com.robot.semantic.EditDistanceLevenshtein;
import com.robot.semantic.RNN.RNNParaphrase;
import com.robot.semantic.RNN.RNNPhaticsClassifier;
import com.robot.semantic.RNN.RNNQAClassifier;
import com.util.Utility;
import com.util.Utility.Printer;
import com.robot.syntax.POSTagger;
import com.robot.syntax.SyntacticParser;
import com.robot.syntax.SyntacticTree;
import com.robot.semantic.Thesaurus;

import com.robot.syntax.AnomalyInspecter;
import com.robot.syntax.CWSTagger;
import com.robot.syntax.Constituent;

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
		Printer printer = new Printer(Utility.workingDirectory + "/corpus/debug.txt");
		int error = 0;
		String seg[] = null;
		String pos[] = null;
		for (String inst : new Utility.Text(Utility.workingDirectory + "CORPUS/pos.txt")) {
			if (inst.startsWith(";")) {
				continue;
			}
			if (seg == null) {
				seg = inst.split("\\s+");
				continue;
			}
			if (pos == null) {
				pos = inst.split("\\s+");

				SyntacticTree tree = SyntacticParser.instance.parse(seg, pos);

				//				AnomalyInspecter.Filter filter = AnomalyInspecter.containsIrregulation(tree);
				//				if (filter != null) {
				////					System.out.println(tree);
				////					System.out.println("Anomaly Inspected : " + filter.regulation);
				//					tree = SyntacticParser.instance.parseWithAdjustment(seg, pos);
				//				}
				//
				if (tree != null) {
					//					System.out.println(tree);
					System.out.println(tree.toStringNonHierarchical());
				} else {
					error++;
				}
				seg = null;
				pos = null;
			}
		}

		for (String str : new Utility.Text(Utility.workingDirectory + "CORPUS/seg.txt")) {
			seg = CWSTagger.convertToSegmentation(str);
			pos = POSTagger.instance.tag(seg);
			SyntacticTree tree = SyntacticParser.instance.parse(seg, pos);

			if (tree.containsIrregulation()) {
				tree = SyntacticParser.instance.parseWithAdjustment(seg, pos);
			}

			if (tree != null) {
				//				System.out.println(tree);
				System.out.println(tree.toStringNonHierarchical());
			} else {
				error++;
			}
		}

		//		System.out.println("error = " + error);
		printer.close();
	}

	public String[] focus() {
		ArrayList<String> arr = new ArrayList<String>();

		for (int i = 0; i < seg.length; ++i) {
			if (pos[i].equals("QUE")) {
				arr.add(seg[i]);
			}
		}

		return arr.toArray(new String[arr.size()]);
	}

	public String[] interrogative() {
		ArrayList<String> arr = new ArrayList<String>();

		for (int i = 0; i < seg.length; ++i) {
			if (pos[i].equals("QUE")) {
				arr.add(seg[i]);
			}
		}

		return arr.toArray(new String[arr.size()]);
	}

	public String[] subject() {
		ArrayList<String> arr = new ArrayList<String>();
		for (SyntacticTree tree : tree.leftChildren) {
			if (tree.dep.equals("suj")) {
				// arr.addAll(tree.getWordSet());
			}
		}
		for (SyntacticTree tree : tree.rightChildren) {
			if (tree.dep.equals("suj")) {
				// arr.addAll(tree.getWordSet());
			}
		}
		return arr.toArray(new String[arr.size()]);
	}

	public String[] object() {
		ArrayList<String> arr = new ArrayList<String>();
		for (SyntacticTree tree : tree.leftChildren) {
			if (tree.dep.equals("obj")) {
				// arr.addAll(tree.getWordSet());
			}
		}
		for (SyntacticTree tree : tree.rightChildren) {
			if (tree.dep.equals("obj")) {
				// arr.addAll(tree.getWordSet());
			}
		}
		return arr.toArray(new String[arr.size()]);
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

	public Sentence(String seg[]) {
		this.seg = seg;
		this.sentence = Utility.convertSegmentationToOriginal(seg);
	}

	public Sentence(SyntacticTree tree) throws Exception {
		this.seg = tree.getLEX();
		this.pos = tree.getPOS();
		this.dep = tree.getSyntacticTree();
		this.tree = tree;
		this.sentence = Utility.convertSegmentationToOriginal(seg);
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

	HashMap<String, Double> entropyMap() {
		synchronized (this) {
			if (entropyMap == null) {
				String[] seg = seg();
				String[] pos = pos();
				HashMap<String, Double> posMap = new HashMap<String, Double>();
				//			log.info("pos = " + Arrays.toString(pos));
				//			log.info("seg = " + Arrays.toString(seg));
				//			log.info("dep = " + Arrays.toString(dep));
				//			log.info("posWeight = " + posWeight);
				for (int i = 0; i < seg.length; ++i) {
					Double obj = Constituent.entropyMap.get(pos[i]);
					if (obj == null) {
						throw new RuntimeException(pos[i] + " does not exist int posWeight.txt");
					}

					double weight = obj;
					if (!posMap.containsKey(seg[i]) || posMap.get(seg[i]) < weight)
						posMap.put(seg[i], weight);
				}

				//				HashMap<String, Double> depMap = new HashMap<String, Double>();
				//				for (int i = 0; i < seg.length; ++i) {
				//					Double obj;
				//					if (dep[i].dep == null)
				//						obj = posWeight.get("root");
				//					else
				//						obj = posWeight.get(dep[i].dep);
				//					if (obj == null) {
				//						throw new Exception(dep[i] + " does not exist int posWeight.txt");
				//					}
				//
				//					double weight = obj;
				//					if (!depMap.containsKey(seg[i]) || depMap.get(seg[i]) < weight)
				//						depMap.put(seg[i], weight);
				//				}

				entropyMap = posMap;
				//				for (Entry<String, Double> entry : depMap.entrySet()) {
				//					entropyMap.put(entry.getKey(), entry.getValue() + entropyMap.get(entry.getKey()));
				//				}
			}
			return entropyMap;
		}
	}

	//	int id = -1;
	public String sentence;
	Protagonist protagonist;
	String speaker;

	/**
	 * indicate whether it is a question or an answer;
	 */
	transient QATYPE qatype;
	//	int anomaly;
	/**
	 * confidence of judgment of QA type;
	 */
	transient double confidence;
	transient SyntacticTree tree;
	transient SyntacticTree[] dep;
	transient String[] seg;
	transient String[] pos;
	transient HashMap<String, Double> entropyMap;

	SyntacticTree getOriginalMathExp() throws Exception {
		if (this.qatype != QATYPE.QUERY)
			return null;
		SyntacticTree subject = this.tree.getSubject();
		if (subject != null && subject.pos.equals("O")) {
			return subject;
		}

		SyntacticTree object = this.tree.getObject();
		if (object != null && object.pos.equals("O")) {
			return object;
		}

		return null;
	}

	public QATYPE qatype() throws Exception {
		if (qatype == null) {
			double score = RNNPhaticsClassifier.instance.classify(this.tree());
			if (score < RNNPhaticsClassifier.threshold) {
				qatype = QATYPE.NEUTRAL;
				this.confidence = 1 - score;
			} else {//score >= RNNPhaticsClassifier.threshold
				this.confidence = RNNQAClassifier.instance.classify(tree());
				if (confidence > RNNQAClassifier.threshold) {
					qatype = QATYPE.QUERY;
					confidence = Math.sqrt(score * confidence);
				} else {//if (confidence <= 0.5) {
					qatype = QATYPE.REPLY;
					confidence = Math.sqrt(score * (1 - confidence));
				}
			}
		}
		return qatype;
	}

	//	public String anomaly() throws Exception {
	//		switch (this.anomaly) {
	//		case INDEX_INTEGRITY:
	//			return "INTEGRITY";
	//		case INDEX_INCOMPLETE:
	//			return "INCOMPLETE";
	//		case INDEX_ANAPHORA:
	//			return "ANAPHORA";
	//		case INDEX_MULTIPLE:
	//			return "MULTIPLE";
	//		default:
	//			throw new Exception("unknown index for anomaly.");
	//		}
	//	}

	public String toString() {
		return sentence;
	}

	public int tokenLength() {
		String[] pos = pos();
		int cnt = 0;
		for (String p : pos) {
			switch (p) {
			case "IJ":
			case "PU":
				break;
			default:
				++cnt;
				break;
			}
		}
		return cnt;
	}

	public String[] seg() {
		synchronized (this) {
			if (seg == null) {
				int length = sentence.length();
				for (; length > 0; --length) {
					if (Utility.endOfSentencePunctuation.indexOf(sentence.charAt(length - 1)) < 0) {
						break;
					}
				}

				if (length == 0) {
					seg = new String[] { sentence };
				} else if (length < sentence.length()) {
					seg = CWSTagger.instance.tag(sentence.substring(0, length));
					seg = Arrays.copyOf(seg, seg.length + 1);
					seg[seg.length - 1] = sentence.substring(length);
				} else {
					seg = CWSTagger.instance.tag(sentence);
				}

			}
			return seg;
		}
	}

	public String[] pos() {
		synchronized (this) {
			if (pos == null) {
				int length = seg().length;
				for (; length > 0; --length) {
					if (Utility.endOfSentencePunctuation.indexOf(seg[length - 1].charAt(0)) < 0) {
						break;
					}
				}

				if (length < seg.length) {
					pos = POSTagger.instance.tag(Arrays.copyOf(seg, length));
					pos = Arrays.copyOf(pos, seg.length);
					for (; length < seg.length; ++length) {
						pos[length] = "PU";
					}

				} else {
					pos = POSTagger.instance.tag(seg);
				}

			}
			return pos;
		}
	}

	public SyntacticTree tree() throws Exception {
		synchronized (this) {
			if (tree == null) {
				String[] seg = seg();
				String[] pos = pos();

				boolean[] indicator = new boolean[seg.length];
				for (int j = 0; j < seg.length; ++j) {
					switch (seg[j]) {
					case "您好":
					case "你好":
					case "请问":
						if (pos[j].equals("IJ")) {
							indicator[j] = true;
							break;
						}
					}
				}

				for (int j = 0; j < seg.length; ++j) {
					switch (pos[j]) {
					case "IJ":
					case "PU":
						indicator[j] = true;
						continue;
					default:
						break;
					}
					break;
				}

				for (int j = pos.length - 1; j >= 0; --j) {
					if (!pos[j].equals("PU")) {
						break;
					}
					switch (seg[j]) {
					case "\"":
					case "\'":
					case "”":
					case "“":
					case "’":
					case "‘":
						break;
					default:
						indicator[j] = true;
						continue;
					}
					break;
				}
				int size = 0;
				for (int i = 0; i < indicator.length; i++) {
					if (indicator[i])
						++size;
				}

				if (size == 0 || indicator.length == size) {
					tree = SyntacticParser.instance.parse(seg, pos);
				} else {
					size = indicator.length - size;
					String segArr[] = new String[size];
					//					String posArr[] = new String[size];
					int index = 0;
					for (int i = 0; i < indicator.length; i++) {
						if (!indicator[i]) {
							segArr[index] = seg[i];
							//							posArr[index] = pos[i];
							++index;
						}
					}
					String posArr[] = POSTagger.instance.tag(segArr);
					tree = SyntacticParser.instance.parse(segArr, posArr);

					for (int i = 0; i < indicator.length; i++) {
						if (!indicator[i])
							break;
						if (seg[i].equals("请问")) {
							tree.preppend("请问", "IJ", "ij");
							segArr = Utility.copier("请问", segArr);
							posArr = Utility.copier("IJ", posArr);
							break;
						}
					}
					for (int i = indicator.length - 1; i >= 0; --i) {
						if (!indicator[i])
							break;
						if (seg[i].matches("[?？]+")) {
							tree.append("?", "PU", "pu");
							segArr = Utility.copier(segArr, "?");
							posArr = Utility.copier(posArr, "PU");
						}
					}

					this.seg = segArr;
					this.pos = posArr;
				}
			}
			return tree;
		}
	}

	public SyntacticTree[] dep() throws Exception {
		synchronized (this) {
			if (dep == null) {
				dep = tree().getSyntacticTree();
			}
			return dep;
		}
	}

	double lengthSimilarity(Sentence sentence) {
		int tokenLength = tokenLength();
		int _tokenLength = sentence.tokenLength();
		if (tokenLength + _tokenLength == 0)
			return 1;
		return 1 - Math.abs(tokenLength - _tokenLength) * 1.0 / (tokenLength + _tokenLength);
	}

	double asymmetricInformation(Sentence sentence) throws Exception {
		double mutualInformation = 0;
		for (Entry<String, Double> entry : entropyMap().entrySet()) {
			double maxSimilarity = -1;
			double information = -1;
			for (Entry<String, Double> entry1 : sentence.entropyMap().entrySet()) {
				double similarity = Thesaurus.instance.similarity(entry.getKey(), entry1.getKey());
				if (similarity > maxSimilarity) {
					maxSimilarity = similarity;
					information = entry1.getValue();
				}
			}
			information = Math.min(entry.getValue(), information) * maxSimilarity * maxSimilarity;
			mutualInformation += information;
		}
		return mutualInformation;
	}

	double entropyInformation = Double.NaN;

	public double entropyInformation() {
		if (entropyInformation == entropyInformation) {
			return entropyInformation;
		}

		entropyInformation = 0;
		for (Entry<String, Double> entry1 : entropyMap().entrySet()) {
			entropyInformation += entry1.getValue();
		}

		return entropyInformation;
	}

	double mutualInformation(Sentence sentence) throws Exception {
		return (asymmetricInformation(sentence) + sentence.asymmetricInformation(this)) / 2;
	}

	// |A ∩ B|/|A ∪ B|
	double jaccardSimilarity(Sentence sentence) throws Exception {
		double com = this.mutualInformation(sentence);
		return com / (entropyInformation() + sentence.entropyInformation() - com);
	}

	// double norm() {
	// double norm = 0;
	// for (Entry<String, Double> entry : this.semanticVector.entrySet()) {
	// norm += entry.getValue() * entry.getValue();
	// }
	// return norm;
	// }

	// double cosineSimilarity(Sentence sentence) {
	// double similarity = 0;
	// for (Entry<String, Double> entry : semanticVector.entrySet()) {
	// if (!sentence.semanticVector.containsKey(entry.getKey()))
	// continue;
	// similarity += sentence.semanticVector.get(entry.getKey()) *
	// entry.getValue();
	// }
	//
	// return similarity;
	// }

	/**
	 * return the Levenshtein Distance between the two sentences;
	 * 
	 * @param sentence
	 * @return
	 */
	public double LevenshteinDistance(Sentence sentence) {
		return EditDistanceLevenshtein.instance.sim(this.sentence.replaceAll("[\\pP\\pS\\s]", ""), sentence.sentence.replaceAll("[\\pP\\pS\\s]", ""));
	}

	/**
	 * the semantic similarity of two sentences can be negative;
	 * 
	 * @param sentence
	 * @return
	 * @throws Exception
	 */
	public double morphologicalSimilarity(Sentence sentence) throws Exception {
		double lengthSimilarity = lengthSimilarity(sentence);
		double levenshteinDistance = LevenshteinDistance(sentence);
		double jaccardSimilarity = jaccardSimilarity(sentence);
		return lengthSimilarity * 0.05 + levenshteinDistance * 0.45 + jaccardSimilarity * 0.5;
	}

	public double questionSimilarity(Sentence sentence) throws Exception {
		double semanticSimilarity;
		try {
			semanticSimilarity = RNNParaphrase.instance.similarity(this, sentence);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			semanticSimilarity = 0.1;
		}

		double morphologicalSimilarity = morphologicalSimilarity(sentence);
		return Math.sqrt(0.5 * morphologicalSimilarity * morphologicalSimilarity + 0.5 * semanticSimilarity * semanticSimilarity);
		//		return morphologicalSimilarity;
	}

	public double answerSimilarity(Sentence sentence) throws Exception {
		double morphologicalSimilarity = morphologicalSimilarity(sentence);
		//		double semanticSimilarity = declarativeSemanticSimilarity(sentence);
		//		if (semanticSimilarity > morphologicalSimilarity)
		//			return semanticSimilarity;
		return morphologicalSimilarity;

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

	static int anomalyType(String[][] morpheme, SyntacticTree featureanomaly) {
		if (morpheme[0].length == 1) {
			return INDEX_INCOMPLETE;
		}
		if (featureanomaly == null) {
			return INDEX_INCOMPLETE;
		}
		if (featureanomaly.match("主语").size() == 0) {
			return INDEX_INCOMPLETE;
		}
		return INDEX_INTEGRITY;
	}

	private static Logger log = Logger.getLogger(Sentence.class);

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

	boolean isIncompleteQuestion() {
		String[] pos = pos();
		int realWordCnt = 0;
		for (int i = 0; i < pos.length; ++i) {
			switch (pos[i]) {
			case "PU":
			case "IJ":
			case "AS":
				continue;
			}
			++realWordCnt;
		}

		if (realWordCnt > 2)
			return false;
		return false;
	}

	boolean topGeneration(int j) {
		return dep[j].parent == null || dep[j].parent.parent == null;
	}

	boolean buriedGeneration(int j) {
		return dep[j].parent != null && dep[j].parent.parent != null;
	}

	void addFeature(HashSet<String> set, int i, int... arr) {
		String featurePos = seg[i] + " " + pos[i];
		String featurePosPos = pos[i];
		for (int index : arr) {
			if (i + index >= 0 && i + index < pos.length) {
				featurePos += "|" + index + "=" + pos[i + index];
				featurePosPos += "|" + index + "=" + pos[i + index];
			}
		}
		set.add(featurePos);
		set.add(featurePosPos);
	}

	void addFeatureSeg(HashSet<String> set, int i, int... arr) {
		String featureSeg = seg[i];

		for (int index : arr) {
			if (i + index > 0 && i + index < seg.length) {
				featureSeg += "|" + index + "=" + seg[i + index];
			}
		}
		set.add(featureSeg);
	}

	void addFeaturePos(HashSet<String> set, int i, int... arr) {
		String featureSeg = pos[i];

		for (int index : arr) {
			if (i + index > 0 && i + index < pos.length) {
				featureSeg += "|" + index + "=" + pos[i + index];
			}
		}
		set.add(featureSeg);
	}

	static String modalVerb[] = { "want", "need", "must", "need", "will", "should", "could" };

	String modalVerb(int j) {
		for (String md : modalVerb) {
			if (Thesaurus.instance.synonymous(seg[j], md)) {
				return md;
			}
		}
		return null;
	}

	//	we should determine the parents of the interrogative, whether there are multiple verbs above.
	boolean isInterrogative(int j) {
		SyntacticTree que = dep[j];
		SyntacticTree it = que;
		while (it != null) {
			it = it.parent;
			if (it != null && it.pos.equals("CS") && (Thesaurus.instance.equivalent(it.seg, "however") || Thesaurus.instance.equivalent(it.seg, "if"))) {
				return false;
			}
		}
		it = que;
		ArrayList<SyntacticTree> posArr = new ArrayList<SyntacticTree>();
		while (it != null) {
			it = it.parent;
			if (it != null) {
				switch (it.pos) {
				case "VT":
				case "VI":
				case "VC":
				case "VA":
					posArr.add(it);
				}
			}
		}
		switch (posArr.size()) {
		case 0:
			return true;
		case 1:
			SyntacticTree verb = posArr.get(0);
			//网上查到多少就多少的
			switch (verb.seg) {
			case "就是":
				if (que.pos.equals("MD")) {
					return true;
				}
				if (que.dep.equals("adj"))
					return true;
			case "就":
				return false;
			}
			return true;
		case 2:
			verb = posArr.get(0);
			switch (verb.pos) {
			case "VC":
				if (Thesaurus.instance.synonymous(posArr.get(1).seg, "tell"))
					return true;
				//这个是按客票销售来开放舱位的，网上查到多少就多少的，网上的价格是已优惠的
				switch (verb.seg) {
				case "就是":
				case "就":
					return false;
				}
				//是不是你们公司把我的资料泄露了
				if (verb.dep.equals("adv"))
					return true;
				//是谁帮他们办理购买的机票
				if (verb.seg.equals("是")) {
					if (verb.dep.equals("obj")) {//但是我不知道是什么型号
						return false;
					}
					if (verb.leftChildren.isEmpty()) {
						return true;
					}
				}
				return false;
			case "VI":
			case "VT":
			case "VA":
				switch (verb.dep) {
				case "de":
					switch (verb.parent.dep) {
					case "obj":
					case "suj":
						return false;
					}
					return true;
				case "obj":
				case "suj":
					return false;
				case "va":
					switch (verb.parent.seg) {
					case "是":
						//那取消了航空公司是怎么安排呢
						return true;
					default:
						return false;
					}
				case "JJ"://具体代金劵发放多少张这个无法确认
					switch (verb.parent.dep) {
					case "obj":
					case "suj":
						return false;
					}
				}

				return true;
			}
		default:
			return false;
		}
	}

	boolean isPredicativeInterrogativeStruct(int j) {
		if (dep[j].dep == null || !dep[j].dep.equals("adj") || j == 0 || dep[j - 1].parent != dep[j].parent)
			return false;
		return dep[j - 1].dep.equals("suj");
	}

	enum InterrogativeVerb {
		provide, tell, ask, query, know, send, business;

		static InterrogativeVerb construct(String seg) {
			for (InterrogativeVerb verb : InterrogativeVerb.values()) {
				if (Thesaurus.instance.synonymous(seg, verb.toString())) {
					return verb;

				}
			}
			return null;
		}
	}

	enum AuxiliaryVerb {
		want, please, could, help;
		static AuxiliaryVerb construct(String seg) {
			for (AuxiliaryVerb verb : AuxiliaryVerb.values()) {
				if (Thesaurus.instance.synonymous(seg, verb.toString())) {
					return verb;

				}
			}
			return null;
		}

	}

	public boolean interrogativeExtractionRNN(int j) throws Exception {
		double probability = RNNQAClassifier.instance.classify(dep[j]);
		return probability > 0.5;
	}

	public Sentence[] splitSentence() {
		ArrayList<String> sent = new ArrayList<String>();
		ArrayList<Sentence> paragraph = new ArrayList<Sentence>();
		for (String lexeme : this.seg()) {
			sent.add(lexeme);
			switch (lexeme.charAt(0)) {
			case '.':
			case '。':

			case ';':
			case '；':

			case '!':
			case '！':

			case '?':
			case '？':

			case '\r':
			case '\n':
				paragraph.add(new Sentence(Utility.toArrayString(sent)));
				sent.clear();
				break;
			default:

			}
		}

		if (!sent.isEmpty()) {
			paragraph.add(new Sentence(Utility.toArrayString(sent)));
			sent.clear();
		}
		return paragraph.toArray(new Sentence[paragraph.size()]);
	}
}
