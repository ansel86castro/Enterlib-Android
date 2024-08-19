package com.enterlib.web;

import com.enterlib.validations.ErrorInfo;

public class ServerValidationInfo{
	
	public String ErrorMessage;
	
	public ServerValidationResult[] ValidationResults;
	
	public ErrorInfo toErrorInfo() {
		ErrorInfo info = new ErrorInfo();
		if(ErrorMessage!=null && ErrorMessage.length() > 0)
			info.add(ErrorMessage);
		
		for (int i = 0; i < ValidationResults.length; i++) {
			ServerValidationResult v = ValidationResults[i];
			info.add(v.Field, v.ErrorMessage);
		}
		return info;
	}
}