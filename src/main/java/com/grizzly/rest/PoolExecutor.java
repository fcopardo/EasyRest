package com.grizzly.rest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by fco on 11-01-18.
 */
public class PoolExecutor {

    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private static ThreadPoolExecutor globalExecutor;
    private static ThreadPoolExecutor cachingExecutor;

    public static ThreadPoolExecutor getExecutor(boolean caching){
        ThreadPoolExecutor executor;
        if(caching){
            if(cachingExecutor == null){
                cachingExecutor =  new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, workQueue);
            }
            executor = cachingExecutor;
        }else{
            if(globalExecutor == null){
                int processors = Runtime.getRuntime().availableProcessors();
                if(processors >= 2) processors = processors/2;
                globalExecutor =  new ThreadPoolExecutor(processors, processors, 1, TimeUnit.SECONDS, workQueue);
            }
            executor = globalExecutor;
        }
        return executor;
    }


}
