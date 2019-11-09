package com.robot.syntax;

import java.util.HashMap;
import java.util.Map;

import com.robot.syntax.Constituent.Cache;

public class ConstituentGradientSingleton extends ConstituentGradient {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConstituentGradientSingleton(Cache map) {
		this.map = map;
	}

	public ConstituentGradientSingleton() {
		this.map = new Cache();
	}

	Cache map;
	public boolean modified = false;
}
