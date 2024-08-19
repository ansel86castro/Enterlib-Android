package com.enterlib.data.web;

import android.content.Context;

import com.enterlib.IClosable;
import com.enterlib.data.IEntityContext;
import com.enterlib.data.IQuerable;
import com.enterlib.data.IRepository;
import com.enterlib.data.ManyToOneRepository;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class WebApiContext implements IClosable, IEntityContext {

	private final Context context;
	private HashMap<Class<?>, WeakReference<WebApiClient<?>>> repositories= new HashMap<Class<?>, WeakReference<WebApiClient<?>>>();
	private boolean isClosed;
	private String baseUrl;
	private WebApiClient.OnUrlConnectionListener urlConnectionListener;

	public WebApiContext(Context context, String baseUrl) {
		this.baseUrl = baseUrl;
		this.context = context;
	}

	public void setUrlConnectionListener(WebApiClient.OnUrlConnectionListener listener){
		this.urlConnectionListener = listener;
	}

	public <T> WebApiClient<T> getClient(Class<T> cls){
		return getClient(cls, cls.getSimpleName());
	}

	public <T> WebApiClient<T> getClient(String className, String controller){
		Class<T> cls;
		try {
			cls = (Class<T>) context.getClassLoader().loadClass(className);
			return getClient(cls, controller );
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public <T> WebApiClient<T> getClient(String className){
		Class<T> cls;
		try {
			cls = (Class<T>) Class.forName(className);
			return getClient(cls);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
		

	public final <T> WebApiClient<T> getClient(Class<T> cls ,String controller) {
		WeakReference<WebApiClient<?>>ref = repositories.get(cls);
		WebApiClient<T> client = null;
		if(ref != null){
			client = (WebApiClient<T>)ref.get();
		}					
		if(client == null){
			client =createClient(cls, baseUrl, controller);
			client.setUrlConnectionListener(urlConnectionListener);

			repositories.put(cls, new WeakReference<WebApiClient<?>>(client));
		}
		
		return client;
	}

	protected <T> WebApiClient<T> createClient(Class<T> cls, String baseUrl ,String controller){
	 	return new WebApiClient<T>(this, cls, baseUrl, controller);
	}
	
	public boolean isClosed() {
		return isClosed;
	}

	@Override
	public void close() {
		if(!isClosed){			
			for (WeakReference<WebApiClient<?>> ref : repositories.values()) {
				WebApiClient<?> rep = ref.get();
				if(rep!=null)
					rep.close();
				ref.clear();
			}
			repositories.clear();
			
			onClosed();
			isClosed =true;
		}		
	}	
	
	protected void onClosed() {
		
	}



	@Override
	public <T> T get(Class<T> model, int id) {
		return getClient(model).get(id);
	}


	@Override
	public <T> T get(Class<T> model, int[] ids) {
		return getClient(model).get(ids);
	}


	@Override
	public <T> boolean create(Class<T> model, T item) {
		return getClient(model).create(item);
	}

	@Override
	public <T> boolean update(Class<T> model, T item) {
		return getClient(model).update(item);
	}


	@Override
	public <T> boolean delete(Class<T> model, T item) {
		return getClient(model).delete(item);
	}

	@Override
	public <T> int delete(Class<T> model, String expression) {
		return getClient(model).delete(expression);
	}

	@Override
    public <T> IRepository<T> getRepository(Class<T> model) {
        return getClient(model);
    }

    public <T> IRepository<T> getRepository(Class<T> model, String fkeyName, int fkeyValue, boolean distint){
		WebApiClient<T> client = getClient(model);
		return new ManyToOneRepository(client, client.getMapByFieldName(fkeyName), fkeyValue, client.getKeys(), distint);
	}

	@Override
	public <T> IRepository<T> getRepository(IRepository<T> repository, Class<T>model, String fkeyName, int fkeyValue, boolean distint) {
		WebApiClient<T> client = getClient(model);
		return new ManyToOneRepository(repository, client.getMapByFieldName(fkeyName), fkeyValue, client.getKeys(), distint);
	}

	public <TRel, TModel> IRepository<TModel> getRepository(Class<TRel> relation, Class<TModel> model, int linking_id, boolean distint){
		ManyToManyApiClient<TRel, TModel> repo = new ManyToManyApiClient<TRel, TModel>(this,baseUrl ,relation, model, linking_id, distint);
		repo.setUrlConnectionListener(urlConnectionListener);
		return repo;
	}

	@Override
	public <T> IQuerable<T> query(Class<T> model) {
		return getClient(model).query();
	}

	@Override
	public <T> T newInstance(Class<T> model) {
		WebApiClient<T> client = getClient(model);
		return client!=null ? client.getInstance():null;
	}
}
