package com.enterlib.data;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import com.enterlib.data.sqlite.EntityMap;

public class WrapperRepository<T> implements IRepository<T> {

	protected IRepository<T> repository;

	public WrapperRepository(IRepository<T> repository){
		this.repository =repository;
		
	}

	@Override
	public T get(int id) {
		return get(id, null);
	}

	@Override
	public T get(int[] ids) {
		return get(ids, null);
	}

	@Nullable
	@Override
	public T get(int id, @Nullable String[] includes) {
		return repository.get(id, includes);
	}

	@Nullable
	@Override
	public T get(int[] ids, @Nullable String[] includes) {
		return repository.get(ids, includes);
	}

	@Override
	public IQuerable<T> query(@Nullable String where, @Nullable String orderBy, int skip, int take, @Nullable String[] includes) {
		return repository.query(where, orderBy, skip, take, includes);
	}

	@Override
	public IQuerable<T> query(String where, String orderBy, int skip, int take) {
		return query(where, orderBy, skip, take, null);
	}

	@Override
	public IQuerable<T> query(String where, String orderBy) {
		return query(where, orderBy, -1, -1, null);
	}

	@Override
	public IQuerable<T> query(String where) {
		return query(where, null, -1, -1, null);
	}

	@Override
	public IQuerable<T> query() {
		return query(null, null, -1, -1, null);
	}

	@Override
	public int count(@Nullable String expression) {
		return repository.count(expression);
	}

	@Override
	public boolean create(T item) {
		return repository.create(item);
	}

	@Override
	public boolean update(T item) {
		return repository.update(item);
	}

	@Override
	public boolean delete(T item) {
		return repository.delete(item);
	}

	@Override
	public int delete(@Nullable String expression) {
		return repository.delete(expression);
	}

	@Override
	public void close() {
		repository.close();
	}

	@Override
	public T getInstance() {
		return repository.getInstance();
	}
}
