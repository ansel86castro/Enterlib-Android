package com.enterlib.parsing.ast;

import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.exceptions.InvalidOperationException;

import java.util.HashMap;

/**
 * Created by Ansel on 1/30/2018.
 */

public class EvaluationContext implements IEvaluationContext {
    public interface Function{
        Object invoke(Object[]parameters);
    }

    HashMap<String, Object>variables = new HashMap<>();
    HashMap<String, Function> functions = new HashMap<>();

    public void stdInit(){
        setFunction("exp", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.exp(((Number)parameters[0]).doubleValue());
            }
        });
        setFunction("abs", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.abs(((Number)parameters[0]).doubleValue());
            }
        });
        setFunction("max", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.max(((Number)parameters[0]).doubleValue(),((Number)parameters[1]).doubleValue());
            }
        });
        setFunction("min", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.max(((Number)parameters[0]).doubleValue(),((Number)parameters[1]).doubleValue());
            }
        });
        setFunction("ifnull", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return parameters[0]!=null?parameters[0]:parameters[1];
            }
        });
        setFunction("format", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return String.format((String)parameters[0], parameters[1]);
            }
        });
        setFunction("rest", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return ((Number)parameters[0]).intValue() % ((Number)parameters[1]).intValue();
            }
        });
        setFunction("pow", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.pow(((Number)parameters[0]) .doubleValue(), ((Number)parameters[1]).intValue());
            }
        });
        setFunction("sqrt", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.sqrt(((Number)parameters[0]).doubleValue());
            }
        });
        setFunction("log10", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.log10(((Number)parameters[0]).doubleValue());
            }
        });
        setFunction("ln", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.log(((Number)parameters[0]).doubleValue());
            }
        });
    }

    @Override
    public Object getValue(String variable) {
        if(!variables.containsKey(variable))
            throw new InvalidOperationException("Variable "+variable+" not defined");
        return variables.get(variable);
    }

    @Override
    public Object invokeFunction(String function, Object[] parameters) {
        if(!functions.containsKey(function))
            throw new InvalidOperationException("Function "+function+" not defined");
        return functions.get(function).invoke(parameters);
    }

    @Override
    public Object getMemberValue(Object target, String member) {
        if(!ReflectionResolver.containsProperty(member, target))
            throw  new InvalidOperationException(String.format("Member %s not found", member));
        return ReflectionResolver.getValue(member, target);
    }

    public void setVariable(String name, Object value){
        variables.put(name, value);
    }

    public void setFunction(String name, Function function){
        functions.put(name, function);
    }
}
