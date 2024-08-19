package com.enterlib.data;

import androidx.annotation.Nullable;


public class ManyToOneRepository<T> extends WrapperRepository<T>{

	private final PropertyMap[] keys;
	private int id;
	private PropertyMap key;
	private String filterValue;	

	public ManyToOneRepository(IRepository<T> repository, PropertyMap key, int id, PropertyMap[]keys, boolean distint) {
		super(repository);
		
		this.id = id;
		this.key= key;
		this.keys = keys;
		
		filterValue =String.format("%s %s %d",key.getName(), distint?"!=":"=", id);
	}
	
	@Override
	public boolean create(T item){
		if(key.getType() == Integer.class)
			key.set(item, id);
		else
			key.setInt(item, id);

		boolean create = false;
		for (int i = 0; i < keys.length; i++) {
			PropertyMap map = keys[i];
			if(map.getInt(item) == 0){
				create = true;
				break;
			}
		}

		if(create){
			return super.create(item);
		}else{
			return super.update(item);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public T getInstance() {
		T item = super.getInstance();
		if(key.getType() == Integer.class)
			key.set(item, id);
		else
			key.setInt(item, id);
		return item;
	}

	public IQuerable<T> query(@Nullable String where, @Nullable String orderBy, int skip, int take, @Nullable String[] includes){
		return super.query(QueryHelper.combine(where, filterValue), orderBy, skip, take, includes);
	}

	@Override
	public int count(@Nullable String expression) {
		return super.count(QueryHelper.combine(expression, filterValue));
	}

	@Override
	public int delete(@Nullable String expression) {
		return super.delete(QueryHelper.combine(expression, filterValue));
	}
}
