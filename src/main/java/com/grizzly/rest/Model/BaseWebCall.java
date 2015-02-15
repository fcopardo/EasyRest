package com.grizzly.rest.Model;

/**
 * Created by fpardo on 8/7/14.
 *
 * Base class for making rest calls. It does not contain a body for the request. You
 * can either subclass it and add the corresponding properties to form the body, or
 * implement the methods of the senRestData interface in your POJO.
 */
public class BaseWebCall implements sendRestData {

    /**
     * Class members.
     * myRequestHeaders is a org.springframework.http.HttpHeaders instance.
     * myUrl is a string for storing the url.
     * myMethod is a String for storing the HTTP method to call. Use the constants in the
     * DefinitionsHttpMethods class.
     */

    private RestContainer restContainer = new RestContainer();

    public RestContainer getRestContainer() {
        return restContainer;
    }

    public void setRestContainer(RestContainer restContainer) {
        this.restContainer = restContainer;
    }
}
