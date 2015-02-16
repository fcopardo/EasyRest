package com.grizzly.rest;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by fpardo on 12/15/14.
 */
public class CachedResponse implements Serializable {

    private String url;
    private Date creationTime;
    private Date expirationTime;
    private boolean overrideTime;

    public CachedResponse(){
        creationTime = Calendar.getInstance(Locale.getDefault()).getTime();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean isOverrideTime() {
        return overrideTime;
    }

    public void setOverrideTime(boolean overrideTime) {
        this.overrideTime = overrideTime;
    }
}
