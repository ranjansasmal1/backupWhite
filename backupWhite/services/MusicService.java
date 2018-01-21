package com.playmusic.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.PresetReverb;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.playmusic.R;
import com.playmusic.helpers.Commons;
import com.playmusic.helpers.Equalizerhelper;
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
    private Commons mApp;
    private Context mContext;
    private Equalizerhelper mEqualizerHelper;
    private MediaStore.Audio activeAudio;
    public static final String ACTION_PLAY = "com.playmusic.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.playmusic.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.playmusic.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.playmusic.ACTION_NEXT";
    public static final String ACTION_STOP = "com.playmusic.ACTION_STOP";
    public static final String LAUNCH_NOW_PLAYING_ACTION = "com.playmusic.services.LAUNCH_NOW_PLAYING_ACTION";
    private int fiftyHertzLevel = 16;
    private int oneThirtyHertzLevel = 16;
    private int threeTwentyHertzLevel = 16;
    private int eightHundredHertzLevel = 16;
    private int twoKilohertzLevel = 16;
    private int fiveKilohertzLevel = 16;
    private int twelvePointFiveKilohertzLevel = 16;
    //notification build object.
    private NotificationCompat.Builder mNotificationBuilder;
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
            mContext=getApplicationContext();
            if (requestAudioFocus()==false)
                stopSelf();

            if (mediaPath!=null && mediaPath!="")
                loadMediaPlayer();
            mApp=(Commons)getApplicationContext();
            mApp.setService((MusicService) this);
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
    /**
     * Initializes the equalizer and audio effects for this service session.
     */
    public void initAudioFX() {

        try {
            //Instatiate the equalizer helper object.
            mEqualizerHelper = new Equalizerhelper(mContext, myMusicPlayer.getAudioSessionId(),
                    mApp.isEqualizerEnabled());

        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            mEqualizerHelper.setIsEqualizerSupported(false);
        } catch (Exception e) {
            e.printStackTrace();
            mEqualizerHelper.setIsEqualizerSupported(false);
        }
    }
    private void applyMediaPlayerEQ(String songId) {
        if (mEqualizerHelper==null)
            return;
        short fiftyHertzBand = mEqualizerHelper.getEqualizer().getBand(50000);
        short oneThirtyHertzBand = mEqualizerHelper.getEqualizer().getBand(130000);
        short threeTwentyHertzBand = mEqualizerHelper.getEqualizer().getBand(320000);
        short eightHundredHertzBand = mEqualizerHelper.getEqualizer().getBand(800000);
        short twoKilohertzBand = mEqualizerHelper.getEqualizer().getBand(2000000);
        short fiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(5000000);
        short twelvePointFiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(9000000);

        //int[] eqValues = mApp.getDBAccessHelper().getSongEQValues(songId);
        //50Hz Band.
        if (fiftyHertzLevel==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) 0);
        }
//        else if (fiftyHertzLevel < 16) {
//
//            if (eqValues[0]==0) {
//                mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) -1500);
//            } else {
//                mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) (-(16-eqValues[0])*100));
//            }
//
//        } else if (eqValues[0] > 16) {
//            mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) ((eqValues[0]-16)*100));
//        }
        //130Hz Band.
        if (oneThirtyHertzBand==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) 0);
        }
//        else if (eqValues[1] < 16) {
//
//            if (eqValues[1]==0) {
//                mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) -1500);
//            } else {
//                mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) (-(16-eqValues[1])*100));
//            }
//
//        } else if (eqValues[1] > 16) {
//            mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) ((eqValues[1]-16)*100));
//        }
        //320Hz Band.
        if (threeTwentyHertzLevel==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) 0);
        }
//        else if (eqValues[2] < 16) {
//
//            if (eqValues[2]==0) {
//                mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) -1500);
//            } else {
//                mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) (-(16-eqValues[2])*100));
//            }
//
//        } else if (eqValues[2] > 16) {
//            mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) ((eqValues[2]-16)*100));
//        }
        mEqualizerHelper.getVirtualizer().setStrength((short) 2);
        mEqualizerHelper.getBassBoost().setStrength((short) 3);
//        if (eqValues[9]==0) {
//            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_NONE);
//        }else if (eqValues[9]==1) {
//            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
//        } else if (eqValues[9]==2) {
//            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
//        } else if (eqValues[9]==3) {
//            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
//        } else if (eqValues[9]==4) {
//            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
//        } else if (eqValues[9]==5) {
//            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
//        } else if (eqValues[9]==6) {
//            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_PLATE);
//        }

    }

    public NotificationCompat BuildNotification(){
        mNotificationBuilder = new NotificationCompat.Builder(mContext);
        mNotificationBuilder.setOngoing(true);
        mNotificationBuilder.setAutoCancel(false);
       // mNotificationBuilder.setSmallIcon(R.drawable.notif_icon);
        //Open up the player screen when the user taps on the notification.
        Intent launchNowPlayingIntent = new Intent();
        launchNowPlayingIntent.setAction(MusicService.LAUNCH_NOW_PLAYING_ACTION);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, launchNowPlayingIntent, 0);
        mNotificationBuilder.setContentIntent(launchNowPlayingPendingIntent);

        return null;
    }
    public Equalizerhelper getEqualizerHelper() {
        return mEqualizerHelper;
    }
    public MediaPlayer getMediaPlayer() {
        return myMusicPlayer;
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
