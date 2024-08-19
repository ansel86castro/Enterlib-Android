package com.enterlib.data.web;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.enterlib.StringUtils;
import com.enterlib.exceptions.ConnectionFailException;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.exceptions.ServerOperationException;
import com.enterlib.serialization.JSonSerializer;
import com.enterlib.web.HttpProxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by hp on 10/29/2016.
 */
public class WebTools {

    private static final String LOG_TAG = WebTools.class.getSimpleName();

    public static Bitmap downloadImage(String urlString) throws InvalidOperationException {
        return downloadImage(urlString, null, null);
    }

    public static Bitmap downloadImage(String urlString,  NameValuePair[]headers) throws InvalidOperationException {
        return downloadImage(urlString, null, headers);
    }

    public static Bitmap downloadImage(String urlString, HttpProxy httpProxy, NameValuePair[]headers)
            throws InvalidOperationException {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            if (httpProxy != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                        httpProxy.getHost(), httpProxy.getPort()));
                urlConnection = (HttpURLConnection) url.openConnection(proxy);
                String encoded = new String(Base64.encode(
                        new String(httpProxy.getUsername() + ":"
                                + httpProxy.getPassword()).getBytes(),
                        Base64.DEFAULT));
                urlConnection.setRequestProperty("Proxy-Authorization", "Basic " + encoded);

                if(headers!=null){
                    for (int i = 0; i < headers.length; i++) {
                        urlConnection.setRequestProperty(headers[i].name, headers[i].value);
                    }
                }

                urlConnection.connect();
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
                if(headers!=null){
                    for (int i = 0; i < headers.length; i++) {
                        urlConnection.setRequestProperty(headers[i].name, headers[i].value);
                    }
                }
            }

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                InputStream in = new BufferedInputStream(
                        urlConnection.getInputStream());
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                return bitmap;
            } else {
                Log.e("DownloadImage", "Failed to download image");
                throw new ServerOperationException("Failed to download image");
            }
        } catch (IOException e) {
            Log.d("DownloadImage", "Failed to download image");

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            throw new ConnectionFailException("Failed to download image");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

    }

    public static HttpURLConnection openConnection(String urlString, NameValuePair... params) throws InvalidOperationException{
        return openConnection(urlString, null, params, null);
    }

    public static HttpURLConnection openConnection(String urlString, NameValuePair[] headers, NameValuePair[] params) throws InvalidOperationException{
        return openConnection(urlString, null, params, headers);
    }

    public static HttpURLConnection openConnection(String urlString, HttpProxy httpProxy, NameValuePair[] params, NameValuePair[] headers)
            throws InvalidOperationException{

        if(params!=null)
            urlString += getUrlParams(params);

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e1) {
            throw new InvalidOperationException(e1.getMessage(), e1);
        }
        HttpURLConnection urlConnection = null;
        try {

            if (httpProxy != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                        httpProxy.getHost(), httpProxy.getPort()));
                urlConnection = (HttpURLConnection) url.openConnection(proxy);
                String encoded = new String(Base64.encode(
                        new String(httpProxy.getUsername() + ":"
                                + httpProxy.getPassword()).getBytes(),
                        Base64.DEFAULT));
                urlConnection.setRequestProperty("Proxy-Authorization",
                        "Basic " + encoded);

                urlConnection.connect();
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
                if(headers!=null){
                    for (int i = 0; i < headers.length; i++) {
                        urlConnection.setRequestProperty(headers[i].name, headers[i].value);
                    }
                }

                urlConnection.connect();
            }

            int statusCode = urlConnection.getResponseCode();

            if (statusCode >= 500 ) {
                Log.d(LOG_TAG, "Server Error. Status Code "+String.valueOf(statusCode));
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                throw new ServerOperationException("Server Error. Status Code "+String.valueOf(statusCode));
            }
            return urlConnection;

        } catch (IOException e) {
            Log.d(LOG_TAG, "Failed to open connection");

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            throw new ConnectionFailException("Failed to open connection");
        }
    }

    private static String getUrlParams(NameValuePair[]args) throws InvalidOperationException {
        try {
            StringBuilder sb = new StringBuilder();
            if (args.length > 0) {
                sb.append("?");
                for (NameValuePair p : args) {
                    String paramString = p.name + "=" + URLEncoder.encode(p.value, "UTF-8");
                    if (sb.length() > 1) {
                        sb.append("&");
                        sb.append(paramString);
                    } else {
                        sb.append(paramString);
                    }
                }
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            Log.d(WebTools.class.getName(), e.getMessage(), e);
            throw new InvalidOperationException(e.getMessage(), e);
        }
    }

    public static <T> T uploadBytes(String urlString, Class<T>responseCls, byte[]data, int offset, int lenght, NameValuePair[]headers){
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e1) {
            throw new InvalidOperationException(e1.getMessage(), e1);
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            if(headers!=null){
                for (int i = 0; i < headers.length; i++) {
                    connection.setRequestProperty(headers[i].name, headers[i].value);
                }
            }

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
            bos.write(data, offset, lenght);
            bos.flush();
            bos.close();

            int statusCode = connection.getResponseCode();

            if (statusCode == 200) {
                InputStream in = new BufferedInputStream(
                        connection.getInputStream());
                String response = StringUtils.readAllText(in);
                in.close();
                return JSonSerializer.deserializeObject(responseCls, response);
            } else {
                Log.e(WebTools.class.getSimpleName(),"Failed to upload data");
                throw new ServerOperationException("Failed to upload data");
            }

        } catch (IOException e) {
            throw  new InvalidOperationException("Failed to upload data");
        }
    }

    public InputStream get(String url,  HttpProxy httpProxy, NameValuePair[] params, NameValuePair[] headers) throws InvalidOperationException{
        HttpURLConnection urlConnection = openConnection(url, httpProxy, params, headers);

        try {

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                return new BufferedInputStream(urlConnection.getInputStream());
            } else {
                Log.e("DownloadImage", "Failed to download image");
                throw new ServerOperationException("Failed to download image");
            }
        }catch (IOException e){
            Log.d(LOG_TAG, "Failed to open connection");

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            throw new ConnectionFailException("Failed to open connection");
        }
    }
}
