package com.enterlib.widgets;

import java.util.List;

import android.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;

import com.enterlib.app.CollectionAdapter;
import com.enterlib.app.IFilterableAdapter;
import com.enterlib.filtering.FilterDialog;

@SuppressLint("AppCompatCustomView")
public class FilterableSpinner extends Button implements OnClickListener {

	public interface OnItemSelectedListener {
		void onItemSelected(Object selectedItem);
	}

	FilterDialog mDialog;
	int mSelectedPosition = 0;
	OnItemSelectedListener mSelectListener;
	IFilterableAdapter adapter;
	private TextViewErrorController errorController;
	Context baseContext;

	public FilterableSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.baseContext = context;
		init();
	}

	public FilterableSpinner(Context context) {
		super(context);
		this.baseContext = context;
		init();
	}

	public FilterableSpinner(Context context, AttributeSet attrs) {
		super(context, attrs, R.style.Widget_DeviceDefault_Light_Spinner );

		this.baseContext = context;
		init();
		// @android:style/Widget.DeviceDefault.Light.Spinner
	}

	private void init() {
		this.setOnClickListener(this);
		// this.setBackgroundResource(android.R.drawable.btn_dropdown);
		this.errorController = new TextViewErrorController(this);


	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.mSelectListener = listener;
	}

	public void setAdapter(IFilterableAdapter adapter) {
		this.adapter = adapter;
	}

	public IFilterableAdapter getAdapter() {
		return adapter;
	}

	public int getSelectedPosition() {
		return mSelectedPosition;
	}

	public Object getSelectedItem() {
		if (adapter == null || mSelectedPosition < 0
				|| mSelectedPosition >= adapter.getCount()) {
			return null;
		}

		return adapter.getItem(mSelectedPosition);
	}

	public void setSelectedPosition(int position) {
		if (adapter == null || mSelectedPosition >= adapter.getCount()) {
			setText(null);
			mSelectedPosition = 0;
			return;
		}
		mSelectedPosition = position;
		Object item = adapter.getItem(position);
		if (item != null) {
			setError(null);
			setText(item.toString());
			if (mSelectListener != null) {
				mSelectListener.onItemSelected(item);
			}
		} else {
			setText("");
		}
	}

	public void setItems(Object[] values) {
		CollectionAdapter<Object> adapter = new CollectionAdapter<Object>(
				baseContext, android.R.layout.simple_list_item_single_choice,
				values);
		setAdapter(adapter);
	}

	public <T> void setItems(List<T> values) {
		CollectionAdapter<T> adapter = new CollectionAdapter<T>(baseContext,
				android.R.layout.simple_list_item_single_choice, values);
		setAdapter(adapter);
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	public void showDialog() {
		if (adapter != null) {
			mDialog = new FilterDialog(baseContext, adapter, mSelectedPosition);
			mDialog.setOnSelectedItemlistener(new FilterDialog.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent,
						View itemView, int position, Object item) {
					setSelectedPosition(position);

				}
			});
			mDialog.show();
		}
	}

	@Override
	public void onClick(View v) {
		errorController.showError(null);
		showDialog();
	}

	@Override
	public void setError(CharSequence error) {
		errorController.showError(error);
	}

}
