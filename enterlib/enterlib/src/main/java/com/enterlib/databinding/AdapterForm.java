package com.enterlib.databinding;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;

import com.enterlib.fields.Form;
import com.enterlib.fields.ItemsField;

public class AdapterForm extends com.enterlib.app.ListAdapter<Object> {

	private ItemsField field;
	private BindingResources bindingRes;

	public AdapterForm(Context context, int layout, ArrayList<Object> objects, ItemsField field) {
		super(context, layout, objects);

		this.field = field;
	}

	public AdapterForm(Context context, int layout, int dropDownLayout, ArrayList<Object> objects, ItemsField field){
		super(context , layout, dropDownLayout, objects);
		this.field = field;
	}

	public AdapterForm(Context context, int layout, Object[] objects,
			ItemsField field) {
		super(context, layout, objects);

		this.field = field;
	}

	public AdapterForm(Context context, int layout, ArrayList<Object> objects,
			BindingResources bindingRes) {
		super(context, layout, objects);

		this.bindingRes = bindingRes;
	}

	public AdapterForm(Context context, int layout, Object[] objects,
			BindingResources bindingRes) {
		super(context, layout, objects);

		this.bindingRes = bindingRes;
	}

	@Override
	protected void updateView(View view, Object item, int position) {
		Object tag = view.getTag();
		Form form;
		Object viewModel = null;
		if (tag == null || !(tag instanceof Form)) {
			BindingResources dc;
			if (field != null) {
				dc = field.getBindingResources();
				viewModel = field.getViewModel();
			} else if (this.bindingRes != null) {
				dc = this.bindingRes;
			} else {
				dc = new BindingResources();
			}
			form = Form.build(dc, view, viewModel != null ? viewModel : item);
			view.setTag(form);
		} else {
			form = (Form) tag;
			if (field != null) {
				viewModel = field.getViewModel();
			}else{
				viewModel = item;
			}

			form.setViewModel(viewModel);
		}

		form.updateTargets(item);

		if (field != null) {
			field.callonTemplateApplied(form, view, item, position);
		}
	}

}