package com.grizzly.rest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;

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

    public static boolean checkConnectivity(){
        Context context = null;
        context = context.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        context = null;
        return isConnected;
    }

}
