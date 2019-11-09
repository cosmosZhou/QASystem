package com.robot.semantic.RNN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.robot.Sentence;
import com.robot.syntax.SyntacticParser;
import com.robot.syntax.SyntacticTree;
import com.robot.syntax.SyntacticTree.BinarizedTree;
import com.robot.syntax.SyntacticTree.BinarizedTree.Combination;
import com.robot.util.InstanceReader;
import com.util.Utility;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Generics;

public class RNNTopicClassifier extends RNNTraining {
	static public RNNTopicClassifier instance;
	public Map<String, RNNTopicClassifier> instMap = new HashMap<String, RNNTopicClassifier>();
	public RNNTopicClassifier parent;

	static {
		String trainDataPath = Utility.workingDirectory + "corpus/topic/";
		instance = new RNNTopicClassifier(trainDataPath);
		scheduleAtFixedRate();
	}

	public static void scheduleAtFixedRate() {

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					instance.trainingSuccessiveLayerClassifier();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		Timer timer = new Timer();
		/*
		 * schedule 和 scheduleAtFixedRate 区别： 可将schedule理解为scheduleAtFixedDelay，
		 * 两者主要区别在于delay和rate 1、schedule，如果第一次执行被延时（delay），
		 * 随后的任务执行时间将以上一次任务实际执行完成的时间为准 2、scheduleAtFixedRate，如果第一次执行被延时（delay），
		 * 随后的任务执行时间将以上一次任务开始执行的时间为准（需考虑同步）
		 * 
		 * 参数：1、任务体 2、延时时间（可以指定执行日期）3、任务执行间隔时间
		 */
		//	         timer.schedule(task, 0, 1000 * 3);
		//		Date now = new Date();
		//		now.getHours();
		//		now.getHours();
		GregorianCalendar now = new GregorianCalendar();
		int h = now.get(Calendar.HOUR_OF_DAY);
		int m = now.get(Calendar.MINUTE);
		m += h * 60;
		int s = now.get(Calendar.SECOND);
		s += m * 60;
		int ms = now.get(Calendar.MILLISECOND);
		ms += s * 1000;
		long total = 1000 * 86400;
		timer.scheduleAtFixedRate(task, total - ms, total);
		System.out.println("now is the time   : " + Utility.toString(now.getTime()));
		System.out.println("task is scheduled : " + (total - ms) * 1.0 / 1000 / 60 / 60 + " hours later.");
	}

	String trainDataPath;

	public RNNTopicClassifier(String trainDataPath) {
		this(trainDataPath, null);
	}

	public RNNTopicClassifier(String trainDataPath, RNNTopicClassifier parent) {
		if (!trainDataPath.endsWith("/")) {
			trainDataPath += "/";
		}
		this.trainDataPath = trainDataPath;
		this.parent = parent;
		modelPath = trainDataPath + "classifier.gz";
		try {
			model = RNNModel.loadSerialized(modelPath);
			model.op.trainOptions.epochs = 10;
			model.op.trainOptions.debugOutputEpochs = -1;
		} catch (Exception e) {
			//			e.printStackTrace();
			RNNOptions op = new RNNOptions();
			op.numHid = 25;
			InstanceReader instanceReader = null;
			try {
				instanceReader = new InstanceReader(trainDataPath);
			} catch (UnsupportedEncodingException | FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			op.classNames = instanceReader.classNames;
			op.numClasses = op.classNames.length;

			//			if (op.numClasses < 2) {
			//				throw new RuntimeException(trainDataPath + " has only one dimension!");
			//			}

			op.trainOptions.epochs = 10;
			op.trainOptions.debugOutputEpochs = -1;
			//			op.simplifiedModel = false;
			//			op.combineClassification = false;

			model = new RNNModel(op);
		}

		for (String className : model.op.classNames) {
			String directory = trainDataPath + className + "/";
			if (new File(directory).isDirectory()) {
				instMap.put(className, new RNNTopicClassifier(directory, this));
			}
		}
	}

	static Combination combination = new Combination() {
		@Override
		public String combine(String seg, String pos) {
			return seg + "/" + pos;
		}
	};


	public Combination combination() {
		return combination;
	}

	public HashMap<String, Integer> mapping() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < model.op.classNames.length; ++i) {
			map.put(model.op.classNames[i], i);
		}
		return map;

	}

	HashMap<String, Integer> trainingSetRatio = new HashMap<String, Integer>();

	public boolean establishTrainingCorpus() throws Exception {
		trainingTrees = new ArrayList<Tree>();
		trainingSetRatio.clear();
		ArrayList<LabeledScoredTreeNode> list = new ArrayList<LabeledScoredTreeNode>();

		HashMap<String, Integer> map = mapping();
		for (String label : map.keySet()) {
			trainingSetRatio.put(label, 0);
		}

		for (com.robot.util.Instance inst : new InstanceReader(trainDataPath)) {
			String sent = (String) inst.getData();
			String label = (String) inst.getTarget();
			if (!isLegal(label)) {
				throw new RuntimeException(label + " is not a legal label. in " + this.trainDataPath);
			}

			Sentence sentence = new Sentence(sent);
			//			System.out.println(sentence);

			BinarizedTree bTreeForTraining = sentence.tree().toBinarizedTree(combination);

			list.add(bTreeForTraining.toLabeledScoredTreeNode());

			Integer classtype = map.get(label);
			if (classtype == null) {
				return false;
			}

			bTreeForTraining.classtype = classtype;
			trainingTrees.add(bTreeForTraining.toLabeledScoredTreeNode());

			if (!inst.bDubious) {
				trainingSetRatio.put(label, trainingSetRatio.get(label) + 1);
			}
		}

		if (model.numClasses == 1)
			return true;

		model.initRandomWordVectors(list);
		return true;
	}

	public void establishTrainingCorpus(String particularLabel) throws Exception {
		trainingTrees = new ArrayList<Tree>();
		ArrayList<LabeledScoredTreeNode> list = new ArrayList<LabeledScoredTreeNode>();

		HashMap<String, Integer> map = mapping();

		for (com.robot.util.Instance inst : new InstanceReader(trainDataPath)) {
			String sent = (String) inst.getData();

			Sentence sentence = new Sentence(sent);
			//			System.out.println(sentence);

			BinarizedTree bTreeForTraining = sentence.tree().toBinarizedTree(combination);

			list.add(bTreeForTraining.toLabeledScoredTreeNode());

			if (particularLabel.equals(inst.getTarget())) {
				bTreeForTraining.classtype = map.get(inst.getTarget());
				trainingTrees.add(bTreeForTraining.toLabeledScoredTreeNode());
			}

		}

		model.initRandomWordVectors(list);
	}

	enum Drapeau {
		discard, criteria, dubious
	};

	public void testRNNModel() throws Exception {
		int err[] = new int[model.op.numClasses];
		int sum[] = new int[model.op.numClasses];
		HashMap<String, Integer> map = mapping();
		HashMap<String, ArrayList<String>> trainingSet = new HashMap<String, ArrayList<String>>();

		HashMap<String, Integer> dubiousTrainingSetRatio = new HashMap<String, Integer>();

		for (String label : map.keySet()) {
			dubiousTrainingSetRatio.put(label, 0);
		}

		for (com.robot.util.Instance inst : new InstanceReader(trainDataPath)) {
			String sent = (String) inst.getData();
			String target = (String) inst.getTarget();

			double probability[] = classifyManifold(new Sentence(sent).tree());
			int predIndex = Utility.maxIndex(probability);

			int targetIndex = map.get(target);
			++sum[targetIndex];

			if (!trainingSet.containsKey(target)) {
				trainingSet.put(target, new ArrayList<String>());
			}

			Drapeau drapeau;
			ArrayList<String> arr = trainingSet.get(target);

			if (targetIndex != predIndex) {
				++err[targetIndex];
				if (inst.bDubious) {
					if (new Random().nextBoolean()) {
						drapeau = Drapeau.dubious;
					} else {
						drapeau = Drapeau.discard;
					}
				} else {
					drapeau = Drapeau.criteria;
				}

			} else {
				if (inst.bDubious) {
					drapeau = Drapeau.dubious;
				} else {
					drapeau = Drapeau.criteria;
				}
			}

			switch (drapeau) {
			case discard:
				System.out.println(";" + sent + " will be discarded.");
				break;
			case criteria:
				arr.add(sent);
				break;
			case dubious:
				int cnt = dubiousTrainingSetRatio.get(target);
				if (cnt <= 3 * trainingSetRatio.get(target) + 100) {
					arr.add(";" + sent);
					dubiousTrainingSetRatio.put(target, ++cnt);
				}

				break;

			default:
				break;
			}
		}

		DecimalFormat df = new DecimalFormat("00.00");
		int errs = Utility.sum(err);
		int sums = Utility.sum(sum);
		for (int i = 0; i < sum.length; ++i) {
			System.out.println("correct percent for " + model.op.classNames[i] + " = " + df.format((sum[i] - err[i]) * 1.0 / sum[i] * 100) + "%");
		}

		System.out.println("correct percent = " + df.format((sums - errs) * 1.0 / sums * 100) + "%");
		System.out.println("total sentences  = " + sums);
		//		for (Map.Entry<String, ArrayList<String>> pair : trainingSet.entrySet()) {
		//			Utility.writeString(trainDataPath + pair.getKey() + ".data", pair.getValue());
		//		}
	}

	public static void readFromExcel() throws Exception {
		for (String[] arr : Utility.readFromExcel(Utility.workingDirectory + "corpus/topic/topic.xlsx", "topic", 9)) {
			System.out.println("UID = " + arr[0]);
			if (arr[0] == null) {
				continue;
			}
			System.out.println("layer1 = " + arr[1]);

			File file1 = new File(Utility.workingDirectory + "corpus/topic/" + arr[1] + "/");
			if (!file1.exists()) {
				file1.mkdirs();
			}

			System.out.println("layer2 = " + arr[2]);
			File file2 = new File(Utility.workingDirectory + "corpus/topic/" + arr[1] + "/" + arr[2] + "/");
			if (!file2.exists()) {
				file2.mkdirs();
			}

			System.out.println("layer3 = " + arr[3]);
			arr[3] = arr[3].replace('/', ',');
			System.out.println("definition = " + arr[4]);
			System.out.println("keyword1 = " + arr[5]);
			System.out.println("keyword2 = " + arr[6]);
			System.out.println("keyword3 = " + arr[7]);
			System.out.println("keyword4 = " + arr[8]);
			HashSet<String> arrStr = new HashSet<String>();
			for (int i = 5; i <= 8; ++i) {
				if (arr[i] != null)
					for (String s : arr[i].split("[；;\\s]")) {
						arrStr.add(s);
					}
			}

			arrStr.add(arr[3]);

			Utility.writeString(Utility.workingDirectory + "corpus/topic/" + arr[1] + "/" + arr[2] + "/" + arr[3] + ".data", arrStr);

			arrStr.add(arr[2]);

			File parent = new File(Utility.workingDirectory + "corpus/topic/" + arr[1] + "/" + arr[2] + ".data");
			if (parent.exists()) {
				Utility.appendString(parent.toString(), arrStr);
			} else {
				Utility.writeString(parent.toString(), arrStr);
			}

			arrStr.add(arr[1]);
			parent = new File(Utility.workingDirectory + "corpus/topic/" + arr[1] + ".data");
			if (parent.exists()) {
				Utility.appendString(parent.toString(), arrStr);
			} else {
				Utility.writeString(parent.toString(), arrStr);
			}

			System.out.println();
		}
	}

	public void trainingSuccessiveLayerClassifier() throws Exception {
		if (bReadyForTraining) {
			train();
			bReadyForTraining = false;
		}

		for (String className : this.model.op.classNames) {
			RNNTopicClassifier rNNTopicClassifier = instMap.get(className);
			if (rNNTopicClassifier != null) {
				rNNTopicClassifier.trainingSuccessiveLayerClassifier();
			}
		}
	}

	void train(String particularLabel) throws Exception {
		establishTrainingCorpus(particularLabel);
		trainingCorpus();
		testRNNModel();
		log.info("train " + this.trainDataPath + " finished!");
	}

	boolean bReadyForTraining = true;

	void train() throws Exception {
		log.info("training " + this.trainDataPath);
		if (!establishTrainingCorpus()) {
			log.info(this.trainDataPath + "classifier.gz" + "will be deleted!");
			new File(this.trainDataPath + "classifier.gz").delete();
			RNNTopicClassifier _this = new RNNTopicClassifier(trainDataPath, parent);
			if (this.parent != null) {
				for (Map.Entry<String, RNNTopicClassifier> pair : parent.instMap.entrySet()) {
					if (pair.getValue().trainDataPath.equals(this.trainDataPath)) {
						parent.instMap.put(pair.getKey(), _this);
						break;
					}
				}
			} else {
				instance = _this;
			}

			if (_this.model.numClasses == 1)
				return;
			_this.establishTrainingCorpus();

			_this.trainingCorpus();
			//		clearInstance(trainDataPath);
			_this.testRNNModel();
		} else {
			if (model.numClasses == 1)
				return;
			trainingCorpus();
			//		clearInstance(trainDataPath);
			testRNNModel();
		}

		log.info("train " + this.trainDataPath + " finished!");
	}

	public String[] classify(String comment) throws Exception {
		ArrayList<String> labels = new ArrayList<String>();
		Sentence[] sent = new Sentence(comment).splitSentence();

		RNNTopicClassifier rnnTopicClassifier = this;

		do {
			//			log.info("trainDataPath = " + rnnTopicClassifier.trainDataPath);
			String label;

			if (rnnTopicClassifier.model.numClasses > 1) {
				double res[] = rnnTopicClassifier.classifyManifold(sent);
				label = rnnTopicClassifier.model.op.classNames[Utility.maxIndex(res)];
			} else {
				label = rnnTopicClassifier.model.op.classNames[0];
			}

			labels.add(label);
			rnnTopicClassifier = rnnTopicClassifier.instMap.get(label);
		} while (rnnTopicClassifier != null);

		log.info("labels = " + Utility.toString(labels, ";"));
		return Utility.toArrayString(labels);
	}

	static class Hierarchy implements Comparable<Hierarchy> {
		Hierarchy(String label, double score, String filePath, Hierarchy parent) {
			this.label = label;
			this.score = score;
			this.filePath = filePath;
			if (new File(filePath).isDirectory()) {
				if (parent == null) {
					this.classifier = RNNTopicClassifier.instance;
				} else {
					this.classifier = parent.classifier.instMap.get(label);
				}
			}
			this.parent = parent;
		}

		String[] labels() {
			ArrayList<String> arr = new ArrayList<String>();
			Hierarchy node = this;
			while (node.label != null) {
				arr.add(node.label);
				node = node.parent;
			}
			String[] str = new String[arr.size()];
			for (int i = 0; i < str.length; i++) {
				str[i] = arr.get(str.length - 1 - i);
			}
			return str;
		}

		int numClasses() {
			return classifier.model.numClasses;
		}

		void initializeKinder(Sentence sent[]) throws Exception {
			double res[] = classifier.classifyManifold(sent);
			Comparator<Integer> pred = new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {
					// TODO Auto-generated method stub
					return Double.compare(res[o1], res[o2]);
				}
			};

			Utility.PriorityQueue<Integer> pq = new Utility.PriorityQueue<Integer>(pred);
			int i = 0;
			for (; i < 3 && i < res.length; ++i) {
				pq.add(i);
			}

			for (; i < res.length; ++i) {
				pq.add(i);
				if (3 < pq.size())
					pq.poll(3);
			}

			int index0 = pq.poll();
			int index1 = pq.poll();
			kinder = new Hierarchy[2];
			String label0 = classifier.model.op.classNames[index0];
			String label1 = classifier.model.op.classNames[index1];
			kinder[0] = new Hierarchy(label0, score * res[index0], filePath + label0 + "/", this);
			kinder[1] = new Hierarchy(label1, score * res[index1], filePath + label1 + "/", this);
		}

		void initializeKinder() {
			kinder = new Hierarchy[1];
			String label = classifier.model.op.classNames[0];
			kinder[0] = new Hierarchy(label, score, filePath + label + "/", this);
		}

		void traverse(Collection<Hierarchy> c) {
			if (this.classifier == null) {
				c.add(this);
			}

			if (kinder == null) {
				return;
			}

			for (Hierarchy k : kinder) {
				k.traverse(c);
			}
		}

		String label;
		String filePath;
		RNNTopicClassifier classifier;
		double score;
		Hierarchy parent;
		Hierarchy[] kinder;

		@Override
		public int compareTo(Hierarchy o) {
			return Double.compare(score, o.score);
		}
	}

	public String[][] classify(String comment, int nSalient) throws Exception {

		Sentence[] sent = new Sentence(comment).splitSentence();
		Hierarchy root = new Hierarchy(null, 1, trainDataPath, null);
		Stack<Hierarchy> stack = new Stack<Hierarchy>();
		stack.add(root);

		while (!stack.isEmpty()) {
			Hierarchy hierarchy = stack.pop();
			log.info("trainDataPath = " + hierarchy.filePath);

			if (hierarchy.numClasses() > 1) {

				hierarchy.initializeKinder(sent);
				if (hierarchy.kinder[0].classifier != null) {
					stack.push(hierarchy.kinder[0]);
				}
				if (hierarchy.kinder[1].classifier != null) {
					stack.push(hierarchy.kinder[1]);
				}
			} else {
				hierarchy.initializeKinder();
				if (hierarchy.kinder[0].classifier != null) {
					stack.push(hierarchy.kinder[0]);
				}
			}
		}

		Utility.PriorityQueue<Hierarchy> pq = new Utility.PriorityQueue<Hierarchy>();
		root.traverse(pq);
		nSalient = Math.min(nSalient, pq.size());
		String[][] arr = new String[nSalient][];
		for (int i = 0; i < nSalient; ++i) {
			arr[i] = pq.poll().labels();
		}

		return arr;
	}

	public void insertLabel(String... label) throws Exception {
		String trainDataPath = this.trainDataPath;
		//		String trainDataPath = this.trainDataPath;

		RNNTopicClassifier classifier = this;
		if (classifier.instMap == null) {

		} else if (classifier.instMap.containsKey(label[0])) {

		} else {

		}
		int i = 0;
		for (; i < label.length - 1; i++) {
			trainDataPath += label[i] + "/";
		}

		String lab = label[i];
		String classNames[] = new InstanceReader(trainDataPath).classNames;

		if (Utility.indexOf(classNames, lab) < 0) {
			throw new RuntimeException(Utility.toString(label) + "does not exist!");
		}

		if (Utility.indexOf(classNames, lab) < 0) {
			throw new RuntimeException(Utility.toString(label) + "does not exist!");
		}

		File file = new File(trainDataPath + lab + ".data");
		file.createNewFile();
		if (classNames.length != 1) {
			file = new File(trainDataPath + "classifier.gz");
			file.delete();

			RNNTopicClassifier rnnTopicClassifier = this;

			rnnTopicClassifier.bReadyForTraining = true;
		}
	}

	public void deleteLabel(String... label) throws Exception {
		String trainDataPath = this.trainDataPath;
		//		String trainDataPath = this.trainDataPath;

		int i = 0;
		for (; i < label.length - 1; i++) {
			trainDataPath += label[i] + "/";
		}

		String lab = label[i];
		String classNames[] = new InstanceReader(trainDataPath).classNames;

		if (Utility.indexOf(classNames, lab) < 0) {
			throw new RuntimeException(Utility.toString(label) + "does not exist!");
		}

		if (Utility.indexOf(classNames, lab) < 0) {
			throw new RuntimeException(Utility.toString(label) + "does not exist!");
		}

		File file = new File(trainDataPath + lab + ".data");
		if (file.exists()) {
			file.delete();
			file = new File(trainDataPath + "classifier.gz");
			file.delete();

			RNNTopicClassifier rnnTopicClassifier = this;

			rnnTopicClassifier.bReadyForTraining = true;

		} else {
			file = new File(trainDataPath + lab);
			if (file.isDirectory()) {
				Utility.delFolder(file.toString());
			}
		}
	}

	public void deleteCriteria(String comment, String... label) throws Exception {

		RNNTopicClassifier rNNTopicClassifier = this;
		int i = 0;
		for (; i < label.length - 1; i++) {
			if (!rNNTopicClassifier.instMap.containsKey(label[i])) {
				throw new RuntimeException(rNNTopicClassifier.trainDataPath + " does not contain label " + label[i]);
			}

			rNNTopicClassifier = rNNTopicClassifier.instMap.get(label[i]);
		}

		if (Utility.removeString(rNNTopicClassifier.trainDataPath + label[i] + ".data", comment) > 0) {
			do {
				rNNTopicClassifier.bReadyForTraining = true;
				rNNTopicClassifier = rNNTopicClassifier.parent;
			} while (rNNTopicClassifier != null);
		}
	}

	//the name of a winows file can't contain,  \/:*?\"<>|
	static boolean isLegal(String label) {
		return !label.matches(".*[\\\\/:*?\"<>|,;]+.*");
	}

	public void insert(String comment, boolean bCriteria, String... label) throws Exception {
		Sentence[] sent = new Sentence(comment).splitSentence();

		Comparator<Sentence> pred = new Comparator<Sentence>() {
			@Override
			public int compare(Sentence o1, Sentence o2) {
				return Double.compare(o1.entropyInformation(), o2.entropyInformation());
			}

		};

		Utility.PriorityQueue<Sentence> priorityQueue = new Utility.PriorityQueue<Sentence>(pred);

		for (Sentence s : sent) {
			priorityQueue.add(s);
		}

		ArrayList<String> arr = new ArrayList<String>();
		for (int i = 0; i <= sent.length / 2; ++i) {
			if (sent.length / 2 >= sent.length)
				throw new RuntimeException("sent.length / 2 >= sent.length, sent.length = " + sent.length);
			String sentence = priorityQueue.poll().sentence;
			if (bCriteria)
				arr.add(sentence);
			else
				arr.add(";" + sentence);

			log.info("key phrase = " + sentence);
		}

		int i = 0;
		RNNTopicClassifier classifier = this;
		for (; i < label.length - 1; i++) {
			log.info(classifier.trainDataPath + " will be trained!");
			classifier.bReadyForTraining = true;
			if (!classifier.instMap.containsKey(label[i])) {
				if (isLegal(label[i])) {
					String folder = classifier.trainDataPath + label[i];
					new File(folder).mkdirs();
					classifier.instMap.put(label[i], new RNNTopicClassifier(folder, classifier));
				} else {
					throw new RuntimeException(label[i] + " is not a legal label.");
				}
			}
			classifier = classifier.instMap.get(label[i]);
		}

		String lab = label[i];

		if (!isLegal(label[i])) {
			throw new RuntimeException(label[i] + " is not a legal label.");
		}

		String trainingFile;
		String trainingFolder = classifier.trainDataPath + lab;
		if (new File(trainingFolder).isDirectory()) {
			log.info(classifier.trainDataPath + " will be trained!");
			classifier.bReadyForTraining = true;
			classifier = classifier.instMap.get(lab);

			String lab1;
			if (classifier.model.numClasses > 1) {
				double res[] = classifier.classifyManifold(sent);
				lab1 = classifier.model.op.classNames[Utility.maxIndex(res)];
			} else {
				lab1 = classifier.model.op.classNames[0];
			}

			trainingFile = trainingFolder + "/" + lab1 + ".data";
		} else {
			trainingFile = trainingFolder + ".data";
			File file = new File(trainingFile);
			if (!file.exists()) {
				file.createNewFile();
			}
		}

		Utility.appendString(trainingFile, Utility.toString(arr, Utility.lineSeparator));

		log.info(classifier.trainDataPath + " will be trained!");
		classifier.bReadyForTraining = true;
		log.info(trainingFile + " is modified!");
	}

	static void trainingFromExcel() throws Exception {
		for (String[] arr : Utility.readFromExcel(Utility.workingDirectory + "corpus/topic/cases.xlsx", "case", 2)) {

			String comment = arr[0];
			//			String[] res = instance.classify(comment);
			//			log.info(Utility.toString(res));

			instance.insert(comment, true, arr[1].split(";"));

			//			res = instance.classify(comment);
			//			log.info(Utility.toString(res));

		}
	}

	public static void main(String[] args) throws Exception {
		instance.trainingSuccessiveLayerClassifier();
		//		trainingFromExcel();
	}

	//	打开之后是有异味的，但比较小，凑近鼻子才可以闻到，薄是薄，但确不是很软，有点硬，尿后，里外都不会有湿的感觉，但屎后，如果没及时更换，就会在大腿根处发生侧漏的现象，但漏的也不太多，总体满意，尤其双十一，超级划算……
	static Logger log = Logger.getLogger(RNNTopicClassifier.class);
}
