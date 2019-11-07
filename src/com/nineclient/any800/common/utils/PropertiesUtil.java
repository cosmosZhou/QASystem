/**
 * <pre>
 * 上海久科信息技术有限公司
 * Copyright (C): 2012
 * 
 * 文件名称：
 * PropertyConfig.java
 * 
 * 文件描述:
 * 配置属性操作类。
 * 
 * Notes:
 * 
 * 修改历史(作者/日期/改动描述):
 * 王彬/2012.04.15/初始化版本。
 * john/2016.11.11/配置文件移到项目外部
 * </pre>
 */
package com.nineclient.any800.common.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.util.Utility;

public class PropertiesUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
	
	/**
	 * 默认配置文件
	 */
	private static String default_config = "robot.properties";
	
	private static Properties mConfig;
	/**
	 * 静态块
	 */
	static {
		mConfig = new Properties();
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(getPath() + default_config));
			mConfig.load(is);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	/**
	 * 
	* @Title: getProperty
	* @Description:读取配置
	* @param key
	* @return    
	* String    
	* @throws
	 */
	public static String getProperty(String key) {
		return mConfig.getProperty(key);
	}

	/**
	*
	* @Title: getProperty 
	* @Description: 读取配置，如果为空，则返回默认值 
	* @param key
	* @param defaultValue
	* @return
	* @return String    返回类型 
	* @throws
	 */
	public static String getProperty(String key, String defaultValue) {
		String value = mConfig.getProperty(key);
		if (value == null)
			return defaultValue;

		return value;
	}

	/**
	 * 
	* @Title: getBooleanProperty
	* @Description:读取配置，如果为空，返回默认值
	* @param name
	* @param defaultValue
	* @return    
	* boolean    
	* @throws
	 */
	public static boolean getBooleanProperty(String name, boolean defaultValue) {
		String value = PropertiesUtil.getProperty(name);

		if (value == null)
			return defaultValue;

		return (new Boolean(value)).booleanValue();
	}

	/**
	 * 
	* @Title: getIntProperty
	* @Description:读取配置
	* @param name
	* @return    
	* int    
	* @throws
	 */
	public static int getIntProperty(String name) {
		return getIntProperty(name, 0);
	}

	/**
	 * 
	* @Title: getIntProperty
	* @Description:读取配置，如果为空，返回默认值
	* @param name
	* @param defaultValue
	* @return    
	* int    
	* @throws
	 */
	public static int getIntProperty(String name, int defaultValue) {
		String value = PropertiesUtil.getProperty(name);

		if (value == null)
			return defaultValue;

		return (new Integer(value)).intValue();
	}

	public static String getPath() {		
		if("\\".equals(System.getProperties().getProperty("file.separator"))){	//windows环境下路径   
			return Utility.workingDirectory;
		}else{	//linux环境下路径
			return "/opt/nfsdata";
		}
	}

//	public static String getPath() {
//		if("\\".equals(System.getProperties().getProperty("file.separator"))){	//windows环境下路径   
//			return "\\opt\\ucc\\conf\\";
//		}else{	//linux环境下路径
//			return "/opt/ucc/conf/";
//		}
//	}
}
