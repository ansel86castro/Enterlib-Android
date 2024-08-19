package com.enterlib.mvvm;

import com.enterlib.fields.Form;
import com.enterlib.validations.ErrorInfo;
import com.enterlib.validations.IValidator;

/**
 * This class defines operations that a View in the Model-View-ViewModel pattern
 * must implement in order to be used with a {@link EditViewModel}
 * */
public interface IEditableView extends IFormView {

	boolean onEditBegin();

	boolean onEditEnd(ErrorInfo errorInfo);

}
