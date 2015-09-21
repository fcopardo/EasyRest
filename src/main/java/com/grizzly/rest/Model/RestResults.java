package com.grizzly.rest.Model;

/**
 * Created by FcoPardo on 9/21/15.
 */
public class RestResults<T> {

    private T subscriberEntity;
    private int status;
    private boolean successful;

    public RestResults(){

    }

    public T getSubscriberEntity() {
        return subscriberEntity;
    }

    public void setSubscriberEntity(T subscriberEntity) {
        this.subscriberEntity = subscriberEntity;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
