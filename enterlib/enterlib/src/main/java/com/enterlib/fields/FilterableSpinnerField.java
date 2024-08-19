package com.enterlib.fields;

import java.util.List;

import android.view.View;
import android.widget.ListAdapter;

import com.enterlib.IEqualityComparer;
import com.enterlib.app.CollectionAdapter;
import com.enterlib.app.IFilterableAdapter;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.widgets.FilterableSpinner;

/**
 * This class is similar to the {@link SpinnerField} but it displays an
 * {@link FilterableSpinner} in the UI
 */
public class FilterableSpinnerField extends SelectionField {

    public static final BindingProperty<FilterableSpinnerField> OnChangeProperty = registerProperty(FilterableSpinnerField.class, "OnChange");

    private OnFieldChangeListener onChangeListener;

    public FilterableSpinnerField() {
		super();
	}

	public FilterableSpinnerField(FilterableSpinner view, boolean required) {
		super(view, required);
	}

	public FilterableSpinnerField(View view, String valueBinding,
			String display, boolean required) {
		super(view, valueBinding, display, required);
	}

	public FilterableSpinnerField(FilterableSpinner view, String display,
			boolean required) {
		super(view, display, required);
	}

	public FilterableSpinnerField(FilterableSpinner view, String valueBinding) {
		super(view, valueBinding);
	}

	public FilterableSpinnerField(FilterableSpinner view) {
		super(view);
	}

    public void setOnChange(final OnFieldChangeListener listener){
        this.onChangeListener = listener;
        FilterableSpinner view = ( FilterableSpinner) getView();
        if(listener!=null) {
            view.setOnItemSelectedListener(new FilterableSpinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(Object selectedItem) {
                    listener.onChange(FilterableSpinnerField.this, selectedItem);
                }
            });
        }else{
            view.setOnItemSelectedListener(null);
        }
    }

	public FilterableSpinner getSpinner() {
		return (FilterableSpinner) view;
	}

	@Override
	protected void onSetErrorMessage(String errorMessage) {
		getSpinner().setError(errorMessage);
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
			FilterableSpinner sp = getSpinner();
			sp.setSelectedPosition(0);
		}else{
			findSelection(value);
		}
	}

	private void findSelection(Object value) {
        if(getComparer() != null){
            FilterableSpinner sp = getSpinner();
            android.widget.SpinnerAdapter adapter = sp.getAdapter();
            if (adapter != null) {
                int selection = Math.max(0, findSelection(value, adapter));
                sp.setSelectedPosition(selection);
            }
        }
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		FilterableSpinner sp = getSpinner();
		sp.setAdapter((IFilterableAdapter) adapter);

		if (value != null) {
			findSelection(value);
		}else{
			sp.setSelectedPosition(0);
		}
	}
	@Override
	public ListAdapter getAdapter() {
		FilterableSpinner sp = getSpinner();
		if(sp == null)
			return null;
	    return sp.getAdapter();
	}

	@Override
	protected void setDefaultAdapter(List<Object> list) {
		setAdapter(new CollectionAdapter<Object>(view.getContext(),
				android.R.layout.simple_list_item_single_choice, list));

	}
}
