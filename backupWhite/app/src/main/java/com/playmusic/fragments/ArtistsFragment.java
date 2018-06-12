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
import com.playmusic.data.AdapterArtistList;
import com.playmusic.data.AdapterSongList;

/**
 * Created by swapan on 25/12/17.
 */

public class ArtistsFragment extends Fragment {
    private GridView artistList;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdapterArtistList adapterArtistList = new AdapterArtistList(getActivity());
        artistList.setAdapter(adapterArtistList);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artists_fragment,
                container, false);
        artistList = (GridView)view.findViewById(R.id.grid_artist);
        return view;
    }

    public void setText(String text) {
//        TextView view = (TextView) getView().findViewById(R.id.detailsText);
//        view.setText(text);
    }
}