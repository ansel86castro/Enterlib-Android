package com.enterlib.ioc;

public class ParameterInfo {
    private int parameterIndex;
    private Object parameterValue;

    public ParameterInfo(int parameterIndex, Object parameterValue) {
        this.parameterIndex = parameterIndex;
        this.parameterValue = parameterValue;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public Object getParameterValue() {
        return parameterValue;
    }
}
