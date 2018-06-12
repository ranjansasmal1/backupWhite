package com.playmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.playmusic.R;
import com.playmusic.data.AdapterPlaylist;
import com.playmusic.data.AdapterSongList;

/**
 * Created by swapan on 25/12/17.
 */

public class PlaylistFragment extends Fragment {

    private ListView listPlayList;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdapterPlaylist adapterPlaylist = new AdapterPlaylist(getActivity());
        listPlayList.setAdapter(adapterPlaylist);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_fragment,
                container, false);
        listPlayList = (ListView)view.findViewById(R.id.play_list);
        return view;
    }

    public void setText(String text) {
//        TextView view = (TextView) getView().findViewById(R.id.detailsText);
//        view.setText(text);
    }
}