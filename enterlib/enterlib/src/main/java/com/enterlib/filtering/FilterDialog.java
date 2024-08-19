package com.enterlib.filtering;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.enterlib.R;
import com.enterlib.app.IFilterableAdapter;

public class FilterDialog extends AlertDialog implements OnItemClickListener,
		TextWatcher, OnEditorActionListener {

	public static interface OnItemSelectedListener {
		void onItemSelected(AdapterView<?> parent, View itemView, int position,
				Object item);
	}

	// private FilterableSpinner control;
	private IFilterableAdapter _adapter;
	private EditText mSearchEdit;
	private ListView mListView;
	private int _selectedPosition;
	private FilterDialog.OnItemSelectedListener onSelectedItemlistener;

	public ListAdapter getAdapter() {
		return _adapter;
	}

	public void setAdapter(IFilterableAdapter _adapter) {
		this._adapter = _adapter;
	}

	public int getSelectedPosition() {
		return _selectedPosition;
	}

	public void setSelectedPosition(int _selectedPosition) {
		this._selectedPosition = _selectedPosition;
	}

	public FilterDialog.OnItemSelectedListener getOnSelectedItemlistener() {
		return onSelectedItemlistener;
	}

	public void setOnSelectedItemlistener(
			FilterDialog.OnItemSelectedListener onSelectedItemlistener) {
		this.onSelectedItemlistener = onSelectedItemlistener;
	}

	public FilterDialog(Context context, IFilterableAdapter adapter,
			int selectedPosition) {
		super(context);

		this._adapter = adapter;
		this._selectedPosition = selectedPosition;

		// setButton(BUTTON_POSITIVE, themeContext.getText(R.string.aceptar),
		// this);
		setIcon(0);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.search_list_popup, null);

		setView(view);

		mListView = (ListView) view.findViewById(R.id.listItems);
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(_adapter);
		if (_selectedPosition >= 0) {
			mListView.setSelection(_selectedPosition);
			mListView.setItemChecked(_selectedPosition, true);
		}

		mSearchEdit = (EditText) view.findViewById(R.id.searchEdit);
		mSearchEdit.addTextChangedListener(this);
		mSearchEdit.setOnEditorActionListener(this);

		int hintColor = context.getResources().getColor(R.color.hint);
		mSearchEdit.setHintTextColor(hintColor);

		adapter.filter(null, null);
	}

	public void setFilterCriteria(String value) {
		mSearchEdit.setHint(value);
	}

	public void setFilterCriteria(int resid) {
		mSearchEdit.setHint(resid);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		mSearchEdit.clearFocus();
		if (onSelectedItemlistener != null) {
			onSelectedItemlistener.onItemSelected(parent, view, position,
					_adapter.getItem(position));
		}
		dismiss();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (_adapter != null) {
			String text = mSearchEdit.getText().toString();
			_adapter.filter(text, null);			
		}

	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			// Handle search key click
			if (_adapter != null) {
				String text = mSearchEdit.getText().toString();
				_adapter.filter(text, null);				
			}
			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			return true;
		}
		return false;
	}

}