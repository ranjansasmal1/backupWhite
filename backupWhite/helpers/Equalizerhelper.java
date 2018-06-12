package com.playmusic.helpers;

import android.content.Context;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;

/**
 * Created by ranjan on 20/1/18.
 */

public class Equalizerhelper {

    Context mContext;
    //Equalizer objects.
    private Equalizer mEqualizer;
    private Equalizer mEqualizer2;
    private Virtualizer mVirtualizer;
    private Virtualizer mVirtualizer2;
    private BassBoost mBassBoost;
    private BassBoost mBassBoost2;
    private PresetReverb mReverb;
    private PresetReverb mReverb2;
    private boolean mIsEqualizerSupported = true;

    //Equalizer setting values.
    private int m50HzLevel = 16;
    private int m130HzLevel = 16;
    private int m320HzLevel = 16;
    private int m800HzLevel = 16;
    private int m2kHzLevel = 16;
    private int m5kHzLevel = 16;
    private int m12kHzLevel = 16;
    private short mVirtualizerLevel = 0;
    private short mBassBoostLevel = 0;
    private short mReverbSetting = 0;
    private Commons mApp;
    public Equalizerhelper(Context context, int audioSessionId1,boolean equalizerEnabled) {
        mContext = context.getApplicationContext();
        mApp=(Commons)context;
        //Init mMediaPlayer's equalizer engine.
        mEqualizer = new Equalizer(0, audioSessionId1);
        mEqualizer.setEnabled(equalizerEnabled);

        //Init mMediaPlayer's virtualizer engine.
        mVirtualizer = new Virtualizer(0, audioSessionId1);
        mVirtualizer.setEnabled(equalizerEnabled);

        //Init mMediaPlayer's bass boost engine.
        mBassBoost = new BassBoost(0, audioSessionId1);
        mBassBoost.setEnabled(equalizerEnabled);

        //Init mMediaPlayer's reverb engine.
        mReverb = new PresetReverb(0, audioSessionId1);
        mReverb.setEnabled(equalizerEnabled);
    }

    /**
     * Releases all EQ objects and sets their references to null.
     */
    public void releaseEQObjects() throws Exception {
        mEqualizer.release();
        mVirtualizer.release();
        mBassBoost.release();
        mReverb.release();

        mEqualizer = null;
        mVirtualizer = null;
        mBassBoost = null;
        mReverb = null;
    }
    public Equalizer getEqualizer() {
        return mEqualizer;
    }

    public Virtualizer getVirtualizer() {
        return mVirtualizer;
    }

    public BassBoost getBassBoost() {
        return mBassBoost;
    }

    public PresetReverb getReverb() {
        return mReverb;
    }

    public void setEqualizer(Equalizer equalizer) {
        mEqualizer = equalizer;
    }

    public void setVirtualizer(Virtualizer virtualizer) {
        mVirtualizer = virtualizer;
    }

    public void setBassBoost(BassBoost bassBoost) {
        mBassBoost = bassBoost;
    }

    public void setReverb(PresetReverb reverb) {
        mReverb = reverb;
    }

    public void setIsEqualizerSupported(boolean isSupported) {
        mIsEqualizerSupported = isSupported;
    }
}
