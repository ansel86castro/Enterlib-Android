package com.enterlib.data;

import android.database.DatabaseUtils;

import com.enterlib.StringUtils;
import com.enterlib.annotations.Filterable;
import com.enterlib.data.sqlite.SQLQuery;
import com.enterlib.filtering.FilterOperators;
import com.enterlib.filtering.IStopWordContainer;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

public final class QueryHelper {

	public static String createStringQuery(String column, String value, int operator, IStopWordContainer swRepository) {
		if(StringUtils.isNullOrWhitespace(value))
			return null;
		
		StringBuilder sb = new StringBuilder();
		String operatorString = getOpString(operator);
		final String[] words = value.split("\\s+");
		final int wordCount = words.length;
						
		int count = 0;
		for (int j = 0; j < wordCount; j++) {		
			String word = words[j];
			if(swRepository!=null && swRepository.constainsWord(word)){
				continue;
			}						
			
			if(operator == FilterOperators.LIKE){
				if(count==0){					
					sb.append("("+column+" LIKE ");					
				}else{
					sb.append(" AND ("+column+" LIKE ");
				}				
				SQLQuery.appendEscapedSQLString(sb, "%"+word+"%");
			}else{
				
				if(count==0){					
					sb.append('(');
					sb.append(column);
					sb.append(' ');
					sb.append(operatorString);
				}else{					
					sb.append(" OR (");
					sb.append(column);
					sb.append(' ');
					sb.append(operatorString);															
				}

				SQLQuery.appendEscapedSQLString(sb, word);
				
			}
			sb.append(")");
			
			count++;
		}
		return sb.toString();
	}

	public static String createDefaultStringQuery(Class<?>cls ,  String value ,IStopWordContainer swRepository) {

		java.lang.reflect.Field[] typefields = cls.getFields();

		if(StringUtils.isNullOrWhitespace(value))
			return null;

		StringBuilder sb = new StringBuilder();

		final String[] words = value.split("\\s+");
		final int wordCount = words.length;

		ArrayList<java.lang.reflect.Field> filterAllColumns = new ArrayList<>();
		for (int i = 0; i < typefields.length; i++) {
			java.lang.reflect.Field typeField = typefields[i];
			int modifier = typeField.getModifiers();
			if (Modifier.isStatic(modifier)) {
				continue;
			}

			Filterable filterable = typeField.getAnnotation(Filterable.class);
			if(filterable!=null && filterable.isDefault()){
				if(typeField.getType() != String.class){
					throw new RuntimeException("Only strings are allowed in the default search for "+typeField.getName());
				}

				filterAllColumns.add(typeField);
			}
		}


		int count = 0;
		for (int i = 0; i < wordCount; i++) {
			if(swRepository!=null && swRepository.constainsWord(words[i]))
				continue;

			String word ="%"+ words[i]+"%";
			if(count > 0)
				sb.append(" AND ");

			sb.append("(");

			for (int j = 0; j < filterAllColumns.size(); j++) {
				java.lang.reflect.Field map = filterAllColumns.get(j);
				String column = map.getName();
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

	public static String getOpString(int op){
		switch (op) {
		case FilterOperators.EQUALS: return "=";
		case FilterOperators.LESS: return "<";
		case FilterOperators.GREATHER: return ">";
		case FilterOperators.LESS_EQUALS: return "<=";
		case FilterOperators.GREATHER_EQUALS: return ">=";
		case FilterOperators.LIKE: return "LIKE";
		case FilterOperators.NOT_EQUALS: return "!=";
		default:
			return "";
		}
	}
	
	public static String scapeSQLString(String value){
		return DatabaseUtils.sqlEscapeString(value);
	}
	
	public static String combine(String whereA, String whereB){
		if(whereA == null || whereA.length() == 0)
			return whereB;
		if(whereB == null || whereB.length() == 0)
			return whereA;
		return  whereA +" AND "+whereB;
	}

	public static String combineOrderBy(String orderByA, String orderByB){
		return combineOrderBy(orderByA, orderByB, ",");
	}

	public static String combineOrderBy(String orderByA, String orderByB, String simbol){
		if(orderByA == null || orderByA.length() == 0)
			return orderByB;
		if(orderByB == null || orderByB.length() == 0)
			return orderByA;
		return  orderByA +simbol+orderByB;
	}
}
