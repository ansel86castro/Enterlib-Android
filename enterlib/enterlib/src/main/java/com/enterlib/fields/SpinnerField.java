package com.enterlib.fields;

import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.enterlib.IEqualityComparer;
import com.enterlib.app.SimpleSpinnerAdapter;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.widgets.FilterableSpinner;
import com.enterlib.widgets.ViewErrorController;

public class SpinnerField extends SelectionField {

    public static final BindingProperty<SpinnerField> OnChangeProperty = registerProperty(SpinnerField.class, "OnChange");

	private ViewErrorController errorController;
    private OnFieldChangeListener onChangeListener;

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			errorController.showError(null);
		}
	};

	public SpinnerField() {
		super();
	}

	public SpinnerField(Spinner view, boolean required) {
		super(view, required);

		init();
	}

	public SpinnerField(View view, String valueBinding, String display,
			boolean required) {
		super(view, valueBinding, display, required);
		init();
	}

	public SpinnerField(Spinner view, String display, boolean required) {
		super(view, display, required);

		init();
	}

	public SpinnerField(Spinner view, String valueBinding) {
		super(view, valueBinding);

		init();
	}

	public SpinnerField(Spinner view) {
		super(view);

		init();
	}

	private void init() {
		errorController = new ViewErrorController(getView());
		errorController.setOnClickListener(onClickListener);
	}

    public void setOnChange(final OnFieldChangeListener listener){
        this.onChangeListener = listener;
        AdapterView<?> view = ( AdapterView<?>) getView();
        if(listener!=null) {
            view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object item = parent.getSelectedItem();
                    listener.onChange(SpinnerField.this, item);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }else{
            view.setOnItemSelectedListener(null);
        }
    }

	@Override
	protected void onViewChanged() {
		super.onViewChanged();

		init();
	}

	public Spinner getSpinner() {
		return (Spinner) getView();
	}

	@Override
	protected void onSetErrorMessage(String errorMessage) {
		errorController.showError(errorMessage);
	}

	/**
	 * returns the {@link FilterableSpinner} selectedItem. This is the same as
	 * calling {@code getSpinner().getSelectedItem()}
	 * */
	@Override
	protected Object getViewValue() {
		return getSpinner().getSelectedItem();
	}

	/**
	 * Set the current position of the spinner.It uses an
	 * {@link IEqualityComparer} for comparing the parameter value with the
	 * objects in the adapter's collection
	 */
	@Override
	protected void setViewValue(Object value) {
		super.setViewValue(value);				
		if(value == null){
			Spinner sp = getSpinner();
			sp.setSelection(0);
		}else {
			findSelection(value);
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		Spinner sp = getSpinner();
		sp.setAdapter((SpinnerAdapter) adapter);

		if (value != null) {
			findSelection(value);
		}else
			sp.setSelection(0);

	}
	
	@Override
	public ListAdapter getAdapter() {
		Spinner sp = getSpinner();
		if(sp == null)
			return null;
		return (ListAdapter) sp.getAdapter();
	}

	private void findSelection(Object value) {
		Spinner sp = getSpinner();
		android.widget.SpinnerAdapter adapter = sp.getAdapter();
		if (adapter == null) {
			return;
		}

		int selection = Math.max(0, findSelection(value, adapter));
		sp.setSelection(selection);

//		if (valueConverter == null && (value instanceof Integer) && (adapter.getItem(selection) instanceof BaseModel)) {
//			valueConverter = new BaseModelConverter();
//		}
	}

	@Override
	protected void setDefaultAdapter(List<Object> list) {
		setAdapter(new SimpleSpinnerAdapter(view.getContext(),
				android.R.layout.simple_list_item_single_choice, list));
	}

}
