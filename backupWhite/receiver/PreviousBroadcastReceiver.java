package com.playmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.playmusic.helpers.Commons;

/**
 * Created by ranjan on 22/1/18.
 */

public class PreviousBroadcastReceiver extends BroadcastReceiver {

    private Commons mApp;

    @Override
    public void onReceive(Context context, Intent intent) {
        mApp = (Commons) context.getApplicationContext();

        if (mApp.isServiceRunning())
            mApp.getService().skipToPreviousTrack();

    }
}
