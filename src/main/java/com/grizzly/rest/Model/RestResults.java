package com.grizzly.rest.Model;

/**
 * Created by FcoPardo on 9/21/15.
 */
public class RestResults<T> {

    private T resultEntity;
    private int status;
    private boolean successful;

    public RestResults(){

    }

    public T getResultEntity() {
        return resultEntity;
    }

    public RestResults<T> setResultEntity(T resultEntity) {
        this.resultEntity = resultEntity;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public RestResults<T> setStatus(int status) {
        this.status = status;
        return this;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public RestResults<T> setSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }
}
