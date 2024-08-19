package com.enterlib.mvvm;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.enterlib.R;
import com.enterlib.databinding.BindingResources;
import com.enterlib.exceptions.ValidationException;
import com.enterlib.fields.Form;
import com.enterlib.validations.ErrorInfo;

/**
 * Created by hp on 10/29/2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FormActivityView extends ActivityView implements IFormView, IEditableView {
    protected Form form;
    private BindingResources res;

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
            form.showErrorDialog(this, getString(R.string.validation_error));
            return false;
        }

        form.updateSource();
        return true;
    }

    public void onFailure(Exception exception) {
        if (exception instanceof ValidationException) {
            ErrorInfo info = ((ValidationException) exception).getError();
            if (info != null && info.containsErrors()) {
                form.setFieldErrors(info);
                form.showErrorDialog(this,
                        getString(R.string.validation_fail_message));
            }
        }else{
            super.onFailure(exception);
        }
    }

    public Form createForm() {
        IViewModel viewModel = getViewModel();
        return Form.build(getBindingResources(), getWindow().getDecorView(), viewModel != null ? viewModel : this);
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
            setResult(Activity.RESULT_OK);
            if(viewModel.isDestroyed()){
                finish();
            }
            return true;
        } else {
            form.setFieldErrors(info);
            form.showErrorDialog(this, getString(R.string.validation_error));
            return  false;
        }
    }
}
