/*
 * Copyright (c) 2014. Francisco Pardo Baeza
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.grizzly.rest;

import android.content.Context;
import com.grizzly.rest.Model.sendRestData;
import org.springframework.http.HttpHeaders;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created on 24/03/14.
 * Creates instances of parametrized AdvancedRestCall
 */
public class WebServiceFactory {

    private HttpHeaders requestHeaders = new HttpHeaders();
    private HttpHeaders responseHeaders = new HttpHeaders();
    private Context context = null;
    private HashMap<String, String> cachedRequests = new HashMap<>();


    public HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }

    public void resetHeaders() {
        requestHeaders = new HttpHeaders();
        responseHeaders = new HttpHeaders();
    }

    public void setResponseHeaders(HttpHeaders responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    private Context getContext() {
        if(context == null) context = context.getApplicationContext();
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public WebServiceFactory() {
    }

    public WebServiceFactory(Context context) {
        this.context = context;
    }

    public <T extends sendRestData, X> EasyRestCall<T, X> getRestCallInstance(Class<T> entityClass, Class<X> responseClass) {

        return this.getRestCallInstance(entityClass, responseClass, false);
    }

    public <T extends sendRestData, X> EasyRestCall<T, X> getRestCallInstance(Class<T> entityClass, Class<X> responseClass, boolean isTest) {

        EasyRestCall<T, X> myRestCall = null;

        if(isTest) {
            myRestCall = new EasyRestCall<>(entityClass, responseClass, 1);
        }
        else {
            myRestCall = new EasyRestCall<>(entityClass, responseClass);
        }
        if(context != null)
        {
            myRestCall.setContext(getContext());

            if(!responseClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName()) ){

                String uuid = UUID.randomUUID().toString()+ Calendar.getInstance().getTime().toString()+entityClass.getCanonicalName()+responseClass.getCanonicalName();


                if(cachedRequests.containsKey(myRestCall.getUrl()) && !myRestCall.getUrl().toString().isEmpty() && !myRestCall.getUrl().toString().equalsIgnoreCase("")){
                    uuid = cachedRequests.get(myRestCall.getUrl());
                }
                myRestCall.setCachedFileName(uuid);
                cachedRequests.put(myRestCall.getUrl(), uuid);
            }
        }
        try{

        }
        catch(NullPointerException e){
            e.printStackTrace();
        }

        return myRestCall;
    }

    public <T, X> GenericRestCall<T, X> getGenericRestCallInstance(Class<T> entityClass, Class<X> responseClass) {

        return this.getGenericRestCallInstance(entityClass, responseClass, false);
    }

    public <T, X> GenericRestCall<T, X> getGenericRestCallInstance(Class<T> entityClass, Class<X> responseClass, boolean isTest) {

        GenericRestCall<T, X> myRestCall = null;

        if(isTest) {
            myRestCall = new GenericRestCall<>(entityClass, responseClass, 1);
        }
        else {
            myRestCall = new GenericRestCall<>(entityClass, responseClass);
        }

        try{
            if(context != null)
            {
                myRestCall.setContext(getContext());

                if(!responseClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName()) ){

                    String uuid = UUID.randomUUID().toString()+ Calendar.getInstance().getTime().toString()+entityClass.getCanonicalName()+responseClass.getCanonicalName();
                    if(cachedRequests.containsKey(myRestCall.getUrl()) && !myRestCall.getUrl().toString().isEmpty() && !myRestCall.getUrl().toString().equalsIgnoreCase("")){
                        uuid = cachedRequests.get(myRestCall.getUrl());
                    }
                    myRestCall.setCachedFileName(uuid);
                    cachedRequests.put(myRestCall.getUrl(), uuid);
                }
            }
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }

        return myRestCall;
    }

    <T extends sendRestData, X> boolean setCache(EasyRestCall<T, X> myRestCall, Class<X> responseClass, Class<T> entityClass){

        boolean bol = false;

        if(context != null && !myRestCall.getUrl().trim().equalsIgnoreCase("") && myRestCall != null)
        {

            if(!responseClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName()) ){


                if(cachedRequests.containsKey(myRestCall.getUrl())){
                    myRestCall.setCachedFileName(cachedRequests.get(myRestCall.getUrl()));
                    return true;
                }
                else{
                    cachedRequests.put(myRestCall.getUrl(), myRestCall.getCachedFileName());
                }
            }
            return false;
        }

        return bol;
    }

    <T extends sendRestData, X> boolean setCache(GenericRestCall<T, X> myRestCall, Class<X> responseClass, Class<T> entityClass){

        boolean bol = false;

        if(context != null && !myRestCall.getUrl().trim().equalsIgnoreCase("") && myRestCall != null)
        {

            if(!responseClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName()) ){


                if(cachedRequests.containsKey(myRestCall.getUrl())){
                    myRestCall.setCachedFileName(cachedRequests.get(myRestCall.getUrl()));
                    return true;
                }
                else{
                    cachedRequests.put(myRestCall.getUrl(), myRestCall.getCachedFileName());
                }
            }
            return false;
        }

        return bol;
    }

}
