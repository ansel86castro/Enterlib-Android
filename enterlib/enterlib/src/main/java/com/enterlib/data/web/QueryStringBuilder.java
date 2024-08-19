package com.enterlib.data.web;

import android.text.TextUtils;

import com.enterlib.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by ansel on 21/09/2016.
 */
public class QueryStringBuilder {
    ArrayList<String> params =new ArrayList<String>();

    public QueryStringBuilder(){

    }

    public QueryStringBuilder(QueryStringBuilder other){
        addAll(other.params);
    }

    public QueryStringBuilder(ArrayList<String>args){
        params.addAll(args);
    }


    public QueryStringBuilder addAll(ArrayList<String>args){
        params.addAll(args);
        return this;
    }

    public QueryStringBuilder addAll(Map<String, String>map){
        Set<Map.Entry<String,String>> entries = map.entrySet();
        for (Map.Entry<String,String> e: entries){
            String key = e.getKey();
            String value = e.getValue();
            if(!TextUtils.isEmpty(value)){
                params.add(key+"="+value);
            }else{
                params.add(key);
            }
        }
        return this;
    }

    public QueryStringBuilder add(String name, String value){
        if(TextUtils.isEmpty(value))
            return this;
        params.add(name + "=" + value);
        return this;
    }

    public QueryStringBuilder addWithEncode(String name, String value){
        if(TextUtils.isEmpty(value))
            return this;
        params.add(name + "=" + encode(value));
        return this;
    }

    public QueryStringBuilder addIf(String name, String value, boolean condition){
        if(condition)
            params.add(name + "=" + value);
        return this;
    }

    public QueryStringBuilder addIf(String name, int value, boolean condition){
        if(condition)
            params.add(name + "=" + String.valueOf(value));
        return this;
    }

    public QueryStringBuilder addIf(String name, double value, boolean condition){
        if(condition)
            params.add(name + "=" + String.valueOf(value));
        return this;
    }


    public QueryStringBuilder addIf(String name, boolean value, boolean condition){
        if(condition)
            params.add(name + "=" + String.valueOf(value));
        return this;
    }

    public QueryStringBuilder add(String name, int value){
        params.add(name + "=" + String.valueOf(value));
        return this;
    }

    public QueryStringBuilder add(String name, double value){
        params.add(name + "=" + String.valueOf(value));
        return this;
    }

    public QueryStringBuilder add(String name, boolean value){
        params.add(name + "=" + String.valueOf(value));
        return this;
    }

    public QueryStringBuilder add(String value){
        if(value == null)
            return this;

        params.add(value);
        return this;
    }

    public void clear(){
        params.clear();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        int count = 0;
        for (String entry : params ) {
            if(StringUtils.isNullOrWhitespace(entry))
                continue;

            if(count > 0)
                sb.append('&');

            sb.append(entry);
            count++;
        }

        if(sb.length() > 0)
            return "?"+sb.toString();
        return "";
    }

    public String toQueryString(){
        StringBuilder sb = new StringBuilder();

        int count = 0;
        for (String entry : params ) {
            if(StringUtils.isNullOrWhitespace(entry))
                continue;

            if(count > 0)
                sb.append('&');

            sb.append(entry);
            count++;
        }
        return  sb.toString();
    }


    public QueryStringBuilder copy(){
        return new QueryStringBuilder(this);
    }

    public static String combineQueryStrings(String a , String b){
        if(StringUtils.isNullOrWhitespace(a))
            return b;
        else if(StringUtils.isNullOrWhitespace(b))
            return a;

        return a+"&"+b;
    }

    public static String combineQueryStrings(String...args){
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < args.length; i++) {
            if(count > 0)
                sb.append('&');

            if(!StringUtils.isNullOrWhitespace(args[i])){
                sb.append(args[i]);
                count++;
            }
        }
        return sb.toString();
    }

    public static String encode(String value){
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String combineEncodedFilter(String filterA, String filterB, String operator){
        if(TextUtils.isEmpty(filterA))
            return filterB;
        else if(TextUtils.isEmpty(filterB))
            return filterA;

        return filterA+"%20"+operator+"%20"+filterB;
    }

    public static String combineEncodedFilterWithAND(String filterA, String filterB){
        return combineEncodedFilter(filterA ,filterB, "and");
    }

    public static String combineEncodedFilterWithOR(String filterA, String filterB){
        return combineEncodedFilter(filterA ,filterB, "or");
    }

}
