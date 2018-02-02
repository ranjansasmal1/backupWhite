package com.playmusic.data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.playmusic.R;

/**
 * Created by swapan on 28/12/17.
 */

public class AdapterPlaylist extends BaseAdapter {

    int mLastPosition;
    private Activity activity;
    private static LayoutInflater inflater=null;

    private static final String[]paths = {"Play item 1", "Play item 2", "Play item 3"};

    public AdapterPlaylist(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return 20;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_item_playlist, null);

        TextView playlistTitle = (TextView)vi.findViewById(R.id.playlist_title); // title
        TextView playlistArtist = (TextView)vi.findViewById(R.id.playlist_artist); // artist name
        ImageView playlistImage=(ImageView)vi.findViewById(R.id.playlist_image); // thumb image
        Spinner playlistSpinner = (Spinner) vi.findViewById(R.id.playlist_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playlistSpinner.setAdapter(adapter);

        float initialTranslation = (mLastPosition <= position ? 500f : -500f);

        vi.setTranslationY(initialTranslation);
        vi.animate()
                .setInterpolator(new DecelerateInterpolator(1.0f))
                .translationY(0f)
                .setDuration(600l)
                .setListener(null);

        // Keep track of the last position we loaded
        mLastPosition = position ;

        return vi;
    }
}