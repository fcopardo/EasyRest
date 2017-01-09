package com.grizzly.rest;

import com.grizzly.rest.Model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FcoPardo on 1/5/17.
 * Builder class for GenericRestCalls. Allows easy setting of common parameters.
 */
public class EasyRestCallBuilder<T extends sendRestData,X,M> extends GenericRestCallBuilder<T, X, M> {

    public EasyRestCallBuilder(Class<T> entityClass, Class<X> jsonResponseEntityClass,
                               Class<M> errorResponseEntityClass, String url,
                               HttpMethod methodToCall, long cacheTime,
                               boolean reprocessWhenRefreshing, boolean automaticCacheRefresh){

        super(entityClass, jsonResponseEntityClass, errorResponseEntityClass, url, methodToCall, cacheTime, reprocessWhenRefreshing, automaticCacheRefresh);
    }

    public EasyRestCallBuilder<T, X, M> setFullAsync(boolean fullAsync){
        this.fullAsync = fullAsync;
        return this;
    }
    
    public EasyRestCallBuilder<T, X, M> setAftertaskCompletion(afterTaskCompletion<X> taskCompletion){
        this.taskCompletion = taskCompletion;
        return this;
    }
    
    public EasyRestCallBuilder<T, X, M> setAftertaskFailure(afterTaskFailure<X> taskFailure){
        this.taskFailure = taskFailure;
        return this;
    }
    
    public EasyRestCallBuilder<T, X, M> setAfterClientTaskFailure(afterClientTaskFailure<M> clientTaskFailure){
        this.clientTaskFailure = clientTaskFailure;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setAfterServerTaskFailure(afterServerTaskFailure<M> ServerTaskFailure){
        this.serverTaskFailure = ServerTaskFailure;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setCommonTasks(commonTasks commonTasks){
        this.commonTasks = commonTasks;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> addSubscriber(Action1<RestResults<X>> action){
        mySubscribers.add(action);
        execute(true);
        return this;
    }

    public EasyRestCallBuilder<T, X, M> deleteSubscriber(Action1<RestResults<X>> action){
        if (mySubscribers == null) mySubscribers = new ArrayList<>();
        mySubscribers.remove(action);
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setAutomaticCacheRefresh(boolean automaticCacheRefresh) {
        this.automaticCacheRefresh = automaticCacheRefresh;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setReprocessWhenRefreshing(boolean reprocessWhenRefreshing) {
        this.reprocessWhenRefreshing = reprocessWhenRefreshing;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setUrl(String url) {
        this.url = url;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setMethodToCall(HttpMethod methodToCall) {
        this.methodToCall = methodToCall;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setMySubscribers(List<Action1<RestResults<X>>> mySubscribers, boolean executeNow) {
        this.mySubscribers = mySubscribers;
        if(executeNow) execute(true);
        return this;
    }

    public EasyRestCallBuilder<T, X, M> setTimeOut(int timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> create(){
        EasyRestCall<T, X, M> restCall = new EasyRestCall<>(entityClass, jsonResponseEntityClass, errorResponseEntityClass)
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

        return restCall;
    }


}
