package com.enterlib.validations;

import android.os.Handler;

/**
 * A Contract for Self-Reporting Entities such as Fields. An IValidator is
 * expected to not only validate but also to reflect the implications of
 * validating that entity, such as changing the state of the validated entity
 * */
public interface IValidator {

	/**
	 * Validates the entity
	 * 
	 * @return true if the validation succeeded
	 * */
	public boolean validate();

	/**
	 * Validates the entity form another {@link Thread}. This must be used when
	 * running several {@link IValidator} from a background Thread.
	 * 
	 * @param handler
	 *            used to post UI updates
	 * @return true if the validation succeeded
	 * */
	public boolean validateAsync(Handler handler);

	/** Returns the error message if the validation fails */
	public String getErrorMessage();
}
