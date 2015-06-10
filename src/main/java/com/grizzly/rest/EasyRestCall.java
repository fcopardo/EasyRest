package com.grizzly.rest;

import com.grizzly.rest.Model.sendRestData;

/**
 * Created by Fco Pardo on 8/23/14.
 */
public class EasyRestCall<T extends sendRestData, X, M> extends GenericRestCall<T, X, M>  {


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
    public void setEntity(T entity) {

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

        this.setUrl(entity.getRestContainer().getRequestUrl());
        this.setMethodToCall(entity.getRestContainer().getMyHttpMethod());
        
        if(entity.getRestContainer().isCacheEnabled() && entity.getRestContainer().getCacheTime()>0L){
            this.isCacheEnabled(true);
            this.setCacheTime(entity.getRestContainer().getCacheTime());
        }
    }

}

