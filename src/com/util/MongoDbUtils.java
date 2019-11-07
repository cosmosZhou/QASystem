package com.util;


import java.text.SimpleDateFormat;
import java.util.Date;

public class MongoDbUtils {

	public static String MONGO_IP = PropertyConfig.getProperty("MONGO_IP", "121.40.130.192");
//"121.40.130.192";

	public static int MONGO_PORT = PropertyConfig.getIntProperty("MONGO_PORT", 29017);

	public static String MONGO_DB = PropertyConfig.getProperty("MONGO_DB", "ucc");;

	public static String MONGO_COLLECTION = PropertyConfig.getProperty("MONGO_COLLECTION", "ecchatrecords");;

	/**
	 * 日期转换 type = 0 yyyy-MM-dd HH:mm:ss type = 1 yyyy-MM-dd type = 2 yyyy/MM/dd
	 * type = 3 yyyyMMdd HHmmss type = 4 HH:mm:ss type = 5 yyyy年MM月dd日 HH时mm分ss秒
	 * type = 6 MM/dd/yyyy
	 * 
	 * @param date
	 * @param type
	 * @return
	 */
	public static String formatDate(Date date, int type) {
		String result = "";
		if (type < 0) {
			type = 0;
		}
		if (date != null) {

			switch (type) {
			case 0:
				result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
				break;
			case 1:
				result = new SimpleDateFormat("yyyy-MM-dd").format(date);
				break;
			case 2:
				result = new SimpleDateFormat("yyyy/MM/dd").format(date);
				break;
			case 3:
				result = new SimpleDateFormat("yyyyMMdd HHmmss").format(date);
				break;
			case 4:
				result = new SimpleDateFormat("HH:mm:ss").format(date);
				break;
			case 5:
				result = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒").format(date);
				break;
			case 6:
				result = new SimpleDateFormat("MM/dd/yyyy").format(date);
				break;
			default:
				result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
				break;
			}
		}
		return result;
	}
}
