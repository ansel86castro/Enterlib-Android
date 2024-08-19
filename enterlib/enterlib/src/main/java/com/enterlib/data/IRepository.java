package com.enterlib.data;

import androidx.annotation.Nullable;

import com.enterlib.IClosable;

public interface IRepository<T> extends IClosable, IFactory<T> {

	@Nullable T get(int id);

	@Nullable T get(int[] ids);

	@Nullable T get(int id , @Nullable String[] includes);

	@Nullable T get(int[] ids,  @Nullable String[] includes);

	IQuerable<T> query(@Nullable String where, @Nullable String orderBy, int skip, int take, @Nullable String[] includes);

	IQuerable<T> query(String where, String orderBy, int skip, int take);

	IQuerable<T> query(String where, String orderBy);

	IQuerable<T> query(String where);

	IQuerable<T> query();

	int count(@Nullable String expression);
	
	boolean create(T item);
	
	boolean update(T item);
	
	boolean delete(T item);

	int delete(@Nullable String expression);
}
