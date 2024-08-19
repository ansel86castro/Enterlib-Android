package com.enterlib.data;

import java.util.ArrayList;

/**
 * Created by ansel on 12/10/2016.
 */
public interface IQuerable<T> extends  Iterable<T> {

    IQuerable<T> where(String expression);

    IQuerable<T> where(String expression, Object...params);

    IQuerable<T> orderBy(String expression);

    IQuerable<T> include(String expression);

    IQuerable<T> include(String[] expression);

    IQuerable<T> skip(int value);

    IQuerable<T> take(int value);

    ArrayList<T> toList();

    IEntityCursor<T> toCursor();

    T first();

    long count();

}
