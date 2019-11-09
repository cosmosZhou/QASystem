package com.robot.syntax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.util.Utility;
import com.util.Utility.Printer;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.maxent.CGRunner;
import edu.stanford.nlp.maxent.Problem;
import edu.stanford.nlp.tagger.maxent.AmbiguityClasses;
import edu.stanford.nlp.tagger.maxent.Dictionary;
import edu.stanford.nlp.tagger.maxent.Extractor;
import edu.stanford.nlp.tagger.maxent.ExtractorFrames;
import edu.stanford.nlp.tagger.maxent.Extractors;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TTags;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;
import edu.stanford.nlp.tagger.maxent.TaggerExperiments;
import edu.stanford.nlp.tagger.maxent.TaggerFeatures;
import edu.stanford.nlp.util.ReflectionLoading;
import edu.stanford.nlp.util.Timing;

public class POSTagger extends MaxentTagger {
	public static Logger log = Logger.getLogger(POSTagger.class);

	void trainAndSaveModel(TaggerConfig config) throws IOException {
		this.dict.dict.clear();
		TaggerExperiments samples = new TaggerExperiments(config, this, true);

		//				log.info("samples " + samples.toString());

		TaggerFeatures feats = samples.getTaggerFeatures();
		//				log.info("feats " + feats.toString());

		byte[][] fnumArr = samples.getFnumArr();
		//		log.info("fnumArr " + Utility.toString(fnumArr));

		//		log.info("Samples from " + config.getFile());
		//		log.info("Number of features: " + feats.size());
		log.info("Tag set: " + tags.tagSet());
		Problem p = new Problem(samples, feats);
		//		prob = new LambdaSolveTagger(p, 0.0001, fnumArr);
		prob.initialize(p, 0.0001, fnumArr);
		CGRunner runner = new CGRunner(prob, config.getModel(), config.getSigmaSquared());
		//		prob.lambda = new double[prob.lambda.length];

		//		runner.solveCG(prob.lambda);
		runner.solveQN(prob.lambda);
		//						runner.solveOWLQN2(config.getRegL1(), prob.lambda);

		//				runner.solveL1(config.getRegL1());
		//				prob.improvedIterative(config.getIterations());
		for (int i = 0; i < prob.lambda.length; ++i) {
			if (Double.isNaN(prob.lambda[i]) || prob.lambda[i] != prob.lambda[i]) {
				throw new RuntimeException("Double.isNaN(prob.lambda[i]) || prob.lambda[i] != prob.lambda[i]");
			}
		}

		if (prob.checkCorrectness()) {
			log.info("Model is correct [empirical expec = model expec]");
		} else {
			log.info("Model is not correct");
			throw new RuntimeException("Model is not correct");
		}

		// Some of the rules may have been optimized so they don't have
		// any effect on the final scores.  Eliminating those rules
		// entirely saves space and runtime
		removeDeadRules();

		int indexMax = Utility.maxIndex(prob.lambda);
		log.info("max lambda = " + prob.lambda[indexMax]);
		log.info("feature " + indexMax + " = " + feats.get(indexMax));

		//		for (int i = 0; i < prob.lambda.length; ++i) {
		//			if (prob.lambda[i] > 10000) {
		//				log.info("lambda[" + i + "] = " + prob.lambda[i] + " is too big!");
		//			}
		//		}

		// If any of the features have been optimized to 0, we can remove
		// them from the LambdaSolve.  This will save quite a bit of space
		// depending on the optimization used
		simplifyLambda();
		//		log.info("BEFORE SAVING, lambda = ");
		//		log.info("lambda = " + Utility.toString(prob.lambda));

		saveModel(Utility.workingDirectory + "models//" + config.getModel());
		//		log.info("AFTER SAVING");
		//		log.info("lambda = " + Utility.toString(prob.lambda));
		//		log.info("Extractors list:");
		//		log.info(extractors.toString() + "\nrare" + extractorsRare.toString());
	}

	void runTraining(TaggerConfig config) throws IOException {
		this.config = config;
		if (config != null) {
			curWordMinFeatureThresh = config.getCurWordMinFeatureThresh();
			minFeatureThresh = config.getMinFeatureThresh();
			rareWordMinFeatureThresh = config.getRareWordMinFeatureThresh();
			rareWordThresh = config.getRareWordThresh();

			veryCommonWordThresh = config.getVeryCommonWordThresh();
			occurringTagsOnly = config.occurringTagsOnly();
			possibleTagsOnly = config.possibleTagsOnly();
			// log.info("occurringTagsOnly: "+occurringTagsOnly);
			// log.info("possibleTagsOnly: "+possibleTagsOnly);

			if (config.getDefaultScore() >= 0)
				defaultScore = config.getDefaultScore();
		}

		//		Date now = new Date();

		//		log.info("## tagger training invoked at " + now + " with arguments:");
		//		config.dump();
		Timing tim = new Timing();

		//		PrintFile log = new PrintFile(config.getModel() + ".props");
		//		log.println("## tagger training invoked at " + now + " with arguments:");
		//		config.dump(log);
		//		log.close();

		trainAndSaveModel(config);
		tim.done("Training POS tagger");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 260569947424040861L;

	private POSTagger(String fileModel) {
		super(fileModel);
	}

	static void swap(Extractor extractor, String oldTag, String newTag, Map<String, int[]> map) {
		Map<String, String> mapReplacement = new HashMap<String, String>();
		switch (extractor.getClass().getSimpleName()) {
		case "Extractor": {
			if (!extractor.isDynamic()) {
				break;
			}

			if (map.containsKey(oldTag)) {
				mapReplacement.put(oldTag, newTag);
			}

			break;
		}

		case "ExtractorTwoTags":
		case "ExtractorThreeTags":
		case "ExtractorContinuousTagConjunction": {
			for (String str : map.keySet()) {
				String tags[] = str.split("!");
				boolean drapeau = false;

				for (int i = 0; i < tags.length; i++) {
					if (tags[i].equals(oldTag)) {
						tags[i] = newTag;
						drapeau = true;
					}
				}

				if (drapeau) {
					mapReplacement.put(str, Utility.toString(tags, "!", null, tags.length));
				}
			}

			break;
		}

		case "ExtractorTwoWordsTag": {
			for (String str : map.keySet()) {
				String tags[] = str.split("!");

				boolean drapeau = false;

				if (tags[1].equals(oldTag)) {
					tags[1] = newTag;
					drapeau = true;
				}

				if (drapeau) {
					mapReplacement.put(str, Utility.toString(tags, "!", null, tags.length));
				}
			}

			break;
		}
		case "ExtractorWordTag": {
			for (String str : map.keySet()) {
				String tags[] = str.split("!");

				boolean drapeau = false;

				if (tags[0].equals(oldTag)) {
					tags[0] = newTag;
					drapeau = true;
				}

				if (drapeau) {
					mapReplacement.put(str, Utility.toString(tags, "!", null, tags.length));
				}
			}

			break;
		}
		case "ExtractorWordTwoTags": {
			for (String str : map.keySet()) {
				String tags[] = str.split("!");

				boolean drapeau = false;

				if (tags[0].equals(oldTag)) {
					tags[0] = newTag;
					drapeau = true;
				}
				if (tags[2].equals(oldTag)) {
					tags[2] = newTag;
					drapeau = true;
				}

				if (drapeau) {
					mapReplacement.put(str, Utility.toString(tags, "!", null, tags.length));
				}
			}

			break;
		}
		}

		for (Map.Entry<String, String> pair : mapReplacement.entrySet()) {
			if (!map.containsKey(pair.getKey())) {
				throw new RuntimeException("!map.containsKey(pair.getKey())");
			}

			System.out.println("replacing " + pair.getKey());
			System.out.println("with tags " + pair.getValue());

			map.put(pair.getValue(), map.get(pair.getKey()));
			map.remove(pair.getKey());
		}
	}

	static boolean isTagExtractor(Extractor extractor) {
		return extractor.isDynamic();
	}

	int countTag(String tag) {
		int count = 0;
		for (Map<String, int[]> map : fAssociations) {
			for (String tagStr : map.keySet()) {
				if (tagStr.contains(tag))
					++count;
			}
		}
		return count;
	}

	public void swap(String oldTag, String newTag) {
		dict.swap(oldTag, newTag);
		tags.swap(oldTag, newTag);

		for (int i = 0; i < extractors.size(); ++i) {
			Extractor extractor = this.extractors.get(i);
			swap(extractor, oldTag, newTag, this.fAssociations.get(i));
		}

		for (int i = 0; i < this.extractorsRare.size(); ++i) {
			Extractor extractor = this.extractorsRare.get(i);
			swap(extractor, oldTag, newTag, this.fAssociations.get(i + extractors.size()));
		}
	}

	// TODO: make these constructors instead of init methods?
	protected void init(TaggerConfig config, Extractors extractors, Extractors extractorsRare) {
		this.config = config;

		String lang, arch;
		String[] openClassTags, closedClassTags;

		this.VERBOSE = config.getVerbose();

		lang = config.getLang();
		arch = config.getArch();
		openClassTags = config.getOpenClassTags();
		closedClassTags = config.getClosedClassTags();
		if (!config.getWordFunction().equals("")) {
			wordFunction = ReflectionLoading.loadByReflection(config.getWordFunction());
		}

		if (((openClassTags.length > 0) && !lang.equals("")) || ((closedClassTags.length > 0) && !lang.equals("")) || ((closedClassTags.length > 0) && (openClassTags.length > 0))) {
			throw new RuntimeException("At least two of lang (\"" + lang + "\"), openClassTags (length " + openClassTags.length + ": " + Arrays.toString(openClassTags) + ")," + "and closedClassTags (length " + closedClassTags.length + ": " + Arrays.toString(closedClassTags) + ") specified---you must choose one!");
		} else if ((openClassTags.length == 0) && lang.equals("") && (closedClassTags.length == 0) && !config.getLearnClosedClassTags()) {
			log.info("warning: no language set, no open-class tags specified, and no closed-class tags specified; assuming ALL tags are open class tags");
		}

		if (openClassTags.length > 0) {
			tags = new TTags();
			tags.setOpenClassTags(openClassTags);
		} else if (closedClassTags.length > 0) {
			tags = new TTags();
			tags.setClosedClassTags(closedClassTags);
		} else {
			tags = new TTags(lang);
		}

		defaultScore = lang.equals("english") ? 1.0 : 0.0;

		if (config != null) {
			rareWordThresh = config.getRareWordThresh();
			minFeatureThresh = config.getMinFeatureThresh();
			curWordMinFeatureThresh = config.getCurWordMinFeatureThresh();
			rareWordMinFeatureThresh = config.getRareWordMinFeatureThresh();
			veryCommonWordThresh = config.getVeryCommonWordThresh();
			occurringTagsOnly = config.occurringTagsOnly();
			possibleTagsOnly = config.possibleTagsOnly();
			// log.info("occurringTagsOnly: "+occurringTagsOnly);
			// log.info("possibleTagsOnly: "+possibleTagsOnly);

			if (config.getDefaultScore() >= 0)
				defaultScore = config.getDefaultScore();
		}

		// just in case, reset the defaultScores array so it will be
		// recached later when needed.  can't initialize it now in case we
		// don't know ysize yet
		defaultScores = null;

		if (config == null || config.getMode() == TaggerConfig.Mode.TRAIN) {
			// initialize the extractors based on the arch variable
			// you only need to do this when training; otherwise they will be
			// restored from the serialized file
			//			this.extractors = new Extractors(ExtractorFrames.getExtractorFrames(arch));
			this.extractors = extractors;
			this.extractorsRare = extractorsRare;

			setExtractorsGlobal();
		}

		ambClasses = new AmbiguityClasses(tags);
	}

	public void trainingCorpus() throws IOException, ClassNotFoundException {
		String opt[] = { "-props", Utility.workingDirectory + "models/chinese.tagger.props" };
		TaggerConfig config = new TaggerConfig(opt);

		//		Extractor[] extractors = { new Extractor(0, false),
		//				new Extractor(-1, false),
		//				new Extractor(1, false),
		//				new Extractor(-2, false),
		//				new Extractor(2, false),
		//				new Extractor(-1, true),
		//				new Extractor(-2, true),
		//				new ExtractorFrames.ExtractorContinuousTagConjunction(-2),
		//				new ExtractorFrames.ExtractorIntonation(), };

		//		fAssociations = Generics.newArrayList();
		//		init(config, new Extractors(extractors), (Extractors) Utility.loadFrom(Utility.workingDirectory + "models//extractorsRare.gz"));
		runTraining(config);

		Printer printer = new Printer(Utility.workingDirectory + "/corpus/dict.txt");

		this.dict.print();
		System.out.println(Utility.toString(instance.tagTokens, Utility.lineSeparator, null, instance.tagTokens.size()));

		printer.close();

		//		System.out.println("maxentTagger = " + toString());
		test();
	}

	//TESTING THE TAGGER:
	void test() throws IOException {
		int error = 0;
		int total = 0;
		VERBOSE = false;
		Printer printer = new Printer(Utility.workingDirectory + "/corpus/debug.pos.txt");
		for (SyntacticTree inst : new DependencyTreeReader()) {
			String[] seg = inst.getLEX();
			String[] _pos = inst.getPOS();
			String[] pos = tag(seg);

			boolean bEqual = true;
			int unequalIndex = -1;
			for (int i = 0; i < pos.length; ++i) {
				if (!_pos[i].equals(pos[i])) {
					bEqual = false;
					unequalIndex = i;
					break;
				}
			}

			if (!bEqual) {
				String[] res = Utility.convertWithAlignment(seg, _pos, pos, Utility.errorMark(pos.length, unequalIndex));

				++error;
				System.out.println(inst.unadornedExpression());
				for (String s : res) {
					System.out.println(s);
				}
			}

			++total;
		}
		System.out.println("error = " + error);
		System.out.println("total = " + total);
		System.out.println("right = " + (1.0 - error * 1.0 / total));
		printer.close();
	}

	public static void main(String[] args) throws Exception {
		//				instance.test();
		//		instance.trainingCorpus();
		Utility.maxPrintability = 50;
		//		Utility.setOut(Utility.workingDirectory + "debug1.txt");
		instance.test();

		double[] lambda;
		List<Map<String, int[]>> fAssociations;

		lambda = instance.prob.lambda.clone();
		fAssociations = ((MaxentTagger) instance).fAssociations;
		Dictionary dict = instance.dict;
		Map<String, Set<String>> tagTokens = instance.tagTokens;
		int curWordMinFeatureThresh = instance.curWordMinFeatureThresh;
		int minFeatureThresh = instance.minFeatureThresh;
		int rareWordMinFeatureThresh = instance.rareWordMinFeatureThresh;
		int rareWordThresh = instance.rareWordThresh;

		AmbiguityClasses ambClasses = instance.ambClasses;
		instance = null;
		instance = new POSTagger(Utility.workingDirectory + "models//chinese.tagger");

		if (lambda == instance.prob.lambda) {
			throw new RuntimeException("POSTagger.lambda == prob.lambda");
		}
		if (!Utility.equals(instance.prob.lambda, lambda)) {
			throw new RuntimeException("!Utility.equals(prob.lambda, POSTagger.lambda)");
		}

		if (fAssociations == instance.fAssociations)
			throw new RuntimeException("POSTagger.lambda == prob.lambda");

		if (!Utility.equals(fAssociations, instance.fAssociations)) {
			Utility.equals(fAssociations, instance.fAssociations);
			throw new RuntimeException("!Utility.equals(prob.lambda, POSTagger.lambda)");
		}

		if (!dict.equals(instance.dict)) {
			dict.equals(instance.dict);
			throw new RuntimeException("!Utility.equals(prob.lambda, POSTagger.lambda)");
		}

		//		if (!Utility.equals(tagTokens, instance.tagTokens)) {
		//			Utility.equals(tagTokens, instance.tagTokens);
		//			throw new RuntimeException("!Utility.equals(prob.lambda, POSTagger.lambda)");
		//		}

		if (curWordMinFeatureThresh != instance.curWordMinFeatureThresh) {
			throw new RuntimeException("curWordMinFeatureThresh != instance.curWordMinFeatureThresh");
		}

		if (minFeatureThresh != instance.minFeatureThresh) {
			throw new RuntimeException("curWordMinFeatureThresh != instance.curWordMinFeatureThresh");
		}

		if (rareWordMinFeatureThresh != instance.rareWordMinFeatureThresh) {
			throw new RuntimeException("curWordMinFeatureThresh != instance.curWordMinFeatureThresh");
		}

		if (rareWordThresh != instance.rareWordThresh) {
			throw new RuntimeException("curWordMinFeatureThresh != instance.curWordMinFeatureThresh");
		}

		//		Utility.setOut(Utility.workingDirectory + "debug2.txt");
		//		instance.shuffle();
		//		instance.test();
	}

	public static List<HasWord> toWordList(String... words) {
		List<HasWord> sent = new ArrayList<>();
		for (String word : words) {
			word = Utility.simplifyString(word);
			CoreLabel cl = new CoreLabel();
			cl.setValue(word);
			cl.setWord(word);
			sent.add(cl);
		}
		return sent;
	}

	public static List<HasWord> toWordList(String[] words, String[] pos) {
		List<HasWord> sent = new ArrayList<>();
		for (int i = 0; i < pos.length; i++) {
			String word = Utility.simplifyString(words[i]);
			CoreLabel cl = new CoreLabel();
			cl.setValue(word);
			cl.setWord(word);
			if (instance.tagSet().contains(pos[i]))
				cl.setTag(pos[i]);
			sent.add(cl);
		}
		return sent;
	}

	public String wordFunction(String w) {
		if (wordFunction != null)
			return wordFunction.apply(w);
		return w;
	}

	public String[] tag(String[] seg) {
		List<TaggedWord> tSentence = tagSentence(toWordList(seg));
		String[] pos = new String[seg.length];
		for (int i = 0; i < pos.length; i++) {
			pos[i] = tSentence.get(i).tag();
		}
		return pos;
	}

	public String[] tag(String[] seg, String[] pos) {
		List<TaggedWord> tSentence = tagSentence(toWordList(seg, pos), true);
		for (int i = 0; i < pos.length; i++) {
			pos[i] = tSentence.get(i).tag();
		}
		return pos;
	}

	public static POSTagger instance;
	static {
		instance = new POSTagger(Utility.workingDirectory + "models/chinese.tagger");
		((ExtractorFrames.ExtractorIntonation) instance.extractors.v[8]).dict = instance.dict.dict;
		instance.wordFunction = null;
	}
}

//!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~
//！＂＃＄％＆＇（）＊＋，－．／０１２３４５６７８９：；＜＝＞？＠ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ［＼］＾＿｀ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ｛｜｝～