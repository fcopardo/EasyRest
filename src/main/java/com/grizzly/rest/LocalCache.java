package com.grizzly.rest;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by fpardo on 12/15/14.
 */
public class LocalCache implements Serializable{

    private HashMap<String, CachedResponse> requests = new HashMap<>();

    public HashMap<String, CachedResponse> getRequests() {
        return requests;
    }

    public void setRequests(HashMap<String, CachedResponse> requests) {
        this.requests = requests;
    }

    public void persistRequest(String url){
        CachedResponse request = new CachedResponse();
        request.setUrl(url);
        this.requests.put(url,request);
    }

    public void deleteRequest(String url){
        this.requests.remove(url);
    }
}
