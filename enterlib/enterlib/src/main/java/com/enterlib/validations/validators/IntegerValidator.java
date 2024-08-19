package com.enterlib.validations.validators;

import com.enterlib.StringUtils;

public class IntegerValidator extends ValueValidator {

	public int MinValue = Integer.MIN_VALUE;
	public int MaxValue = Integer.MAX_VALUE;

	public IntegerValidator(int minValue, int maxValue) {
		MinValue = minValue;
		MaxValue = maxValue;
	}

	public IntegerValidator(int minValue, int maxValue, String errorMessage) {
		super(errorMessage);
		MinValue = minValue;
		MaxValue = maxValue;
	}

	public IntegerValidator() {
		super();

	}

	public IntegerValidator(String errorMessage) {
		super(errorMessage);

	}

	@Override
	public boolean validateValue(Object value) {
		if (value == null) {
			return true;
		}

		if (value instanceof String) {
			String str = (String) value;
			if (StringUtils.isNullOrWhitespace(str)) {
				return true;
			}

			try {

				int number = Integer.decode(str);
				return number >= MinValue && number <= MaxValue;
			} catch (NumberFormatException e) {
				//Log.d("IntegerValidator", Messages.getString("IntegerValidator.Formato_no_valido") ); //$NON-NLS-1$ //$NON-NLS-2$
				//throw new ConversionFailException(Messages.getString("IntegerValidator.InvalidFormat"),e); //$NON-NLS-1$
				return false;
			}
		} else if (value instanceof Integer) {
			int number = (Integer) value;
			return number >= MinValue && number <= MaxValue;
		} else {
			//throw new ConversionFailException(Messages.getString("IntegerValidator.StringTypeRequired")); //$NON-NLS-1$
			return false;
		}
	}

}
