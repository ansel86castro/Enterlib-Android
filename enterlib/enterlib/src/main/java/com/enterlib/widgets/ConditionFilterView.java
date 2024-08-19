package com.enterlib.widgets;

import java.util.Date;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.enterlib.R;
import com.enterlib.filtering.FilterCondition;
import com.enterlib.filtering.SearchViewFilterController;
import com.enterlib.widgets.DateTimePickerButton.OnDateChangeListener;

public class ConditionFilterView extends FrameLayout implements TextWatcher,
		OnEditorActionListener, View.OnClickListener {

	private ViewGroup mRoot;
	private ViewGroup mContainer;
	private EditText mSearchEdit;
	private ImageButton mShowCriterias;

	private SearchViewFilterController controller;
	private FilterCondition _condition;
	private boolean doFilter = true;
	private ImageButton clearButton;

	public ConditionFilterView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ConditionFilterView(Context context) {
		this(context, null);
	}

	public ConditionFilterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = LayoutInflater.from(context);
		mRoot = (ViewGroup) inflater.inflate( com.enterlib.R.layout.layout_search_box, this, false);

		mContainer = (ViewGroup) mRoot.findViewById(R.id.container);
		mSearchEdit = (EditText) mContainer.findViewById(R.id.searchEdit);

		mShowCriterias = (ImageButton) mRoot.findViewById(R.id.showCriterias);
		mShowCriterias.setOnClickListener(this);

		clearButton = (ImageButton) mRoot.findViewById(R.id.imageBtnClear);
		clearButton.setOnClickListener(this);
		
		
		mSearchEdit.setOnEditorActionListener(this);

		addView(mRoot);
	}

	public void setController(SearchViewFilterController controller) {
		this.controller = controller;

		if (controller!=null && controller.getConditionsCount() <= 1) {
			mShowCriterias.setVisibility(GONE);
		}
	}

	public FilterCondition getCondition() {
		return _condition;
	}

	public void setCondition(FilterCondition condition) {
		this._condition = condition;
		clearButton.setVisibility(View.GONE);
		
		// mark to do not apply filtering where changing the default editView
		doFilter = false;

		if (_condition.getView() == null) {
			if (_condition.getQueryValue() != null) {
				mSearchEdit.setText(_condition.getQueryValue().toString());
			} else {
				mSearchEdit.setText(null);
			}

			mSearchEdit.addTextChangedListener(this);
			mSearchEdit.setHint(_condition.getQueryHint());

			int hintColor = getResources().getColor(R.color.hint);
			mSearchEdit.setHintTextColor(hintColor);

			// if(_condition instanceof StringFilterCondition<?>){
			// StringFilterCondition<?> stringCondition =
			// (StringFilterCondition<?>) _condition;
			// mSearchEdit.setInputType(stringCondition.getInputType());
			// }else{
			// mSearchEdit.setInputType(InputType.TYPE_CLASS_TEXT);
			// }

			if (mContainer.getChildAt(0) != mSearchEdit) {
				mContainer.removeAllViews();
				mContainer.addView(mSearchEdit);
			}

			mSearchEdit.invalidate();

		} else {
			View conditionView = _condition.getView();

			if (conditionView instanceof TextView) {
				TextView tv = (TextView) conditionView;
				tv.setHint(_condition.getQueryHint());
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

			mContainer.removeAllViews();
			mContainer.addView(conditionView);
		}

		// restore filtering flag
		doFilter = true;

		afterTextChanged(null);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			afterTextChanged(null);

			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			return true;
		}
		return false;
	}
	
	public void hideInput(){
		if (_condition == null || controller == null) {
			return;
		}
		
		View v;
		if (_condition.getView() != null){
			v = _condition.getView();					
		}else{
			v=mSearchEdit;
		}
		
		if(v instanceof EditText){
			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
		
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
		if (_condition == null || controller == null || !doFilter) {
			return;
		}

		if (_condition.getView() == null) {
			String value = mSearchEdit.getText().toString();
			_condition.setQueryValue(value);
		} else {
			_condition.updateQueryValue();
		}		
		
		Object queryValue= _condition.getQueryValue();
		if(!_condition.hideClear(queryValue)){
			if(clearButton.getVisibility() != View.VISIBLE)
				clearButton.setVisibility(View.VISIBLE);
		}else if(clearButton.getVisibility() != View.GONE){
			clearButton.setVisibility(View.GONE);
		}
		
		controller.performFiltering(_condition);
	}

	public void doFilter() {
		afterTextChanged(null);
	}

	@Override
	public void onClick(View v) {
		if (_condition == null || controller == null) {
			return;
		}

		if(v.getId() == R.id.showCriterias){
			if (_condition.getView() == null) {
				_condition.setQueryValue(mSearchEdit.getText().toString());
			} else {
				_condition.updateQueryValue();
			}
	
			if (controller.getConditionsCount() > 1) {
				hideInput();
				controller.showConditionsDialog();
			}
		}else if(v.getId() == R.id.imageBtnClear){
			if (_condition.getView() == null) {
				mSearchEdit.setText(null);
				_condition.setQueryValue(null);
			} else {				
				_condition.clear();
				controller.performFiltering(_condition);
			}			
		}

	}

	public void clearValue() {
		doFilter = false;

		mSearchEdit.setText(null);

		doFilter = true;
	}
}
