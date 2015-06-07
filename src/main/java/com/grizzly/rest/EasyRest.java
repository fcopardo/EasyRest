package com.grizzly.rest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fpardo on 12/18/14.
 * Utility class.
 */
public class EasyRest {

    /**
     * Deletes the EasyRest cache.
     * @param context a valid application context.
     */
    public static void deleteCache(Context context){
        context = context.getApplicationContext();
        File f = new File(context.getCacheDir().getAbsolutePath() + File.separator + "EasyRest");
        if(f.exists()){
            for(File file: f.listFiles()){
                file.delete();
            }
        }
        context = null;
    }

    /**
     * Deletes the EasyRest cache of the specified types of answer, and older than @maximumTime
     * @param context a valid android context.
     * @param classes the response types to be deleted.
     * @param maximumTime The maximum caching time.
     */
    public static void deleteCache(Context context, final List<Class> classes, final long maximumTime){
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

}
