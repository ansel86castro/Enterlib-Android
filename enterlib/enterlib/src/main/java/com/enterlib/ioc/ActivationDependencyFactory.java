package com.enterlib.ioc;

import android.app.Activity;

import com.enterlib.IServiceProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Ansel on 11/3/2017.
 */

public class ActivationDependencyFactory implements IDependencyFactory {

    Class<?>mappedType;
    Constructor<?> constructor;

    public ActivationDependencyFactory(Class<?>type){
        this.mappedType = type;
        Constructor<?>[] constructors = type.getConstructors();
        if(constructors!=null && constructors.length > 0){
            this.constructor = constructors[0];
        }
    }

    @Override
    public Object createInstance(IServiceProvider serviceProvider, Class<?> requestType, ParameterInfo[]parameterInfos) {
        Object instance;
        if(constructor == null){
            try {
                instance = mappedType.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }else{
            Class<?>[]parameterTypes = constructor.getParameterTypes();
            Object[]parameterValues = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                if(!getValueFromParameterInfos(parameterInfos, parameterValues, i)) {
                    parameterValues[i] = serviceProvider.getService(parameterTypes[i]);
                }
            }
            try {
                instance = constructor.newInstance(parameterValues);
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
}
