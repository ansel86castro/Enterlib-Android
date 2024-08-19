package com.enterlib.widgets;

import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.enterlib.R;

public class ViewErrorController {
	ErrorPopup mErrorPopup;
	CharSequence error;
	View view;
	View.OnClickListener onClickListener;

	public ViewErrorController(View view) {
		this.view = view;
	}

	public void showError(CharSequence error) {
		this.error = error;
		if (error == null || error.length() == 0) {
			hideError();
		} else {
			showError();
		}
	}

	private void showError() {

		Drawable dr = view.getContext().getResources()
				.getDrawable(R.drawable.indicator_input_error);
		dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
		// view.setCompoundDrawables(null, null, dr, null);

		if (mErrorPopup == null) {
			LayoutInflater inflater = LayoutInflater.from(view.getContext());
			final TextView err = (TextView) inflater.inflate(
					R.layout.textview_hint, null);
			err.setOnClickListener(onClickListener);

			final float scale = view.getResources().getDisplayMetrics().density;
			mErrorPopup = new ErrorPopup(err, (int) (200 * scale + 0.5f),
					(int) (50 * scale + 0.5f));
			mErrorPopup.setFocusable(false);
			// The user is entering text, so the input method is needed. We
			// don't want the popup to be displayed on top of it.
			mErrorPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		}

		TextView tv = (TextView) mErrorPopup.getContentView();
		chooseSize(mErrorPopup, error, tv);
		tv.setText(error);

		mErrorPopup.showAsDropDown(view, view.getWidth(), 0);
		mErrorPopup.fixDirection(mErrorPopup.isAboveAnchor());
	}

	public View.OnClickListener getOnClickListener() {
		return onClickListener;
	}

	public void setOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
		int wid = tv.getPaddingLeft() + tv.getPaddingRight();
		int ht = tv.getPaddingTop() + tv.getPaddingBottom();

		int defaultWidthInPixels = view.getResources().getDimensionPixelSize(
				R.dimen.textview_error_popup_default_width);
		Layout l = new StaticLayout(text, tv.getPaint(), defaultWidthInPixels,
				Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
		float max = 0;
		for (int i = 0; i < l.getLineCount(); i++) {
			max = Math.max(max, l.getLineWidth(i));
		}

		/*
		 * Now set the popup size to be big enough for the text plus the border
		 * capped to DEFAULT_MAX_POPUP_WIDTH
		 */
		pop.setWidth(wid + (int) Math.ceil(max));
		pop.setHeight(ht + l.getHeight());
	}

	private void hideError() {
		if (mErrorPopup != null) {
			if (mErrorPopup.isShowing()) {
				mErrorPopup.dismiss();
			}
		}
	}

}
