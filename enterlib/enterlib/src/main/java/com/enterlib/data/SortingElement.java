package com.enterlib.data;

import com.enterlib.parsing.ast.OrderByExpression;

import java.util.Comparator;

/**
 * Created by hp on 11/26/2016.
 */
public class SortingElement {
    String expression;
    boolean descending;
    boolean active;
    Comparator<?> comparator;
    private String hint;

    public SortingElement(String hint, String expression, boolean descending) {
        this.expression = expression;
        this.descending = descending;
        this.hint = hint;
    }

    public SortingElement(String hint, String expression, boolean descending, boolean isActive) {
        this.expression = expression;
        this.descending = descending;
        this.hint = hint;
        this.active = isActive;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isDescending() {
        return descending;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Comparator<?> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<?> comparator) {
        this.comparator = comparator;
    }

    @Override
    public String toString() {
        String orderBy = expression;
        if (descending) {
            orderBy += " " + OrderByExpression.DESC;
        }
        return orderBy;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
