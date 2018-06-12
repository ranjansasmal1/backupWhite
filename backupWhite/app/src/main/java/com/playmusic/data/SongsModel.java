package com.playmusic.data;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * Created by parash on 11/1/18.
 */
public class SongsModel implements Serializable {

    private String ID;
    private String TITLE;
    private String ARTIST;
    private String DATA;
    private String DISPLAY_NAME;
    private String DURATION;
    private String ALBUM_ART;
    private String DATE_ADDED;
    private String SIZE;
    private String TRACK;
    private String YEAR;
    private String COUNT;
    private boolean selected;


    public String getID(){
        return this.ID;
    }
    public void setID(String id){
        this.ID = id;
    }

    public String getTitle(){
        return this.TITLE;
    }
    public void setTitle(String title){
        this.TITLE = title;
    }

    public String getArtist(){
        return this.ARTIST;
    }
    public void setArtist(String artist){
        this.ARTIST = artist;
    }

    public String getData(){
        return this.DATA;
    }
    public void setData(String data){
        this.DATA = data;
    }

    public String getDisplayName(){
        return this.DISPLAY_NAME;
    }
    public void setDisplayNam(String displayNam){
        this.DISPLAY_NAME = displayNam;
    }

    public String getDuration(){
        return this.DURATION;
    }
    public void setDuration(String duration){
        this.DURATION = duration;
    }

    public String getAlbumArt(){
        return this.ALBUM_ART;
    }
    public void setAlbumArt(String albumArt){
//        Bitmap bm = BitmapFactory.decodeFile(albumArt);
//        Uri sArtworkUri = Uri
//                .parse("content://media/external/audio/albumart");
//        Uri uri = ContentUris.withAppendedId(sArtworkUri,
//                Long.parseLong(albumArt));
        this.ALBUM_ART = albumArt;
    }

    public String getDateAdded(){
        return this.DATE_ADDED;
    }
    public void setDateAdded(String dateAdded){
        this.DATE_ADDED = dateAdded;
    }

    public String getSize(){
        return this.SIZE;
    }
    public void setSize(String size){
        this.SIZE = size;
    }

    public String getTrack(){
        return this.TRACK;
    }
    public void setTrack(String track){
        this.TRACK = track;
    }

    public String getYear(){
        return this.YEAR;
    }
    public void setYear(String year){
        this.YEAR = year;
    }

    public String getCount(){
        return this.COUNT;
    }
    public void setCount(String count){
        this.ID = count;
    }

}