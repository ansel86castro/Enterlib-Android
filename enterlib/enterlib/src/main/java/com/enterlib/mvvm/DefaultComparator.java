package com.enterlib.mvvm;

import java.util.Comparator;

public class DefaultComparator<T> implements Comparator<T> {
	public int Order = 1;// 1=Ascending -1=Descending

	@Override
	public int compare(T lhs, T rhs) {
		if( lhs == null)
			throw new IllegalArgumentException("lhs");
		if(rhs == null)
			throw new IllegalArgumentException("rhs");

		String v1 = lhs.toString();
		String v2 = rhs.toString();

		return v1.compareToIgnoreCase(v2) * Order;
	}
}