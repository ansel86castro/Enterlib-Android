package com.enterlib;

/** Defines a Factory patterns. It is used to creates instances */
public interface IFactory {

	/**
	 * Must create a new instance or return a previus created instance in the
	 * case the factory implements the Singleton Pattern
	 * */
	Object create();
}
