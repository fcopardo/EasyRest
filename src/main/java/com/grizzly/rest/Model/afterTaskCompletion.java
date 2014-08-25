package com.grizzly.rest.Model;

/**
 * This interface allows any class to receive the results of a EasyRestCall/GenericRestCall asynchronously.
 * Created by Fco Pardo on 8/23/14.
 */
public interface afterTaskCompletion<T> {

    /**
     * Implement the desired behavior after receiving the rest call result.
     * @param result
     */
    void onTaskCompleted(T result);
}
