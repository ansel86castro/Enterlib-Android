package com.enterlib.data;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.enterlib.IClosable;
import com.enterlib.exceptions.InvalidOperationException;

public abstract class BaseRepositoryManager implements IClosable {
	
	HashMap<Class<?>, WeakReference<IRepository<?>>> repositories= new HashMap<Class<?>, WeakReference<IRepository<?>>>();	
	
	@SuppressWarnings("unchecked")
	public <T> IRepository<T> getRepository(Class<T>cls){
		WeakReference<IRepository<?>> ref = repositories.get(cls);
		IRepository<T> rep = null;
		
		if(ref!=null)
			rep = (IRepository<T>) ref.get();
		
		if(rep == null){
			rep = createRepository(cls);
			if(rep == null)
				throw new InvalidOperationException("Unable to create repository for "+cls.getSimpleName());
			repositories.put(cls, new WeakReference<IRepository<?>>(rep));
		}
		
		return rep;
	}
	
	protected abstract <T> IRepository<T> createRepository(Class<T>cls);
	
	@Override
	public void close() {
		for (WeakReference<IRepository<?>> ref : repositories.values()) {
			IRepository<?> rep = ref.get();
			if(rep!=null)
				rep.close();
					
		}
		repositories.clear();
		
	}
	
	
}
