package com.enterlib.fields;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.widget.ListAdapter;

import com.enterlib.data.IPagedCursor;
import com.enterlib.data.IEntityCursor;
import com.enterlib.data.IRepository;
import com.enterlib.databinding.AdapterEntityCursorForm;
import com.enterlib.databinding.AdapterForm;
import com.enterlib.mvvm.BaseViewModel;
import com.enterlib.parsing.ast.OrderByExpression;
import com.enterlib.threading.IAsyncLoadOperation;
import com.enterlib.threading.AsyncManager;
import com.enterlib.threading.IWorkPost;

public class AdapterProvider {

	private class LoadOperation implements IAsyncLoadOperation, IWorkPost{
		ItemsField listField;
		IPagedCursor<Object> pagedCursor;

		public LoadOperation(ItemsField listField, IPagedCursor<Object> pagedCursor) {
			this.listField = listField;
			this.pagedCursor = pagedCursor;
		}

		@Override
		public boolean loadAsync() throws Exception {
			return runWork();
		}

		@Override
		public void onDataLoaded() {
			onWorkFinish(null);
		}

		@Override
		public boolean runWork() throws Exception {
			pagedCursor.loadPage(0);
			return true;
		}

		@Override
		public void onWorkFinish(Exception workException) {
			if(workException!=null){
				Log.e("AdapterProvider", workException.getLocalizedMessage(), workException);
				listField.onSetErrorMessage(workException.getLocalizedMessage());
				return;
			}

			listField.setAdapter(new AdapterEntityCursorForm(context,
					adapterLayoutRes,  pagedCursor,
					listField));
		}
	}

	private class RepositoryQueryOperation implements IAsyncLoadOperation, IWorkPost{
		IRepository<?>repository;
		IEntityCursor<Object>cursor;
		ItemsField listField;

		public RepositoryQueryOperation(IRepository<?> repository, ItemsField listField) {
			this.repository = repository;
			this.listField = listField;
		}

		@Override
		public boolean loadAsync() throws Exception {
			return runWork();
		}

		@Override
		public void onDataLoaded() {
			onWorkFinish(null);
		}

		@Override
		public boolean runWork() throws Exception {
			cursor = (IEntityCursor<Object>) repository.query().toCursor();
			return true;
		}

		@Override
		public void onWorkFinish(Exception workException) {
			if(workException!=null){
				Log.e("AdapterProvider", workException.getLocalizedMessage(), workException);
				listField.onSetErrorMessage(workException.getLocalizedMessage());
				return;
			}

			listField.setAdapter(new AdapterEntityCursorForm(context,
					adapterLayoutRes,  cursor,
					listField));
		}
	}

	public int getAdapterLayoutRes() {
		return adapterLayoutRes;
	}

	public void setAdapterLayoutRes(int adapterLayoutRes) {
		this.adapterLayoutRes = adapterLayoutRes;
	}

	public int getAdapterDropDownRes() {
		return adapterDropDownRes;
	}

	public void setAdapterDropDownRes(int adapterDropDownRes) {
		this.adapterDropDownRes = adapterDropDownRes;
	}

	int adapterLayoutRes;
	int adapterDropDownRes;
	Context context;

	public AdapterProvider(Context context, int adapterLayoutRes) {
		this.adapterLayoutRes = adapterLayoutRes;
		this.adapterDropDownRes = adapterLayoutRes;
		this.context = context;
	}

	public AdapterProvider(Context context, int adapterLayoutRes, int adapterDropDownRes){
		this(context, adapterLayoutRes);
		this.adapterDropDownRes = adapterDropDownRes;
	}

	public AdapterProvider() {
	}



	@SuppressWarnings("unchecked")
	public void onItemsSet(ItemsField listField) {
		Object items = listField.getItems();

		if (items == null) {
			listField.setAdapter(null);
			return;
		}

		if (items instanceof IEntityCursor<?>) {
			setEntityCursor(listField, (IEntityCursor<Object>)items);
			return;
		}else if(items instanceof IRepository<?>){
			IRepository<?> repository = (IRepository<?>) items;
			Object viewModel = listField.getViewModel();
			if(viewModel instanceof BaseViewModel){
				BaseViewModel baseViewModel = (BaseViewModel) viewModel;
				baseViewModel.doLoadOperationAsync(new RepositoryQueryOperation(repository, listField));
			}else{
				AsyncManager.postAsync(new RepositoryQueryOperation(repository, listField));
			}
			return;
		}

		ArrayList<Object> collection = null;
		if (items.getClass().isArray()) {
			Object[] objects = (Object[]) items;
			collection = new ArrayList<Object>(objects.length);
			for (int i = 0; i < objects.length; i++) {
				collection.add(objects[i]);
			}

		} else if (items instanceof ArrayList<?>) {
			collection = (ArrayList<Object>) items;
		}

		if (collection == null) {
			throw new RuntimeException("Items is not an array or collection");
		}

		if (adapterLayoutRes == 0) {
			listField.setAdapter(getAdapter(collection));
			return;
		}
		listField.setAdapter(new AdapterForm(context, adapterLayoutRes, adapterDropDownRes, collection, listField));

	}

	private void setEntityCursor(ItemsField listField, IEntityCursor<Object> items) {
		if (adapterLayoutRes == 0) {
            throw new RuntimeException("Must specified an ItemTemplate");
        }

		if(items instanceof IPagedCursor<?>){
            IPagedCursor<Object> pagedCursor = (IPagedCursor<Object>) items;
            if(!pagedCursor.isLoaded()){
                Object viewModel = listField.getViewModel();
                if(viewModel instanceof BaseViewModel){
                    BaseViewModel baseViewModel = (BaseViewModel) viewModel;
                    baseViewModel.doLoadOperationAsync(new LoadOperation(listField, pagedCursor));
                }else{
                    AsyncManager.postAsync(new LoadOperation(listField, pagedCursor));
                }
            }else{
                listField.setAdapter(new AdapterEntityCursorForm(context,
                        adapterLayoutRes, items,
                        listField));
            }
        }else {

            listField.setAdapter(new AdapterEntityCursorForm(context,
                    adapterLayoutRes, items,
                    listField));
        }
	}

	public ListAdapter getAdapter(ArrayList<Object> collection) {
		return null;
	}
}