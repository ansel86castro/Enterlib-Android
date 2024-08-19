package com.enterlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/** Class used to show common Android Dialogs */
public final class DialogUtil {

	public static void showAlertDialog(Context context, String title,
			String message, DialogInterface.OnClickListener acceptListener) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(title);
		ab.setMessage(message);
		ab.setIcon(R.drawable.ic_action_warning);
		ab.setPositiveButton(R.string.cerrar, acceptListener);
		ab.show();

	}
	
	public static void showAlertDialog(Context context, 
			String title,
			String message, 
			DialogInterface.OnClickListener acceptListener, 
			DialogInterface.OnClickListener cancelListener) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(title);
		ab.setMessage(message);
		ab.setIcon(R.drawable.ic_action_warning);
		ab.setNegativeButton(R.string.cancel, cancelListener);
		ab.setPositiveButton(R.string.accept, acceptListener);
		ab.show();

	}
	
	public static void showAlertDialog(Context context, 
			String title,		
			DialogInterface.OnClickListener acceptListener, 
			DialogInterface.OnClickListener cancelListener) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(title);		
		ab.setIcon(R.drawable.ic_action_warning);
		ab.setNegativeButton(R.string.cancel, cancelListener);
		ab.setPositiveButton(R.string.accept, acceptListener);
		ab.show();

	}

	
	public static void showAlertDialog(Context context, String title, DialogInterface.OnClickListener acceptListener) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(title);		
		ab.setIcon(R.drawable.ic_action_warning);
		ab.setPositiveButton(R.string.cerrar, acceptListener);
		ab.show();

	}

	public static void showInfoDialog(Context context, String title,
			String message, DialogInterface.OnClickListener acceptListener) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(title);
		ab.setMessage(message);
		ab.setIcon(R.drawable.ic_action_info);
		ab.setPositiveButton(R.string.cerrar, acceptListener);
		ab.show();

	}

	
	public static void showErrorDialog(Context context, String message,
			DialogInterface.OnClickListener acceptListener) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(R.string.error);
		ab.setMessage(message);
		ab.setIcon(R.drawable.indicator_input_error);
		ab.setPositiveButton(R.string.cerrar, acceptListener);
		ab.show();
	}

	public static void showErrorDialog(Context context, String message) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(R.string.error);
		ab.setMessage(message);
		ab.setIcon(R.drawable.indicator_input_error);
		ab.setPositiveButton(R.string.cerrar,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
		ab.show();
	}

	public static void showErrorDialogAndFinish(final Activity activity,
			String message) {
		AlertDialog.Builder ab = new AlertDialog.Builder(activity);
		ab.setTitle(R.string.error);
		ab.setMessage(message);
		ab.setIcon(R.drawable.indicator_input_error);
		ab.setPositiveButton(R.string.cerrar,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						activity.finish();
					}
				});
		ab.show();
	}

	// public static void showErrorDialog(Context context, ErrorInfo e, String
	// title){
	// AlertDialog.Builder ab = new AlertDialog.Builder(context);
	// ab.setTitle(title!=null? title : context.getString(R.string.error));
	// ab.setPositiveButton(R.string.cerrar, null);
	// ab.setIcon(R.drawable.indicator_input_error);
	//
	// LayoutInflater inflater = (LayoutInflater)
	// context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// View rootView= inflater.inflate(R.layout.dialog_notify, null);
	// TableLayout table = (TableLayout) rootView.findViewById(R.id.panel);
	// TableLayout.LayoutParams param = new
	// TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
	// TableLayout.LayoutParams.WRAP_CONTENT);
	//
	// //display general errors
	// String generalError = e.getGeneralError();
	// if(!generalError.isEmpty()){
	// TableRow row = new TableRow(context);
	// row.setLayoutParams(param);
	// row.setPadding(0, 0, 0, 10);
	//
	// TextView label = (TextView) inflater.inflate(R.layout.error_text_notify,
	// null);
	// label.setText(generalError);
	// row.addView(label);
	// table.addView(row);
	// }
	//
	// for (ValidationResult item : e.getValidationResults()) {
	// TableRow row = new TableRow(context);
	// row.setLayoutParams(param);
	// row.setPadding(0, 0, 0, 10);
	//
	// //add field name at column 0
	// TextView label = (TextView) inflater.inflate(R.layout.error_field_notify,
	// null);
	// label.setText(item.getField());
	// row.addView(label);
	//
	// //add field error at column 1
	// label = (TextView) inflater.inflate(R.layout.error_field_value_notify,
	// null);
	// label.setText(item.getError());
	// row.addView(label);
	//
	// //add the row to the table
	// table.addView(row);
	// }
	//
	// ab.setView(rootView);
	// ab.show();
	// }

	public static ProgressDialog getProgressDialog(Context context, int stringID) {
		return getProgressDialog(context, context.getString(stringID));
	}

	public static ProgressDialog getProgressDialog(Context context,
			String message) {
		ProgressDialog progresbar = new ProgressDialog(context);
		progresbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progresbar.setMessage(message);
		progresbar.setCancelable(true);
		return progresbar;
	}
}
