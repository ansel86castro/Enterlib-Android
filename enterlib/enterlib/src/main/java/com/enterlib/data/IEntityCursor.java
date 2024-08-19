package com.enterlib.data;

import com.enterlib.IClosable;

public interface IEntityCursor<T> extends IClosable, Iterable<T> {

	int getCount();

	T getItem(int position);
	
}
