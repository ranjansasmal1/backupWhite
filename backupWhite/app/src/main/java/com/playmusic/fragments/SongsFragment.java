package com.playmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.playmusic.R;
import com.playmusic.data.AdapterSongList;
import com.playmusic.data.SongsModel;
import com.playmusic.model.GetAllSonds;
import com.playmusic.utils.StorageUtil;


/**
 * Created by swapan on 25/12/17.
 */

public class SongsFragment extends Fragment {

    private ListView listSong;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // load song list
//        GetAllSonds getAllSonds = new GetAllSonds(getActivity());
//        List<SongsModel> songList = getAllSonds.getAllMusicPathList();
        StorageUtil storageUtil = new StorageUtil(getActivity());
        AdapterSongList adapterSongList = new AdapterSongList(getActivity(), storageUtil.loadAudio());
        listSong.setAdapter(adapterSongList);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.songs_fragment,
                container, false);
        listSong = (ListView)view.findViewById(R.id.list_song_list);

       return view;
    }

    public void setText(String text) {
//        TextView view = (TextView) getView().findViewById(R.id.detailsText);
//        view.setText(text);
    }
}