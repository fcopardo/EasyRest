package com.grizzly.rest;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by fpardo on 12/15/14.
 */
class LocalCache implements Serializable{

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

    public boolean isResponseCached(String url){
        if(requests.containsKey(url)){
            if(requests.get(url).isOverrideTime()){
                return true;
            }
            else{
                if(requests.get(url).getExpirationTime().after(Calendar.getInstance(Locale.getDefault()).getTime())){
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String getRequest(String url){
        return requests.get(url).getUrl();
    }

    public void deleteRequest(String url){
        this.requests.remove(url);
    }
}
