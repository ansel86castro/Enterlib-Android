package com.enterlib.validations.validators;

import com.enterlib.StringUtils;

/*
 * A general purpose regular expression value validator
 */
public class RegExValueValidator extends StringValidator {
	private String regExPattern;

	public RegExValueValidator(String inRegExPattern) {
		super("invalid format");
		regExPattern = inRegExPattern;
	}

	public RegExValueValidator(String inRegExPattern, String errorMessage) {
		super(errorMessage);
		regExPattern = inRegExPattern;
	}

	public String getPattern(){
		return regExPattern;
	}
	
	public void setPattern(String pattern){
		regExPattern = pattern;
	}
	
	@Override
	public boolean validateValue(String value) {
		if (StringUtils.isNullOrWhitespace(value) || value.matches(regExPattern)) {
			return true;
		}
		return false;
	}
}
