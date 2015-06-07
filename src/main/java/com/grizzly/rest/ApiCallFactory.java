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

    private WebServiceFactory webServiceFactory = null;
    private Map<String, RestContainer> requestCollection = new HashMap<>();

    public ApiCallFactory(String baseUrl){
        webServiceFactory = new WebServiceFactory();
        webServiceFactory.setBaseUrl(baseUrl);
    }

    public ApiCallFactory(String baseUrl, Context context){
        webServiceFactory = new WebServiceFactory(context);
        webServiceFactory.setBaseUrl(baseUrl);
    }

    public void addApiMemberCall(String name, RestContainer configuration){
        requestCollection.put(name, configuration);
    }

    public <T, X> GenericRestCall<T, X> getApiCall(String requestName,
                                                   Class<T> requestBodyType, Class<X> responseBodyType,
                                                   afterTaskCompletion<X> taskCompletion,
                                                   afterTaskFailure failure,
                                                   afterClientTaskFailure clientFailure,
                                                   afterServerTaskFailure serverFailure,
                                                   commonTasks commonTasks
                                                   ){

        GenericRestCall<T, X> restCall = webServiceFactory.getGenericRestCallInstance(requestBodyType, responseBodyType);
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

