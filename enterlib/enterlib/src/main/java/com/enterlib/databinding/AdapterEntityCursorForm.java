package com.enterlib.databinding;

import android.content.Context;
import android.view.View;
import android.widget.ListAdapter;

import com.enterlib.widgets.EntityCursorAdapter;
import com.enterlib.data.IEntityCursor;
import com.enterlib.fields.Form;
import com.enterlib.fields.ItemsField;
import com.enterlib.mvvm.BaseViewModel;

public class AdapterEntityCursorForm extends EntityCursorAdapter<Object>
		implements ListAdapter {

	private ItemsField mField;
	private BindingResources bindingRes;

	public AdapterEntityCursorForm(Context context, int resLayout, IEntityCursor<Object> cursor, ItemsField listField) {
		super(context, resLayout, cursor);

		this.mField = listField;
	}

	public AdapterEntityCursorForm(Context context, int resLayout, IEntityCursor<Object> cursor, BindingResources resources) {
		super(context, resLayout, cursor);

		this.bindingRes = resources;
	}
	
	public AdapterEntityCursorForm(Context context, int resLayout, IEntityCursor<Object> cursor){
		super(context, resLayout, cursor);
		
		this.bindingRes = new BindingResources();
		
	}

	@Override
	protected void updateView(View view, Object item, int position) {
		Object tag = view.getTag();
		Form form;
		Object viewModel = null;
		if (tag == null || !(tag instanceof Form)) {
			BindingResources dc;
			if (mField != null) {
				dc = mField.getBindingResources();
				viewModel = mField.getViewModel();
			} else if (this.bindingRes != null) {
				dc = this.bindingRes;
			} else {
				dc = new BindingResources();
			}
			form = Form.build(dc, view, viewModel != null ? viewModel : item);
			view.setTag(form);
		} else {
			form = (Form) tag;
			if (mField != null) {
				viewModel = mField.getViewModel();
			}else {
				viewModel = item;
			}
			form.setViewModel(viewModel);
		}

		form.updateTargets(item);

		if (mField != null) {
			mField.callonTemplateApplied(form, view, item, position);
		}
	}
	
	@Override
	public void onPageLoaded(Exception e) {
		if(e!=null && mField!=null){
			Object viewModel = mField.getViewModel();
			if(viewModel instanceof BaseViewModel){
				BaseViewModel baseViewModel = (BaseViewModel) viewModel;
				if(baseViewModel.getView() != null){
					baseViewModel.getView().onFailure(e);
				}
			}
		}
		super.onPageLoaded(e);
	}

}
