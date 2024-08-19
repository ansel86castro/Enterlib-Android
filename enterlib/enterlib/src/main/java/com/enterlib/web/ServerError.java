package com.enterlib.web;

public class ServerError {
	
	public static final int SUCCES_CODE = 200;
	
	public static final int FAILURE_CODE = 500;
	
	public String Message;
	
	public String ExceptionMessage;
	
	public String ExceptionType;
	
	public String StackTrace;

	public String getErrorMessage() {
		if(ExceptionMessage != null)
			return String.format("%s:\n%s", Message, ExceptionMessage);
		return Message;
	}
	
	@Override
	public String toString() {
		String msg = getErrorMessage();
		msg+=String.format("\nExceptionTyp:%s\nStackTrace:%s", ExceptionType, StackTrace);
		return msg;
	} 
	
}
