package com.enterlib.parsing;

import com.enterlib.parsing.ast.BinaryExpression;
import com.enterlib.parsing.ast.Expression;
import com.enterlib.parsing.ast.ExpressionList;
import com.enterlib.parsing.ast.ExpressionType;
import com.enterlib.parsing.ast.FunctionCall;
import com.enterlib.parsing.ast.LiteralExpression;
import com.enterlib.parsing.ast.LogicalExpression;
import com.enterlib.parsing.ast.MemberExpression;
import com.enterlib.parsing.ast.RecognitionException;
import com.enterlib.parsing.ast.RelationalExpression;
import com.enterlib.parsing.ast.UnaryExpression;
import com.enterlib.parsing.ast.VariableExpression;

import java.util.ArrayList;

/**
 * Created by Ansel on 1/30/2018.
 */

public class ExpressionParser {
    /*
	 *  or_exp : and_exp (OR and_exp)*
	 *  	   ;
	 *
	 *  and_exp: rel_exp( AND rel_exp)*
	 *  	   ;
	 *
	 *  rel_exp: lvalue (
	 *  	( EQUAL
	 *  	| NEQUAL
	 *  	| LESS
	 *  	| LEQUAL
	 *  	| GREATER
	 *  	| GEQUAL
	 *  	| LIKE ) lvalue )?
	 *
	 *  lvalue: ID ( DOT ID)*
	 *  	| INT
	 *  	| DOUBLE
	 *  	| STRING
	 *  	| NULL
			| LPARENT or_exp RPARENT
			| SUB lvalue
	 * */

    QueryLexicalAnalizer lexicalAnalizer;

    public ExpressionParser(){
        lexicalAnalizer = new QueryLexicalAnalizer();
    }


    public Token match(TokenType token) {
        Token t = lexicalAnalizer.getCurrent();
        if(!t.match(token)){
            throw new RecognitionException("Expecting "+token);
        }
        return lexicalAnalizer.getNextToken();
    }


    public Expression parse(String expression){
        lexicalAnalizer.setValue(expression);
        Expression exp = or_exp();
        if(lexicalAnalizer.getCurrent() != null){
            throw new RecognitionException("Expecting EOF");
        }
        return exp;
    }

    public Expression or_exp(){
        Expression exp = and_expression();

        while(true){
            Token t  =  lexicalAnalizer.getCurrent();
            if (t == null) {
                break;
            }

            if(t.match(TokenType.OR)){
                //throw new RecognitionException("Expecting OR",t.Col, t.Row);
                lexicalAnalizer.getNextToken();
                Expression right = and_expression();
                exp = new LogicalExpression(exp, right, BinaryExpression.OP_OR);
            }else{
                return exp;
            }
        }


        return exp;
    }

    private Expression and_expression() {
        Expression exp = rel_expression();

        while(true){
            Token t  = lexicalAnalizer.getCurrent();
            if (t == null) {
                break;
            }

            if(t.match(TokenType.AND)){
                lexicalAnalizer.getNextToken();
                Expression right = rel_expression();
                exp = new LogicalExpression(exp, right, BinaryExpression.OP_AND);

                //throw new RecognitionException("Expecting AND",t.Col, t.Row);
            }else{
                return exp;
            }
        }


        return exp;
    }

    private Expression rel_expression() {
        Expression exp = aritmetic_exp();

        while (true) {
            Token t = lexicalAnalizer.getCurrent();
            if (t == null) {
                return exp;
            }

            int op = getOperator(t);
            if(     op != BinaryExpression.OP_EQUAL &&
                    op !=BinaryExpression.OP_LESS &&
                    op !=BinaryExpression.OP_GREATHER &&
                    op !=BinaryExpression.OP_GREATHER_EQ &&
                    op !=BinaryExpression.OP_LESS_EQ &&
                    op !=BinaryExpression.OP_DISTINT &&
                    op!=BinaryExpression.OP_LIKE)
                break;

            lexicalAnalizer.getNextToken();
            Expression right = aritmetic_exp();
            exp = new RelationalExpression(exp, right, op);
        }
        return exp;
    }

    private Expression aritmetic_exp(){
        Expression exp = factor_exp();

        while (true) {
            Token t = lexicalAnalizer.getCurrent();
            if (t == null) {
                return exp;
            }

            int op = getOperator(t);
            if(op != BinaryExpression.OP_ADD && op != BinaryExpression.OP_SUB)
                break;

            lexicalAnalizer.getNextToken();
            Expression right = factor_exp();
            exp = new BinaryExpression(exp, right, op);
        }
        return exp;
    }

    private Expression factor_exp(){
        Expression exp = lvalue();

        while (true) {
            Token t = lexicalAnalizer.getCurrent();
            if (t == null) {
                return exp;
            }


            int op = getOperator(t);
            if(op != BinaryExpression.OP_MUL && op != BinaryExpression.OP_DIV)
                break;

            lexicalAnalizer.getNextToken();
            Expression right = lvalue();
            exp = new BinaryExpression(exp, right, op);
        }
        return exp;
    }

    private int getOperator(Token t) {
        int op = 0;
        if(t.match(TokenType.EQUAL)){
            op = BinaryExpression.OP_EQUAL;
        }else if(t.match(TokenType.NEQUAL)){
            op = BinaryExpression.OP_DISTINT;
        }else if(t.match(TokenType.LESS)){
            op = BinaryExpression.OP_LESS;
        }else if(t.match(TokenType.GREATER)){
            op = BinaryExpression.OP_GREATHER;
        }else if(t.match(TokenType.GEQUAL)){
            op = BinaryExpression.OP_GREATHER_EQ;
        }else if(t.match(TokenType.LEQUAL)){
            op = BinaryExpression.OP_LESS_EQ;
        }else if(t.match(TokenType.LIKE)){
            op = BinaryExpression.OP_LIKE;
        }else if(t.match(TokenType.PLUS)){
            op = BinaryExpression.OP_ADD;
        }else if(t.match(TokenType.SUB)){
            op = BinaryExpression.OP_SUB;
        }else if(t.match(TokenType.MUL)){
            op = BinaryExpression.OP_MUL;
        }else if(t.match(TokenType.DIV)){
            op = BinaryExpression.OP_DIV;
        }
        return op;
    }

    private Expression lvalue() {
        Token t = lexicalAnalizer.getCurrent();
        Expression exp=null;

        if(t.match(TokenType.ID)){
            Token idToken = t;
            t = lexicalAnalizer.getNextToken();
            if(t == null){
                exp =  new VariableExpression(idToken.Value);
            }
            else if(t.match(TokenType.DOT)){
                exp =  new VariableExpression(idToken.Value);

                //member exp
                while (true) {
                    if (t == null) return exp;

                    else if (t.match(TokenType.DOT)) {
                        t = lexicalAnalizer.getNextToken();
                        if (t == null || !t.match(TokenType.ID)) {
                            throw new RecognitionException("Expecting ID", t.Col, t.Row);
                        }
                        exp = new MemberExpression(exp, t.Value);
                        t = lexicalAnalizer.getNextToken();
                    } else {
                        return exp;
                    }
                }
            }else if(t.match(TokenType.LPARENT)){
                //funcall exp
                exp = new FunctionCall(idToken.Value, callList());
            }else{
                exp =  new VariableExpression(idToken.Value);
            }
        }else if(t.match(TokenType.DOUBLE)){
            lexicalAnalizer.getNextToken();
            exp =  new LiteralExpression(t.Value, ExpressionType.Double);
        }else if(t.match(TokenType.INT)){
            lexicalAnalizer.getNextToken();
            exp =  new LiteralExpression(t.Value, ExpressionType.Integer);
        }else if(t.match(TokenType.STRING)){
            lexicalAnalizer.getNextToken();
            exp =  new LiteralExpression(t.Value, ExpressionType.String);
        }else if(t.match(TokenType.NULL)){
            lexicalAnalizer.getNextToken();
            exp =  new LiteralExpression(t.Value, ExpressionType.Null);
        }else if(t.match(TokenType.TRUE) || t.match(TokenType.FALSE)){
            lexicalAnalizer.getNextToken();
            exp =  new LiteralExpression(t.Value, ExpressionType.Bool, t.TokenType);
        }else if(t.match(TokenType.LPARENT)){
            lexicalAnalizer.getNextToken();
            exp = or_exp();
            match(TokenType.RPARENT);
        }else if(t.match(TokenType.SUB)){
            lexicalAnalizer.getNextToken();
            Expression valueExp = lvalue();
            exp =  new UnaryExpression(valueExp, BinaryExpression.OP_SUB);
        }else if( t.match(TokenType.LBRACK)) {
            exp = listExpression();
        }
        return exp;

        //throw new RecognitionException("Unspected token "+t.Value, t.Col, t.Row);

    }

    private ExpressionList listExpression() {
        match(TokenType.LBRACK);

        ExpressionList list=new ExpressionList();
        list.add(or_exp());

        while(true){
            Token t  = lexicalAnalizer.getCurrent();
            if (t == null) {
                break;
            }

            if(!t.match(TokenType.SEMICOLON)){
               break;
            }
            lexicalAnalizer.getNextToken();
            list.add(or_exp());
        }

        match(TokenType.RBRACK);
        return list;
    }

   private Expression[] callList(){
       match(TokenType.LPARENT);

       ArrayList<Expression> list=new ArrayList<>();
       list.add(or_exp());

       while(true){
           Token t  = lexicalAnalizer.getCurrent();
           if (t == null) {
               break;
           }
           if(t.match(TokenType.SEMICOLON)){
               lexicalAnalizer.getNextToken();
               list.add(or_exp());
           }else{
               break;
           }
       }

       match(TokenType.RPARENT);
       Expression[]array = new Expression[list.size()];
       list.toArray(array);
       return array;
   }
}
