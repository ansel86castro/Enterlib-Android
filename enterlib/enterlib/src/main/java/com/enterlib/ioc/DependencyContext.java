package com.enterlib.ioc;

import android.util.Log;

import com.enterlib.IClosable;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ansel on 11/3/2017.
 */

public class DependencyContext implements IDependencyContext {

    static class TypeResolveInfo
    {
        public Object singleton;

        public IDependencyFactory factory;

        public LifeType lifeType;

        public boolean isInitialized;

        public TypeResolveInfo(){}

        public TypeResolveInfo(Object singleton, IDependencyFactory factory, LifeType lifeType) {
            this.singleton = singleton;
            this.factory = factory;
            this.lifeType = lifeType;
        }
    }

    static IDependencyContext context;

    HashMap<Class<?>, TypeResolveInfo> typeInfoMap = new HashMap<Class<?>, TypeResolveInfo>();

    HashMap<Class<?>, List<String>> propertyInfoMap = new HashMap<Class<?>, List<String>>();

    HashMap<Class<?>, Object> cache;

    DependencyContext parent;

    private DependencyContext(DependencyContext parent){
        this.parent = parent;
    }

    public DependencyContext(){
        this(null);
    }

    public static IDependencyContext getContext() {
        return context;
    }

    public static void setContext(IDependencyContext context) {
        DependencyContext.context = context;
    }

    public DependencyContext getParent(){
        return  parent;
    }

    public LifeType getLifeType(Class<?> cls)
    {
        TypeResolveInfo info = typeInfoMap.get(cls);
        if(info!=null)
            return info.lifeType;
        return null;
    }

    @Override
    public <T> DependencyContext registerSingleton(Class<T> cls, T singleton){
        TypeResolveInfo info = new TypeResolveInfo(singleton, null, LifeType.Singleton);
        typeInfoMap.put(cls, info);
        return this;
    }

    @Override
    public DependencyContext registerFactory(Class<?> cls, IDependencyFactory factory, LifeType lifeType){
        TypeResolveInfo info =new TypeResolveInfo(null, factory, lifeType);
        typeInfoMap.put(cls, info);
        return this;
    }

    @Override
    public <T> IDependencyContext registerType(Class<T> cls, LifeType lifeType) {
        return registerFactory(cls, new ActivationDependencyFactory(cls), lifeType);
    }

    @Override
    public <I, T extends I> DependencyContext registerTypes(Class<I> contract, Class<T> cls, LifeType lifeType){
         return registerFactory(contract, new ActivationDependencyFactory(cls), lifeType);
    }

    @Override
    public DependencyContext registerInjectableProperty(Class<?> cls, String property){
        List<String>props = propertyInfoMap.get(cls);
        if(props == null){
            props = new ArrayList<>();
            props.add(property);
            propertyInfoMap.put(cls, props);
        }else{
            props.add(property);
        }
        return this;
    }

    public <T> T getService(Class<T>cls){
        return (T)getService(cls, null, null);
    }

    public <T> T getService(Class<T>cls, ParameterInfo...parameterInfos){
        return (T)getService(cls, null, parameterInfos);
    }

    private Object getService(Class<?>cls, DependencyContext childContext, ParameterInfo[]parameterInfos){
        Object service = null;
        LifeType life;

        DependencyContext currentContext = childContext != null ? childContext: this;
        TypeResolveInfo info = typeInfoMap.get(cls);

        if(info != null){
            //cls is registered inside the context
            life = info.lifeType;

            if(info.singleton != null) {
                service = info.singleton;
                if(!info.isInitialized) {
                    injectPropertyDependencies(cls, service, currentContext);
                    info.isInitialized = true;
                }
            }
            else if(life == LifeType.Scope){
                service = currentContext.getFromCache(cls);
                if(service == null && info.factory != null) {
                    service = info.factory.createInstance(currentContext, cls, parameterInfos);
                    currentContext.addToCache(cls, service);
                    injectPropertyDependencies(cls, service, currentContext);
                }
            }else if(info.factory !=null){
                service = info.factory.createInstance(this, cls, parameterInfos);
                injectPropertyDependencies(cls, service, currentContext);
                if(life == LifeType.Singleton){
                    info.singleton = service;
                    info.isInitialized = true;
                }
            }
        } else if(parent != null){
            //cls is not registered in this context so look for it
            //in the parent context
            service = parent.getService(cls, currentContext, parameterInfos);
        } else if(!cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
            //cls is nonabstract class
            life = LifeType.Default;
            service = createInstance(cls, currentContext, parameterInfos);
            injectPropertyDependencies(cls, service, currentContext);
        }
        return  service;
    }

    private void addToCache(Class<?> cls, Object service) {
        if(cache == null)
            cache = new HashMap<>();
         cache.put(cls, service);
    }

    private Object getFromCache(Class<?> cls) {
       if(cache == null)
           return null;
        return cache.get(cls);
    }


    private Object createInstance(Class<?> cls, DependencyContext currentContext, ParameterInfo[]parameterInfos) {
        Object instance;
        Constructor<?>[] constructors = cls.getConstructors();
        if(constructors == null || constructors.length == 0 ){
            try {
                instance = cls.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }else{
            Class<?>[]parameterTypes = constructors[0].getParameterTypes();
            Object[]parameterValues = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                if(!getValueFromParameterInfos(parameterInfos, parameterValues, i)) {
                    parameterValues[i] = currentContext.getService(parameterTypes[i]);
                }
            }
            try {
                instance = constructors[0].newInstance(parameterValues);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return instance;
    }

    private boolean getValueFromParameterInfos(ParameterInfo[] parameterInfos, Object[] parameterValues, int i) {
        if(parameterInfos!=null){
            for (int j = 0; j < parameterInfos.length; j++) {
                if(parameterInfos[j].getParameterIndex() == i){
                    parameterValues[i] = parameterInfos[j].getParameterValue();
                    return true;
                }
            }
        }
        return false;
    }

    private void injectPropertyDependencies(Class<?> cls, Object service, DependencyContext currentContext) {
        if(service instanceof IServiceProviderResolver){
            ((IServiceProviderResolver) service).setServiceProvider(currentContext);
        }

        List<String>props = propertyInfoMap.get(cls);
        if(props == null)
            return;

        for (int i = 0; i < props.size(); i++) {
            String propName = props.get(i);
            try {
                Method method = cls.getMethod(propName);
                Class<?>[]parameters =  method.getParameterTypes();
                Object dependency = currentContext.getService(parameters[0]);
                method.invoke(service, dependency);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public IDependencyContext createScope(){
        return  new DependencyContext(this);
    }

    @Override
    public void dispose(){
        for (TypeResolveInfo info: typeInfoMap.values() ) {
            if(info.singleton != null)
            {
                if(info.singleton instanceof IClosable) {
                    ((IClosable) info.singleton).close();
                }else if(info.singleton instanceof Closeable){
                    try {
                        ((Closeable) info.singleton).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(getClass().getSimpleName(), "dispose: "+e.getLocalizedMessage());
                    }
                }
            }
        }

        typeInfoMap.clear();

        if(cache != null) {
            for (Object obj : cache.values()) {
                if (obj != null) {
                    if (obj instanceof IClosable) {
                        ((IClosable) obj).close();
                    } else if (obj instanceof Closeable) {
                        try {
                            ((Closeable) obj).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            cache.clear();
        }
    }


}
