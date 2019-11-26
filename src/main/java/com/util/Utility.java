package com.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Spliterator;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jblas.DoubleMatrix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.util.DataSource.Driver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import oracle.jdbc.pool.OracleDataSource;

public class Utility {
	public static void main(String[] args) throws Exception {
		Timer Timer = new Timer();

		String sentence = "((((它/DT/de)的/DE/adj)优势/NN/adj)在于/VT/adv)(，/PU/pu)无须/VT/root((驾驶员/NN/suj)进行/VT/obj((任何/JJ/adj)操作/VBG/obj(，/PU/pu)(((不会/NEG/adj)分散/NN/adj)注意力/NN/de)))";

		Matcher m = Pattern.compile("在于/(?!(VC|VT))[^\\(\\)/]+").matcher(sentence);

		if (m.find()) {
			System.out.println(m.group());
			System.out.println("matches");
		} else {
			System.out.println("not");
		}

		Timer.report();
	}

	static public void pause() throws IOException {
		System.out.println("press any key to continue.");
		BufferedReader bufferedReader = Utility.readFromStdin();
		bufferedReader.readLine();
	}

	static public long parseDigitFromChinese(String num) throws Exception {
		String[] res = Utility.regexSingleton(num, "(\\S+亿)?(\\S+)?");
		// System.out.println(Utility.toString(res, "\n"));

		long sum = 0;
		if (res[1] != null)
			sum += parseDigitFromChinese99999999(res[1].substring(0, res[1].length() - 1)) * 100000000;
		if (res[2] != null)
			sum += parseDigitFromChinese99999999(res[2]);

		return sum;
	}

	static public long parseDigitFromChinese99999999(String num) throws Exception {
		// 拾、佰、仟、万、亿、
		String[] res = Utility.regexSingleton(num, "(\\S+万)?(\\S+)?");
		// System.out.println(Utility.toString(res, "\n"));
		long sum = 0;
		if (res[1] != null)
			sum += parseDigitFromChinese9999(res[1].substring(0, res[1].length() - 1)) * 10000;
		if (res[2] != null)
			sum += parseDigitFromChinese9999(res[2]);

		return sum;
	}

	static public long parseDigitFromChinese9999(String num) throws Exception {
		// 拾、佰、仟、万、亿、
		String[] res = Utility.regexSingleton(num, "(\\S千)?(\\S百)?(\\S*十)?(\\S)?");
		// System.out.println(Utility.toString(res, "\n"));
		long sum = 0;
		int magnitude = 1000;
		for (int i = 1; i < res.length; ++i) {
			int digit = 0;

			if (res[i] != null) {
				switch (res[i].charAt(0)) {
				case '0':
				case '０':
				case '零':
				case '〇':
					break;
				case '1':
				case '１':
				case '壹':
				case '一':
				case '十':
					digit = 1;
					break;
				case '2':
				case '２':
				case '贰':
				case '二':
				case '两':
					digit = 2;
					break;
				case '3':
				case '３':
				case '叁':
				case '三':
					digit = 3;
					break;
				case '4':
				case '４':
				case '肆':
				case '四':
					digit = 4;
					break;
				case '5':
				case '５':
				case '伍':
				case '五':
					digit = 5;
					break;
				case '6':
				case '６':
				case '陆':
				case '六':
					digit = 6;
					break;
				case '7':
				case '７':
				case '柒':
				case '七':
					digit = 7;
					break;
				case '8':
				case '８':
				case '捌':
				case '八':
					digit = 8;
					break;
				case '9':
				case '９':
				case '玖':
				case '九':
					digit = 9;
					break;
				default:
					throw new Exception("unsupported digit " + res[i]);
				}
				sum += digit * magnitude;
			}
			magnitude /= 10;
		}
		return sum;
	}

	static public String dights[] = { "0０零〇", "1１壹一十", "2２贰二两", "3３叁三", "4４肆四", "5５伍五", "6６陆六", "7７柒七", "8８捌八", "9９玖九",
			"百千万亿" };
	static public String dightsArabic = "0123456789";
	static public String dightsArabicMB = "０１２３４５６７８９";
	static public String dightsChineseCapital = "零壹贰叁肆伍陆柒捌玖";
	static public String dightsChinese = "〇一二三四五六七八九十";

	static public String workingDirectory = "../";

	static public String corpusDirectory() {
		return workingDirectory + "corpus/";
	}

	static public String modelsDirectory() {
		return workingDirectory + "models/";
	}

	static public String[] convertToSegmentation(String str) {
		str = Pattern.compile("([" + Utility.sPunctuation + "])").matcher(str).replaceAll(" $1 ");

		str = Pattern.compile("(?<=[\\d]+)( +([\\.．：:]) +)(?=[\\d]+)").matcher(str).replaceAll("$2");

		int length = str.length();
		while (true) {
			str = Pattern.compile("([" + Utility.sPunctuation + "]) +\\1").matcher(str).replaceAll("$1$1");
			if (str.length() < length) {
				length = str.length();
				continue;
			} else
				break;
		}

		return str.trim().split("\\s+");
	}


	static public String[] convertWithAlignment(String[]... arr) {
		String[] res = new String[arr.length];
		for (int i = 0; i < res.length; ++i) {
			res[i] = "";
		}

		int size = arr[0].length;
		for (int j = 0; j < size; ++j) {
			int length[] = new int[arr.length];

			for (int i = 0; i < arr.length; ++i) {
				res[i] += arr[i][j] + ' ';
				length[i] = length(arr[i][j]);
			}

			int maxLength = max(length);
			for (int i = 0; i < arr.length; ++i) {
				res[i] += toString(maxLength - length[i], ' ');
			}
		}

		return res;
	}

	// public static class IndexedPriorityQueue<_Ty> {
	// PriorityQueue<Integer> pq;
	// ArrayList<Couplet<_Ty, Integer>> arr;
	//
	// IndexedPriorityQueue(final Comparator<_Ty> pred) {
	// arr = new ArrayList<Couplet<_Ty, Integer>>();
	// Comparator<Integer> _Pr = new Comparator<Integer>() {
	// public int compare(Integer i, Integer j) {
	// return pred.compare(arr.get(i).x, arr.get(j).x);
	// }
	// };
	// pq = new PriorityQueue<Integer>(1, _Pr);
	//
	// }
	//
	// // dequeue operation;
	// _Ty poll() {
	// int index = pq.poll();
	// _Ty var = arr.get(index).x;
	// arr.add(index, null);
	// return var;
	// }
	//
	// void set(int i, _Ty _Val) {
	// while (i >= arr.size())
	// arr.add(null);
	// arr.get(i).x = _Val;
	// arr.get(i).y = pq.reset(arr.get(i).y, i);
	// }
	// }

	static public void saveTo(String file, Object... obj) throws IOException {
		File f = new File(file);
		File path = f.getParentFile();
		if (path != null && !path.exists()) {
			path.mkdirs();
		}

		ObjectOutputStream out = new ObjectOutputStream(
				new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))));
		for (Object o : obj)
			out.writeObject(o);
		out.close();
	}

	ObjectInputStream getObjectGZIPBufferedFileInputStream(String file) throws FileNotFoundException, IOException {
		return new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
	}

	public static Object[] loadFrom(String file, int size) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(
				new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
		Object[] obj = new Object[size];
		for (int i = 0; i < obj.length; ++i)
			obj[i] = in.readObject();
		in.close();
		return obj;
	}

	public static Object loadFrom(String file) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(
				new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));

		Object obj = in.readObject();
		in.close();
		return obj;
	}

	static void test_NodeShadow() {
		String str[] = { "中国", "进出口", "银行", "与", "中国", "银行", "加强", "合作", "。" };
		// String str[] = { "aa", "aaa", "aa", "a", "aa", "aa", "aa", "aa", "a"
		// };
		Node<String> node[] = new Node[str.length];
		for (int i = 0; i < node.length; ++i) {
			node[i] = new Node<String>(str[i]);
		}
		node[6].x = node[2];
		node[6].y = node[7];

		node[2].x = node[1];
		node[2].y = node[5];

		node[1].x = node[0];
		node[5].x = node[4];
		node[4].x = node[3];
		node[7].y = node[8];
		NodeShadow tree = node[6].buildShadowTree();
		log.info("tree\n" + tree);
	}

	static void test_LNodeShadow() {
		String str[] = { "中国", "进出口", "银行", "与", "中国", "银行", "加强", "友好", "广泛", "合作", "了", "。" };
		// String str[] = { "aa", "aaa", "aa", "a", "aa", "aa", "aa", "aa", "a"
		// };
		LNode<String> node[] = new LNode[str.length];
		for (int i = 0; i < node.length; ++i) {
			node[i] = new LNode<String>(str[i]);
		}
		node[6].x = new LNode[] { node[2] };
		node[6].y = new LNode[] { node[9], node[11] };

		node[2].x = new LNode[] { node[0], node[1] };
		node[2].y = new LNode[] { node[5] };

		node[5].x = new LNode[] { node[3], node[4] };
		node[9].x = new LNode[] { node[7], node[8] };
		node[9].y = new LNode[] { node[10] };
		LNodeShadow tree = node[6].buildShadowTree();
		log.info("tree\n" + tree);
	}

	static public ArrayList<String> skipLinePremiere(Iterator<Row> rows) {
		ArrayList<String> array = new ArrayList<String>();
		// read the first row to determine the initial position of the question.
		if (rows.hasNext()) {
			Row row = rows.next(); // 获得行数据
			Iterator<Cell> cells = row.cellIterator(); // 获得第一行的迭代器
			while (cells.hasNext()) {
				Cell cell = cells.next();
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
					System.out.print(cell.getNumericCellValue() + "\t");
					array.add("" + cell.getNumericCellValue());
					break;
				case Cell.CELL_TYPE_STRING:
					System.out.print(cell.getStringCellValue() + "\t");
					array.add(cell.getStringCellValue());
					break;
				case Cell.CELL_TYPE_FORMULA:
					System.out.print(cell.getStringCellValue() + "\t");
					array.add(cell.getStringCellValue());
					break;
				case Cell.CELL_TYPE_BLANK:
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					System.out.print(cell.getBooleanCellValue() + "\t");
					array.add("" + cell.getBooleanCellValue());
					break;

				case Cell.CELL_TYPE_ERROR:
					break;
				}

			}
			System.out.println();
			return array;
		}
		return null;
	}

	static public String convertSegmentationToOriginal(String[] arr) {
		String str = "";
		int i = 0;
		for (; i < arr.length - 1; ++i) {
			str += arr[i];
			if (Utility.isEnglish(arr[i].charAt(arr[i].length() - 1)) && Utility.isEnglish(arr[i + 1].charAt(0))) {
				str += " ";
				continue;
			}
		}
		str += arr[i];
		return str;
	}

	static public String convertFromSegmentation(String[] arr) {
		String str = "";
		int i = 0;
		for (; i < arr.length - 1; ++i) {
			str += arr[i];
			if (sPunctuation.indexOf(arr[i].charAt(arr[i].length() - 1)) >= 0
					|| sPunctuation.indexOf(arr[i + 1].charAt(0)) >= 0)
				continue;
			str += " ";
		}
		str += arr[i];
		return str;
	}

	static public String convertFromSegmentation(List<String> arr) {
		String str = "";
		int i = 0;
		for (; i < arr.size() - 1; ++i) {
			str += arr.get(i);
			if (sPunctuation.indexOf(arr.get(i).charAt(arr.get(i).length() - 1)) >= 0
					|| sPunctuation.indexOf(arr.get(i + 1).charAt(0)) >= 0)
				continue;
			str += " ";
		}
		str += arr.get(i);
		return str;
	}

	public static class RegexIterator implements Iterator<String[]>, Iterable<String[]> {
		Matcher matcher;
		// int beginIndex = 0;

		public RegexIterator(String str, String regex) {
			matcher = Pattern.compile(regex).matcher(str);
		}

		@Override
		public boolean hasNext() {
			return matcher.find();
		}

		@Override
		public java.lang.String[] next() {
			// if (beginIndex != matcher.start()) {
			// String res = "parsing error for " + str;
			// res += "\n" + "regex = " + regex;
			// res += "\n" + "regex = " + regex;
			// res += "\n" + "str + beginIndex = " + str.substring(beginIndex);
			// res += "\n" + "str + matcher.start() = " + str.substring(matcher.start());
			// throw new Exception(res);
			// }
			String[] group = new String[matcher.groupCount()];
			for (int i = 0; i < group.length; ++i)
				group[i] = matcher.group(i + 1);
			// x.add(group);
			// beginIndex = matcher.end();

			return group;
		}

		@Override
		public RegexIterator iterator() {
			// TODO Auto-generated method stub
			return this;
		}
	}

	public static RegexIterator regex(String str, String regex) throws Exception {
		return new RegexIterator(str, regex);
	}

	public static String[] regexSingleton(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		String[] x = null;
		int beginIndex = 0;
		if (matcher.find()) {
			if (beginIndex != matcher.start()) {
				return x;
			}
			x = new String[matcher.groupCount() + 1];
			for (int i = 0; i < x.length; ++i)
				x[i] = matcher.group(i);
		}
		return x;
	}

	public interface Replacer {
		String replace(String str);
	}

	public static String replace(String str, String regex, Replacer replacer) throws Exception {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		String tmp = "";
		int beginIndex = 0;
		while (matcher.find()) {
			if (beginIndex != matcher.start()) {
				tmp += str.substring(beginIndex, matcher.start());
			}
			tmp += replacer.replace(matcher.group(0));
			beginIndex = matcher.end();
		}
		if (beginIndex == 0)
			return str;
		tmp += str.substring(beginIndex);
		return tmp;
	}

	static public abstract class Reader<String> implements Iterator<String>, Iterable<String> {
		public void remove() {
			throw new IllegalStateException("This Iterator<Instance> does not support remove().");
		}

		public Iterator<String> iterator() {
			return this;
		}
	}

	static public class Text extends Reader<String> {
		public Text(String path) throws UnsupportedEncodingException, FileNotFoundException {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			this.path = path;
		}

		public java.lang.String path;
		public BufferedReader bufferedReader;
		String line;

		public <C extends Collection<String>> C collect(C c) {
			for (String s : this) {
				c.add(s);
			}
			return c;
		}

		@Override
		public boolean hasNext() {
			for (;;) {
				try {
					line = bufferedReader.readLine();
					if (line == null) {
						bufferedReader.close();
						return false;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					return false;
				}

				if (line.length() > 0) {
					char byteOrderMark = line.charAt(0);
					if (byteOrderMark == 0xFEFF) {
						line = line.substring(1);
					}
				}

				line = line.trim();
				if (line.length() > 0)
					return true;
			}
		}

		@Override
		public String next() {
			return line;
		}

		/**
		 * the length of the string content returned is dependent of the char-Encoding
		 * scheme; x denotes the number of English letters; y denotes the number of
		 * Chinese characters; for ASCII encoding, the length = x + 2 y; for UTF-8
		 * encoding, the length = 3 + x + 3 y;
		 */
		public String fetchContent() throws IOException {
			File file = new File(path);
			char[] cbuf = new char[(int) file.length()];
			java.util.Arrays.fill(cbuf, ' ');
			bufferedReader.read(cbuf);
			bufferedReader.close();
			if (cbuf[0] == 0xFEFF) {
				return new String(cbuf, 1, cbuf.length - 1);
			}
			return new String(cbuf);
		}
	}

	static public void enforceEndl(String path) throws IOException {
		writeString(path, new Text(path).collect(new ArrayList<String>()));
	}

	static public String lineSeparator = "\n";

	static public class TextReader extends Reader<String> {
		public TextReader(String path) throws UnsupportedEncodingException, FileNotFoundException {
			bufferedReader = new RandomAccessFile(path, "rw");
			this.path = path;
		}

		public java.lang.String path;
		public RandomAccessFile bufferedReader;
		String line;

		public <C extends Collection<String>> C collect(C c) {
			for (String s : this) {
				c.add(s);
			}
			return c;
		}

		@Override
		public boolean hasNext() {
			for (;;) {
				try {
					line = bufferedReader.readLine();
					if (line == null) {
						bufferedReader.close();
						return false;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					return false;
				}

				if (line.length() > 0) {
					char byteOrderMark = line.charAt(0);
					if (byteOrderMark == 0xFEFF) {
						line = line.substring(1);
					}
				}

				line = line.trim();
				if (line.length() > 0)
					return true;
			}
		}

		@Override
		public String next() {
			return line;
		}
	}

	static public void writeString(String path, Collection<String> c) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));

		for (String line : c) {
			if (line == null)
				continue;
			writer.write(line);
			writer.write(lineSeparator);
		}
		writer.close();
	}

	static public void writeString(String path, String str) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
		// BufferedWriter writer = new BufferedWriter(new FileWriter(path));

		writer.write(str);
		// writer.newLine();

		writer.close();
	}

	static public int appendString(String path, Collection<String> c) throws IOException {
		ArrayList<String> set = new Text(path).collect(new ArrayList<String>());
		int sum = 0;
		for (String s : c) {
			if (!set.contains(s)) {
				set.add(s);
				sum++;
			}
		}
		writeString(path, set);
		return sum;
	}

	static public int appendString(String path, String... str) throws IOException {
		int sum = 0;
		ArrayList<String> set = new Text(path).collect(new ArrayList<String>());

		for (String s : str) {
			if (set.contains(s))
				continue;
			set.add(s);
			++sum;
		}

		writeString(path, set);
		return sum;
	}

	/**
	 * prepend = append before the list;
	 */

	static public int prependString(String path, Collection<String> c) throws IOException {
		ArrayList<String> set = new Text(path).collect(new ArrayList<String>());
		int sum = 0;

		for (String s : c) {
			if (!set.contains(s)) {
				set.add(sum, s);
				sum++;
			}
		}
		writeString(path, set);
		return sum;
	}

	static public int prependString(String path, String... str) throws IOException {
		int sum = 0;
		ArrayList<String> set = new Text(path).collect(new ArrayList<String>());

		for (String s : str) {
			if (set.contains(s))
				continue;
			set.add(sum, s);
			++sum;
		}

		writeString(path, set);
		return sum;
	}

	static public void removeString(String path, Collection<String> c) throws IOException {
		ArrayList<String> set = new Text(path).collect(new ArrayList<String>());
		set.removeAll(c);
		writeString(path, set);
	}

	static public int removeString(String path, String... str) throws IOException {
		ArrayList<String> set = new Text(path).collect(new ArrayList<String>());
		int tally = 0;
		for (String s : str) {
			if (set.remove(s)) {
				++tally;
			}
		}

		writeString(path, set);
		return tally;
	}

	static public ArrayList<String> collect(String path) throws FileNotFoundException {
		Scanner scanner = new Scanner(new FileInputStream(path), "UTF-8");
		ArrayList<String> al = new ArrayList<String>();
		if (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (line.length() > 0) {
				char byteOrderMark = line.charAt(0);
				if (byteOrderMark == 0xFEFF)
					line = line.substring(1);
			}
			line = line.trim();
			if (line.length() > 0)
				al.add(line);
		}

		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			line = line.trim();
			if (line.length() > 0)
				al.add(line);
		}
		scanner.close();
		return al;
	}

	public static final String EnglishPunctuation = ",.:;!?()[]{}'\"=<>";
	public static final String ChinesePunctuation = "，。：；！？（）「」『』【】～‘’′”“《》、…．·";
	public static final String sPunctuation = EnglishPunctuation + ChinesePunctuation;
	public static final String endOfSentencePunctuation = "？?;；。.．,，…!！、";

	public static final String EnglishSpecial = "*/~|+-@&^#\\`_";
	public static final String ChineseSpecial = "—－ˉ·¨﹏｜＋–±→╯╰¤";

	public static final String EnglishQuantifier = "$%";
	public static final String ChineseQuantifier = "°￠￡￥";

	public static double inner_product(double[] x, double[] y) throws Exception {
		if (x.length != y.length)
			throw new Exception("x.length != y.length");
		double sum = 0;
		for (int i = 0; i < y.length; ++i) {
			sum += x[i] * y[i];
		}

		return sum;
	}

	public static float sum(float... value) {
		float sum = 0;
		for (float t : value) {
			sum += t;
		}
		return sum;
	}

	public static int sum(int... value) {
		int sum = 0;
		for (int t : value) {
			sum += t;
		}
		return sum;
	}

	public static double sum(double... value) {
		double sum = 0;
		for (double t : value) {
			sum += t;
		}
		return sum;
	}

	public static double sumOfSquares(double... value) {
		double sum = 0;
		for (double t : value) {
			sum += t * t;
		}
		return sum;
	}

	public static double EucledianNorm(double... value) {
		return Math.sqrt(sumOfSquares(value));
	}

	public static float average(float... value) {
		return sum(value) / value.length;
	}

	public static double average(double... value) {
		return sum(value) / value.length;
	}

	public static <T extends Comparable<T>> T max(Collection<T> arr) {
		Iterator<T> it = arr.iterator();
		T t = it.next();
		for (; it.hasNext();) {
			T autre = it.next();
			if (autre.compareTo(t) > 0) {
				t = autre;
			}
		}
		return t;
	}

	public static <T extends Comparable<T>> T min(Collection<T> arr) {
		Iterator<T> it = arr.iterator();
		T t = it.next();
		for (; it.hasNext();) {
			T autre = it.next();
			if (autre.compareTo(t) < 0) {
				t = autre;
			}
		}
		return t;
	}

	public static int max(int... arr) {
		return arr[maxIndex(arr)];
	}

	public static float max(float... arr) {
		return arr[maxIndex(arr)];
	}

	public static float min(float... arr) {
		return arr[minIndex(arr)];
	}

	public static double max(double... arr) {
		return arr[maxIndex(arr)];
	}

	public static int maxIndex(float... arr) {
		int maxIndex = 0;

		for (int x = 1; x < arr.length; x++) {
			if (arr[x] > arr[maxIndex])
				maxIndex = x;
		}

		return maxIndex;
	}

	public static int minIndex(float... arr) {
		int minIndex = 0;

		for (int x = 1; x < arr.length; x++) {
			if (arr[x] < arr[minIndex])
				minIndex = x;
		}

		return minIndex;
	}

	public static int maxIndex(int... arr) {
		int maxIndex = 0;

		for (int x = 1; x < arr.length; x++) {
			if (arr[x] > arr[maxIndex])
				maxIndex = x;
		}

		return maxIndex;
	}

	public static int maxIndex(double... arr) {
		int maxIndex = 0;

		for (int x = 1; x < arr.length; x++) {
			if (arr[x] > arr[maxIndex])
				maxIndex = x;
		}

		return maxIndex;
	}

	public static double square(double x) {
		return x * x;
	}

	public static double cubic(double x) {
		return x * x * x;
	}

	public static double[] normalize(double... probability) {
		double sum = 0;

		for (int i = 0; i < probability.length; ++i)
			sum += probability[i];

		if (sum == 0) {
			// log.info("the sum of probability = 0!");
			return null;
		}
		for (int i = 0; i < probability.length; ++i)
			probability[i] /= sum;
		return probability;
	}

	public static float[] normalize(float... probability) {
		double sum = 0;

		for (int i = 0; i < probability.length; ++i)
			sum += probability[i];

		if (sum == 0) {
			// log.info("the sum of probability = 0!");
			return null;
		}
		for (int i = 0; i < probability.length; ++i)
			probability[i] /= sum;
		return probability;
	}

	public static String getStringCellValue(Row row, int labelIndex) {
		Cell cell = row.getCell(labelIndex);
		if (cell == null)
			return null;
		String type = row.getCell(labelIndex).getStringCellValue();
		type = type.trim();
		if (type.length() == 0)
			return null;
		return type;
	}

	static public String[] split(String o) {
		if (o == null)
			return null;
		return o.split(",\\s+");
	}

	public static boolean salient_informant(int vec[]) {
		for (int i = 0; i < vec.length; ++i) {
			for (int j = i + 1; j < vec.length; ++j) {
				if (vec[i] == 0 && vec[j] >= 3 || vec[j] == 0 && vec[i] >= 3
						|| vec[i] * vec[j] != 0 && (vec[i] * vec[i] + vec[j] * vec[j]) / (vec[i] * vec[j]) >= 4)
					return true;
			}
		}
		return false;
	}

	// post-condition: return a value in the range of [0, length]; the value
	// returned is no less than _Val;
	public static <_Ty> int binary_search(Vector<_Ty> arr, _Ty value, Comparator<_Ty> comparator) {
		int begin = 0, end = arr.size();
		for (;;) {
			int mid = (begin + end) >> 1;
			if (begin == end)
				return mid;
			int ret = comparator.compare(arr.get(mid), value);
			if (ret < 0)
				begin = mid + 1;
			else if (ret > 0)
				end = mid;
			else
				return mid;
		}
	}

	// post-condition: return a value in the range of [0, length]; the value
	// returned is no less than _Val;
	public static <_Ty> int binary_search(_Ty[] arr, _Ty value, Comparator<_Ty> comparator) {
		int begin = 0, end = arr.length;
		for (;;) {
			int mid = (begin + end) >> 1;
			if (begin == end)
				return mid;
			int ret = comparator.compare(arr[mid], value);
			if (ret < 0)
				begin = mid + 1;
			else if (ret > 0)
				end = mid;
			else
				return mid;
		}
	}

	static public <T> int indexOf(T[] elementData, T o) {
		return indexOf(elementData, o, 0);
	}

	static public <T> boolean contains(T[] elementData, T o) {
		return indexOf(elementData, o) >= 0;
	}

	static public <T> int indexOf(T[] elementData, T o, int index) {
		if (o == null) {
			for (int i = index; i < elementData.length; i++)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = index; i < elementData.length; i++)
				if (o.equals(elementData[i]))
					return i;
		}
		return -1;
	}

	// a b c d
	// a b y d
	// e b c d
	// e b y d
	// e f c d
	// e f y d
	// z y d
	// x y d
	// d y d

	static public void test_collocation() {
		String[] s0 = { "a", "e", "b", "f", "c", "z", "x", "d", "x", "f", "y", "d", "y", "e" };
		String[] s1 = { "d", "x", "z", "x", "e", "f", "a", "y", "b", "y", "d", "f", "c", "d" };

		String[] s2 = { "y", "z", "f", "e", "f", "b", "t", "b", "d", "c", "w", "x", "z", "x" };

		Vector<String> v0 = toVector(s0);
		Vector<String> v1 = toVector(s1);
		Vector<String> v2 = toVector(s2);
		HashSet<Vector<String>> set0 = collocate(v0, v1);
		HashSet<Vector<String>> set1 = collocate(v1, v2);
		HashSet<Vector<String>> set2 = collocate(v2, v0);
		log.info(set0);
		log.info(set1);
		log.info(set2);
		for (Vector<String> criteria : set0) {
			if (!Utility.containsAll(v0, criteria)) {
				log.info(criteria + "does not exist in " + v0);
			}
			if (!Utility.containsAll(v1, criteria)) {
				log.info(criteria + "does not exist in " + v1);
			}
		}

		for (Vector<String> criteria : set1) {
			if (!Utility.containsAll(v2, criteria)) {
				log.info(criteria + "does not exist in " + v2);
			}
			if (!Utility.containsAll(v1, criteria)) {
				log.info(criteria + "does not exist in " + v1);
			}
		}

		for (Vector<String> criteria : set2) {
			if (!Utility.containsAll(v2, criteria)) {
				log.info(criteria + "does not exist in " + v2);
			}
			if (!Utility.containsAll(v0, criteria)) {
				log.info(criteria + "does not exist in " + v0);
			}
		}

		HashSet<Vector<String>> dif0 = asymmetric_set_difference(set1, set0);
		HashSet<Vector<String>> dif1 = asymmetric_set_difference(set2, set1);
		HashSet<Vector<String>> dif2 = asymmetric_set_difference(set0, set2);
		for (Vector<String> criteria : dif0) {
			if (Utility.containsAll(v0, criteria)) {
				log.info(criteria + "does exist in " + v0);
			}
			if (!Utility.containsAll(v2, criteria)) {
				log.info(criteria + "does not exist in " + v2);
			}
			if (!Utility.containsAll(v1, criteria)) {
				log.info(criteria + "does not exist in " + v1);
			}
		}

		for (Vector<String> criteria : dif1) {
			if (Utility.containsAll(v1, criteria)) {
				log.info(criteria + "does exist in " + v1);
			}
			if (!Utility.containsAll(v0, criteria)) {
				log.info(criteria + "does not exist in " + v0);
			}
			if (!Utility.containsAll(v2, criteria)) {
				log.info(criteria + "does not exist in " + v2);
			}
		}
		for (Vector<String> criteria : dif2) {
			if (Utility.containsAll(v2, criteria)) {
				log.info(criteria + "does exist in " + v2);
			}
			if (!Utility.containsAll(v0, criteria)) {
				log.info(criteria + "does not exist in " + v0);
			}
			if (!Utility.containsAll(v1, criteria)) {
				log.info(criteria + "does not exist in " + v1);
			}
		}

	}

	static public <T> HashSet<T> asymmetric_set_difference(HashSet<T> o1, HashSet<T> o2) {
		HashSet<T> set = (HashSet<T>) o1.clone();
		set.removeAll(o2);
		return set;
	}

	static public <String> Vector<String> toVector(String[] str) {
		Vector<String> v = new Vector<String>(str.length);
		// v.setSize(str.length);
		for (String ch : str) {
			v.add(ch);
		}
		return v;
	}

	static public Vector<Integer> toVector(int[] str) {
		Vector<Integer> v = new Vector<Integer>(str.length);
		// v.setSize(str.length);
		for (Integer ch : str) {
			v.add(ch);
		}
		return v;
	}

	static public <String> HashSet<String> toHashSet(String[] str) {
		HashSet<String> v = new HashSet<String>();
		for (String ch : str) {
			v.add(ch);
		}
		return v;
	}

	static public <String> TreeSet<String> toTreeSet(String[] str) {
		TreeSet<String> v = new TreeSet<String>();
		for (String ch : str) {
			v.add(ch);
		}
		return v;
	}

	static public HashSet<Vector<String>> collocate(Vector<String> s0, Vector<String> s1) {
		Collocation collocation = new Collocation(s0, s1);
		// log.info("analyzing: ");
		// log.info(s0);
		// log.info(s1);
		collocation.clear();
		collocation.collocation(0, 0);
		return (HashSet<Vector<String>>) collocation.set.clone();
	}

	static public boolean containsAll(Vector<String> source, Vector<String> criteria) {
		Collocation Collocation = new Collocation(source, criteria);
		return Collocation.containsAll(0, 0);
	}

	static public class Collocation {
		Collocation(Vector<String> source, Vector<String> criteria) {
			this.criteria = criteria;
			this.source = source;
			arr = new Vector[criteria.size()];
			for (int i = 0; i < criteria.size(); ++i) {
				String lexeme = criteria.get(i);
				arr[i] = new Vector<Integer>();
				for (int j = 0; j < source.size(); ++j) {
					j = source.indexOf(lexeme, j);
					if (j < 0)
						break;
					arr[i].add(j);
				}
			}
		}

		void print() {
			for (Vector<String> str : set) {
				// for (String s : str)
				// System.out.print(s + " ");
				System.out.println(str);
			}
		}

		static final int LENGTH = 3;
		// the maximum positional difference between two consecutive lexemes,
		// i.e., there will be maximumly INTERVAL - 1 elements in between.
		final static int INTERVAL = 2;
		Vector<String> criteria, source;
		Vector<Integer> arr[];

		final static HashSet<Vector<String>> set = new HashSet<Vector<String>>();
		final static Vector<String> collector = new Vector<String>();

		void clear() {
			set.clear();
			collector.clear();
		}

		void collocation(int i, int j) {
			if (collector.size() > 1) {// possible error, the modification of
										// collector will also affect the
										// content of the hash set!!
				set.add((Vector<String>) collector.clone());
			}

			if (collector.size() >= LENGTH)
				return;

			int endi = criteria.size();
			if (collector.size() != 0)
				endi = Math.min(i + INTERVAL, endi);

			for (int _i = i; _i < endi; ++_i) {
				Vector<Integer> vec = arr[_i];
				if (vec.size() == 0)
					continue;

				for (int _j = j;;) {
					int index = Utility.binary_search(vec, _j, new Comparator<Integer>() {
						public int compare(Integer x, Integer y) {
							return Integer.compare(x, y);
						}
					});

					if (index == vec.size())
						break;

					_j = vec.get(index);
					assert (_j >= j);
					if (j != 0 && _j >= j + INTERVAL)
						break;
					collector.add(criteria.get(_i));

					collocation(_i + 1, ++_j);
					collector.setSize(collector.size() - 1);
				}
			}
		}

		boolean containsAll(int i, int j) {
			for (int _i = i; _i < criteria.size(); ++_i) {
				Vector<Integer> vec = arr[_i];
				if (vec.size() == 0)
					return false;

				for (int _j = j;;) {
					int index = Utility.binary_search(vec, _j, new Comparator<Integer>() {
						public int compare(Integer x, Integer y) {
							return Integer.compare(x, y);
						}
					});

					if (index == vec.size())
						break;

					_j = vec.get(index);
					assert (_j >= j);
					if (j != 0 && _j >= j + INTERVAL)
						break;

					if (containsAll(_i + 1, ++_j))
						return true;
				}
				return false;
			}
			return true;
		}
	}

	// get the set from its sting equivalent
	static public String[] get_lexeme_set(String COLLOCATION) {
		return COLLOCATION.substring(1, COLLOCATION.length() - 1).split(", ");
	}

	static public class Couplet<_Kty, _Ty> implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		static public class Integer implements Serializable {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Integer() {
				this(0, 0);
			}

			public Integer(int x, int y) {
				this.x = x;
				this.y = y;
			}

			public int x, y;
		}

		static public class Double implements Serializable {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Double() {
				this(0, 0);
			}

			public Double(double x, double y) {
				this.x = x;
				this.y = y;
			}

			public double x, y;
		}

		public Couplet(Couplet<_Kty, _Ty> rhs) {// copy constructor;
			x = rhs.x;
			y = rhs.y;
		}

		public Couplet(_Kty x, _Ty y) {
			this.x = x;
			this.y = y;
		}

		public Couplet() {
		}

		public void set(Couplet<_Kty, _Ty> rhs) {
			x = rhs.x;
			y = rhs.y;
		}

		public void set(_Kty x, _Ty y) {
			this.x = x;
			this.y = y;
		}

		public void swap(Couplet<_Kty, _Ty> rhs) {
			Couplet<_Kty, _Ty> tmp = new Couplet<_Kty, _Ty>(this);
			set(rhs);
			rhs.set(tmp);
		}

		public _Kty x;
		public _Ty y;

		public String toString() {
			String str = "";

			if (x instanceof String[]) {
				str += Utility.toString((String[]) x);
				str += "\n";
			} else
				str += x;
			str += "->";
			if (y instanceof String[]) {
				str += Utility.toString((String[]) y);
				str += "\n";
			} else
				str += y;
			return str;
		}

	}

	// // symmetric pairs;
	// static public class Pair<_Ty> extends Couplet<_Ty, _Ty> {
	// public Pair(_Ty x, _Ty y) {
	// super(x, y);
	// }
	//
	// public int hashCode() {
	// return x.hashCode() + y.hashCode();
	// }
	//
	// public boolean equals(Object anObject) {
	// if (this == anObject) {
	// return true;
	// }f
	// if (anObject instanceof Pair) {
	// Pair autre = (Pair) anObject;
	//
	// return x.equals(autre.x) && y.equals(autre.y)
	// || x.equals(autre.y) && y.equals(autre.x);
	// }
	// return false;
	// }
	// }
	//
	public static Vector<HashSet<String>> collocate(HashSet<String> a, HashSet<String> b) {
		HashSet<String> set = intersect(a, b);
		Vector<HashSet<String>> v = new Vector<HashSet<String>>();

		if (set.size() >= 2)
			combination(set, 2, v);
		if (set.size() >= 3)
			combination(set, 3, v);

		return v;
	}

	public static <T> TreeSet<T> intersect(TreeSet<T> a, TreeSet<T> b) {
		TreeSet<T> ret = (TreeSet<T>) a.clone();
		ret.retainAll(b);
		// to ensure the same order of the elements in the hash set!
		return ret;
	}

	public static <T> HashSet<T> intersect(HashSet<T> a, HashSet<T> b) {
		HashSet<T> ret = (HashSet<T>) a.clone();
		ret.retainAll(b);
		// to ensure the same order of the elements in the hash set!
		return ret;
	}

	// A BETTER SOLUTION SHOULD COME UP.
	public static <T> double similarity(HashSet<T> a, HashSet<T> b) {
		double mutual = intersect(a, b).size();
		return mutual / (a.size() + b.size() - mutual);
	}

	public static final int CHAR_ENGLISH = 0;
	public static final int CHAR_CHINESE = 1;
	public static final int CHAR_DIGIT = 2;
	public static final int CHAR_PUNCTUATION = 3;
	public static final int CHAR_QUANTIFIER = 4;
	public static final int CHAR_SPECIAL = 5;

	public static int chartype(char ch) {
		if (isEnglish(ch))
			return CHAR_ENGLISH;
		else if (Character.isDigit(ch))
			return CHAR_DIGIT;
		else if (EnglishPunctuation.indexOf(ch) >= 0 || ChinesePunctuation.indexOf(ch) >= 0)
			return CHAR_PUNCTUATION;
		else if (EnglishSpecial.indexOf(ch) >= 0 || ChineseSpecial.indexOf(ch) >= 0)
			return CHAR_SPECIAL;
		else if (EnglishQuantifier.indexOf(ch) >= 0 || ChineseQuantifier.indexOf(ch) >= 0)
			return CHAR_QUANTIFIER;
		else
			return CHAR_CHINESE;
	}

	public static Vector<String> splitCombination(String lexeme) {
		Vector<String> str = new Vector<String>();
		int i = 0;
		do {
			int beg = i;

			int type = chartype(lexeme.charAt(i));

			do {
				for (++i; i < lexeme.length() && chartype(lexeme.charAt(i)) == type; ++i)
					;
			} while (i < lexeme.length() - 1 && (lexeme.charAt(i) == '.' || lexeme.charAt(i) == '-')
					&& chartype(lexeme.charAt(i + 1)) == type && (type == CHAR_ENGLISH || type == CHAR_DIGIT));

			// if (type != CHAR_SPECIAL)
			str.add(lexeme.substring(beg, i));
		} while (i < lexeme.length());

		return str;
	}

	public static boolean isEnglish(char ch) {
		return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
	}

	boolean contain(String str, String criteria) {
		return str.matches(criteria);
	}

	// remove tailing punctuations;
	// refer to Orielly.Mastering.Regular.Expressions.pdf for details!
	// Basic Unicode Properties
	// \p{P} \p{Punctuation} Punctuation characters.
	// \p{S} \p{Symbol} Various types of Dingbats and symbols.
	public static String trim(String msgContent) throws Exception {
		if (msgContent.matches("[\\pP\\pS\\s]*")) {
			return null;
		}
		String[] res = Utility.regexSingleton(msgContent, "[\\pP\\pS\\s]*([^\\pP\\pS\\s][\\s\\S]*)");
		msgContent = res[1];
		res = Utility.regexSingleton(msgContent, "([\\s\\S]*[^\\pP\\pS\\s])[\\pP\\pS\\s]*");
		msgContent = res[1];
		return msgContent;
	}

	public static String[] trim(String str[]) throws Exception {
		for (int i = 0; i < str.length; i++) {
			str[i] = str[i].trim();
		}
		return str;
	}

	public static void excel() throws Exception {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		String myDB = "jdbc:odbc:Driver={Microsoft Excel Driver (*.xls)};DBQ=d:/example.xlsx;"
				+ "DriverID=22;READONLY=false";
		Connection con = DriverManager.getConnection(myDB, "", "");
		// con.prepareStatement("select * from [Sheet1$]");
		Statement stmt = con.createStatement();
		String excelQuery = "select * from [Sheet1$]";
		ResultSet result = stmt.executeQuery(excelQuery);
		while (result.next()) {

		}
	}

	// https://www.javacodegeeks.com/2012/11/java-regular-expression-tutorial-with-examples.html
	public static void test_regular_exp() throws Exception {
		// In the first example, at runtime first capturing group is (\w\d)
		// which evaluates to “a2″ when matched with the input String “a2a2″ and
		// saved in memory. So \1 is referring to “a2″ and hence it returns
		// true. Due to same reason second statement prints false.
		// Try to understand this scenario for statement 3 and 4 yourself.
		// String content = "这个\\\\\\'航'班目前\\\\'看到是''正点航班的状态";
		// System.out.println(content);
		// System.out.println(remove_apostrophe(content));
		// "http://([^\"]+(?:df|gif|png|bmp|jpeg|JPG|GIF|PNG|BMP|JPEG|mp3|mp4))"

		String sentence = "anallagmatic";
		String regex = "^(?!.*(.)(.)\2\1)";
		if (sentence.matches(regex)) {
			System.out.println("matches");
		} else {
			System.out.println("not");
		}

		String content = "<div class='sevice_chat'><div class='chat_box'><div class='chat_01'><span class='chat_title'><sender>y</sender>&nbsp;&nbsp;&nbsp;&nbsp;<time>2016-09-05 11:59:33</time></span><br><span><content><span style='font-size:12px;font-weight:normal;font-style:normal;text-decoration:none;line-height:12px;font-family:宋体'>haha </span><br>这条消息已经被撤回了</content></span><div style='display:none;'><receiver>muc28c50b7e@conference.openfire-test</receiver></div></div></div></div><div class='msg_back_success clearfix'>该消息已被撤回</div><div class='sevice_chat'><div class='chat_box'><div class='chat_01'><span class='chat_title'><sender>y</sender>&nbsp;&nbsp;&nbsp;&nbsp;<time>2016-09-05 11:59:34</time></span><br><span><content><span style='font-size:12px;font-weight:normal;font-style:normal;text-decoration:none;line-height:12px;font-family:宋体'>heihei</span><br></content></span><div style='display:none;'><receiver>muc28c50b7e@conference.openfire-test</receiver></div></div></div></div><div class='sevice_chat'><div class='chat_box'><div class='chat_01'><span class='chat_title'><sender>y</sender>&nbsp;&nbsp;&nbsp;&nbsp;<time>2016-09-05 11:59:39</time></span><br><span><content><span style='font-size:12px;font-weight:normal;font-style:normal;text-decoration:none;line-height:12px;font-family:宋体'>123</span><br>这条消息已经被撤回了</content></span><div style='display:none;'><receiver>muc28c50b7e@conference.openfire-test</receiver></div></div></div></div><div class='msg_back_success clearfix'>该消息已被撤回</div>";
		// String regex = "<div class='(\\S+)'>" + "<div class='(\\S+)'>" + "<div
		// class='(\\S+)'>" + "<span class='(\\S+)'>" + "<sender>([\\s\\S]+?)</sender>"
		// + "(&nbsp;)*" +
		// "<time>(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})</time></span><br>" +
		// "<span><content>([\\s\\S]*?)</content></span>" + "<div
		// style='display:none;'><receiver>(\\S+)</receiver>" + "(</div>){4}" + "(<div
		// class='msg_back_success clearfix'>[\\s\\S]*?</div>)?";
		// + "(?(?=<div class='msg_back_success clearfix'>)该消息已被撤回</div>)";
		//

		// Pattern.compile(regex).matcher("")
		for (String[] str : Utility.regex(content, regex)) {
			System.out.println();
			// System.out.println();
			for (int i = 0; i < str.length; ++i) {
				System.out.println(str[i]);
			}
			// System.out.println(str[1]);
			// System.out.println(str[2]);
			// System.out.println(str[3]);
			// System.out.println(str[4]);
			// System.out.println(str[7]);
			// System.out.println(Utility.format(str[8]));
			System.out.println();
		}

		if (true)
			return;
		if ("2(--0...".matches("2[^\\(\\)]+")) {
			System.out.println("matches");
		} else {
			System.out.println("not");
		}

		// System.out.println("p.y.length = " + p.y.length);
		// for (String str : p.y) {
		// System.out.println("str.length() = " + str.length());
		// System.out.println(Utility.toString(str));
		// }
		//
		if (true)
			return;

		String input = "aaaaaaaa";
		regex = "aa";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		int from = 0;
		int count = 0;
		while (matcher.find(from)) {
			count++;
			from = matcher.start() + 1;
		}
		System.out.println(count);
		if (true)
			return;

		String str = "asdf3338 . 0888kjl3339 . 348kjl";

		pattern = Pattern.compile("\\d+( \\. )\\d+");
		matcher = pattern.matcher(str);
		String tmp = "";
		int beginIndex = 0;
		while (matcher.find()) {
			tmp += str.substring(beginIndex, matcher.start());
			tmp += matcher.group().replace(matcher.group(1), ".");

			System.out.println("Found the text \'" + matcher.group() + "\' starting at " + matcher.start()
					+ " index and ending at index " + matcher.end());

			System.out.println("matcher.group(1) = \'" + matcher.group(1) + "\'");

			System.out.println("tmp = " + tmp);
			beginIndex = matcher.end();
		}

		str = Utility.sPunctuation;
		System.out.println(str); // true
		tmp = str.replaceAll("[" + Utility.sPunctuation.replace("[", "\\[").replace("]", "\\]") + "]", ",");
		System.out.println(tmp); // true
		System.out.println("tmp.length() = " + tmp.length()); // true
		System.out.println("str.length() = " + str.length()); // true

		System.out.println(Pattern.matches("(\\w\\d)\\1", "a2a2")); // true

		System.out.println(Pattern.matches("(\\w\\d)\\1", "a2b2")); // false

		System.out.println(Pattern.matches("(AB)(B\\d)\\2\\1", "ABB2B2AB")); // true
		System.out.println(Pattern.matches("(AB)(B\\d)\\2\\1", "ABB2B3AB")); // false

		pattern = Pattern.compile("ab", Pattern.CASE_INSENSITIVE);

		matcher = pattern.matcher("ABcabdAb");
		// using Matcher find(), group(), start() and end() methods

		while (matcher.find()) {

			System.out.println("Found the text \'" + matcher.group()

					+ "\' starting at " + matcher.start()

					+ " index and ending at index " + matcher.end());
		}

		// using Pattern split() method

		pattern = Pattern.compile("\\W");
		String[] words = pattern.split("one@two#three:four$five");

		for (String s : words) {
			System.out.println("Split using Pattern.split(): " + s);
		}

		// using Matcher.replaceFirst() and replaceAll() methods
		pattern = Pattern.compile("1*2");
		matcher = pattern.matcher("11234512678");

		System.out.println("Using replaceAll: " + matcher.replaceAll("_"));

		System.out.println("Using replaceFirst: " + matcher.replaceFirst("_"));
		// Matcher matcher = Pattern.compile("<P.*?>([\\s\\S]*?)</P>").matcher(
		// msgContent);
		// if (matcher.find()) {
		// msgContent = matcher.group(1);
		// }
		// Pattern spanPattern =
		// Pattern.compile("<span.*?>([\\s\\S]*?)</span>");
		// Pattern SPANPattern =
		// Pattern.compile("<SPAN.*?>([\\s\\S]*?)</SPAN>");
		// Pattern APattern = Pattern.compile("<A.*?>([\\s\\S]*?)</A>");
		//
		// Matcher span = spanPattern.matcher(msgContent);
		//
		// String ret = "";
		// while (span.find()) {
		// String prefix = "";
		// String postfix = "";
		//
		// String strContent = span.group(1);
		//
		// Matcher SPAN = SPANPattern.matcher(strContent);
		// if (SPAN.find()) {
		// prefix += strContent.substring(0, strContent.indexOf("<SPAN"));
		// postfix = strContent.substring(strContent.indexOf("</SPAN>")
		// + "</SPAN>".length())
		// + postfix;
		// strContent = SPAN.group(1);
		// }
		//
		// Matcher A = APattern.matcher(strContent);
		// if (A.find()) {
		// prefix += strContent.substring(0, strContent.indexOf("<A"));
		// postfix = strContent.substring(strContent.indexOf("</A>")
		// + "</A>".length())
		// + postfix;
		// strContent = A.group(1);
		// }
		// ret += (prefix + strContent + postfix).trim();
		// }
	}

	public static void test_integers() {
		int[][] a = combination(6, 1);
		for (int[] v : a) {
			for (int b : v) {
				System.out.printf("%4d", b);
			}
			System.out.println();
		}
	}

	// precondition: i >= 1;
	public static double pow(double x, int i) {
		if (i == 0)
			return 1;
		if (i == 1)
			return x;
		if ((i & 1) == 1)
			return pow(x * x, i >> 1) * x;
		return pow(x * x, i >> 1);
	}

	public static long pow(long x, int i) {
		if (i == 0)
			return 1;
		if (i == 1)
			return x;
		if ((i & 1) == 1)
			return pow(x * x, i >> 1) * x;
		return pow(x * x, i >> 1);
	}

	public static <_Ty> void swap(_Ty a[], int i, int j) {
		_Ty tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}

	public static void swap(int a[], int i, int j) {
		int tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}

	public static void swap(double a[], int i, int j) {
		double tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}

	public static <_Ty> void swap(List<_Ty> a, int i, int j) {
		_Ty tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}

	public static <T> void shuffle(T[] array) {
		Random rnd = new Random();
		for (int i = array.length; i > 1; i--)
			swap(array, i - 1, rnd.nextInt(i));
	}

	public static <T> void shuffle(List<T> array) {
		Random rnd = new Random();
		for (int i = array.size(); i > 1; i--)
			swap(array, i - 1, rnd.nextInt(i));
	}

	public static void shuffle(int[] array) {
		Random rnd = new Random();
		for (int i = array.length; i > 1; i--)
			swap(array, i - 1, rnd.nextInt(i));
	}

	public static void shuffle(double[] array) {
		Random rnd = new Random();
		for (int i = array.length; i > 1; i--)
			swap(array, i - 1, rnd.nextInt(i));
	}

	public static void etude() {
		System.out.printf("now we test Euclidean algorithm, while loops versus recursion, which is faster??\n");

		int k = 3;
		long begin = System.currentTimeMillis();

		for (int i = 0; i < 100000000; ++i)
			getGCD((1 << 30) * i + 2, (1 << 24) + (1 << k));

		long end = System.currentTimeMillis();
		System.out.printf("time duration for MultiNestTool.getGCD is: %d millisecond(s)\n", end - begin);

		begin = System.currentTimeMillis();

		for (int i = 0; i < 100000000; ++i)
			gcd((1 << 30) * i + 2, (1 << 24) + (1 << k));

		end = System.currentTimeMillis();
		System.out.printf("time duration for gcd is: %d millisecond(s)\n", end - begin);

		System.out.printf("now set the frist parameter to zero\n");
		getGCD(0, 2);
		gcd(0, 2);
	}

	static int getGCD(int a, int b) {
		// if all else fails
		int result = 1;

		if (a < b) {
			int tmp = a;

			a = b;
			b = tmp;
		}

		for (;;) {
			int c = a % b;

			if (c == 0) {
				result = b;

				break;
			}

			a = b;
			b = c;
		}

		return result;
	}

	public static int gcd(int x, int y) {
		if (y == 0)
			return x;
		return gcd(y, x % y);
	}

	public static <_Ty> void erase(_Ty arr[], int i, int length) {
		_Ty tmp = arr[i];
		System.arraycopy(arr, i + 1, arr, i, --length - i);// length - 1 - (i +
															// 1) + 1
		arr[length] = tmp;
	}

	public static <_Ty> void erase(Vector<_Ty> arr, int i, int length) {
		_Ty tmp = arr.get(i);
		// System.arraycopy(arr, i + 1, arr, i, --length - i);//length - 1 - (i
		// + 1) + 1
		--length;
		for (int j = i; j < length; ++j)
			arr.set(j, arr.get(j + 1));
		arr.set(length, tmp);
	}

	public static void erase(int arr[], int i, int length) {
		int tmp = arr[i];
		System.arraycopy(arr, i + 1, arr, i, --length - i);// length - 1 - (i +
															// 1) + 1
		arr[length] = tmp;
	}

	// post-condition: the length of the array is incremented;
	public static <_Ty> void insert(_Ty arr[], int i, int length, _Ty _Val) {
		for (int k = length; k > i; --k)
			arr[k] = arr[k - 1];
		arr[i] = _Val;
	}

	public static <_Ty> void insert(Vector<_Ty> arr, int i, int length, _Ty _Val) {
		for (int k = length; k > i; --k)
			arr.set(k, arr.get(k - 1));
		arr.set(i, _Val);
	}

	public static void insert(int arr[], int i, int length, int _Val) {
		for (int k = length; k > i; --k)
			arr[k] = arr[k - 1];
		arr[i] = _Val;
	}

	// S^2 = ${i = 0, n} (x[i] - ~x)^2 / n; where ~x = ${i = 0, n} x[i] / n;
	// also, S^2 = ${i = 0, n} x[i]^2 / n - (~x)^2;
	// also, delta^2 = S^2 * n / (n - 1);
	public static double std_deviation(double arr[]) {
		double ave = 0;
		int n = arr.length;
		for (int i = 0; i < n; ++i) {
			ave += arr[i];
		}
		ave /= n;

		double variance = 0;
		for (int i = 0; i < n; ++i) {
			variance += arr[i] * arr[i];
		}
		variance -= n * ave * ave;
		variance /= n - 1;

		return Math.sqrt(variance);
	}

	public static float std_deviation(float arr[]) {
		double copy[] = new double[arr.length];

		for (int i = 0; i < arr.length; ++i) {
			copy[i] = arr[i];
		}
		return (float) std_deviation(copy);
	}

	// below is the class for prime numbers.
	public static class Prime {
		// method for comparing two prime numbers.
		public boolean equ(Prime x) {
			return i == x.i;
		}

		public boolean less(Prime x) {
			return i < x.i;
		}

		// default prime number is 2;
		public Prime(int n) {
			if (v[size - 1] >= n) {
				int beg = 0, end = size, mid;
				for (;;) {
					assert (beg < end);
					mid = (beg + end) >> 1;
					if (v[mid] < n) {
						beg = mid + 1;
						continue;
					}

					if (v[mid] > n) {
						end = mid;
						// continue;
					} else {
						i = mid;
						break;
					}
				}
			} else {
				while (v[size - 1] < n)
					expand();
				assert (v[size - 1] == n);
				i = size - 1;
			}
		}

		// convert a prime number to a double word (<= 2^32);
		public int value() {
			return v[i];
		}

		// get the next prime number;
		public Prime next() {
			++i;
			if (i == size)
				expand();
			return this;
		}

		// get the previous prime number;
		public Prime previous() {
			--i;
			return this;
		}

		// the index for the prime number in the prime array;
		private int i;

		// check whether an odd number is a prime or a composite;
		static boolean primality(int n) {
			for (int i = 1; v[i] <= (int) Math.sqrt(n); ++i)
				if (n % v[i] == 0)
					return false;// is a composite number, return false;
			return true;
		}

		// if the present knowledge of our prime numbers is not enough, we need
		// to expand it.//?
		static void expand() {
			for (int n = v[size - 1] + 2;; n += 2) {
				if (primality(n)) {
					if (size == v.length) {
						int v[] = new int[Prime.v.length << 1]; // double the
																// length of the
																// current
																// array;
						System.arraycopy(Prime.v, 0, v, 0, Prime.v.length);

						Prime.v = v;
					}

					v[size] = n;
					++size; // push the data at the back of the array;
					break;
				}
			}
		}

		static int v[] = { 2, 3 };
		static int size = 2;

		public static void test() {
			long begin = System.currentTimeMillis();

			int i = 0;
			for (Prime x = new Prime(2); x.value() < 100; x = x.next()) {
				++i;
				System.out.printf("%7d\t", x.value());
				if (i % 10 == 0)
					System.out.println();
			}

			long end = System.currentTimeMillis();
			System.out.printf("\ntime duration for prime algorithm is: %d millisecond(s)\n", end - begin);
		}
	}

	public static double summation_cmb(double width[], int c[]) {
		double sum = 0;
		for (int d : c) {
			sum += width[d];
		}
		return sum;
	}

	public static int cmb(int x, int n) {
		int pdt = 1;
		for (int i = 1; i <= n; ++i) {
			pdt *= x - n + i;
			pdt /= i;
		}
		return pdt;
	}

	// return a combination of k elements selected among {0, 1, 2, n - 2, n -
	// 1};
	public static int[][] combination(int n, int k) {
		assert n >= k && k > 0 : "n >= k && k > 0";
		int v[][] = new int[cmb(n, k)][];
		int x[] = new int[k];
		int index = 0;

		int i = 0;
		do {
			if (x[i] <= n - (k - i)) {
				if (i == k - 1) {
					v[index++] = x.clone();
				} else {
					++i;
					x[i] = x[i - 1];
				}
				++x[i];
			} else {
				--i;
				++x[i]; // backtracking to the previous index.
			}
		} while (x[0] <= n - k);
		return v;
	}

	public static void combination(HashSet<String> set, int k, Vector<HashSet<String>> v) {
		int n = set.size();
		String[] array = new String[n];
		int i = 0;
		for (String s : set) {
			array[i++] = s;
		}

		for (int[] combination : combination(n, k)) {
			HashSet<String> s = new HashSet<String>();
			for (int index : combination) {
				s.add(array[index]);
			}
			v.add(s);
		}
	}

	public static Workbook read_excel(String fileName) throws Exception {
		System.out.println("excel file processed: " + fileName);
		InputStream input = new FileInputStream(fileName); // 建立输入流
		Workbook wb;
		if (fileName.endsWith("xlsx"))
			// xmlbeans-2.6.0.jar dom4j-1.6.1.JAR must be imported!
			wb = new XSSFWorkbook(input);
		else
			wb = new HSSFWorkbook(input);
		return wb;
	}

	public static Workbook createWorkbook(boolean xlsx) throws Exception {
		Workbook wb;
		if (xlsx)
			// xmlbeans-2.6.0.jar dom4j-1.6.1.JAR must be imported!
			wb = new XSSFWorkbook();
		else
			wb = new HSSFWorkbook();
		return wb;
	}

	// static public Sheet createSheet(String fileName, String sheetName) throws
	// Exception{
	// return createSheet(Utility.read_excel(fileName), sheetName);
	// }

	static public Sheet createSheet(Workbook wb, String sheetName) throws Exception {
		if (wb.getSheet(sheetName) != null) {
			wb.removeSheetAt(wb.getSheetIndex(sheetName));
		}

		return wb.createSheet(sheetName);
	}

	public static Sheet read_excel(String fileName, String name) throws Exception {
		Workbook wb = read_excel(fileName);
		Sheet sheet = wb.getSheet(name);
		if (sheet == null)
			return wb.getSheetAt(0);
		return sheet;
	}

	public static void write_excel(Workbook wb, String fileName) throws Exception {
		wb.write(new FileOutputStream(fileName));
	}

	public static Date parseDateFormat(final String strDate) throws ParseException {
		return parseDateFormat(strDate, "yyyy-MM-dd HH:mm:ss");
	}

	public static Date parseDateFormat(final String strDate, final String pattern) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(pattern);
		return sdf.parse(strDate);
	}

	public static class Oracle {
		Connection con = null;

		public Oracle() throws Exception {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String url = "jdbc:oracle:thin:@192.168.11.201:1521:orcl";
			String user = "ucc73";// 用户名
			String password = "client";// 密码
			con = DriverManager.getConnection(url, user, password);
		}

		boolean execute(String sql) throws SQLException {
			boolean ret = false;
			PreparedStatement pre = con.prepareStatement(sql);
			ret = pre.execute();
			pre.close();
			return ret;
		}
	}

	static public String remove_apostrophe(String str) {
		return str.replace("\'", "\\\'").replaceAll("\\\\+", "\\\\");

	}

	/**
	 * this procedure evaluates the asymmetric set difference of clutter between (
	 * standard and strUnwanted). so mathematically, dif = clutter + supplement -
	 * (standard + strUnwanted);
	 * 
	 * @param clutter     , the keyword string set from the cluttered data from the
	 *                    source file where data are updated everyday.
	 * @param supplement  , the supplementary keywords from the target file
	 * @param standard    , the keyword string set from the standard target file
	 *                    which we are finally interested in, from which we can see
	 *                    the final result.
	 * @param strUnwanted , the unwanted keywords which the user has blocked from
	 *                    being used.
	 * @return the asymmetric set difference of the cluttered keywords set and those
	 *         from the target file.
	 */
	public static String asymmetric_set_difference(String clutter, String supplement, String standard,
			String strUnwanted) {
		HashSet<String> set = new HashSet<String>();
		for (String s : clutter.split(";")) {
			if (!s.equals(""))
				set.add(s);
		}

		for (String s : supplement.split(";")) {
			if (!s.equals(""))
				set.add(s);
		}

		for (String s : standard.split(";")) {
			if (!s.equals(""))
				set.remove(s);
		}

		for (String s : strUnwanted.split(";")) {
			if (!s.equals(""))
				set.remove(s);
		}

		String dif = set.toString().replaceAll(", ", ";");
		return dif.substring(1, dif.length() - 1);
	}

	static class NodeShadow extends Utility.Couplet<NodeShadow, NodeShadow> {
		// objects hold a formatted label string and the level,column
		// coordinates for a shadow tree node
		String value; // formatted node value
		int i, j;

		int max_width() {
			int width = length(value);
			if (x != null) {
				int width_x = x.max_width();
				if (width_x > width)
					width = width_x;
			}
			if (y != null) {
				int width_y = y.max_width();
				if (width_y > width)
					width = width_y;
			}
			return width;
		}

		void hierarchize() {
			hierarchize(0, 0);
		}

		void hierarchize(int level, int... column) {
			if (x != null)
				x.hierarchize(level + 1, column);
			// allocate node for left child at next level in tree; attach node
			i = level;
			j = column[0]++; // update column to next cell in the table

			if (y != null)
				y.hierarchize(level + 1, column);
		}

		// the font type should be simsun;
		public String toString() {
			String cout = "";
			int currLevel = 0;
			int currCol = 0;

			// build the shadow tree
			hierarchize();
			final int colWidth = max_width() + 1;

			// use during the level order scan of the shadow tree
			NodeShadow currNode;
			//
			// store siblings of each nodeShadow object in a queue so that they
			// are visited in order at the next level of the tree
			Queue<NodeShadow> q = new LinkedList<NodeShadow>();
			//
			// insert the root in the queue and set current level to 0
			q.add(this);
			//
			// continue the iterative process until the queue
			// is empty
			while (q.size() != 0) {
				// delete front node from queue and make it the
				// current node
				currNode = q.poll();

				if (currNode.i > currLevel) {
					// if level changes, output a newline
					currLevel = currNode.i;
					currCol = 0;
					cout += lineSeparator;
				}

				char ch;
				if (currNode.x != null) {
					q.add(currNode.x);
					int dif = colWidth - length(currNode.x.value);
					cout += Utility.toString((currNode.x.j - currCol) * colWidth + dif, ' ');
					cout += Utility.toString((currNode.j - currNode.x.j) * colWidth - dif, '_');

					ch = '_';
				} else {
					cout += Utility.toString((currNode.j - currCol) * colWidth, ' ');

					ch = ' ';
				}

				cout += Utility.toString(colWidth - length(currNode.value), ch) + currNode.value;

				currCol = currNode.j;
				if (currNode.y != null) {
					q.add(currNode.y);

					cout += Utility.toString((currNode.y.j - currCol) * colWidth, '_');

					currCol = currNode.y.j;
				}

				++currCol;
			}
			cout += lineSeparator;

			return cout;
		}
	}

	static class Node<_Ty> extends Utility.Couplet<Node<_Ty>, Node<_Ty>> {
		_Ty value;

		Node(_Ty value) {
			this.value = value;
		}

		Node(_Ty value, Node<_Ty> x, Node<_Ty> y) {
			super(x, y);
			this.value = value;
		}

		public NodeShadow buildShadowTree() {
			// recursive inorder scan used to build the shadow tree
			NodeShadow newNode = new NodeShadow();// create the new shadow
													// tree node
			if (x != null)
				newNode.x = x.buildShadowTree();
			// allocate node for left child at next level in tree;

			// initialize data members of the new node
			newNode.value = value.toString();
			if (y != null)
				newNode.y = y.buildShadowTree();

			return newNode;
		}
	}

	public static int length(String value) {
		int length = 0;
		for (int i = 0; i < value.length(); ++i) {
			char ch = value.charAt(i);
			if ((ch & 0xff80) != 0)
				length += 2;
			else
				++length;
		}
		return length;
	}

	public static class LNodeShadow extends Utility.Couplet<LNodeShadow[], LNodeShadow[]> {
		// objects hold a formatted label string and the level,column
		// coordinates for a shadow tree node
		String value; // formatted node value
		int i, j;
		public int max_width = -1;

		LNodeShadow() {
		}

		public LNodeShadow(String value) {
			this.value = value;
		}

		static int max_width(LNodeShadow[] list) {
			int length = 0;
			for (LNodeShadow x : list) {
				int width = x.max_width();
				if (width > length) {
					length = width;
				}
			}
			return length;
		}

		public int max_width() {
			int width = length(value);
			if (x != null) {
				int width_x = max_width(x);
				if (width_x > width)
					width = width_x;
			}
			if (y != null) {
				int width_y = max_width(y);
				if (width_y > width)
					width = width_y;
			}
			return width;
		}

		void hierarchize() {
			hierarchize(0, 0);
		}

		static void hierarchize(LNodeShadow list[], int level, int... column) {
			for (LNodeShadow x : list) {
				x.hierarchize(level, column);
			}
		}

		// static int size(List<LNodeShadow> list){
		// int size = 0;
		// for (LNodeShadow x : list){
		// size += x.size();
		// }
		// return size;
		// }
		//
		// int size(){
		// int size = 1;
		// if (x != null)
		// size += size(x);
		// if (y != null)
		// size += size(y);
		// return size;
		// }
		//
		// static int sizeInbeween(List<LNodeShadow> list){
		// int size = list.size();
		// if (size == 1)
		// return size;
		// int i = 0;
		// size += size(list.get(i).y);
		//
		// for (++i; i < list.size() - 1; ++i){
		// size += list.get(i).size();
		// }
		// size += size(list.get(i).x);
		// return size;
		// }
		//
		void hierarchize(int level, int... column) {
			if (x != null)
				hierarchize(x, level + 1, column);
			// allocate node for left child at next level in tree; attach node
			i = level;
			j = column[0]++; // update column to next cell in the table

			if (y != null)
				hierarchize(y, level + 1, column);
		}

		// the font type should be simsun;
		public String toString() {
			String cout = "";
			int currLevel = 0;
			int currCol = 0;

			// build the shadow tree
			hierarchize();
			// final int colWidth = Math.max(max_width, max_width()) + 1;
			final int colWidth = max_width;

			// use during the level order scan of the shadow tree
			LNodeShadow currNode;
			//
			// store siblings of each nodeShadow object in a queue so that they
			// are visited in order at the next level of the tree
			Queue<LNodeShadow> q = new LinkedList<LNodeShadow>();
			//
			// insert the root in the queue and set current level to 0
			q.add(this);
			//
			// continue the iterative process until the queue
			// is empty
			while (q.size() != 0) {
				// delete front node from queue and make it the
				// current node
				currNode = q.poll();

				if (currNode.i > currLevel) {
					// if level changes, output a newline
					currLevel = currNode.i;
					currCol = 0;
					cout += lineSeparator;
				}

				char ch;
				if (currNode.x != null) {
					assert currNode.x.length > 0;
					for (LNodeShadow t : currNode.x)
						q.add(t);
					LNodeShadow head = currNode.x[0];
					// the string is right-aligned / right-justified, that's why
					// there a series of leading ' ';
					int dif = colWidth - length(head.value);// for leading ' 's
					cout += Utility.toString((head.j - currCol) * colWidth + dif, ' ');
					cout += Utility.toString((currNode.j - head.j) * colWidth - dif, '_');

					ch = '_';
				} else {
					cout += Utility.toString((currNode.j - currCol) * colWidth, ' ');

					ch = ' ';
				}

				// for leading white spaces;
				cout += Utility.toString(colWidth - length(currNode.value), ch) + currNode.value;

				currCol = currNode.j;
				if (currNode.y != null) {
					for (LNodeShadow t : currNode.y)
						q.add(t);

					LNodeShadow last = currNode.y[currNode.y.length - 1];
					cout += Utility.toString((last.j - currCol) * colWidth, '_');

					currCol = last.j;
				}

				++currCol;
			}
			cout += lineSeparator;

			return cout;
		}
	}

	static class LNode<_Ty> extends Utility.Couplet<LNode<_Ty>[], LNode<_Ty>[]> {
		_Ty value;

		LNode(_Ty value) {
			this.value = value;
		}

		LNode(_Ty value, LNode<_Ty> x[], LNode<_Ty> y[]) {
			super(x, y);
			this.value = value;
		}

		public LNodeShadow buildShadowTree() {
			// recursive inorder scan used to build the shadow tree
			LNodeShadow newNode = new LNodeShadow();// create the new shadow
													// tree node
			if (x != null) {
				newNode.x = new LNodeShadow[x.length];
				for (int i = 0; i < x.length; ++i)
					newNode.x[i] = x[i].buildShadowTree();
			}
			// allocate node for left child at next level in tree;

			// initialize data members of the new node
			newNode.value = value.toString();
			if (y != null) {
				newNode.y = new LNodeShadow[y.length];
				for (int i = 0; i < y.length; ++i)
					newNode.y[i] = y[i].buildShadowTree();
			}

			return newNode;
		}
	}

	static public BufferedWriter[] classLabelFile(String path, String classname[]) throws IOException {
		// path += "/" + this.getClass().getSimpleName() + "/";
		File dir = new File(path);
		if (dir != null && !dir.exists()) {
			dir.mkdirs();
		}

		BufferedWriter[] writer = new BufferedWriter[classname.length + 1];
		int i;
		for (i = 0; i < classname.length; ++i) {
			writer[i] = new BufferedWriter(new FileWriter(path + classname[i] + ".data"));
		}

		writer[i] = new BufferedWriter(new FileWriter(path + "untrained.txt"));

		return writer;
	}

	static public void generateTrainingFile() throws Exception {
		String classname[] = { "EVENT", "WHEN", "WHERE", "WHY", "HOW", "WHETHER", "CONTACT", "PRODUCT", "PEOPLE",
				"ATTRIBUTE", "MONEY", "CONTENT", "ATTITUDE", "GREETING" };

		BufferedWriter BufferedWriter[] = classLabelFile("models/topic/", classname);
		Workbook wb = read_excel("models/thesaurus.xlsx");

		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> rows = sheet.rowIterator();
		Utility.skipLinePremiere(rows);

		// read the first row to determine the initial position of the
		// question.
		int labelIndex = 5;
		while (rows.hasNext()) {
			Row row = rows.next();

			int index;
			String type = getStringCellValue(row, labelIndex);
			if (type == null)
				index = classname.length;
			else {
				index = Utility.indexOf(classname, type);
				if (index < 0) {
					log.info("type = " + type);
				}
				assert index >= 0 && index < classname.length;
			}

			String str = row.getCell(0).getStringCellValue();

			BufferedWriter[index].write(str);
			BufferedWriter[index].newLine();
		}

		for (BufferedWriter Writer : BufferedWriter) {
			Writer.close();
		}
	}

	static public void regex(String path, String regex, String mistaken, String correct) throws IOException {
		ArrayList<String> delete = new ArrayList<String>();
		ArrayList<String> arr = new ArrayList<String>();
		for (String s : new Text(path + "\\" + mistaken + ".data")) {
			if (s.matches(".*" + regex + ".*")) {
				arr.add(s + "\t" + correct + "\t" + mistaken);
				delete.add(s);

				log.info(s + "\t" + correct + "\t" + mistaken);
			}
		}

		Utility.removeString(path + "\\" + mistaken + ".data", delete);
		Utility.appendString(path + "\\" + "UNKNOWN.data", arr);
	}

	static public void replace(String file, String old, String replacement) throws IOException {
		ArrayList<String> arr = new ArrayList<String>();
		for (String s : new Text(file)) {
			if (s.contains(old))
				s = s.replace(old, replacement);
			arr.add(s);
		}

		Utility.writeString(file, arr);
	}

	public static double entropy(double x) {
		return -x * Math.log(x) - (1 - x) * Math.log(1 - x);
	}

	public static double Poisson(double x) {
		return -Math.expm1(-x);
	}

	static public class Timer {
		long start;

		public Timer() {
			start();
		}

		public void start() {
			// start = new Date().getTime();
			start = System.currentTimeMillis();
		}
		public long lapsedSeconds() {
			return (System.currentTimeMillis() - start) / 1000;
		}


		public void report() {
			double dif = System.currentTimeMillis() - start;
			dif /= 1000;
			log.info("Total Time duration " + dif + " seconds.");
			start();
		}
	}

	public static boolean verbose = false;

	public static double uninterpolated_average_precision(int... brr) {
		double sum = 0;
		int k = 0;
		for (int i = 0; i < brr.length; ++i) {
			double e = ++k * 1.0 / brr[i];
			sum += e;
			double alpha = k * 1.0 / brr.length;
			log.info("alpha = " + alpha);
			log.info("precision e = " + e);
			if (e > alpha) {
				log.info("precision  reaches alpha");
			}
			log.info("average = " + (sum / (i + 1)));
		}

		sum /= brr.length;
		return sum;
	}

	static boolean isPower2(int n) {
		return (n & -n) == n;
	}

	static void test_isPower2() {
		int x[] = new int[31];
		for (int i = 0; i < x.length; ++i) {
			x[i] = 2 << i;
		}
		for (int i = 0; i < x.length; ++i) {
			int t = x[i];
			if (isPower2(t))
				log.info("" + t + " is ofPower2");
			else {
				log.info("" + t + " is not ofPower2");
				assert false;
			}
			t++;
			if (isPower2(t)) {
				log.info("" + t + " is ofPower2");
				assert false;
			} else
				log.info("" + t + " is not ofPower2");
			t -= 2;
			if (isPower2(t)) {
				log.info("" + t + " is ofPower2");
				assert false;
			} else
				log.info("" + t + " is not ofPower2");
		}
	}

	static public class KeyGenerator extends Vector<Couplet.Integer> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static void test() throws Exception {
			KeyGenerator keyGenerator = new KeyGenerator();
			Random r = new java.util.Random();

			int arr[] = new int[400];
			for (int i = 0; i < arr.length; ++i) {
				arr[i] = r.nextInt(400);
			}

			HashSet<Integer> set = new HashSet<Integer>();
			for (int a : arr) {
				set.add(a);
			}
			log.info(set.toString());
			for (int a : set) {
				keyGenerator.register_key(a);
			}

			for (int i = 0; i < 1000; ++i) {
				int key = keyGenerator.generate_key();
				if (!set.add(key))
					throw new Exception("logic error");
				log.info("registering " + key);
			}

			for (int a : set) {
				keyGenerator.unregister_key(a);
			}
		}

		public int elementSize() {
			int sum = 0;
			for (Couplet.Integer e : this) {
				sum += e.y - e.x;
			}
			return sum;
		}

		public KeyGenerator conjugate() {
			for (int i = 0; i < size() - 1; ++i) {
				this.get(i).x = this.get(i).y;
				this.get(i).y = this.get(i + 1).x;
			}
			this.remove(size() - 1);
			return this;
		}

		public int[] keySet() {
			int[] arr = new int[elementSize()];
			int i = 0;
			for (Couplet.Integer s : this) {
				for (int j = s.x; j < s.y; ++j) {
					arr[i++] = j;
				}
			}
			return arr;
		}

		@Override
		public synchronized String toString() {
			String str = "";
			for (Couplet.Integer s : this) {
				str += s.x + " -> " + s.y + " = " + (s.y - s.x);
				str += "\n";
			}
			// TODO Auto-generated method stub
			return str;
		}

		public void register_key(int key) throws Exception {
			int mid = binary_search(key);

			if (mid < size()) {
				if (get(mid).x == key) {
					throw new Exception("the key was registered already!");
				}
				if (get(mid).x == key + 1) {
					--get(mid).x;
					if (mid > 0 && get(mid).x == get(mid - 1).y) {
						get(mid - 1).y = get(mid).y;
						remove(mid);
					}
					return;
				}
			}
			if (mid > 0) {
				if (get(mid - 1).y == key) {
					++get(mid - 1).y;
					return;
				} else if (get(mid - 1).y > key) {
					throw new Exception("the key was registered already!");
				}
			}
			add(mid, new Couplet.Integer(key, key + 1));
			if (!isRregistered(key)) {
				log.info(this.toString());
				throw new Exception("the key was not registered correctly");
			}
		}

		public boolean isRregistered(int key) throws Exception {
			int mid = binary_search(key);

			if (mid < size()) {
				if (get(mid).x == key) {
					return true;
				}
				if (get(mid).x == key + 1) {
					return false;
				}
			}
			if (mid > 0) {
				if (get(mid - 1).y == key) {
					return false;
				} else if (get(mid - 1).y > key) {
					return true;
				}
			}
			return false;
		}

		int binary_search(int key) {
			return Utility.binary_search(this, new Couplet.Integer(key, key), new Comparator<Couplet.Integer>() {
				@Override
				public int compare(Utility.Couplet.Integer o1, Utility.Couplet.Integer o2) {
					// TODO Auto-generated method stub
					return java.lang.Integer.compare(o1.x, o2.x);
				}
			});
		}

		public void unregister_key(int key) throws Exception {
			int mid = binary_search(key);

			if (mid == size()) {
				--mid;
				if (mid < 0) {
					throw new Exception("the key was not yet registered!");
				} else {
					if (get(mid).y <= key) {
						throw new Exception("the key was not yet registered!");
					}
				}
			} else {
				if (get(mid).x == key) {
					++get(mid).x;
					if (get(mid).x == get(mid).y) {
						remove(mid);
					}
					return;
				}

				if (get(mid).x == key + 1) {
					throw new Exception("the key was not yet registered!");
				} else if (mid > 0) {
					if (get(mid - 1).y <= key) {
						throw new Exception("the key was not yet registered!");
					} else {
						--mid;
					}
				} else {
					throw new Exception("the key was not yet registered!");
				}
			}

			if (key < get(mid).x || key >= get(mid).y) {
				log.info(this.toString());
				throw new Exception("unregistering the key lying out of bound, key = " + key + " mid = " + mid);
			}

			if (key == get(mid).x) {
				++get(mid).x;
				if (get(mid).x == get(mid).y) {
					remove(mid);
				}
			} else if (key == get(mid).y - 1) {
				--get(mid).y;
				if (get(mid).x == get(mid).y) {
					log.info(this.toString());
					throw new Exception("while unregistering the key, an empty range is detected!" + " mid = " + mid);
				}
			} else {
				this.add(mid + 1, new Couplet.Integer(key + 1, get(mid).y));
				get(mid).y = key;
			}

			if (isRregistered(key)) {
				log.info(this.toString());
				throw new Exception("the key wasn't unregistered correctly");
			}
		}

		public int generate_key() throws Exception {
			int key;
			if (size() == 0)
				key = 1;
			else
				key = get(0).y;
			register_key(key);
			return key;
		}
	}

	// the output variable reaches its peak when the input variable becomes 1/2
	// ln3;
	public static double sinusoidalHyperbolicTangent(double x) {
		return Math.sqrt(Math.sin(Math.tanh(x) * Math.PI));
	}

	// the output variable reaches its peak when the input variable becomes T;
	public static double sinusoidalHyperbolicTangent(double x, double T) {
		return sinusoidalHyperbolicTangent(0.5 * Math.log(3) * x / T);
	}

	public static int[] parseInt(String str[]) {
		int arr[] = new int[str.length];
		for (int i = 0; i < str.length; ++i) {
			arr[i] = Integer.parseInt(str[i]);
		}
		return arr;
	}

	public static int[] parseInt(String str) {
		return parseInt(str.split("\\s+"));
	}

	public static HashMap<String, Double> parseMap(String str) throws Exception {
		HashMap<String, Double> map = new HashMap<String, Double>();

		// log.info("str = " + str);
		for (String[] res : Utility.regex(str + ", ", "(\\S+) = (\\S+), ")) {
			map.put(res[0], Double.parseDouble(res[1]));
		}
		return map;
	}

	static public class MyLogger {
		private static Vector<String> m_vecLogCache = null;

		final int RECORD_COUNT = 500;
		final int SLEEP_TIME = 1000;// 1s

		static boolean m_bWriteThreadRun = false;

		public MyLogger() {
			if (!m_bWriteThreadRun) // 未启动时，启动
			{
				Thread write_log_thread = new Thread(new WriteThread(), "write_log_thread");
				write_log_thread.setDaemon(true);
				write_log_thread.start();
			}
		}

		private void log1(String msg) {
			if (MyLogger.m_bPrint2terminal) // 调试信息显示到终端
			{
				System.out.println(msg);
				return;
			}

			if (m_vecLogCache == null) {
				m_vecLogCache = new Vector<String>();
			}
			m_vecLogCache.add(msg);
			if (m_vecLogCache.size() > RECORD_COUNT) {
				writeLog2File(m_vecLogCache, MyLogger.m_sLogPath + getFormatDate() + ".log");
			}
		}

		private void log1(String msg, Object... obj) {
			if (MyLogger.m_bPrint2terminal) // 调试信息显示到终端
			{
				System.out.printf(msg, obj);
				System.out.println();
				return;
			}

			if (m_vecLogCache == null) {
				m_vecLogCache = new Vector<String>();
			}
			m_vecLogCache.add(msg);
			if (m_vecLogCache.size() > RECORD_COUNT) {
				writeLog2File(m_vecLogCache, MyLogger.m_sLogPath + getFormatDate() + ".log");
			}
		}

		private void log_throwable(String msg, Throwable throwable) {
			throwable.printStackTrace();
		}

		public void debug(String msg) {
			log1(msg);
		}

		public void debug(String msg, Object... obj) {
			log1(msg, obj);
		}

		public void debug_throwable(String msg, Throwable throwable) {
			log1(msg, throwable);
		}

		public void warn(String msg) {
			log1(msg);
		}

		public void warn(String msg, Throwable throwable) {
			log1(msg, throwable);
		}

		public void error(String msg) {
			log1(msg);
		}

		public void error(String msg, Throwable throwable) {
			log1(msg, throwable);
		}

		public void info(String msg) {
			log1(msg);
		}

		public void info(String msg, Throwable throwable) {
			log1(msg, throwable);
		}

		public void fatal(String msg) {
			log1(msg);
		}

		public void fatal(String msg, Throwable throwable) {
			log1(msg, throwable);
		}

		// 将缓冲的日志记录文件
		public void flushLogs2File() {
			if (m_vecLogCache == null || m_vecLogCache.size() <= 0) {
				return;
			}
			String filename = MyLogger.m_sLogPath + getFormatDate() + ".log";
			writeLog2File(m_vecLogCache, filename);
		}

		public synchronized String writeLog2File(Vector<String> vecLogs, String filename) {
			if (vecLogs == null)
				return "";

			int nBeg = 0, nEnd = 0, nPagSize = 20; // 每次写入的大小页可调
			int nPage = 0, nTotalPages = 0;
			int totalRecords = vecLogs.size();
			String curData = "";
			int nbufsize = 0;
			char[] wbuf = null;
			int ncurpos = 0;
			int i = 0;
			// 将数据写入本地临时文件
			try {
				FileOutputStream fOutStm = new FileOutputStream(filename, true);
				BufferedWriter fOut = new BufferedWriter(new OutputStreamWriter(fOutStm)); // 设置输出接口按中文编码
				fOut.flush();
				nTotalPages = totalRecords / nPagSize + 1;
				for (nPage = 0; nPage < nTotalPages; nPage += 1) {
					nBeg = nPage * nPagSize;
					if (nBeg + nPagSize < totalRecords) {
						nEnd = (nPage + 1) * nPagSize;
					} else {
						nEnd = totalRecords;
					}

					nbufsize = 0;
					for (i = nBeg; i < nEnd; i += 1) // 计算缓冲区大小
					{
						curData = vecLogs.get(i);
						if (curData != null && curData.length() > 0) {
							nbufsize += curData.length();
						}
					}

					// 分配缓冲区
					if (nbufsize > 0) {
						wbuf = new char[nbufsize];
						ncurpos = 0;
						for (i = nBeg; i < nEnd; i += 1) // 准备数据
						{
							curData = vecLogs.get(i);
							if (curData != null) {
								curData.getChars(0, curData.length(), wbuf, ncurpos);
								ncurpos += curData.length();
							}
						}

						// 写入文件
						if (wbuf != null && wbuf.length > 0) {
							fOut.write(wbuf);
							wbuf = null;
						}
					}
				}
				fOut.flush();
				fOut.close();

				fOutStm.flush();
				fOutStm.close();
				vecLogs.removeAllElements();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(e.toString());
				return e.toString();
			}

			return "";
		}

		class WriteThread implements Runnable {
			public void run() {
				String sDate = "";
				String sLogPath = "";
				m_bWriteThreadRun = true;
				do {
					try {
						synchronized (m_vecLogCache) {
							if (m_vecLogCache.size() > 0) {
								writeLog2File(m_vecLogCache, MyLogger.m_sLogPath + getFormatDate() + ".log");
							}
						}
						Thread.sleep(SLEEP_TIME); // 1s
					} catch (Exception e) {
					}

					if (Thread.interrupted()) {
						m_bWriteThreadRun = false;
						break;
					}

					if (!MyLogger.m_bWebActive) {
						m_bWriteThreadRun = false;
						break;
					}
				} while (true);

				if (null != m_vecLogCache && m_vecLogCache.size() > 0) {
					writeLog2File(m_vecLogCache, sLogPath + sDate + ".log");
				}
			}
		}

		static public final int DEBUG_NO = 0; // 不记录调试信息
		static public final int DEBUG_FATAL = 1; // 记录致命的调试信息
		static public final int DEBUG_ERROR = 2; // 记录出错信息
		static public final int DEBUG_WARNING = 3; // 记录警告信息
		static public final int DEBUG_CAUTION = 4; // 记录小心注意事项
		static public final int DEBUG_INFO = 5; // 记录一般提示信息
		static public final int DEBUG_TRACING = 6; // 普通跟踪调试信息

		public static int m_iDebuglevel = 4; // 需要初始化，从配置文件读取
		public static boolean m_bPrint2terminal = true; // 需要初始化，从配置文件读取
		public static String m_sLogPath = "";// 需要初始化，从配置文件读取

		public static boolean m_bWebActive = true; // web服务需要设值，独立程序不需要

		private static MyLogger logger = new MyLogger();

		public static void log(String data) {
			String time = getFormatDateTime() + " ";
			String msg = "";

			msg += time;
			msg += data;
			msg += "\n";
			MyLogger.logger.debug(msg);
		}

		public static void log(Object data) {
			// log(String.valueOf(data));
		}

		public static void log() {
			// log("");
		}

		public static void log(String data, Object... obj) {
			String time = getFormatDateTime() + " ";
			String msg = "";

			msg += time;
			msg += data;
			msg += "\n";
			MyLogger.logger.debug(msg, obj);
		}

		public static void log_level(String data, int level) {
			String msg;
			if (level <= MyLogger.m_iDebuglevel) {
				String time = getFormatDateTime() + " ";
				msg = "";
				msg += time;
				msg += data;
				msg += "\n";
				MyLogger.logger.debug(msg);
			}
		}

		public static void log_level(String stype, String data) {
			String time = getFormatDateTime() + " ";
			String msg = "";
			msg += time;
			if (null != stype && !"".equals(stype)) {
				msg += "--";
				msg += stype;
				msg += "--: ";
			}
			msg += data;
			msg += "\n";
			MyLogger.logger.debug(msg);
		}

		public static void log_level(String stype, String data, int level) {
			String msg;
			if (level <= MyLogger.m_iDebuglevel) {
				String time = getFormatDateTime() + " ";
				msg = "";
				msg += time;
				if (null != stype && !"".equals(stype)) {
					msg += "--";
					msg += stype;
					msg += "--: ";
				}
				msg += data;
				msg += "\n";
				MyLogger.logger.debug(msg);
			}
		}

		public static String formatData(String str, int model) {
			int fillCount = 0;
			String returnStr = str;
			fillCount = model - str.length();
			if (fillCount < 0)
				return str.substring(-fillCount - 1);
			for (int i = 0; i < fillCount; i++)
				returnStr = "0" + returnStr;
			return returnStr;
		}

		// yyyy-mm-dd
		public static String getFormatDate() {
			String year, month, day;
			Calendar today = Calendar.getInstance();
			year = formatData(Integer.toString(today.get(Calendar.YEAR)), 4);
			month = formatData(Integer.toString(today.get(Calendar.MONTH) + 1), 2);
			day = formatData(Integer.toString(today.get(Calendar.DAY_OF_MONTH)), 2);
			return year + "-" + month + "-" + day;
		}

		// hh:mm:ss
		public static String getFormatTime() {
			String hour, minute, second;
			Calendar today = Calendar.getInstance();
			hour = formatData(Integer.toString(today.get(Calendar.HOUR_OF_DAY)), 2);
			minute = formatData(Integer.toString(today.get(Calendar.MINUTE)), 2);
			second = formatData(Integer.toString(today.get(Calendar.SECOND)), 2);
			return hour + ":" + minute + ":" + second;
		}

		// yyyy-mm-dd hh:mm:ss
		public static String getFormatDateTime() {
			return getFormatDate() + " " + getFormatTime();
		}

		public static void testMyLogger() throws Exception {
			MyLogger.m_sLogPath = "D:\\workspace\\test\\log\\";

			MyLogger.logger.flushLogs2File(); // 如果是独立程序，需调用此函数
		}
	}

	public static class TestLogger {
		private static Logger logger = Logger.getLogger(TestLogger.class);

		/**
		 * @param args
		 */
		public static void test(String[] args) {
			// System.out.println("This is println message.");

			// 记录debug级别的信息
			logger.debug("This is debug message.");

			// 记录info级别的信息
			logger.info("This is info message.");
			// 记录error级别的信息
			logger.error("This is error message.");
		}
	}

	/**
	 * HikariCP使用
	 * 
	 * @author CoolKing
	 *
	 */
	// http://repo1.maven.org/maven2/com/zaxxer/HikariCP-java7/2.4.8/
	public static class DataSource {
		String key[] = { "user", "password", "useUnicode", "characterEncoding", "autoReconnect", };

		String value[] = { "user", "password", "true", "utf-8", "true", };

		abstract public class Invoker<TYPE> {
			protected abstract TYPE invoke() throws Exception;

			public DataSource getDataSource() throws Exception {
				return DataSource.this;
			}

			public TYPE execute() throws Exception {
				TYPE obj = null;
				open();
				try {
					obj = invoke();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					close();
				}
				return obj;
			}

		}

		private HikariDataSource ds;
		Connection con = null;

		public DatabaseMetaData getDatabaseMetaData() throws SQLException {
			return con.getMetaData();
		}

		int cnt = 0;

		public synchronized Connection open() throws Exception {
			++cnt;
			if (con != null) {
				log.info("Connection is already opened. cnt = " + cnt);
				// the Connection might be shut down automatically if it is not
				// used for a long time;
				if (con.isClosed()) {
					con = getConnection();
				}
				return con;
			}

			// Class.forName("com.mysql.jdbc.Driver");
			// con = DriverManager.getConnection(url, user, password);
			con = getConnection();
			log.info("Connection is opened.");
			return con;
		}

		public synchronized void close() throws Exception {
			--cnt;
			if (con == null || cnt < 0) {
				log.info("Connection is already closed. cnt = " + cnt);
				return;
			}

			if (cnt == 0) {
				con.close();
				con = null;
				// System.gc();
			}
			log.info("Connection is closed.");
		}

		public class Query implements Iterable<ResultSet>, Iterator<ResultSet> {

			public Query(String sql) throws SQLException {
				// log.info("Query : " + sql);
				prepareStatement(sql);
			}

			public void prepareStatement(String sql) throws SQLException {
				preparedStatement = con.prepareStatement(sql);
				result = preparedStatement.executeQuery();
			}

			ResultSet result;
			PreparedStatement preparedStatement;

			public void close() throws SQLException {
				result.close();
				preparedStatement.close();
			}

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				try {
					if (result.next())
						return true;
					else {
						close();
						return false;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}

			@Override
			public ResultSet next() {
				// TODO Auto-generated method stub
				return result;
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub

			}

			@Override
			public Iterator<ResultSet> iterator() {
				// TODO Auto-generated method stub
				return this;
			}
		}

		public boolean execute(String sql) throws SQLException {
			log.info("sql: " + sql);
			boolean ret = false;
			PreparedStatement preparedStatement = con.prepareStatement(sql);
			try {
				ret = preparedStatement.execute();
			} catch (Exception e) {
				preparedStatement.close();
				e.printStackTrace();
				throw e;
			} finally {
				preparedStatement.close();
			}

			return ret;
		}

		public class BatchExecutive {
			public BatchExecutive() throws SQLException {
				statement = con.createStatement();
			}

			Statement statement;

			public void addBatch(String sql) throws SQLException {
				statement.addBatch(sql);
			}

			public void executeBatch() throws SQLException {
				try {
					statement.executeBatch();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					statement.close();
				}
			}
		}

		protected int[] execute(String sql[]) throws SQLException {
			return execute(sql, sql.length);
		}

		protected int[] execute(String sql[], int length) throws SQLException {
			Statement statement = con.createStatement();

			int[] res = null;
			try {
				for (int i = 0; i < length; ++i) {
					log.info("sql: " + sql[i]);
					if (sql[i] == null)
						throw new Exception("null sql occurred.");
					statement.addBatch(sql[i]);
				}
				res = statement.executeBatch();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				statement.close();
			}
			// con.commit();

			return res;
		}

		public DataSource(String url, String user, String password, Driver driver, String serverTimezone) {
			int minimum = 10;
			int maximum = 50;
			// pool configuration
			HikariConfig config = new HikariConfig();
			switch (driver) {
			case mysql:
				config.setDriverClassName("com.mysql.jdbc.Driver");
				String value[] = { user, password, "true", "utf-8", "true", serverTimezone};
				String key[] = { "user", "password", "useUnicode", "characterEncoding", "autoReconnect", "serverTimezone"};

				for (int i = 0; i < value.length; ++i) {
					url += '&' + key[i] + '=' + value[i];
				}

				config.setJdbcUrl(url);
				log.info("url = " + url);
				config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
				config.setConnectionTestQuery("SELECT 1");

				break;
			case oracle:
				// config.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
				// config.setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
				config.setDriverClassName("oracle.jdbc.driver.OracleDriver");

				OracleDataSource dataSource = null;
				try {
					dataSource = new OracleDataSource();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				log.info("url = " + url);
				dataSource.setURL(url);
				dataSource.setUser(user);
				dataSource.setPassword(password);
				config.setDataSource(dataSource);
				// config.setConnectionTestQuery("SELECT 1");
				break;
			}

			config.addDataSourceProperty("cachePrepStmts", true);
			config.addDataSourceProperty("prepStmtCacheSize", 500);

			config.setAutoCommit(true);
			// 池中最小空闲链接数量
			config.setMinimumIdle(minimum);
			// 池中最大链接数量
			config.setMaximumPoolSize(maximum);

			ds = new HikariDataSource(config);
		}

		/**
		 * 初始化连接池
		 * 
		 * @param minimum
		 * @param Maximum
		 */
		public DataSource(String url, String user, String password) {
			int minimum = 10;
			int Maximum = 50;
			// 连接池配置
			HikariConfig config = new HikariConfig();
			config.setDriverClassName("com.mysql.jdbc.Driver");

			value[0] = user;
			value[1] = password;
			for (int i = 0; i < value.length; ++i) {
				url += '&' + key[i] + '=' + value[i];
			}
			config.setJdbcUrl(url);
			log.info("url = " + url);
			config.addDataSourceProperty("cachePrepStmts", true);
			config.addDataSourceProperty("prepStmtCacheSize", 500);
			config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
			config.setConnectionTestQuery("SELECT 1");
			config.setAutoCommit(true);
			// 池中最小空闲链接数量
			config.setMinimumIdle(minimum);
			// 池中最大链接数量
			config.setMaximumPoolSize(Maximum);

			ds = new HikariDataSource(config);
		}

		/**
		 * 销毁连接池
		 */
		@SuppressWarnings("deprecation")
		public void shutdown() {
			ds.shutdown();
		}

		/**
		 * 从连接池中获取链接
		 * 
		 * @return
		 */
		public Connection getConnection() {
			try {
				return ds.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
				ds.resumePool();
				return null;
			}
		}

		public static void test(String[] args) throws SQLException {
			String url = "jdbc:mysql://121.40.196.48:3306/ucc?";
			String user = "root";
			String password = "client1!";

			DataSource ds = new DataSource(url, user, password);
			Connection conn = ds.getConnection();

			// ......
			// 最后关闭链接
			conn.close();
		}
	}

	public static Logger log = Logger.getLogger(Utility.class);

	public static class PriorityQueue<_Ty> extends ArrayList<_Ty> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public Comparator<_Ty> pred;

		public PriorityQueue(Comparator<_Ty> pred) {
			this.pred = pred;
		}

		// default to a maximum heap;
		public PriorityQueue() {
			this(true);
		}

		public PriorityQueue(boolean bMaximumHeap) {
			if (bMaximumHeap)
				this.pred = new Comparator<_Ty>() {
					@SuppressWarnings("unchecked")
					@Override
					public int compare(_Ty o1, _Ty o2) {
						return ((Comparable<? super _Ty>) o1).compareTo(o2);
					}
				};
			else
				this.pred = new Comparator<_Ty>() {
					@SuppressWarnings("unchecked")
					@Override
					public int compare(_Ty o1, _Ty o2) {
						return ((Comparable<? super _Ty>) o2).compareTo(o1);
					}
				};
		}

		public PriorityQueue(ArrayList<_Ty> arr) {
			this();
			make_heap(arr);
		}

		public PriorityQueue(_Ty[] arr) {
			this();
			make_heap(arr, arr.length);
		}

		public void make_heap(_Ty ptr[], int size) {
			for (int i = 0; i < size; ++i)
				add(ptr[i]);
			make_heap();
		}

		public void make_heap(ArrayList<_Ty> arr) {
			for (int i = 0; i < arr.size(); ++i)
				add(arr.get(i));
			make_heap();
		}

		// make nontrivial [_First, _Last) into a heap, using pred
		public void make_heap() {
			int _Hole = size() >> 1;
			while (0 < _Hole--)
				// reheap top half, bottom to top
				adjust_heap(_Hole);
		}

		private static int _Idx; // used to look for the right kinder / parent.

		// look for the right kinder _Idx * 2 + 2; remember the left kinder is
		// _Idx * 2 + 1, the right kinder might have exceed the array bound and
		// we might have failed to find the left kinder.
		private boolean shl() {
			++_Idx;
			_Idx <<= 1;
			return _Idx < size();
		}

		int adjust_heap(int _Hole) { // percolate _Hole to _Bottom, then push
										// _Val, using pred
			_Ty _Val = get(_Hole);
			int _Top = _Hole;
			_Idx = _Hole;
			while (shl()) { // move _Hole down to larger kinder
				if (pred.compare(get(_Idx), get(_Idx - 1)) < 0)
					--_Idx;
				set(_Hole, get(_Idx));
				_Hole = _Idx;
			}

			if (_Idx == size()) { // only kinder at bottom, move _Hole down to
									// it
				--_Idx;
				set(_Hole, get(_Idx));
				_Hole = _Idx;
			}
			return push_heap(_Top, _Hole, _Val);
		}

		private boolean shr(int _Top) {// look for the right kinder (_Idx - 1) /
										// 2; remember the left kinder is _Idx *
										// 2 + 1, the right kinder might have
										// exceed the array bound and we might
										// have failed to find the left kinder.
			if (_Top < _Idx) {
				--_Idx;
				_Idx >>= 1;
				return true;
			}
			return false; // what happens if _Idx <= _Top ? then the parent of
							// _Idx will locate before _Top, which is not
							// supposed to be done;
		}

		int push_heap(int _Top, int _Hole, _Ty _Val) { // percolate _Hole to
														// _Top or where _Val
														// belongs
			_Idx = _Hole;
			while (shr(_Top) && pred.compare(get(_Idx), _Val) < 0) {// move
																	// _Hole up
																	// to parent
				set(_Hole, get(_Idx));
				_Hole = _Idx;
			}
			set(_Hole, _Val);// drop _Val into final hole
			return _Hole;
		}

		// dequeue operator
		// pop *_First to *(_Last - 1) and reheap, using pred
		public _Ty poll() {
			if (size() == 0)
				return null;
			_Ty _Val = get(0);
			if (size() == 1) {
				remove(0);
				return _Val;
			}

			set(0, get(size() - 1));
			remove(size() - 1);
			adjust_heap(0);
			return _Val;
		}

		// dequeue operator
		// pop *_First to *(_Last - 1) and reheap, using pred
		public _Ty poll(int i) {
			_Ty _Val = get(i);

			_Ty end = get(size() - 1);
			remove(size() - 1);
			if (i != size()) {
				set(i, end);
				adjust_heap(i);
			}
			return _Val;
		}

		// enqueue operator
		public boolean add(_Ty _Val) {
			super.add(_Val);
			push_heap(0, size() - 1, _Val);
			return true;
		}

		// enqueue operator
		public int push(_Ty _Val) {
			super.add(_Val);
			int _Hole = push_heap(0, size() - 1, _Val);
			return _Hole;
		}

		int reset(int i, _Ty _Val) {
			// allow i to equal size??
			assert (i <= size());
			super.set(i, _Val);
			return adjust_heap(i);
		}

		public _Ty peek() {
			if (size() == 0)
				return null;
			return get(0);
		}
	}

	public static class PriorityQueueUnique<_Ty> extends ArrayList<_Ty> {
		public Comparator<_Ty> pred;

		public PriorityQueueUnique(Comparator<_Ty> pred) {
			this.pred = pred;
		}

		// default to a maximum heap;
		public PriorityQueueUnique() {
			this(true);
		}

		public PriorityQueueUnique(boolean bMaximumHeap) {
			if (bMaximumHeap)
				this.pred = new Comparator<_Ty>() {
					@SuppressWarnings("unchecked")
					@Override
					public int compare(_Ty o1, _Ty o2) {
						return ((Comparable<? super _Ty>) o1).compareTo(o2);
					}
				};
			else
				this.pred = new Comparator<_Ty>() {
					@SuppressWarnings("unchecked")
					@Override
					public int compare(_Ty o1, _Ty o2) {
						return ((Comparable<? super _Ty>) o2).compareTo(o1);
					}
				};
		}

		public PriorityQueueUnique(ArrayList<_Ty> arr) {
			this();
			make_heap(arr);
		}

		public void make_heap(_Ty ptr[], int size) {
			for (int i = 0; i < size; ++i)
				add(ptr[i]);
			make_heap();
		}

		public void make_heap(ArrayList<_Ty> arr) {
			for (int i = 0; i < arr.size(); ++i)
				add(arr.get(i));
			make_heap();
		}

		// make nontrivial [_First, _Last) into a heap, using pred
		public void make_heap() {
			int _Hole = size() >> 1;
			while (0 < _Hole--)
				// reheap top half, bottom to top
				adjust_heap(_Hole);
		}

		private static int _Idx; // used to look for the right kinder / parent.

		// look for the right kinder _Idx * 2 + 2; remember the left kinder is
		// _Idx * 2 + 1, the right kinder might have exceed the array bound and
		// we might have failed to find the left kinder.
		private boolean shl() {
			++_Idx;
			_Idx <<= 1;
			return _Idx < size();
		}

		int adjust_heap(int _Hole) { // percolate _Hole to _Bottom, then push
										// _Val, using pred
			_Ty _Val = get(_Hole);
			int _Top = _Hole;
			_Idx = _Hole;
			while (shl()) { // move _Hole down to larger kinder
				if (pred.compare(get(_Idx), get(_Idx - 1)) <= 0)
					--_Idx;
				set(_Hole, get(_Idx));
				_Hole = _Idx;
			}

			if (_Idx == size()) { // only kinder at bottom, move _Hole down to
									// it
				--_Idx;
				set(_Hole, get(_Idx));
				_Hole = _Idx;
			}
			return push_heap(_Top, _Hole, _Val);
		}

		private boolean shr(int _Top) {// look for the right kinder (_Idx - 1) /
										// 2; remember the left kinder is _Idx *
										// 2 + 1, the right kinder might have
										// exceed the array bound and we might
										// have failed to find the left kinder.
			if (_Top < _Idx) {
				--_Idx;
				_Idx >>= 1;
				return true;
			}
			return false; // what happens if _Idx <= _Top ? then the parent of
							// _Idx will locate before _Top, which is not
							// supposed to be done;
		}

		int push_heap(int _Top, int _Hole, _Ty _Val) { // percolate _Hole to
														// _Top or where _Val
														// belongs
			_Idx = _Hole;
			while (shr(_Top) && pred.compare(get(_Idx), _Val) <= 0) {// move
																		// _Hole
																		// up
																		// to
																		// parent
				set(_Hole, get(_Idx));
				_Hole = _Idx;
			}

			set(_Hole, _Val);// drop _Val into final hole
			return _Hole;
		}

		// dequeue operator
		// pop *_First to *(_Last - 1) and reheap, using pred
		public _Ty poll() {
			if (size() == 0)
				return null;
			_Ty _Val = get(0);
			if (size() == 1) {
				remove(0);
				return _Val;
			}

			set(0, get(size() - 1));
			remove(size() - 1);
			adjust_heap(0);
			return _Val;
		}

		// dequeue operator
		// pop *_First to *(_Last - 1) and reheap, using pred
		public _Ty poll(int i) {
			_Ty _Val = get(i);

			_Ty end = get(size() - 1);
			remove(size() - 1);
			if (i != size()) {
				set(i, end);
				adjust_heap(i);
			}
			return _Val;
		}

		// enqueue operator
		public boolean add(_Ty _Val) {
			super.add(_Val);
			push_heap(0, size() - 1, _Val);
			return true;
		}

		// enqueue operator
		public int push(_Ty _Val) {
			super.add(_Val);
			int _Hole = push_heap(0, size() - 1, _Val);
			return _Hole;
		}

		int reset(int i, _Ty _Val) {
			// allow i to equal size??
			assert (i <= size());
			super.set(i, _Val);
			return adjust_heap(i);
		}

		public _Ty peek() {
			if (size() == 0)
				return null;
			return get(0);
		}
	}

	public static class IndexedPriorityQueue<_Ty> {
		public PriorityQueue<Integer> pq;
		public Vector<Couplet<_Ty, Integer>> arr = new Vector<Couplet<_Ty, Integer>>();

		public IndexedPriorityQueue(final Comparator<_Ty> pred) {
			arr = new Vector<Couplet<_Ty, Integer>>();
			Comparator<Integer> _Pr = new Comparator<Integer>() {
				public int compare(Integer i, Integer j) {
					return pred.compare(arr.get(i).x, arr.get(j).x);
				}
			};
			pq = new PriorityQueue<Integer>(_Pr);
		}

		public _Ty poll() {// queue operation;
			Integer indexPtr = pq.poll();
			if (indexPtr == null)
				return null;
			int index = indexPtr;
			_Ty var = arr.get(index).x;
			arr.set(index, new Couplet<_Ty, Integer>(null, -1));
			return var;
		}

		public _Ty peek() {// queue operation;
			Integer indexPtr = pq.peek();
			if (indexPtr == null)
				return null;
			int index = indexPtr;
			_Ty var = arr.get(index).x;
			return var;
		}

		public _Ty get(int i) {
			return arr.get(i).x;
		}

		public void add(int i, _Ty _Val) throws Exception {
			while (i >= arr.size())
				arr.add(new Couplet<_Ty, Integer>(null, -1));
			arr.get(i).x = _Val;
			arr.get(i).y = pq.push(i);
		}

		// private void set(int i, _Ty _Val) throws Exception {
		// while (i >= arr.size())
		// arr.add(new Couplet<_Ty, Integer>(null, -1));
		// arr.get(i).x = _Val;
		//
		// if (arr.get(i).y < 0){
		// arr.get(i).y = pq.push(i);
		// if (pq.get(arr.get(i).y) != i){
		// throw new Exception("pq.get(arr.get(i).y) != i");
		// }
		// }
		// else{
		// if (pq.get(arr.get(i).y) != i){
		// throw new Exception("pq.get(arr.get(i).y) != i");
		// }
		//
		// arr.get(i).y = pq.reset(arr.get(i).y, i);
		//
		// if (pq.get(arr.get(i).y) != i){
		// throw new Exception("pq.get(arr.get(i).y) != i");
		// }
		// }
		// }
		//
		@Override
		public java.lang.String toString() {
			String str = pq.toString();
			str += "\n";

			for (int i = 0; i < arr.size(); ++i) {
				str += "x[" + i + "] = " + get(i);
				str += "\n";
			}

			return str;
		}
	}

	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 删除指定文件夹下所有文件
	// param path 文件夹完整绝对路径
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	public static BufferedReader readFromStdin() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		return br;
	}

	/**
	 * 
	 * @param ext = extension of the temporary file
	 * @return
	 * @throws IOException
	 */
	public static String createTemporaryFile(String ext) throws IOException {
		return createTemporaryFile(Utility.workingDirectory, ext);
	}

	/**
	 * 
	 * @param workingDirectory
	 * @param ext
	 * @return
	 * @throws IOException
	 */
	public static String createTemporaryFile(String workingDirectory, String ext) throws IOException {
		String fullUploadPath = new File(workingDirectory).getAbsolutePath() + "/"
				+ (new SimpleDateFormat("yyyy/MM/dd")).format(new Date()) + "/";

		File path = new File(fullUploadPath);
		if (!path.exists()) {
			path.mkdirs();
		}

		fullUploadPath += UUID.randomUUID() + "." + ext;
		new File(fullUploadPath).createNewFile();
		return fullUploadPath;
	}

	public static String readFileFromURL(String httpPath) {
		String fullUploadPath = null;
		BufferedInputStream dis = null;
		FileOutputStream fos = null;

		try {
			fullUploadPath = createTemporaryFile(httpPath.substring(httpPath.lastIndexOf(".") + 1));

			URL url = new URL(httpPath);
			log.info("network file address = " + url.getHost() + " -- " + url.toString());
			HttpURLConnection huconn = (HttpURLConnection) url.openConnection();
			// huconn.setConnectTimeout(180000);
			// huconn.setReadTimeout(180000);
			// huconn.setConnectTimeout(0);
			// huconn.setReadTimeout(0);

			dis = new BufferedInputStream(huconn.getInputStream());

			fos = new FileOutputStream(new File(fullUploadPath));
			// dis.available();
			int available = 1024;
			byte[] buff = new byte[available];
			int num = -1;
			while ((num = dis.read(buff)) != -1) {
				fos.write(buff, 0, num);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				dis.close();
				fos.flush();
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return fullUploadPath;
	}

	/**
	 * something equivalent to c/c++ compiler's __FUNCTION__
	 * 
	 * @return
	 */

	public static String __FUNCTION__() {
		// Thread.currentThread().getStackTrace()[1] .getMethodName();
		// is a better way since it wont create an exception

		StackTraceElement[] arr = new Throwable().getStackTrace();
		if (arr.length < 2)
			return "unknown function by the java compiler";

		return arr[1].toString();
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath String 原文件路径 如：c:/fqf.txt
	 * @param newPath String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	static public void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[inStream.available()];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.flush();
				fs.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath String 原文件路径 如：c:/fqf
	 * @param newPath String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 */
	static public void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			e.printStackTrace();

		}
	}

	static public <_Ty> int hashCode(_Ty tag[]) {
		int hashCode = 0;
		for (int i = 0; i < tag.length; ++i) {
			hashCode = hashCode * 31 + tag[i].hashCode();
		}
		return hashCode;
	}

	public static boolean equals(String[] seg, String[] segSub, int I) throws Exception {
		for (int i = I, j = 0; i < seg.length && j < segSub.length; ++i, ++j) {
			if (!seg[i].equals(segSub[j])) {
				return false;
			}
		}
		return true;
	}

	public static int containsSubstr(String[] seg, String[] segSub) throws Exception {
		return containsSubstr(seg, segSub, 0);
	}

	public static int containsSubstr(String[] seg, String[] segSub, int start) throws Exception {
		for (int i = start; i <= seg.length - segSub.length; ++i) {
			if (equals(seg, segSub, i))
				return i;
		}
		return -1;
	}

	static public <_Ty> boolean equals(_Ty a[], _Ty b[]) {
		if (a == null)
			return b == null;

		if (a.length != b.length) {
			return false;
		}

		for (int i = 0; i < b.length; ++i) {
			if (!equals(a[i], b[i])) {
				return false;
			}
		}
		return true;
	}

	static public boolean equals(Object a, Object b) {
		if (a == null)
			return b == null;
		if (a instanceof Map) {
			if (b instanceof Map)
				return equals((Map) a, (Map) b);
			else
				return false;
		}
		if (a instanceof List) {
			if (b instanceof List)
				return equals((List) a, (List) b);
			else
				return false;
		}
		if (a instanceof Set) {
			if (b instanceof Set)
				return equals((Set) a, (Set) b);
			else
				return false;
		}
		if (a instanceof int[]) {
			if (b instanceof int[])
				return equals((int[]) a, (int[]) b);
			else
				return false;
		}
		if (a instanceof double[]) {
			if (b instanceof double[])
				return equals((double[]) a, (double[]) b);
			else
				return false;
		}
		if (a instanceof String[]) {
			if (b instanceof String[])
				return equals((String[]) a, (String[]) b);
			else
				return false;
		}
		return a.equals(b);
	}

	static public <_Ty> boolean equals(List<_Ty> a, List<_Ty> b) {
		if (a == null)
			return b == null;

		if (a.size() != b.size()) {
			return false;
		}

		for (int i = 0; i < b.size(); ++i) {
			if (!equals(a.get(i), b.get(i))) {
				return false;
			}
		}
		return true;
	}

	static public <_Ty> boolean equals(Set<_Ty> a, Set<_Ty> b) {
		if (a == null)
			return b == null;

		if (a.size() != b.size()) {
			return false;
		}
		return a.containsAll(b);
	}

	static public <K, V> boolean equals(Map<K, V> a, Map<K, V> b) {
		if (a == null)
			return b == null;

		if (a.size() != b.size()) {
			return false;
		}

		for (Map.Entry<K, V> entry : a.entrySet()) {
			if (!equals(entry.getValue(), b.get(entry.getKey())))
				return false;
		}

		return true;
	}

	static public boolean equals(int a[], int b[]) {
		if (a == null)
			return b == null;
		if (a.length != b.length) {
			return false;
		}

		for (int i = 0; i < b.length; ++i) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	static public boolean equals(double a[], double b[]) {
		if (a == null)
			return b == null;

		if (a.length != b.length) {
			return false;
		}

		for (int i = 0; i < b.length; ++i) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * sauf = except
	 * 
	 * @param adverb
	 * @param i
	 * @return
	 */
	static public <_Ty> _Ty[] copierSauf(_Ty[] adverb, int i) {
		@SuppressWarnings("unchecked")
		_Ty[] adverbNew = (_Ty[]) Array.newInstance(adverb.getClass().getComponentType(), adverb.length - 1);
		int index = 0;
		for (int j = 0; j < adverb.length; ++j) {
			if (i != j)
				adverbNew[index++] = adverb[j];
		}
		return adverbNew;
	}

	static public <_Ty> _Ty[] copierSauf(_Ty[] adverb, _Ty i) {
		return copierSauf(adverb, Utility.indexOf(adverb, i));
	}

	static public <_Ty> _Ty[] copier(_Ty[] adverb, _Ty element) {
		@SuppressWarnings("unchecked")
		_Ty[] adverbNew = (_Ty[]) Array.newInstance(adverb.getClass().getComponentType(), adverb.length + 1);
		System.arraycopy(adverb, 0, adverbNew, 0, adverb.length);
		adverbNew[adverb.length] = element;
		return adverbNew;
	}

	static public <_Ty> _Ty[] copier(_Ty[] a, _Ty[] b) {
		_Ty[] c = (_Ty[]) new Object[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public <_Ty> _Ty[] copier(_Ty element, _Ty[] adverb) {
		@SuppressWarnings("unchecked")
		_Ty[] adverbNew = (_Ty[]) Array.newInstance(adverb.getClass().getComponentType(), adverb.length + 1);
		System.arraycopy(adverb, 0, adverbNew, 1, adverb.length);
		adverbNew[0] = element;
		return adverbNew;
	}

	/**
	 * return a copy of the array.
	 * 
	 * @param list
	 * @return
	 */
	static public <_Ty> ArrayList<_Ty> copier(List<_Ty> list) {
		@SuppressWarnings("unchecked")
		ArrayList<_Ty> arr = new ArrayList<_Ty>();
		for (_Ty e : list) {
			arr.add(e);
		}
		return arr;
	}

	public static LinkedList<File> obtenirTousLesArchives(String path, String filter) {
		LinkedList<File> files = new LinkedList<File>();

		File fpath = new File(path);

		if (fpath.isDirectory()) {
			File[] flist = fpath.listFiles();
			for (int i = 0; i < flist.length; i++) {
				if (flist[i].isFile()) {
					if (filter == null)
						files.push(flist[i]);
					else if (flist[i].getName().endsWith(filter))
						files.push(flist[i]);
				}
			}
		} else {
			System.err.println("输入必须为目录");
		}
		return files;
	}

	public static LinkedList<File> obtenirTousLesArchives(String path) {
		return obtenirTousLesArchives(path, null);
	}

	public static <T> boolean isNull(T arr[]) {
		return arr == null || arr.length == 0;
	}

	public static int maxPrintability = 25;

	public static String toString(Object data) {
		if (data == null) {
			return null;
		} else if (data instanceof String[]) {
			return toString((String[]) data);
		} else if (data instanceof String[][]) {
			return toString((String[][]) data);
		} else if (data instanceof int[][]) {
			return toString((int[][]) data);
		} else if (data instanceof int[]) {
			return toString((int[]) data);
		} else if (data instanceof float[]) {
			return toString((float[]) data);
		} else if (data instanceof double[]) {
			return toString((double[]) data);
		} else if (data instanceof Object[]) {
			return toString((Object[]) data);
		} else {
			return data.toString();
		}
	}

	public static <K, V> String toString(Entry<K, V> data) {
		return toString(data.getKey()) + " = " + toString(data.getValue());
	}

	public static <K, V> String toString(Couplet<K, V> data) {
		return toString(data.x) + " -> " + toString(data.y);
	}

	public static <K, V> String toString(Map<K, V> value) {
		return toString(value.entrySet(), "\n");
	}

	public static <K, V> String toString(Map<K, V> value, String delimiter) {
		return toString(value.entrySet(), delimiter);
	}

	public static <T> String toString(Collection<T> value) {
		return toString(value, ", ", "[]");
	}

	public static <T> String toString(Collection<T> value, String delimiter, String brackets) {
		return toString(value, delimiter, brackets, maxPrintability);
	}

	public static <T> String toString(Collection<T> value, String delimiter) {
		return toString(value, delimiter, null, maxPrintability);
	}

	public static <T> String toString(Collection<T> value, String delimiter, String brackets, int maxPrintability) {
		int length = value.size();
		if (length == 0)
			return brackets == null ? "" : brackets;

		if (maxPrintability <= 0)
			maxPrintability = Integer.MAX_VALUE;
		if (length > maxPrintability) {
			length = maxPrintability;
		}

		String s = "";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(0);
		int i = 0;

		for (Iterator<T> it = value.iterator(); it.hasNext() && i < length; ++i) {
			T t = it.next();
			if (t instanceof String[]) {
				s += toString((String[]) t);
			} else if (t instanceof int[]) {
				s += toString((int[]) t);
			} else if (t instanceof Entry) {
				s += toString((Entry) t);
			} else if (t instanceof Map) {
				s += toString(((Map) t).entrySet(), ", ");
			} else {
				s += t;
			}
			if (i == length - 1)
				break;
			if (delimiter != null)
				s += delimiter;
			else
				s += " ";

		}

		if (value.size() > maxPrintability)
			s += "... (length = " + value.size() + ")";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(1);
		return s;
	}

	public static <T> String toString(Iterable<T> value) {
		return toString(value, ", ", "[]");
	}

	public static <T> String toString(Iterable<T> value, String delimiter, String brackets) {
		return toString(value, delimiter, brackets, maxPrintability);
	}

	public static <T> String toString(Iterable<T> value, String delimiter) {
		return toString(value, delimiter, null, maxPrintability);
	}

	public static <T> String toString(Iterable<T> value, String delimiter, String brackets, int maxPrintability) {
		int length = maxPrintability;

		String s = "";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(0);
		int i = 0;

		int size = 0;
		for (Iterator<T> it = value.iterator(); it.hasNext() && i < length; ++i) {
			++size;
			T t = it.next();
			if (t instanceof String[]) {
				s += toString((String[]) t);
			} else if (t instanceof int[]) {
				s += toString((int[]) t);
			} else if (t instanceof Entry) {
				s += toString((Entry) t);
			} else {
				s += t;
			}
			if (i == length - 1)
				break;
			if (delimiter != null)
				s += delimiter;
			else
				s += " ";

		}

		if (size > maxPrintability)
			s += "... (length = " + size + ")";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(1);
		return s;
	}

	public static <K, V> String toString(Map<K, V> value, String delimiter, String brackets, int maxPrintability) {
		return toString(value.entrySet(), delimiter, brackets, maxPrintability);
	}

	public static <T> String toString(T[] value, String delimiter, String brackets) {
		return toString(value, delimiter, brackets, maxPrintability);
	}

	public static <T> String toString(T[] value, int maxPrintability) {
		return toString(value, null, null, maxPrintability);
	}

	public static <T> String toString(T[] value, String delimiter, String brackets, int maxPrintability) {
		if (value == null)
			return null;

		int length = value.length;
		if (length == 0)
			return brackets == null ? "" : brackets;

		if (maxPrintability <= 0)
			maxPrintability = Integer.MAX_VALUE;
		if (length > maxPrintability) {
			length = maxPrintability;
		}

		StringBuilder s = new StringBuilder();
		if (brackets != null && brackets.length() == 2)
			s.append(brackets.charAt(0));
		int i = 0;

		for (i = 0; i < length; ++i) {
			T t = value[i];
			s.append(toString(t));
			if (i == length - 1)
				break;

			if (delimiter != null)
				s.append(delimiter);
			else
				s.append(" ");
		}

		if (value.length > maxPrintability)
			s.append("... (length = " + value.length + ")");
		if (brackets != null && brackets.length() == 2)
			s.append(brackets.charAt(1));
		return s.toString();
	}

	public static <T> String toString(T[] value) {
		return toString(value, ", ", null);
	}

	public static <T> String toString(T[] value, String delimiter) {
		return toString(value, delimiter, null);
	}

	public static String toString(int[] value, String delimiter) {
		return toString(value, delimiter, null, maxPrintability);
	}

	public static String toString(int[] value, String delimiter, String brackets, int maxPrintability) {
		if (value == null)
			return null;

		int length = value.length;
		if (length == 0)
			return brackets == null ? "" : brackets;
		if (maxPrintability <= 0)
			maxPrintability = Integer.MAX_VALUE;
		if (length > maxPrintability) {
			length = maxPrintability;
		}

		String s = "";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(0);
		int i = 0;

		for (i = 0; i < length; ++i) {
			int t = value[i];
			s += t;
			if (i == length - 1)
				break;

			if (delimiter != null)
				s += delimiter;
			else
				s += " ";

		}

		if (value.length > maxPrintability)
			s += "... (length = " + value.length + ")";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(1);
		return s;
	}

	public static String toString(int[] value, String delimiter, String brackets) {
		if (value == null)
			return null;

		int length = value.length;
		if (length == 0)
			return brackets == null ? "" : brackets;
		if (maxPrintability <= 0)
			maxPrintability = Integer.MAX_VALUE;
		if (length > maxPrintability) {
			length = maxPrintability;
		}

		String s = "";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(0);
		int i = 0;

		for (i = 0; i < length; ++i) {
			int t = value[i];
			s += t;
			if (i == length - 1)
				break;

			if (delimiter != null)
				s += delimiter;
			else
				s += " ";

		}

		if (value.length > maxPrintability)
			s += "... (length = " + value.length + ")";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(1);
		return s;
	}

	public static String toString(double[] value, String delimiter, String brackets) {
		if (value == null)
			return null;

		int length = value.length;
		if (length == 0)
			return brackets == null ? "" : brackets;
		if (maxPrintability <= 0)
			maxPrintability = Integer.MAX_VALUE;
		if (length > maxPrintability) {
			length = maxPrintability;
		}

		String s = "";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(0);
		int i = 0;

		for (i = 0; i < length; ++i) {
			double t = value[i];
			s += t;
			if (i == length - 1)
				break;

			if (delimiter != null)
				s += delimiter;
			else
				s += " ";

		}

		if (value.length > maxPrintability)
			s += "... (length = " + value.length + ")";
		if (brackets != null && brackets.length() == 2)
			s += brackets.charAt(1);
		return s;
	}

	public static String toString(int[] value) {
		return toString(value, ", ", "[]", maxPrintability);
	}

	public static String toString(byte[] value) {
		if (value == null) {
			return null;
		}
		if (value.length == 0) {
			return "[]";
		}

		String s = "[";
		int length = value.length;
		if (maxPrintability <= 0)
			maxPrintability = Integer.MAX_VALUE;
		if (value.length > maxPrintability) {
			length = maxPrintability;
		}

		for (int i = 0; i < length; ++i) {
			s += value[i];
			s += ", ";
		}

		if (value.length > maxPrintability)
			return s += "... (length = " + value.length + ")]";
		else
			return s.substring(0, s.length() - 2) + "]";
	}

	public static String toString(float[] value) {
		if (value == null) {
			return null;
		}
		if (value.length == 0) {
			return "[]";
		}

		String s = "[";

		int length = value.length;
		if (maxPrintability <= 0)
			maxPrintability = Integer.MAX_VALUE;
		if (value.length > maxPrintability) {
			length = maxPrintability;
		}

		for (int i = 0; i < length; ++i) {
			s += value[i];
			s += ", ";
		}

		if (value.length > maxPrintability)
			return s += "... (length = " + value.length + ")]";
		else
			return s.substring(0, s.length() - 2) + "]";
	}

	public static String toString(double[] value) {
		return toString(value, ", ", "[]");
	}

	public static String toString(Date time) {
		if (time == null)
			return "yyyy-MM-dd HH:mm:ss";
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
	}

	public static String toString(int size, char ch) {
		// if (size < 0){
		// log.info("java.lang.NegativeArraySizeException");
		// return "";
		// }
		char arr[] = new char[size];
		java.util.Arrays.fill(arr, ch);
		return new String(arr);
	}

	public static char last(String str) {
		return str.charAt(str.length() - 1);
	}

	public static <T> T last(T[] str) {
		return str[str.length - 1];
	}

	public static <T> T last(List<T> str) {
		return str.get(str.size() - 1);
	}

	public static char first(String str) {
		return str.charAt(0);
	}

	// reverse the order;
	public static <T> void reverse(T v[]) {
		for (int i = 0; i < v.length / 2; ++i) {
			swap(v, i, v.length - 1 - i);
		}
	}

	public static <T> void reverse(List<T> v) {
		for (int i = 0; i < v.size() / 2; ++i) {
			swap(v, i, v.size() - 1 - i);
		}
	}

	public static String[] errorMark(int length, int... index) {
		String[] tag = new String[length];
		Arrays.fill(tag, "  ");
		for (int i : index) {
			tag[i] = "--";
		}

		return tag;
	}

	public static String[] toUpperCase(String[] str) {
		for (int i = 0; i < str.length; i++) {
			str[i] = str[i].toUpperCase();
		}
		return str;
	}

	public static String[] toLowerCase(String[] str) {
		for (int i = 0; i < str.length; i++) {
			str[i] = str[i].toLowerCase();
		}
		return str;
	}

	public static boolean isUpperCase(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i))) {
				continue;
			}
			return false;
		}
		return true;
	}

	public static boolean isLowerCase(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (Character.isLowerCase(str.charAt(i))) {
				continue;
			}
			return false;
		}
		return true;
	}

	public static boolean isConsecutive(String seg) {
		if (seg.length() <= 1) {
			return false;
		}
		for (int i = 1; i < seg.length(); ++i) {
			if (seg.charAt(i) != seg.charAt(i - 1)) {
				return false;
			}
		}
		return true;
	}

	public static String removeEndOfSentencePunctuation(String str) {
		String[] res = Utility.regexSingleton(str, "(.*?)[" + Utility.endOfSentencePunctuation + "]*$");
		return res[1];
	}

	public static class Printer {
		public Printer(String file) throws FileNotFoundException {
			out = System.out;
			ps = new PrintStream(file);
			System.setOut(ps);
		}

		PrintStream out;
		PrintStream ps;

		public void close() {
			ps.close();
			System.setOut(out);

		}
	}

	public static void setOut(String file) throws FileNotFoundException {
		System.setOut(new PrintStream(file));
	}

	public static ArrayList<String[]> readFromExcel(String fileName, String sheetName, int columnSize)
			throws Exception {
		Sheet sheet = Utility.read_excel(fileName, sheetName);

		Iterator<Row> rows = sheet.rowIterator();
		Utility.skipLinePremiere(rows);

		ArrayList<String[]> array = new ArrayList<String[]>();
		while (rows.hasNext()) {
			String arr[] = new String[columnSize];
			Row row = rows.next();
			for (int i = 0; i < columnSize; ++i) {
				Cell cell = row.getCell(i);
				if (cell == null)
					continue;
				String str = null;
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
					str = "" + cell.getNumericCellValue();
					break;
				case Cell.CELL_TYPE_STRING:
					str = cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_FORMULA:
					str = cell.getStringCellValue();
					System.out.println("Cell.CELL_TYPE_FORMULA");
					break;
				case Cell.CELL_TYPE_BLANK:
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					str = "" + cell.getBooleanCellValue();
					break;

				case Cell.CELL_TYPE_ERROR:
					break;
				}

				if (str != null) {
					str = str.trim();
					if (str.length() == 0)
						continue;
				}
				arr[i] = str;
			}

			array.add(arr);
		}

		return array;
	}

	public static List<Integer> asList(int... arr) {
		List<Integer> featureArr = new ArrayList<Integer>(arr.length);
		for (int t : arr) {
			featureArr.add(t);
		}
		return featureArr;
	}

	public static int[] asList(List<Integer> list) {
		int arr[] = new int[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i);
		}
		return arr;
	}

	/*
	 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */

	/*
	 *
	 *
	 *
	 *
	 *
	 * Written by Josh Bloch of Google Inc. and released to the public domain, as
	 * explained at http://creativecommons.org/publicdomain/zero/1.0/.
	 */

	/**
	 * Resizable-array implementation of the {@link Deque} interface. Array deques
	 * have no capacity restrictions; they grow as necessary to support usage. They
	 * are not thread-safe; in the absence of external synchronization, they do not
	 * support concurrent access by multiple threads. Null elements are prohibited.
	 * This class is likely to be faster than {@link Stack} when used as a stack,
	 * and faster than {@link LinkedList} when used as a queue.
	 *
	 * <p>
	 * Most {@code ArrayDeque} operations run in amortized constant time. Exceptions
	 * include {@link #remove(Object) remove}, {@link #removeFirstOccurrence
	 * removeFirstOccurrence}, {@link #removeLastOccurrence removeLastOccurrence},
	 * {@link #contains contains}, {@link #iterator iterator.remove()}, and the bulk
	 * operations, all of which run in linear time.
	 *
	 * <p>
	 * The iterators returned by this class's {@code iterator} method are
	 * <i>fail-fast</i>: If the deque is modified at any time after the iterator is
	 * created, in any way except through the iterator's own {@code remove} method,
	 * the iterator will generally throw a {@link ConcurrentModificationException}.
	 * Thus, in the face of concurrent modification, the iterator fails quickly and
	 * cleanly, rather than risking arbitrary, non-deterministic behavior at an
	 * undetermined time in the future.
	 *
	 * <p>
	 * Note that the fail-fast behavior of an iterator cannot be guaranteed as it
	 * is, generally speaking, impossible to make any hard guarantees in the
	 * presence of unsynchronized concurrent modification. Fail-fast iterators throw
	 * {@code ConcurrentModificationException} on a best-effort basis. Therefore, it
	 * would be wrong to write a program that depended on this exception for its
	 * correctness: <i>the fail-fast behavior of iterators should be used only to
	 * detect bugs.</i>
	 *
	 * <p>
	 * This class and its iterator implement all of the <em>optional</em> methods of
	 * the {@link Collection} and {@link Iterator} interfaces.
	 *
	 * <p>
	 * This class is a member of the
	 * <a href="{@docRoot}/../technotes/guides/collections/index.html"> Java
	 * Collections Framework</a>.
	 *
	 * @author Josh Bloch and Doug Lea
	 * @since 1.6
	 * @param <E> the type of elements held in this collection
	 */
	static public class ArrayDeque<E> extends AbstractCollection<E> implements Deque<E>, Cloneable, Serializable {
		/**
		 * The array in which the elements of the deque are stored. The capacity of the
		 * deque is the length of this array, which is always a power of two. The array
		 * is never allowed to become full, except transiently within an addX method
		 * where it is resized (see doubleCapacity) immediately upon becoming full, thus
		 * avoiding head and tail wrapping around to equal each other. We also guarantee
		 * that all array cells not holding deque elements are always null.
		 */
		transient Object[] elements; // non-private to simplify nested class access

		/**
		 * The index of the element at the head of the deque (which is the element that
		 * would be removed by remove() or pop()); or an arbitrary number equal to tail
		 * if the deque is empty.
		 */
		transient int head;

		/**
		 * The index at which the next element would be added to the tail of the deque
		 * (via addLast(E), add(E), or push(E)).
		 */
		transient int tail;

		/**
		 * The minimum capacity that we'll use for a newly created deque. Must be a
		 * power of 2.
		 */
		private static final int MIN_INITIAL_CAPACITY = 8;

		// ****** Array allocation and resizing utilities ******

		/**
		 * Allocates empty array to hold the given number of elements.
		 *
		 * @param numElements the number of elements to hold
		 */
		private void allocateElements(int numElements) {
			int initialCapacity = MIN_INITIAL_CAPACITY;
			// Find the best power of two to hold elements.
			// Tests "<=" because arrays aren't kept full.
			if (numElements >= initialCapacity) {
				initialCapacity = numElements;
				initialCapacity |= (initialCapacity >>> 1);
				initialCapacity |= (initialCapacity >>> 2);
				initialCapacity |= (initialCapacity >>> 4);
				initialCapacity |= (initialCapacity >>> 8);
				initialCapacity |= (initialCapacity >>> 16);
				initialCapacity++;

				if (initialCapacity < 0) // Too many elements, must back off
					initialCapacity >>>= 1;// Good luck allocating 2 ^ 30 elements
			}
			elements = new Object[initialCapacity];
		}

		/**
		 * Doubles the capacity of this deque. Call only when full, i.e., when head and
		 * tail have wrapped around to become equal.
		 */
		private void doubleCapacity() {
			assert head == tail;
			int p = head;
			int n = elements.length;
			int r = n - p; // number of elements to the right of p
			int newCapacity = n << 1;
			if (newCapacity < 0)
				throw new IllegalStateException("Sorry, deque too big");
			Object[] a = new Object[newCapacity];
			System.arraycopy(elements, p, a, 0, r);
			System.arraycopy(elements, 0, a, r, p);
			elements = a;
			head = 0;
			tail = n;
		}

		/**
		 * Copies the elements from our element array into the specified array, in order
		 * (from first to last element in the deque). It is assumed that the array is
		 * large enough to hold all elements in the deque.
		 *
		 * @return its argument
		 */
		private <T> T[] copyElements(T[] a) {
			if (head < tail) {
				System.arraycopy(elements, head, a, 0, size());
			} else if (head > tail) {
				int headPortionLen = elements.length - head;
				System.arraycopy(elements, head, a, 0, headPortionLen);
				System.arraycopy(elements, 0, a, headPortionLen, tail);
			}
			return a;
		}

		/**
		 * Constructs an empty array deque with an initial capacity sufficient to hold
		 * 16 elements.
		 */
		public ArrayDeque() {
			elements = new Object[16];
		}

		/**
		 * Constructs an empty array deque with an initial capacity sufficient to hold
		 * the specified number of elements.
		 *
		 * @param numElements lower bound on initial capacity of the deque
		 */
		public ArrayDeque(int numElements) {
			allocateElements(numElements);
		}

		/**
		 * Constructs a deque containing the elements of the specified collection, in
		 * the order they are returned by the collection's iterator. (The first element
		 * returned by the collection's iterator becomes the first element, or
		 * <i>front</i> of the deque.)
		 *
		 * @param c the collection whose elements are to be placed into the deque
		 * @throws NullPointerException if the specified collection is null
		 */
		public ArrayDeque(Collection<? extends E> c) {
			allocateElements(c.size());
			addAll(c);
		}

		// The main insertion and extraction methods are addFirst,
		// addLast, pollFirst, pollLast. The other methods are defined in
		// terms of these.

		/**
		 * Inserts the specified element at the front of this deque.
		 *
		 * @param e the element to add
		 * @throws NullPointerException if the specified element is null
		 */
		public void addFirst(E e) {
			if (e == null)
				throw new NullPointerException();
			elements[head = (head - 1) & (elements.length - 1)] = e;
			if (head == tail)
				doubleCapacity();
		}

		/**
		 * Inserts the specified element at the end of this deque.
		 *
		 * <p>
		 * This method is equivalent to {@link #add}.
		 *
		 * @param e the element to add
		 * @throws NullPointerException if the specified element is null
		 */
		public void addLast(E e) {
			if (e == null)
				throw new NullPointerException();
			elements[tail] = e;
			if ((tail = (tail + 1) & (elements.length - 1)) == head)
				doubleCapacity();
		}

		/**
		 * Inserts the specified element at the front of this deque.
		 *
		 * @param e the element to add
		 * @return {@code true} (as specified by {@link Deque#offerFirst})
		 * @throws NullPointerException if the specified element is null
		 */
		public boolean offerFirst(E e) {
			addFirst(e);
			return true;
		}

		/**
		 * Inserts the specified element at the end of this deque.
		 *
		 * @param e the element to add
		 * @return {@code true} (as specified by {@link Deque#offerLast})
		 * @throws NullPointerException if the specified element is null
		 */
		public boolean offerLast(E e) {
			addLast(e);
			return true;
		}

		/**
		 * @throws NoSuchElementException {@inheritDoc}
		 */
		public E removeFirst() {
			E x = pollFirst();
			if (x == null)
				throw new NoSuchElementException();
			return x;
		}

		/**
		 * @throws NoSuchElementException {@inheritDoc}
		 */
		public E removeLast() {
			E x = pollLast();
			if (x == null)
				throw new NoSuchElementException();
			return x;
		}

		public E pollFirst() {
			int h = head;
			@SuppressWarnings("unchecked")
			E result = (E) elements[h];
			// Element is null if deque empty
			if (result == null)
				return null;
			elements[h] = null; // Must null out slot
			head = (h + 1) & (elements.length - 1);
			return result;
		}

		public E pollLast() {
			int t = (tail - 1) & (elements.length - 1);
			@SuppressWarnings("unchecked")
			E result = (E) elements[t];
			if (result == null)
				return null;
			elements[t] = null;
			tail = t;
			return result;
		}

		public E get(int i) {
			@SuppressWarnings("unchecked")
			E result = (E) elements[(head + i) & (elements.length - 1)];
			if (result == null)
				throw new NoSuchElementException();
			return result;
		}

		/**
		 * @throws NoSuchElementException {@inheritDoc}
		 */
		public E getFirst() {
			@SuppressWarnings("unchecked")
			E result = (E) elements[head];
			if (result == null)
				throw new NoSuchElementException();
			return result;
		}

		/**
		 * @throws NoSuchElementException {@inheritDoc}
		 */
		public E getLast() {
			@SuppressWarnings("unchecked")
			E result = (E) elements[(tail - 1) & (elements.length - 1)];
			if (result == null)
				throw new NoSuchElementException();
			return result;
		}

		@SuppressWarnings("unchecked")
		public E peekFirst() {
			// elements[head] is null if deque empty
			return (E) elements[head];
		}

		@SuppressWarnings("unchecked")
		public E peekLast() {
			return (E) elements[(tail - 1) & (elements.length - 1)];
		}

		/**
		 * Removes the first occurrence of the specified element in this deque (when
		 * traversing the deque from head to tail). If the deque does not contain the
		 * element, it is unchanged. More formally, removes the first element {@code e}
		 * such that {@code o.equals(e)} (if such an element exists). Returns
		 * {@code true} if this deque contained the specified element (or equivalently,
		 * if this deque changed as a result of the call).
		 *
		 * @param o element to be removed from this deque, if present
		 * @return {@code true} if the deque contained the specified element
		 */
		public boolean removeFirstOccurrence(Object o) {
			if (o == null)
				return false;
			int mask = elements.length - 1;
			int i = head;
			Object x;
			while ((x = elements[i]) != null) {
				if (o.equals(x)) {
					delete(i);
					return true;
				}
				i = (i + 1) & mask;
			}
			return false;
		}

		/**
		 * Removes the last occurrence of the specified element in this deque (when
		 * traversing the deque from head to tail). If the deque does not contain the
		 * element, it is unchanged. More formally, removes the last element {@code e}
		 * such that {@code o.equals(e)} (if such an element exists). Returns
		 * {@code true} if this deque contained the specified element (or equivalently,
		 * if this deque changed as a result of the call).
		 *
		 * @param o element to be removed from this deque, if present
		 * @return {@code true} if the deque contained the specified element
		 */
		public boolean removeLastOccurrence(Object o) {
			if (o == null)
				return false;
			int mask = elements.length - 1;
			int i = (tail - 1) & mask;
			Object x;
			while ((x = elements[i]) != null) {
				if (o.equals(x)) {
					delete(i);
					return true;
				}
				i = (i - 1) & mask;
			}
			return false;
		}

		// *** Queue methods ***

		/**
		 * Inserts the specified element at the end of this deque.
		 *
		 * <p>
		 * This method is equivalent to {@link #addLast}.
		 *
		 * @param e the element to add
		 * @return {@code true} (as specified by {@link Collection#add})
		 * @throws NullPointerException if the specified element is null
		 */
		public boolean add(E e) {
			addLast(e);
			return true;
		}

		/**
		 * Inserts the specified element at the end of this deque.
		 *
		 * <p>
		 * This method is equivalent to {@link #offerLast}.
		 *
		 * @param e the element to add
		 * @return {@code true} (as specified by {@link Queue#offer})
		 * @throws NullPointerException if the specified element is null
		 */
		public boolean offer(E e) {
			return offerLast(e);
		}

		/**
		 * Retrieves and removes the head of the queue represented by this deque.
		 *
		 * This method differs from {@link #poll poll} only in that it throws an
		 * exception if this deque is empty.
		 *
		 * <p>
		 * This method is equivalent to {@link #removeFirst}.
		 *
		 * @return the head of the queue represented by this deque
		 * @throws NoSuchElementException {@inheritDoc}
		 */
		public E remove() {
			return removeFirst();
		}

		/**
		 * Retrieves and removes the head of the queue represented by this deque (in
		 * other words, the first element of this deque), or returns {@code null} if
		 * this deque is empty.
		 *
		 * <p>
		 * This method is equivalent to {@link #pollFirst}.
		 *
		 * @return the head of the queue represented by this deque, or {@code null} if
		 *         this deque is empty
		 */
		public E poll() {
			return pollFirst();
		}

		/**
		 * Retrieves, but does not remove, the head of the queue represented by this
		 * deque. This method differs from {@link #peek peek} only in that it throws an
		 * exception if this deque is empty.
		 *
		 * <p>
		 * This method is equivalent to {@link #getFirst}.
		 *
		 * @return the head of the queue represented by this deque
		 * @throws NoSuchElementException {@inheritDoc}
		 */
		public E element() {
			return getFirst();
		}

		/**
		 * Retrieves, but does not remove, the head of the queue represented by this
		 * deque, or returns {@code null} if this deque is empty.
		 *
		 * <p>
		 * This method is equivalent to {@link #peekFirst}.
		 *
		 * @return the head of the queue represented by this deque, or {@code null} if
		 *         this deque is empty
		 */
		public E peek() {
			return peekFirst();
		}

		// *** Stack methods ***

		/**
		 * Pushes an element onto the stack represented by this deque. In other words,
		 * inserts the element at the front of this deque.
		 *
		 * <p>
		 * This method is equivalent to {@link #addFirst}.
		 *
		 * @param e the element to push
		 * @throws NullPointerException if the specified element is null
		 */
		public void push(E e) {
			addFirst(e);
		}

		/**
		 * Pops an element from the stack represented by this deque. In other words,
		 * removes and returns the first element of this deque.
		 *
		 * <p>
		 * This method is equivalent to {@link #removeFirst()}.
		 *
		 * @return the element at the front of this deque (which is the top of the stack
		 *         represented by this deque)
		 * @throws NoSuchElementException {@inheritDoc}
		 */
		public E pop() {
			return removeFirst();
		}

		private void checkInvariants() {
			assert elements[tail] == null;
			assert head == tail ? elements[head] == null
					: (elements[head] != null && elements[(tail - 1) & (elements.length - 1)] != null);
			assert elements[(head - 1) & (elements.length - 1)] == null;
		}

		/**
		 * Removes the element at the specified position in the elements array,
		 * adjusting head and tail as necessary. This can result in motion of elements
		 * backwards or forwards in the array.
		 *
		 * <p>
		 * This method is called delete rather than remove to emphasize that its
		 * semantics differ from those of {@link List#remove(int)}.
		 *
		 * @return true if elements moved backwards
		 */
		private boolean delete(int i) {
			checkInvariants();
			final Object[] elements = this.elements;
			final int mask = elements.length - 1;
			final int h = head;
			final int t = tail;
			final int front = (i - h) & mask;
			final int back = (t - i) & mask;

			// Invariant: head <= i < tail mod circularity
			if (front >= ((t - h) & mask))
				throw new ConcurrentModificationException();

			// Optimize for least element motion
			if (front < back) {
				if (h <= i) {
					System.arraycopy(elements, h, elements, h + 1, front);
				} else { // Wrap around
					System.arraycopy(elements, 0, elements, 1, i);
					elements[0] = elements[mask];
					System.arraycopy(elements, h, elements, h + 1, mask - h);
				}
				elements[h] = null;
				head = (h + 1) & mask;
				return false;
			} else {
				if (i < t) { // Copy the null tail as well
					System.arraycopy(elements, i + 1, elements, i, back);
					tail = t - 1;
				} else { // Wrap around
					System.arraycopy(elements, i + 1, elements, i, mask - i);
					elements[mask] = elements[0];
					System.arraycopy(elements, 1, elements, 0, t);
					tail = (t - 1) & mask;
				}
				return true;
			}
		}

		public boolean remove(int i) {
			final int mask = elements.length - 1;
			i += head;
			i = i & mask;
			checkInvariants();
			final Object[] elements = this.elements;

			final int h = head;
			final int t = tail;
			final int front = (i - h) & mask;
			final int back = (t - i) & mask;

			// Invariant: head <= i < tail mod circularity
			if (front >= ((t - h) & mask))
				throw new ConcurrentModificationException();

			// Optimize for least element motion
			if (front < back) {
				if (h <= i) {
					System.arraycopy(elements, h, elements, h + 1, front);
				} else { // Wrap around
					System.arraycopy(elements, 0, elements, 1, i);
					elements[0] = elements[mask];
					System.arraycopy(elements, h, elements, h + 1, mask - h);
				}
				elements[h] = null;
				head = (h + 1) & mask;
				return false;
			} else {
				if (i < t) { // Copy the null tail as well
					System.arraycopy(elements, i + 1, elements, i, back);
					tail = t - 1;
				} else { // Wrap around
					System.arraycopy(elements, i + 1, elements, i, mask - i);
					elements[mask] = elements[0];
					System.arraycopy(elements, 1, elements, 0, t);
					tail = (t - 1) & mask;
				}
				return true;
			}
		}

		// *** Collection Methods ***

		/**
		 * Returns the number of elements in this deque.
		 *
		 * @return the number of elements in this deque
		 */
		public int size() {
			return (tail - head) & (elements.length - 1);
		}

		/**
		 * Returns {@code true} if this deque contains no elements.
		 *
		 * @return {@code true} if this deque contains no elements
		 */
		public boolean isEmpty() {
			return head == tail;
		}

		/**
		 * Returns an iterator over the elements in this deque. The elements will be
		 * ordered from first (head) to last (tail). This is the same order that
		 * elements would be dequeued (via successive calls to {@link #remove} or popped
		 * (via successive calls to {@link #pop}).
		 *
		 * @return an iterator over the elements in this deque
		 */
		public Iterator<E> iterator() {
			return new DeqIterator();
		}

		public Iterator<E> descendingIterator() {
			return new DescendingIterator();
		}

		private class DeqIterator implements Iterator<E> {
			/**
			 * Index of element to be returned by subsequent call to next.
			 */
			private int cursor = head;

			/**
			 * Tail recorded at construction (also in remove), to stop iterator and also to
			 * check for comodification.
			 */
			private int fence = tail;

			/**
			 * Index of element returned by most recent call to next. Reset to -1 if element
			 * is deleted by a call to remove.
			 */
			private int lastRet = -1;

			public boolean hasNext() {
				return cursor != fence;
			}

			public E next() {
				if (cursor == fence)
					throw new NoSuchElementException();
				@SuppressWarnings("unchecked")
				E result = (E) elements[cursor];
				// This check doesn't catch all possible comodifications,
				// but does catch the ones that corrupt traversal
				if (tail != fence || result == null)
					throw new ConcurrentModificationException();
				lastRet = cursor;
				cursor = (cursor + 1) & (elements.length - 1);
				return result;
			}

			public void remove() {
				if (lastRet < 0)
					throw new IllegalStateException();
				if (delete(lastRet)) { // if left-shifted, undo increment in next()
					cursor = (cursor - 1) & (elements.length - 1);
					fence = tail;
				}
				lastRet = -1;
			}

			public void forEachRemaining(Consumer<? super E> action) {
				Objects.requireNonNull(action);
				Object[] a = elements;
				int m = a.length - 1, f = fence, i = cursor;
				cursor = f;
				while (i != f) {
					@SuppressWarnings("unchecked")
					E e = (E) a[i];
					i = (i + 1) & m;
					if (e == null)
						throw new ConcurrentModificationException();
					action.accept(e);
				}
			}
		}

		private class DescendingIterator implements Iterator<E> {
			/*
			 * This class is nearly a mirror-image of DeqIterator, using tail instead of
			 * head for initial cursor, and head instead of tail for fence.
			 */
			private int cursor = tail;
			private int fence = head;
			private int lastRet = -1;

			public boolean hasNext() {
				return cursor != fence;
			}

			public E next() {
				if (cursor == fence)
					throw new NoSuchElementException();
				cursor = (cursor - 1) & (elements.length - 1);
				@SuppressWarnings("unchecked")
				E result = (E) elements[cursor];
				if (head != fence || result == null)
					throw new ConcurrentModificationException();
				lastRet = cursor;
				return result;
			}

			public void remove() {
				if (lastRet < 0)
					throw new IllegalStateException();
				if (!delete(lastRet)) {
					cursor = (cursor + 1) & (elements.length - 1);
					fence = head;
				}
				lastRet = -1;
			}
		}

		/**
		 * Returns {@code true} if this deque contains the specified element. More
		 * formally, returns {@code true} if and only if this deque contains at least
		 * one element {@code e} such that {@code o.equals(e)}.
		 *
		 * @param o object to be checked for containment in this deque
		 * @return {@code true} if this deque contains the specified element
		 */
		public boolean contains(Object o) {
			if (o == null)
				return false;
			int mask = elements.length - 1;
			int i = head;
			Object x;
			while ((x = elements[i]) != null) {
				if (o.equals(x))
					return true;
				i = (i + 1) & mask;
			}
			return false;
		}

		/**
		 * Removes a single instance of the specified element from this deque. If the
		 * deque does not contain the element, it is unchanged. More formally, removes
		 * the first element {@code e} such that {@code o.equals(e)} (if such an element
		 * exists). Returns {@code true} if this deque contained the specified element
		 * (or equivalently, if this deque changed as a result of the call).
		 *
		 * <p>
		 * This method is equivalent to {@link #removeFirstOccurrence(Object)}.
		 *
		 * @param o element to be removed from this deque, if present
		 * @return {@code true} if this deque contained the specified element
		 */
		public boolean remove(Object o) {
			return removeFirstOccurrence(o);
		}

		/**
		 * Removes all of the elements from this deque. The deque will be empty after
		 * this call returns.
		 */
		public void clear() {
			int h = head;
			int t = tail;
			if (h != t) { // clear all cells
				head = tail = 0;
				int i = h;
				int mask = elements.length - 1;
				do {
					elements[i] = null;
					i = (i + 1) & mask;
				} while (i != t);
			}
		}

		/**
		 * Returns an array containing all of the elements in this deque in proper
		 * sequence (from first to last element).
		 *
		 * <p>
		 * The returned array will be "safe" in that no references to it are maintained
		 * by this deque. (In other words, this method must allocate a new array). The
		 * caller is thus free to modify the returned array.
		 *
		 * <p>
		 * This method acts as bridge between array-based and collection-based APIs.
		 *
		 * @return an array containing all of the elements in this deque
		 */
		public Object[] toArray() {
			return copyElements(new Object[size()]);
		}

		/**
		 * Returns an array containing all of the elements in this deque in proper
		 * sequence (from first to last element); the runtime type of the returned array
		 * is that of the specified array. If the deque fits in the specified array, it
		 * is returned therein. Otherwise, a new array is allocated with the runtime
		 * type of the specified array and the size of this deque.
		 *
		 * <p>
		 * If this deque fits in the specified array with room to spare (i.e., the array
		 * has more elements than this deque), the element in the array immediately
		 * following the end of the deque is set to {@code null}.
		 *
		 * <p>
		 * Like the {@link #toArray()} method, this method acts as bridge between
		 * array-based and collection-based APIs. Further, this method allows precise
		 * control over the runtime type of the output array, and may, under certain
		 * circumstances, be used to save allocation costs.
		 *
		 * <p>
		 * Suppose {@code x} is a deque known to contain only strings. The following
		 * code can be used to dump the deque into a newly allocated array of
		 * {@code String}:
		 *
		 * <pre>
		 * {
		 * 	&#64;code
		 * 	String[] y = x.toArray(new String[0]);
		 * }
		 * </pre>
		 *
		 * Note that {@code toArray(new Object[0])} is identical in function to
		 * {@code toArray()}.
		 *
		 * @param a the array into which the elements of the deque are to be stored, if
		 *          it is big enough; otherwise, a new array of the same runtime type is
		 *          allocated for this purpose
		 * @return an array containing all of the elements in this deque
		 * @throws ArrayStoreException  if the runtime type of the specified array is
		 *                              not a supertype of the runtime type of every
		 *                              element in this deque
		 * @throws NullPointerException if the specified array is null
		 */
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			int size = size();
			if (a.length < size)
				a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
			copyElements(a);
			if (a.length > size)
				a[size] = null;
			return a;
		}

		// *** Object methods ***

		/**
		 * Returns a copy of this deque.
		 *
		 * @return a copy of this deque
		 */
		public ArrayDeque<E> clone() {
			try {
				@SuppressWarnings("unchecked")
				ArrayDeque<E> result = (ArrayDeque<E>) super.clone();
				result.elements = Arrays.copyOf(elements, elements.length);
				return result;
			} catch (CloneNotSupportedException e) {
				throw new AssertionError();
			}
		}

		private static final long serialVersionUID = 2340985798034038923L;

		/**
		 * Saves this deque to a stream (that is, serializes it).
		 *
		 * @serialData The current size ({@code int}) of the deque, followed by all of
		 *             its elements (each an object reference) in first-to-last order.
		 */
		private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
			s.defaultWriteObject();

			// Write out size
			s.writeInt(size());

			// Write out elements in order.
			int mask = elements.length - 1;
			for (int i = head; i != tail; i = (i + 1) & mask)
				s.writeObject(elements[i]);
		}

		/**
		 * Reconstitutes this deque from a stream (that is, deserializes it).
		 */
		private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
			s.defaultReadObject();

			// Read in size and allocate array
			int size = s.readInt();
			allocateElements(size);
			head = 0;
			tail = size;

			// Read in all elements in the proper order.
			for (int i = 0; i < size; i++)
				elements[i] = s.readObject();
		}

		/**
		 * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em> and
		 * <em>fail-fast</em> {@link Spliterator} over the elements in this deque.
		 *
		 * <p>
		 * The {@code Spliterator} reports {@link Spliterator#SIZED},
		 * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
		 * {@link Spliterator#NONNULL}. Overriding implementations should document the
		 * reporting of additional characteristic values.
		 *
		 * @return a {@code Spliterator} over the elements in this deque
		 * @since 1.8
		 */
		public Spliterator<E> spliterator() {
			return new DeqSpliterator<E>(this, -1, -1);
		}

		static final class DeqSpliterator<E> implements Spliterator<E> {
			private final ArrayDeque<E> deq;
			private int fence; // -1 until first use
			private int index; // current index, modified on traverse/split

			/** Creates new spliterator covering the given array and range */
			DeqSpliterator(ArrayDeque<E> deq, int origin, int fence) {
				this.deq = deq;
				this.index = origin;
				this.fence = fence;
			}

			private int getFence() { // force initialization
				int t;
				if ((t = fence) < 0) {
					t = fence = deq.tail;
					index = deq.head;
				}
				return t;
			}

			public DeqSpliterator<E> trySplit() {
				int t = getFence(), h = index, n = deq.elements.length;
				if (h != t && ((h + 1) & (n - 1)) != t) {
					if (h > t)
						t += n;
					int m = ((h + t) >>> 1) & (n - 1);
					return new DeqSpliterator<>(deq, h, index = m);
				}
				return null;
			}

			public void forEachRemaining(Consumer<? super E> consumer) {
				if (consumer == null)
					throw new NullPointerException();
				Object[] a = deq.elements;
				int m = a.length - 1, f = getFence(), i = index;
				index = f;
				while (i != f) {
					@SuppressWarnings("unchecked")
					E e = (E) a[i];
					i = (i + 1) & m;
					if (e == null)
						throw new ConcurrentModificationException();
					consumer.accept(e);
				}
			}

			public boolean tryAdvance(Consumer<? super E> consumer) {
				if (consumer == null)
					throw new NullPointerException();
				Object[] a = deq.elements;
				int m = a.length - 1, f = getFence(), i = index;
				if (i != fence) {
					@SuppressWarnings("unchecked")
					E e = (E) a[i];
					index = (i + 1) & m;
					if (e == null)
						throw new ConcurrentModificationException();
					consumer.accept(e);
					return true;
				}
				return false;
			}

			public long estimateSize() {
				int n = getFence() - index;
				if (n < 0)
					n += deq.elements.length;
				return (long) n;
			}

			@Override
			public int characteristics() {
				return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.SUBSIZED;
			}
		}

	}

	public static int[] toArrayInteger(List<Integer> arr) {
		int[] a = new int[arr.size()];
		for (int i = 0; i < a.length; i++) {
			a[i] = arr.get(i);
		}
		return a;
	}

	public static double[] toArrayDouble(List<Double> arr) {
		double[] a = new double[arr.size()];
		for (int i = 0; i < a.length; i++) {
			a[i] = arr.get(i);
		}
		return a;
	}

	public static String[] toArrayString(Collection<String> arr) {
		String[] a = new String[arr.size()];
		int i = 0;
		for (String s : arr) {
			a[i++] = s;
		}
		return a;
	}

	public static <_Ty> _Ty[] toArray(Collection<_Ty> arr) {
		if (arr.isEmpty()) {
			return null;
		}

		// @SuppressWarnings("unchecked")
		Iterator<_Ty> it = arr.iterator();

		assert it != null : arr;
		_Ty element = it.next();

		assert element != null : arr;

		_Ty[] a = (_Ty[]) Array.newInstance(element.getClass(), arr.size());

		int i = 0;
		for (_Ty s : arr) {
			a[i++] = s;
		}
		return a;
	}

	static Map<Character, Character> charMap = null;

	static Map<Character, Character> charMap() {
		if (charMap == null) {
			try {
				charMap = new HashMap<Character, Character>();
				for (String s : new Text(workingDirectory + "models/simplify.txt")) {
					String[] res = regexSingleton(s, "(.+)\\s*=>\\s*(.+)");
					String x = res[1];
					char y = res[2].charAt(0);
					for (int i = 0; i < x.length(); ++i) {
						char ch = x.charAt(i);
						if (!Character.isWhitespace(ch)) {
							charMap.put(ch, y);
							// log.info(ch + " => " + y);
						}
					}
				}
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return charMap;
	}

	public static String simplifyString(String seg) {
		if (seg == null) {
			return null;
		}

		char last = '\0';
		for (int i = 0; i < seg.length(); ++i) {
			char ch = seg.charAt(i);
			if (!charMap().containsKey(ch) && ch != last) {
				last = ch;
				continue;
			}

			StringBuilder s = new StringBuilder(seg.substring(0, i));
			for (; i < seg.length(); ++i) {
				ch = seg.charAt(i);
				Character chTransformed = charMap.get(ch);
				if (chTransformed != null) {
					ch = chTransformed;
				}

				if (ch != last)
					last = ch;
				else
					continue;

				s.append(last);
			}
			return s.toString();
		}
		return seg;
	}

	public static String[] simplifyString(String seg[]) {
		String[] arr = new String[seg.length];
		for (int i = 0; i < seg.length; i++) {
			arr[i] = simplifyString(seg[i]);
		}
		return arr;
	}

	static public interface ICommand {
		// the returned value indicates whether the undoing process should continue;
		public boolean run() throws Exception;
	}

	static public class Listener extends Stack<ICommand> {

		public boolean undo() throws Exception {
			while (!empty()) {
				ICommand cmd = peek();
				if (cmd.run()) {
					pop();
				} else {
					return true;
				}
			}
			return false;
		}

		public boolean undo(int multiple) throws Exception {
			for (int i = 0; i <= multiple; ++i) {
				if (!undo())
					return false;
			}
			return true;
		}
	}

	public static <_Ty> boolean islicit_order(_Ty a[], Comparator<_Ty> pred) {
		for (int i = 1; i < a.length; ++i)
			if (pred.compare(a[i], a[i - 1]) < 0)
				return false;
		return true;
	}

	public static void test_primitive_integers() {
		Integer arr[] = new Integer[10];
		for (Integer t : arr) {
			System.out.printf("%d\t", t);
			// int d = t;
			// System.out.printf("%d\t", d);
		}

		for (Integer t : arr) {
			if (t == null) {
				System.out.printf("null\t");
			} else
				System.out.printf("%d\t", t);

		}
		System.out.println();
		for (int i = 0; i < arr.length; ++i) {
			if (i % 3 == 0)
				arr[i] = i;
		}

		for (Integer t : arr) {
			if (t == null) {
				System.out.printf("null\t");
			} else
				System.out.printf("%d\t", t);

		}
	}

	public static <_Ty> void select_sort(_Ty[] a, Comparator<_Ty> pred) { // Sort a[] into increasing order.
		for (int i = 0; i < a.length; ++i) { // Exchange a[i] with smallest entry in a[i+1...N).
			int min = i; // index of minimal entr.
			for (int j = i + 1; j < a.length; ++j)
				if (pred.compare(a[j], a[min]) < 0)
					min = j;
			swap(a, i, min);
		}
	}

	public static <_Ty> boolean merge_sort(_Ty[] a, int begin, int mid, int end, Comparator<_Ty> pred, _Ty[] b) {
		if (begin == mid || mid == end || pred.compare(a[mid - 1], a[mid]) < 0)
			return false;
		for (int i = begin, j = mid, k = begin; k < end; ++k)
			if (i >= mid)
				b[k] = a[j++];
			else if (j >= end)
				b[k] = a[i++];
			else if (pred.compare(a[i], a[j]) < 0) // on condition that i < mid && j < end
				b[k] = a[i++];
			else
				b[k] = a[j++];
		return true;
	}

	public static <_Ty> boolean merge_sort(_Ty a[], int begin, int mid, int end, Comparator<_Ty> pred, _Ty[] b,
			boolean drapeau) {
		if (begin == mid || mid == end)
			return false;
		_Ty _a[] = drapeau ? b : a;
		if (pred.compare(a[mid - 1], _a[mid]) < 0) {
			if (drapeau) {
				System.arraycopy(b, mid, a, mid, end - mid);
			}
			return false;
		}
		for (int i1 = begin, i2 = mid, k = begin; k < end; ++k)
			if (i1 >= mid)
				b[k] = _a[i2++];
			else if (i2 >= end)
				b[k] = a[i1++];
			else if (pred.compare(a[i1], _a[i2]) < 0) // on condition that i < mid && j < end
				b[k] = a[i1++];
			else
				b[k] = _a[i2++];
		return true;
	}

	public static <_Ty> void merge_sort(_Ty[] a, Comparator<_Ty> pred) {
		_Ty b[] = (_Ty[]) new Object[a.length]; // Allocate space just once.
		if (merge_sort(a, 0, a.length, pred, b))
			System.arraycopy(b, 0, a, 0, b.length);
	}

	private static <_Ty> boolean merge_sort(_Ty[] a, int begin, int end, Comparator<_Ty> pred, _Ty b[]) {
		if (end - begin <= 1)
			return false;
		int mid = (begin + end) >> 1;
		boolean drapeau1 = merge_sort(a, begin, mid, pred, b);
		boolean drapeau2 = merge_sort(a, mid, end, pred, b);

		if (drapeau1) {
			_Ty tmp[] = a;
			a = b;
			b = tmp;
		}

		return drapeau1 ^ merge_sort(a, begin, mid, end, pred, b, drapeau2 ^ drapeau1);
	}

	public static <_Ty> void insert_sort(_Ty[] a, Comparator<_Ty> pred) {
		insert_sort(a, 0, a.length, pred, 1);
	}

	public static <_Ty> void insert_sort(_Ty[] a, int begin, int end, Comparator<_Ty> pred, int h) { // Sort a[] into
																										// increasing
																										// order.
		for (int i = begin + h; i < end; ++i) // Insert a[i] among a[i-h], a[i-2*h], a[i-3*h]... .
			for (int j = i; j >= h && pred.compare(a[j], a[j - h]) < 0; j -= h)
				swap(a, j, j - h);
	}

	public static <_Ty> void shell_sort(_Ty[] a, Comparator<_Ty> pred) { // Sort a[] into increasing order.
		// 1, 4, 13, 40, 121, 364, 1093, ...
		int h = 1;// h[n] = (3 ^ n - 1) / 3;
		while (h < a.length)
			h = 3 * h + 1;
		while ((h /= 3) >= 1) // h-sort the array.
			insert_sort(a, 0, a.length, pred, h);
	}

	public static <_Ty> void quick_sort(_Ty[] a, Comparator<_Ty> pred) {
		quick_sort(a, 0, a.length, pred);
	}

	public static <_Ty> int sentinel(_Ty[] a, int i, int j, Comparator<_Ty> pred) {
		--j;
		int sentinel = (i + j) >> 1;

		if (pred.compare(a[i], a[sentinel]) < 0) {
			if (pred.compare(a[j], a[i]) < 0)
				sentinel = i;
			else if (pred.compare(a[j], a[sentinel]) < 0)
				sentinel = j;
		} else {
			if (pred.compare(a[i], a[j]) < 0)
				sentinel = i;
			else if (pred.compare(a[sentinel], a[j]) < 0)
				sentinel = j;
		}
		return sentinel;
	}

	public static <_Ty> void quick_sort(_Ty[] a, int begin, int end, Comparator<_Ty> pred) {
		if (end - begin <= 5) {
			insert_sort(a, begin, end, pred, 1);
			return;
		}

		swap(a, begin, sentinel(a, begin, end, pred));

		_Ty pivot = a[begin]; // partitioning item
		int i = begin, j = end; // left and right scan indices

		for (;;) { // Scan right, scan left, check for scan complete, and exchange.
			while (pred.compare(a[++i], pivot) < 0)
				; // if (i == end) break;
			while (pred.compare(pivot, a[--j]) < 0)
				; // if (j == begin) break;
			if (i < j)
				swap(a, i, j);
			else
				break;
		}
		swap(a, begin, j); // Put v = a[j] into position, with a[begin..j) <= a[j] <= a(j..end).

		quick_sort(a, begin, j, pred); // Sort left part a[begin .. j-1].
		++j;
		quick_sort(a, j, end, pred); // Sort right part a[j+1 .. end].
	}

	public static <_Ty> void swap(Vector<_Ty> a, int i, int j) {
		_Ty tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}

	public static <_Ty> void duplicate_sort(_Ty[] a, int begin, int end, Comparator<_Ty> pred) { // See page 289 for
																									// public sort()
																									// that calls this
																									// method.
		if (end - begin <= 1)
			return;
		int x = begin, i = begin + 1, y = end;
		_Ty pivot = a[begin];
		while (i < y) {
			if (pred.compare(a[i], pivot) < 0)
				swap(a, x++, i++); // make sure that all the element less than pivot is put before the equal range;
			else if (pred.compare(pivot, a[i]) < 0)
				swap(a, i, --y); // make sure that all the element more than pivot is put behind the equal range;
			else
				++i;
		} // Now a[begin, x) < v = a[x, y) < a[y, end).
		duplicate_sort(a, begin, x, pred);
		duplicate_sort(a, y, end, pred);
	}

	public interface ScalarVector {
		public double distance(ScalarVector o);
	}

	public static void getAverageRGB(Image img, Area validArea, double[] averageRGB) {
		assert (img != null) && (img instanceof BufferedImage);
		assert validArea != null;
		assert (averageRGB != null) && (averageRGB.length >= 4);

		Rectangle rect = validArea.getBounds();
		AffineTransform affineTransform = AffineTransform.getTranslateInstance(-rect.x, -rect.y);
		validArea = validArea.createTransformedArea(affineTransform);

		BufferedImage buffImage = (BufferedImage) img;

		int imgWidth = buffImage.getWidth();
		int imgHeight = buffImage.getHeight();

		double[] allRGB = new double[4];

		int count = 0;
		for (int i = 0; i < imgWidth; i++) {
			for (int j = 0; j < imgHeight; j++) {
				if (validArea.contains(i, j)) {
					count++;
					Color color = new Color(buffImage.getRGB(i, j), true);
					allRGB[0] += color.getBlue();
					allRGB[1] += color.getGreen();
					allRGB[2] += color.getRed();
				}
			}
		}

		if (count > 0)
			for (int i = 0; i < 3; i++)
				averageRGB[i] = allRGB[i] / count;
	}

	static Rectangle.Double nest(Rectangle.Double sheet, Rectangle.Double part) {
		assert (equ(part.y, sheet.y) && equ(part.x, sheet.x)) : "equ(part.y, sheet.y) && equ(part.x,  sheet.x)";
		part.y += part.height;
		part.height = sheet.getMaxY() - part.y;
		sheet.x += part.width;
		sheet.width -= part.width;
		return part;
	}

	static public class matrix {
		public double ptr[][];

		public matrix(int n, int m) {
			ptr = new double[n][m];
		}

		public int get_col_size() {
			return ptr[0].length;
		}

		public int get_row_size() {
			return ptr.length;
		}

		public static matrix identity(int n) {
			matrix m = new matrix(n, n);
			for (int i = 0; i < n; ++i)
				m.ptr[i][i] = 1;
			return m;
		}

		public matrix mul(matrix rhs) {
			int m = this.get_row_size(), k = rhs.get_col_size(), n = rhs.get_row_size();
			assert (this.get_col_size() == k) : "unmatched matrix sizes.";
			matrix mat = new matrix(m, n);
			for (int i = 0; i < m; ++i)
				for (int j = 0; j < n; ++j)
					for (int t = 0; t < k; ++t)
						mat.ptr[i][j] += ptr[i][t] * rhs.ptr[t][j];
			return mat;
		}
	}

	public static boolean drapeau_partition = false;
	public static boolean debug = true;
	public static boolean frage = true;
	public static boolean korrektur = false;
	public static boolean utilization_first = true;

	public static int version = 0;
	public static int spy = 0;

	public static final int TOPOLOGY = -1;
	public static final int CYAN = 0;
	public static final int MEGENTA = 1;
	public static final int YELLOW = 2;
	public static final int BLACK = 3;
	public static int SORTOPTION = TOPOLOGY;
	public static final double EPSILON = 1.0 / (1 << 15);
	public static final double QUADRANT = 90.0;
	public static final double VIERTEL = Math.PI / 2;

	static public class pair<_Kty, _Ty> {
		static public class Integer {
			public Integer() {
				this(0, 0);
			}

			public Integer(int x, int y) {
				this.x = x;
				this.y = y;
			}

			public int x, y;
		}

		// static public class IntegerLong {
		// public IntegerLong() {
		// this(0, 0);
		// }
		//
		// public IntegerLong(int x, long y) {
		// this.x = x;
		// this.y = y;
		// }
		//
		// public int x;
		// public long y;
		// }

		static public class Double {
			public Double() {
				this(0, 0);
			}

			public Double(double x, double y) {
				this.x = x;
				this.y = y;
			}

			public double x, y;
		}

		public pair(pair<_Kty, _Ty> rhs) {// copy constructor;
			x = rhs.x;
			y = rhs.y;
		}

		public pair(_Kty x, _Ty y) {
			this.x = x;
			this.y = y;
		}

		public pair() {
		}

		public void set(pair<_Kty, _Ty> rhs) {
			x = rhs.x;
			y = rhs.y;
		}

		public void set(_Kty x, _Ty y) {
			this.x = x;
			this.y = y;
		}

		public void swap(pair<_Kty, _Ty> rhs) {
			pair<_Kty, _Ty> tmp = new pair<_Kty, _Ty>(this);
			set(rhs);
			rhs.set(tmp);
		}

		public _Kty x;
		public _Ty y;
	}

	public static void print(Rectangle.Double rc) {
		System.out.printf("x = %6.1f, y = %6.1f, w = %6.1f, h = %6.1f, x' = %6.1f, y' = %6.1f\n", rc.x, rc.y, rc.width,
				rc.height, rc.getMaxX(), rc.getMaxY());
	}

	public static <_Ty> void merge_sort(_Ty arr1[], _Ty arr2[], _Ty ret[], Comparator<_Ty> pred) {
		merge_sort(arr1, arr1.length, arr2, arr2.length, ret, pred);
	}

	public static <_Ty extends Comparable<_Ty>> _Ty[] merge_sort(_Ty arr1[], _Ty arr2[]) {
		@SuppressWarnings("unchecked")
		// _Ty ret[] = (_Ty[]) new Object[arr1.length + arr2.length];
		_Ty[] ret = (_Ty[]) Array.newInstance(arr1.getClass().getComponentType(), arr1.length + arr2.length);
		merge_sort(arr1, arr1.length, arr2, arr2.length, ret, new Comparator<_Ty>() {

			@Override
			public int compare(_Ty o1, _Ty o2) {
				return o1.compareTo(o2);
			}

		});
		return ret;
	}

	@SafeVarargs
	public static <_Ty extends Comparable<_Ty>> _Ty[] merge_sort(_Ty[]... arr) {
		_Ty[] sum = arr[0];
		for (int i = 1; i < arr.length; ++i) {
			sum = merge_sort(sum, arr[i]);
		}
		return sum;
	}

	// precondition: the destine array is not the same as the source arrays;
	public static <_Ty> void merge_sort(_Ty arr1[], int sz1, _Ty arr2[], int sz2, _Ty dst[], Comparator<_Ty> pred) {
		int i = 0, j = 0, k = 0;
		while (i < sz1 && j < sz2) {
			if (pred.compare(arr1[i], arr2[j]) < 0)
				dst[k++] = arr1[i++];
			else
				dst[k++] = arr2[j++];
		}
		while (i < sz1)
			dst[k++] = arr1[i++];
		while (j < sz2)
			dst[k++] = arr2[j++];
	}

	public static boolean intersect(Rectangle.Double source, Rectangle.Double target) {
		return less(source.x, target.getMaxX()) && less(source.y, target.getMaxY()) && less(target.x, source.getMaxX())
				&& less(target.y, source.getMaxY());
	}

	public static boolean include(Rectangle.Double source, Rectangle.Double target) {
		return !less(target.x, source.x) && !less(target.y, source.y) && !less(source.getMaxX(), target.getMaxX())
				&& !less(source.getMaxY(), target.getMaxY());
	}

	public static interface criteria<_Ty> {
		public boolean eligible(_Ty candidate);
	}

	// return the index of the first element that is not eligible; ie, search for
	// the first defective.
	public static <_Ty> int binary_search(Vector<_Ty> arr, criteria<_Ty> eligible) {
		int begin = 0, end = arr.size();
		for (;;) {
			int mid = (begin + end) >> 1;
			if (begin == end)
				return mid;
			if (eligible.eligible(arr.get(mid)))
				begin = mid + 1;
			else
				end = mid;
		}
	}

	public static <_Ty> int binary_search(_Ty arr[], criteria<_Ty> eligible) {
		int begin = 0, end = arr.length;
		for (;;) {
			int mid = (begin + end) >> 1;
			if (begin == end)
				return mid;
			if (eligible.eligible(arr[mid]))
				begin = mid + 1;
			else
				end = mid;
		}
	}

	// post-condition: retur a value in the range of [0, length]; the value returned
	// is no less than _Val;
	public static <_Ty> int binary_search(_Ty arr[], int length, _Ty _Val, Comparator<_Ty> pred) {
		int begin = 0, end = length;
		for (;;) {
			int mid = (begin + end) >> 1;
			if (begin == end)
				return mid;
			if (pred.compare(arr[mid], _Val) < 0)
				begin = mid + 1;
			else
				end = mid;
		}
	}

	// post-condition: retur a value in the range of [0, length]; the value returned
	// is no less than _Val;
	public static <_Ty> int binary_search(Vector<_Ty> arr, int length, _Ty _Val, Comparator<_Ty> pred) {
		int begin = 0, end = length;
		for (;;) {
			int mid = (begin + end) >> 1;
			if (begin == end)
				return mid;
			if (pred.compare(arr.get(mid), _Val) < 0)
				begin = mid + 1;
			else
				end = mid;
		}
	}

	public static Point2D.Double offset_ll(double dx, double dy, double radian) {// the lower left corner of the
																					// bounding box;
		while (radian < 0)
			radian += 2 * Math.PI;
		while (radian >= Math.PI * 2)
			radian -= 2 * Math.PI;
		double cos = Math.cos(radian);
		double sin = Math.sin(radian);
		double x = 0, y = 0;
		if (radian < Math.PI / 2) {
			x = -dy * sin;
		} else if (radian < Math.PI) {
			x = dx * cos - dy * sin;
			y = dy * cos;
		} else if (radian < Math.PI * 3 / 2) {
			x = dx * cos;
			y = dx * sin + dy * cos;
		} else {
			y = dx * sin;
		}
		return new Point2D.Double(x, y);
	}

	public static Point2D.Double offset_ur(double dx, double dy, double radian) {// the up right corner of the bounding
																					// box;
		while (radian < 0)
			radian += 2 * Math.PI;
		while (radian >= Math.PI * 2)
			radian -= 2 * Math.PI;
		double cos = Math.cos(radian);
		double sin = Math.sin(radian);
		double x, y;
		if (radian < Math.PI / 2) {
			x = dx * cos;
			y = dx * sin + dy * cos;
		} else if (radian < Math.PI) {
			x = 0;
			y = dx * sin;
		} else if (radian < Math.PI * 3 / 2) {
			x = -dy * sin;
			y = 0;
		} else {
			x = dx * cos - dy * sin;
			y = dy * cos;
		}
		return new Point2D.Double(x, y);
	}

	public static boolean negative(double x) {
		if (Math.abs(x) < EPSILON)
			return false;
		return x < 0;
	}

	public static boolean positive(double x) {
		if (Math.abs(x) < EPSILON)
			return false;
		return x > 0;
	}

	public static boolean zero(double x) {
		if (Math.abs(x) < EPSILON)
			return true;
		return false;
	}

	public static boolean less(double d1, double d2) {
		if (Math.abs(d1 - d2) < EPSILON)
			return false;
		return Double.compare(d1, d2) < 0;
	}

	public static boolean equ(double d1, double d2) {
		if (Math.abs(d1 - d2) < EPSILON)
			return true;
		return Double.compare(d1, d2) == 0;
	}

	public static int compare(double d1, double d2) {
		if (Math.abs(d1 - d2) < EPSILON)
			return 0;
		return Double.compare(d1, d2);
	}

	public static Point2D.Double swap(Point2D.Double pt) {
		double tmp = pt.x;
		pt.x = pt.y;
		pt.y = tmp;
		return pt;
	}

	public static int floor(double x) {
		if (negative(x))
			return -ceil(-x);
		int n = (int) x;
		if (equ((double) (n + 1), x))
			return n + 1;
		return n;
	}

	public static int ceil(double x) {
		if (negative(x))
			return -floor(-x);
		int n = (int) x;
		if (equ((double) n, x))
			return n;
		return n + 1;
	}

	public static <_Ty> _Ty max(_Ty arr[], Comparator<_Ty> _Pr) {
		_Ty var = arr[0];
		for (int i = 1; i < arr.length; ++i) {
			if (_Pr.compare(var, arr[i]) < 0)
				var = arr[i];
		}
		return var;
	}

	public static <_Ty> _Ty min(_Ty arr[], Comparator<_Ty> _Pr) {
		_Ty var = arr[0];
		for (int i = 1; i < arr.length; ++i) {
			if (_Pr.compare(arr[i], var) < 0)
				var = arr[i];
		}
		return var;
	}

	static Rectangle.Double diminish(Rectangle.Double oasis, double dx) {
		oasis.x += dx;
		oasis.width -= dx;
		return oasis;
	}

	static int integers(double width[], int i, double a, double b) {
		if (i == width.length)
			return 0;
		if (less(width[i], a)) {
			int n = integers(width, i + 1, a - width[i], b - width[i]);
			if (n > 0) {
				return n + 1;
			}
		} else {
			if (!less(b, width[i]))
				return 1;
		}

		return integers(width, i + 1, a, b);
	}

	public static int integers(double width[], double a, double b) {
		return integers(width, 0, a, b);
	}

	// return a combination of k elements selected among {0, 1, 2, n - 2, n - 1};
	public static int[][] integers(int n, int k) {
		assert n >= k && k > 0 : "n >= k && k > 0";
		int v[][] = new int[cmb(n, k)][];
		int x[] = new int[k];
		int index = 0;

		int i = 0;
		do {
			if (x[i] <= n - (k - i)) {
				if (i == k - 1) {
					v[index++] = x.clone();
				} else {
					++i;
					x[i] = x[i - 1];
				}
				++x[i];
			} else {
				--i;
				++x[i]; // backtracking to the previous index.
			}
		} while (x[0] <= n - k);
		return v;
	}

	public static ArrayList list() {
		return new ArrayList<>();
	}

	public static HashSet set() {
		return new HashSet<>();
	}

	public static HashMap dict() {
		return new HashMap<>();
	}

	public static void print(Object... arr) {
		for (int i = 0; i < arr.length;) {
			System.out.print(arr[i]);
			System.out.print(++i == arr.length ? "\n" : " ");
		}
	}
	
	public static DoubleMatrix[] add(DoubleMatrix[] x, DoubleMatrix[] y) {
		DoubleMatrix[] z = new DoubleMatrix[x.length];
		for (int i = 0; i < x.length; ++i) {
			z[i] = x[i].add(y[i]);
		}

		return z;
	}

	
	public static DoubleMatrix addi(DoubleMatrix x, DoubleMatrix y) {
		if (x == null)
			return y;
		if (y == null)
			return x;

		return x.addi(y);
	}

	public static DoubleMatrix[] addi(DoubleMatrix[] x, DoubleMatrix[] y) {
		for (int i = 0; i < x.length; ++i) {
			x[i].addi(y[i]);
		}

		return x;
	}

	public static DoubleMatrix[] divi(DoubleMatrix[] x, double y) {
		for (int i = 0; i < x.length; ++i) {
			x[i].divi(y);
		}

		return x;
	}


	public static DoubleMatrix[] concatHorizontally(DoubleMatrix[] x, DoubleMatrix[] y) {
		DoubleMatrix[] z = new DoubleMatrix[x.length];
		for (int i = 0; i < x.length; ++i) {
			z[i] = DoubleMatrix.concatHorizontally(x[i], y[i]);
		}
		return z;
	}

	public static DoubleMatrix[] muli(DoubleMatrix[] x, DoubleMatrix[] y) {
		for (int i = 0; i < x.length; ++i) {
			x[i].muli(y[i]);
		}

		return x;
	}

	public static class BinaryReader {
		public DataInputStream dis;
		private String s_FilePath;

		public BinaryReader(String file) throws FileNotFoundException {
			s_FilePath = file;
			dis = new DataInputStream(new FileInputStream(new File(s_FilePath)));
		}

		public double[] readArray1() throws IOException {
			int dimension = dis.readInt();
			System.out.printf("x = %d\n", dimension);
			double[] arr = new double[dimension];
			for (int i = 0; i < arr.length; ++i) {
				arr[i] = dis.readDouble();
			}
//			System.out.println(Utility.toString(arr));
			return arr;
		}

		public double[][] readArray2() throws IOException {
			int dimension0 = dis.readInt();
			int dimension1 = dis.readInt();
			System.out.printf("x = %d, y = %d\n", dimension0, dimension1);

			double[][] arr = new double[dimension0][dimension1];
			for (int i0 = 0; i0 < arr.length; ++i0) {
				for (int i1 = 0; i1 < arr[i0].length; ++i1) {
					arr[i0][i1] = dis.readDouble();
				}
			}
//			System.out.println(Utility.toString(arr[0]));
			return arr;
		}

		public HashMap<String, Integer> readMap(HashMap<String, Integer> word2id) throws IOException {
			int length = dis.readInt();
			System.out.printf("length = %d\n", length);

			for (int i = 0; i < length; ++i) {
				char[] arr = new char[dis.readInt()];
				for (int j = 0; j < arr.length; ++j) {
					arr[j] = dis.readChar();
				}
				word2id.put(new String(arr), dis.readInt());
			}

			return word2id;
		}

		public HashMap<Character, Integer> readCharMap(HashMap<Character, Integer> word2id) throws IOException {
			int length = dis.readInt();
			System.out.printf("length = %d\n", length);

			for (int i = 0; i < length; ++i) {
				word2id.put(dis.readChar(), dis.readInt());
			}

			return word2id;
		}

		public double[][][] readArray3() throws IOException {
			int dimension0 = dis.readInt();
			int dimension1 = dis.readInt();
			int dimension2 = dis.readInt();
			System.out.printf("d0 = %d, d1 = %d, d2 = %d\n", dimension0, dimension1, dimension2);
			double[][][] arr = new double[dimension0][dimension1][dimension2];
			for (int i0 = 0; i0 < arr.length; ++i0) {
				for (int i1 = 0; i1 < arr[i0].length; ++i1) {
					for (int i2 = 0; i2 < arr[i0][i1].length; ++i2) {
						arr[i0][i1][i2] = dis.readDouble();
					}
				}
			}
//			System.out.println(Utility.toString(arr[0][0]));
			return arr;
		}

		public double[][][][] readArray4() throws IOException {
			int dimension0 = dis.readInt();
			int dimension1 = dis.readInt();
			int dimension2 = dis.readInt();
			int dimension3 = dis.readInt();
			System.out.printf("d0 = %d, d1 = %d, d2 = %d, d3 = %d\n", dimension0, dimension1, dimension2, dimension3);
			double[][][][] arr = new double[dimension0][dimension1][dimension2][dimension3];
			for (int i0 = 0; i0 < arr.length; ++i0) {
				for (int i1 = 0; i1 < arr[i0].length; ++i1) {
					for (int i2 = 0; i2 < arr[i0][i1].length; ++i2) {
						for (int i3 = 0; i3 < arr[i0][i1][i2].length; ++i3) {
							arr[i0][i1][i2][i3] = dis.readDouble();
						}
					}
				}
			}
//			System.out.println(Utility.toString(arr[0][0][0]));
			return arr;
		}

		public double[][][][][] readArray5() throws IOException {
			int dimension0 = dis.readInt();
			int dimension1 = dis.readInt();
			int dimension2 = dis.readInt();
			int dimension3 = dis.readInt();
			int dimension4 = dis.readInt();
			System.out.printf("d0 = %d, d1 = %d, d2 = %d, d3 = %d, d4 = %d\n", dimension0, dimension1, dimension2,
					dimension3, dimension4);
			double[][][][][] arr = new double[dimension0][dimension1][dimension2][dimension3][dimension4];
			for (int i0 = 0; i0 < arr.length; ++i0) {
				for (int i1 = 0; i1 < arr[i0].length; ++i1) {
					for (int i2 = 0; i2 < arr[i0][i1].length; ++i2) {
						for (int i3 = 0; i3 < arr[i0][i1][i2].length; ++i3) {
							for (int i4 = 0; i4 < arr[i0][i1][i2][i3].length; ++i4) {
								arr[i0][i1][i2][i3][i4] = dis.readDouble();
							}
						}
					}
				}
			}
//			System.out.println(Utility.toString(arr[0][0][0][0]));
			return arr;
		}

		public void readBinaryStream() {
			try {
				if (dis != null) {
					while (dis.available() > 0) {
						System.out.println(dis.available());
						System.out.println(dis.readBoolean());
						char c = (char) dis.readChar();
						System.out.println(c);
						System.out.println(dis.readDouble());
						System.out.println(dis.readFloat());
						System.out.println(dis.readInt());
						System.out.println(dis.readLong());
						System.out.println(dis.readShort());
						System.out.println(dis.readUTF());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static double toDouble(float x) {
		return x;
	}

	public static double[] toDouble(float x[]) {
		double[] y = new double[x.length];
		for (int i = 0; i < y.length; i++) {
			y[i] = x[i];
		}
		return y;
	}

	public static double[][] toDouble(float x[][]) {
		double[][] y = new double[x.length][];
		for (int i = 0; i < y.length; i++) {
			y[i] = toDouble(x[i]);
		}
		return y;
	}

	static public double min(DoubleMatrix x, int row_index) {
		double v = Double.POSITIVE_INFINITY;
		for (int i = row_index, end = row_index + x.columns * x.rows; i < end; i += x.rows) {
			double xi = x.get(i);
			if (xi == xi && xi < v) {
				v = xi;
			}
		}
		return v;
	}

	static public int argmin(DoubleMatrix x, int row_index) {
		double v = Double.POSITIVE_INFINITY;
		int argmin = -1;
		for (int i = row_index, end = row_index + x.columns * x.rows, j = 0; i < end; i += x.rows, ++j) {
			double xi = x.get(i);
			if (xi == xi && xi < v) {
				v = xi;
				argmin = j;
			}
		}
		return argmin;
	}

	static public int[] argmin(DoubleMatrix x) {
		int[] arr = new int[x.rows];
		for (int i = 0; i < x.rows; ++i) {
			arr[i] = argmin(x, i);
		}
		return arr;
	}

	static public DoubleMatrix min(DoubleMatrix x) {
		double[] arr = new double[x.rows];
		for (int i = 0; i < x.rows; ++i) {
			arr[i] = min(x, i);
		}
		// return a row vector!
		return new DoubleMatrix(1, x.rows, arr);
	}

	static public String jsonify(Object object) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(object);
	}


	static public <String> String[] tuple(String... arr) {
		return arr;
	}
	static public <String> List<String> list(String... arr) {
		ArrayList<String> list = new ArrayList<String>(arr.length);
		for (int i = 0; i < arr.length; i++) {
			list.add(arr[i]);
		}

		return list;
	}
	static public <T> T dejsonify(String json, Class<T> valueType) throws IOException {
		return new ObjectMapper().readValue(json, valueType);
	}

}