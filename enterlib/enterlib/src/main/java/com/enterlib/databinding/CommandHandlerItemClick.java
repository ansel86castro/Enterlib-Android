package com.enterlib.databinding;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.enterlib.fields.Field;
import com.enterlib.fields.ListField;
import com.enterlib.generics.IObserver;
import com.enterlib.mvvm.SelectionCommand;

public class CommandHandlerItemClick extends BindingHandler implements IObserver<Boolean>, OnItemClickListener {

    public CommandHandlerItemClick(String sourceProperty) {
		super(sourceProperty);
		Mode = Field.OneTime;
	}

	private SelectionCommand command;
	AdapterView<?> view;
	protected Field field;
	protected boolean enableItemClick = true;

	@Override
	public void updateTarget(Field field, Object source, BindingResources res) {
		this.field = field;
		this.view = (AdapterView<?>) field.getView();

		Object viewModel = field.getViewModel();
		if (!ReflectionResolver.containsProperty(SourceProperty, source)
				&& viewModel != null) {
			source = viewModel;
		}

		if(command!=null){
			command.getEnabledObservable().removeObserver(this);
		}

		command = (SelectionCommand) ReflectionResolver.getValue(SourceProperty, source);
		if (command != null) {
			field.setEnabled(getCommand().isEnabled());
			getCommand().getEnabledObservable().addObserver(this);
		}else{
			field.setEnabled(true);
		}

		this.view.setOnItemClickListener(this);
	}

	@Override
	public void onNotify(Boolean value) {
		this.enableItemClick = value;
		field.setEnabled(enableItemClick);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (!enableItemClick) {
			return;
		}			

		if(command!=null) {
			command.invoke(field, parent, view, position, id);
		}
	}

	public SelectionCommand getCommand() {
		return command;
	}

	public void setCommand(SelectionCommand command) {
		this.command = command;
	}
}