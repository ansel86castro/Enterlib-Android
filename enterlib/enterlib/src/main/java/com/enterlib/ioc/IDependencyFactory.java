package com.enterlib.ioc;

import com.enterlib.IServiceProvider;

/**
 * Created by Ansel on 11/3/2017.
 */

public interface IDependencyFactory {
    Object createInstance(IServiceProvider serviceProvider, Class<?> requestType, ParameterInfo[]parameterInfos);
}
