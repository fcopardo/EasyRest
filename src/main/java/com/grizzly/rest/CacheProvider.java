package com.grizzly.rest;

/**
 * Created by FcoPardo on 2/16/15.
 */
interface CacheProvider {
    <T, X, M> boolean setCache(GenericRestCall<T, X, M> myRestCall, Class<X> responseClass, Class<T> entityClass, Class<M> errorBodyClass);
}
