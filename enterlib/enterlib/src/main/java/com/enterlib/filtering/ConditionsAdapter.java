package com.enterlib.filtering;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.enterlib.R;
import com.enterlib.StringUtils;
import com.enterlib.app.ListAdapter;

class ConditionsAdapter extends ListAdapter<FilterCondition> implements
		OnCheckedChangeListener, OnClickListener {
	IConditionStateCallback controller;

	public interface IConditionStateCallback {

		void setActive(FilterCondition condition, boolean value);

		void onConditionSelected(FilterCondition condition);
	}

	public ConditionsAdapter(Context context,
			IConditionStateCallback controller,
			ArrayList<FilterCondition> objects) {
		super(context, R.layout.adapter_search_conditions, objects);
		this.controller = controller;
	}

	@Override
	protected void updateView(View view, FilterCondition item, int position) {
		Button tvCondition = (Button) view.findViewById(R.id.lbCondition);
		tvCondition.setText(item.queryHint);
		tvCondition.setOnClickListener(this);
		tvCondition.setTag(item);

		TextView tvValue = (TextView) view.findViewById(R.id.lbQueryVal);
		Object value = item.getConvertedValue();
		if (value != null) {
			String str = value.toString();
			tvValue.setText(str);
			tvValue.setVisibility(!StringUtils.isNullOrWhitespace(str) ? View.VISIBLE : View.GONE);
		} else {
			tvValue.setText(null);
			tvValue.setVisibility(View.GONE);
		}

		CheckBox cb = (CheckBox) view.findViewById(R.id.cbActive);
		cb.setTag(item);
		cb.setChecked(item.isActive);
		
		if(!item.isCombinable){
			cb.setEnabled(false);
			cb.setClickable(false);
			cb.setVisibility(View.INVISIBLE);
		}else{
			cb.setEnabled(true);
			cb.setClickable(true);
			cb.setVisibility(View.VISIBLE);
		}

		cb.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		FilterCondition c = (FilterCondition) buttonView.getTag();

		controller.setActive(c, isChecked);
	}

	@Override
	public void onClick(View v) {
		FilterCondition c = (FilterCondition) v.getTag();
		controller.onConditionSelected(c);

	}

}