package com.grizzly.rest;

import com.grizzly.rest.Model.sendRestData;

/**
 * Created by Fco Pardo on 8/23/14.
 */
public class EasyRestCall<T extends sendRestData, X> extends GenericRestCall<T, X>  {

    public EasyRestCall(Class<T> entityClass, Class<X> jsonResponseEntityClass){

        super(entityClass, jsonResponseEntityClass);
    }

    public EasyRestCall(Class<T> entityClass, Class<X> jsonResponseEntityClass, int test){

        super(entityClass, jsonResponseEntityClass, test);
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
    }

}

