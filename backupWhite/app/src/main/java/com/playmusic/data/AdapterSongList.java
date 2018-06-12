package com.playmusic.data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.playmusic.R;
import com.playmusic.utils.ConveterUtils;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swapan on 28/12/17.
 */

public class AdapterSongList extends BaseAdapter {

    int mLastPosition;
    private Activity activity;
    private static LayoutInflater inflater=null;
    private List<SongsModel> songsModels;
    ConveterUtils conveterUtils = new ConveterUtils();

    private static final String[]paths = {"Play item 1", "Play item 2", "Play item 3"};

    public AdapterSongList(Activity a, List<SongsModel> songsModels) {
        activity = a;
        this.songsModels = songsModels;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return songsModels.size();
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
            vi = inflater.inflate(R.layout.list_item_song, null);

        TextView title = (TextView)vi.findViewById(R.id.song_title); // title
        TextView artist = (TextView)vi.findViewById(R.id.song_artist); // artist name
        TextView size = (TextView)vi.findViewById(R.id.song_size); // duration
        TextView duration = (TextView)vi.findViewById(R.id.song_duration);
        TextView number = (TextView)vi.findViewById(R.id.song_number);
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.song_image); // thumb image
        Spinner spinner = (Spinner) vi.findViewById(R.id.song_spinner);

        ArrayAdapter<String>adapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item,paths);

        number.setText(position+1+"");

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        title.setText(songsModels.get(position).getDisplayName());
        artist.setText(songsModels.get(position).getTitle());
        duration.setText(calculateSongDuration(songsModels.get(position).getDuration())+"");
        size.setText(calculateSongSize(songsModels.get(position).getSize()) + " MB");
        //number.setText(songsModels.get(position).getCount());



        Picasso.with(activity)
                .load(conveterUtils.getAlbumArtUri(songsModels
                        .get(position)
                        .getAlbumArt()))
                .placeholder(R.drawable.ic_music_default)
                .into(thumb_image);

//        Animation scaleUp = AnimationUtils.loadAnimation(activity, R.anim.scale_up_fast);
//        vi.setAnimation(scaleUp);

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

    public double calculateSongSize(String size){

        double orgSize = Double.parseDouble(size);
        double mbSize = (orgSize /1024) / 1024;

        DecimalFormat df = new DecimalFormat("##.#");
        return Double.valueOf(df.format(mbSize));
    }

    public String calculateSongDuration(String size){

        double orgSize = Double.parseDouble(size);
        //double mbSize = (orgSize /1000) / 60;
        int seconds = (int) (orgSize/1000)%60;
        int minutes = (int) ((orgSize - seconds)/1000)/60;

        return minutes +  ":" + seconds;
    }
}