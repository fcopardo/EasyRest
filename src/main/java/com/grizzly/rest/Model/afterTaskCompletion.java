package com.grizzly.rest.Model;

/**
 * Created by Fco Pardo on 8/23/14.
 */
public interface afterTaskCompletion {

    <T> void onTaskCompleted(T result, Class<T> myClass);
}
