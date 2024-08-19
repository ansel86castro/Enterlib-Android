package com.enterlib.mvvm;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

/**
 * Defines the contract for a View in a Model-View-ViewModel pattern. It defines
 * a Proxy object that sites between the {@link BaseViewModel} and the Android
 * Components like the {@link Activity} or {@link Fragment} that manages the UI
 */
public interface IView extends INavigator {

	/** Must return true if the view can update its UI */
	boolean isValid();

	Context getContext();

	void onAsyncOperationBegin();

	void onAsyncOperationBegin(String message);

    void onAsyncOperationBegin(int resId);

	void onFailure(Exception exception);

	void onAsyncOperationEnd();

	void onLoadCompleted();

	IViewModel getViewModel();
}
