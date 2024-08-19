package com.enterlib.data.sqlite;

import com.enterlib.data.IEntityCursor;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by Ansel on 5/1/2018.
 */

public class SqlQueryIterator<T> implements Iterator<T> {
    IEntityCursor<T> cursor;
    int position = -1;
    int count;

    public SqlQueryIterator(IEntityCursor<T> cursor) {
        super();
        this.cursor = cursor;
        this.count = cursor.getCount();
    }

    @Override
    public boolean hasNext() {
        if( (position + 1) < count){
            return  true;
        }

        cursor.close();
        return false;
    }

    @Override
    public T next() {
        position++;

        if(position >= count)
            throw new NoSuchElementException();

        return cursor.getItem(position);
    }

}
