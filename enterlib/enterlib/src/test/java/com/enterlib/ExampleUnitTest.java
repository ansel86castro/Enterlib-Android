package com.enterlib;

import com.enterlib.parsing.ExpressionParser;
import com.enterlib.parsing.ast.EvaluationContext;
import com.enterlib.parsing.ast.Expression;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void eval_numericExpression() throws Exception{
        EvaluationContext context = new EvaluationContext();
        context.setVariable("Amount", 50);
        context.setVariable("Remaining", 10);
        context.setFunction("exp", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return Math.exp(((Number)parameters[0]).doubleValue());
            }
        });
        context.setFunction("format", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return String.format((String)parameters[0], parameters[1]);
            }
        });

        ExpressionParser parser = new ExpressionParser();

        Expression expression =parser.parse("Amount + (0.5*Remaining / exp(Remaining)-1 )");
        Object value = expression.dynamicEval(context);
        Double result = 50 +( 0.5 * 10 / Math.exp(10)-1);
        assertEquals(value, result);
    }

    @Test
    public void eval_formatExpression() throws Exception{
        EvaluationContext context = new EvaluationContext();
        context.setVariable("Name", "Ansel");
        context.setFunction("format", new EvaluationContext.Function() {
            @Override
            public Object invoke(Object[] parameters) {
                return String.format((String)parameters[0], parameters[1]);
            }
        });

        ExpressionParser parser = new ExpressionParser();

        Expression expression =parser.parse("format('Hellow %s .'+2018, Name)");
        Object value = expression.dynamicEval(context);
        assertEquals(value, "Hellow Ansel .2018");
    }


}