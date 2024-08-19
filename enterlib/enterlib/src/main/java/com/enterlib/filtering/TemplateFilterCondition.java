package com.enterlib.filtering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.enterlib.data.IFactory;
import com.enterlib.databinding.BindingResources;
import com.enterlib.fields.Form;

public class TemplateFilterCondition extends FilterCondition {

	Form form;
	private View rootView;
	private IFactory<Object>viewModelFactory;
	
	public TemplateFilterCondition(String queryName, String queryHint, Context context, int layoutResource, BindingResources res, Object viewModel ) {
		super(queryName, queryHint);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		rootView = inflater.inflate(layoutResource, null);
		form = Form.build(res, rootView, viewModel);
		form.updateTargets();
	}

	public Form getForm() {
		return form;
	}


	public IFactory<Object> getViewModelFactory() {
		return viewModelFactory;
	}

	public TemplateFilterCondition setViewModelFactory(IFactory<Object> viewModelFactory) {
		this.viewModelFactory = viewModelFactory;
		return this;
	}

	@Override
	public View getView() {
		return rootView;
	}
	
	@Override
	public void clear() {
		Object viewModel = form.getViewModel();
		if(viewModel instanceof IClearable){
			((IClearable) viewModel).clear();
		}else if(viewModelFactory!=null){
			viewModel = viewModelFactory.getInstance();
		}
		else if(viewModel != null){
			try {
				viewModel = viewModel.getClass().newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		form.setViewModel(viewModel);
		form.updateTargets();
		
	}
	
	@Override
	public void updateQueryValue() {
		form.updateSource();
		setQueryValue(form.getViewModel());
	}
	
}
