package com.grizzly.rest.Model;

import org.springframework.http.HttpStatus;

/**
 * Created by FcoPardo on 6/7/15.
 */
public interface commonTasks {
    void performCommonTask(boolean result, HttpStatus statusCode);
}
