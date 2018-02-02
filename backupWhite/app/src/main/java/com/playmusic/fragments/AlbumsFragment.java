package com.playmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import com.playmusic.R;
import com.playmusic.data.AdapterAlbumList;
import com.playmusic.data.AdapterSongList;

/**
 * Created by swapan on 25/12/17.
 */

public class AlbumsFragment extends Fragment {

    private GridView albumList;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdapterAlbumList adapterAlbumList = new AdapterAlbumList(getActivity());
        albumList.setAdapter(adapterAlbumList);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.albums_fragment,
                container, false);
        albumList = (GridView) view.findViewById(R.id.grid_album);
        return view;
    }

    public void setText(String text) {
//        TextView view = (TextView) getView().findViewById(R.id.detailsText);
//        view.setText(text);
    }
}