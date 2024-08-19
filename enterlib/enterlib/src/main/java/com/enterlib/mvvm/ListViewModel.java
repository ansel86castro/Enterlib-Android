package com.enterlib.mvvm;

import java.util.List;

import com.enterlib.threading.AsyncManager;
import com.enterlib.threading.IWorkPost;

public abstract class ListViewModel extends BaseViewModel {

	
	public ListViewModel() {
		
	}

	public ListViewModel(IView view) {
		super(view);
		
	}

	public void attach(String progressDialogMessage, final Object item, final AttachedListener listener){
		IView view = getView();
		if (view != null && view.isValid()) {
			view.onAsyncOperationBegin(progressDialogMessage);
		}

		AsyncManager.postAsync(new IWorkPost() {
			@Override
			public boolean runWork() throws Exception {
				return attachAsync(item);
			}

			@Override
			public void onWorkFinish(Exception workException) {
				setChanged();
				IView view = getView();
				if (view != null && view.isValid()) {
					view.onAsyncOperationEnd();
					if (workException != null) {
						view.onFailure(workException);
					}
				}
				listener.onAttached(item, workException);
			}
		});
	}

	protected  boolean attachAsync(Object item)throws Exception{
		return false;
	}
	
	public void delete(String progressDialogMessage, final List<Object>items){
		IView view = getView();
		if (view != null && view.isValid()) {
			view.onAsyncOperationBegin(progressDialogMessage);
		}
		
		AsyncManager.postAsync(new IWorkPost() {
			@Override
			public boolean runWork() throws Exception {
				return deleteListAsync(items);
			}

			@Override
			public void onWorkFinish(Exception workException) {
				setChanged();
				IView view = getView();
				if (view != null && view.isValid()) {
					view.onAsyncOperationEnd();
					if (workException != null) {
						view.onFailure(workException);
					}
				}
				onDeleted(items);
			}
		});

	}

	protected void onDeleted(List<Object> items) {
		IView view = getView();
		if(view instanceof OnDeleteListener){
			((OnDeleteListener) view).onDeleted(items);
		}
	}

	protected boolean deleteListAsync(List<Object> items) throws Exception {
		return true;
	}

	public void attachList(String progressDialogMessage, final List<Object> items, final AttachedListener listener) {
		IView view = getView();
		if (view != null && view.isValid()) {
			view.onAsyncOperationBegin(progressDialogMessage);
		}

		AsyncManager.postAsync(new IWorkPost() {
			@Override
			public boolean runWork() throws Exception {
				for (Object object : items) {
					attachAsync(object);
				}
				return true;
			}

			@Override
			public void onWorkFinish(Exception workException) {
				setChanged();
				IView view = getView();
				if (view != null && view.isValid()) {
					view.onAsyncOperationEnd();
					if (workException != null) {
						view.onFailure(workException);
					}
				}
				listener.onAttached(items, workException);
			}
		});
		
	}

}
