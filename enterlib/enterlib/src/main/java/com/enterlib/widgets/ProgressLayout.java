package com.enterlib.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.enterlib.R;

public class ProgressLayout extends FrameLayout implements AnimationListener {

	private ProgressBar mProgressBar;
	private TextView mMessage;
	private View view;
	private Animation fadeIn;
	private Animation fadeOut;
	private boolean isShowing;

	public ProgressLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public ProgressLayout(Context context) {
		this(context, null);
	}

	public ProgressLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		if (isInEditMode()) {
			return;
		}

		LayoutInflater inflater = LayoutInflater.from(context);

		view = inflater.inflate(com.enterlib.R.layout.layout_load_frame, null,
				false);

		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
		mMessage = (TextView) view.findViewById(R.id.tvMessage);

		view.setVisibility(GONE);

		fadeIn = AnimationUtils.loadAnimation(getContext(),
				android.R.anim.fade_in);
		fadeOut = AnimationUtils.loadAnimation(getContext(),
				android.R.anim.fade_out);
		fadeOut.setAnimationListener(this);

		fadeIn.setDuration(150);
		fadeOut.setDuration(150);

		addView(view);

		if (isInEditMode()) {
			mMessage.setText(R.string.loading);
		}
	}

	public boolean isShowing() {
		return isShowing;
	}

	public void showProgress() {
		if (isShowing) {
			fadeOut.cancel();
			fadeIn.cancel();
		}

		isShowing = true;
		fadeIn.reset();

		view.startAnimation(fadeIn);
		view.setVisibility(VISIBLE);

		if (getChildCount() > 1) {
			View container = getChildAt(1);
			container.setVisibility(INVISIBLE);
		}
	}

	public void setMessage(String message) {
		mMessage.setText(message);
	}

	public void setMessage(int resId) {
		mMessage.setText(resId);
	}

	public void closeProgress() {
		if (!isShowing) {
			return;
		}

		fadeIn.cancel();

		fadeOut.reset();
		view.startAnimation(fadeOut);
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		view.setVisibility(GONE);

		if (getChildCount() > 1) {
			View container = getChildAt(1);
			container.startAnimation(fadeIn);
			container.setVisibility(VISIBLE);
		}
		isShowing = false;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

}
