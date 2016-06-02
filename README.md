EasyRest
========

EasyRest is an extension to Spring-Android restTemplate that allows making rest calls using generics. it features arbitrary caching (IE: Dear server, I don't care about your opinion, I already have this data, so I'm gonna use it, because I prefer to show something to the user, than nothing!), deferred calls (IE: I already have this data, so I will send this to the user, and meanwhile, I will refresh it), and handles the asynchrony/synchrony. Caching is both physical and logical, survives between app sessions, and works very akin to a NOSQL database; a cached "register" is indentified using the requested body, the url and its parameters, so in lightweighed situations, it can replace a local storage. It has a mirror project for desktop (EasyRest-Desktop: https://github.com/fcopardo/EasyRest-desktop), with the same capabilities and signatures.

why another rest framework? There are a lot of options out there...

Back in the day, I was working in a project composed of a desktop (no, no web. DESKTOP) program, a webpage, and an android app. Gingerbread was the version at the time, and I was working on both a swing app (Yes, I know, I know!!!!) and the servers. And because the android apps are coded in java, I found myself answering about, then coding, and finally working into the android app too. With such a workload, I started to create wrapper classes around libraries and common processes (databases, networking, threading, caching...), so I could code the three things simultaneosly. And there came a day, when I realized I was copypasting identical packages over and over... so I created libraries, and I needed to distribute them somehow. So, I published this!

Ok, how does it works??

First of all, you are gonna need the jitpack repository in your app:

Gradle:

add the jitpack repository:

maven { 
    url "https://jitpack.io" 
}

and then the dependency:

compile ('com.github.fcopardo:easyrest:v1.3.7@aar'){
    transitive = true
    ///If you are using jackson, spring-android and ok-http, they are included here.
}

Because we are using jackson, we also should add this to the android configuration:

android {

//Loots of things

packagingOptions {
        exclude 'META-INF/DEPENDENCIES.txt'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/notice.txt'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/dependencies.txt'
        exclude 'META-INF/LGPL2.1'
        exclude 'META-INF/ASL2.0'
        exclude 'META-INF/services/com.fasterxml.jackson.core.ObjectCodec'
        exclude 'META-INF/services/com.fasterxml.jackson.core.JsonFactory'
    }
}


For every call, you are gonna need to inject both the Response body type and the Request body type, as java classes (you can either use jackson or just create a common POJO). Obviously you can use the Void type if a body is not required.

Basic Example:

GenericRestCall<Void, aModel, String> restCall = new GenericRestCall<>(Void.class, aModel.class, String.class)
                .setUrl(someUrl) //A string
                .addUrlParams(queryParams) // A Map of String, Object with queryparams to be appended to the URL
                .isCacheEnabled(true) /*Defines if caching is activated. If true, checks how old the cache of the call is, and avoids making the call if the cache is fresh enough, or if the API is unreachable and sends the cached data to actions and callbacks*/
                .setCacheTime(10800000L) //Sets the arbitrary caching time, in miliseconds.
                .setMethodToCall(HttpMethod.GET) //The HTTP call. Currently, POST, PUT, GET and DELETE are supported.
                .setTaskCompletion(new afterTaskCompletion<aModel>() {
                    @Override
                    public void onTaskCompleted(aModel o) {
                    //a direct success callback.
                    }
                }
                .setTaskFailure(new afterTaskFailure() {
                    @Override
                    public void onTaskFailed(Object o, Exception e) {
                    /*a direct failure callback. There are specific versions for server failures and client failures,
                    setServerTaskFailure and setClientTaskFailure. A setCommonTasks listener is also available for general callbacks which have to be executed no matter what happened.
                    */
                    }
                })
                .setAutomaticCacheRefresh(true); /*If true, detonates an API call after the whole flux is resolved, in a new thread, and refresh the cache with it, allowing you to make deferred calls and answering quickly to the user, but with a high degree of freshness guaranteed. */
                
                
    Rest Calls can also be created using the WebServiceFactory class. This class allows centralization of headers, caching times, android context, timeout times and base URLs.
    
            WebServiceFactory webServiceFactory = new WebServiceFactory();
            webServiceFactory.setTimeOutValue(15000);
            webServiceFactory.setContext(context);
            
            webServiceFactory.getGenericRestCallInstance(Void.class, aModel.class)
            .setUrl(someUrl)
            .addUrlParams(queryParams)
            .isCacheEnabled(true)
            .setMethodToCall(HttpMethod.GET)
            //callbacks here
            
            Other functions:
            
            setReprocessWhenRefreshing(boolean) /*When set to true, executes all the direct callbacks but commonTasks in a deferred call after refreshing the cache. This is useful when non UI-related parts of your app needs the refreshed data as soon as possible.*/
            addSuccessSubscriber(Action<RestResults<X>> subscriber) /*Allows adding a RXandroid action to the call. You can have as many actions subscribed as you like.*/
            deleteSuccessSubscriber(Action1<RestResults<X>> action) /*Deletes a subscribed action. Combined, this methods allow working in a lifecycle-safe way*/
            
            Documentation is a work in progress, so have a little patience.
            
            Have fun!
