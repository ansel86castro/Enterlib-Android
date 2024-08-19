package com.enterlib.databinding;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.enterlib.fields.Field;
import com.enterlib.generics.IObserver;
import com.enterlib.mvvm.SelectionCommand;

public class CommandHanlderItemSelected extends BindingHandler implements
		IObserver<Boolean>, OnItemSelectedListener {
	public CommandHanlderItemSelected(String sourceProperty) {
		super(sourceProperty);
		Mode = Field.OneTime;
	}

	SelectionCommand command;
	AdapterView<?> view;
	Field field;
	boolean enableItemClick = true;

	@Override
	public void updateTarget(Field field, Object source, BindingResources res) {
		this.field = field;
		this.view = (AdapterView<?>) field.getView();

		Object viewModel = field.getViewModel();
		if (!ReflectionResolver.containsProperty(SourceProperty, source) && viewModel != null) {
			source = viewModel;
		}

		if(command != null){
			command.getEnabledObservable().removeObserver(this);
		}

		command = (SelectionCommand) ReflectionResolver.getValue(SourceProperty, source);
		if (command != null) {
			field.setEnabled(command.isEnabled());
			command.getEnabledObservable().addObserver(this);
		}else{
			field.setEnabled(true);
		}

		this.view.setOnItemSelectedListener(this);
	}

	@Override
	public void onNotify(Boolean value) {
		this.enableItemClick = value;
		field.setEnabled(enableItemClick);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (!enableItemClick) {
			return;
		}
		if(command!=null)
			command.invoke(field, parent, view, position, id);

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}
}