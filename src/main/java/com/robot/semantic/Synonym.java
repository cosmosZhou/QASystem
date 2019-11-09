/*
 * 文件名：Thesaurus.java
 * 版权：Copyright 2008-20012 复旦大学 All Rights Reserved.
 * 描述：程序总入口
 * 修改人：xpqiu
 * 修改时间：2008-12-25
 * 修改内容：新增
 *
 * 修改人：〈修改人〉
 * 修改时间：YYYY-MM-DD
 * 跟踪单号：〈跟踪单号〉
 * 修改单号：〈修改单号〉
 * 修改内容：〈修改内容〉
 */
package com.robot.semantic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import com.robot.DateBase.MySQL;
import com.util.Utility;
import com.util.Utility.DataSource.Invoker;
import com.util.Utility.DataSource.Query;

/**
 * 本类用来分析《哈工大同义词林》 http://jyc.5156edu.com/ http://fyc.5156edu.com/html2/1.html
 * http://www.thesaurus.com/ http://xh.5156edu.com/ http://hanyu.baidu.com/
 * http://dict.baidu.com/
 * 
 * @author Administrator
 * @version 1.0
 * @since 1.0
 */
public class Synonym implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8769545243512672019L;

	public HashMap<String, List<String>> keyWord_Identifier_HashMap;// <关键词，编号List集合>哈希
	public HashMap<String, Integer> first_KeyWord_Depth_HashMap;// <第一层编号，深度>哈希
	public HashMap<String, Integer> second_KeyWord_Depth_HashMap;// <前二层编号，深度>哈希
	public HashMap<String, Integer> third_KeyWord_Depth_HashMap;// <前三层编号，深度>哈希
	public HashMap<String, Integer> fourth_KeyWord_Depth_HashMap;// <前四层编号，深度>哈希
	// public HashMap<String, HashSet<String>> ciLin_Sort_keyWord_HashMap = new
	// HashMap<String, HashSet<String>>();//<(同义词)编号，关键词Set集合>哈希

	// 3.初始化词林相关
	public Synonym() {
		keyWord_Identifier_HashMap = new HashMap<String, List<String>>();
		first_KeyWord_Depth_HashMap = new HashMap<String, Integer>();
		second_KeyWord_Depth_HashMap = new HashMap<String, Integer>();
		third_KeyWord_Depth_HashMap = new HashMap<String, Integer>();
		fourth_KeyWord_Depth_HashMap = new HashMap<String, Integer>();

		int i;
		String str = null;
		String[] strs = null;
		List<String> list = null;
		BufferedReader inFile = null;
		try {
			// 初始化<关键词， 编号set>哈希
			inFile = new BufferedReader(new InputStreamReader(new FileInputStream(Utility.workingDirectory + "models/synonym/keyWord_Identifier_HashMap.txt"), "utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				list = new Vector<String>();
				for (i = 1; i < strs.length; i++)
					list.add(strs[i]);
				keyWord_Identifier_HashMap.put(strs[0], list);
			}

			// 初始化<第一层编号，高度>哈希
			inFile.close();
			inFile = new BufferedReader(new InputStreamReader(new FileInputStream(Utility.workingDirectory + "models/synonym/first_KeyWord_Depth_HashMap.txt"), "utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				first_KeyWord_Depth_HashMap.put(strs[0], Integer.valueOf(strs[1]));
			}

			// 初始化<前二层编号，高度>哈希
			inFile.close();
			inFile = new BufferedReader(new InputStreamReader(new FileInputStream(Utility.workingDirectory + "models/synonym/second_KeyWord_Depth_HashMap.txt"), "utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				second_KeyWord_Depth_HashMap.put(strs[0], Integer.valueOf(strs[1]));
			}

			// 初始化<前三层编号，高度>哈希
			inFile.close();
			inFile = new BufferedReader(new InputStreamReader(new FileInputStream(Utility.workingDirectory + "models/synonym/third_KeyWord_Depth_HashMap.txt"), "utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				third_KeyWord_Depth_HashMap.put(strs[0], Integer.valueOf(strs[1]));
			}

			// 初始化<前四层编号，高度>哈希
			inFile.close();
			inFile = new BufferedReader(new InputStreamReader(new FileInputStream(Utility.workingDirectory + "models/synonym/fourth_KeyWord_Depth_HashMap.txt"), "utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				fourth_KeyWord_Depth_HashMap.put(strs[0], Integer.valueOf(strs[1]));
			}
			inFile.close();
			save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void save() throws IOException {
		Utility.saveTo(Utility.workingDirectory + "models/Thesaurus.m", this);
	}

	static public boolean synonymous(String key1, String key2) {
		return similarity(key1, key2) >= 0.9;

	}

	/**
	 * calculate the semantic similarity between the two words;
	 * 
	 * @param key1
	 * @param key2
	 * @return
	 */
	static public double similarity(String key1, String key2) {
		if (key1 == null) {
			if (key2 == null) {
				return 1.0;
			} else
				return 0;
		}
		if (key1.equals(key2))
			return 1.0;

		if (!instance.keyWord_Identifier_HashMap.containsKey(key1) || !instance.keyWord_Identifier_HashMap.containsKey(key2)) {
			// 其中有一个不在词林中，则返回相似度为0.1
			// System.out.println(key1 + " " + key2 + "有一个不在同义词词林中！");
			return 0.05;
		}

		return instance.getMaxIdentifierSimilarity(instance.keyWord_Identifier_HashMap.get(key1), instance.keyWord_Identifier_HashMap.get(key2));
	}

	public double getMaxIdentifierSimilarity(List<String> identifierList1, List<String> identifierList2) {
		int i, j;
		double maxSimilarity = 0, similarity = 0;
		for (i = 0; i < identifierList1.size(); i++) {
			j = 0;
			while (j < identifierList2.size()) {
				similarity = getIdentifierSimilarity(identifierList1.get(i), identifierList2.get(j));
				// System.out.println(identifierList1.get(i) + " " +
				// identifierList2.get(j) + " " + similarity);
				if (similarity > maxSimilarity)
					maxSimilarity = similarity;
				// if (maxSimilarity == 1.0)
				if (maxSimilarity >= 1)
					return 0.9999;
				j++;
			}
		}
		return maxSimilarity;
	}

	// evaluate the vector similarity
	public double getIdentifierSimilarity(String identifier1, String identifier2) {
		int n = 0, k = 0;// n是分支层的节点总数, k是两个分支间的距离.
		// double a = 0.5, b = 0.6, c = 0.7, d = 0.96;
		double a = 0.65, b = 0.8, c = 0.9, d = 0.96;
		if (identifier1.equals(identifier2)) {// 在第五层相等
			if (identifier1.substring(7).equals("="))
				return 1;
			else
				return 0.5;
		} else if (identifier1.substring(0, 5).equals(identifier2.substring(0, 5))) {// 在第四层相等
																						// Da13A01=
			n = fourth_KeyWord_Depth_HashMap.get(identifier1.substring(0, 5));
			k = Integer.valueOf(identifier1.substring(5, 7)) - Integer.valueOf(identifier2.substring(5, 7));
			if (k < 0)
				k = -k;
			return Math.cos(n * Math.PI / 180) * ((double) (n - k + 1) / n) * d;
		} else if (identifier1.substring(0, 4).equals(identifier2.substring(0, 4))) {// 在第三层相等
																						// Da13A01=
			n = third_KeyWord_Depth_HashMap.get(identifier1.substring(0, 4));
			k = identifier1.substring(4, 5).charAt(0) - identifier2.substring(4, 5).charAt(0);
			if (k < 0)
				k = -k;
			return Math.cos(n * Math.PI / 180) * ((double) (n - k + 1) / n) * c;
		} else if (identifier1.substring(0, 2).equals(identifier2.substring(0, 2))) {// 在第二层相等
			n = second_KeyWord_Depth_HashMap.get(identifier1.substring(0, 2));
			k = Integer.valueOf(identifier1.substring(2, 4)) - Integer.valueOf(identifier2.substring(2, 4));
			if (k < 0)
				k = -k;
			return Math.cos(n * Math.PI / 180) * ((double) (n - k + 1) / n) * b;
		} else if (identifier1.substring(0, 1).equals(identifier2.substring(0, 1))) {// 在第一层相等
			n = first_KeyWord_Depth_HashMap.get(identifier1.substring(0, 1));
			k = identifier1.substring(1, 2).charAt(0) - identifier2.substring(1, 2).charAt(0);
			if (k < 0)
				k = -k;
			return Math.cos(n * Math.PI / 180) * ((double) (n - k + 1) / n) * a;
		}

		return 0.1;
	}

	public void learning() throws Exception {
		String[] arr = Utility.toArrayString(Synonym.instance.keyWord_Identifier_HashMap.keySet());
		for (int i = 0; i < arr.length; ++i) {
			for (int j = i; j < arr.length; ++j) {
				Synonym.instance.insertIntoDatabase(arr[i], arr[j]);
			}
		}
	}

	public static void main(String args[]) throws Exception {
		//		Synonym.instance.test("计算机", "电脑");
		//		Synonym.instance.test("咱俩", "我们");
		//		Synonym.instance.test("我们", "我们");
		//		Synonym.instance.test("炊事员", "门房");
		//		Synonym.instance.test("大脑", "人脑");
		//
		//		Synonym.instance.test("价格", "价钱");
		//		Synonym.instance.test("价格", "价位");
		//		Synonym.instance.test("价格", "钱");
		//		Synonym.instance.test("卖", "出售");
		//		Synonym.instance.test("价格", "钱");
		//		Synonym.instance.test("禁例", "律令");
		//
		Synonym.instance.learning();
	}

	public void deleteVocabulary(String key) {
		keyWord_Identifier_HashMap.remove(key);
	}

	public void training_synonym(String key1, String key2) throws IOException {
		training_synonym_in_memory(key1, key2);
		save();
	}

	// train the model so as to make two words synonyms;
	void training_synonym_in_memory(String key1, String key2) {
		if (key1.equals(key2))
			return;

		List<String> list1 = keyWord_Identifier_HashMap.get(key1);
		List<String> list2 = keyWord_Identifier_HashMap.get(key2);
		if (list1 != null) {

			if (list2 != null) {

			} else {
				list2 = new Vector<String>();
				for (String code : list1) {
					list2.add(code);
				}

				keyWord_Identifier_HashMap.put(key2, list2);

			}
		} else {
			if (list2 != null) {
				list1 = new Vector<String>();
				for (String code : list2) {
					list1.add(code);
				}

				keyWord_Identifier_HashMap.put(key1, list1);

			} else {

			}
		}

		for (String code : list1) {
			if (code.endsWith("=")) {
				if (!list2.contains(code)) {
					list2.add(code);
				}
				return;
			}
		}

		for (String code : list2) {
			if (code.endsWith("=")) {
				if (!list1.contains(code)) {
					list1.add(code);
				}
				return;
			}
		}

		for (String code : list1) {
			if (code.endsWith("#")) {
				code = code.replace('#', '=');
				if (!list1.contains(code)) {
					list1.add(code);
				}

				if (!list2.contains(code)) {
					list2.add(code);
				}
				return;
			}
		}

		for (String code : list2) {
			if (code.endsWith("#")) {
				code = code.replace('#', '=');
				if (!list1.contains(code)) {
					list1.add(code);
				}

				if (!list2.contains(code)) {
					list2.add(code);
				}
				return;
			}
		}
	}

	public void test(String word1, String word2) {
		double sim = 0;
		sim = similarity(word1, word2);// 计算两个词的相似度
		System.out.println(word1 + "/" + word2 + " = " + sim);
	}

	public void insertIntoDatabase(String x, String y) throws Exception {
		double similarity = similarity(x, y);// 计算两个词的相似度
		System.out.println(x + "/" + y + " = " + similarity);

		MySQL.instance.new Invoker<Object>() {
			@Override
			protected Object invoke() throws Exception {
				MySQL.instance.insert(x, y, (int) (similarity * 10000));
				return null;
			}
		}.execute();

	}

	public static Synonym instance;
	static {
		try {
			instance = (Synonym) Utility.loadFrom(Utility.workingDirectory + "models/Thesaurus.m");
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			instance = new Synonym();
		}
	}
}
