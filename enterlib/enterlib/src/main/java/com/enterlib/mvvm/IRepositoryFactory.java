package com.enterlib.mvvm;

import android.app.Activity;
import android.app.Fragment;

public interface IRepositoryFactory {
	Object getInstance(Activity activity, Fragment fragment);
}
