package com.enterlib.data.sqlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.enterlib.data.IEntityContext;
import com.enterlib.data.IQuerable;
import com.enterlib.data.IRepository;
import com.enterlib.data.ManyToOneRepository;
import com.enterlib.exceptions.InvalidOperationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class EntityMapContext implements IEntityContext {

	private static final String DB_INITIALIZED = "EM_DB_INITIALIZED";
	private static final String LOG_TAG = EntityMapContext.class.getSimpleName();

    public static final String DB_VERSION = "DB_VERSION";
	private  boolean foreingsKeysEnabled = true;

	private static class DbHelperManager extends SQLiteOpenHelper{

		private boolean isForeingsKeysEnabled;

		public DbHelperManager(Context context, String database, boolean isForeingsKeysEnabled) {
			super(context, database, null, 1);
			this.isForeingsKeysEnabled = isForeingsKeysEnabled;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);

			if(isForeingsKeysEnabled)
				db.execSQL("PRAGMA foreign_keys=ON");
		}
	}
	
	HashMap<Class<?>, WeakReference<EntityMap<?>>> entityMaps= new HashMap<Class<?>, WeakReference<EntityMap<?>>>();
	SQLiteDatabase readableDb;
	SQLiteDatabase writableDb;
	boolean isClosed;
	SQLiteOpenHelper openHelper;
	String databaseName;
	Context context;

	public EntityMapContext(SQLiteOpenHelper openHelper){
		this.openHelper = openHelper;
	}

	public EntityMapContext(Context context, String database){
		this(context, database, true);
	}

	public EntityMapContext(Context context, String database, boolean foreignKeysEnabled){
		this(new DbHelperManager(context, database, foreignKeysEnabled));
		this.foreingsKeysEnabled = foreignKeysEnabled;
		this.databaseName = database;
		this.context = context;
	}

	@Override
	public void close() {
		if(!isClosed){
			
			if(readableDb != null && readableDb.isOpen()){
				readableDb.close();
				readableDb = null;
			}
			
			if(writableDb != null && writableDb.isOpen()){
				writableDb.close();
				writableDb = null;
			}
				
			if(openHelper!=null){
				openHelper.close();
			}

            for (WeakReference<EntityMap<?>>mapRef: entityMaps.values()) {
                EntityMap<?> map = mapRef.get();
                if(map != null){
                    map.close();
                }
            }

            entityMaps.clear();
			isClosed = true;

            onClosed();
		}

	}

	protected void onClosed() {
		
	}

	public boolean isClosed() {
		return isClosed;
	}

	public String getDatabaseName(){
		return databaseName;
	}

	public SQLiteDatabase getDatabase(boolean write) {
		if(isClosed){				
			isClosed =false;
		}
		
		if(write){
			if(writableDb == null || !writableDb.isOpen()){
				writableDb = getWritableDatabase();
			}
			return writableDb;			
		}else{
			if(readableDb == null || !readableDb.isOpen()){
				readableDb = getReadableDatabase();
			}
			return readableDb;			
		}
	}

	protected SQLiteDatabase getWritableDatabase(){
		if(openHelper == null){
			if(databaseName!=null)
				openHelper = new DbHelperManager(context, databaseName , foreingsKeysEnabled);
			else
				throw new RuntimeException("SQLiteOpenHelper not defined");
		}
		return openHelper.getWritableDatabase();
	}
	
	protected SQLiteDatabase getReadableDatabase(){
		if(openHelper == null){
			if(databaseName!=null)
				openHelper = new DbHelperManager(context, databaseName, foreingsKeysEnabled);
			else
				throw new RuntimeException("SQLiteOpenHelper not defined");
		}

		return openHelper.getReadableDatabase();
	}

	@SuppressWarnings("unchecked")
	public <T> EntityMap<T> getMap(Class<T> cls) {
		WeakReference<EntityMap<?>> ref = entityMaps.get(cls);
		EntityMap<T>map = null;

		if(ref != null)
			map = (EntityMap<T>)ref.get();

		if(map == null){
			map = EntityMap.from(cls, this);
			entityMaps.put(cls, new WeakReference<EntityMap<?>>(map));
		}

		return map;
	}

	@Override
	public <T> IRepository<T> getRepository(Class<T> model) {
		return getMap(model);
	}

	@Override
	public <T> IQuerable<T> query(Class<T> model) {
		return getMap(model).query();
	}

	public <T> EntityMap<T> getMap(String className){
		Class<T> cls;
		try {
			cls = (Class<T>) Class.forName(className);
			return getMap(cls);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public static boolean isDatabaseDeployed(Context context, String database){				
		return isDatabaseDeployed(context, database, PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()));								
	}	
	
	public static boolean isDatabaseDeployed(Context context, String database, SharedPreferences preferences){				
		return preferences.getBoolean(DB_INITIALIZED+database, false);								
	}	
	
	private static boolean savePreference(SharedPreferences preferences, String key, Boolean value){
		boolean ok = false;
		if (key != null && !key.equals("")){
			try{
				Editor editor = preferences.edit();
				editor.putBoolean(key, value);
				editor.commit();
				ok = true;
			}catch(Exception exc){
				Log.d("EntityMapContext", exc.getMessage(), exc);
			}
		}
		return ok;
	}

    private static boolean savePreference(SharedPreferences preferences, String key, int value){
        boolean ok = false;
        if (key != null && !key.equals("")){
            try{
                Editor editor = preferences.edit();
                editor.putInt(key, value);
                editor.commit();
                ok = true;
            }catch(Exception exc){
                Log.d("EntityMapContext", exc.getMessage(), exc);
            }
        }
        return ok;
    }
	

	private static File getDatabaseFile(Context context ,String database){
		File databaseFile = context.getDatabasePath(database);
		return databaseFile;
	}

    public static boolean deploy(Context context, String database, int version){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int nb_version =  preferences.getInt(DB_VERSION, -1);
        boolean result = false;

        if(version != nb_version){
             result = deploy(context, database, true);
        }

        if(result){
            savePreference(preferences, DB_VERSION, version);
        }
        return result;
    }

	public static boolean deploy(Context context, String database){
		return deploy(context, database, false);
	}
	
	
	public static boolean deploy(Context context, String database, boolean force){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		if(!force && isDatabaseDeployed(context, database, preferences))
			return false;			
		
		savePreference(preferences, DB_INITIALIZED+database, false);
		
		File databaseFile =  getDatabaseFile(context, database);		
		if(databaseFile.exists()){			
			if(!databaseFile.delete()){
				Log.e(LOG_TAG, "Unable to Update Database to version");
			}	
		}
			
		
		File databaseDir = databaseFile.getParentFile();	
		if(!databaseDir.exists()){
			//make sure directory exist
			databaseDir.mkdirs();
		}
		
		try{
			
			//copy the db from the assets folder into 
			//the databases folder				
			
			InputStream inputStream =  context.getAssets().open(database);			
			OutputStream outputStream = new FileOutputStream(databaseFile);						 
								
			
			//---copy 32K at a time---		
			byte[] buffer = new byte[32768];
			int length;
			try{
				while((length = inputStream.read(buffer)) > 0) {					
					outputStream.write(buffer, 0, length);					
				
				}
				outputStream.flush();
			}finally{
				inputStream.close();
				outputStream.close();								
			}
			
			savePreference(preferences, DB_INITIALIZED+database, true);
			return true;
			
		} catch(FileNotFoundException e) {
			Log.e(LOG_TAG, e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		} catch(IOException e) {
			Log.e(LOG_TAG, e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}	
	}

	public static void exportDatabase (Context context, String database, String directoryName, String exportedDatabaseName) throws FileNotFoundException {

		File databaseFile =  getDatabaseFile(context, database);
		if(!databaseFile.exists()){
			throw new FileNotFoundException(databaseFile.getAbsolutePath());
		}

		try{

			//copy the db from the assets folder into
			//the databases folder
			InputStream inputStream =  new FileInputStream(databaseFile);

			OutputStream outputStream = new FileOutputStream(new File(directoryName,exportedDatabaseName));


			//---copy 32K at a time---
			byte[] buffer = new byte[32768];
			int length;
			try{
				while((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);

				}
				outputStream.flush();
			}finally{
				inputStream.close();
				outputStream.close();
			}

		} catch(IOException e) {
			Log.e(LOG_TAG, e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static void importDatabase(Context context, String database, String filename) throws IOException {
		File databaseFile =  getDatabaseFile(context, database);
		if(databaseFile.exists()){
			if(!databaseFile.delete()){
				Log.e(LOG_TAG, "Unable to Update Database to version");
				throw  new IOException("Unable to Update Database to version");
			}
		}


		File databaseDir = databaseFile.getParentFile();
		if(!databaseDir.exists()){
			//make sure directory exist
			databaseDir.mkdirs();
		}

		try{

			//copy the db from the filename into
			//the databases folder

			InputStream inputStream = new FileInputStream(filename);
			OutputStream outputStream = new FileOutputStream(databaseFile);


			//---copy 32K at a time---
			byte[] buffer = new byte[32768];
			int length;
			try{
				while((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);

				}
				outputStream.flush();
			}finally{
				inputStream.close();
				outputStream.close();
			}

		} catch(FileNotFoundException e) {
			Log.e(LOG_TAG, e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		} catch(IOException e) {
			Log.e(LOG_TAG, e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public <T> T get(Class<T> model, int id) {
		return getMap(model).get(id);
	}

	@Override
	public <T> T get(Class<T> model, int[] ids) {
		return getMap(model).get(ids);
	}
	

	
//	public <T> Collection<T> getEntitySet(Class<T> model, String filter) {
//		EntityMap<T>map = getMap(model);			
//		return new EntitySet<T>(model, map.getCursor(null, filter!=null?map.buildWhere(filter):null, null, null), this);
//	}
//	
//
//	
//	public <TRelation, TModel> Collection<TModel> getEntitySet(Class<TRelation> relation, Class<TModel> model, int fkeyId) {
//		EntityMap<TRelation>relMap = getMap(relation);
//		EntityMap<TModel>modelMap = getMap(model);
//		
//		String modelName= modelMap.getTableName();
//		
//		PropertyMap[] keys = relMap.getKeys();
//		PropertyMap fkey = null;		
//		for (int i = 0; i < keys.length; i++) {
//			PropertyMap key = keys[i];			
//			if(key.IsForeignKey){
//				String fkeyTable = key.FKey_table();			
//				if(fkeyTable!=null && fkeyTable.equals(modelName)){
//					if(fkey.FKey_To()==null)
//						throw new InvalidOperationException("Missing property 'to' in @ForeingKey annotation for "+ relation.getSimpleName());
//					fkey = key;
//					break;
//				}else{
//					
//				}
//			}
//		}
//		
//		if(fkey == null)
//			throw new InvalidOperationException("Unable to determine @ForeingKey in "+relation.getSimpleName());
//		
//		String select =String.format("SELECT related.* FROM \"%s\" rel INNER JOIN ( %s ) related on rel.%s=related.%s", 
//				relMap.getTableName(),
//				modelMap.createSelect(),
//				fkey.getSqlColumn(),
//				fkey.FKey_To());
//		
//		
//		IEntityCursor<TModel> cusor = modelMap.getCursor(select, null, null, null);		
//		return null;
//	}

	@Override
	public <T> boolean create(Class<T> model, T item) {
		return getMap(model).create(item);		
	}

	@Override
	public <T> boolean update(Class<T> model, T item) {
		return getMap(model).update(item);
	}

	@Override
	public <T> boolean delete(Class<T> model, T item) {
		return getMap(model).delete(item);
	}

	@Override
	public <T> int delete(Class<T> model, String expression) {
		return getMap(model).delete(expression);
	}

	@Override
	public <T> IRepository<T> getRepository(Class<T> model, String fkeyName, int fkeyValue ,boolean distint) {
		EntityMap<T>map = getMap(model);
		return new ManyToOneRepository(map,map.getMapByFieldName(fkeyName), fkeyValue, map.getKeys(), distint);
	}

	@Override
	public <T> IRepository<T> getRepository(IRepository<T> repository, Class<T> model, String fkeyName, int fkeyValue, boolean distint) {
		EntityMap<T>map = getMap(model);
		return new ManyToOneRepository(repository, map.getMapByFieldName(fkeyName), fkeyValue, map.getKeys(), distint);
	}

	@Override
	public <TRel, TModel> IRepository<TModel> getRepository(Class<TRel> relation, Class<TModel> model, int linking_id, boolean distint) {
		return new ManyToManyMap<TRel, TModel>(this, relation, model, linking_id, distint);
	}

	@Override
	public <T> T newInstance(Class<T> model) {
		return getMap(model).getInstance();
	}
		
}
