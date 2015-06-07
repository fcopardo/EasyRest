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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grizzly.rest.Definitions.DefinitionsHttpMethods;
import com.grizzly.rest.Model.*;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Rest class based on the Spring RestTemplate. Allows to send T objects, and retrieves a X result. All classes
 * should be annotated to allow the proper serialization and deserialization. Headers, URL an method must be provided using the setters.
 *
 * @author Fco Pardo
 */
public class GenericRestCall<T, X> extends AsyncTask<Void, Void, Boolean> {

    /**
     * Constants for method calls
     */
    public static final String CONTENT_TYPE = "content_type";
    public static final String JSON = "application/json";
    public static final String XML = "application/xml";

    private static final int ERROR = 1;
    private static final int SERVER_ERROR = 2;
    private static final int CLIENT_ERROR = 3;

    /**
     * Class members
     * T: the Entity representing the data to be sent.
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
    private HttpStatus responseStatus = HttpStatus.I_AM_A_TEAPOT;
    private boolean result = false;
    private HttpMethod fixedMethod;
    //private boolean noReturn = false;

    private afterTaskCompletion<X> taskCompletion;
    private afterTaskFailure<X> taskFailure;
    private afterServerTaskFailure<X> serverTaskFailure;
    private afterClientTaskFailure<X> clientTaskFailure;
    private com.grizzly.rest.Model.commonTasks commonTasks;

    private int errorType = 0;

    private Activity activity;
    private String waitingMessage;
    private ProgressDialog pd = null;
    private Exception failure;
    private HttpServerErrorException serverFailure;
    private HttpClientErrorException clientFailure;
    private ResourceAccessException connectionException;
    private boolean bodyless = false;
    private Context context = null;
    private String cachedFileName = "";
    private boolean enableCache = true;
    private CacheProvider cacheProvider = null;
    private long cacheTime = 899999;
    private boolean reprocessWhenRefreshing = false;
    private boolean automaticCacheRefresh = false;

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
            //restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            restTemplate.setRequestFactory(new OkHttpRequestFactory());
            ((OkHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(60000);
            ((OkHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(60000);
        }
        restTemplate.setRequestFactory(new OkHttpRequestFactory());
        ((OkHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(60000);
        ((OkHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(60000);
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
     * Setter for the request's timeout
     */
    public void setTimeOut(int miliseconds){
        try{
            ((OkHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(miliseconds);
            ((OkHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(miliseconds);
        }
        catch(ClassCastException e){
            System.out.println("The factory used wasn't OkHttp");
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

    public HttpStatus getResponseStatus() {
        return responseStatus;
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


    public void addUrlParams(Map<String, Object> urlParameters) {

        if(urlParameters != null && !urlParameters.isEmpty()){
            String customUrl = getUrl();
            StringBuilder builder = new StringBuilder();

            String separator = "?";

            builder.append(customUrl);
            for(String key: urlParameters.keySet()){
                Object value = urlParameters.get(key);

                if(value.toString().contains(" ")){
                    value = value.toString().replace(" ", "+");
                }

                builder.append(separator);
                builder.append(key);
                builder.append("=");
                builder.append(value);

                separator = "&";
            }
            setUrl(builder.toString());
        }
    }

    /**
     * Sets the URL to be used.
     * @param Url the URL of the rest call.
     */
    public void setUrl(String Url) {
        url = Url;
    }

    public String getUrl(){
        return getURI().toString();
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

    /**
     * Interface. Allows to attach a body of code to be executed after a successful rest call.
     * @param task a class implementing the afterTaskCompletion interface.
     */
    public void setTaskCompletion(afterTaskCompletion task){
        this.taskCompletion = task;
    }

    /**
     * Interface. Allows to attach a body of code to be executed after a failed rest call.
     * @param taskFailure a class implementing the afterTaskFailure interface.
     */
    public void setTaskFailure(afterTaskFailure taskFailure) {
        this.taskFailure = taskFailure;
    }

    /**
     * Interface to be executed when a server error occurs.
     * @param serverTaskFailure an instance of the afterServerTaskFailure interface
     */
    public void setServerTaskFailure(afterServerTaskFailure<X> serverTaskFailure) {
        this.serverTaskFailure = serverTaskFailure;
    }

    /**
     * Interface to be executed when a client error arises.
     * @param clientTaskFailure an instance of the afterClientTaskFailure interface
     */
    public void setClientTaskFailure(afterClientTaskFailure<X> clientTaskFailure) {
        this.clientTaskFailure = clientTaskFailure;
    }

    /**
     * Interface to be executed after all processes are finalized, no matter the result.
     * @param commonTasks an instance of the commonTasks interface.
     */
    public void setCommonTasks(com.grizzly.rest.Model.commonTasks commonTasks) {
        this.commonTasks = commonTasks;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String getWaitingMessage() {
        return waitingMessage;
    }

    public void setWaitingMessage(String waitingMessage) {
        this.waitingMessage = waitingMessage;
    }

    /**
     * Allows to set the "bodyless" argument. If true, a post request can be sent without a body.
     * @param bol the value to set.
     */
    public void setBodyless(boolean bol){
        bodyless = bol;
    }

    /**
     * Returns the "bodyless" state.
     * @return
     */
    public boolean isBodyless(){
        return bodyless;
    }

    private Context getContext() {
        if(context == null){
            context = context.getApplicationContext();
        }
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Process the response of the rest call and assigns the body to the responseEntity.
     * @param response a valid response.
     * @return true or false.
     */
    private boolean processResponseWithData(ResponseEntity<X> response){
        responseStatus = response.getStatusCode();
        this.setResponseHeaders(response.getHeaders());
        if(!response.getBody().equals(null)) {
            jsonResponseEntity = response.getBody();

            if(context != null && enableCache){
                createSolidCache();
            }
        }
        return true;
    }

    /**
     * Process the response of the rest call.
     * @param response a valid response.
     * @return true or false.
     */
    private boolean processResponseWithouthData(ResponseEntity<X> response){
        responseStatus = response.getStatusCode();
        this.setResponseHeaders(response.getHeaders());
        return true;
    }

    void setCachedFileName(String s){

        if(s.contains(getContext().getCacheDir().getAbsolutePath() + File.separator + "EasyRest" + File.separator
                + jsonResponseEntityClass.getSimpleName())){
            cachedFileName = s;
        }
        else{
            cachedFileName = getContext().getCacheDir().getAbsolutePath() + File.separator + "EasyRest" + File.separator
                    + jsonResponseEntityClass.getSimpleName()+s;
        }

    }

    String getCachedFileName(){

        if(cachedFileName.isEmpty() || cachedFileName.equalsIgnoreCase("")){

            String fileName = getContext().getCacheDir().getAbsolutePath() + File.separator + "EasyRest" + File.separator
                    + jsonResponseEntityClass.getSimpleName()
                    +getURI().getAuthority()+getURI().getPath().replace("/", "_")+getURI().getQuery();

            return fileName;
        }
        System.out.println("CACHE: "+cachedFileName);
        return cachedFileName;
    }

    private void createSolidCache(){

        ObjectMapper mapper = new ObjectMapper();

        try {
            File dir = new File(getContext().getCacheDir().getAbsolutePath() + File.separator + "EasyRest");
            dir.mkdir();
            File f = new File(getCachedFileName());
            mapper.writeValue(f, jsonResponseEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean getFromSolidCache()
    {
        ObjectMapper mapper = new ObjectMapper();

        if(cacheProvider!=null){
            cacheProvider.setCache(this, jsonResponseEntityClass, entityClass);
        }

        try {
            File dir = new File(getContext().getCacheDir().getAbsolutePath() + File.separator + "EasyRest");
            dir.mkdir();
            File f = new File(getCachedFileName());
            if(f.exists()){
                jsonResponseEntity = mapper.readValue(f, jsonResponseEntityClass);
                this.responseStatus = HttpStatus.OK;
                return true;
            }
            System.out.println("EasyRest - Cache Failure - FileName: "+ getCachedFileName());
        } catch (JsonGenerationException e) {

            this.failure = e;
            e.printStackTrace();

        } catch (JsonMappingException e) {

            this.failure = e;
            e.printStackTrace();

        } catch (IOException e) {

            this.failure = e;
            e.printStackTrace();

        }
        catch(NullPointerException e){
            this.failure = e;
            e.printStackTrace();
        }
        return false;

    }

    public void isCacheEnabled(boolean bol){
        enableCache = bol;
    }

    void setCacheProvider(CacheProvider provider){
        cacheProvider = provider;
    }
    
    public void setCacheTime(Long time){
        cacheTime = time;
    }

    public void setReprocessWhenRefreshing(boolean reprocessWhenRefreshing) {
        this.reprocessWhenRefreshing = reprocessWhenRefreshing;
    }

    public void setAutomaticCacheRefresh(boolean automaticCacheRefresh) {
        this.automaticCacheRefresh = automaticCacheRefresh;
    }

    /**
     * Post call. Sends T in J form to retrieve a X result.
     */
    private void doPost() {

        try {

            HttpEntity<?> requestEntity;
            if(!bodyless){
                requestEntity = new HttpEntity<Object>(entity, requestHeaders);
            }
            else{
                requestEntity = new HttpEntity<Object>(requestHeaders);
            }

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            try {

                if(jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())){
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
                    result = this.processResponseWithouthData(response);
                }
                else{

                    ResponseEntity<X> response = null;

                    if(context !=null){
                        File f = new File(getCachedFileName());
                        if(f.exists() && ((enableCache && Calendar.getInstance(Locale.getDefault()).getTimeInMillis()-f.lastModified()<=cacheTime) || !EasyRest.checkConnectivity(getContext()))) {
                            getFromSolidCache();
                            if(this.automaticCacheRefresh)this.createDelayedCall(reprocessWhenRefreshing);
                            result = true;
                        }
                        else{
                            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, jsonResponseEntityClass);
                            result = this.processResponseWithData(response);
                        }
                    }
                    else{
                        response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, jsonResponseEntityClass);
                        result = this.processResponseWithData(response);
                    }
                }
            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                this.responseStatus = e.getStatusCode();
                System.out.println("BAD:" + e.getResponseBodyAsString());
                failure = e;
                System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                e.printStackTrace();
                this.result = false;
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpClientErrorException.class.getCanonicalName())){
                    errorType = CLIENT_ERROR;
                    clientFailure = (HttpClientErrorException) e;
                }
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpServerErrorException.class.getCanonicalName())){
                    errorType = SERVER_ERROR;
                    serverFailure = (HttpServerErrorException) e;
                }
            }
            catch(org.springframework.web.client.ResourceAccessException e){
                failure = e;
                connectionException = e;
                errorType = ERROR;
                System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                e.printStackTrace();
            }
        } catch (Exception e) {
            failure = e;
            System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
            System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
            System.out.println(" in the url " + url);
            e.printStackTrace();
            this.result = false;
            errorType = ERROR;
        }

    }

    /**
     * Get call. It doesn't send anything, but retrieves X.
     */
    private void doGet() {

        try {

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            try {
                HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

                if (jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Void.class);
                    result = this.processResponseWithouthData(response);
                } else {
                    ResponseEntity<X> response = null;

                    if (context != null) {
                        File f = new File(getCachedFileName());

                        if(f.exists() && ((enableCache && Calendar.getInstance(Locale.getDefault()).getTimeInMillis()-f.lastModified()<=cacheTime) || !EasyRest.checkConnectivity(getContext()))) {
                            getFromSolidCache();
                            if(this.automaticCacheRefresh)this.createDelayedCall(reprocessWhenRefreshing);
                            result = true;
                            System.out.println("EasyRest - We got from cache!");
                        } else {
                            /*if (entityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) {
                                response = restTemplate.exchange(url, HttpMethod.GET, null, jsonResponseEntityClass);
                            } else {
                                response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, jsonResponseEntityClass);
                            }*/
                            response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, jsonResponseEntityClass);
                            result = this.processResponseWithData(response);
                            System.out.println("EasyRest - We got from service, cache failed!");
                        }
                    } else {
                        response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, jsonResponseEntityClass);
                        result = this.processResponseWithData(response);
                        System.out.println("EasyRest - We got from service, we had no cache!");
                    }
                }

            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e ) {
                this.responseStatus = e.getStatusCode();
                System.out.println("BAD:" + e.getResponseBodyAsString());
                failure = e;
                System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                e.printStackTrace();
                this.result = false;
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpClientErrorException.class.getCanonicalName())){
                    errorType = CLIENT_ERROR;
                    clientFailure = (HttpClientErrorException) e;
                }
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpServerErrorException.class.getCanonicalName())){
                    errorType = SERVER_ERROR;
                    serverFailure = (HttpServerErrorException) e;
                }
            }
        } catch (Exception e) {
            failure = e;
            System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
            System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
            System.out.println(" in the url " + url);
            e.printStackTrace();
            this.result = false;
            errorType = ERROR;
        }
    }

    /**
     * Delete call. Sends T to retrieve a C result.
     */
    private void doDelete() {

        try {

            HttpEntity<?> requestEntity = new HttpEntity<Object>(entity, requestHeaders);
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            try {

                if(jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())){
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
                    result = this.processResponseWithouthData(response);
                }
                else{
                    ResponseEntity<X> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, jsonResponseEntityClass);
                    result = this.processResponseWithData(response);
                }
            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                this.responseStatus = e.getStatusCode();
                System.out.println("BAD:" + e.getResponseBodyAsString());
                failure = e;
                System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                e.printStackTrace();
                this.result = false;
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpClientErrorException.class.getCanonicalName())){
                    errorType = CLIENT_ERROR;
                    clientFailure = (HttpClientErrorException) e;
                }
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpServerErrorException.class.getCanonicalName())){
                    errorType = SERVER_ERROR;
                    serverFailure = (HttpServerErrorException) e;
                }
            }
        } catch (Exception e) {
            failure = e;
            System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
            System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
            System.out.println(" in the url " + url);
            e.printStackTrace();
            this.result = false;
            errorType = ERROR;
        }

    }

    /**
     * Delete call. Sends a String and retrieves another String.
     *
     * @param singleArgument
     */
    private void doDelete(String singleArgument) {

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
            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                this.responseStatus = e.getStatusCode();
                System.out.println("BAD:" + e.getResponseBodyAsString());
                failure = e;
                System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                e.printStackTrace();
                this.result = false;
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpClientErrorException.class.getCanonicalName())){
                    errorType = CLIENT_ERROR;
                    clientFailure = (HttpClientErrorException) e;
                }
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpServerErrorException.class.getCanonicalName())){
                    errorType = SERVER_ERROR;
                    serverFailure = (HttpServerErrorException) e;
                }
            }
        } catch (Exception e) {
            failure = e;
            System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
            System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
            System.out.println(" in the url " + url);
            e.printStackTrace();
            this.result = false;
            errorType = ERROR;
        }

    }

    /**
     * Put call. Sends T in J form to retrieve a X result.
     */
    private void doPut() {

        try {

            HttpEntity<?> requestEntity;
            if(!bodyless){
                requestEntity = new HttpEntity<Object>(entity, requestHeaders);
            }
            else{
                requestEntity = new HttpEntity<Object>(requestHeaders);
            }

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(new MappingJackson2HttpMessageConverter());
            restTemplate.setMessageConverters(messageConverters);

            try {
                if(jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())){
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
                    result = this.processResponseWithouthData(response);
                }
                else{

                    ResponseEntity<X> response = null;

                    if(context !=null){
                        File f = new File(getCachedFileName());
                        if(f.exists() && ((enableCache && Calendar.getInstance(Locale.getDefault()).getTimeInMillis()-f.lastModified()<=cacheTime) || !EasyRest.checkConnectivity(getContext()))) {
                            getFromSolidCache();
                            if(this.automaticCacheRefresh)this.createDelayedCall(reprocessWhenRefreshing);
                            result = true;
                        }
                        else{
                            response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, jsonResponseEntityClass);
                            result = this.processResponseWithData(response);
                        }
                    }
                    else{
                        response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, jsonResponseEntityClass);
                        result = this.processResponseWithData(response);
                    }
                }
            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                this.responseStatus = e.getStatusCode();
                System.out.println("BAD:" + e.getResponseBodyAsString());
                failure = e;
                System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                e.printStackTrace();
                this.result = false;
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpClientErrorException.class.getCanonicalName())){
                    errorType = CLIENT_ERROR;
                    clientFailure = (HttpClientErrorException) e;
                }
                if(e.getClass().getCanonicalName().equalsIgnoreCase(HttpServerErrorException.class.getCanonicalName())){
                    errorType = SERVER_ERROR;
                    serverFailure = (HttpServerErrorException) e;
                }
            }
        } catch (Exception e) {
            failure = e;
            System.out.println("The error was caused by the body "+entityClass.getCanonicalName());
            System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
            System.out.println(" in the url " + url);
            e.printStackTrace();
            this.result = false;
            errorType = ERROR;
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

        if(activity != null){
            if(waitingMessage == null || waitingMessage.trim().equalsIgnoreCase("")){
                waitingMessage = activity.getString(R.string.waiting_message);
            }
            pd = new ProgressDialog(activity);
            pd.setTitle(activity.getString(R.string.loading_data));
            pd.setMessage(waitingMessage);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

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
            this.doDelete();
        }

        if (this.getMethodToCall()==HttpMethod.PUT) {
            this.doPut();
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

        if(pd != null) {
            pd.dismiss();
        }

        this.result = result.booleanValue();

        if(context != null && enableCache){
            getFromSolidCache();
        }

        if(result){
            if(taskCompletion != null){
                taskCompletion.onTaskCompleted(jsonResponseEntity);
            }
        }
        else{

            if(context != null && taskCompletion != null && enableCache){
                //TODO: create a more generic naming approach
                if(getFromSolidCache()){
                    taskCompletion.onTaskCompleted(jsonResponseEntity);
                }
                else{
                    errorExecution();
                }
            }
            else{
                errorExecution();
            }
        }
        if(commonTasks!=null) commonTasks.performCommonTask();
        context = null;
    }

    private void errorExecution(){
        boolean executed = false;

        if(serverTaskFailure != null && !executed){
            try {
                if(jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())){
                    serverTaskFailure.onServerTaskFailed(null, serverFailure);
                }
                else{
                    serverTaskFailure.onServerTaskFailed(jsonResponseEntityClass.newInstance(), serverFailure);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            }
            executed = true;
        }

        if(clientTaskFailure != null && !executed){
            try {
                if(jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())){
                    clientTaskFailure.onClientTaskFailed(null, clientFailure);
                }
                else{
                    clientTaskFailure.onClientTaskFailed(jsonResponseEntityClass.newInstance(), clientFailure);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            }
            executed = true;
        }

        if(taskFailure != null && !executed){
            try {
                if(jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())){
                    taskFailure.onTaskFailed(null, failure);
                }
                else{
                    taskFailure.onTaskFailed(jsonResponseEntityClass.newInstance(), failure);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            }
            executed = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void createDelayedCall(boolean enableDelayedReprocess){
        GenericRestCall<T, X> delayedCall = new GenericRestCall(entityClass, jsonResponseEntityClass);
        delayedCall.setMethodToCall(this.methodToCall);
        delayedCall.setUrl(this.getUrl());
        delayedCall.setRequestHeaders(this.getRequestHeaders());
        if(entity!=null && !entityClass.getClass().getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) delayedCall.setEntity(this.entity);
        delayedCall.setCacheTime(0L);
        if(!enableDelayedReprocess){
            delayedCall.setTaskCompletion(null);
            delayedCall.setTaskFailure(null);
            delayedCall.setClientTaskFailure(null);
            delayedCall.setServerTaskFailure(null);
            delayedCall.setActivity(null);
        }
        else{
            if(taskCompletion!=null) delayedCall.setTaskCompletion(this.taskCompletion);
            if(taskFailure!=null) delayedCall.setTaskFailure(this.taskFailure);
            if(clientTaskFailure!=null) delayedCall.setClientTaskFailure(this.clientTaskFailure);
            if(serverTaskFailure!=null) delayedCall.setServerTaskFailure(this.serverTaskFailure);
            if(activity!=null) delayedCall.setActivity(this.activity);
        }
        if(getContext()!=null)delayedCall.setContext(context.getApplicationContext());
        delayedCall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void execute(boolean asynchronously){
        if(asynchronously){
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else{
            try {
                this.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
