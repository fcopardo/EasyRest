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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.grizzly.rest.Model.RestResults;
import com.grizzly.rest.Model.sendRestData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import rx.Subscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 24/03/14.
 * Creates instances of parametrized AdvancedRestCall
 */
public class WebServiceFactory implements CacheProvider{

    private HttpHeaders requestHeaders = new HttpHeaders();
    private HttpHeaders responseHeaders = new HttpHeaders();
    private Context context = null;
    private long globalCacheTime = 899999;
    private int timeOutValue = 60000;
    private static HashMap<String, String> cachedRequests = new HashMap<>();
    private String baseUrl = "";
    private MappingJackson2HttpMessageConverter jacksonConverter;
    private HashMap<String, GenericRestCallBuilder> genericRestCallBuilders = new HashMap<>();

    private Map<String, List<Subscriber<RestResults>>> subscribers;

    public HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void resetHeaders() {
        requestHeaders = new HttpHeaders();
        responseHeaders = new HttpHeaders();
    }

    private Context getContext() {
        if(context == null) context = context.getApplicationContext();
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setGlobalCacheTime(long time){
        globalCacheTime = time;
    }

    public void setTimeOutValue(int miliseconds){
        if(miliseconds>=0){
            timeOutValue = miliseconds;
        }
        else{
            throw new IllegalArgumentException("The timeout must be greater than zero");
        }
    }

    public void setBaseUrl(String BaseUrl){
        baseUrl = BaseUrl;
    }

    public WebServiceFactory() {
    }

    public WebServiceFactory(Context context) {
        this.context = context;
    }

    public <T extends sendRestData, X> EasyRestCall<T, X, Void> getRestCallInstance(Class<T> entityClass, Class<X> responseClass) {

        return this.getRestCallInstance(entityClass, responseClass, Void.class, false);
    }

    public <T extends sendRestData, X> EasyRestCall<T, X, Void> getRestCallInstance(Class<T> entityClass, Class<X> responseClass, boolean isTest) {

        return this.getRestCallInstance(entityClass, responseClass, Void.class, isTest);
    }

    public <T extends sendRestData, X, M> EasyRestCall<T, X, M> getRestCallInstance(Class<T> entityClass, Class<X> responseClass, Class<M> errorBodyClass) {

        return this.getRestCallInstance(entityClass, responseClass, errorBodyClass, false);
    }

    public <T extends sendRestData, X, M> EasyRestCall<T, X, M> getRestCallInstance(Class<T> entityClass, Class<X> responseClass, Class<M> errorBodyClass, boolean isTest) {

        EasyRestCall<T, X, M> myRestCall = null;

        if(isTest) {
            myRestCall = new EasyRestCall<>(entityClass, responseClass, errorBodyClass, 1);
        }
        else {
            if(genericRestCallBuilders.containsKey(entityClass.getSimpleName()+responseClass.getSimpleName()+errorBodyClass.getSimpleName())){
                GenericRestCallBuilder<T, X, M> restCallBuilder = new EasyRestCallBuilder<>
                        (entityClass, responseClass, errorBodyClass, "", HttpMethod.GET, globalCacheTime, false, false)
                        .setTimeOut(timeOutValue);

                if(requestHeaders!= null && !requestHeaders.isEmpty()){
                    restCallBuilder.setRequestHeaders(requestHeaders);
                }
                if(!baseUrl.isEmpty() && baseUrl.trim().equalsIgnoreCase("") && baseUrl != null){
                    restCallBuilder.setUrl(baseUrl);
                }

                myRestCall = (EasyRestCall<T, X, M>) restCallBuilder.create();
                genericRestCallBuilders.put(entityClass.getSimpleName()+responseClass.getSimpleName()+errorBodyClass.getSimpleName(), restCallBuilder);
            }
            else {
                myRestCall = (EasyRestCall<T, X, M>) genericRestCallBuilders.get(entityClass.getSimpleName()+responseClass.getSimpleName()+errorBodyClass.getSimpleName()).create();
            }
        }

        setExtras(myRestCall);
        return myRestCall;
    }

    public <T, X> GenericRestCall<T, X, Void> getGenericRestCallInstance(Class<T> entityClass, Class<X> responseClass, boolean isTest) {

        return this.getGenericRestCallInstance(entityClass, responseClass, Void.class, isTest);
    }

    public <T, X> GenericRestCall<T, X, Void> getGenericRestCallInstance(Class<T> entityClass, Class<X> responseClass) {

        return this.getGenericRestCallInstance(entityClass, responseClass, Void.class, false);
    }

    public <T, X, M> GenericRestCall<T, X, M> getGenericRestCallInstance(Class<T> entityClass, Class<X> responseClass, Class<M> errorBodyClass) {

        return this.getGenericRestCallInstance(entityClass, responseClass, errorBodyClass, false);
    }

    public <T, X, M> GenericRestCall<T, X, M> getGenericRestCallInstance(Class<T> entityClass, Class<X> responseClass, Class<M> errorBodyClass, boolean isTest) {

        GenericRestCall<T, X, M> myRestCall = null;

        if(isTest) {
            myRestCall = new GenericRestCall<>(entityClass, responseClass, errorBodyClass, 1);
        }
        else {
            if(genericRestCallBuilders.containsKey(entityClass.getSimpleName()+responseClass.getSimpleName()+errorBodyClass.getSimpleName())){
                GenericRestCallBuilder<T, X, M> restCallBuilder = new GenericRestCallBuilder<>
                        (entityClass, responseClass, errorBodyClass, "", HttpMethod.GET, globalCacheTime, false, false)
                        .setTimeOut(timeOutValue);

                if(requestHeaders!= null && !requestHeaders.isEmpty()){
                    restCallBuilder.setRequestHeaders(requestHeaders);
                }
                if(!baseUrl.isEmpty() && baseUrl.trim().equalsIgnoreCase("") && baseUrl != null){
                    restCallBuilder.setUrl(baseUrl);
                }

                myRestCall = restCallBuilder.create();
                genericRestCallBuilders.put(entityClass.getSimpleName()+responseClass.getSimpleName()+errorBodyClass.getSimpleName(), restCallBuilder);
            }
            else {
                myRestCall = (GenericRestCall<T, X, M>) genericRestCallBuilders.get(entityClass.getSimpleName()+responseClass.getSimpleName()+errorBodyClass.getSimpleName()).create();
            }
        }

        setExtras(myRestCall);

        return myRestCall;
    }

    @Override
    public <T, X, M> boolean setCache(GenericRestCall<T, X, M> myRestCall, Class<X> responseClass, Class<T> entityClass, Class<M> errorBodyClass){

        boolean bol = false;

        if(!myRestCall.getUrl().trim().equalsIgnoreCase("") && myRestCall != null)
        {

            if(!responseClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName()) ){


                /*if(cachedRequests.containsKey(myRestCall.getUrl())){
                    myRestCall.setCachedFileName(cachedRequests.get(myRestCall.getUrl()));
                    return true;
                }
                else{
                    cachedRequests.put(myRestCall.getUrl(), myRestCall.getCachedFileName());
                }*/
            }
            return false;
        }

        return bol;
    }

    /**
     * Factory getter method. Returns either the current instance of the MappingJackson2HttpConverter,
     * or a initialized one wih FAIL_ON_UNKNOW_PROPERTIES and FAIL_ON_INVALID_SUBTYPE set to false.
     * @return the jacksonConverter to be used by all the rest calls created by this factory.
     */
    public MappingJackson2HttpMessageConverter getJacksonConverter() {
        if(jacksonConverter == null){
            MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
            jacksonConverter.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jacksonConverter.getObjectMapper().configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        }
        return jacksonConverter;
    }

    /**
     * Allows setting a custom MappingJackson2HttpConverter
     * @param jacksonConverter the converter to be set.
     */
    public void setJacksonConverter(MappingJackson2HttpMessageConverter jacksonConverter) {
        this.jacksonConverter = jacksonConverter;
    }

    protected void setExtras(GenericRestCall myRestCall){
        try{
            if(context != null)
            {
                myRestCall.setContext(getContext());
                myRestCall.setCacheProvider(this);
            }
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        if(jacksonConverter!=null){
            myRestCall.setJacksonMapper(jacksonConverter);
        }
    }
}
