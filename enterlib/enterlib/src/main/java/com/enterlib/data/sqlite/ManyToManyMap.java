package com.enterlib.data.sqlite;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.enterlib.data.IQuerable;
import com.enterlib.data.IRepository;
import com.enterlib.data.PropertyMap;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.parsing.ExpressionParser;
import com.enterlib.parsing.ast.Expression;

public class ManyToManyMap<TRelation, TModel> implements IRepository<TModel>, ISQLQueryProvider<TModel> {

	private final boolean disting;
	//model forering key
	private PropertyMap fkey;
    //to one foreign key
	private PropertyMap key;		
	private EntityMap<TRelation> relMap;
	private EntityMap<TModel> modelMap;
	private EntityMapContext mapContext;
	private int id;
	private Class<TRelation> relation;
	private Class<TModel> model;
	private boolean isClosed;
	private BaseVariableDefinition variableDefinition;

	public ManyToManyMap(EntityMapContext context,
						 Class<TRelation> relation,
						 Class<TModel> model,
						 int id,
						 boolean distint) {

		relMap = context.getMap(relation);
		modelMap = context.getMap(model);
		mapContext = context;		
		this.id = id;
		this.relation = relation;
		this.model= model;
		this.disting = distint;
		
		init();
	}


	private void init() {
		String modelName= modelMap.getTableName();
		
		PropertyMap[] keys = relMap.getKeys();
		fkey = null;
		for (int i = 0; i < keys.length; i++) {
			PropertyMap key = keys[i];			
			if(key.IsForeignKey){
				String fkeyTable = key.FKey_table();			
				if(fkeyTable!=null && fkeyTable.equals(modelName)){
					if(key.FKey_To()==null)
						throw new InvalidOperationException("Missing property 'to' in @ForeingKey annotation for "+ relation.getSimpleName());
					fkey = key;
					break;
				}
			}
		}		
		
		if(fkey == null)
			throw new InvalidOperationException("Unable to determine @ForeingKey in "+relation.getSimpleName());		

		for (int i = 0; i < keys.length; i++) {
			if(fkey!=keys[i]){
				key = keys[i];
				break;
			}				
		}

		ExpressionParser parser = new ExpressionParser();
		Expression expression = parser.parse(String.format("$s.%s = %d", relMap.getName(), key.getName(), this.id));

		 variableDefinition = new BaseVariableDefinition(relMap.getName(),
				modelMap, fkey.getSqlColumn(), relMap.getEntityClass(), fkey.getName(),
				expression,
				fkey.getType());
	}

    @Override
    public SQLQuery<TModel> getQuery() {
        SQLQuery<TModel> query = new SQLQuery(modelMap);
		query.setListener(new OnQueryListener() {
			@NonNull
			@Override
			public SQLQuery onQuery(@NonNull SQLQuery query) {
				if (!disting) {
					query.setFrom("\"" + relMap.getTableName() + "\" r0");
					String t0 = query.getTableRef(modelMap.getTableName());
					if (t0 == null)
						t0 = query.createTableRef(modelMap.getTableName());

					query.pushJoint(String.format("INNER JOIN \"%s\" " + t0 + " on r0.%s = " + t0 + ".%s \r\n",
							modelMap.getTableName(),
							fkey.getSqlColumn(),
							fkey.FKey_To()));
					query.addWhere(String.format("r0.%s = %d", key.getSqlColumn(), id));
				}else{
					query.addWhere(String.format("(t0.%s not in (SELECT r0.%s FROM %s r0 WHERE %s))",
							fkey.FKey_To(),
							fkey.getSqlColumn() ,
							relMap.getTableName(),
							String.format("r0.%s = %d", key.getSqlColumn(), id)));
				}
				return query;
			}
		});

		return  query;
    }

	public  boolean isClosed(){
		return isClosed;
	}

	@Override
	public void close() {
		if(!isClosed) {
			modelMap.close();
			relMap.close();
			isClosed = true;
		}
	}



	private int getModelId(Object object) {
		PropertyMap modelKey = modelMap.getMapByFieldName(fkey.getFKeyModelField());
		int modelId = modelKey.getInt(object);
		return modelId;
	}

	@Nullable
	@Override
	public TModel get(int id) {
		return modelMap.get(id);
	}

	@Nullable
    @Override
    public TModel get(int[] ids) {
        return modelMap.get(ids);
    }

    @Nullable
    @Override
    public TModel get(int id, @Nullable String[] includes) {
        return modelMap.get(id, includes);
    }

    @Nullable
    @Override
    public TModel get(int[] ids, @Nullable String[] includes) {
        return modelMap.get(ids, includes);
    }

    @Override
    public IQuerable<TModel> query(@Nullable String where, @Nullable String orderBy, int skip, int take, @Nullable String[] includes) {
        SQLQuery<TModel> q = getQuery();
		q.where(where);
		q.orderBy(orderBy);
        q.setSkip(skip);
        q.setTake(take);
		q.include(includes);
        return q;
    }

	@Override
	public IQuerable<TModel> query(String where, String orderBy, int skip, int take) {
		return query(where, orderBy, skip, take, null);
	}

	@Override
	public IQuerable<TModel> query(String where, String orderBy) {
		return query(where, orderBy, -1, -1, null);
	}

	@Override
	public IQuerable<TModel> query(String where) {
		return query(where, null, -1, -1, null);
	}

	@Override
	public IQuerable<TModel> query() {
		return query(null, null, -1, -1, null);
	}

	@Override
    public int count(@Nullable String expression) {
        SQLQuery<TModel> q = getQuery();
       	q.where(expression);
        return (int)q.count();
    }

    @Override
    public boolean create(TModel item) {
        PropertyMap[] keys = modelMap.getKeys();
        boolean create = false;
        for (int i = 0; i < keys.length; i++) {
            PropertyMap map = keys[i];
            if(map.getInt(item) == 0){
                create = true;
                break;
            }
        }

        if(create){
            modelMap.create(item);
        }

        TRelation rel = relMap.getInstance();
        key.setInt(rel, id);

        PropertyMap modelKey = modelMap.getMapByFieldName(fkey.getFKeyModelField());
        int modelId = modelKey.getInt(item);
        fkey.setInt(rel, modelId);

        return relMap.create(rel);
    }

    @Override
    public boolean update(TModel item) {
        return modelMap.update(item);
    }

    @Override
    public boolean delete(TModel item) {
        int modelId = getModelId(item);
        return relMap.delete(String.format("%s = %d and %s = %d", key.getName(), id, fkey.getName(), modelId)) > 0;
    }

    @Override
    public int delete(@Nullable String expression) {
		SQLQuery<TModel> q = getQuery();
		q.where(expression);
		q.compile(SQLQuery.GEN_NONE);

		q.addColumn("r0."+fkey.getSqlColumn());
		String innerQuery = fkey.getSqlColumn()+" in ("+q.toSql()+")";

		SQLiteDatabase db =this.mapContext.getDatabase(true);
		 int rows = db.delete("\"" + relMap.getTableName() + "\"", innerQuery, null);
        return rows;
    }

    @Override
    public TModel getInstance() {
        return modelMap.getInstance();
    }

}
