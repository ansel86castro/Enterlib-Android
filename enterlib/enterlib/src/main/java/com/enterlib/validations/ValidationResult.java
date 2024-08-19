package com.enterlib.validations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.enterlib.app.UIUtils;
import com.enterlib.databinding.BindingResources;

import android.content.Context;
import android.content.res.Resources;

public class ValidationResult implements Serializable {
	
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<String> _messages;	
	private String _field;	

	public ValidationResult(String field, String errorMessage) {
		this._field = field;

		this._messages = new ArrayList<String>(1);
		this._messages.add(errorMessage);
	}	
		
	public List<String> getListErrors() {
		return _messages;
	}
	

	public String getField() {
		return _field;
	}

	public String getError() {
		if (_messages.size() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (String string : _messages) {
			sb.append(string);
			sb.append('\n');
		}
		return sb.toString();
	}

	public String getError(Context context) {
		if (_messages.size() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (String string : _messages) {
			int resId = UIUtils.findResourceId(context, string, "string");
			if(resId == 0)
				sb.append(string);
			else
				sb.append(context.getString(resId));			
			sb.append('\n');
		}
		return sb.toString();
	}

	
	@Override
	public String toString() {
		return _field;
	}

	public void add(String errorMessage) {
		_messages.add(errorMessage);

	}
	
	

}
