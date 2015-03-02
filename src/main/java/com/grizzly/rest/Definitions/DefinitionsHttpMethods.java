/*
 * Copyright (c) 2014. Francisco Pardo Baeza
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.grizzly.rest.Definitions;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 26-03-14. Contains basic values and functions related to http connections.
 */
public class DefinitionsHttpMethods {

    /**
     * Constants for calling Http Methods
     */

    public static final HttpMethod METHOD_GET = HttpMethod.GET;
    public static final HttpMethod METHOD_POST = HttpMethod.POST;
    public static final HttpMethod METHOD_PUT = HttpMethod.PUT;
    public static final HttpMethod METHOD_DELETE = HttpMethod.DELETE;


    /**
     * Evaluates if a string represents a valid http method.
     * @param method a String to be evaluated.
     * @return true or false.
     */
    public static boolean isHttpMethod(HttpMethod method) {
        if(DefinitionsHttpMethods.getHttpMethods().contains(method)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the methods currently supported by the framework.
     * @return a List of String with the methods.
     */
    public static List<HttpMethod> getHttpMethods() {
        List<HttpMethod> list = new ArrayList<>();
        list.add(DefinitionsHttpMethods.METHOD_POST);
        list.add(DefinitionsHttpMethods.METHOD_GET);
        list.add(DefinitionsHttpMethods.METHOD_PUT);
        list.add(DefinitionsHttpMethods.METHOD_DELETE);
        return list;
    }

    /**
     * Returns the http states currently supported.
     * @return a List of int representing the supported http states.
     */
    public static List<Integer> getHttpStates() {
        List<Integer> list = new ArrayList<>();
        list.add(HttpStatus.OK.value());
        list.add(HttpStatus.ACCEPTED.value());
        list.add(HttpStatus.CREATED.value());
        list.add(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return list;
    }
}
