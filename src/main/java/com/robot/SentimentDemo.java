package com.robot;

import java.io.*;
import java.util.*;

import com.util.Utility;

import edu.stanford.nlp.coref.CorefCoreAnnotations;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class SentimentDemo {
	public static void main(String[] args) throws IOException {
		// set up optional output files
		PrintStream out = System.out;

		// Create a CoreNLP pipeline. To build the default pipeline, you can just use:
		//   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		// Here's a more complex setup example:
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, parse");

		props.put("pos.model", Utility.workingDirectory + "nlp/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		props.put("sentiment.model", Utility.workingDirectory + "nlp/sentiment/sentiment.ser.gz");
		props.put("parse.model", Utility.workingDirectory + "nlp/lexparser/englishPCFG.ser.gz");
		props.put("parse.binaryTrees", "true");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		//Add in sentiment
		//Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
		Annotation annotation;
		if (args.length > 0) {
			annotation = new Annotation(IOUtils.slurpFileNoExceptions(args[0]));
		} else {
			annotation = new Annotation("Kosgi Santosh sent an email to Stanford University. He didn't get a reply.");
		}

		// run all the selected Annotators on this text
		pipeline.annotate(annotation);

		SentimentAnnotator sentimentAnnotator = new SentimentAnnotator(StanfordCoreNLP.STANFORD_SENTIMENT, props);
		sentimentAnnotator.annotate(annotation);

		// this prints out the results of sentence analysis to file(s) in good formats
		//		pipeline.prettyPrint(annotation, out);
		// Access the Annotation in code
		// The toString() method on an Annotation just prints the text of the Annotation
		// But you can see what is in it with other methods like toShorterString()
		out.println();
		out.println("The top level annotation");
		out.println(annotation.toShorterString());
		out.println();

		// An Annotation is a Map with Class keys for the linguistic analysis types.
		// You can get and use the various analyses individually.
		// For instance, this gets the parse tree of the first sentence in the text.
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			//			out.println("The keys of the sentence's CoreMap are:");
			//			out.println(sentence.keySet());
			//			out.println(sentence.toShorterString());
			//			out.println();
			//			out.println("The sentence tokens are:");
			//			for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
			//				out.println(token.toShorterString());
			//			}

			//			SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
			//			out.println(graph.toString(SemanticGraph.OutputFormat.LIST));

			SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//			System.out.println(graph.toString(SemanticGraph.OutputFormat.LIST));

			//			graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			//			out.println(graph.toString(SemanticGraph.OutputFormat.LIST));

			graph = sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class);
//			System.out.println(graph.toString(SemanticGraph.OutputFormat.READABLE));

//			graph = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
//			System.out.println(graph.toString(SemanticGraph.OutputFormat.RECURSIVE));

			Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
			Tree binarized = sentence.get(TreeCoreAnnotations.BinarizedTreeAnnotation.class);
			Tree collapsedUnary = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
			out.println();
			out.println(((LabeledScoredTreeNode)collapsedUnary).infixExpression());
			
//			out.println(binarized.toString());
//			tree.pennPrint(out);
			out.println();
			//			out.println("The first sentence basic dependencies are:");
//			out.println(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));
			//			out.println("The first sentence collapsed, CC-processed dependencies are:");
			graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//			out.println(graph.toString(SemanticGraph.OutputFormat.LIST));

			out.println("The sentence overall sentiment rating is " + sentence.get(SentimentCoreAnnotations.SentimentClass.class));
		}
	}

}
