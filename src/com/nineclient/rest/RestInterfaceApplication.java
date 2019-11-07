package com.nineclient.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class RestInterfaceApplication extends Application {
	HashSet<Object> singletons = new HashSet<Object>();

	public RestInterfaceApplication() {
		singletons.add(new Knowledge());
	}

	@Override
	public Set<Class<?>> getClasses() {
		HashSet<Class<?>> set = new HashSet<Class<?>>();
		return set;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
