package com.playmusic.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.playmusic.data.SongsModel;
import com.playmusic.utils.StorageUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by parash on 11/1/18.
 */

public class GetAllSonds {

    Context context;

    public GetAllSonds(Context context){
        this.context = context;
    }

    public ArrayList<SongsModel> getAllMusicPathList() {
        ArrayList<SongsModel> musicPathArrList = new ArrayList<>();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursorAudio = context.getContentResolver().query(songUri, null, null, null, null);
        if (cursorAudio != null && cursorAudio.moveToFirst()) {

            Cursor cursorAlbum;
            if (cursorAudio != null && cursorAudio.moveToFirst()) {

                int count = 1;
                do {
                    Long albumId = Long.valueOf(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                    cursorAlbum = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            new String[]{
                            MediaStore.Audio.Albums._ID,
                            MediaStore.Audio.Albums.ALBUM_ART
                            },
                            MediaStore.Audio.Albums._ID + "=" + albumId, null, null);

                    if(cursorAlbum != null && cursorAlbum.moveToFirst()){

                        // set song objects
                        SongsModel songsModel = new SongsModel();
                        songsModel.setID(cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums._ID)));
//                        songsModel.setCount(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media._COUNT)));
                        songsModel.setTitle(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                        songsModel.setArtist(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        songsModel.setData(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
                        songsModel.setDisplayNam(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                        songsModel.setDuration(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                        songsModel.setAlbumArt(cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums._ID)));
                        songsModel.setDateAdded(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)));
                        songsModel.setSize(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                        songsModel.setTrack(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.TRACK)));
                        songsModel.setYear(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.YEAR)));

                        //String s = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                        //Uri d = Uri.parse(s);
                        musicPathArrList.add(songsModel);
                    }
                    count++;
                } while (cursorAudio.moveToNext());
            }
        }
        return musicPathArrList;
    }
        // get all folder list
    public File[] getAllFolder(){
        File myDirectory = new File("/");
        File[] directories = myDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        return directories;
    }

    // short array list for Album
    public ArrayList<SongsModel> shortForAlbum(){
        StorageUtil storageUtil = new StorageUtil(context);
        //ArrayList<SongsModel> shortList = storageUtil.loadAudio().stream().distinct().collect(Collectors.toCollection(Collectors.toList()));
//        Set<String> uniqueList = new HashSet<String>(storageUtil.loadAudio());
//        duplicatList = new ArrayList<String>(uniqueList); //let GC will doing free memory
    }

}
