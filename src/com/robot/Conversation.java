package com.robot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.robot.QACouplet.Origin;
import com.robot.Sentence.Protagonist;
import com.util.Utility;
import com.util.Utility.Couplet;

public class Conversation {
	Sentence[] sent;
	Date[] date;
	String content;
	String visitor;

	public Conversation(String visitor, String content) throws ParseException, Exception {
		this.visitor = visitor;
		this.content = content;
		Vector<Sentence> history = new Vector<Sentence>();
		Vector<Date> timeList = new Vector<Date>();
		Date time0 = null;
		for (String[] a : Utility.regex(content, regexExcel)) {
			String speaker = a[0].trim();
			Protagonist protagonist = speaker.equals(visitor) ? Protagonist.CUSTOMER : Protagonist.OPERATOR;
			Date time = null;
			try {
				time = Utility.parseDateFormat(a[1]);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (time0 != null && time != null) {
				if (time.before(time0)) {
					log.info("error in time: ");
					log.info("time before: " + time0);
					log.info("time after : " + time);

					log.info("content:" + content);
					continue;
				}
				time0 = time;
			}

			String str = filter(format(a[2].trim()));
			if (str != null) {
				try {

					history.add(new Sentence(str, protagonist, speaker));
					timeList.add(time);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		initialize(history, timeList);
	}

	public boolean isPrivateInformation() {
		if (visitor.matches(".*1[\\d]{10}.*")) {
			return true;
		}

		for (Sentence sent : this.sent) {
			if (sent.sentence.matches(".*1[\\d]{10}.*")) {
				return true;
			}
		}
		return false;
	}

	public Conversation(String content) throws ParseException, Exception {
		Vector<Sentence> history = new Vector<Sentence>();
		Vector<Date> time = new Vector<Date>();

		// log.info("res.length = " + res.length);
		for (String[] s : Utility.regex(content, regexHtml)) {
			// log.info();
			// log.info();
			// for (int i = 1; i < str.length; ++i){
			// log.info(Utility.format(str[i]));
			// }
			// log.info(str[1]);
			// log.info(str[2]);
			// log.info(str[3]);
			// log.info(str[4]);
			// log.info(str[7]);
			// log.info(Utility.format(str[8]));
			// log.info();
			//
			if (s[10] != null)
				continue;

			String sentence[] = s[7].split("\n+");

			Protagonist protagonist = protagonist(s);
			String speaker = s[4];
			Date date = Utility.parseDateFormat(s[6]);
			for (String sent : sentence) {
				sent = filter(format(sent));
				if (sent == null)
					continue;

				history.add(new Sentence(sent, protagonist, speaker));
				time.add(date);
			}
		}

		initialize(history, time);
	}

	void initialize(Vector<Sentence> history, Vector<Date> time) throws Exception {
		this.sent = history.toArray(new Sentence[history.size()]);
		this.date = time.toArray(new Date[time.size()]);
		if (sent.length != date.length) {
			throw new Exception("sent.length != date.length in " + Utility.__FUNCTION__());
		}
	}

	// decompile the dialog into a jeson string.
	public String compile(Couplet<Vector<Sentence>, Vector<Date>> dialog) throws ParseException, Exception {
		return null;
	}

	public static String timeFormat = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
	static String regexHtml = "<div class='(\\S+)'>" + "<div class='(\\S+)'>" + "<div class='(\\S+)'>" + "<span class='(\\S+)'>" + "<sender>([\\s\\S]+?)</sender>" + "(&nbsp;)*" + "<time>(" + timeFormat + ")</time></span><br>" + "<span><content>([\\s\\S]*?)</content></span>" + "<div style='display:none;'><receiver>(\\S*)</receiver>" + "(</div>){4}" + "(<div class='msg_back_success clearfix'>[\\s\\S]*?</div>)?";
	//	static String regexHtml = "<div class='(\\S+)'>" + "<div class='(\\S+)'>" + "<div class='(\\S+)'>" + "<span class='(\\S+)'>" + "<sender>([\\s\\S]+?)<\\\\{0,1}/sender>" + "(&nbsp;)*" + "<time>(" + timeFormat + ")<\\\\{0,1}/time><\\\\{0,1}/span><br>" + "<span><content>([\\s\\S]*?)<\\\\{0,1}/content><\\\\{0,1}/span>" + "<div style='display:none;'><receiver>(\\S*)<\\\\{0,1}/receiver>" + "(<\\\\{0,1}/div>){4}" + "(<div class='msg_back_success clearfix'>[\\s\\S]*?<\\\\{0,1}/div>)?";
	public static final String regexExcel = "([^\n]+)[ ]{4}(" + timeFormat + ")\n{1,}([\\s\\S]*?)\n{2,}";

	static String decompile(String str) throws Exception {
		String[] res = Utility.regexSingleton(str, "([\\S]+?):([\\s\\S]+)");
		String protagonist = res[1];

		int index = -1;
		if (protagonist.equals("CUSTOMER")) {
			index = Protagonist.CUSTOMER.ordinal();
		} else if (protagonist.equals("OPERATOR")) {
			index = Protagonist.OPERATOR.ordinal();
		} else {
			throw new Exception("illegal protagonist = " + protagonist);
		}
		return decompile(res[2], index);
	}

	static String decompile(Sentence str) throws Exception {
		return decompile(str.sentence, str.protagonist.ordinal());
	}

	static String decompile(String content, int protagonist) throws Exception {
		return "<div class='" + roleStr[protagonist][0] + "'><div class='" + roleStr[protagonist][1] + "'><div class='" + roleStr[protagonist][2] + "'><span class='" + roleStr[protagonist][3] + "'><sender>" + "sender" + "</sender>&nbsp;&nbsp;&nbsp;&nbsp;<time>" + Utility.toString(new Date()) + "</time></span><br><span><content>" + content.trim() + "</content></span><div style='display:none;'><receiver>" + "receiver" + "</receiver></div></div></div></div>";
	}

	// decompile the dialog into a jeson string.
	public String decompile() throws ParseException, Exception {
		String str = "";
		for (Sentence s : sent) {
			str += s.decompile();
		}
		return str;
	}

	// decompile the dialog into a jeson string.
	static public String decompile(String[] sent) throws ParseException, Exception {
		String str = "";
		for (String s : sent) {
			str += decompile(s);
		}
		return str;
	}

	// decompile the dialog into a jeson string.
	static public String decompile(Sentence[] sent) throws ParseException, Exception {
		String str = "";
		for (Sentence s : sent) {
			str += decompile(s);
		}
		return str;
	}

	/**
	 * make sure that SentenceClassifier.INDEX_CUSTOMER = 0;
	 * SentenceClassifier.INDEX_OPERATOR = 1;
	 * 
	 */
	static String[][] roleStr = { { "me_chat", "me_box", "me_01", "me_title" }, { "sevice_chat", "chat_box", "chat_01", "chat_title" }, { "robot_chat", "robot_box", "rb_01", "robot_title", }, };

	static boolean isprotagonist(String[] operatorStr, String str[]) {
		for (int i = 0; i < operatorStr.length; ++i) {
			if (str[i].equals(operatorStr[i]))
				continue;
			return false;
		}
		return true;
	}

	static Protagonist protagonist(String str[]) throws Exception {
		if (isprotagonist(roleStr[Protagonist.OPERATOR.ordinal()], str))
			return Protagonist.OPERATOR;
		//		if (isprotagonist(roleStr[SentenceClassifier.INDEX_CUSTOMER], str))
		//			return SentenceClassifier.INDEX_CUSTOMER;
		return Protagonist.CUSTOMER;
	}

	public static void main(String[] args) throws ParseException, Exception {
		String str = "语音识别结果：塔妮娅女士您好，很高兴为您服务";
		log.info(str);
		str = filter(str);
		log.info(str);
		//		for (String content : new Utility.StringReader(Utility.workingDirectory + "corpus/conversation.txt")){
		//					Conversation conversation = new Conversation(content);
		//			for (Sentence s : conversation.sent) {
		//				log.info(s);
		//			}			
		//		}
	}

	ArrayList<QACouplet> compile() throws Exception {
		ArrayList<QACouplet> res = new ArrayList<QACouplet>();

		for (Sentence sentence : this.sent) {
			log.info(sentence.protagonist + " " + sentence.qatype() + " : " + sentence.sentence);
		}

		for (int i = 0; i < this.sent.length; ++i) {
			i = Sentence.find_question_sequential(this.sent, i);
			if (i < 0)
				break;
			Utility.Couplet<Integer, Double> pair = Sentence.find_answer_sequential(this.sent, i);
			if (pair == null)
				continue;

			int j = pair.x;
			Sentence question = sent[i];

			if (question.isIncompleteQuestion() && i > 0) {
				String prev = sent[i - 1].sentence;
				String punct = "";
				if (",.:;!?，。：；！？、…．".indexOf(prev.charAt(prev.length() - 1)) < 0) {
					punct += "，";
				}

				question = new Sentence(prev + punct + question.sentence, Protagonist.CUSTOMER);
			}

			Sentence answer = this.sent[j];
			double confidence = pair.y;

			log.info("question  : " + question);
			log.info("answer    : " + answer);
			log.info("confidence: " + confidence);
			log.info("time: " + Utility.toString(this.date[j]));

			res.add(new QACouplet(question, answer, confidence, this.date[j], this.sent[j].speaker, Origin.ROBOT_RESERVOIR));
		}

		return res;
	}

	static String filter(String str) {
		if (str == null)
			return null;
		str = str.toLowerCase();
		//		"语音识别结果：你能识别吗？"
		String[] res = Utility.regexSingleton(str, "语音识别结果：([\\s\\S]*)");

		if (res != null) {
			str = res[1].trim();
		}

		String punct = "[，。；！：,.;!:]*";
		String monseiur = "\\S{0,3}(先生|女士|小姐)";
		String content = "([\\s\\S]*)";
		res = Utility.regexSingleton(str, punct + monseiur + punct + content);
		if (res != null) {
			str = res[2].trim();
		}

		String bonsur = "(你好|您好|请问|亲|hello)";
		res = Utility.regexSingleton(str, punct + bonsur + punct + content);
		if (res != null) {
			str = res[2].trim();
		}
		res = Utility.regexSingleton(str, punct + monseiur + punct + content);
		if (res != null) {
			str = res[2].trim();
		}
		if (str.isEmpty())
			return null;
		return str;
	}

	public String toHTML() {
		String content = "";
		for (int i = 0; i < sent.length; i++) {
			int indexProtagonist = sent[i].protagonist.ordinal();
			content += "<div class='" + roleStr[indexProtagonist][0] + "'><div class='" + roleStr[indexProtagonist][1] + "'><div class='" + roleStr[indexProtagonist][2] + "'><span class='" + roleStr[indexProtagonist][3] + "'>";
			content += "<sender>" + sent[i].speaker + "</sender>&nbsp;&nbsp;&nbsp;&nbsp;";
			content += "<time>" + Utility.toString(date[i]) + "</time></span><br>";
			content += "<span><content>" + sent[i].sentence + "</content></span>";
			content += "<div style='display:none;'><receiver></receiver></div></div></div></div>";
		}

		return content;
	}

	public static String format(String content) {
		content = content.trim();
		content = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(content);
		content = content.replaceAll("[\\s]+", " ");
		content = content.replaceAll("<.*?>", ";");

		content = content.trim();
		content = remove_unwanted(content, CharCriteria.CharCriteriaSpace);

		content = content.replaceAll(";+", ";");
		if (content.length() == 0)
			return null;

		if (content.charAt(0) == ';')
			content = content.substring(1);

		if (content.length() == 0)
			return null;

		if (content.charAt(content.length() - 1) == ';')
			content = content.substring(0, content.length() - 1);

		return remove_unwanted(content, CharCriteria.CharCriteriaSemiclon);
	}

	static abstract class CharCriteria {
		CharCriteria(char ch) {
			space = ch;
		}

		abstract boolean unwanted(String str, int i);

		char space;

		static CharCriteria CharCriteriaSpace = new CharCriteria(' ') {
			public boolean unwanted(String str, int i) {
				int type = Utility.chartype(str.charAt(i - 1));
				int type1 = Utility.chartype(str.charAt(i + 1));

				return type != Utility.CHAR_ENGLISH && type != Utility.CHAR_DIGIT || type1 != Utility.CHAR_ENGLISH && type1 != Utility.CHAR_DIGIT;
			}
		};

		static CharCriteria CharCriteriaSemiclon = new CharCriteria(';') {
			String criteria = ",.:;!?，。：；！？、…";

			public boolean unwanted(String str, int i) {
				return criteria.indexOf(str.charAt(i + 1)) >= 0 || criteria.indexOf(str.charAt(i - 1)) >= 0;
			}
		};
	}

	static String remove_unwanted(String str, CharCriteria criteria) {
		Vector<Integer> arrIndex = new Vector<Integer>();

		for (int i = 0; i < str.length(); ++i) {
			if (str.charAt(i) != criteria.space)
				continue;

			if (criteria.unwanted(str, i))
				arrIndex.add(i);
			++i;
		}

		if (arrIndex.size() == 0)
			return str;

		int i = 0;
		String tmp = str.substring(0, arrIndex.get(i));
		for (++i; i < arrIndex.size(); ++i) {
			tmp += str.substring(arrIndex.get(i - 1) + 1, arrIndex.get(i));
		}
		tmp += str.substring(arrIndex.get(i - 1) + 1);
		return tmp;
	}

	public static Logger log = Logger.getLogger(Conversation.class);
}
