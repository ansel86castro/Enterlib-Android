package com.enterlib.ioc;

import com.enterlib.IServiceProvider;

/**
 * Created by Ansel on 11/3/2017.
 */

public interface IDependencyContext extends IServiceProvider {

    <T> IDependencyContext registerSingleton(Class<T> cls, T singleton);

    IDependencyContext registerFactory(Class<?> cls, IDependencyFactory factory, LifeType lifeType);

    <T> IDependencyContext registerType(Class<T> cls, LifeType lifeType);

    <I, T extends I> IDependencyContext registerTypes(Class<I> contract, Class<T> cls, LifeType lifeType);

    IDependencyContext registerInjectableProperty(Class<?> cls, String property);

    IDependencyContext createScope();

    void dispose();
}
