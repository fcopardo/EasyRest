package com.grizzly.rest.Test.TestEntities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by fpardo on 8/7/14.
 */
public class TestString {

    @JsonProperty("my_value")
    private String myValue;

    public String getMyValue() {
        return myValue;
    }

    public void setMyValue(String myValue) {
        this.myValue = myValue;
    }
}
