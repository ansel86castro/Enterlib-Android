package com.enterlib.ioc;

import com.enterlib.IServiceProvider;

/**
 * Created by Ansel on 11/3/2017.
 */

public interface IServiceProviderResolver {
    IServiceProvider getServiceProvider();

    void setServiceProvider(IServiceProvider provider);
}
