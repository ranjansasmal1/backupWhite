package com.playmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.playmusic.helpers.Commons;

/**
 * Created by ranjan on 22/1/18.
 */

public class StopServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Stop the service.
        Commons app = (Commons) context.getApplicationContext();
        app.getService().stopSelf();

    }

}