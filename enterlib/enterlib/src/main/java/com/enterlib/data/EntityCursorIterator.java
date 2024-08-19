package com.enterlib.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EntityCursorIterator<T> implements Iterator<T>{
	IEntityCursor<T>cursor;
	int position = -1;
	int count;
	public EntityCursorIterator(IEntityCursor<T> cursor) {
		super();
		this.cursor = cursor;
		this.count = cursor.getCount();
	}

	@Override
	public boolean hasNext() {			
		return (position + 1) < count;
	}

	@Override
	public T next() {
		position++;
		if(position >= count)
			throw new NoSuchElementException();
		
		return cursor.getItem(position);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();		
	}
	
}