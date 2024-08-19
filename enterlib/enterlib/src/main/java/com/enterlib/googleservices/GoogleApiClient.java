package com.enterlib.googleservices;

import android.graphics.Bitmap;
import android.util.Log;

import com.enterlib.StringUtils;
import com.enterlib.data.web.WebTools;
import com.enterlib.exceptions.ConnectionFailException;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.exceptions.ServerOperationException;
import com.enterlib.googleservices.autocomplete.AutocompleteInfo;
import com.enterlib.googleservices.geocode.GeocodeInfo;
import com.enterlib.googleservices.routes.RoutesInfo;
import com.enterlib.serialization.JSonSerializer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by ansel on 17/09/2016.
 */
public class GoogleApiClient {

    static String LOG_TAG = GoogleApiClient.class.getSimpleName();

    public static final String TYPE_GEOCODE = "geocode";

    public static final String TYPE_ADDRESS = "address ";

    public static final String TYPE_ESTABLISHMENT  = "establishment";

    public static final String TYPE_REGIONS  = "(regions)";

    public static final String TYPE_CITIES  = "(cities)";

    public static RoutesInfo getRouteInfo(String originAddr, String destinationAddr)throws Exception{
        String urlString;
        try {
            String orig = URLEncoder.encode(originAddr, "UTF-8");
            String dest = URLEncoder.encode(destinationAddr, "UTF-8");

            urlString =String.format("http://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&sensor=false&mode=driving&alternatives=true",
                    orig, dest);
        }
        catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            throw e;
        }

        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();

            int statusCode =  urlConnection.getResponseCode();

            if(statusCode == 200){
                String json = StringUtils.readAllText(urlConnection.getInputStream());
                JSonSerializer serializer = new JSonSerializer();
                return (RoutesInfo) serializer.deserialize(RoutesInfo.class, json);
            }
            else{
                Log.e(LOG_TAG, "Failed to file");
                throw new ServerOperationException("Failed to file");
            }
        }
        catch(IOException e){
            Log.e(LOG_TAG, "Failed to file");

            if(urlConnection!=null)
                urlConnection.disconnect();
            throw new ConnectionFailException("Failed to file");
        }
        finally {
            if(urlConnection!=null)
                urlConnection.disconnect();
        }
    }

    public static Bitmap getStaticMap(int width, int height, String addres){
        String size = width+"x"+height;
        final String url;
        try {
            url = "http://maps.googleapis.com/maps/api/staticmap?size="+size+"&markers=size:mid|color:red|"
                    + URLEncoder.encode(addres, "UTF-8") + "&zoom=15&sensor=false";

            return WebTools.downloadImage(url);

        } catch (UnsupportedEncodingException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }
    }

    public static AutocompleteInfo getAutocompleteInfo(String input,
                                                       String country,
                                                       String key,
                                                       String types,
                                                       String languaje){
        String urlString = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
        int params = 0;
        if(key!=null) {
            urlString += "key=" + key;
            params++;
        }
        if(country!=null){
            if(params>0){
                urlString+="&";
            }
            urlString+="components=country:"+country;
            params++;
        }

        if(types!=null){
            if(params>0){
                urlString+="&";
            }
            urlString+="types="+types;
            params++;
        }
        if(languaje!=null){
            if(params>0){
                urlString+="&";
            }
            urlString+="language="+languaje;
            params++;
        }


        if(params>0){
            urlString+="&";
        }
        try {
            urlString += "input=" + URLEncoder.encode(input, "UTF-8");
            params++;
        }  catch (UnsupportedEncodingException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }

        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();

            int statusCode =  urlConnection.getResponseCode();

            if(statusCode == 200){
                String json = StringUtils.readAllText(urlConnection.getInputStream());
                JSonSerializer serializer = new JSonSerializer();
                return (AutocompleteInfo) serializer.deserialize(AutocompleteInfo.class, json);
            }
            else{
                Log.e(LOG_TAG, "Failed to file");
                throw new ServerOperationException("Operation  failed ,status "+String.valueOf(statusCode));
            }
        }
        catch(IOException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            if(urlConnection!=null)
                urlConnection.disconnect();
            throw new ConnectionFailException(e.getMessage(), e);
        }
        finally {
            if(urlConnection!=null)
                urlConnection.disconnect();
        }

    }

    public static GeocodeInfo getGeocodeInfo(String address)  {
        String urlString;
        try {
            String encoded_address = URLEncoder.encode(address, "UTF-8");
            urlString = String.format("http://maps.googleapis.com/maps/api/geocode/json?address=%s", encoded_address);
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            throw new InvalidOperationException(e.getLocalizedMessage(), e);
        }

        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();

            int statusCode =  urlConnection.getResponseCode();

            if(statusCode == 200){
                String json = StringUtils.readAllText(urlConnection.getInputStream());
                JSonSerializer serializer = new JSonSerializer();
                return (GeocodeInfo) serializer.deserialize(GeocodeInfo.class, json);
            }
            else{
                Log.e(LOG_TAG, "Failed to file");
                throw new ServerOperationException("Failed to file");
            }
        }
        catch(IOException e){
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);

            if(urlConnection!=null)
                urlConnection.disconnect();
            throw new ConnectionFailException(e.getLocalizedMessage(), e);
        }
        finally {
            if(urlConnection!=null)
                urlConnection.disconnect();
        }

    }


}
