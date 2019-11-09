package com.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.util.Utility;

public class PropertyConfig {
	private static String default_config = "config.ini";
	private static Properties mConfig;
	static {
		mConfig = new Properties();
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(Utility.workingDirectory + default_config));

			mConfig.load(is);
			is.close();
		} catch (Exception e) {
			// InputStream is;
			// try {
			// is = new BufferedInputStream(new FileInputStream("./"
			// + default_config));
			// mConfig.load(is);
			// is.close();
			//
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }

			e.printStackTrace();
		}
	}

	// public static String getProperty(String key) {
	// String value = mConfig.getProperty(key);
	// //System.out.println(key+"--"+value);
	// /*解密*/
	// Blowfish propertyEncryptor = new Blowfish("9client");
	// String m = propertyEncryptor.decryptString(value);
	// if(null != m){ //如果解密为null表示配置文件没加密
	// value = m;
	// //System.out.println(key+"--"+value);
	// }
	// return value;
	// }

	public static String getProperty(String key, String defaultValue) {
		String value = mConfig.getProperty(key);
		if (value == null)
			return defaultValue;

		return value;
	}

	public static String getProperty(String key) {
		return mConfig.getProperty(key);
	}

	public static boolean getBooleanProperty(String name, boolean defaultValue) {
		String value = PropertyConfig.getProperty(name, null);

		if (value == null)
			return defaultValue;

		return (new Boolean(value)).booleanValue();
	}

	public static int getIntProperty(String name) {
		return getIntProperty(name, 0);
	}

	public static int getIntProperty(String name, int defaultValue) {
		String value = PropertyConfig.getProperty(name, null);

		if (value == null)
			return defaultValue;

		return (new Integer(value)).intValue();
	}

}
