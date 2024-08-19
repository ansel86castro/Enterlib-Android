package com.enterlib;

import com.enterlib.ioc.ParameterInfo;

/**
 * Represents a contract that register services ,It notifies the
 * {@link IServicesNotify} when a new service is registered or removed. This
 * contract is used for leverage communication between loosely couple
 * components. Share components are register with this {@link IServiceProvider}
 * so another components can access the service without knowing how published
 * the service
 * */
public interface IServiceProvider {

	/**
	 * Must return an instance of the service implementing the corresponding
	 * serviceType
	 * 
	 * @param cls the registed type of the service
	 * @return The service that was register for the given serviceType
	 */
	<T>  T getService(Class<T>cls);

	<T> T getService(Class<T>cls, ParameterInfo...parameterInfos);
}
