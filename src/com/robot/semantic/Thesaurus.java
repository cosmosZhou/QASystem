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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.log4j.Logger;

import com.util.Utility;

/**
 * 本类用来分析《哈工大同义词林》 http://jyc.5156edu.com/ http://fyc.5156edu.com/html2/1.html
 * http://www.thesaurus.com/ http://xh.5156edu.com/ http://hanyu.baidu.com/
 * http://dict.baidu.com/
 * 
 * @author Administrator
 * @version 1.0
 * @since 1.0
 */
public class Thesaurus implements Serializable {

	HashMap<String, Etymology> mapEtymology = new HashMap<String, Etymology>();
	/**
	 * 
	 */
	private static final long serialVersionUID = -8769545243512672019L;

	// 3.初始化词林相关
	public void initialize() throws Exception {
		//		Utility.removeString(Utility.workingDirectory + "models//synset.txt", new Utility.StringReader(Utility.workingDirectory + "models//remove.txt").collect(new ArrayList<String>()));
		for (String str : new Utility.Text(Utility.workingDirectory + "models//synset.txt")) {
			String[] word = str.split("(\\s*=\\s*)|(\\s*~\\s*)|(\\s*\\|\\s*)|(\\s*-\\s*)|(\\s*<\\s*)");
			Etymology lexeme[] = new Etymology[word.length];
			for (int i = 0; i < lexeme.length; i++) {
				String x = word[i].intern();
				if (!mapEtymology.containsKey(x)) {
					mapEtymology.put(x, new Etymology(x));
				}
				lexeme[i] = mapEtymology.get(x);
			}

			if (str.indexOf('<') >= 0) {
				for (int i = 1; i < lexeme.length; ++i) {
					try {
						lexeme[i - 1].addHypernym(lexeme[i]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				//				try {
				//					Etymology.synchronizeHypernymSet(lexeme);
				//				} catch (Exception e) {
				//					log.info("while executing:");
				//					log.info(str);
				//					e.printStackTrace();
				//					throw e;
				//				}

				if (str.indexOf('=') >= 0) {
					for (int i = 0; i < lexeme.length; ++i) {
						for (int j = i + 1; j < lexeme.length; ++j) {
							lexeme[i].equivalent.add(lexeme[j]);
							lexeme[j].equivalent.add(lexeme[i]);
						}
					}
				} else if (str.indexOf('-') >= 0) {
					for (int i = 0; i < lexeme.length; ++i) {
						for (int j = i + 1; j < lexeme.length; ++j) {
							lexeme[i].synonym.add(lexeme[j]);
							lexeme[j].synonym.add(lexeme[i]);
						}
					}
				} else if (str.indexOf('~') >= 0) {
					for (int i = 0; i < lexeme.length; ++i) {
						for (int j = i + 1; j < lexeme.length; ++j) {
							lexeme[i].relative.add(lexeme[j]);
							lexeme[j].relative.add(lexeme[i]);
						}
					}
				} else if (str.indexOf('|') >= 0) {
					if (lexeme.length != 2) {
						throw new RuntimeException("synonym.length != 2");
					}

					lexeme[0].antonym.add(lexeme[1]);
					lexeme[1].antonym.add(lexeme[0]);
				} else {
					throw new RuntimeException(str);
				}
			}
		}
	}

	public boolean synonymous(String key1, String key2) {
		return similarity(key1, key2) >= 0.9;
	}

	public boolean equivalent(String key1, String key2) {
		return similarity(key1, key2) >= 1.0;
	}

	public boolean antonymous(String key1, String key2) {
		return similarity(key1, key2) <= -0.9;
	}

	/**
	 * calculate the semantic similarity between the two words;
	 * 
	 * @param key1
	 * @param key2
	 * @return
	 */
	public double similarity(String key1, String key2) {
		if (key1 == null) {
			if (key2 == null) {
				return 1.0;
			} else
				return 0;
		}
		if (key2 == null)
			return 0;
		key1 = key1.toLowerCase();
		key2 = key2.toLowerCase();
		if (key1.equals(key2)) {
			return 1;
		}
		Etymology e1 = mapEtymology.get(key1);
		Etymology e2 = mapEtymology.get(key2);
		if (e1 == null || e2 == null) {
			return 0;
		}

		return e1.similarity(e2);
	}

	public static void main(String args[]) throws ClassNotFoundException, IOException {
		log.info("test begins.");
		instance.test("女王", "皇帝");
		instance.test("女王", "母亲");
		instance.test("女王", "妓女");
		instance.test("武则天", "仙女");
		instance.test("武则天", "皇帝");
		instance.test("计算机", "计算器");
		instance.test("计算机", "电脑");
		instance.test("咱俩", "我们");
		instance.test("我们", "我们");
		instance.test("炊事员", "门房");
		instance.test("大脑", "人脑");

		instance.test("价格", "价钱");
		instance.test("价格", "价位");
		instance.test("价格", "钱");
		instance.test("价格", "股票");
		instance.test("价格", "钞票");

		instance.test("卖", "出售");
		instance.test("办", "SOLVE");
		instance.test("办", "干");
		instance.test("办", "解决");

		instance.test("涵意", "含义");
		instance.test("涵义", "涵意");
		instance.test("意思", "涵义");
		instance.test("意思", "涵意");
		instance.test("什么", "WHAT");
		instance.test("WHAT", "COMBIEN");
		instance.test("WHAT", "how much");
		instance.test("WHAT", "多少");
		instance.test("how much", "多少");

		log.info("test synonyms...");
		instance.testSynonym("COMBIEN", new String[] { "多少", "几几", "几", "几个" });
		instance.testSynonym("WHAT", new String[] { "何", "什么", "啥", "什麼", "何许" });
		instance.testSynonym("WHICH", new String[] { "哪些", "哪一", "哪个", "哪家", "哪辆", "哪种", "哪双" });
		instance.testSynonym("HOW", new String[] { "怎么", "咋", "多", "怎样", "如何", "何如", "咋样" });
		instance.testSynonym("WHERE", new String[] { "哪里", "哪儿", "何处", "何方", "哪" });
		instance.testSynonym("WHEN", new String[] { "何时", "啥时" });
		instance.testSynonym("WHY", new String[] { "咋能", "咋会", "为啥", "何故", "为何", "凭啥", "干嘛" });
		instance.testSynonym("WHO", new String[] { "谁", "哪位" });

		instance.testSynonym("de", new String[] { "的", "地", "得" });
		instance.testSynonym("meaning", new String[] { "东西", "东东", "含义", "意义", "意思", "涵义", "涵意" });
		instance.testSynonym("if", new String[] { "假设", "假若", "假如", "假定", "如果", "若", "倘若", "已知", "假使", "倘使", "若是", "若使", "要是", "如是" });
		instance.testSynonym("tell", new String[] { "说", "提供", "告知", "介绍", "告诉", "解释", "讲讲", "讲", "谈谈", "谈", "阐明", "阐释", "解答", "回答", "说明" });
		instance.testSynonym("query", new String[] { "查询", "查看", "查詢", "查" });
		instance.testSynonym("SOLVE", new String[] { "搞", "办", "弄", "解决", "处理", "应对", "应付" });
		instance.testSynonym("know", new String[] { "知道", "了解", "知晓", "知悉", "熟悉", "通晓" });
		instance.testSynonym("ask", new String[] { "问", "咨询", "询问" });
		instance.testSynonym("knowhow", new String[] { "流程", "方法", "途径", "法子", "方式" });
		instance.testSynonym("reason", new String[] { "原因", "缘故", "缘由", "理由", "故", "回事", "说法" });
		instance.testSynonym("want", new String[] { "需要", "想", "想要", "希望", "就想", "只想" });
		instance.testSynonym("need", new String[] { "要", "需要", "就要", "是要" });
		instance.testSynonym("good", new String[] { "好", "行", "可以", "可行", "可好" });
		instance.testSynonym("TIME", new String[] { "时间", "时候", "时期", "时刻", "号", "时", "点", "分", "月份", "年" });
		instance.testSynonym("suffer", new String[] { "受到", "接受", "遭遇", "遇到" });
		instance.testSynonym("place", new String[] { "地方", "处", "地点", "地址" });
		instance.testSynonym("people", new String[] { "人", "位", "同志", "同事", "家伙", "人士", "伙计" });
		instance.testSynonym("interjection", new String[] { "呀", "嘛", "噢", "嗯", "嗨", "哼", "唉", "啦", "喃", "哈", "啊", "呗", "喀", "哟", "哦", "恩", "嘻", "嗯", "咯" });
		instance.testSynonym("interrogation", new String[] { "吗", "吧", "么", "麽", "呢" });
		instance.testSynonym("status", new String[] { "样", "样子", "情况", "状况", "状态" });
		instance.testSynonym("equal", new String[] { "有", "就是", "是", "为", "等于" });
		instance.testSynonym("has", new String[] { "有", "剩余", "剩下", "剩余" });
		instance.testSynonym("less", new String[] { "小于" });
		instance.testSynonym("above", new String[] { "到", "够" });
		instance.testSynonym("more", new String[] { "大于", "超过" });
		instance.testSynonym("belong", new String[] { "属于", "属" });
		instance.testSynonym("include", new String[] { "包括", "包含" });
		instance.testSynonym("inside", new String[] { "在", "在于", "位于" });
		instance.testSynonym("not", new String[] { "不", "没", "否", "非", "非也", "没有", "不是", "無", "无", "未" });

	}

	public double test(String word1, String word2) {
		double sim = 0;
		sim = similarity(word1, word2);// 计算两个词的相似度
		System.out.println("similarity = " + sim + " between " + word1 + "  " + word2);
		return sim;
	}

	public void testSynonym(String word1, String word2[]) {
		for (String str : word2) {
			if (test(word1, str) < 0.9) {
				throw new RuntimeException("test(word1, str) < 0.9");
			}
		}
	}

	public void synSetFromHarbin() throws IOException {
		String trainingCorpusTxt = Utility.workingDirectory + "models//synset.txt";
		Utility.setOut(trainingCorpusTxt);
		int maxLength[] = new int[5];
		for (String str : new Utility.Text(Utility.workingDirectory + "models//synonym.txt")) {
			String[] synset = str.split("\\s+");
			String synLable = synset[0];
			String vector[] = new String[5];
			vector[0] = synLable.substring(0, 1);
			vector[1] = synLable.substring(1, 2);
			vector[2] = synLable.substring(2, 4);
			vector[3] = synLable.substring(4, 5);
			vector[4] = synLable.substring(5, 7);
			int index[] = new int[5];
			index[0] = vector[0].charAt(0) - 'A';
			index[1] = vector[1].charAt(0) - 'a';
			index[2] = Integer.parseInt(vector[2]) - 1;
			index[3] = vector[3].charAt(0) - 'A';
			index[4] = Integer.parseInt(vector[4]) - 1;

			for (int i = 0; i < index.length; ++i) {
				if (index[i] > maxLength[i]) {
					maxLength[i] = index[i];
				}
			}
		}

		for (int i = 0; i < maxLength.length; ++i) {
			++maxLength[i];
		}

		log.info(Utility.toString(maxLength));

		String[][][][][] map = new String[maxLength[0]][maxLength[1]][maxLength[2]][maxLength[3]][maxLength[4]];

		for (String str : new Utility.Text(Utility.workingDirectory + "nlp/synonym.txt")) {
			String[] synset = str.split("\\s+");
			//			
			String synLable = synset[0];
			String[] synonym = Arrays.copyOfRange(synset, 1, synset.length);

			switch (Utility.last(synLable)) {
			case '=':
				System.out.print(synonym[0]);
				for (int i = 1; i < synonym.length; ++i) {
					System.out.print(" = " + synonym[i]);
				}
				System.out.println();
				break;

			case '@':
				break;
			case '#':
				System.out.print(synonym[0]);
				for (int i = 1; i < synonym.length; ++i) {
					System.out.print(" ~ " + synonym[i]);
				}
				System.out.println();
				break;
			default:
				throw new RuntimeException("Utility.last(synLable) = " + Utility.last(synLable));
			}

			synLable = synLable.substring(0, synLable.length() - 1);
			//			log.info(synLable);
			String vector[] = new String[5];
			vector[0] = synLable.substring(0, 1);
			vector[1] = synLable.substring(1, 2);
			vector[2] = synLable.substring(2, 4);
			vector[3] = synLable.substring(4, 5);
			vector[4] = synLable.substring(5, 7);
			int index[] = new int[5];
			index[0] = vector[0].charAt(0) - 'A';
			index[1] = vector[1].charAt(0) - 'a';
			index[2] = Integer.parseInt(vector[2]) - 1;
			index[3] = vector[3].charAt(0) - 'A';
			index[4] = Integer.parseInt(vector[4]) - 1;
			map[index[0]][index[1]][index[2]][index[3]][index[4]] = synonym[0];

			if (index[4] > 0) {
				for (String s : synonym) {
					System.out.println(s + " < " + map[index[0]][index[1]][index[2]][index[3]][0]);
				}
			} else if (index[3] > 0) {
				for (String s : synonym) {
					System.out.println(s + " < " + map[index[0]][index[1]][index[2]][0][0]);
				}
			} else if (index[2] > 0) {
				for (String s : synonym) {
					System.out.println(s + " < " + map[index[0]][index[1]][0][0][0]);
				}
			} else if (index[1] > 0) {
				for (String s : synonym) {
					System.out.println(s + " < " + map[index[0]][0][0][0][0]);
				}
			}
		}

	}

	private static Logger log = Logger.getLogger(Thesaurus.class);
	public static Thesaurus instance;
	static {
		try {
			//			log.info("test begins.");
			instance = new Thesaurus();
			instance.initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
