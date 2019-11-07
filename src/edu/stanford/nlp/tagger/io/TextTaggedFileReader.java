package edu.stanford.nlp.tagger.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import com.robot.syntax.DependencyTreeReader;
import com.robot.syntax.SyntacticTree;
import com.util.Utility;

import edu.stanford.nlp.ling.TaggedWord;

public class TextTaggedFileReader implements TaggedFileReader {
	//	final BufferedReader reader;
	final DependencyTreeReader reader;
	final String tagSeparator;
	final String filename;

	int numSentences = 0;

	List<TaggedWord> next;

	public TextTaggedFileReader(TaggedFileRecord record) {
		filename = record.file;
		try {
			reader = new DependencyTreeReader(filename);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		tagSeparator = record.tagSeparator;

		primeNext();
	}

	public TextTaggedFileReader(String filename) {
		this.filename = filename;
		try {
			reader = new DependencyTreeReader(filename);
//			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		tagSeparator = null;
		primeNext();
	}

	public Iterator<List<TaggedWord>> iterator() {
		return this;
	}

	public String filename() {
		return filename;
	}

	public boolean hasNext() {
		return next != null;
	}

	public List<TaggedWord> next() {
		if (next == null) {
			throw new NoSuchElementException();
		}
		List<TaggedWord> thisIteration = next;
		primeNext();
		return thisIteration;
	}

	SyntacticTree readNext() {
		if (reader.hasNext()) {
			return reader.next();
		}
		return null;
	}

	void primeNext() {
		SyntacticTree tree = readNext();
		if (tree == null) {
			next = null;
			return;
		}

		String[] segs = tree.getLEX();
		String[] poss = tree.getPOS();

		++numSentences;
		next = new ArrayList<>();

		for (int i = 0; i < poss.length; i++) {
			next.add(new TaggedWord(Utility.simplifyString(segs[i]), poss[i]));
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
