package com.enterlib.mvvm;

import android.content.Intent;
import android.os.Bundle;

public interface INavigator {

	interface INavigationResultCallback {
		void onResult(int requestCode, int resultCode, Intent data);
	}

	void navigateTo(int requestCode, Bundle extras, Object data);
}
