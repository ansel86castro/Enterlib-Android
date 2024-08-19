package com.enterlib.filtering;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.enterlib.R;
import com.enterlib.widgets.DateTimePickerButton;
import com.enterlib.widgets.DateTimePickerButton.OnDateChangeListener;
import com.enterlib.widgets.FilterableSpinner;

public class ConditionFilterDialog extends AlertDialog implements
		OnItemClickListener, TextWatcher, OnEditorActionListener {

	public static interface OnItemSelectedListener {
		void onItemSelected(AdapterView<?> parent, View itemView, int position,
				Object item);
	}

	private EditText _mSearchEdit;
	private ListView _mListView;
	private int _selectedPosition;
	private OnItemSelectedListener _onSelectedItemlistener;
	private FilterCondition _condition;
	private DialogFilterController _controller;
	private View rootView;

	public ConditionFilterDialog(Context context, FilterCondition condition,
			DialogFilterController controller, ListAdapter adapter,
			int selectedIndex) {
		super(context);

		this._controller = controller;

		setIcon(0);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		rootView = inflater.inflate(R.layout.dialog_search_custom, null);

		setView(rootView);
		setButton(DialogInterface.BUTTON_NEGATIVE,
				context.getString(R.string.condition_filter_dialog_close),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		if (_controller.getConditionsCount() > 1) {
			setButton(
					DialogInterface.BUTTON_POSITIVE,
					context.getString(R.string.condition_filter_dialog_criterias),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (_mSearchEdit != null) {
								_condition.queryValue = _mSearchEdit.getText()
										.toString();
							} else {
								_condition.updateQueryValue();								
							}
							if (_controller.getConditionsCount() > 1) {
								_controller.showConditionsDialog();
							}
						}
					});
		}

		_mListView = (ListView) rootView.findViewById(R.id.listItems);
		_mListView.setOnItemClickListener(this);
		_mListView.setAdapter(adapter);
		_selectedPosition = selectedIndex;

		if (_selectedPosition >= 0) {
			_mListView.setSelection(_selectedPosition);
			_mListView.setItemChecked(_selectedPosition, true);
		}

		setCondition(condition);
	}

	public FilterCondition getCondition() {
		return _condition;
	}

	public void setCondition(FilterCondition condition) {
		this._condition = condition;
		this._mSearchEdit = null;
		
		View conditionView = _condition.getView();
		if (conditionView == null) {
			_mSearchEdit = (EditText) rootView.findViewById(R.id.searchEdit);
			if (_condition.queryValue != null) {
				_mSearchEdit.setText(_condition.queryValue.toString());
			}

			_mSearchEdit.addTextChangedListener(this);
			_mSearchEdit.setOnEditorActionListener(this);
			_mSearchEdit.setHint(_condition.queryHint);

			int hintColor = rootView.getResources().getColor(R.color.hint);
			_mSearchEdit.setHintTextColor(hintColor);

		} else {
			FrameLayout container = (FrameLayout) rootView
					.findViewById(R.id.container);			
			if (conditionView instanceof TextView) {
				TextView tv = (TextView) conditionView;
				tv.setHint(_condition.queryHint);

				int hintColor = getContext().getResources().getColor(
						R.color.hint);
				tv.setHintTextColor(hintColor);
			}

			if (conditionView instanceof EditText) {
				EditText edit = (EditText) conditionView;
				edit.addTextChangedListener(this);
				edit.setOnEditorActionListener(this);
			} else if (conditionView instanceof CompoundButton) {
				((CompoundButton) conditionView)
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								afterTextChanged(null);
							}
						});
			} else if (conditionView instanceof DateTimePickerButton) {
				((DateTimePickerButton) conditionView)
						.setOnDateChangeListener(new OnDateChangeListener() {
							@Override
							public void onDateChange(DateTimePickerButton view,
									Date date) {
								afterTextChanged(null);
							}
						});
			} else if (conditionView instanceof Spinner) {
				((Spinner) conditionView)
						.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent,
									View view, int position, long id) {
								afterTextChanged(null);
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {

							}

						});
			} else if (conditionView instanceof FilterableSpinner) {
				((FilterableSpinner) conditionView)
						.setOnItemSelectedListener(new FilterableSpinner.OnItemSelectedListener() {
							@Override
							public void onItemSelected(Object selectedItem) {
								afterTextChanged(null);
							}
						});
			}

			ViewGroup parent = (ViewGroup) conditionView.getParent();
			if (parent != null) {
				parent.removeView(conditionView);
			}

			container.removeAllViews();
			container.addView(conditionView);

		}

		afterTextChanged(null);
	}

	public void setOnSelectedItemlistener(
			OnItemSelectedListener _onSelectedItemlistener) {
		this._onSelectedItemlistener = _onSelectedItemlistener;
	}

	@Override
	public void dismiss() {
		if (_condition.getView() != null) {
			FrameLayout container = (FrameLayout) rootView
					.findViewById(R.id.container);
			View conditionView = _condition.getView();
			container.removeView(conditionView);
		}
		super.dismiss();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			afterTextChanged(null);

			if (_mSearchEdit != null) {
				InputMethodManager imm = (InputMethodManager) getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTextChanged(Editable s) {
		Adapter adapter = _mListView.getAdapter();
		if (adapter != null) {
			if (this._mSearchEdit != null) {
				_condition.setQueryValue(_mSearchEdit.getText().toString());
			} else  {				
				_condition.updateQueryValue();
			}

			_controller.performFiltering(_condition);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (_mSearchEdit != null) {
			_mSearchEdit.clearFocus();
		}
		if (_onSelectedItemlistener != null) {
			_onSelectedItemlistener.onItemSelected(parent, view, position,
					_mListView.getItemAtPosition(position));
		}
		// dismiss();
	}

}
