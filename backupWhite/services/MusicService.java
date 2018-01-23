package com.playmusic.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.PresetReverb;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

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
    //Crossfade.
    private int mCrossfadeDuration;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private handsetPlugReceiver mhanHandsetPlugReceiver;
    private final IBinder binder=new LocalBinder();
    private boolean isServiceInitialized=false;
    private Commons mApp;
    private Context mContext;
    private Equalizerhelper mEqualizerHelper;
    private int mCurrentSongIndex;
    private MediaStore.Audio activeAudio;
    public static final String ACTION_PLAY = "com.playmusic.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.playmusic.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.playmusic.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.playmusic.ACTION_NEXT";
    public static final String ACTION_STOP = "com.playmusic.ACTION_STOP";
    public static final String LAUNCH_NOW_PLAYING_ACTION = "com.playmusic.LAUNCH_NOW_PLAYING_ACTION";
    public static final String PREVIOUS_ACTION = "com.playmusic.PREVIOUS_ACTION";
    public static final String PLAY_PAUSE_ACTION = "com.playmusic.PLAY_PAUSE_ACTION";
    public static final String NEXT_ACTION = "com.playmusic.NEXT_ACTION";
    public static final String STOP_SERVICE = "com.playmusic.STOP_SERVICE";
    private int fiftyHertzLevel = 16;
    private int oneThirtyHertzLevel = 16;
    private int threeTwentyHertzLevel = 16;
    private int eightHundredHertzLevel = 16;
    private int twoKilohertzLevel = 16;
    private int fiveKilohertzLevel = 16;
    private int twelvePointFiveKilohertzLevel = 16;
    private boolean mMediaPlayerPrepared=false;
    //Handler object.
    private Handler mHandler;
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
            mHandler = new Handler();
            if (mediaPath!=null && mediaPath!="")
                loadMediaPlayer(0);
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
                     loadMediaPlayer(0);
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
        //play song when media player is ready for playing now..
        mMediaPlayerPrepared=true;
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }


    private void loadMediaPlayer(int index) {
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
            loadMediaPlayer(0);
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

    public Notification BuildNotification(){
        mNotificationBuilder = new NotificationCompat.Builder(mContext);
        mNotificationBuilder.setOngoing(true);
        mNotificationBuilder.setAutoCancel(false);
       // mNotificationBuilder.setSmallIcon(R.drawable.notif_icon);
        //Open up the player screen when the user taps on the notification.
        Intent launchNowPlayingIntent = new Intent();
        launchNowPlayingIntent.setAction(MusicService.LAUNCH_NOW_PLAYING_ACTION);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, launchNowPlayingIntent, 0);
        mNotificationBuilder.setContentIntent(launchNowPlayingPendingIntent);
        //Grab the notification layout.
        RemoteViews notificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_custom_layout);
        RemoteViews expNotificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_custom_expanded_layout);

        //Initialize the notification layout buttons.
        Intent previousTrackIntent = new Intent();
        previousTrackIntent.setAction(MusicService.PREVIOUS_ACTION);
        PendingIntent previousTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, previousTrackIntent, 0);

        Intent playPauseTrackIntent = new Intent();
        playPauseTrackIntent.setAction(MusicService.PLAY_PAUSE_ACTION);
        PendingIntent playPauseTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, playPauseTrackIntent, 0);

        Intent nextTrackIntent = new Intent();
        nextTrackIntent.setAction(MusicService.NEXT_ACTION);
        PendingIntent nextTrackPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, nextTrackIntent, 0);

        Intent stopServiceIntent = new Intent();
        stopServiceIntent.setAction(MusicService.STOP_SERVICE);
        PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, stopServiceIntent, 0);

        //Check if audio is playing and set the appropriate play/pause button.
        if (mApp.getService().isPlayingMusic()) {
            notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_pause_light);
            expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_pause_light);
        } else {
            notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_play_light);
            expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_play_light);
        }

        //Set the notification content.
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_one, "Give title here");
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_two, "Give Artist here");
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_three, "Give Album here");

        notificationView.setTextViewText(R.id.notification_base_line_one, "Give title here");
        notificationView.setTextViewText(R.id.notification_base_line_two, "Give Artist here");

        //Set the states of the next/previous buttons and their pending intents.
        if (mApp.getService().isOnlySongInQueue()) {
            //This is the only song in the queue, so disable the previous/next buttons.
            expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.INVISIBLE);
            expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous, View.INVISIBLE);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);

            notificationView.setViewVisibility(R.id.notification_base_next, View.INVISIBLE);
            notificationView.setViewVisibility(R.id.notification_base_previous, View.INVISIBLE);
            notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);

        }else if (mApp.getService().isFirstSongInQueue()) {
            //This is the the first song in the queue, so disable the previous button.
            expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous, View.INVISIBLE);
            expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.VISIBLE);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextTrackPendingIntent);

            notificationView.setViewVisibility(R.id.notification_base_previous, View.INVISIBLE);
            notificationView.setViewVisibility(R.id.notification_base_next, View.VISIBLE);
            notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);

        } else if (mApp.getService().isLastSongInQueue()) {
            //This is the last song in the cursor, so disable the next button.
            expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous, View.VISIBLE);
            expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.INVISIBLE);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextTrackPendingIntent);

            notificationView.setViewVisibility(R.id.notification_base_previous, View.VISIBLE);
            notificationView.setViewVisibility(R.id.notification_base_next, View.INVISIBLE);
            notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);

        }else {
            //We're smack dab in the middle of the queue, so keep the previous and next buttons enabled.
            expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous,View.VISIBLE);
            expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.VISIBLE);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextTrackPendingIntent);
            expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_previous, previousTrackPendingIntent);

            notificationView.setViewVisibility(R.id.notification_base_previous,View.VISIBLE);
            notificationView.setViewVisibility(R.id.notification_base_next, View.VISIBLE);
            notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
            notificationView.setOnClickPendingIntent(R.id.notification_base_previous, previousTrackPendingIntent);

        }

        //Set the "Stop Service" pending intents.
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, stopServicePendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_collapse, stopServicePendingIntent);

        Bitmap bitmap = null;
        //Set the album art.
        expNotificationView.setImageViewBitmap(R.id.notification_expanded_base_image, bitmap);
        notificationView.setImageViewBitmap(R.id.notification_base_image, bitmap);

        //Attach the shrunken layout to the notification.
        mNotificationBuilder.setContent(notificationView);

        //Build the notification object.
        Notification notification = mNotificationBuilder.build();

        //Attach the expanded layout to the notification and set its flags.
        notification.bigContentView = expNotificationView;
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE |
                Notification.FLAG_NO_CLEAR |
                Notification.FLAG_ONGOING_EVENT;
        return notification;
    }
    public Equalizerhelper getEqualizerHelper() {
        return mEqualizerHelper;
    }
    public MediaPlayer getMediaPlayer() {
        return myMusicPlayer;
    }

    /**
     * Returns true if there's only one song in the current queue.
     * False, otherwise.
     */
    public boolean isOnlySongInQueue() {
        if (getCurrentSongIndex()==0 )//if cursor length == 1 then it also happen
            return true;
        else
            return false;

    }
    /**
     * Returns true if mCurrentSongIndex is pointing at the last
     * song in the queue. False, otherwise.
     */
    public boolean isLastSongInQueue() {
        if (getCurrentSongIndex()==11)// if (getCursor().getCount()-1) == current index then it is last in queue..
            return true;
        else
            return false;

    }

    /**
     * Returns true if mCurrentSongIndex is pointing at the first
     * song in the queue and there is more than one song in the
     * queue. False, otherwise.
     */
    public boolean isFirstSongInQueue() {
        if (getCurrentSongIndex()==0 ) //if cursor length greater than 1 then disable also previous btn
            return true;
        else
            return false;

    }

    /**
     * Increments mCurrentSongIndex based on mErrorCount.
     * Returns the new value of mCurrentSongIndex.
     */
    public int incrementCurrentSongIndex() {
       // if ((getCurrentSongIndex()+1) < getCursor().getCount())
            mCurrentSongIndex++;

        return mCurrentSongIndex;
    }

    /**
     * Decrements mCurrentSongIndex by one. Returns the new value
     * of mCurrentSongIndex.
     */
    public int decrementCurrentSongIndex() {
        if ((getCurrentSongIndex()-1) > -1)
            mCurrentSongIndex--;

        return mCurrentSongIndex;
    }

    /**
     * Returns the current value of mCurrentSongIndex.
     */
    public int getCurrentSongIndex() {
        return mCurrentSongIndex;
    }
    public void setCurrentSongIndex(int currentSongIndex) {
        mCurrentSongIndex = currentSongIndex;
    }
    public boolean skipToPreviousTrack() {
        /*
         * If the current track is not within the first three seconds,
         * reset it. If it IS within the first three seconds, skip to the
         * previous track.
         */
        try {
            if (getMediaPlayer().getCurrentPosition() > 3000) {
                getMediaPlayer().seekTo(0);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        getMediaPlayer().reset();
        clearCrossfadeCallbacks();

        //Loop the players if the repeat mode is set to repeat the current song.
       // if (getRepeatMode()==Common.REPEAT_SONG) {
           // getMediaPlayer().setLooping(true);
      //  }
//Remove crossfade runnables and reset all volume levels.
       // getHandler().removeCallbacks(crossFadeRunnable);
        getMediaPlayer().setVolume(1.0f, 1.0f);
        decrementCurrentSongIndex();

        //Start the playback process.
        //mFirstRun = true;
        loadMediaPlayer(getCurrentSongIndex());
        return true;
    }

    /**
     * Skips to the next track (if there is one) and starts
     * playing it. Returns true if the operation succeeded.
     * False, otherwise.
     */
    public boolean skipToNextTrack() {
        try {
            //Reset both MediaPlayer objects.
            getMediaPlayer().reset();
            clearCrossfadeCallbacks();

            //Loop the players if the repeat mode is set to repeat the current song.
//            if (getRepeatMode()==Common.REPEAT_SONG) {
//                getMediaPlayer().setLooping(true);
//                getMediaPlayer2().setLooping(true);
//            }

            //Remove crossfade runnables and reset all volume levels.
            //getHandler().removeCallbacks(crossFadeRunnable);
            getMediaPlayer().setVolume(1.0f, 1.0f);

            //Increment the song index.
            incrementCurrentSongIndex();

            //Update the UI.
           // String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION };
           // String[] flagValues = new String[] { getCurrentSongIndex() + "" };
          //  mApp.broadcastUpdateUICommand(updateFlags, flagValues);

            //Start the playback process.
           // mFirstRun = true;
            loadMediaPlayer(getCurrentSongIndex());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    /**
     * Skips to the specified track index (if there is one) and starts
     * playing it. Returns true if the operation succeeded.
     * False, otherwise.
     */
    public boolean skipToTrack(int trackIndex) {
        try {
            //Reset both MediaPlayer objects.
            getMediaPlayer().reset();
            clearCrossfadeCallbacks();

            //Loop the players if the repeat mode is set to repeat the current song.
           // if (getRepeatMode()==Common.REPEAT_SONG) {
             //   getMediaPlayer().setLooping(true);
            //}

            //Remove crossfade runnables and reset all volume levels.
           // getHandler().removeCallbacks(crossFadeRunnable);
            getMediaPlayer().setVolume(1.0f, 1.0f);


            //Update the song index.
            setCurrentSongIndex(trackIndex);

            //Update the UI.
           // String[] updateFlags = new String[] { Common.UPDATE_PAGER_POSTIION };
            String[] flagValues = new String[] { getCurrentSongIndex() + "" };
           // mApp.broadcastUpdateUICommand(updateFlags, flagValues);

            //Start the playback process.
           // mFirstRun = true;
            loadMediaPlayer(trackIndex);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /**
     * Toggles the playback state between playing and paused and
     * returns whether the current media player is now playing
     * music or not.
     */
    public boolean togglePlaybackState() {
        if (isPlayingMusic())
            pauseMedia();
        else
            resumeMedia();

        return isPlayingMusic();
    }

    /**
     * Indicates if music is currently playing.
     */
    public boolean isPlayingMusic() {
        try {
            if (getMediaPlayer().isPlaying())
                return true;
            else
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Removes all crossfade callbacks on the current
     * Handler object. Also resets the volumes of the
     * MediaPlayer objects to 1.0f.
     */
    private void clearCrossfadeCallbacks() {
        if (mHandler==null)
            return;

        mHandler.removeCallbacks(startCrossFadeRunnable);
       // mHandler.removeCallbacks(crossFadeRunnable);

        try {
            getMediaPlayer().setVolume(1.0f, 1.0f);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }


    /**
     * First runnable that handles the cross fade operation between two tracks.
     */
    public Runnable startCrossFadeRunnable = new Runnable() {

        @Override
        public void run() {

            //Check if we're in the last part of the current song.
            try {
                if (getMediaPlayer().isPlaying()) {

                    int currentTrackDuration = getMediaPlayer().getDuration();
                    int currentTrackFadePosition = currentTrackDuration - (mCrossfadeDuration*1000);
                    if (getMediaPlayer().getCurrentPosition() >= currentTrackFadePosition) {
                        //Launch the next runnable that will handle the cross fade effect.
                        //mHandler.postDelayed(crossFadeRunnable, 100);

                    } else {
                        mHandler.postDelayed(startCrossFadeRunnable, 1000);
                    }

                } else {
                    mHandler.postDelayed(startCrossFadeRunnable, 1000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };


    /**
     * Checks if we have AudioFocus. If not, it explicitly requests it.
     *
     * @return True if we have AudioFocus. False, otherwise.
     */
    private boolean checkAndRequestAudioFocus() {
            if (requestAudioFocus()==true) {
                return true;
            } else {
                //Unable to get focus. Notify the user.
                Toast.makeText(mContext, "Unable to get audio focus", Toast.LENGTH_LONG).show();
                return false;
            }
    }

    public Handler getHandler() {
        return mHandler;
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
