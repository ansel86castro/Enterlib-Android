package com.enterlib.converters;

import com.enterlib.StringUtils;
import com.enterlib.exceptions.ConversionFailException;


public class CurrencyConverter implements IValueConverter {

	private String currencySimbol;
	
	public CurrencyConverter() {
		
	}
	
	public CurrencyConverter(String currencySimbol){
		this.currencySimbol = currencySimbol;
	}
	
	@Override
	public Object convert(Object value) throws ConversionFailException {
		if(value == null)
			return null;
		String str =StringUtils.parseCurrency((Double)value);		
		return currencySimbol!=null ? currencySimbol+str : str;		
	}

	@Override
	public Object convertBack(Object value) throws ConversionFailException {
		String str = (String) value;
		if(StringUtils.isNullOrWhitespace(str))
			return null;
		
		if(currencySimbol!=null){
			if(str.startsWith(currencySimbol)){
				str=str.substring(currencySimbol.length());
			}
		}

		return Double.valueOf(str);
	}

	

}
