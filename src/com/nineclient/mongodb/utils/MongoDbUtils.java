package com.nineclient.mongodb.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.nineclient.any800.common.utils.PropertiesUtil;

public class MongoDbUtils {

	public static String MONGO_IP = PropertiesUtil.getProperty("mongo.host");

//	public static int MONGO_PORT = PropertiesUtil.getIntProperty("mongo.port", 27017);
	public static int MONGO_PORT = PropertiesUtil.getIntProperty("mongo.port", 29017);

	public static String MONGO_DB = PropertiesUtil.getProperty("mongo.db");

	public static String MONGO_COLLECTION = PropertiesUtil.getProperty("mongo.collection");

	public static String MONGO_IM_COLLECTION = PropertiesUtil.getProperty("mongo.im.collection");
	
	public static String MONGO_IM_GROUP_COLLECTION = PropertiesUtil.getProperty("mongo.imgroup.collection");
	
	public static String MONGO_OP_BEHAVIOR_COLLECTION = PropertiesUtil.getProperty("mongo.im.op.behavior.collection");

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
