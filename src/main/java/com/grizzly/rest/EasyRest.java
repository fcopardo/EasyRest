package com.grizzly.rest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fpardo on 12/18/14.
 * Utility class.
 */
public class EasyRest {

    private static boolean DebugMode = true;

    /**
     * Deletes the EasyRest cache.
     * @param context a valid application context.
     */
    public static void deleteCache(Context context){

        class Task extends AsyncTask<Void, Void, Boolean>{

            Context context;

            @Override
            protected Boolean doInBackground(Void... params) {

                context = context.getApplicationContext();
                File f = new File(context.getCacheDir().getAbsolutePath() + File.separator + "EasyRest");
                if(f.exists()){
                    for(File file: f.listFiles()){
                        file.delete();
                    }
                }
                context = null;

                return null;
            }
        }

        Task myTask = new Task();
        myTask.context = context;
        myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * Deletes the EasyRest cache of the specified types of answer, and older than @maximumTime
     * @param context a valid android context.
     * @param classes the response types to be deleted.
     * @param maximumTime The maximum caching time.
     */
    public static void deleteCache(Context context, List<Class> classes, long maximumTime){


        class Task extends AsyncTask<Void, Void, Boolean>{

            public Context context;
            public List<Class> classes = new ArrayList<>();
            public long maximumTime;

            @Override
            protected Boolean doInBackground(Void... params) {

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
                return null;
            }
        }

        Task myTask = new Task();
        myTask.classes = classes;
        myTask.context = context;
        myTask.maximumTime = maximumTime;
        myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

}
