package com.grizzly.rest;

import java.util.LinkedHashMap;

/**
 * Created by FcoPardo on 3/9/16.
 */
public class LiteCachingStorage {

    private LinkedHashMap<String, Object> cachedRequests = new LinkedHashMap<>();
    private int cachingSize = 100;
    private int cachedRequestAmount = 0;


    public void addRequest(String name, Object entity){
        if(cachedRequestAmount < cachingSize) {
            cachedRequests.put(name, entity);
        }
        else{
            for(String s: cachedRequests.keySet()){
                cachedRequests.remove(s);
                break;
            }
        }
        cachedRequestAmount++;
    }

    public Object getRequest(String name){
        if (cachedRequests.containsKey(name)) return cachedRequests.get(name);
        return null;
    }

    public boolean isCachedRequest(String name){
        return cachedRequests.containsKey(name);
    }

    public int getCachingSize() {
        return cachingSize;
    }

    public void setCachingSize(int cachingSize) {
        this.cachingSize = cachingSize;
    }
}
