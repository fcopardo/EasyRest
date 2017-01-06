package com.grizzly.rest;

import com.grizzly.rest.Model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by FcoPardo on 1/5/17.
 * Builder class for GenericRestCalls. Allows easy setting of common parameters.
 */
public class GenericRestCallBuilder<T,X,M> {

    private Class<T> entityClass;
    private Class<X> jsonResponseEntityClass;
    private Class<M> errorResponseEntityClass;
    private String url = "";
    private HttpMethod methodToCall;
    private HttpHeaders requestHeaders = new HttpHeaders();
    private afterTaskCompletion<X> taskCompletion;
    private afterTaskFailure<X> taskFailure;
    private afterServerTaskFailure<M> serverTaskFailure;
    private afterClientTaskFailure<M> clientTaskFailure;
    private com.grizzly.rest.Model.commonTasks commonTasks;
    private long cacheTime = 899999;
    private boolean reprocessWhenRefreshing = false;
    private boolean automaticCacheRefresh = false;

    private List<Action1<RestResults<X>>> mySubscribers;
    private boolean fullAsync = false;

    public GenericRestCallBuilder(Class<T> entityClass, Class<X> jsonResponseEntityClass,
                                  Class<M> errorResponseEntityClass, String url,
                                  HttpMethod methodToCall, long cacheTime,
                                  boolean reprocessWhenRefreshing, boolean automaticCacheRefresh){
        this.entityClass = entityClass;
        this.jsonResponseEntityClass = jsonResponseEntityClass;
        this.errorResponseEntityClass = errorResponseEntityClass;
        this.url = url;
        this.methodToCall = methodToCall;
        this.cacheTime = cacheTime;
        this.automaticCacheRefresh = automaticCacheRefresh;
        this.reprocessWhenRefreshing = reprocessWhenRefreshing;
    }

    public GenericRestCallBuilder<T, X, M> setFullAsync(boolean fullAsync){
        this.fullAsync = fullAsync;
        return this;
    }
    
    public GenericRestCallBuilder<T, X, M> setAftertaskCompletion(afterTaskCompletion<X> taskCompletion){
        this.taskCompletion = taskCompletion;
        return this;
    }
    
    public GenericRestCallBuilder<T, X, M> setAftertaskFailure(afterTaskFailure<X> taskFailure){
        this.taskFailure = taskFailure;
        return this;
    }
    
    public GenericRestCallBuilder<T, X, M> setAfterClientTaskFailure(afterClientTaskFailure<M> clientTaskFailure){
        this.clientTaskFailure = clientTaskFailure;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setAfterServerTaskFailure(afterServerTaskFailure<M> ServerTaskFailure){
        this.serverTaskFailure = ServerTaskFailure;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setCommonTasks(commonTasks commonTasks){
        this.commonTasks = commonTasks;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> addSubscriber(Action1<RestResults<X>> action){
        mySubscribers.add(action);
        execute(true);
        return this;
    }

    public GenericRestCallBuilder<T, X, M> deleteSubscriber(Action1<RestResults<X>> action){
        if (mySubscribers == null) mySubscribers = new ArrayList<>();
        mySubscribers.remove(action);
        return this;
    }

    public void execute(boolean asyncronously){
        GenericRestCall<T, X, M> restCall = new GenericRestCall<>(entityClass, jsonResponseEntityClass, errorResponseEntityClass)
                .setMethodToCall(methodToCall)
                .setUrl(url)
                .setRequestHeaders(requestHeaders)
                .setTaskCompletion(taskCompletion)
                .setTaskFailure(taskFailure)
                .setClientTaskFailure(clientTaskFailure)
                .setServerTaskFailure(serverTaskFailure)
                .setSuccessSubscribers(mySubscribers)
                .setCommonTasks(commonTasks)
                .setFullAsync(fullAsync)
                .setAutomaticCacheRefresh(automaticCacheRefresh)
                .setReprocessWhenRefreshing(reprocessWhenRefreshing)
                .setCacheTime(cacheTime);

        if(asyncronously){
            restCall.execute(true);
        }else{
            try {
                restCall.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }


}
