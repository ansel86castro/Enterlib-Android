package com.enterlib.mvvm.support;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.enterlib.DialogUtil;
import com.enterlib.IServiceProvider;
import com.enterlib.R;
import com.enterlib.app.UIUtils;
import com.enterlib.exceptions.BusinessException;
import com.enterlib.exceptions.ConnectionFailException;
import com.enterlib.ioc.DependencyContext;
import com.enterlib.mvvm.BaseViewModel;
import com.enterlib.mvvm.IErrorReportService;
import com.enterlib.mvvm.IView;
import com.enterlib.mvvm.IViewModel;
import com.enterlib.widgets.ProgressLayout;

import java.util.List;

public class ActivityView extends AppCompatActivity implements IView{

	private Dialog dialog;
	private boolean isValid;
	private IViewModel viewModel;

	private String loadingMessage;
	private String connectionFailMessage;
	private String notificationDialogTitle;
	private ProgressLayout progressLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadingMessage = getString(R.string.loading);
		connectionFailMessage = getString(R.string.conexion_fallida);
		notificationDialogTitle = getString(R.string.nofificaciones);

	}


    public void setProgressLayout(ProgressLayout progresslayout) {
		this.progressLayout = progresslayout;
	}

	public ProgressLayout getProgressLayout() {
		return progressLayout;
	}

	public String getLoadingMessage() {
		return loadingMessage;
	}

	public void setLoadingMessage(String loadingMessage) {
		this.loadingMessage = loadingMessage;
	}

	public String getConnectionFailMessage() {
		return connectionFailMessage;
	}

	public void setConnectionFailMessage(String connectionFailMessage) {
		this.connectionFailMessage = connectionFailMessage;
	}

	public String getNotificationDialogTitle() {
		return notificationDialogTitle;
	}

	public void setNotificationDialogTitle(String notificationDialogTitle) {
		this.notificationDialogTitle = notificationDialogTitle;
	}


	protected Dialog getProgressDialog() {
		return dialog;
	}

	protected void setProgressDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	public void load() {
		if (viewModel != null) {
			viewModel.load();
		}
	}

	public IViewModel getViewModel() {
		return viewModel;
	}

	protected void setViewModel(BaseViewModel viewModel) {
		this.viewModel = viewModel;
	}

	public void showMessage(String string) {
		UIUtils.showMessage(getApplicationContext(), string);
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Override
	public Context getContext() {
		return this;
	}

	public void setIsValid(boolean value) {
		this.isValid = value;
		if (!value) {
			onAsyncOperationEnd();
		}
	}
	
	@Override
	protected void onStart() {
		setIsValid(true);

		if(viewModel!=null && !viewModel.isLoaded()){
			viewModel.load();
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		setIsValid(false);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if(viewModel!=null && !viewModel.isDestroyed()){
			viewModel.onDestroy();
		}

        super.onDestroy();
	}
	
	@Override
	public void onAsyncOperationBegin() {
		onAsyncOperationBegin(loadingMessage);
	}

	@Override
	public void onAsyncOperationBegin(String message) {
		onAsyncOperationEnd();
		if (progressLayout != null) {
			progressLayout.setMessage(message);
			progressLayout.showProgress();
		} else {
			dialog = DialogUtil.getProgressDialog(this, message);
			dialog.show();
		}

	}

	@Override
	public void onAsyncOperationBegin(int resId) {
		onAsyncOperationBegin(getString(resId));
	}

	@Override
	public void onAsyncOperationEnd() {
		if (progressLayout != null) {
			progressLayout.closeProgress();
		} else {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			dialog = null;
		}
	}

	@Override
	public void onLoadCompleted() {

	}

	@Override
	public void onFailure(Exception workException) {
		if (workException instanceof ConnectionFailException) {
			DialogUtil.showErrorDialog(this, connectionFailMessage);
		} else if (workException instanceof BusinessException) {
			DialogUtil.showErrorDialog(this, workException.getMessage());
		} else {
			logError(workException, null);
		}
	}

	protected void logError(Exception workException, Object data) {
		IErrorReportService reportService = null;
		if(this instanceof IServiceProvider){
			reportService = ((IServiceProvider)this).getService(IErrorReportService.class);
		}else if(DependencyContext.getContext()!=null){
			reportService = DependencyContext.getContext().getService(IErrorReportService.class);
		}
		if (reportService != null) {
			reportService.reportError(this, this, workException, data);
		} else {
			DialogUtil.showErrorDialog(this, workException.getMessage());
		}
	}

	public void showNotifications(List<String> notifications ) {
		if (viewModel == null) {
			return;
		}

		if (notifications.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < notifications.size(); i++) {
				sb.append(notifications.get(i));
				sb.append('\n');
			}
			DialogUtil.showAlertDialog(this, notificationDialogTitle,
					sb.toString(), null);
		}
	}

	@Override
	public void navigateTo(int requestCode, Bundle extras, Object data) {

	}

}
