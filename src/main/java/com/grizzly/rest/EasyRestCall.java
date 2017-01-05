package com.grizzly.rest;

import android.app.Activity;
import android.content.Context;
import com.grizzly.rest.Model.*;


/**
 * Created by Fco Pardo on 8/23/14.
 */
public class EasyRestCall<T extends sendRestData, X, M> extends GenericRestCall<T,X,M> {

    public EasyRestCall(Class<T> entityClass, Class<X> jsonResponseEntityClass, Class<M> errorResponseEntityClass){

        super(entityClass, jsonResponseEntityClass, errorResponseEntityClass);
    }

    public EasyRestCall(Class<T> entityClass, Class<X> jsonResponseEntityClass, Class<M> errorResponseEntityClass, int test){

        super(entityClass, jsonResponseEntityClass, errorResponseEntityClass, test);
    }

    /**
     * Sets the argument entity. It also gets the connection parameters
     * Url, Method, Url parameters and request headers from the entity.
     * @param entity a class implementing sendRestData, to be sent in the request.
     */
    @Override
    public EasyRestCall<T, X, M> setEntity(T entity) {

        super.setEntity(entity);
        try{
            if(entity.getRestContainer().getMyRequestHeaders() != null) {

                if(getRequestHeaders()!=null && !getRequestHeaders().isEmpty()){
                    for(String s: entity.getRestContainer().getMyRequestHeaders().keySet()){
                        getRequestHeaders().put(s, entity.getRestContainer().getMyRequestHeaders().get(s));
                    }
                }
                else{
                    this.setRequestHeaders(entity.getRestContainer().getMyRequestHeaders());
                }
            }
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }

        if(this.getUrl() == null || getUrl().trim().equalsIgnoreCase("") ) this.setUrl(entity.getRestContainer().getRequestUrl());
        this.setMethodToCall(entity.getRestContainer().getMyHttpMethod());
        
        if(entity.getRestContainer().isCacheEnabled() && entity.getRestContainer().getCacheTime()>0L){
            this.isCacheEnabled(true);
            this.setCacheTime(entity.getRestContainer().getCacheTime());
        }

        return this;
    }

    @Override
    public EasyRestCall<T, X, M> getThis(){
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setCacheTime(Long time){
        super.setCacheTime(time);
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setContext(Context context){
        super.setContext(context);
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setTaskCompletion(afterTaskCompletion task){
        super.setTaskCompletion(task);
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setTaskFailure(afterTaskFailure taskFailure) {
        super.setTaskFailure(taskFailure);
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setServerTaskFailure(afterServerTaskFailure<M> serverTaskFailure) {
        super.setServerTaskFailure(serverTaskFailure);
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setClientTaskFailure(afterClientTaskFailure<M> clientTaskFailure) {
        super.setClientTaskFailure(clientTaskFailure);
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setCommonTasks(com.grizzly.rest.Model.commonTasks commonTasks) {
        super.setCommonTasks(commonTasks);
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setActivity(Activity activity) {
        super.setActivity(activity);
        return this;
    }

    @Override
    public EasyRestCall<T, X, M> setFullAsync(boolean fullAsync) {
        super.setFullAsync(fullAsync);
        return this;
    }

}

