package com.grizzly.rest;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by fpardo on 12/31/13.
 */
public class CachingAgent extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
