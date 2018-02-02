package com.playmusic.data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.playmusic.R;

/**
 * Created by swapan on 28/12/17.
 */

public class AdapterFolderList extends BaseAdapter {
    int mLastPosition;
    private Activity activity;
    private static LayoutInflater inflater=null;

    public AdapterFolderList(Activity a) {
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
            vi = inflater.inflate(R.layout.list_item_folder, null);

        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView artist = (TextView)vi.findViewById(R.id.artist); // artist name
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image

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