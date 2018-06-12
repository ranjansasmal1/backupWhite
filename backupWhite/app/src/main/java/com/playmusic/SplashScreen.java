package com.playmusic;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.playmusic.data.SongsModel;
import com.playmusic.model.GetAllSonds;
import com.playmusic.utils.StorageUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SplashScreen extends AppCompatActivity {

    private static WeakReference<SplashScreen> mActivity;

    private ProgressBar mProgress;
    public ArrayList<SongsModel> songList;
    GetAllSonds getAllSonds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mActivity = new WeakReference<>(this);

//        //delay for 2 seconds and start the home activity
//        Completable.complete()
//                .delay(2, TimeUnit.SECONDS)
//                .doOnComplete(this::startHomeActivity)
//                .subscribe();




//    private void startHomeActivity() {
//        if (mActivity.get() != null) {
//            Activity activity = mActivity.get();
//            Intent homeIntent = new Intent(activity, HomeActivity.class);
//            startActivity(homeIntent);
//            activity.finish();
//        }
//    }


    mProgress = (ProgressBar) findViewById(R.id.splash_screen_progress_bar);
        getAllSonds = new GetAllSonds(getApplicationContext());
        File[] s = getAllSonds.getAllFolder();

    // Start lengthy operation in a background thread
        new Thread(new Runnable() {
        public void run() {
            StorageUtil storageUtil = new StorageUtil(getApplicationContext());

            try {
                if(storageUtil.loadAudio().size() < 1){

                    songList = getAllSonds.getAllMusicPathList();
                    storageUtil.storeAudio(songList);
                }
            }catch (Exception ex){
                songList = getAllSonds.getAllMusicPathList();
                storageUtil.storeAudio(songList);
            }


            doWork();
            startApp();
            finish();
        }
    }).start();


}

    private void doWork() {
        for (int progress=0; progress<100; progress+=2) {
            try {
                Thread.sleep(10);
                mProgress.setProgress(progress);
            } catch (Exception e) {
                e.printStackTrace();
                //Timber.e(e.getMessage());
            }
        }
    }

    private void startApp() {
        Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
        startActivity(intent);
    }

}
