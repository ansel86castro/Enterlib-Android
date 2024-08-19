package com.enterlib.validations.validators;

import com.enterlib.StringUtils;

public class DoubleValidator extends ValueValidator {

	public double MinValue = Double.MIN_VALUE;
	public double MaxValue = Double.MAX_VALUE;

	public DoubleValidator() {
		super();
	}

	public DoubleValidator(String errorMessage) {
		super(errorMessage);
	}

	public DoubleValidator(double minValue, double maxValue) {
		MinValue = minValue;
		MaxValue = maxValue;
	}

	public DoubleValidator(double minValue, double maxValue, String errorMessage) {
		super(errorMessage);
		MinValue = minValue;
		MaxValue = maxValue;
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

				double number = Double.parseDouble(str);
				return number >= MinValue && number <= MaxValue;
			} catch (NumberFormatException e) {
				//Log.d("DoubleValidator", Messages.getString("IntegerValidator.Formato_no_valido") ); //$NON-NLS-1$ //$NON-NLS-2$
				//throw new ConversionFailException(Messages.getString("IntegerValidator.InvalidFormat"),e); //$NON-NLS-1$
				return false;
			}
		} else if (value instanceof Double) {
			double number = (Double) value;
			return number >= MinValue && number <= MaxValue;
		} else {
			//throw new ConversionFailException(Messages.getString("IntegerValidator.StringTypeRequired")); //$NON-NLS-1$
			return false;
		}
	}

}
