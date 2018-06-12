package com.playmusic.utils;

import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by parash on 13/1/18.
 */

public class ConveterUtils {


    // convert album_art string to Uri
    public Uri getAlbumArtUri(String albumArt){
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri,
                Long.parseLong(albumArt));
        return uri;
    }
}
