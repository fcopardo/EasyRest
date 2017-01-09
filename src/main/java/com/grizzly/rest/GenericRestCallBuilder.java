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

    protected Class<T> entityClass;
    protected Class<X> jsonResponseEntityClass;
    protected Class<M> errorResponseEntityClass;
    protected String url = "";
    protected HttpMethod methodToCall;
    protected HttpHeaders requestHeaders = new HttpHeaders();
    protected afterTaskCompletion<X> taskCompletion;
    protected afterTaskFailure<X> taskFailure;
    protected afterServerTaskFailure<M> serverTaskFailure;
    protected afterClientTaskFailure<M> clientTaskFailure;
    protected com.grizzly.rest.Model.commonTasks commonTasks;
    protected long cacheTime = 899999;
    protected boolean reprocessWhenRefreshing = false;
    protected boolean automaticCacheRefresh = false;
    protected int timeOut;

    protected List<Action1<RestResults<X>>> mySubscribers;
    protected boolean fullAsync = false;

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

    public GenericRestCallBuilder<T, X, M> setAutomaticCacheRefresh(boolean automaticCacheRefresh) {
        this.automaticCacheRefresh = automaticCacheRefresh;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setReprocessWhenRefreshing(boolean reprocessWhenRefreshing) {
        this.reprocessWhenRefreshing = reprocessWhenRefreshing;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setUrl(String url) {
        this.url = url;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setMethodToCall(HttpMethod methodToCall) {
        this.methodToCall = methodToCall;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setMySubscribers(List<Action1<RestResults<X>>> mySubscribers, boolean executeNow) {
        this.mySubscribers = mySubscribers;
        if(executeNow) execute(true);
        return this;
    }

    public GenericRestCallBuilder<T, X, M> setTimeOut(int timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public void execute(boolean asyncronously){

        if(asyncronously){
            create().execute(true);
        }else{
            try {
                create().execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public GenericRestCall<T, X, M> create(){

        return new GenericRestCall<>(entityClass, jsonResponseEntityClass, errorResponseEntityClass)
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
                .setCacheTime(cacheTime)
                .setTimeOut(timeOut);
    }


}
