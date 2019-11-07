package com.robot.syntax;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.util.Utility;

public class DependencyTreeReader implements Iterable<SyntacticTree>, Iterator<SyntacticTree> {
	public static void main(String[] args) {
		log.info(setPOSSupportedTags);
		log.info(setDEPSupportedTags);
	}

	static String depCorpus = Utility.workingDirectory + "corpus/dep.txt";
	static String _depCorpus = Utility.workingDirectory + "corpus/_dep.txt";

	public DependencyTreeReader(String txt) throws UnsupportedEncodingException, FileNotFoundException {
		StringReader = new Utility.Text(txt);
	}

	public DependencyTreeReader() throws UnsupportedEncodingException, FileNotFoundException {
		this(depCorpus);
	}

	Utility.Text StringReader;
	SyntacticTree instance;
	String infix = null;
	static Set<String> setPOSSupportedTags = POSTagger.instance.tagSet();
	static Set<String> setDEPSupportedTags;
	static {
		initializeDEPSupportedTags();
	}

	static void initializeDEPSupportedTags() {
		setDEPSupportedTags = new HashSet<String>();
		for (String label : SyntacticParser.instance.tagSet()) {
			setDEPSupportedTags.add(label.toUpperCase());
		}
	}

	public String stringRepresentative(SyntacticTree instance) {
		String sent = instance.unadornedExpression();
		sent = Utility.removeEndOfSentencePunctuation(sent);
		return sent;
	}

	public static boolean isPOSsequence(String str[]) {
		for (String e : str) {
			if (setPOSSupportedTags.contains(e.toUpperCase()))
				continue;
			else
				return false;
		}
		return true;
	}

	public static boolean isInfix(String str) {
		return str.startsWith("(") || str.endsWith(")");
	}

	static boolean isComment(String str) {
		return str.startsWith(";");
	}

	public static boolean isDEPsequence(String str[]) {
		for (String e : str) {
			if (setDEPSupportedTags.contains(e.toUpperCase()))
				continue;
			else
				return false;
		}
		return true;
	}

	void parse_pos(String infix, String pos[]) {
		try {
			Utility.toUpperCase(pos);
			instance = Compiler.compile(infix, pos);
			this.infix = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	void parse_dep(String infix, String dep[]) {
		try {
			instance = Compiler.compile(infix, POSTagger.instance.tag(Compiler.parse(infix)), dep);
			this.infix = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	void parse(String infix, String pos[], String dep[]) {
		try {
			Utility.toUpperCase(pos);
			instance = Compiler.compile(infix, pos, dep);
			this.infix = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	void parse_infix(String infix) {
		try {
			instance = Compiler.compile(infix);
			this.infix = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public boolean hasNext() {
		if (infix == null) {
			for (;;) {
				if (!StringReader.hasNext())
					return false;
				infix = StringReader.next();

				if (isComment(infix))
					continue;
				if (isInfix(infix))
					break;
			}
		}

		if (!StringReader.hasNext()) {
			parse_infix(infix);
			return true;
		}

		// deal with the case where only infix is known, seg, pos dep is
		// unknown;
		String str = StringReader.next();
		if (isComment(str)) {
			parse_infix(infix);
			return true;
		} else if (isInfix(str)) {
			parse_infix(infix);
			infix = str;
			return true;
		}

		String sstr[] = str.split("\\s+");
		String seg[] = null;
		String pos[] = null;
		String dep[] = null;

		if (isPOSsequence(sstr)) {
			pos = sstr;
			if (!StringReader.hasNext()) {
				parse_pos(infix, pos);
				return true;
			}
		} else if (isDEPsequence(sstr)) {
			dep = sstr;
			if (!StringReader.hasNext()) {
				parse_dep(infix, dep);
				return true;
			}
		} else {
			seg = sstr;
			if (!StringReader.hasNext()) {
				parse_infix(infix);
				return true;
			}
		}
		// deal with the case where seg is known, or pos is known, or dep is
		// known;
		str = StringReader.next();
		boolean isComment = isComment(str);
		boolean isInfix = false;
		if (!isComment)
			isInfix = isInfix(str);
		if (isComment || isInfix) {
			if (pos != null) {
				parse_pos(infix, pos);
			} else if (dep != null) {
				parse_dep(infix, dep);
			} else {
				parse_infix(infix);
			}
			if (isInfix)
				infix = str;

			return true;
		}

		sstr = str.split("\\s+");

		if (isPOSsequence(sstr)) {
			if (pos != null) {
				log.info("multiple pos messages specified");
				log.info("infix = " + infix);
				throw new RuntimeException("multiple pos messages specified");
				//				parse_pos(infix, pos);
				//				return true;
			}

			pos = sstr;
			if (dep != null) {
				parse(infix, pos, dep);
				return true;
			}

			if (!StringReader.hasNext()) {
				parse_pos(infix, pos);
				return true;
			}
		} else if (isDEPsequence(sstr)) {
			if (dep != null) {
				log.info("multiple dep messages specified");
				log.info("infix = " + infix);
				throw new RuntimeException("multiple dep messages specified");

				//				parse_dep(infix, dep);
				//				return true;
			}

			dep = sstr;
			if (pos != null) {
				parse(infix, pos, dep);
				return true;
			}

			if (!StringReader.hasNext()) {
				parse_dep(infix, dep);
				return true;
			}
		} else {
			if (seg != null) {
				log.info("multiple seg messages specified");
				log.info("infix = " + infix);
				throw new RuntimeException("multiple seg messages specified");

				//				parse_infix(infix);
				//				return true;
			}

			if (!StringReader.hasNext()) {
				if (pos != null)
					parse_pos(infix, pos);
				else if (dep != null)
					parse_dep(infix, dep);
				else {
					log.info("impossible to occur");
					log.info("infix = " + infix);
					throw new RuntimeException("impossible to occur");

					//					return false;
				}

				return true;
			}
		}

		// deal with the case where seg, pos are known, or seg, dep are
		// known,
		str = StringReader.next();
		isComment = isComment(str);
		isInfix = false;
		if (!isComment)
			isInfix = isInfix(str);
		if (isComment || isInfix) {
			if (pos != null) {
				parse_pos(infix, pos);
			} else if (dep != null) {
				parse_dep(infix, dep);
			} else {
				parse_infix(infix);
			}
			if (isInfix)
				infix = str;
			return true;
		}

		sstr = str.split("\\s+");

		if (isPOSsequence(sstr)) {
			if (pos != null) {
				log.info("multiple pos messages specified");
				log.info("infix = " + infix);
				throw new RuntimeException("multiple pos messages specified");

				//				parse_pos(infix, pos);
				//				return true;
			}
			// now pos, dep are all known,
			pos = sstr;
			parse(infix, pos, dep);
			return true;
		} else if (isDEPsequence(sstr)) {
			if (dep != null) {
				log.info("multiple dep messages specified");
				log.info("infix = " + infix);
				throw new RuntimeException("multiple dep messages specified");

				//				parse_dep(infix, dep);
				//				return true;
			}
			// now pos, dep are all known,
			dep = sstr;
			parse(infix, pos, dep);
			return true;
		} else {
			log.info("multiple seg messages specified");
			log.info("infix = " + infix);
			throw new RuntimeException("multiple seg messages specified");
		}
	}

	@Override
	public SyntacticTree next() {
		return instance;
	}

	@Override
	public Iterator<SyntacticTree> iterator() {
		return this;
	}

	public ArrayList<SyntacticTree> collect() {
		ArrayList<SyntacticTree> arr = new ArrayList<SyntacticTree>();
		for (SyntacticTree t : this) {
			if (!t.isValidate()) {
				log.info(t);
				log.info(t.infixPOSExpression());
				continue;
				//				throw new RuntimeException();
			}
			arr.add(t);
		}
		return arr;
	}

	public static void addTrainingInstance(SyntacticTree... tree) throws IOException {
		ArrayList<String> arr = new ArrayList<String>();
		HashSet<String> set = new HashSet<String>();

		for (SyntacticTree t : tree) {
			if (!t.isValidate())
				continue;
			if (set.add(t.simplifiedString())) {
				arr.add(t.infixPOSExpression());
			}
		}

		for (SyntacticTree t : new DependencyTreeReader()) {
			if (!t.isValidate())
				continue;

			if (set.add(t.simplifiedString())) {
				arr.add(t.infixPOSExpression());
			}
		}

		Utility.writeString(depCorpus, arr);
	}

	public static void addTrainingInstance(Collection<SyntacticTree> tree) throws IOException {
		ArrayList<String> arr = new ArrayList<String>();
		HashSet<String> set = new HashSet<String>();

		for (SyntacticTree t : tree) {
			if (!t.isValidate())
				continue;
			if (set.add(t.simplifiedString())) {
				arr.add(t.infixPOSExpression());
			}
		}

		for (SyntacticTree t : new DependencyTreeReader()) {
			if (!t.isValidate())
				continue;

			if (set.add(t.simplifiedString())) {
				arr.add(t.infixPOSExpression());
			}
		}

		Utility.writeString(depCorpus, arr);
	}

	public static void writeTrainingInstance(ArrayList<SyntacticTree> tree) throws IOException {
		ArrayList<String> arr = new ArrayList<String>();
		HashSet<String> set = new HashSet<String>();

		for (SyntacticTree t : tree) {
			if (!t.isValidate())
				continue;

			if (set.add(t.simplifiedString())) {
				arr.add(t.infixPOSExpression());
			}
		}

		Utility.writeString(depCorpus, arr);
	}

	private static Logger log = Logger.getLogger(DependencyTreeReader.class);
}
