package com.grizzly.rest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileFilter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fpardo on 12/18/14.
 * Utility class.
 */
public class EasyRest {

    private static boolean DebugMode = true;

    private static LiteCachingStorage defaultQuickCache;

    /**
     * Deletes the EasyRest cache.
     * @param context a valid application context.
     */
    public static void deleteCache(Context context){


        class Task implements Runnable{

            Context context;

            @Override
            public void run() {
                context = context.getApplicationContext();
                File f = new File(context.getCacheDir().getAbsolutePath() + File.separator + "EasyRest");
                if(f.exists()){
                    for(File file: f.listFiles()){
                        file.delete();
                    }
                }
                context = null;
            }
        }

        Task myTask = new Task();
        myTask.context = context;
        Thread thread = new Thread(myTask);
        thread.start();


    }

    /**
     * Deletes the EasyRest cache of the specified types of answer, and older than @maximumTime
     * @param context a valid android context.
     * @param classes the response types to be deleted.
     * @param maximumTime The maximum caching time.
     */
    public static void deleteCache(Context context, List<Class> classes, long maximumTime){


        class Task implements Runnable{

            public Context context;
            public List<Class> classes = new ArrayList<>();
            public long maximumTime;


            @Override
            public void run() {
                context = context.getApplicationContext();
                File f = new File(context.getCacheDir().getAbsolutePath() + File.separator + "EasyRest");

                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        for(Class aClass : classes){
                            if(pathname.getName().contains(aClass.getSimpleName())
                                    && pathname.lastModified() > maximumTime){
                                return true;
                            }
                        }
                        return false;
                    }
                };

                if(f.exists()){
                    List<File> files = new ArrayList<>(Arrays.asList(f.listFiles(filter)));
                    for(File file: files){
                        file.delete();
                    }
                }
                context = null;
            }
        }

        Task myTask = new Task();
        myTask.classes = classes;
        myTask.context = context;
        myTask.maximumTime = maximumTime;
        Thread thread = new Thread(myTask);
        thread.start();

    }

    public static boolean checkConnectivity(){
        Context context = null;
        context = context.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        context = null;
        return isConnected;
    }

    public static boolean checkConnectivity(Context context){
        context = context.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        context = null;
        return isConnected;
    }

    public static void setDebugMode(boolean debugMode){
        DebugMode = debugMode;
    }

    public static boolean isDebugMode(){
        return DebugMode;
    }

    /**
     * Creates a SHA-1 hash from a given string.
     * @param password the string to be hashed.
     * @return a String representing the SHA-1 form of the argument.
     * @throws java.security.NoSuchAlgorithmException if the SHA-1 algorithm is absent from the JVM.
     */
    static String getHashOne(String password)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(password.getBytes());

        byte byteData[] = md.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return sb.toString();
    }

    public static void setQuickCachingAmount(int amount){
        if(defaultQuickCache == null) defaultQuickCache = new LiteCachingStorage();
        defaultQuickCache.setCachingSize(amount);
    }

    static void cacheRequest(String name, Object entity){
        if(defaultQuickCache == null) defaultQuickCache = new LiteCachingStorage();
        defaultQuickCache.addRequest(name, entity);
    }

    static Object getCachedRequest(String name){
        if(defaultQuickCache==null) defaultQuickCache = new LiteCachingStorage();
        if(defaultQuickCache.isCachedRequest(name)) return defaultQuickCache.getRequest(name);
        return null;
    }

    static boolean isCachedRequest(String name){
        if(defaultQuickCache==null) defaultQuickCache = new LiteCachingStorage();
        return defaultQuickCache.isCachedRequest(name);
    }

}
