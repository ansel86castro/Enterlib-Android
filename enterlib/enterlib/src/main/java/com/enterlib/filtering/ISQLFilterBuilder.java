package com.enterlib.filtering;

public interface ISQLFilterBuilder {
	String createCondition(FilterCondition c, String sqlColumn, int dbType);
}
