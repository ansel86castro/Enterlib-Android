package com.enterlib.mvvm.support;


import android.app.Activity;

import androidx.fragment.app.Fragment;

public interface IRepositoryFactory {
	Object getInstance(Activity activity, Fragment fragment);
}
