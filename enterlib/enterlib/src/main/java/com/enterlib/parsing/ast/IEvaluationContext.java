package com.enterlib.parsing.ast;

/**
 * Created by Ansel on 1/29/2018.
 */

public interface IEvaluationContext {

    Object getValue(String variable);

    Object invokeFunction(String function, Object[]parameters);

    Object getMemberValue(Object target, String member);
}
