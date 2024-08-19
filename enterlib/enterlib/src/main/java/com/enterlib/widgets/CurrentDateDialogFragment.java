package com.enterlib.widgets;

import java.util.Calendar;

import android.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CurrentDateDialogFragment extends DialogFragment {

	private DateTimePickerButton parent;

	@SuppressLint("NewApi")
	public static CurrentDateDialogFragment newIntance(
			DateTimePickerButton parent) {
		CurrentDateDialogFragment frag = new CurrentDateDialogFragment();
		frag.parent = parent;

		Bundle args = new Bundle();
		args.putInt("parent", parent.getId());
		frag.setArguments(args);

		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Establecer la fecha actual?")
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								onAccept();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}

	public void onAccept() {
		if (parent == null) {
			int id = getArguments().getInt("parent");
			parent = (DatePickerButton) getActivity().findViewById(id);
		}
		if (parent != null) {
			Calendar c = Calendar.getInstance();
			parent.setDate(c.getTime());
		}
	}
}