package com.util;

import java.io.File;

import org.ini4j.ConfigParser;

public class PropertyConfig {
	public static ConfigParser config;
	static {
		try {
			config = new ConfigParser();
			String config_path = new File(PropertyConfig.class.getResource("").getFile()).getParentFile().getParent()
					+ "/config.ini";

			System.out.println("config_path = " + config_path);
			config.read(config_path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println(config);
	}

}
