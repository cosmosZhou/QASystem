package com.robot;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.robot.Sentence.Protagonist;
import com.robot.Sentence.QATYPE;
import com.util.Utility;

public class InformationExtraction {

	public static void main(String[] args) throws Exception {
		String fileName = Utility.workingDirectory + "corpus/chatRecord.xls";

		InformationExtraction knowledgeExtraction = new InformationExtraction(fileName);
		//		knowledgeExtraction.informationExtraction();
		int cnt = 0;
		Workbook workbook = Utility.createWorkbook(true);
		Sheet sheet = workbook.createSheet("conversation");
		int rownum = 0;
		for (Conversation conversation : knowledgeExtraction.new DialogueDialysis()) {
			if (conversation.isPrivateInformation()) {
				log.info(conversation.content);
				log.info("is Private Information");
				++cnt;
				Row row = sheet.createRow(rownum++);
				row.createCell(0).setCellValue(conversation.visitor);
				row.createCell(1).setCellValue(conversation.content);
			} else {
			}
		}
		Utility.write_excel(workbook, Utility.workingDirectory + "corpus/non_private_conversation.xlsx");
		log.info("cnt = " + cnt);
	}

	public class DialogueDialysis extends com.util.Utility.Reader<Conversation> {
		public DialogueDialysis() throws Exception {
			rowsChatRecord = wb.getSheetAt(0).rowIterator();

			Sheet sheet = wb.getSheetAt(0);
			rowsChatRecord = sheet.rowIterator();
			for (;;) {
				ArrayList<String> headers = Utility.skipLinePremiere(rowsChatRecord);
				if (headers == null) {
					throw new Exception("（访客昵称）或（对话内容）列表缺失");
				}
				indexOfContent = headers.indexOf("对话内容");
				if (indexOfContent < 0) {
					indexOfContent = headers.indexOf("content");
				}

				indexOfVisitor = headers.indexOf("访客昵称");
				if (indexOfVisitor < 0) {
					indexOfVisitor = headers.indexOf("visitor");
				}

				if (indexOfContent >= 0 && indexOfVisitor >= 0) {
					break;
				}

				if (indexOfContent >= 0 && indexOfVisitor < 0) {
					log.info("（访客昵称）列表缺失");
					break;
				}

				if (indexOfContent < 0 && indexOfVisitor >= 0) {
					throw new Exception("（对话内容）列表缺失");
				}
			}

			log.info("indexOfContent = " + indexOfContent);
			log.info("indexOfVisitor = " + indexOfVisitor);
		}

		Iterator<Row> rowsChatRecord;
		int rowid = 0;

		Conversation dialog;

		@Override
		public boolean hasNext() {
			for (;;) {
				if (!rowsChatRecord.hasNext()) {
					return false;
				}
				Row row = rowsChatRecord.next(); // 获得行数据
				Cell cellContent = row.getCell(indexOfContent);
				if (cellContent == null) {
					log.info("cellContent is null");
					continue;
				}

				String content = cellContent.getStringCellValue();
				log.info("content = \n" + content);

				if (content.length() == 0)
					continue;

				String visitor = null;
				if (indexOfVisitor >= 0) {
					visitor = row.getCell(indexOfVisitor).getStringCellValue();

					log.info("visitor = " + visitor);
					if (visitor.length() == 0)
						continue;
					try {
						dialog = new Conversation(visitor, content);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				} else {
					try {
						dialog = new Conversation(content);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				}

				return true;
			}
		}

		@Override
		public Conversation next() {
			return dialog;
		}
	}

	protected String chatRecordFile;
	Workbook wb;
	int indexOfContent = -1;
	int indexOfVisitor = -1;

	public InformationExtraction(String chatRecordFile) throws Exception {
		this.chatRecordFile = chatRecordFile;
		wb = Utility.read_excel(chatRecordFile);
		//		String err = test_format_of_chat_record();
		//		if (err != null)
		//			log.info("test_format_of_chat_record, error: " + err);
	}

	class NaturalQuestionInfo {
		NaturalQuestionInfo(int pos, double confidence) {
			this.pos = pos;
			this.confidence = confidence;
		}

		int pos;
		double confidence;
	}

	int find_question_reversal(Sentence[] history, int i, double confidence[]) {
		confidence[0] = 1;
		for (--i; i >= 0; --i) {
			// if it is a OPERATOR, to see whether the old answer is overwhelmed by the new answer proposed by the OPERATOR
			confidence[0] *= history[i].confidence;
			if (history[i].protagonist == Protagonist.OPERATOR) {
				if (history[i].qatype == QATYPE.REPLY)
					break;

				continue;
			}

			if (history[i].qatype == QATYPE.REPLY)
				continue;

			return i;
		}

		return -1;
	}

	class NaturalAnswerInfo {
		NaturalAnswerInfo(int pos, int criteria, double confidence) {
			this.pos = pos;
			this.answerID = criteria;
			this.confidence = Math.sqrt(confidence);
		}

		int pos;
		int answerID;
		double confidence;
	}

	HashMap<Integer, String> answerMap = new HashMap<Integer, String>();
	HashMap<Integer, HashSet<String>> criteriaMap = new HashMap<Integer, HashSet<String>>();

	public void informationExtraction(String fileName, int maxLine) throws Exception {
		Workbook wb = Utility.read_excel(fileName);
		int rowid = 0;
		Sheet sheet = Utility.createSheet(wb, "问答对");
		Row row = sheet.createRow(rowid);
		int column = 0;
		row.createCell(column++).setCellValue("自然问");
		row.createCell(column++).setCellValue("自然答");
		row.createCell(column++).setCellValue("自信值/%");

		for (Conversation conversation : new DialogueDialysis()) {
			Sentence[] v = conversation.sent;
			for (int i = 0; i < v.length; ++i) {
				i = Sentence.find_question_sequential(v, i);
				if (i < 0)
					break;
				Utility.Couplet<Integer, Double> pair = Sentence.find_answer_sequential(v, i);
				if (pair == null)
					continue;

				String question = v[i].sentence;
				String answer = v[pair.x].sentence;
				double confidence = pair.y;

				log.info("question  : " + question);
				log.info("answer    : " + answer);
				log.info("confidence: " + confidence);

				row = sheet.createRow(++rowid);
				column = 0;
				row.createCell(column++).setCellValue(question);
				row.createCell(column++).setCellValue(answer);
				row.createCell(column++).setCellValue(confidence);

				if (rowid >= maxLine)
					break;
			}
		}
		wb.write(new FileOutputStream(fileName));
	}

	public void informationExtraction() throws Exception {
		for (Conversation conversation : new DialogueDialysis()) {
			QASystem.instance.getRepertoire("2c908088594a7757015a63f6929e5f92").update(conversation);
		}
	}

	// test_format_of_chat_record，该函数判断用户提供的历史对话记录的数据格式是否合法。 2,
	public String test_format_of_chat_record() throws Exception {
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> rows = sheet.rowIterator();
		for (;;) {
			ArrayList<String> headers = Utility.skipLinePremiere(rows);
			indexOfContent = headers.indexOf("对话内容");
			indexOfVisitor = headers.indexOf("访客昵称");
			if (indexOfContent >= 0 && indexOfVisitor >= 0) {
				break;
			}
		}

		log.info("indexOfContent = " + indexOfContent);
		log.info("indexOfCustomer = " + indexOfVisitor);
		int line = 0;
		while (rows.hasNext()) {
			++line;
			Row row = rows.next(); // 获得行数据
			String customer = row.getCell(indexOfVisitor).getStringCellValue();

			log.info("customer = " + customer);

			String content = row.getCell(indexOfContent).getStringCellValue();
			log.info("content = \n" + content);
		}

		return null;

	}

	// test_format_of_criteria_answer，该函数判断用户提供的标准答案的数据格式是否合法。 4,
	static public String test_format_of_criteria_answer(String fileName) throws Exception {
		Workbook wb = null;
		wb = Utility.read_excel(fileName);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> rows = sheet.rowIterator();
		Utility.skipLinePremiere(rows);
		int line = 0;
		while (rows.hasNext()) {
			++line;
			Row row = rows.next(); // 获得行数据
			Iterator<Cell> cells = row.cellIterator(); // 获得第一行的迭代器
			if (cells.hasNext()) {
				Cell cell = cells.next();

				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
				case Cell.CELL_TYPE_FORMULA:
					break;
				default:
					return "cell type must be numeric at line " + line + " typeinfo is " + cell.getCellType();
				}

				if (cells.hasNext()) {
					cell = cells.next();
					String answer = cell.getStringCellValue().trim();
					if (answer.length() == 0)
						return "missing answer at line " + line;
				} else
					return "missing answer at line " + line;
			} else
				log.info("missing answer id");
		}
		return null;
	}

	public static void test(String chatRecordFile) throws Exception {

		Utility.workingDirectory = new File(QASystem.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "\\";

		log.info("Utility.workingDirectory = " + Utility.workingDirectory);

		chatRecordFile = chatRecordFile.trim();
	}

	private static Logger log = Logger.getLogger(InformationExtraction.class);
}
