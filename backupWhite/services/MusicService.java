package com.playmusic.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.playmusic.receiver.handsetPlugReceiver;

import java.io.IOException;

/**
 * Created by ranjan on 17/1/18.
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{


    private MediaPlayer myMusicPlayer;
    private String mediaPath;
    private int resumePosition;
    private String TAG="myMusicPlayer";
    private AudioManager audioManager;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private handsetPlugReceiver mhanHandsetPlugReceiver;
    private final IBinder binder=new LocalBinder();
    private boolean isServiceInitialized=false;
    private MediaStore.Audio activeAudio;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //get media file path as well as its instances.
        try{
            mediaPath=intent.getExtras().getString("mediaFile");

            if (requestAudioFocus()==false)
                stopSelf();

            if (mediaPath!=null && mediaPath!="")
                loadMediaPlayer();

        }catch (Exception e){
            e.printStackTrace();
        }

        if (!isServiceInitialized)
            initService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                //resume player again
                 if (myMusicPlayer==null)
                     loadMediaPlayer();
                  else if (!myMusicPlayer.isPlaying())
                      myMusicPlayer.start();
                  break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //check when audio player focus is lose and then stop player with release
                 if (myMusicPlayer.isPlaying())
                     stopMedia();
                     myMusicPlayer.release();
                     myMusicPlayer=null;
                  break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                  if (myMusicPlayer.isPlaying())
                      myMusicPlayer.pause();
                  break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                  if (myMusicPlayer.isPlaying())
                      myMusicPlayer.setVolume(1f,1f);
                  break;

        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //stop player
        stopMedia();
        //stop service as well as.
        stopSelf();
        isServiceInitialized=false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //handle errors.
        switch (what){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.wtf(TAG,"Media Player not valid for playback feature"+extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.wtf(TAG,"Media Player is going to Died"+extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.wtf(TAG,"Media player is unknown"+extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //play song when media player is ready for playing now.
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }


    private void loadMediaPlayer() {
        myMusicPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        myMusicPlayer.setOnCompletionListener(this);
        myMusicPlayer.setOnErrorListener(this);
        myMusicPlayer.setOnPreparedListener(this);
        myMusicPlayer.setOnBufferingUpdateListener(this);
        myMusicPlayer.setOnSeekCompleteListener(this);
        myMusicPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        myMusicPlayer.reset();

        myMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            myMusicPlayer.setDataSource(mediaPath);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        myMusicPlayer.prepareAsync();
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    private void playMedia() {
        if (!myMusicPlayer.isPlaying()) {
            myMusicPlayer.start();
        }
    }

    private void stopMedia() {
        if (myMusicPlayer == null) return;
        if (myMusicPlayer.isPlaying()) {
            myMusicPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (myMusicPlayer.isPlaying()) {
            myMusicPlayer.pause();
            resumePosition = myMusicPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!myMusicPlayer.isPlaying()) {
            myMusicPlayer.seekTo(resumePosition);
            myMusicPlayer.start();
        }
    }


    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
          //  audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
//            if (audioIndex != -1 && audioIndex < audioList.size()) {
//                //index is in a valid range
//                activeAudio = audioList.get(audioIndex);
//            } else {
//                stopSelf();
//            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            myMusicPlayer.reset();
            loadMediaPlayer();
            //updateMetaData();
           // buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter("");
        registerReceiver(playNewAudio, filter);
    }

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mhanHandsetPlugReceiver, intentFilter);
    }
    private void initService(){
            mhanHandsetPlugReceiver=new handsetPlugReceiver(MusicService.this);
        isServiceInitialized=true;
    }
    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (myMusicPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (myMusicPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myMusicPlayer != null) {
            stopMedia();
            myMusicPlayer.release();
            isServiceInitialized=false;
        }
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        unregisterReceiver(playNewAudio);
        unregisterReceiver(mhanHandsetPlugReceiver);
        removeAudioFocus();
        stopSelf();
    }
    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
