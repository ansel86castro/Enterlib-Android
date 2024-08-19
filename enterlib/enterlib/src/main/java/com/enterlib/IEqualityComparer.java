package com.enterlib;

/**
 * Defines a functionality to compare two objects. The objects don't have to be
 * of the same type .It is the implementation who decide how the objects are
 * considered equals or represent the same entity
 * */
public interface IEqualityComparer {

	/**
	 * must return true if the objects are considered equals or represents the
	 * same entity
	 */
	boolean equals(Object item, Object value);
}