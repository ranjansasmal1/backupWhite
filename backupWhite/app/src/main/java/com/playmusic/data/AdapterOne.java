package com.playmusic.data;

import android.app.Activity;
import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.playmusic.R;
import com.squareup.picasso.Picasso;

import java.util.List;

;

/**
 * Created by parash on 28/12/17.
 */

public class AdapterOne extends RecyclerView.Adapter<AdapterOne.MyViewHolder> {

    private List<SongsModel> songsModels;
    Activity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre;
        public ImageView imageView;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.txt_one_song_name);
            imageView =(ImageView)view.findViewById(R.id.img_one_song_art);
//            genre = (TextView) view.findViewById(R.id.genre);
//            year = (TextView) view.findViewById(R.id.year);
        }
    }


    public AdapterOne(Activity a) {
        //this.songsModels = songsModel;
        this.activity =a;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_one, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

//        Picasso.with(activity)
//                .load(songsModels
//                .get(position)
//                .getAlbumArt())
//                .placeholder(R.drawable.default_song_image)
//                .into(holder.imageView);
//       // Glide.with(activity).load(songsModels.get(position).getAlbumArt()).into(holder.imageView);
//        holder.title.setText(songsModels.get(position).getDisplayName());

    }

    @Override
    public int getItemCount() {
        return 20;
    }
}