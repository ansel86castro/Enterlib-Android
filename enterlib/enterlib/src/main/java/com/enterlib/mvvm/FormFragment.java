package com.enterlib.mvvm;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.enterlib.R;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.INotifyPropertyChanged;
import com.enterlib.databinding.IPropertyChangedListener;
import com.enterlib.exceptions.ValidationException;
import com.enterlib.fields.Field;
import com.enterlib.fields.Form;
import com.enterlib.validations.ErrorInfo;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FormFragment extends FragmentView implements INotifyPropertyChanged, IFormView ,IEditableView{

	public static final String READ_ONLY_FIELDS = "READ_ONLY_FIELDS";

	private ArrayList<IPropertyChangedListener> listeners;
	protected Form form;
	private BindingResources res;


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if(getViewModel() != null && getViewModel().isLoaded()){

			if(form!=null){
				form.bindView(view);
			}

			onLoadCompleted();
		}
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangedListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener can not be null");
		}

		if (listeners == null) {
			listeners = new ArrayList<IPropertyChangedListener>();
		}

		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);

	}

	@Override
	public boolean removePropertyChangeListener(
			IPropertyChangedListener listener) {
		if (listeners == null) {
			return false;
		}
		return listeners.remove(listener);
	}

	protected void onPropertyChange(String propName) {
		if (listeners == null) {
			return;
		}

		int length = listeners.size();
		for (int i = 0; i < length; i++) {
			listeners.get(i).onPropertyChange(this, propName);
		}
	}

	@Override
	public Form getForm() {
		return form;
	}

	public void setForm(Form value) {
		form = value;
	}

	@Override
	public boolean validate() {
		if (form == null) {
			return false;
		}

		if (!form.validate()) {
			form.showErrorDialog(getActivity(), getString(R.string.validation_error));
			return false;
		}

		form.updateSource();
		return true;
	}

	public void onFailure(Exception exception) {
		if (exception instanceof ValidationException) {
			Activity activity = getActivity();
			ErrorInfo info = ((ValidationException) exception).getError();
			if (info != null && info.containsErrors()) {
				form.setFieldErrors(info);
				form.showErrorDialog(activity,
						getString(R.string.validation_fail_message));
			}
		}else{
			super.onFailure(exception);
		}
	}

	public Form createForm() {
        IViewModel viewModel = getViewModel();
		return Form.build(getBindingResources(), getView(), viewModel != null ? viewModel : this);
	}

	protected BindingResources getBindingResources() {
		res = new BindingResources();
	   return res;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (form != null) {
			form.saveState(outState);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (form == null) {
			form = createForm();
		} else if(onViewCreatedCalled) {
			form.bindView(getView());
		}

		if (savedInstanceState != null && form != null) {
			form.restoreState(savedInstanceState);
		}

		if(form!=null) {
			String[] readonlyFields = getActivity().getIntent().getStringArrayExtra(READ_ONLY_FIELDS);
			if (readonlyFields != null && form != null) {
				for (int i = 0; i < readonlyFields.length; i++) {
					Field field = form.getFieldByBinding(readonlyFields[i]);
					if (field != null)
						field.setEnabled(false);
				}
			}
		}
	}
	
	public void updateTargets() {
		if (form == null) {
			return;
		}

        try {
            form.updateTargets();
        }catch (Exception e) {
            Log.d(getClass().getName(), e.getMessage(), e);
            logError(e, "Binding Error");
        }
	}

	public void updateTargets(Object viewModel) {
		if (form == null) {
			return;
		}

        try {
		    form.updateTargets(viewModel);
        }catch (Exception e) {
            Log.d(getClass().getName(), e.getMessage(), e);
            logError(e, "Binding Error");
        }
	}

	public void updateSources() {
		if (form == null) {
			return;
		}

        try{
		    form.updateSource();
        }catch (Exception e) {
            Log.d(getClass().getName(), e.getMessage(), e);
            logError(e, "Binding Error");
        }
	}

	public void updateSources(Object viewModel) {
		if (form == null) {
			return;
		}
        try{
		    form.updateSource(viewModel);
        }catch (Exception e) {
            Log.d(getClass().getName(), e.getMessage(), e);
            logError(e, "Binding Error");
        }
	}

    @Override
    public void onLoadCompleted() {
        super.onLoadCompleted();
        updateTargets();
    }


    @Override
    public boolean onEditBegin() {
        return false;
    }

    @Override
    public boolean onEditEnd(ErrorInfo info) {
        if (info == null || !info.containsErrors()) {
            IViewModel viewModel =getViewModel();
			getActivity().setResult(Activity.RESULT_OK);
            if(viewModel.isDestroyed()){
                getActivity().finish();
            }
			return true;
        } else {
            form.setFieldErrors(info);
            form.showErrorDialog(getActivity(), getString(R.string.validation_error));
			return false;
        }
    }
}
