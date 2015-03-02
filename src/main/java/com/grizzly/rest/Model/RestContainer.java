package com.grizzly.rest.Model;

import android.net.Uri;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fpardo on 8/7/14.
 * restCntainer is a wrapper around several values needed to make rest calls.
 */
public class RestContainer {

    /**
     * Class members
     * myHttpMethod is a valid HttpMethod
     * myRequestHeaders is a valid HttpHeader object
     * requestUrl is a string representing the url to be called
     * urlParameters is a map containing every url parameter
     */
    private HttpMethod myHttpMethod = null;
    private HttpHeaders myRequestHeaders = null;
    private String requestUrl = "";
    private Map<String, Object> urlParameters = null;
    private boolean cacheEnabled = true;

    public void setMyHttpMethod(HttpMethod Method) {

        myHttpMethod = Method;
    }

    public HttpMethod getMyHttpMethod() {
        return myHttpMethod;
    }

    public HttpHeaders getMyRequestHeaders() {
        return myRequestHeaders;
    }

    public void setMyRequestHeaders(HttpHeaders myRequestHeaders) {
        this.myRequestHeaders = myRequestHeaders;
    }

    public String getRequestUrl() {

        if(urlParameters != null && !urlParameters.isEmpty()){
            String customUrl = requestUrl;
            StringBuilder builder = new StringBuilder();

            String separator = "?";

            builder.append(customUrl);
            for(String key: urlParameters.keySet()){
                Object value = urlParameters.get(key);

                if(value.toString().contains(" ")){
                    value = value.toString().replace(" ", "+");
                }

                builder.append(separator);
                builder.append(key);
                builder.append("=");
                builder.append(value);

                separator = "&";
            }
            return builder.toString();
        }
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public Map<String, Object> getUrlParameters() {
        return urlParameters;
    }

    public void setUrlParameters(Map<String, Object> urlParameters) {
        this.urlParameters = urlParameters;
    }

    public void addParameterToUrl(String param, Object value){
        if(urlParameters == null ) urlParameters = new HashMap<>();
        urlParameters.put(param, value);
    }

    public boolean deleteParamFromUrl(String param) {

        try{
            urlParameters.remove(param);
            return true;
        }
        catch(Exception noParameter){
            return false;
        }
    }

}
