package com.enterlib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Text;

/** Utility class to handle common string operations */
public final class StringUtils {

	/**
	 * Check if the string is null, whitespace or empty
	 * 
	 * @param s
	 *            the String
	 * @return returns true if the string is invalid
	 */
	public static boolean invalidString(String s) {
		return !validString(s);
	}

	/**
	 * Check if the string is not null, whitespace or empty
	 * 
	 * @param s
	 *            the String
	 * @return returns true if the string is valid
	 */
	public static boolean validString(String s) {
		if (s == null || TextUtils.isEmpty(s)) {
			return false;
		}
		if (s.trim().equalsIgnoreCase("")) {
			return false;
		}
		return true;
	}

	/**
	 * @param value
	 * @return
	 */
	public static boolean isNullOrWhitespace(String value) {
		if (value == null) {
			return true;
		}

		int length = value.length();
		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static String readAllText(InputStream is) throws IOException {
		BufferedInputStream reader = new BufferedInputStream(is);
		StringBuilder sb = new StringBuilder();
		try {
			int c;
			while (( c =reader.read()) != -1){
				sb.append((char)c);
			}
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				Log.e("StringUtils", e.getMessage(), e);
				throw  e;
			}
		}
		return sb.toString();
	}

	public static boolean startsWordWith(String prefixString, String value) {
		if(prefixString == null || prefixString.length() ==0)
			return true;
		
		if (value == null || value.length() == 0) {
			return false;
		}
		
		
		prefixString = prefixString.toLowerCase();
		value = value.toLowerCase();

		// First match against the whole, non-splitted value
		if (value.startsWith(prefixString)) {
			return true;
		}

		final String[] words = value.split(" ");
		final int wordCount = words.length;

		// Start at index 0, in case valueText starts with space(s)
		for (int k = 0; k < wordCount; k++) {
			if (words[k].startsWith(prefixString)) {
				return true;
			}
		}

		return false;
	}
	
	 
	@SuppressLint("DefaultLocale")
	public static String parseTwoDigitsFormat(int n){
		return  String.format("%02d", n);
	}
	
	@SuppressLint("DefaultLocale")
	public static String parse(double n){
		return  String.format("%02.2f", n);
	}
	
	public static String parseCurrency(double n){
		return  String.format("%,.2f", n);
	}
	
	public static void append(StringBuilder sb, char c, int times){
		for (int i = 0; i < times; i++) {
			sb.append(c);
		}
	}

	public static String aggregate(String[]array, String joint, int start, int end){
		StringBuilder sb = new StringBuilder();
		for (int i = start; i< end; i++){
			if(i > start){
				sb.append(joint);
			}
			sb.append(array[i]);
		}
		return sb.toString();
	}

	public static String aggregate(Object[]array, String joint, int start, int end){
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++){
			if(i > start){
				sb.append(joint);
			}
			sb.append(array[i].toString());
		}
		return sb.toString();
	}

	public static String aggregate(List<?> list, String joint, int start, int end){
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++){
			if(i > start){
				sb.append(joint);
			}
			sb.append(list.get(i).toString());
		}
		return sb.toString();
	}
}
