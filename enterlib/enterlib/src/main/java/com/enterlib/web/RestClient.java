package com.enterlib.web;

import android.util.Log;

import com.enterlib.StringUtils;
import com.enterlib.converters.DateConverter;
import com.enterlib.exceptions.BusinessException;
import com.enterlib.exceptions.ConnectionFailException;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.exceptions.ServerOperationException;
import com.enterlib.exceptions.ValidationException;
import com.enterlib.serialization.JSonSerializer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Map;

public class RestClient  {
	private static final String LOG_TAG = RestClient.class.getSimpleName();

    class InterceptorHandler implements RestClientRequestHandler {

		public RestClientInterceptor interceptor;
		public RestClientRequestHandler next;

		public InterceptorHandler(RestClientInterceptor interceptor, RestClientRequestHandler next) {
			this.interceptor =interceptor;
			this.next = next;
		}

		public HttpURLConnection handleRequest(RestClientRequest request)   {
			return interceptor.handleRequest(request, next);
		}
	}

	private boolean isClosed;
	private DateConverter dateConverter = new DateConverter("yyyy-MM-dd HH:mm:ss");
	private HttpProxy proxy;
	private JSonSerializer serializer;
	private int responseCode;
	private String baseUrl;
	private RestClientRequestHandler _handler;
	private DefaultRequestHandler defaltRequestHandler = new DefaultRequestHandler();

	public RestClient(){
		this(null);
	}

	public RestClient(String baseUrl) {
		this.baseUrl = baseUrl;
		this.serializer = new JSonSerializer();
		_handler = defaltRequestHandler;
	}

	public RestClient addInterceptor(RestClientInterceptor interceptor){
		if(_handler instanceof  InterceptorHandler){
			InterceptorHandler handler = new InterceptorHandler(interceptor, defaltRequestHandler);
			((InterceptorHandler)_handler).next = handler;
		}else{
			_handler = new InterceptorHandler(interceptor, defaltRequestHandler);
		}
		return this;
	}


	public boolean isClosed(){
		return isClosed;
	}

	public void close() {
		isClosed = true;
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


	public <TResult> TResult Invoke(Class<TResult>cls, RestClientRequest request) {
		request.setBaseUrl(baseUrl);
		HttpURLConnection conn = _handler.handleRequest(request);
		String json = getResponseString(conn);
		return (TResult)serializer.deserialize(cls, json);
	}

	public void Invoke(RestClientRequest request){
		request.setBaseUrl(baseUrl);
		HttpURLConnection conn = _handler.handleRequest(request);
		getResponseString(conn);
	}

	@SuppressWarnings("unchecked")
	public void post(String url, Object item){
		RestClientRequest request = new RestClientRequest()
				.setUrl(url)
				.setMethod("POST")
				.setContent(item)
				.setAcceptJson();
		 Invoke(request);
	}

	@SuppressWarnings("unchecked")
	public <TResult> TResult post(Class<TResult>cls, String url, Object item){
		RestClientRequest request = new RestClientRequest()
				.setUrl(url)
				.setMethod("POST")
				.setContent(item)
				.setAcceptJson();

		return Invoke(cls, request);
	}

	public <TResult> TResult post(Class<TResult>cls, String url, InputStream data){
		RestClientRequest request = new RestClientRequest()
				.setUrl(url)
				.setMethod("POST")
				.setContent(data)
				.setAcceptJson();

		return Invoke(cls, request);
	}

	public <TResult> TResult post(Class<TResult>cls, String url, byte[] data){
		RestClientRequest request = new RestClientRequest()
				.setUrl(url)
				.setMethod("POST")
				.setContent(data)
				.setAcceptJson();

		return Invoke(cls, request);
	}
	
	@SuppressWarnings("unchecked")
	public <TResult> TResult put(Class<TResult>cls, String url, Object item){
		RestClientRequest request = new RestClientRequest()
				.setUrl(url)
				.setMethod("PUT")
				.setContent(item)
				.setAcceptJson();

		return Invoke(cls, request);
	}
	
	@SuppressWarnings("unchecked")
	public <TResult> TResult get(Class<TResult>cls, String url , Map<String,String> args){
		RestClientRequest request = new RestClientRequest()
				.setUrl(url)
				.setMethod("GET")
				.setAcceptJson()
				.setParameters(args);

		return Invoke(cls, request);
	}
	
	@SuppressWarnings("unchecked")
	public <TResult> TResult delete(Class<TResult>cls, String url, Map<String,String> args){
		RestClientRequest request = new RestClientRequest()
				.setUrl(url)
				.setMethod("DELETE")
				.setAcceptJson()
				.setParameters(args);

		return Invoke(cls, request);
	}

	public void delete(String url){
		RestClientRequest request = new RestClientRequest()
				.setUrl(url)
				.setMethod("DELETE")
				.setAcceptJson();

		Invoke(request);
	}

	class DefaultRequestHandler implements  RestClientRequestHandler{

		@Override
		public HttpURLConnection handleRequest(RestClientRequest request) {
			HttpURLConnection connection = request.createConnection(proxy, serializer);
			return  connection;
		}
	}

}
