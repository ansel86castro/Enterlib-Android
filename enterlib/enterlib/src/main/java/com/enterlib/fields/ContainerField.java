package com.enterlib.fields;

import java.util.ArrayList;

import android.database.DataSetObserver;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;

import com.enterlib.StringUtils;
import com.enterlib.databinding.BindingHandler;
import com.enterlib.databinding.BindingHandlerReflection;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.validations.IValidator;
import com.enterlib.widgets.ViewErrorController;

public class ContainerField extends ItemsField {

	public static final BindingProperty<ContainerField> ValidatorProperty = registerProperty(
			ContainerField.class, new BindingProperty<ContainerField>(
					"Validator") {
				@Override
		public void set(ContainerField object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null && dc.hasValue(member.getValueString())) {
				object.validator = (IValidator) dc.get(member
								.getValueString());
			} else {
				object.addBindingHandler(new BindingHandlerReflection(
								member.getValueString(), member.getKey(), OneWay));
			}
		}
	});
	
	public static final BindingProperty<Field> HideTargetProperty = registerProperty(Field.class, 
			new BindingProperty<Field>("HideTarget") {
			@Override
			public void set(Field object, ExpressionMember value,
						BindingResources dc) {
					String hideTarget = value.getValueString();
					object.addBindingHandler(new HideBindingHandler(hideTarget));
			}
		
	});


	private ViewErrorController errorController;
	IValidator validator;
	ArrayList<Field>childrens;
	
	DataSetObserver adapterObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			ViewGroup viewGroup = (ViewGroup)view;
			viewGroup.removeAllViews();
			
			int count = adapter.getCount();				
			for (int i = 0; i < count; i++) {
				View child = adapter.getView(i, null, viewGroup);
				viewGroup.addView(child);
			}
			viewGroup.invalidate();
		}
	};
	
	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			errorController.showError(null);
		}
	};

	private ListAdapter adapter;

	public ContainerField() {
		setRestorable(false);
	}

	public ContainerField(ViewGroup view) {
		super(view);
		init();
		setRestorable(false);
	}

	public IValidator getValidator() {
		return validator;
	}

	public void setValidator(IValidator validator) {
		this.validator = validator;
	}

	@Override
	protected void onViewChanged() {
		super.onViewChanged();

		init();
	}
	
	
	public void addField(Field field){
		if(childrens == null)
			childrens = new ArrayList<Field>();
		childrens.add(field);
	}
	
	public boolean removeField(Field field){
		if(childrens!=null)
			return childrens.remove(field);
		return false;
	}
	
	public void clearFields(){
		this.childrens.clear();
	}

	private void init() {
		errorController = new ViewErrorController(getView());
		errorController.setOnClickListener(onClickListener);
	}

	@Override
	protected void onSetErrorMessage(String errorMessage) {
		errorController.showError(errorMessage);
	}

	@Override
	protected Object getViewValue() {
		return items;
	}

	@Override
	protected void setViewValue(Object value) {
		this.items = value;
		if (adapterProvider != null) {
			adapterProvider.onItemsSet(this);
		}
	}

	@Override
	public boolean validate() {
		if (validator != null) {
			if (!validator.validate()) {
				setErrorMessage(validator.getErrorMessage());
				return false;
			} else {
				setErrorMessage(null);
				return true;
			}
		}

		return true;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		View view = getView();					
		if (view instanceof AbsListView) {
			((AbsListView) view).setAdapter(adapter);
		} else if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			viewGroup.removeAllViews();
			if(adapter!=null){
				if(this.adapter!=null){
					this.adapter.unregisterDataSetObserver(adapterObserver);
				}
				
				adapter.registerDataSetObserver(adapterObserver);
				int count = adapter.getCount();				
				for (int i = 0; i < count; i++) {
					View child = adapter.getView(i, null, viewGroup);
					viewGroup.addView(child);
				}
			}
			viewGroup.invalidate();
		}
		this.adapter = adapter;
	}
	
	@Override
	public ListAdapter getAdapter() {
		return adapter;
	}
	
	@Override
	public void onPropertyChange(Object object, String propertyName) {
		super.onPropertyChange(object, propertyName);
	
		if (valueBinding!=null && propertyName.equals(valueBinding)) {
			onDataSourceChanged();
		}
	}

	private void onDataSourceChanged() {	
		if(childrens!=null){
			int count = childrens.size();
			for (int i = 0; i < count; i++) {
				Field f =childrens.get(i); 
				f.updateTarget(getValue());	
				
				if(f instanceof ContainerField){
					((ContainerField) f).onDataSourceChanged();
				}
			}
		}
	}

    @Override
    protected void onValueResolved(Object value, boolean asyncLoaded) {
        super.onValueResolved(value, asyncLoaded);
        if(asyncLoaded){
            onDataSourceChanged();
        }
    }

    static class HideBindingHandler extends BindingHandler{
		
		public HideBindingHandler(String sourceProperty) {
			super(sourceProperty);
		}

		@Override
		public void updateTarget(Field field, Object source,
				BindingResources res) {
						
			Object viewModel = field.getViewModel();
			if (!ReflectionResolver.containsProperty(SourceProperty, source) && viewModel != null) {
				source = viewModel;
			}

			Object sourceValue = ReflectionResolver.getValue(SourceProperty, source);
			if(sourceValue == null || (sourceValue instanceof String && StringUtils.isNullOrWhitespace((String)sourceValue))){
				field.setVisible(false);
			}else{
				field.setVisible(true);
			}
		}
		
	}

}
