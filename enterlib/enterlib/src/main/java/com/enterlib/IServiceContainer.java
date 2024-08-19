package com.enterlib;

import com.enterlib.IServiceProvider;

/** Defines a contract that provides an {@link IServiceProvider} */
public interface IServiceContainer {

	IServiceProvider getServiceProvider();

}