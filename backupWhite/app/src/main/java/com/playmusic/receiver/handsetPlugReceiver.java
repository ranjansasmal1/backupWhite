package com.playmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.playmusic.services.MusicService;

/**
 * Created by ranjan on 17/1/18.
 */

public class handsetPlugReceiver extends BroadcastReceiver {
    MusicService musicService;
    public handsetPlugReceiver(MusicService musicService){
        this.musicService=musicService;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()== AudioManager.ACTION_AUDIO_BECOMING_NOISY && this.isInitialStickyBroadcast()){
           this.musicService.pauseMedia();
        }
    }
}
