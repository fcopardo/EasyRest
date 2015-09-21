package com.grizzly.rest.Model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fpardo on 9/17/15.
 */
public class DynaBean implements DynamicBean
{
    // and then "other" stuff:
    protected Map<String,Object> other = new HashMap<>();

    public Object get(String name) {
        return other.get(name);
    }

    // "any getter" needed for serialization
    @JsonAnyGetter
    public Map<String,Object> any() {
        return other;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        other.put(name, value);
    }
}