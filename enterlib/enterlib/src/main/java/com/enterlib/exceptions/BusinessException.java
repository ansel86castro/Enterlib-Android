package com.enterlib.exceptions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.enterlib.R;

public class BusinessException extends InvalidOperationException {

	public static final int TYPE_ERROR = 0;
	public static final int TYPE_WARNING = 1;
	public static final int TYPE_INFO = 2;

	/**
	 *
	 */
	private static final long serialVersionUID = 5760587212855754309L;

	private int type;

	public int getType(){
		return type;
	}
    public BusinessException() {
		super();

	}


	public BusinessException(int type, String detailMessage) {
		super(detailMessage);
		this.type = type;
	}

	public BusinessException(int type, String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		this.type = type;
	}

	public BusinessException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);

	}

	public BusinessException(String detailMessage) {
		super(detailMessage);

	}

	public Dialog showDialog(Context context){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);

		switch (type){
			case TYPE_ERROR:
				ab.setTitle(R.string.error);
				ab.setIcon(R.drawable.indicator_input_error);
				break;
			case TYPE_WARNING:
				ab.setTitle(R.string.warning);
				ab.setIcon(R.drawable.ic_action_warning);
				break;
			case TYPE_INFO:
				ab.setTitle(R.string.info);
				ab.setIcon(R.drawable.ic_action_info);
				break;
		}

		ab.setMessage(getLocalizedMessage());
		ab.setPositiveButton(R.string.cerrar,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});

		return ab.show();
	}
}
