package com.enterlib.app;

/**
 * Defines a contract for chaining and performing filtering This contract is
 * used by the {@link CollectionAdapter.FilterAgreggator} Filter to allow
 * filtering on several conditions.
 * */
public interface IFilterPredicate<T> {

	/**
	 * Evaluates the filtering predicate
	 * 
	 * @param value
	 * @return true if the parameter passes the condition of this
	 *         {@link IFilterPredicate} or false in other case
	 */
	boolean eval(String constraint, IFilterableAdapter adapter, T value);
}
