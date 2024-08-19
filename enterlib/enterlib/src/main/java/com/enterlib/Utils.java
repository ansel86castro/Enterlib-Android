package com.enterlib;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {
	@SuppressWarnings("unchecked")
	public static List<Object> getList(Object items) {
		ArrayList<Object> list = null;

		if (items.getClass().isArray()) {
			Object[] objects = (Object[]) items;
			list = new ArrayList<Object>(objects.length);
			for (int i = 0; i < objects.length; i++) {
				list.add(objects[i]);
			}
		} else if (items instanceof ArrayList<?>) {
			list = (ArrayList<Object>) items;
		} else if (items instanceof Collection<?>) {
			list = new ArrayList<Object>((Collection<?>) items);
		} else {
			throw new UnsupportedOperationException("Collection not supported");
		}
		return list;
	}
	
	public static <T> T[]  getArray(Class<T>cls, List<T>list){		
		@SuppressWarnings("unchecked")
		T[]array = (T[]) Array.newInstance(cls, list.size());	
		
		for (int i = 0; i < array.length; i++) {
			array[i]=list.get(i);
		}
		
		return array;
	}


	public static <T> T[] asOptionalArray(Class<T>cls, T[] items, T optional) {
		T[] newItems;
		if (items == null) {
			newItems = (T[]) Array.newInstance(cls, 1);
			newItems[0]= optional;
			return newItems;
		}

		newItems = (T[]) Array.newInstance(cls, items.length + 1);
		newItems[0] = optional;
		for (int i = 0; i < items.length; i++) {
			newItems[i + 1] = items[i];
		}
		return newItems;
	}

	public static <T> T[] asOptionalArray(Class<T>cls, T[] items) {
		T optional;
		try {
			optional = cls.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		T[] newItems;
		if (items == null) {
			newItems = (T[]) Array.newInstance(cls, 1);
			newItems[0]= optional;
			return newItems;
		}

		newItems = (T[]) Array.newInstance(cls, items.length + 1);
		newItems[0] = optional;
		for (int i = 0; i < items.length; i++) {
			newItems[i + 1] = items[i];
		}
		return newItems;
	}

	public static <T> T[] asOptionalArray(Class<T>cls, List<T> items, T optional) {
		T[] newItems;
		if (items == null) {
			newItems = (T[]) Array.newInstance(cls, 1);
			newItems[0]= optional;
			return newItems;
		}

		newItems = (T[]) Array.newInstance(cls, items.size() + 1);
		newItems[0] = optional;
		for (int i = 0; i < items.size(); i++) {
			newItems[i + 1] = items.get(i);
		}
		return newItems;
	}

	public static <T> T[] asOptionalArray(Class<T>cls, List<T> items) {
		T optional;
		try {
			optional = cls.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		T[] newItems;
		if (items == null) {
			newItems = (T[]) Array.newInstance(cls, 1);
			newItems[0]= optional;
			return newItems;
		}

		newItems = (T[]) Array.newInstance(cls, items.size() + 1);
		newItems[0] = optional;
		for (int i = 0; i < items.size(); i++) {
			newItems[i + 1] = items.get(i);
		}
		return newItems;
	}

	public static <T> ArrayList<T> asOptionalList(Class<T>cls, T[] items, T optional) {
		ArrayList<T> newItems = new ArrayList<T>(items.length + 1);
		newItems.add(optional);
		for (int i = 0; i < items.length; i++) {
			newItems.add(items[i]);
		}
		return newItems;
	}

	public static <T> ArrayList<T> asOptionalList(Class<T>cls,List<T> items, T optional) {
		ArrayList<T> newItems = new ArrayList<T>(items.size() + 1);

		newItems.add(optional);
		for (int i = 0; i < items.size(); i++) {
			newItems.add(items.get(i));
		}
		return newItems;
	}
	
}
