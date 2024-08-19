package com.enterlib.data;


import com.enterlib.IClosable;

public interface IEntityContext extends IClosable{
	
	<T> T get(Class<T>model, int id);
	
	<T> T get(Class<T>model, int[]ids);	
	
	<T> boolean create(Class<T>model, T item);
	
	<T> boolean update(Class<T>model, T item);
	
	<T> boolean delete(Class<T>model, T item);

	<T> int delete(Class<T>model, String expression);

	<T> IRepository<T> getRepository(Class<T>model);

	<T> IQuerable<T> query(Class<T>model);

	<T> IRepository<T> getRepository(Class<T> model,  String fkeyName, int fkeyValue, boolean distint);

	<T> IRepository<T> getRepository(IRepository<T>repository ,Class<T>model, String fkeyName, int fkeyValue, boolean distint);

	<TRel, TModel> IRepository<TModel> getRepository(Class<TRel> relation, Class<TModel> model, int linking_id, boolean distint);
	
	<T> T newInstance(Class<T> model);
}
