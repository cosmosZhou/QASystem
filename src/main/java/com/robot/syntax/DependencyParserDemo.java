package com.robot.syntax;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.robot.util.Instance;
import com.robot.util.InstanceReader;
import com.util.Utility;

/**
 * Demonstrates how to first use the tagger, then use the NN dependency parser.
 * Note that the parser will not work on untagged text.
 *
 * @author Jon Gauthier
 */
public class DependencyParserDemo {

	/** A logger for this class */
	private static Redwood.RedwoodChannels log = Redwood.channels(DependencyParserDemo.class);

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
		String modelPath = Utility.workingDirectory + "nlp/parser\\nndep/UD_Chinese.gz";
		String taggerPath = Utility.workingDirectory + "nlp/pos-tagger/chinese-distsim/chinese-distsim.tagger";

		MaxentTagger tagger = new MaxentTagger(taggerPath);
		SyntacticParser parser = new SyntacticParser(modelPath);

		Utility.setOut(Utility.workingDirectory + "corpus/stanford_dep.txt");
		for (Instance inst : new InstanceReader(Utility.workingDirectory + "corpus/dep.txt")) {
			SyntacticTree tree = (SyntacticTree) inst.getData();
			String[] seg = tree.getLEX();
			List<TaggedWord> tSentence = tagger.tagSentence(POSTagger.toWordList(seg));

			SyntacticTree gs = parser.parse(tSentence);
			// Print typed dependencies
			System.out.println(gs.toString());
			System.out.println();
		}

	}

}
