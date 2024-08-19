package com.enterlib.parsing.ast;

import com.enterlib.parsing.Token;

public class RecognitionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int Col;
	public int Row;
	public Token Token;

	public RecognitionException(String msg) {
		super(msg);
	}

	public RecognitionException(String string, int col, int row) {
		this(String.format("%s COL:%d, ROW:%d",string, col, row));
		this.Col = col;
		this.Row = row;
	}

	
}
