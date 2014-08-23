/*
 * Copyright (c) 2014. Francisco Pardo Baeza.
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
package com.grizzly.rest;

import android.os.AsyncTask;
import android.os.Build;
import com.grizzly.rest.Definitions.DefinitionsHttpMethods;
import com.grizzly.rest.Model.afterTaskCompletion;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rest class based on the Spring RestTemplate. Allows to send T objects, and retrieves a X result. All classes
 * should be annotated to allow the proper serialization and deserialization. Headers, URL an method must be provided using the setters.
 *
 * @author
 */
public class GenericRestCall<T, X> extends AsyncTask<Void, Void, Boolean> {

    /**
     * Constants for method calls
     */
    public static final String CONTENT_TYPE = "content_type";
    public static final String JSON = "application/json";
    public static final String XML = "application/xml";

    /**
     * Class members
     * T: the Ormlite.Entity representing the data to be sent.
     * X: The Json.Entity to be returned.
     */
    private Class<T> entityClass;
    private Class<X> jsonResponseEntityClass;
    private T entity;
    private X jsonResponseEntity;
    private String singleArgument;
    private String url = "";
    private RestTemplate restTemplate = new RestTemplate();
    private HttpMethod methodToCall;
    private HttpHeaders requestHeaders = new HttpHeaders();
    private HttpHeaders responseHeaders;
    private boolean result = false;
    private HttpMethod fixedMethod;
    private boolean noReturn = false;
    private afterTaskCompletion taskCompletion;

    /**
     * Base constructor.
     *
     * @param EntityClass
     * @param JsonResponseEntityClass
     */
    public GenericRestCall(Class<T> EntityClass, Class<X> JsonResponseEntityClass) {
        this.entityClass = EntityClass;
        this.jsonResponseEntityClass = JsonResponseEntityClass;
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        /**
         * There is a bug in Jelly Bean regarding the HTTPUrlConnection class. This forces the restTemplate to use
         * the apache classes instead in all Jelly Bean builds, and lets restTemplate to choose freely in
         * all the other versions.
         */
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <=18) {
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        }
    }

    /**
     * Base constructor for testing.
     *
     * @param EntityClass
     * @param JsonResponseEntityClass
     */
    public GenericRestCall(Class<T> EntityClass, Class<X> JsonResponseEntityClass, int test) {
        this.entityClass = EntityClass;
        this.jsonResponseEntityClass = JsonResponseEntityClass;
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    /**
     * Base constructor for petitions with empty return
     *
     * @param EntityClass
     */
    public GenericRestCall(Class<T> EntityClass, HttpMethod Method) {
        this.entityClass = EntityClass;
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        /**
         * There is a bug in Jelly Bean regarding the HTTPUrlConnection class. This forces the restTemplate to use
         * the apache classes instead in all Jelly Bean builds, and lets restTemplate to choose freely in
         * all the other versions.
         */
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <=18) {
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        }
        if(DefinitionsHttpMethods.isHttpMethod(Method)) {
            fixedMethod = methodToCall = Method;
        }
    }

    /**
     * Returns the argument entity.
     * @return a subclass of BaseModel
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Sets the argument entity.
     * @param entity a class implementing sendRestData, to be sent in the request.
     */
    public void setEntity(T entity) {
        this.entity = entity;
    }

    /**
     * Returns the response of the rest call, in X form.
     * @return an X instance of the rest call response.
     */
    public X getJsonResponseEntity() {
        return jsonResponseEntity;
    }

    /**
     * Returns the response of the rest call, in F form. F is defined in the method call.
     * @param objectClass The class to be returned.
     * @param <F> Any class. It should match the expected return type.
     * @return The response in F class.
     */
    public <F> F getJsonResponseEntity(Class<F> objectClass) {
        return (F)jsonResponseEntity;
    }

    /**
     * Sets the response entity.
     * @param jsonResponseEntity an instance of X.
     */
    public void setJsonResponseEntity(X jsonResponseEntity) {
        this.jsonResponseEntity = jsonResponseEntity;
    }

    /**
     * Returns the Http headers to be used.
     * @return an instance of HttpHeaders.
     */
    public HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Sets the Http headers to be used.
     * @param requestHeaders an instance of HttpHeaders.
     */
    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    /**
     * Returns the headers from the response.
     * @return an HttpHeaders instance from the request response.
     */
    public HttpHeaders getResponseHeaders() {
        if(responseHeaders==null) {
            responseHeaders = new HttpHeaders();
        }
        return responseHeaders;
    }

    private void setResponseHeaders(HttpHeaders responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Returns the REST method to be called when the service call is executed.
     * @return a String with the method.
     */
    public HttpMethod getMethodToCall() {
        return methodToCall;
    }

    public boolean isResult() {
        return result;
    }

    public String getSingleArgument() {
        return singleArgument;
    }

    public void setSingleArgument(String singleArgument) {
        this.singleArgument = singleArgument;
    }

    /**
     * Allows to set the Http method to call. If a invalid or unsupported method is passed, it will be ignored.
     *
     * @param MethodToCall a valid http method.
     */
    public void setMethodToCall(HttpMethod MethodToCall) {

        if(fixedMethod == null){

            if(DefinitionsHttpMethods.isHttpMethod(MethodToCall)) {
                this.methodToCall = MethodToCall;
            }
        }
        else {
            methodToCall = fixedMethod;
        }
    }

    /**
     * Sets the URL to be used.
     * @param Url the URL of the rest call.
     */
    public void setUrl(String Url) {
        url = Url;
    }

    private URI getURI() {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri;
    }

    public void setTaskCompletion(afterTaskCompletion task){
        this.taskCompletion = task;
    }

    /**
     * Process the response of the rest call and assigns the body to the responseEntity.
     * @param response a valid response.
     * @return true or false.
     */
    private boolean processResponseWithData(ResponseEntity<X> response){
        HttpStatus status = response.getStatusCode();
        this.setResponseHeaders(response.getHeaders());
        if(DefinitionsHttpMethods.getHttpStates().contains(status.value())) {
            if(!response.getBody().equals(null)) {
                jsonResponseEntity = response.getBody();
            }
            return true;
        }
        return false;
    }

    /**
     * Process the response of the rest call.
     * @param response a valid response.
     * @return true or false.
     */
    private boolean processResponseWithouthData(ResponseEntity<X> response){
        HttpStatus status = response.getStatusCode();
        this.setResponseHeaders(response.getHeaders());
        if(DefinitionsHttpMethods.getHttpStates().contains(status.value())) {
            return true;
        }
        return false;
    }

    /**
     * Post call. Sends T in J form to retrieve a X result.
     */
    public void doPost() {

        try {

            HttpEntity<?> requestEntity = new HttpEntity<Object>(entity, requestHeaders);
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            try {
                ResponseEntity<X> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, jsonResponseEntityClass);
                result = this.processResponseWithData(response);
            } catch (Exception e) {
                e.printStackTrace();
                this.result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.result = false;
        }

    }

    /**
     * Get call. It doesn't send anything, but retrieves X.
     */
    public void doGet() {

        try {

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            try {
                HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
                ResponseEntity<X> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, jsonResponseEntityClass);



                result = this.processResponseWithData(response);
            } catch (Exception e) {
                e.printStackTrace();
                this.result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.result = false;
        }

    }

    /**
     * Delete call. Sends T to retrieve a C result.
     */
    public void doDelete() {

        try {

            HttpEntity<?> requestEntity = new HttpEntity<Object>(entity, requestHeaders);
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            try {

                ResponseEntity<X> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, jsonResponseEntityClass);

                result = this.processResponseWithData(response);
            } catch (Exception e) {
                e.printStackTrace();
                this.result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.result = false;
        }

    }

    /**
     * Delete call. Sends a String and retrieves another String.
     *
     * @param singleArgument
     */
    public void doDelete(String singleArgument) {

        try {

            HttpEntity<String> requestEntity = new HttpEntity<String>(singleArgument, requestHeaders);
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            /*
             * commented: testing GSON instead of Jackson as a message converter
             * */
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            //messageConverters.create(new GsonHttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            try {

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);

                HttpStatus status = response.getStatusCode();
                if (status == HttpStatus.OK || status == HttpStatus.ACCEPTED || status == HttpStatus.CREATED) {
                    this.result = true;
                } else {
                    this.result = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.result = false;
        }


    }


    /**
     * The following methods belong to the AsyncTask functions, and allows any subclasses to operate directly. To retrieve the boolean value,
     * invoke the Subclass.execute(parameters).get() method.
     */

    /**
     * Put here any calls to tasks to do before the execution of the thread
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // mProgressDialog.show();
    }

    /**
     * Thread execution. Put here the remote calls.
     *
     * @param params
     * @return
     */
    @Override
    protected Boolean doInBackground(Void... params) {

        if (this.getMethodToCall()==DefinitionsHttpMethods.METHOD_POST) {
            this.doPost();
        }

        if (this.getMethodToCall()==DefinitionsHttpMethods.METHOD_GET) {
            this.doGet();
        }

        if (this.getMethodToCall()==DefinitionsHttpMethods.METHOD_DELETE) {
            if (singleArgument.isEmpty()) {
                this.doDelete();
            } else {
                this.doDelete(singleArgument);
            }
        }
        return this.result;
    }


    /**
     * Put here any calls to tasks to do after the execution of the thread.
     * It allows to retrieve the boolean result of the rest call in an asynchronous way.
     *
     * @param result the result of the doInBackground operation.
     */
    @Override
    protected void onPostExecute(Boolean result) {
        // mProgressDialog.dismiss();
        this.result = result.booleanValue();
        if(taskCompletion != null){
            taskCompletion.onTaskCompleted(jsonResponseEntity, jsonResponseEntityClass);
        }
    }

}
