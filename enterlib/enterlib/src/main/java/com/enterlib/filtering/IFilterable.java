package com.enterlib.filtering;

import java.util.ArrayList;

public interface IFilterable {
	void doFilter(ArrayList<FilterCondition> fixedConditions, FilterListener listener);
}