package com.enterlib.databinding;

import android.view.View;

import com.enterlib.fields.Field;
import com.enterlib.generics.IObserver;
import com.enterlib.mvvm.Command;

public class CommandHandlerClick extends BindingHandler implements
		View.OnClickListener, IObserver<Boolean> {

    private Object source;
    Command command;
    Field field;

    public CommandHandlerClick(String sourceProperty) {
		super(sourceProperty);
		Mode = Field.OneTime;
	}


	@Override
	public void updateTarget(Field field, Object source, BindingResources res) {
		this.field = field;
        this.source = source;

		View view = field.getView();

		Object viewModel = field.getViewModel();
		if (!ReflectionResolver.containsProperty(SourceProperty, source)
				&& viewModel != null) {
			source = viewModel;
		}

		if(command!=null){
			command.getEnabledObservable().removeObserver(this);
		}

		command = (Command)ReflectionResolver.getValue(SourceProperty, source);
		if (command != null) {
			field.setEnabled(command.isEnabled());
			command.getEnabledObservable().addObserver(this);
		}else{
			field.setEnabled(true);
		}

		view.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (command == null) {
			return;
		}

		command.invoke(field, source);

	}

	@Override
	public void onNotify(Boolean value) {
		field.setEnabled(value);
	}
}