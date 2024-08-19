package com.enterlib.filtering;

import com.enterlib.IClosable;

public interface IStopWordContainer extends IClosable {
	
	boolean constainsWord(String word);
		
}
