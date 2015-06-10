package com.grizzly.rest;

import android.content.Context;
import com.grizzly.rest.Model.RestContainer;
import com.grizzly.rest.Model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by FcoPardo on 6/6/15.
 */
public class ApiCallFactory {

    protected WebServiceFactory webServiceFactory = null;
    private Map<Integer, RestContainer> requestCollection = new HashMap<>();

    public ApiCallFactory(String baseUrl){
        webServiceFactory = new WebServiceFactory();
        webServiceFactory.setBaseUrl(baseUrl);
    }

    public ApiCallFactory(String baseUrl, Context context){
        webServiceFactory = new WebServiceFactory(context);
        webServiceFactory.setBaseUrl(baseUrl);
    }

    public ApiCallFactory(String baseUrl, WebServiceFactory factory, Context context){
        webServiceFactory = factory;
        webServiceFactory.setBaseUrl(baseUrl);
    }

    public void addApiMemberCall(Integer name, RestContainer configuration){
        requestCollection.put(name, configuration);
    }

    public RestContainer getApimemberCall(String name){
        if(requestCollection.containsKey(name)) return requestCollection.get(name);
        return null;
    }

    public void deleteApiMemberCall(String name){
        if(requestCollection.containsKey(name)) requestCollection.remove(name);
    }

    /**
     * Base call for bodyless requests.
     * @param requestName
     * @param responseBodyType
     * @param taskCompletion
     * @param failure
     * @param clientFailure
     * @param serverFailure
     * @param commonTasks
     * @param isAsyncCall
     * @param <X>
     */
    public <X> void callApiMethod(int requestName,
                                    Class<X> responseBodyType,
                                    afterTaskCompletion<X> taskCompletion,
                                    afterTaskFailure failure,
                                    afterClientTaskFailure clientFailure,
                                    afterServerTaskFailure serverFailure,
                                    commonTasks commonTasks,
                                    boolean isAsyncCall){

        GenericRestCall<Void, X, Void> restCall = createApiMethod(requestName, Void.class, responseBodyType, Void.class, taskCompletion, failure, clientFailure, serverFailure, commonTasks);
        restCall.execute(isAsyncCall);
    }

    /**
     * Base call for bodyless requests.
     * @param requestName
     * @param responseBodyType
     * @param taskCompletion
     * @param failure
     * @param clientFailure
     * @param serverFailure
     * @param commonTasks
     * @param isAsyncCall
     * @param <X>
     */
    public <X, M> void callApiMethod(int requestName,
                                  Class<X> responseBodyType, Class<M> errorBodyType,
                                  afterTaskCompletion<X> taskCompletion,
                                  afterTaskFailure failure,
                                  afterClientTaskFailure clientFailure,
                                  afterServerTaskFailure serverFailure,
                                  commonTasks commonTasks,
                                  boolean isAsyncCall){

        GenericRestCall<Void, X, M> restCall = createApiMethod(requestName, Void.class, responseBodyType, errorBodyType, taskCompletion, failure, clientFailure, serverFailure, commonTasks);
        restCall.execute(isAsyncCall);
    }

    /**
     * Base call for request with an attached body.
     * @param requestName
     * @param requestBodyType
     * @param responseBodyType
     * @param requestBody
     * @param taskCompletion
     * @param failure
     * @param clientFailure
     * @param serverFailure
     * @param commonTasks
     * @param isAsyncCall
     * @param <T>
     * @param <X>
     */
    public <T,X> void callApiMethod(int requestName,
                                    Class<T> requestBodyType,
                                    Class<X> responseBodyType,
                                    T requestBody,
                                    afterTaskCompletion<X> taskCompletion,
                                    afterTaskFailure failure,
                                    afterClientTaskFailure clientFailure,
                                    afterServerTaskFailure serverFailure,
                                    commonTasks commonTasks,
                                    boolean isAsyncCall){

        GenericRestCall<T, X, Void> restCall = createApiMethod(requestName, requestBodyType, responseBodyType, Void.class, taskCompletion, failure, clientFailure, serverFailure, commonTasks);
        restCall.setEntity(requestBody);
        restCall.execute(isAsyncCall);
    }

    /**
     * Base call for request with an attached body.
     * @param requestName
     * @param requestBodyType
     * @param responseBodyType
     * @param requestBody
     * @param taskCompletion
     * @param failure
     * @param clientFailure
     * @param serverFailure
     * @param commonTasks
     * @param isAsyncCall
     * @param <T>
     * @param <X>
     */
    public <T,X,M> void callApiMethod(int requestName,
                                    Class<T> requestBodyType,
                                    Class<X> responseBodyType,
                                    Class<M> errorBodyType,
                                    T requestBody,
                                    afterTaskCompletion<X> taskCompletion,
                                    afterTaskFailure failure,
                                    afterClientTaskFailure clientFailure,
                                    afterServerTaskFailure serverFailure,
                                    commonTasks commonTasks,
                                    boolean isAsyncCall){

        GenericRestCall<T, X, M> restCall = createApiMethod(requestName, requestBodyType, responseBodyType, errorBodyType, taskCompletion, failure, clientFailure, serverFailure, commonTasks);
        restCall.setEntity(requestBody);
        restCall.execute(isAsyncCall);
    }

    public <T, X, M> GenericRestCall<T, X, M> createApiMethod(int requestName,
                                    Class<T> requestBodyType, Class<X> responseBodyType, Class<M> errorBodyType,
                                    afterTaskCompletion<X> taskCompletion,
                                    afterTaskFailure failure,
                                    afterClientTaskFailure clientFailure,
                                    afterServerTaskFailure serverFailure,
                                    commonTasks commonTasks){

        GenericRestCall<T, X, M> restCall = webServiceFactory.getGenericRestCallInstance(requestBodyType, responseBodyType, errorBodyType);
        if(requestCollection.containsKey(requestName)){
            RestContainer params = requestCollection.get(requestName);
            restCall.setUrl(restCall.getUrl()+params.getRequestUrl());
            if(params.getMyRequestHeaders()!=null) restCall.addUrlParams(params.getUrlParameters());
            if(params.getMyHttpMethod()!=null) restCall.setMethodToCall(params.getMyHttpMethod());
            if(params.isCacheEnabled() && params.getCacheTime()>0L){
                restCall.isCacheEnabled(true);
                restCall.setCacheTime(params.getCacheTime());
            }
        }
        if(taskCompletion!=null)restCall.setTaskCompletion(taskCompletion);
        if(failure!=null)restCall.setTaskFailure(failure);
        if(clientFailure!=null)restCall.setClientTaskFailure(clientFailure);
        if(serverFailure!=null)restCall.setServerTaskFailure(serverFailure);
        if(commonTasks!=null) restCall.setCommonTasks(commonTasks);
        return restCall;
    }

}

