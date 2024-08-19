package com.enterlib.mvvm;

import android.view.View;
import android.widget.AdapterView;

import com.enterlib.fields.Field;

public abstract class SelectionCommand extends BaseCommand {
	public abstract void invoke(Field field, AdapterView<?> adapterView,
			View itemView, int position, long id);
}
