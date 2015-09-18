package com.grizzly.rest.Model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.Map;

/**
 * Interface model for dynamic java beans. This require a HashMap<String, Object> member into the implemented class,
 * or some kind of accessor to one.
 * Created by fpardo on 9/17/15.
 */
public interface DynamicBean {

    Object get(String name);

    @JsonAnyGetter
    public Map<String,Object> any();

    @JsonAnySetter
    public void set(String name, Object value);

}
