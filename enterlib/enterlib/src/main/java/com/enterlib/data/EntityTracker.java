package com.enterlib.data;

import java.lang.reflect.Modifier;

import com.enterlib.annotations.ColumnMap;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.widgets.IModelStateObserver;

import android.util.SparseArray;

public class EntityTracker<T> implements IModelStateObserver {

	public static class EntityState<T> {

		public int Id;

		public int State;

		public T Entity;

		public EntityState(int id, int state, T entity) {
			super();
			Id = id;
			State = state;
			Entity = entity;
		}						
	}
	
	public static final int NONE = 0;
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 2;
	public static final int UPDATED = 3;
	
	SparseArray<EntityState<T>> mStateArray;
	private String key;		
			
	public EntityTracker(String key) {		
		mStateArray = new SparseArray<EntityState<T>>();
		this.key = key;
	}	
	
	public EntityTracker(Class<T>cls){
		java.lang.reflect.Field[] typefields = cls.getFields();
		
		for (int i = 0; i < typefields.length; i++) {
			java.lang.reflect.Field field = typefields[i];
			int modifier = field.getModifiers();
			if (Modifier.isStatic(modifier)) {
				continue;
			}
			
			ColumnMap dbMap = field.getAnnotation(ColumnMap.class);
			if(dbMap!=null && dbMap.key()){				
				key = field.getName();
				break;				
			}else{
				String fieldName = field.getName();
				if(fieldName.equalsIgnoreCase("Id")){
					key = field.getName();
					break;
				}
				else if(fieldName.equals(cls.getSimpleName()+"Id")){
						key = field.getName();
						break;
				}
			}
		}
		
		mStateArray = new SparseArray<EntityState<T>>();
	}
	
	public EntityTracker(T[] models, String key) {
		mStateArray = new SparseArray<EntityState<T>>(models.length);
		this.key = key;
		
		for (int i = 0; i < models.length; i++) {
			int id = (Integer) ReflectionResolver.getValue(key, models[i]);
			mStateArray.put(id, new EntityState<T>(id, UNCHANGED, models[i]));
		}
	}

	public EntityTracker(Iterable<T> models, String key) {
		mStateArray = new SparseArray<EntityState<T>>();
		for (T item : models) {
			int id = (Integer) ReflectionResolver.getValue(key, item);
			mStateArray.put(id, new EntityState<T>(id, UNCHANGED, item));
		}		
	}
	
	public EntityTracker(Iterable<T> models, Class<T> cls) {
		this(cls);
		
		for (T item : models) {
			int id = (Integer) ReflectionResolver.getValue(key, item);
			mStateArray.put(id, new EntityState<T>(id, UNCHANGED, item));
		}		
	}

	public void reset(T[] models) {
		mStateArray.clear();

		for (int i = 0; i < models.length; i++) {
			int id = (Integer) ReflectionResolver.getValue(key, models[i]);
			mStateArray.put(id, new EntityState<T>(id, UNCHANGED, models[i]));
		}
	}

	public void reset(Iterable<T> models) {
		mStateArray.clear();

		for (T item : models) {
			int id = (Integer) ReflectionResolver.getValue(key, item);
			mStateArray.put(id, new EntityState<T>(id, UNCHANGED, item));
		}
	}

	public int setState(int state, T model) {
		int id = (Integer) ReflectionResolver.getValue(key, model);
		return setState(id, state, model);
	}

	public int setState(int modelId, int state, T model) {
		EntityState<T> mstate = mStateArray.get(modelId);
		if (mstate == null) {
			mstate = new EntityState<T>(modelId, ADDED, model);
			mStateArray.put(modelId, mstate);
			return ADDED;
		}

		if (mstate.State == ADDED) {
			if (state == REMOVED) {
				mstate.State = NONE;
				mStateArray.remove(modelId);
			}
		} else if (mstate.State == REMOVED) {
			if (state == ADDED) {
				mstate.State = UNCHANGED;
			}
		} else if (mstate.State == UNCHANGED) {
			if (state == REMOVED) {
				mstate.State = REMOVED;
			}
		} else if (mstate.State == NONE) {
			mstate.State = state;
		}
		return mstate.State;
	}

	public int getState(int modelId) {
		EntityState<T> mstate = mStateArray.get(modelId);
		if (mstate == null) {
			return NONE;
		}
		return mstate.State;
	}

	public int getCount() {
		return mStateArray.size();
	}

	public EntityState<T> getEntityState(int index) {
		return mStateArray.valueAt(index);
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean onAdded(Object model) {
		int id = (Integer) ReflectionResolver.getValue(key, model);
		if (getState(id) == ADDED) {
			return false;
		}

		return setState(ADDED, (T)model) != NONE;
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean onRemoved(Object model) {
		setState(REMOVED, (T)model);
		return true;

	}
}
