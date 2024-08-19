package com.enterlib.data.web;

public class NameValuePair {
	public final String name;
	public final String value;

	public NameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public NameValuePair(String name, int value) {
		this.name = name;
		this.value = String.valueOf(value);
	}
	
	public NameValuePair(String name, Object value) {
		this.name = name;
		this.value = value.toString();
	}

}
