package com.enterlib.data.web;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;


import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.enterlib.StringUtils;
import com.enterlib.annotations.ColumnMap;
import com.enterlib.annotations.ExternalColumn;
import com.enterlib.converters.DateConverter;
import com.enterlib.data.IEntityContext;
import com.enterlib.data.IEntityCursor;
import com.enterlib.data.IQuerable;
import com.enterlib.data.IRepository;
import com.enterlib.data.PropertyMap;
import com.enterlib.data.IFactory;
import com.enterlib.exceptions.BusinessException;
import com.enterlib.exceptions.ConnectionFailException;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.exceptions.ServerOperationException;
import com.enterlib.exceptions.ValidationException;
import com.enterlib.parsing.OrderByParser;
import com.enterlib.parsing.QueryParser;
import com.enterlib.parsing.ast.ASTContext;
import com.enterlib.parsing.ast.Expression;
import com.enterlib.parsing.ast.ITypeDefinition;
import com.enterlib.parsing.ast.IVariableDefinition;
import com.enterlib.serialization.JSonSerializer;
import com.enterlib.web.HttpProxy;
import com.enterlib.web.ServerError;
import com.enterlib.web.ServerValidationInfo;

public class WebApiClient<T> implements IRepository<T>, ASTContext, IFactory<T>, ITypeDefinition {

    public WebApiClient() {

    }

    public interface OnUrlConnectionListener{

		String getUrl(String url);

		void onConnectionCreated(HttpURLConnection connection);
	}

	private static final String LOG_TAG = WebApiClient.class.getSimpleName();
	
	private boolean isClosed;	
	private String url;
	private Class<T> cls;
	private HashMap<String, PropertyMap> mappings;
	private PropertyMap[] keys;
	private PropertyMap[] filterAllColumns;	
	private DateConverter dateConverter = new DateConverter("yyyy-MM-dd HH:mm:ss");
	private HttpProxy proxy;
	private JSonSerializer serializer;
	private int responseCode;
	private IFactory<T>factory;
	private String baseUrl;
	private String controller;
	private Constructor<T> constructor;
	protected WebApiContext webContext;
	private OnUrlConnectionListener urlConnectionListener;


    private IFactory<T>jsonFactory = new IFactory<T>() {
        @Override
        public T getInstance() {
            try {
                if(factory!=null)
                    return factory.getInstance();
                else if(constructor!=null){
                    return constructor.newInstance(webContext);
                }else{
                    return cls.newInstance();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                throw new InvalidOperationException(e.getMessage(), e);
            }
        }
    };

	protected int maxNbLoadedPages = 10;
	private int pageSize = 30;


	public WebApiClient(WebApiContext webContext, Class<T> cls, String baseUrl, String controller) {
		this.cls = cls;
		this.webContext = webContext;
		this.baseUrl = baseUrl;
		this.controller = controller;
		this.url = baseUrl+ controller + "/";
		this.serializer = new JSonSerializer(){
			@Override
			protected Object createInstance(Class<?> cls) throws InvalidOperationException {
				if(cls == WebApiClient.this.cls){
					return jsonFactory.getInstance();
				}
				Object instance = WebApiClient.this.webContext.newInstance(cls);
				return instance != null ? instance: super.createInstance(cls);
			}
		};
		
		mappings = new HashMap<String, PropertyMap>();
		java.lang.reflect.Field[] typefields = cls.getFields();
		
		ArrayList<PropertyMap>keyList = new ArrayList<PropertyMap>();
		ArrayList<PropertyMap>fastSearchList = new ArrayList<PropertyMap>();
		
		ArrayList<java.lang.reflect.Field>navigationMaps = new ArrayList<java.lang.reflect.Field>();
		
		for (int i = 0; i < typefields.length; i++) {
			java.lang.reflect.Field typeField = typefields[i];
			int modifier = typeField.getModifiers();
			if (Modifier.isStatic(modifier)) {
				continue;
			}
			
			ExternalColumn nav = typeField.getAnnotation(ExternalColumn.class);
			if(nav!=null){
				navigationMaps.add(typeField);
				continue;
			}
			
			ColumnMap dbMap = typeField.getAnnotation(ColumnMap.class);
			if(dbMap == null){				
				continue;
			}

			PropertyMap fieldMap = new PropertyMap(this,typeField, dbMap);
			mappings.put(typeField.getName(), fieldMap);
			
			if(fieldMap.IsKey){
				keyList.add(fieldMap);				
			}
			
			if(fieldMap.IsFastSearch){
				fastSearchList.add(fieldMap);
			}
		}
		
		//link navigation columns to its corresponding foreign keys
		for (java.lang.reflect.Field field : navigationMaps) {
			
			PropertyMap pmap = new PropertyMap(this, field);
			ExternalColumn nav = field.getAnnotation(ExternalColumn.class);
			pmap.NavFKMap = mappings.get(nav.fk());			
			
			mappings.put(field.getName(), pmap);		
			
			if(pmap.IsFastSearch){
				fastSearchList.add(pmap);
			}
		}
				
		
		if(keyList.size() > 0){
			Collections.sort(keyList,new Comparator<PropertyMap>() {

				@Override
				public int compare(PropertyMap lhs, PropertyMap rhs) {
					return  (lhs.Order < rhs.Order)? -1 :
							(lhs.Order > rhs.Order)? 1 : 0;
				}
			});
			
			keys = new PropertyMap[keyList.size()];
			keyList.toArray(keys);
		}
		
		if(fastSearchList.size() > 0){
			filterAllColumns = new PropertyMap[fastSearchList.size()];
			fastSearchList.toArray(filterAllColumns);			
		}	
		
		try {
			constructor = cls.getDeclaredConstructor(IEntityContext.class);
			constructor.setAccessible(true);			
		} catch (NoSuchMethodException e) {
			Log.w(LOG_TAG, "A constructor with  one IEntityContext as  parameter was not found so Lazy evaluation will be disabled");
		}

	}

	public PropertyMap[] getKeys() {
		return keys;
	}

	public Class<T> getCls() {
		return cls;
	}

	public void setUrlConnectionListener(OnUrlConnectionListener listener){
		this.urlConnectionListener = listener;
	}

	public int getMaxNbLoadedPages() {
		return maxNbLoadedPages;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setMaxNbLoadedPages(int maxNbLoadedPages) {
		this.maxNbLoadedPages = maxNbLoadedPages;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	HashMap<String, PropertyMap> getMappings() {
		return mappings;
	}
		
	PropertyMap getMapByFieldName(String fieldName){
		return mappings.get(fieldName);			
	}
	
	public IFactory<T> getFactory() {
		return factory;
	}


	public void setFactory(IFactory<T> factory) {
		this.factory = factory;
	}


	public String getUrl() {
		return url;
	}
	
	
	public String getBaseUrl(){
		return baseUrl;
	}		
	
	public String getController(){
		return controller;
	}

	public WebApiContext getWebContext() {
		return webContext;
	}

	public String getUrl(String controller){
		return baseUrl+"/"+controller+"/";
	}
	
	protected String getRequestUrl(String segments, String query){
		String value = url;
		
		if(segments!=null)
			value+=segments;
		
		if(!TextUtils.isEmpty(query)){
			if(query.charAt(0)!='?')
				query="?"+query;		
			value+=query;
		}
		
		return  value;
	}


	public boolean isClosed(){
		return isClosed;
	}
	
	@Override
	public void close() {
		isClosed = true;
	}
	
	public DateConverter getDateConverter() {
		return dateConverter;
	}

	public void setDateConverter(DateConverter dateConverter) {
		this.dateConverter = dateConverter;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HttpProxy getProxy() {
		return proxy;
	}

	public void setProxy(HttpProxy proxy) {
		this.proxy = proxy;
	}


	public JSonSerializer getSerializer() {
		return serializer;
	}


	public void setSerializer(JSonSerializer serializer) {
		this.serializer = serializer;
	}


	public String buildWhere(String condition){
		if(condition == null || condition.length() == 0)
			return "";
		
		QueryParser parser = new  QueryParser();		
		Expression exp = parser.parse(condition);
				
		exp.checkSemantic(this);
		StringBuilder sb = new StringBuilder();
		exp.genOData(this, sb, 0);
		if(sb.length() > 0){
			try {
				return URLEncoder.encode(sb.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.d(LOG_TAG, e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return "";
	}

	public String buildOrderBy(String expression){
		if(TextUtils.isEmpty(expression))
			return "";

		OrderByParser parser = new  OrderByParser();
		Expression exp = parser.parse(expression);

		exp.checkSemantic(this);
		StringBuilder sb = new StringBuilder();
		exp.genOData(this, sb, 0);
		if(sb.length() > 0){
			try {
				return URLEncoder.encode(sb.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.d(LOG_TAG, e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return "";
	}

	
	protected String getResponseString(HttpURLConnection connection)
			throws InvalidOperationException {

		String responseString;
		try {

			responseCode = connection.getResponseCode();
			String errorMessage = connection.getResponseMessage();

            if(responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(getClass().getSimpleName(), String.format("Status CODE %d Message:%s", responseCode, errorMessage));
            }

            InputStream instream;
            if ( responseCode == HttpURLConnection.HTTP_OK){
                instream = connection.getInputStream();
            }else{
                instream = connection.getErrorStream();
            }

			responseString = StringUtils.readAllText(instream);
			connection.disconnect();

			if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST){
				//server validation exception
				ServerValidationInfo err = (ServerValidationInfo) serializer.deserialize(ServerValidationInfo.class, responseString);
				if(err.ValidationResults == null || err.ValidationResults.length == 0){
					throw new BusinessException(err.ErrorMessage);
				}
				throw new ValidationException(err.toErrorInfo());

			}else if(responseCode < HttpURLConnection.HTTP_OK || responseCode > HttpURLConnection.HTTP_PARTIAL){
				ServerError err = (ServerError) serializer.deserialize(ServerError.class, responseString);
				Log.e(getClass().getName(), err.toString());
				throw new ServerOperationException(err.getErrorMessage());
			}

		} catch (IOException e) {
			responseCode = -1;
			Log.d(LOG_TAG, "Failed to open connection");

			if (connection != null) {
				connection.disconnect();
			}
			throw new ConnectionFailException("Failed to open connection", e);
		}

		return responseString;
	}
	
	private String createWhere(T entity) {		
		StringBuilder where = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			PropertyMap map = keys[i];
			int id =map.getInt(entity);

			if(i > 0)
				where.append("%20and%20");

			where.append(map.getSqlColumn() +"%20eq%20" +String.valueOf(id));
		}
		return where.toString();
	}
	
	@SuppressWarnings("unchecked")
	protected T deserialize(String json){
		return (T)serializer.deserialize(cls, json);
	}
	
	protected ArrayList<T> deserializeList(String json){
		return serializer.deserializeList(cls, json);
	}

	private void setJsonContent(HttpURLConnection post, Object item) {
		String json =serializer.serialize(item);
		byte bytes[]=json.getBytes();
		try {
			post.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			post.setDoOutput(true);
			post.setFixedLengthStreamingMode(bytes.length);

			OutputStream out = new BufferedOutputStream(post.getOutputStream());
			out.write(bytes);
			out.flush();
			out.close();
		} catch (UnsupportedEncodingException e) {
			Log.d(LOG_TAG, e.getMessage(), e);
			throw new InvalidOperationException(e.getMessage(), e);
		} catch (IOException e) {
			post.disconnect();
			throw new ConnectionFailException(e.getMessage(), e);
		}
	}

	public HttpURLConnection createdUrlConnection(String url){
		if(urlConnectionListener!=null) {
			url = urlConnectionListener.getUrl(url);
		}

		URL connUrl;
		try {
			connUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}
		HttpURLConnection urlConnection;
		try {
			if (this.proxy != null) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
						this.proxy.getHost(), this.proxy.getPort()));

					urlConnection = (HttpURLConnection) connUrl.openConnection(proxy);

				String encoded = new String(Base64.encode(new String(this.proxy.getUsername() + ":" + this.proxy.getPassword()).getBytes(), Base64.DEFAULT));
				urlConnection.setRequestProperty("Proxy-Authorization",
						"Basic " + encoded);
			}else{
				urlConnection = (HttpURLConnection) connUrl.openConnection();
			}
		} catch (IOException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}

		urlConnection.setRequestProperty("Accept", "application/json; charset=utf-8");

		Locale defaultLocale = Locale.getDefault();
		String code = defaultLocale.getLanguage().toUpperCase(defaultLocale);
		urlConnection.setRequestProperty("Accept-Language", code);

		if(urlConnectionListener!=null){
			urlConnectionListener.onConnectionCreated(urlConnection);
		}

		return urlConnection;

	}
	
	@SuppressWarnings("unchecked")
	public <TResult> TResult post(Class<TResult>cls, String url, Object item){
		HttpURLConnection urlConnection = createdUrlConnection(url);

		try {
			urlConnection.setRequestMethod("POST");
		} catch (ProtocolException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}

		setJsonContent(urlConnection, item);
		String json = getResponseString(urlConnection);
		return (TResult)serializer.deserialize(cls, json);
	}

	public <TResult> TResult post(Class<TResult>cls, String url, InputStream data){
		HttpURLConnection urlConnection = createdUrlConnection(url);

		try {
			urlConnection.setRequestMethod("POST");
		} catch (ProtocolException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}

		try {
			urlConnection.setRequestProperty("Content-Type", "application/octet-stream");
			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);

			OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

			byte[]buffer = new byte[1024];
			int size;
			while((size = data.read(buffer)) != -1){
				out.write(buffer, 0, size);
			}
			out.flush();

			out.close();
			data.close();
		} catch (UnsupportedEncodingException e) {
			Log.d(LOG_TAG, e.getMessage(), e);
			throw new InvalidOperationException(e.getMessage(), e);
		} catch (IOException e) {
			urlConnection.disconnect();
			throw new ConnectionFailException(e.getMessage(), e);
		}
		String json = getResponseString(urlConnection);
		return (TResult)serializer.deserialize(cls, json);
	}

	public <TResult> TResult post(Class<TResult>cls, String url, byte[] data){
		HttpURLConnection urlConnection = createdUrlConnection(url);

		try {
			urlConnection.setRequestMethod("POST");
		} catch (ProtocolException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}

		try {
			urlConnection.setRequestProperty("Content-Type", "application/octet-stream");
			urlConnection.setDoOutput(true);
			urlConnection.setFixedLengthStreamingMode(data.length);

			OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
			out.write(data);
			out.flush();

			out.close();
		} catch (UnsupportedEncodingException e) {
			Log.d(LOG_TAG, e.getMessage(), e);
			throw new InvalidOperationException(e.getMessage(), e);
		} catch (IOException e) {
			urlConnection.disconnect();
			throw new ConnectionFailException(e.getMessage(), e);
		}
		String json = getResponseString(urlConnection);
		return (TResult)serializer.deserialize(cls, json);
	}
	
	@SuppressWarnings("unchecked")
	public <TResult> TResult put(Class<TResult>cls, String url, Object item){
		HttpURLConnection urlConnection = createdUrlConnection(url);

		try {
			urlConnection.setRequestMethod("PUT");
		} catch (ProtocolException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}

		setJsonContent(urlConnection, item);
		String json = getResponseString(urlConnection);
		return (TResult)serializer.deserialize(cls, json);
	}
	
	@SuppressWarnings("unchecked")
	public <TResult> TResult get(Class<TResult>cls, String url){
		HttpURLConnection urlConnection = createdUrlConnection(url);
		String json = getResponseString(urlConnection);
		return (TResult)serializer.deserialize(cls, json);
	}
	
	@SuppressWarnings("unchecked")
	public <TResult> TResult delete(Class<TResult>cls, String url){
		HttpURLConnection urlConnection = createdUrlConnection(url);
		try {
			urlConnection.setRequestMethod("DELETE");
		} catch (ProtocolException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}
		String json = getResponseString(urlConnection);
		return (TResult)serializer.deserialize(cls, json);
	}


	@Override
	public String getName() {
		return cls.getSimpleName();
	}

	@Override
	public Class<?> getEntityClass() {
		return cls;
	}

	/**Publics*/
	
	@Override
	public IVariableDefinition getVariableDefinition(String name) {	
		return mappings.get(name);
	}

    @Override
    public IVariableDefinition[] geVariableDefinitions() {
        IVariableDefinition[]vars = new IVariableDefinition[mappings.size()];
        mappings.values().toArray(vars);
        return  vars;
    }

    @Override
	public ITypeDefinition getTypeDefinition(String name) {
		String packageName = cls.getPackage().getName();
		return webContext.getClient(packageName + "." + name);
	}

	public int actionCount(String queryString){
        return get(int.class, getRequestUrl("count/", queryString));
	}

	@Override
	public int count(String condition){
		if(condition == null)
			return actionCount(null);
		return actionCount("?filter=" + buildWhere(condition));
	}

    @Nullable
    @Override
    public T get(int id) {
        return get(id, null);
    }

    @Nullable
    @Override
    public T get(int[] ids) {
        return get(ids, null);
    }

    @Override
	public T get(int id, @Nullable String[] includes) {
		String includeStr = null;
		if(includes!=null && includes.length > 0)
			includeStr ="include="+ StringUtils.aggregate(includes, "," ,0 , includes.length);

        HttpURLConnection urlConnection = createdUrlConnection(getRequestUrl("get/"+String.valueOf(id), includeStr));
        String json = getResponseString(urlConnection);
        return deserialize(json);
	}

    @Override
	public T get(int[] ids, @Nullable String[] includes) {
		if(keys == null || keys.length == 0)
			throw new RuntimeException("Key not found");
		
		String filter = "?filter=";
		for (int i = 0; i < keys.length; i++) {
			PropertyMap map = keys[i];			
			if(i > 0){
				filter+="%20and%20";
			}
			filter+= map.getName() +"%20eq%20"+ String.valueOf(ids[i]);
		}

		if(includes!=null && includes.length > 0)
			filter += "&include="+ StringUtils.aggregate(includes, "," ,0 , includes.length);

        HttpURLConnection urlConnection = createdUrlConnection(getRequestUrl("find/", filter));
        String json = getResponseString(urlConnection);
        return deserialize(json);
	}

	@Override
	public boolean create(T item) {
        HttpURLConnection urlConnection = createdUrlConnection(getRequestUrl("post/", null));

        try {
            urlConnection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }

        setJsonContent(urlConnection, item);
        String json = getResponseString(urlConnection);
        if(TextUtils.isEmpty(json))
            return false;

        serializer.deserialize(item, json);
        return true;
	}

	

	@Override
	public boolean update(T item) {
        HttpURLConnection urlConnection = createdUrlConnection(getRequestUrl("put/", null));

        try {
            urlConnection.setRequestMethod("PUT");
        } catch (ProtocolException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }

        setJsonContent(urlConnection, item);
        String json = getResponseString(urlConnection);
        if(TextUtils.isEmpty(json))
            return false;

        return (boolean) serializer.deserialize(boolean.class, json);
	}	

	@Override
	public boolean delete(T item) {
		String filter="?filter="+createWhere(item);
		return delete(int.class, getRequestUrl("delete/",filter)) > 0;
	}

	@Override
	public int delete(@Nullable String expression) {
		String filter="?filter="+buildWhere(expression);
		return delete(int.class, getRequestUrl("delete/", filter));
	}

	@Override
	public IQuerable<T> query(@Nullable String where, @Nullable String orderBy, int skip, int take, @Nullable String[] includes) {
		return new WebApiQuerable<T>(this, buildWhere(where), buildOrderBy(orderBy), take, skip, includes);
	}

	@Override
	public IQuerable<T> query(String where, String orderBy, int skip, int take) {
		return query(where, orderBy, skip , take, null);
	}

	@Override
	public IQuerable<T> query(String where, String orderBy) {
		return query(where, orderBy, -1 , -1, null);
	}

	@Override
	public IQuerable<T> query(String where) {
		return query(where, null, -1 , -1, null);
	}

	@Override
	public IQuerable<T> query() {
		return query(null, null, -1 , -1, null);
	}

	public ArrayList<T> getList(String action, String queryString) {
		HttpURLConnection urlConnection = createdUrlConnection(getRequestUrl(action, queryString));
		String json = getResponseString(urlConnection);
		return deserializeList(json);
	}


	public T getFirst(String queryString) {
		HttpURLConnection urlConnection = createdUrlConnection(getRequestUrl("find/", queryString));
		String json = getResponseString(urlConnection);
		return deserialize(json);
	}

	public IEntityCursor<T> getCursor(String filter, String orderBy, int skip, int take, String include) {
		WebEntityCursor<T> cursor= new WebEntityCursor<T>(this, maxNbLoadedPages, take > 0 ? take: pageSize);
		cursor.setFilter(filter);
		cursor.setOrderBy(orderBy);
		if(include !=null){
			cursor.setIncludes(include);
		}
		cursor.loadPage(0);
		return cursor;
	}


	@Override
	public T getInstance() {
		return jsonFactory.getInstance();
	}

	
	
	
	
}
