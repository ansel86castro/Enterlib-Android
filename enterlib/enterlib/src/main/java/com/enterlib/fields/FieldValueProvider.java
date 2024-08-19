/**
 * 
 */
package com.enterlib.fields;

import android.content.Context;
import android.util.Log;

import com.enterlib.IServiceProvider;
import com.enterlib.ioc.DependencyContext;
import com.enterlib.threading.LoaderHandler;
import com.enterlib.threading.LoaderHandler.LoadTask;

/**
 * @author Ansel Castro
 *
 */
public abstract class FieldValueProvider<T> {
		
	private static final String LOG_TAG = FieldValueProvider.class.getSimpleName();
	
	private T value;
	LoaderHandler asycLoader;
	
	public boolean isValueAvailable(){
		return value!=null;
	}
	
	public void setLoaderHandler(LoaderHandler loader){
		this.asycLoader = loader;
	}
	
	protected void setValue(T value){
		this.value = value;
	}
	
	public T getValue(){
		return value;
	}
	
	protected abstract T loadValue(Field field) throws Exception;
	
	public void getValueAsync(final Field field){
		if(asycLoader == null){
			asycLoader = getAsyncLoader(field);
		}

		onLoadStart(field);
		
		asycLoader.postTask(new LoadTask() {
			
			@Override
			public Object runAsync(Object args) throws Exception {
				value = loadValue(field);
				return value;
			}
			
			@Override
			public void onComplete(Object result, Exception e) {
				if(e != null){
					field.setErrorMessage(e.getLocalizedMessage());
					Log.e(LOG_TAG, e.getLocalizedMessage(), e);
				}
                onLoadCompleted(field, result, e);
				
			}
		});
	}

	protected void onLoadStart(Field field){

	}

	protected void onLoadCompleted(Field field, Object value, Exception e){
        if(e != null) {
            return;
        }

        field.setViewValue(value);
	}

	protected LoaderHandler getAsyncLoader(Field field) {
		Context context = field.getContext();
		LoaderHandler asycLoader = null;

		if(context instanceof IServiceProvider){
			 asycLoader = ((IServiceProvider) context).getService(LoaderHandler.class);
		}else if(DependencyContext.getContext()!=null){
			asycLoader = DependencyContext.getContext().getService(LoaderHandler.class);
		}
		if(asycLoader == null){
			asycLoader = new LoaderHandler();			
		}
		return asycLoader;
	}
	
}
