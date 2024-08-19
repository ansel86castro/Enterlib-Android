package com.enterlib.mvvm;

import android.content.Context;

public interface IErrorReportService {
	void reportError(Context context, IView view,
			Exception exception, Object data);
}
