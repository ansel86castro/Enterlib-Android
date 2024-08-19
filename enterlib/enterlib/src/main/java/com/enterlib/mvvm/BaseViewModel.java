package com.enterlib.mvvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.enterlib.IObserver;
import com.enterlib.databinding.IPropertyChangedListener;
import com.enterlib.threading.AsyncManager;
import com.enterlib.threading.IAsyncLoadOperation;
import com.enterlib.threading.IWorkPost;


public  class BaseViewModel extends Observable implements IViewModel {

	public static final int EDIT = 1;

	private IView view;
	private boolean _isLoaded;
	private List<String> notifications;
	private IViewModel parentViewModel;
	private boolean isDestroyed;

	public BaseViewModel(IView view) {
		this.view = view;
	}

	public BaseViewModel() {
	}

	public BaseViewModel(IView view, IViewModel parent){
		this(view);
		this.parentViewModel = parent;
	}

	@Override
	public IViewModel getParentViewModel() {
		return parentViewModel;
	}


	public boolean hasNotifications() {
		return notifications != null && notifications.size() > 0;
	}

	public List<String> getNotifications() {
		if (notifications == null) {
			notifications = new ArrayList<String>();
		}
		return notifications;
	}

	/**
	 * @return the View
	 */
	public IView getView() {
		return view;
	}

	@Override
	public void onDestroy() {
		if(!isDestroyed){
			isDestroyed = true;
		}
	}

	/**
	 * set the new view of the ViewModel
	 * 
	 * @param view
	 */
	public void setView(IView view) {
		this.view = view;
		onViewChanged();
	}

	/** Returns true if the data has been loaded */
	public boolean isLoaded() {
		return _isLoaded;
	}

	@Override
	public boolean isDestroyed() {
		return isDestroyed;
	}

	protected void setLoaded(boolean value) {
		_isLoaded = value;
	}

	/** Called after the view was changed */
	protected void onViewChanged() {
	}

	/**
	 * loads the view model's data in another {@link Thread} and notifies the
	 * {@link IView} when the data is available or an error occurred.
	 * */
	public void load() {
		onLoading();

		if (view != null && view.isValid()) {
			view.onAsyncOperationBegin();
		}

		AsyncManager.postAsync(new IWorkPost() {

			@Override
			public boolean runWork() throws Exception {
				return loadAsync();
			}

			@Override
			public void onWorkFinish(Exception workException) {
				_isLoaded = true;
				setChanged();
				if (view != null && view.isValid()) {
					view.onAsyncOperationEnd();
					if (workException != null) {
						view.onFailure(workException);
					} else {
						view.onLoadCompleted();
					}
				}
				onLoaded(workException);
			}
		});
	}

	public void loadInUIThread() {
		onLoading();

		Exception workException = null;

		try {
			loadAsync();

		} catch (Exception e) {
			workException = e;
		}

		_isLoaded = true;
		setChanged();

		if (view != null && view.isValid()) {
			if (workException != null) {
				view.onFailure(workException);
			} else {
				view.onLoadCompleted();
			}
		}

		onLoaded(workException);

		if (view == null && workException != null) {
			throw new RuntimeException(workException.getMessage(), workException);
		}
	}


	protected void doAsyncWork(String progressDialogMessage, final IWorkPost post) {
		if (view != null && view.isValid()) {
			view.onAsyncOperationBegin(progressDialogMessage);
		}

		AsyncManager.postAsync(new IWorkPost() {

			@Override
			public boolean runWork() throws Exception {
				return post.runWork();
			}

			@Override
			public void onWorkFinish(Exception workException) {
				setChanged();
				if (view != null && view.isValid()) {
					view.onAsyncOperationEnd();
					if (workException != null) {
						view.onFailure(workException);
					}
					post.onWorkFinish(workException);
				}
			}
		});
	}

	public void doLoadOperationAsync(final IAsyncLoadOperation operation) {
		if (view != null && view.isValid()) {
			view.onAsyncOperationBegin();
		}

		AsyncManager.postAsync(new IWorkPost() {

			@Override
			public boolean runWork() throws Exception {
				return operation.loadAsync();
			}

			@Override
			public void onWorkFinish(Exception workException) {
				setChanged();
				if (view != null && view.isValid()) {
					view.onAsyncOperationEnd();

					if (workException != null) {
						view.onFailure(workException);
					} else {
						operation.onDataLoaded();
					}
				}
			}
		});
	}

	/** Implementations must do data loading here */
	protected  boolean loadAsync() throws Exception{
		return true;
	}

	/** This is called before the loading begin */
	protected void onLoading() {
	}

	/**
	 * This is called after the loading finished or an error occurred
	 * 
	 * @param workException
	 *            The exception representing the fault during the invocation of
	 *            {@code loadingAsync}
	 * */
	protected void onLoaded(Exception workException) {
		notifyObservers(workException);
	}


	private ArrayList<IPropertyChangedListener> listeners;

	@Override
	public void addPropertyChangeListener(IPropertyChangedListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener can not be null");
		}

		if (listeners == null) {
			listeners = new ArrayList<IPropertyChangedListener>();
		}

		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);

	}

	@Override
	public boolean removePropertyChangeListener(
			IPropertyChangedListener listener) {
		if (listeners == null) {
			return false;
		}

		return listeners.remove(listener);
	}

	protected void onPropertyChange(String propName) {
		if (listeners == null) {
			return;
		}

		int length = listeners.size();
		IPropertyChangedListener[] listenersCopy = new IPropertyChangedListener[length];
		listeners.toArray(listenersCopy);

		for (int i = 0; i < length; i++) {
			listenersCopy[i].onPropertyChange(this, propName);
		}
	}

	public void delete(String progressDialogMessage, final OnDeleteListener onDeleteListener) {
		if (view != null && view.isValid()) {
			view.onAsyncOperationBegin(progressDialogMessage);
		}

		AsyncManager.postAsync(new IWorkPost() {
			@Override
			public boolean runWork() throws Exception {
				return deleteAsync();
			}

			@Override
			public void onWorkFinish(Exception workException) {
				setChanged();
				if (view != null && view.isValid()) {
					view.onAsyncOperationEnd();
					if (workException != null) {
						view.onFailure(workException);
					}
				}
				onDeleted(workException, onDeleteListener);
			}
		});
	}	

	protected void onDeleted(Exception exception, OnDeleteListener listener) {
		if (exception != null) {
			return;
		}		
		if(listener!= null)
			listener.onDeleted(null);
		else if(view instanceof OnDeleteListener){
			((OnDeleteListener) view).onDeleted(null);
		}
		
	}

	protected boolean deleteAsync() throws Exception {
		return false;
	}
	
	public void navigateTo(int requestCode){
		if(view!=null){
			view.navigateTo(requestCode, null, null);
		}
	}
	
	public void navigateTo(int requestCode, Object data){
		if(view!=null){
			view.navigateTo(requestCode, null, data);
		}
	}
	
	public void navigateTo(int requestCode, Bundle bundle, Object data){
		if(view!=null){
			view.navigateTo(requestCode, bundle, data);
		}
	}
}