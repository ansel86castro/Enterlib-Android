package com.enterlib.filtering;

import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.enterlib.StringUtils;
import com.enterlib.annotations.Filterable;
import com.enterlib.data.QueryHelper;
import com.enterlib.data.sqlite.SQLQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by hp on 10/29/2016.
 */
public class DefaultSearchCondition<T> extends StringFilterCondition<T> {
    private String[]fields;

    public DefaultSearchCondition(String queryHint, String[] fields) {
        super(TextUtils.concat(fields).toString(), queryHint);

        this.fields = fields;
        this.setCombinable(false);
    }

    public DefaultSearchCondition(String queryHint, Class<T>cls){
        super("", queryHint);

        java.lang.reflect.Field[] typefields = cls.getFields();
        ArrayList<Field> filterAllColumns = new ArrayList<>();
        for (int i = 0; i < typefields.length; i++) {
            java.lang.reflect.Field typeField = typefields[i];
            int modifier = typeField.getModifiers();
            if (Modifier.isStatic(modifier)) {
                continue;
            }

            Filterable filterable = typeField.getAnnotation(Filterable.class);
            if(filterable!=null && filterable.isDefault() && typeField.getType() == String.class){
                    filterAllColumns.add(typeField);
            }
        }
        
        fields = new String[filterAllColumns.size()];
        for (int i = 0; i < filterAllColumns.size(); i++) {
            fields[i]=filterAllColumns.get(i).getName();
        }

        queryName = TextUtils.concat(fields).toString();
    }

    @Override
    public String getFilterExpression() {
        if(fields == null || fields.length == 0){
            return  null;
        }

        if(StringUtils.isNullOrWhitespace((String) queryValue))
            return null;

        StringBuilder sb = new StringBuilder();
        final String[] words = ((String) queryValue).split("\\s+");
        final int wordCount = words.length;

        int count = 0;
        for (int i = 0; i < wordCount; i++) {
            if(getStopWordContainer()!=null && getStopWordContainer().constainsWord(words[i]))
                continue;

            String word ="%"+ words[i]+"%";
            if(count > 0)
                sb.append(" AND ");

            sb.append("(");

            for (int j = 0; j < fields.length; j++) {
                String column = fields[i];
                if(j==0){
                    sb.append(column+" LIKE ");
                }else{
                    sb.append(" OR "+column+" LIKE ");
                }

                SQLQuery.appendEscapedSQLString(sb, word);
            }

            sb.append(")");
            count++;
        }
        return sb.toString();
    }
}
