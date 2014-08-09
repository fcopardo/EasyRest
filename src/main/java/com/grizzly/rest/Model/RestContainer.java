package com.grizzly.rest.Model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * Created by fpardo on 8/7/14.
 */
public class RestContainer {

    private HttpMethod myHttpMethod = null;
    private HttpHeaders myRequestHeaders = null;
    private String requestUrl = "";

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
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
}
