package com.enterlib.data.sqlite;

/**
 * Created by hp on 10/29/2016.
 */
public interface ISQLQueryProvider<T> {
    SQLQuery<T> getQuery();
}
