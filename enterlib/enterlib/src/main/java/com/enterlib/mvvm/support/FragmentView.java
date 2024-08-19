package com.enterlib.mvvm.support;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.enterlib.DialogUtil;
import com.enterlib.IServiceProvider;
import com.enterlib.R;
import com.enterlib.exceptions.BusinessException;
import com.enterlib.exceptions.ConnectionFailException;
import com.enterlib.ioc.DependencyContext;
import com.enterlib.mvvm.AttachedListener;
import com.enterlib.mvvm.IErrorReportService;
import com.enterlib.mvvm.IView;
import com.enterlib.mvvm.IViewModel;
import com.enterlib.mvvm.ListViewModel;
import com.enterlib.widgets.ProgressLayout;

import java.util.List;

public class FragmentView extends Fragment implements IView ,AttachedListener{

	private ProgressLayout progressLayout;
	private Dialog dialog;
	private boolean isValid;
	private IViewModel viewModel;
	private String loadingMessage;
	private String connectionFailMessage;
	private String notificationDialogTitle;
	protected boolean onViewCreatedCalled;

	public void setProgressLayout(ProgressLayout progresslayout) {
		this.progressLayout = progresslayout;
	}

	public ProgressLayout getProgressLayout() {
		return progressLayout;
	}

	protected Dialog getProgessDialog() {
		return dialog;
	}

	protected void setProgressDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	public IViewModel getViewModel() {
		return viewModel;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		onViewCreatedCalled =true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (viewModel == null) {
			loadingMessage = getString(R.string.loading);
			connectionFailMessage = getString(R.string.conexion_fallida);
			notificationDialogTitle = getString(R.string.nofificaciones);
			viewModel = createViewModel(savedInstanceState);
		}

		setIsValid(true);
	}

	protected IViewModel createViewModel(Bundle savedInstanceState){
		return null;
	}

	public void showMessage(String string) {
		Activity activity = getActivity();
		Toast toast = Toast.makeText(activity, string, Toast.LENGTH_SHORT);
		toast.show();		
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Override
	public Context getContext() {
		return getActivity();
	}


	public void setIsValid(boolean value) {
		this.isValid = value;
		if (!value) {
			onAsyncOperationEnd();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if(viewModel!=null){
			if(!viewModel.isLoaded())
				viewModel.load();
			else if(onViewCreatedCalled){
				onLoadCompleted();
			}
		}else{
			onLoadCompleted();
		}
	}

	@Override
	public void onLoadCompleted() {
		onViewCreatedCalled = false;
	}

	public void load() {
		if (viewModel != null) {
			viewModel.load();
		}
	}

	
	@Override
	public void onDestroy() {
		setIsValid(false);	
		
		if (viewModel != null && !viewModel.isDestroyed()) {
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
			dialog = DialogUtil.getProgressDialog(getActivity(), message);
			dialog.show();
		}

	}

	@Override
	public void onAsyncOperationBegin(int resId) {

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
	public void onFailure(Exception workException) {
		Activity activity = getActivity();
		if (workException instanceof ConnectionFailException) {
			Log.d(getClass().getSimpleName(), workException.getLocalizedMessage(), workException);
			if (isVisible()) {
				DialogUtil.showErrorDialog(activity, connectionFailMessage);
			}
		} else if (workException instanceof BusinessException) {
			if (isVisible()) {
				((BusinessException) workException).showDialog(activity);
			}
		} else {
			logError(workException, null);
		}
	}

	protected void logError(Exception workException, Object data) {
		IErrorReportService reportService = null;
		if(getActivity() instanceof IServiceProvider){
			reportService = ((IServiceProvider)getActivity()).getService(IErrorReportService.class);
		}else if(DependencyContext.getContext()!=null){
			reportService = DependencyContext.getContext().getService(IErrorReportService.class);
		}

		if (reportService != null) {
			reportService.reportError(getActivity(), this, workException, data);
		} else {
			DialogUtil.showErrorDialog(getActivity(),
                    workException.getMessage());
		}
		
		Log.e(getClass().getSimpleName(), workException.getMessage(),workException);		
	}

	public void showNotifications(List<String> notifications) {
		if (notifications.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < notifications.size(); i++) {
				sb.append(notifications.get(i));
				sb.append("\r\n\r\n");
			}
			DialogUtil.showAlertDialog(getActivity(), notificationDialogTitle,
					sb.toString(), null);
		}
	}

	@Override
	public void navigateTo(int requestCode, Bundle extras, Object data) {

	}
	
	@Override
	public void onAttached(Object item, Exception exception) {
		if(exception !=null)
			return;
		
		getActivity().setResult(Activity.RESULT_OK);
		getActivity().finish();
		
	}
	
	public void attach(Object item){
		if(getViewModel() instanceof ListViewModel){
			((ListViewModel)getViewModel())
				.attach(getString(R.string.linking), item, this);
		}
	}
	
	public void attachList(List<Object>items){
		if(getViewModel() instanceof ListViewModel){
			((ListViewModel)getViewModel())
				.attachList(getString(R.string.linking), items, this);
		}
	}

}