package com.grizzly.rest;

import android.content.Context;

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
        File f = new File(context.getCacheDir().getAbsolutePath() + File.separator + "EasyRest");
        if(f.exists()){
            for(File file: f.listFiles()){
                file.delete();
            }
        }
    }

}
