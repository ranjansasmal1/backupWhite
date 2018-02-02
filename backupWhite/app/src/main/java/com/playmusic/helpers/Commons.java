package com.playmusic.helpers;

import android.app.Application;

import com.playmusic.services.MusicService;

/**
 * Created by ranjan on 20/1/18.
 */

public class Commons extends Application {

    MusicService mMusicService;
    private boolean mIsServiceRunning=false;
    public Commons(){

    }

    public void setService(MusicService service) {
        mMusicService = service;
    }

    public void setIsServiceRunning(boolean running) {
        mIsServiceRunning = running;
    }

    public void setIsEqualizerEnabled(boolean isEnabled) {
        //getSharedPreferences().edit().putBoolean("EQUALIZER_ENABLED", isEnabled).commit();

        //Reload the EQ settings.
        if (isServiceRunning()) {
            try {
                getService().getEqualizerHelper().releaseEQObjects();
                getService().initAudioFX();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public boolean isServiceRunning() {
        return mIsServiceRunning;
    }
    public MusicService getService() {
        return mMusicService;
    }
    public boolean isEqualizerEnabled(){
        return true;
    }
}
