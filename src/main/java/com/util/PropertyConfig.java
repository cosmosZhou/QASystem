package com.util;

import java.util.List;
import java.util.Map;

import org.ini4j.ConfigParser;

public class PropertyConfig {
	public static ConfigParser config;
	static {
		try {
			config = new ConfigParser();
			config.read(Utility.workingDirectory + "config.ini");

			List<Map.Entry<String, String>> items = config.items("mysql.connector");
			for (Map.Entry<String, String> item : items) {
				System.out.println(item.getKey() + " = " + item.getValue());
			} // end for

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		System.out.println(config);
	}

}
