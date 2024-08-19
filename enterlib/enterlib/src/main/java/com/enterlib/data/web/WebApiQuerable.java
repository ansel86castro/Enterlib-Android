package com.enterlib.data.web;

import android.text.TextUtils;

import com.enterlib.data.IEntityCursor;
import com.enterlib.data.IQuerable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hp on 10/28/2016.
 */
public class WebApiQuerable<T> implements IQuerable<T> {

    WebApiClient<T> client;
    StringBuilder where;
    StringBuilder orderBy;
    StringBuilder includes;
    int take=-1;
    int skip=-1;

    public WebApiQuerable(WebApiClient<T> client, String where, String orderBy, int take, int skip, String[] includes){
        this.client = client;
        if(!TextUtils.isEmpty(where)){
            this.where = new StringBuilder(where);
        }
        if(!TextUtils.isEmpty(orderBy)){
            this.orderBy = new StringBuilder(orderBy);
        }
        this.take = take;
        this.skip = skip;
        if(includes!=null){
            this.includes = new StringBuilder();
            for (int i = 0; i < includes.length; i++) {
                if(i >0){
                    this.includes.append(',');
                }
                this.includes.append(includes[i]);
            }
        }
    }

    public WebApiQuerable(WebApiClient<T> client){
        this.client = client;
    }

    @Override
    public IQuerable<T> where(String expression) {
        String compiled = client.buildWhere(expression);
        if(!TextUtils.isEmpty(compiled)){
            if(where == null) {
                where = new StringBuilder(compiled);
            }else{
                where.append("%20and%20");
                where.append(compiled);
            }
        }
        return this;
    }

    @Override
    public IQuerable<T> where(String expression, Object ... params){
        return where(String.format(expression, params));
    }

    @Override
    public IQuerable<T> orderBy(String expression) {
        String compiled = client.buildOrderBy(expression);
        if(!TextUtils.isEmpty(compiled)){
            if(orderBy == null) {
                orderBy = new StringBuilder(compiled);
            }else{
                try {
                    orderBy.append(URLEncoder.encode(",", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                }
                orderBy.append(compiled);
            }
        }
        return this;
    }

    @Override
    public IQuerable<T> include(String expression) {
       if(TextUtils.isEmpty(expression))
           return  this;
        if(includes == null)
            includes = new StringBuilder(expression);
        else{
            includes.append(',');
            includes.append(expression);
        }
        return this;
    }

    @Override
    public IQuerable<T> include(String[] expressions) {
        if(expressions == null)
            return this;
        if(includes == null)
            includes = new StringBuilder();
        for (int i = 0; i < expressions.length; i++) {
            if(i > 0){
                includes.append(',');
            }
            includes.append(expressions[i]);
        }
        return this;
    }

    @Override
    public IQuerable<T> skip(int value) {
        this.skip = value;
        return this;
    }

    @Override
    public IQuerable<T> take(int value) {
        this.take = value;
        return this;
    }

    @Override
    public String toString() {
        QueryStringBuilder qb = new QueryStringBuilder();
        if(where!=null)
            qb.add("filter", where.toString());
        if(orderBy!=null)
            qb.add("orderby", orderBy.toString());
        if(skip>0)
            qb.add("skip", skip);
        if(take>0)
            qb.add("top", take);
        if(includes!=null)
            qb.add("include", includes.toString());

        return qb.toString();
    }

    @Override
    public ArrayList<T> toList() {
        return client.getList("get/",toString());
    }

    @Override
    public IEntityCursor<T> toCursor() {
        return client.getCursor(
                where!=null?where.toString():null,
                orderBy!=null?orderBy.toString():null,
                skip,
                take,
                includes!=null?includes.toString():null
        );
    }

    @Override
    public T first() {
        return client.getFirst(toString());
    }

    @Override
    public long count() {
        if(where!=null)
            return client.actionCount("?filter="+where.toString());
        else{
            return  client.actionCount(null);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new WebApiIterator<>(toCursor());
    }
}
