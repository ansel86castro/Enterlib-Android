package com.enterlib.web;

import android.renderscript.ScriptGroup;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.enterlib.exceptions.ConnectionFailException;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.serialization.JSonSerializer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RestClientRequest {
    private static final String LOG_TAG = "RestClientRequest";

    private String _baseUrl;
    private String _url;
    private Map<String, String> _headers = new HashMap<>();
    private Object _content;
    private String method = "GET";
    private Map<String,String> _args = new HashMap<>();

    public String getBaseUrl(){
        return _baseUrl;
    }

    public RestClientRequest setBaseUrl(String value){
        _baseUrl = value;
        return this;
    }

    public String getUrl() {
        if(_baseUrl != null)
            return _baseUrl+_url;

        return _url;
    }

    public RestClientRequest setUrl(String url) {
        this._url = url;
        return  this;
    }

    public Object getContent() {
        return _content;
    }

    public RestClientRequest setContent(Object content) {
        if(content instanceof Authorizable){
            setHeader("Authorization", "");
        }

        this._content = content;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RestClientRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public RestClientRequest authorized(){
        setHeader("Authorization", "");
        return this;
    }

    public RestClientRequest authorized(String token){
        setHeader("Authorization", "Bearer "+token);
        return this;
    }

    public  Map<String, String> getHeaders(){
        return _headers;
    }

    public String getHeader(String header){
        return _headers.get(header);
    }

    public RestClientRequest setHeader(String header, String value){
        _headers.put(header, value);
        return this;
    }

    public RestClientRequest setParameter(String key, String value){
        _args.put(key, value);
        return this;
    }

    public RestClientRequest setParameters(Map<String, String>values){
        if(values != null) {
            _args.putAll(values);
        }
        return this;
    }

    protected String getRequestUrl(String segments, String query){
        String value = segments;

        if(!TextUtils.isEmpty(query)){
            if(query.charAt(0)!='?')
                query="?"+query;
            value+=query;
        }

        return  value;
    }

    public RestClientRequest SetDefaultLocale(){
        Locale defaultLocale = Locale.getDefault();
        String code = defaultLocale.getLanguage().toUpperCase(defaultLocale);
        return setHeader("Accept-Language", code);
    }

    public RestClientRequest setAcceptJson(){
        return setHeader("Accept", "application/json");
    }

//    public RestClientRequest setContenTypeJson(){
//        return  setHeader("Content-Type", "application/json");
//    }

    public HttpURLConnection createConnection(HttpProxy httpProxy, JSonSerializer serializer){
        try {
            AddArguments();
        } catch (UnsupportedEncodingException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }

        URL connUrl;
        try {
            connUrl = new URL(getUrl());
        } catch (MalformedURLException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }

        HttpURLConnection urlConnection;
        try {
            if (httpProxy != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxy.getHost(), httpProxy.getPort()));

                urlConnection = (HttpURLConnection) connUrl.openConnection(proxy);

                String encoded = new String(Base64.encode(new String(httpProxy.getUsername() + ":" + httpProxy.getPassword()).getBytes(), Base64.DEFAULT));
                urlConnection.setRequestProperty("Proxy-Authorization",  "Basic " + encoded);
            }else{
                urlConnection = (HttpURLConnection) connUrl.openConnection();
            }
        } catch (IOException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }

        try {
            urlConnection.setRequestMethod(method);
        } catch (ProtocolException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }

        for (Map.Entry<String, String> entry : _headers.entrySet()) {
            urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        if(method == "POST" || method == "PUT" && _content != null)
        {
            if(_content instanceof InputStream){
                setBinaryContent(urlConnection, (InputStream) _content);
            }else  if(_content instanceof  byte[]){
                setBinaryContent(urlConnection, (byte[]) _content);
            }else {
                setJsonContent(urlConnection, serializer);
            }
        }

        return urlConnection;
    }

    private void AddArguments() throws UnsupportedEncodingException {
        if(_args.size() > 0) {
            StringBuilder sb = new StringBuilder(_url);
            sb.append("?");
            int i = 0;
            for (Map.Entry<String, String> entry : _args.entrySet()) {
                if (i > 0)
                    sb.append('&');

                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                sb.append('=');
                sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                i++;
            }

            _url=sb.toString();
        }

    }

    private void setJsonContent(HttpURLConnection conn , JSonSerializer serializer) {
        String json =serializer.serialize(_content);
        byte bytes[]= json.getBytes();
        try {
           conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(bytes.length);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            out.write(bytes);
            out.flush();
            out.close();
        } catch (UnsupportedEncodingException e) {
            Log.d(LOG_TAG, e.getMessage(), e);
            throw new InvalidOperationException(e.getMessage(), e);
        } catch (IOException e) {
            conn.disconnect();
            throw new ConnectionFailException(e.getMessage(), e);
        }
    }

    private void setBinaryContent(HttpURLConnection urlConnection, InputStream data){
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
    }

    private void setBinaryContent(HttpURLConnection urlConnection, byte[] data){
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
    }

    public static RestClientRequest create(String url, String method, Object content){
        return new RestClientRequest()
                .setUrl(url)
                .setMethod(method)
                .setContent(content)
                .setAcceptJson();
    }

    public static RestClientRequest get(String url){
        return create(url, "GET", null);
    }

    public static RestClientRequest post(String url, Object content){
        return create(url, "POST", content);
    }

    public static RestClientRequest put(String url, Object content){
        return create(url, "PUT", content);
    }

    public static RestClientRequest delete(String url){
        return create(url, "DELETE", null);
    }
}
