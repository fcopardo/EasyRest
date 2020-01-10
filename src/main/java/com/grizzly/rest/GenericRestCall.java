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
import android.util.Log;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grizzly.rest.Definitions.DefinitionsHttpMethods;
import com.grizzly.rest.Model.*;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.*;
import rx.Observable;
import rx.functions.Action1;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Rest class based on the Spring RestTemplate. Allows to send T objects, and retrieves a X result. All classes
 * should be annotated to allow the proper serialization and deserialization. Headers, URL an method must be provided using the setters.
 *
 * @author Fco Pardo
 */
public class GenericRestCall<T, X, M> extends AsyncTask<Void, Void, Boolean> {

    /**
     * Constants for method calls
     */
    public static final String CONTENT_TYPE = "content_type";
    public static final String JSON = "application/json";
    public static final String XML = "application/xml";

    /*private static final int ERROR = 1;
    private static final int SERVER_ERROR = 2;
    private static final int CLIENT_ERROR = 3;*/

    /**
     * Class members
     * T: the Entity representing the data to be sent.
     * X: The Json.Entity to be returned.
     */
    private Class<T> entityClass;
    private Class<X> jsonResponseEntityClass;
    private Class<M> errorResponseEntityClass;
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
    protected HttpMethod fixedMethod;
    private Map<DeserializationFeature, Boolean> deserializationFeatureMap;

    private afterTaskCompletion<X> taskCompletion;
    private afterTaskFailure<X> taskFailure;
    private afterServerTaskFailure<M> serverTaskFailure;
    private afterClientTaskFailure<M> clientTaskFailure;
    private com.grizzly.rest.Model.commonTasks commonTasks;

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
    private String errorResponse = "";

    private List<Action1<RestResults<X>>> mySubscribers;
    private MappingJackson2HttpMessageConverter jacksonConverter;
    private Observable<RestResults<X>> observable;
    private boolean fullAsync = false;

    /**
     * Base constructor.
     *
     * @param EntityClass
     * @param JsonResponseEntityClass
     */
    public GenericRestCall(Class<T> EntityClass, Class<X> JsonResponseEntityClass, Class<M> ErrorResponseEntityClass) {
        this.entityClass = EntityClass;
        this.jsonResponseEntityClass = JsonResponseEntityClass;
        this.errorResponseEntityClass = ErrorResponseEntityClass;
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.setRequestFactory(new OkHttpRequestFactory());
        ((OkHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(60000);
        ((OkHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(60000);
    }

    /**
     * Base constructor for testing.
     *
     * @param EntityClass
     * @param JsonResponseEntityClass
     */
    public GenericRestCall(Class<T> EntityClass, Class<X> JsonResponseEntityClass, Class<M> ErrorResponseEntityClass, int test) {
        this.entityClass = EntityClass;
        this.jsonResponseEntityClass = JsonResponseEntityClass;
        this.errorResponseEntityClass = ErrorResponseEntityClass;
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    /**
     * Setter for the request's timeout
     */
    public GenericRestCall<T, X, M> setTimeOut(int miliseconds) {
        try {
            ((OkHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(miliseconds);
            ((OkHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(miliseconds);
        } catch (ClassCastException e) {
            System.out.println("The factory used wasn't OkHttp");
        }
        return this;
    }

    /**
     * Returns the argument entity.
     *
     * @return a subclass of BaseModel
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Sets the argument entity.
     *
     * @param entity a class implementing sendRestData, to be sent in the request.
     */
    public GenericRestCall<T, X, M> setEntity(T entity) {
        this.entity = entity;
        return this;
    }

    /**
     * Returns the response of the rest call, in X form.
     *
     * @return an X instance of the rest call response.
     */
    public X getJsonResponseEntity() {
        return jsonResponseEntity;
    }

    /**
     * Returns the response of the rest call, in F form. F is defined in the method call.
     *
     * @param objectClass The class to be returned.
     * @param <F>         Any class. It should match the expected return type.
     * @return The response in F class.
     */
    public <F> F getJsonResponseEntity(Class<F> objectClass) {
        return (F) jsonResponseEntity;
    }

    /**
     * Sets the response entity.
     *
     * @param jsonResponseEntity an instance of X.
     */
    public GenericRestCall<T, X, M> setJsonResponseEntity(X jsonResponseEntity) {
        this.jsonResponseEntity = jsonResponseEntity;
        return this;
    }

    public String getErrorResponse(){
        return errorResponse;
    }

    /**
     * Returns the Http headers to be used.
     *
     * @return an instance of HttpHeaders.
     */
    public HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Sets the Http headers to be used.
     *
     * @param requestHeaders an instance of HttpHeaders.
     */
    public GenericRestCall<T, X, M> setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    /**
     * Returns the headers from the response.
     *
     * @return an HttpHeaders instance from the request response.
     */
    public HttpHeaders getResponseHeaders() {
        if (responseHeaders == null) {
            responseHeaders = new HttpHeaders();
        }
        return responseHeaders;
    }

    private void setResponseHeaders(HttpHeaders responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Returns the REST method to be called when the service call is executed.
     *
     * @return a String with the method.
     */
    public HttpMethod getMethodToCall() {
        return methodToCall;
    }

    public Map<DeserializationFeature, Boolean> getdeserializationFeatureMap() {
        if (deserializationFeatureMap == null) deserializationFeatureMap = new HashMap<>();
        return deserializationFeatureMap;
    }

    public GenericRestCall<T, X, M> setdeserializationFeatureMap(Map<DeserializationFeature, Boolean> deserializationFeatureMap) {
        this.deserializationFeatureMap = deserializationFeatureMap;
        return this;
    }

    public GenericRestCall<T, X, M> addDeserializationFeature(DeserializationFeature deserializationFeature, boolean activated) {
        getdeserializationFeatureMap();
        deserializationFeatureMap.put(deserializationFeature, activated);
        return this;
    }

    /*public boolean isResult() {
        return result;
    }

    public String getSingleArgument() {
        return singleArgument;
    }

    public void setSingleArgument(String singleArgument) {
        this.singleArgument = singleArgument;
    }*/

    public HttpStatus getResponseStatus() {
        if (responseStatus == null) responseStatus = HttpStatus.I_AM_A_TEAPOT;
        return responseStatus;
    }

    /**
     * Allows to set the Http method to call. If a invalid or unsupported method is passed, it will be ignored.
     *
     * @param MethodToCall a valid http method.
     */
    public GenericRestCall<T, X, M> setMethodToCall(HttpMethod MethodToCall) {

        if (fixedMethod == null) {

            if (DefinitionsHttpMethods.isHttpMethod(MethodToCall)) {
                this.methodToCall = MethodToCall;
            }
        } else {
            methodToCall = fixedMethod;
        }
        return this;
    }


    public GenericRestCall<T, X, M> addUrlParams(Map<String, Object> urlParameters) {

        if (urlParameters != null && !urlParameters.isEmpty()) {
            String customUrl = getUrl();
            StringBuilder builder = new StringBuilder();

            String separator = "?";

            builder.append(customUrl);
            for (String key : urlParameters.keySet()) {
                Object value = urlParameters.get(key);

                if (value.toString().contains(" ")) {
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
        return this;
    }

    /**
     * Sets the URL to be used.
     *
     * @param Url the URL of the rest call.
     */
    public GenericRestCall<T, X, M> setUrl(String Url) {
        url = Url;
        return this;
    }

    public String getUrl() {
        if(getURI().toString().isEmpty()){
            return url;
        }
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
     *
     * @param task a class implementing the afterTaskCompletion interface.
     */
    public GenericRestCall<T, X, M> setTaskCompletion(afterTaskCompletion task) {
        this.taskCompletion = task;
        return this;
    }

    /**
     * Interface. Allows to attach a body of code to be executed after a failed rest call.
     *
     * @param taskFailure a class implementing the afterTaskFailure interface.
     */
    public GenericRestCall<T, X, M> setTaskFailure(afterTaskFailure taskFailure) {

        this.taskFailure = taskFailure;
        return this;
    }

    /**
     * Interface to be executed when a server error occurs.
     *
     * @param serverTaskFailure an instance of the afterServerTaskFailure interface
     */
    public GenericRestCall<T, X, M> setServerTaskFailure(afterServerTaskFailure<M> serverTaskFailure) {
        this.serverTaskFailure = serverTaskFailure;
        return this;
    }

    /**
     * Interface to be executed when a client error arises.
     *
     * @param clientTaskFailure an instance of the afterClientTaskFailure interface
     */
    public GenericRestCall<T, X, M> setClientTaskFailure(afterClientTaskFailure<M> clientTaskFailure) {
        this.clientTaskFailure = clientTaskFailure;
        return this;
    }

    /**
     * Interface to be executed after all processes are finalized, no matter the result.
     *
     * @param commonTasks an instance of the commonTasks interface.
     */
    public GenericRestCall<T, X, M> setCommonTasks(com.grizzly.rest.Model.commonTasks commonTasks) {
        this.commonTasks = commonTasks;
        return this;
    }

    public GenericRestCall<T, X, M> setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public GenericRestCall<T, X, M> addHeader(String header, String value) {
        this.getRequestHeaders().add(header, value);
        return this;
    }

    public String getWaitingMessage() {
        return waitingMessage;
    }

    public void setWaitingMessage(String waitingMessage) {
        this.waitingMessage = waitingMessage;
    }

    /**
     * Allows to set the "bodyless" argument. If true, a post request can be sent without a body.
     *
     * @param bol the value to set.
     */
    public void setBodyless(boolean bol) {
        bodyless = bol;
    }

    /**
     * Returns the "bodyless" state.
     *
     * @return
     */
    public boolean isBodyless() {
        return bodyless;
    }

    private Context getContext() {
        if (context == null) {
            context = context.getApplicationContext();
        }
        return context;
    }

    public GenericRestCall<T, X, M> setContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     * Process the response of the rest call and assigns the body to the responseEntity.
     *
     * @param response a valid response.
     * @return true or false.
     */
    private boolean processResponseWithData(ResponseEntity<X> response) {
        responseStatus = response.getStatusCode();
        this.setResponseHeaders(response.getHeaders());
        if (!response.getBody().equals(null)) {
            jsonResponseEntity = response.getBody();

            if (context != null && enableCache) {
                createSolidCache();
            }
        }
        return true;
    }

    /**
     * Process the response of the rest call.
     *
     * @param response a valid response.
     * @return true or false.
     */
    private boolean processResponseWithouthData(ResponseEntity<X> response) {
        responseStatus = response.getStatusCode();
        this.setResponseHeaders(response.getHeaders());
        return true;
    }

    void setCachedFileName(String s) {

        if (s.contains(getContext().getCacheDir().getAbsolutePath() + File.separator + "EasyRest" + File.separator
                + jsonResponseEntityClass.getSimpleName())) {
            cachedFileName = s;
        } else {
            cachedFileName = getContext().getCacheDir().getAbsolutePath() + File.separator + "EasyRest" + File.separator
                    + jsonResponseEntityClass.getSimpleName() + s;
        }

    }

    String getCachedFileName() {

        return getCachedFileName(getContext());
    }

    String getCachedFileName(Context context) {

        if (cachedFileName.isEmpty() || cachedFileName.equalsIgnoreCase("")) {

            String queryKey = "";
            try {
                queryKey = EasyRest.getHashOne(getURI().getAuthority() + getURI().getPath().replace("/", "_") + getURI().getQuery());
            } catch (NoSuchAlgorithmException e) {
                queryKey = getURI().getAuthority() + getURI().getPath().replace("/", "_") + getURI().getQuery();
                e.printStackTrace();
            }

            String fileName = context.getCacheDir().getAbsolutePath() + File.separator + "EasyRest" + File.separator
                    + jsonResponseEntityClass.getSimpleName()
                    + queryKey;

            return fileName;
        }
        if (EasyRest.isDebugMode()) System.out.println("CACHE: " + cachedFileName);
        return cachedFileName;
    }

    private void createSolidCache() {

        EasyRest.cacheRequest(getCachedFileName(), jsonResponseEntity);
        class Task implements Runnable{

            public Context context;

            @Override
            public void run() {
                ObjectMapper mapper = new ObjectMapper();

                try {
                    Random random = new Random();
                    int x = random.nextInt();
                    Log.e("EasyRest-Cache", "Starting serialization "+x);
                    File dir = new File(context.getCacheDir().getAbsolutePath() + File.separator + "EasyRest");
                    dir.mkdir();
                    File f = new File(getCachedFileName(context));
                    mapper.writeValue(f, jsonResponseEntity);
                    Log.e("EasyRest-Cache", "Finished serialization "+x);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Task task = new Task();
        task.context = getContext();
        PoolExecutor.getExecutor(true).execute(task);
    }

    private boolean getFromSolidCache() {

        if(EasyRest.getCachedRequest(getCachedFileName())!=null){
            jsonResponseEntity = (X) EasyRest.getCachedRequest(getCachedFileName());
            this.responseStatus = HttpStatus.OK;
            return true;
        }

        ObjectMapper mapper = new ObjectMapper();

        if (cacheProvider != null) {
            cacheProvider.setCache(this, jsonResponseEntityClass, entityClass, errorResponseEntityClass);
        }

        try {
            File dir = new File(getContext().getCacheDir().getAbsolutePath() + File.separator + "EasyRest");
            dir.mkdir();
            File f = new File(getCachedFileName());
            if (f.exists()) {
                jsonResponseEntity = mapper.readValue(f, jsonResponseEntityClass);
                this.responseStatus = HttpStatus.OK;
                EasyRest.cacheRequest(getCachedFileName(), jsonResponseEntity);
                return true;
            }
            if (EasyRest.isDebugMode()) System.out.println("EasyRest - Cache Failure - FileName: " + getCachedFileName());
        } catch (JsonGenerationException e) {

            this.failure = e;
            e.printStackTrace();

        } catch (JsonMappingException e) {

            this.failure = e;
            e.printStackTrace();

        } catch (IOException e) {

            this.failure = e;
            e.printStackTrace();

        } catch (NullPointerException e) {
            this.failure = e;
            e.printStackTrace();
        }
        return false;

    }

    public GenericRestCall<T, X, M> isCacheEnabled(boolean bol) {
        enableCache = bol;
        return this;
    }

    void setCacheProvider(CacheProvider provider) {
        cacheProvider = provider;
    }

    public GenericRestCall<T, X, M> setCacheTime(Long time) {
        cacheTime = time;
        return this;
    }

    public GenericRestCall<T, X, M> setReprocessWhenRefreshing(boolean reprocessWhenRefreshing) {
        this.reprocessWhenRefreshing = reprocessWhenRefreshing;
        return this;
    }

    public GenericRestCall<T, X, M> setAutomaticCacheRefresh(boolean automaticCacheRefresh) {
        this.automaticCacheRefresh = automaticCacheRefresh;
        return this;
    }

    public MappingJackson2HttpMessageConverter getJacksonMapper() {

        if (jacksonConverter == null) {
            jacksonConverter = new MappingJackson2HttpMessageConverter();
            jacksonConverter.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jacksonConverter.getObjectMapper().configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        }

        if (!getdeserializationFeatureMap().isEmpty()) {
            for (DeserializationFeature feature : getdeserializationFeatureMap().keySet()) {
                jacksonConverter.getObjectMapper().configure(feature, getdeserializationFeatureMap().get(feature));
            }
        }
        return jacksonConverter;
    }

    public GenericRestCall<T, X, M> setJacksonMapper(MappingJackson2HttpMessageConverter customConverter) {
        jacksonConverter = customConverter;
        return this;
    }

    public GenericRestCall<T, X, M> addSuccessSubscriber(Action1<RestResults<X>> subscriber) {
        if (mySubscribers == null) mySubscribers = new ArrayList<>();

        mySubscribers.add(subscriber);
        return this;
    }

    public GenericRestCall<T, X, M> deleteSuccessSubscriber(Action1<RestResults<X>> subscriber) {
        if (mySubscribers == null) mySubscribers = new ArrayList<>();
        mySubscribers.remove(subscriber);
        return this;
    }

    public GenericRestCall<T, X, M> setSuccessSubscribers(List<Action1<RestResults<X>>> subscribers) {
        if(mySubscribers != null){
            mySubscribers.addAll(subscribers);
        }else{
            mySubscribers = subscribers;
        }
        return this;
    }

    public boolean isFullAsync() {
        return fullAsync;
    }

    public GenericRestCall<T, X, M> setFullAsync(boolean fullAsync) {
        this.fullAsync = fullAsync;
        return this;
    }


    /**
     * Generic rest call method
     */
    private void doCall() {

        try {

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(getJacksonMapper());
            restTemplate.setMessageConverters(messageConverters);

            try {
                HttpEntity<?> requestEntity;
                if (!entityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) {
                    requestEntity = new HttpEntity<>(entity, requestHeaders);
                } else {
                    requestEntity = new HttpEntity<>(requestHeaders);
                }

                if (jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) {
                    ResponseEntity response = restTemplate.exchange(url, getMethodToCall(), requestEntity, Void.class);
                    result = this.processResponseWithouthData(response);
                } else {
                    ResponseEntity<X> response = null;

                    if (context != null) {
                        File f = new File(getCachedFileName());

                        if (f.exists() && ((enableCache && Calendar.getInstance(Locale.getDefault()).getTimeInMillis() - f.lastModified() <= cacheTime) || !EasyRest.checkConnectivity(getContext()))) {
                            getFromSolidCache();
                            if (this.automaticCacheRefresh) this.createDelayedCall(reprocessWhenRefreshing);
                            result = true;
                            System.out.println("EasyRest - We got from cache!");
                        } else {
                            response = restTemplate.exchange(url, getMethodToCall(), requestEntity, jsonResponseEntityClass);
                            result = this.processResponseWithData(response);
                            if(enableCache){
                                if(!automaticCacheRefresh && cacheTime==0){
                                    System.out.println("EasyRest - We got from service!");
                                }else{
                                    System.out.println("EasyRest - We got from service, cache failed! "+getCachedFileName());
                                }
                            }
                        }
                    } else {
                        response = restTemplate.exchange(url, getMethodToCall(), requestEntity, jsonResponseEntityClass);
                        result = this.processResponseWithData(response);
                        System.out.println("EasyRest - We got from service, we had no cache!");
                    }
                }

            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                this.responseStatus = e.getStatusCode();
                System.out.println("BAD:" + e.getResponseBodyAsString());
                errorResponse = e.getResponseBodyAsString();
                failure = e;
                System.out.println("The error was caused by the body " + entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                System.out.println(" with the response " + errorResponse);
                e.printStackTrace();
                this.result = false;
                if (e.getClass().getCanonicalName().equalsIgnoreCase(HttpClientErrorException.class.getCanonicalName())) {
                    //errorType = CLIENT_ERROR;
                    clientFailure = (HttpClientErrorException) e;
                }
                if (e.getClass().getCanonicalName().equalsIgnoreCase(HttpServerErrorException.class.getCanonicalName())) {
                    //errorType = SERVER_ERROR;
                    serverFailure = (HttpServerErrorException) e;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Post call. Sends T in J form to retrieve a X result.
     */
    private void doPost() {

        try {

            HttpEntity<?> requestEntity;
            if (!bodyless) {
                requestEntity = new HttpEntity<Object>(entity, requestHeaders);
            } else {
                requestEntity = new HttpEntity<Object>(requestHeaders);
            }

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(getJacksonMapper());
            restTemplate.setMessageConverters(messageConverters);

            try {

                if (jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
                    result = this.processResponseWithouthData(response);
                } else {

                    ResponseEntity<X> response = null;

                    if (context != null) {
                        File f = new File(getCachedFileName());
                        if (f.exists() && ((enableCache && Calendar.getInstance(Locale.getDefault()).getTimeInMillis() - f.lastModified() <= cacheTime) || !EasyRest.checkConnectivity(getContext()))) {
                            getFromSolidCache();
                            if (this.automaticCacheRefresh) this.createDelayedCall(reprocessWhenRefreshing);
                            result = true;
                        } else {
                            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, jsonResponseEntityClass);
                            result = this.processResponseWithData(response);
                        }
                    } else {
                        response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, jsonResponseEntityClass);
                        result = this.processResponseWithData(response);
                    }
                }
            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                handleException(e);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                failure = e;
                connectionException = e;
                //errorType = ERROR;
                System.out.println("The error was caused by the body " + entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                e.printStackTrace();
            } catch (org.springframework.http.converter.HttpMessageNotReadableException e) {
                System.out.println("Conversion error the error found " + e.getMessage());
                System.out.println(" the expected response type was " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                e.printStackTrace();
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    /**
     * Get call. It doesn't send anything, but retrieves X.
     */
    private void doGet() {

        try {

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(getJacksonMapper());
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

                        if (f.exists() && ((enableCache && Calendar.getInstance(Locale.getDefault()).getTimeInMillis() - f.lastModified() <= cacheTime) || !EasyRest.checkConnectivity(getContext()))) {
                            getFromSolidCache();
                            if (this.automaticCacheRefresh) this.createDelayedCall(reprocessWhenRefreshing);
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
                            if(enableCache){
                                if(!automaticCacheRefresh && cacheTime==0){
                                    System.out.println("EasyRest - We got from service!");
                                }else{
                                    System.out.println("EasyRest - We got from service, cache failed! "+getCachedFileName());
                                }
                            }
                        }
                    } else {
                        response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, jsonResponseEntityClass);
                        result = this.processResponseWithData(response);
                        System.out.println("EasyRest - We got from service, we had no cache!");
                    }
                }

            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                this.responseStatus = e.getStatusCode();
                System.out.println("BAD:" + e.getResponseBodyAsString());
                errorResponse = e.getResponseBodyAsString();
                failure = e;
                System.out.println("The error was caused by the body " + entityClass.getCanonicalName());
                System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
                System.out.println(" in the url " + url);
                System.out.println(" with the response " + errorResponse);
                e.printStackTrace();
                this.result = false;
                if (e.getClass().getCanonicalName().equalsIgnoreCase(HttpClientErrorException.class.getCanonicalName())) {
                    //errorType = CLIENT_ERROR;
                    clientFailure = (HttpClientErrorException) e;
                }
                if (e.getClass().getCanonicalName().equalsIgnoreCase(HttpServerErrorException.class.getCanonicalName())) {
                    //errorType = SERVER_ERROR;
                    serverFailure = (HttpServerErrorException) e;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Delete call. Sends T to retrieve a C result.
     */
    private void doDelete() {

        try {

            HttpEntity<?> requestEntity = new HttpEntity<Object>(entity, requestHeaders);
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(getJacksonMapper());
            restTemplate.setMessageConverters(messageConverters);

            try {
                if (jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
                    result = this.processResponseWithouthData(response);
                } else {
                    ResponseEntity<X> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, jsonResponseEntityClass);
                    result = this.processResponseWithData(response);
                }
            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                handleException(e);
            }
        } catch (Exception e) {
            handleException(e);
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
            messageConverters.add(getJacksonMapper());
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
                handleException(e);
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    /**
     * Put call. Sends T in J form to retrieve a X result.
     */
    private void doPut() {

        try {

            HttpEntity<?> requestEntity;
            if (!bodyless) {
                requestEntity = new HttpEntity<Object>(entity, requestHeaders);
            } else {
                requestEntity = new HttpEntity<Object>(requestHeaders);
            }

            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            messageConverters.add(getJacksonMapper());
            restTemplate.setMessageConverters(messageConverters);

            try {
                if (jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
                    result = this.processResponseWithouthData(response);
                } else {

                    ResponseEntity<X> response = null;

                    if (context != null) {
                        File f = new File(getCachedFileName());
                        if (f.exists() && ((enableCache && Calendar.getInstance(Locale.getDefault()).getTimeInMillis() - f.lastModified() <= cacheTime) || !EasyRest.checkConnectivity(getContext()))) {
                            getFromSolidCache();
                            if (this.automaticCacheRefresh) this.createDelayedCall(reprocessWhenRefreshing);
                            result = true;
                        } else {
                            response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, jsonResponseEntityClass);
                            result = this.processResponseWithData(response);
                        }
                    } else {
                        response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, jsonResponseEntityClass);
                        result = this.processResponseWithData(response);
                    }
                }
            } catch (org.springframework.web.client.HttpClientErrorException | HttpServerErrorException e) {
                handleException(e);
            }
        } catch (Exception e) {
            handleException(e);
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

        if (activity != null) {
            if (waitingMessage == null || waitingMessage.trim().equalsIgnoreCase("")) {
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

        if (EasyRest.isDebugMode()) {
            Log.e("EasyRest", "Call type:" + getMethodToCall().name());
            Log.e("EasyRest", "Starting time:" + Calendar.getInstance().getTime());
            if (getRequestHeaders() != null) {
                Log.e("EasyRest", "Request Headers");
                Log.e("EasyRest", "URL : " + getUrl());
                for (String s : getRequestHeaders().keySet()) {
                    Log.e("EasyRest", s + ":" + getRequestHeaders().get(s));
                }
            }
        }

        if (this.getMethodToCall() == DefinitionsHttpMethods.METHOD_POST) {
            //this.doPost();
            doCall();
        }

        if (this.getMethodToCall() == DefinitionsHttpMethods.METHOD_GET) {
            //this.doGet();
            doCall();
        }

        if (this.getMethodToCall() == DefinitionsHttpMethods.METHOD_DELETE) {
            this.doDelete();
        }

        if (this.getMethodToCall() == HttpMethod.PUT) {
            this.doPut();
        }

        if (EasyRest.isDebugMode()) {
            Log.e("EasyRest", "Call Time:" + Calendar.getInstance().getTime());
        }

        if(fullAsync){
            afterCall(result);
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

        if (pd != null) {
            pd.dismiss();
        }

        if(!fullAsync) afterCall(result);
    }

    private void afterCall(Boolean result){

        this.result = result.booleanValue();

        if (context != null && enableCache) {
            getFromSolidCache();
        }

        if (responseStatus.value() > 399) result = false;

        if (result) {
            if (EasyRest.isDebugMode()) {
                if (getResponseHeaders() != null) {
                    Log.e("EasyRest", "Response Headers");
                    for (String s : getResponseHeaders().keySet()) {
                        Log.e("EasyRest", s + ":" + getResponseHeaders().get(s));
                    }
                    Log.e("EasyRest", "Here comes your code!");
                }
            }
            if (taskCompletion != null) {
                taskCompletion.onTaskCompleted(jsonResponseEntity);
                notifySubscribers(jsonResponseEntity, true);
            }
        } else {

            if (context != null && taskCompletion != null && enableCache) {
                //TODO: create a more generic naming approach
                if (getFromSolidCache()) {
                    taskCompletion.onTaskCompleted(jsonResponseEntity);
                    notifySubscribers(jsonResponseEntity, true);
                } else {
                    errorExecution();
                }
            } else {
                errorExecution();
            }
        }
        if (commonTasks != null) commonTasks.performCommonTask(result, this.getResponseStatus());
        context = null;

    }

    private void errorExecution() {
        boolean executed = false;

        if (serverTaskFailure != null && !executed && getResponseStatus().value() > 499) {
            serverTaskFailure.onServerTaskFailed(getErrorBody(errorResponseEntityClass, errorResponse), serverFailure);
            executed = true;
        }

        if (clientTaskFailure != null && !executed && getResponseStatus().value() < 499 && getResponseStatus().value() > 399) {
            clientTaskFailure.onClientTaskFailed(getErrorBody(errorResponseEntityClass, errorResponse), clientFailure);
            executed = true;
        }

        if (taskFailure != null && !executed) {
            try {
                if (jsonResponseEntityClass.getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName())) {
                    taskFailure.onTaskFailed(null, failure);
                } else {
                    Log.e("EASYREST", "SAFEGUARD");
                    taskFailure.onTaskFailed(jsonResponseEntityClass.newInstance(), failure);
                }

            } catch (InstantiationException e) {
                try {
                    taskFailure.onTaskFailed(null, failure);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            }
            executed = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void createDelayedCall(boolean enableDelayedReprocess) {
        GenericRestCall<T, X, M> delayedCall = new GenericRestCall(entityClass, jsonResponseEntityClass, errorResponseEntityClass);
        delayedCall.setMethodToCall(this.methodToCall);
        delayedCall.setUrl(this.getUrl());
        delayedCall.setRequestHeaders(this.getRequestHeaders());
        delayedCall.setAutomaticCacheRefresh(false);
        if (entity != null && !entityClass.getClass().getCanonicalName().equalsIgnoreCase(Void.class.getCanonicalName()))
            delayedCall.setEntity(this.entity);
        delayedCall.setCacheTime(0L);
        if (!enableDelayedReprocess) {
            delayedCall.setTaskCompletion(null);
            delayedCall.setTaskFailure(null);
            delayedCall.setClientTaskFailure(null);
            delayedCall.setServerTaskFailure(null);
            delayedCall.setActivity(null);
        } else {
            if (taskCompletion != null) delayedCall.setTaskCompletion(this.taskCompletion);
            if (taskFailure != null) delayedCall.setTaskFailure(this.taskFailure);
            if (clientTaskFailure != null) delayedCall.setClientTaskFailure(this.clientTaskFailure);
            if (serverTaskFailure != null) delayedCall.setServerTaskFailure(this.serverTaskFailure);
            if (activity != null) delayedCall.setActivity(this.activity);
        }
        if (getContext() != null) {
            delayedCall.setContext(context.getApplicationContext());
            delayedCall.isCacheEnabled(true);
        }
        delayedCall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void execute(boolean asynchronously) {
        if (asynchronously) {
            switch (getStatus()){
                case PENDING:
                    this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case RUNNING:
                    break;
                case FINISHED:
                    GenericRestCall<T, X, M> restCall = new GenericRestCall<>(entityClass, jsonResponseEntityClass, errorResponseEntityClass)
                            .setCacheTime(cacheTime)
                            .setAutomaticCacheRefresh(automaticCacheRefresh)
                            .setClientTaskFailure(clientTaskFailure)
                            .setCommonTasks(commonTasks)
                            .setContext(context)
                            .setdeserializationFeatureMap(deserializationFeatureMap)
                            .setEntity(entity)
                            .setFullAsync(fullAsync)
                            .setMethodToCall(methodToCall)
                            .setReprocessWhenRefreshing(reprocessWhenRefreshing)
                            .setRequestHeaders(requestHeaders)
                            .setServerTaskFailure(serverTaskFailure)
                            .setTaskCompletion(taskCompletion)
                            .setTaskFailure(taskFailure)
                            .setUrl(url);
                    if(mySubscribers != null){
                        for(Action1<RestResults<X>> s : mySubscribers){
                            restCall.addSuccessSubscriber(s);
                        }
                    }
                    restCall.execute(fullAsync);
                    break;
            }
        } else {
            try {
                this.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T getErrorBody(Class<T> errorBodyClass, String jsonBody) {
        try {

            //JSONObject jsonObj = new JSONObject(jsonBody.toString());

            T errorBody = errorBodyClass.newInstance();
            ObjectMapper mapper = new ObjectMapper();
            errorBody = mapper.readValue(jsonBody, errorBodyClass);
            return errorBody;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GenericRestCall<T, X, M> getThis() {
        return this;
    }

    private void notifySubscribers(X result, boolean success) {

        if (mySubscribers != null && mySubscribers.size() > 0) {
            RestResults<X> results = new RestResults<>();
            results.setResultEntity(result);
            results.setStatus(this.getResponseStatus().value());
            results.setSuccessful(success);

            rx.Observable<RestResults<X>> observable = rx.Observable.just(results);
            for (Action1<RestResults<X>> subscriber : mySubscribers) {
                observable.subscribe(subscriber);
            }
        }

    }

    private <T extends HttpStatusCodeException> void handleException(T e){
        this.responseStatus = e.getStatusCode();
        if (EasyRest.isDebugMode()) System.out.println("BAD:" + e.getResponseBodyAsString());
        errorResponse = e.getResponseBodyAsString();
        failure = e;
        printError(e);
        this.result = false;
        if (e.getClass().getCanonicalName().equalsIgnoreCase(HttpClientErrorException.class.getCanonicalName())) {
            //errorType = CLIENT_ERROR;
            clientFailure = (HttpClientErrorException) e;
        }
        if (e.getClass().getCanonicalName().equalsIgnoreCase(HttpServerErrorException.class.getCanonicalName())) {
            //errorType = SERVER_ERROR;
            serverFailure = (HttpServerErrorException) e;
        }
    }

    private void handleException(Exception e){
        failure = e;
        printError(e);
        this.result = false;
        //errorType = ERROR;
    }

    /**
     * Returns an observable emitting a RestResults<X> object. The RestResults class wraps the response status,
     * whether the call was successful or not, and the serialized response.
     * @return an observable emitting a RestResults<responseBody> object.
     */
    public Observable<RestResults<X>> asObservable(){

        observable = Observable.defer(()->{
            boolean result = doInBackground();
            RestResults<X> restResults = new RestResults<X>()
                        .setStatus(getResponseStatus().value())
                        .setSuccessful(result);
            if(result) restResults.setResultEntity(jsonResponseEntity);

            afterCall(result);
            return Observable.just(restResults);

        });
        return observable;
    }

    /**
     * Returns a plain observable emitting the response body.
     * @return an Observable<responseBody> object.
     */
    public Observable<X> asSimpleObservable(){
        Observable<X> xObservable = Observable.defer(()->{
            boolean result = doInBackground();
            afterCall(result);
            return Observable.just(jsonResponseEntity);
        });
        return xObservable;
    }

    protected <L extends Exception> void printError(L e){
        if (EasyRest.isDebugMode()) {
            System.out.println("The error was caused by the body " + entityClass.getCanonicalName());
            System.out.println(" and the response " + jsonResponseEntityClass.getCanonicalName());
            System.out.println(" in the url " + url);
            e.printStackTrace();
        }
    }


}
