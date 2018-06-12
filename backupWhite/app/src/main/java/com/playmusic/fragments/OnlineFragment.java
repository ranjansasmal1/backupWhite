package com.playmusic.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.playmusic.R;
import com.playmusic.data.*;
import com.playmusic.model.GetAllSonds;

import java.util.HashMap;
import java.util.List;


/**
 * Created by swapan on 25/12/17.
 */

public class OnlineFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{

    private SliderLayout mDemoSlider;

    RecyclerView recyclerViewOne,
            recyclerViewTwo,
            recyclerViewThree,
            recyclerViewFour,
            recyclerViewSix,
            recyclerViewFive;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // call initComponents func to initial all components
        initComponents(view);
        //call initSlider func to initial slider
        initSlider();

        // load song list
        //GetAllSonds getAllSonds = new GetAllSonds(getActivity());
        //List<SongsModel> songList = getAllSonds.getAllMusicPathList();

        LinearLayoutManager layoutManagerOne = new LinearLayoutManager(getActivity());
        layoutManagerOne.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewOne.setLayoutManager(layoutManagerOne);

        AdapterOne adapterOne = new AdapterOne(getActivity());
        recyclerViewOne.setAdapter(adapterOne);

        LinearLayoutManager layoutManagerTwo = new LinearLayoutManager(getActivity());
        layoutManagerTwo.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewTwo.setLayoutManager(layoutManagerTwo);

        AdapterTwo adapterTwo = new AdapterTwo(getActivity());
        recyclerViewTwo.setAdapter(adapterTwo);

        LinearLayoutManager layoutManagerThree = new LinearLayoutManager(getActivity());
        layoutManagerThree.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewThree.setLayoutManager(layoutManagerThree);

        AdapterThree adapterThree = new AdapterThree(getActivity());
        recyclerViewThree.setAdapter(adapterThree);

        LinearLayoutManager layoutManagerFour = new LinearLayoutManager(getActivity());
        layoutManagerFour.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewFour.setLayoutManager(layoutManagerFour);

        AdapterFour adapterTFour = new AdapterFour(getActivity());
        recyclerViewFour.setAdapter(adapterTFour);

        LinearLayoutManager layoutManagerFive = new LinearLayoutManager(getActivity());
        layoutManagerFive.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewFive.setLayoutManager(layoutManagerFive);

        AdapterFive adapterTFive = new AdapterFive(getActivity());
        recyclerViewFive.setAdapter(adapterTFive);

        LinearLayoutManager layoutManagerSix = new LinearLayoutManager(getActivity());
        layoutManagerSix.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewSix.setLayoutManager(layoutManagerSix);

        AdapterSix adapterSix = new AdapterSix(getActivity());
        recyclerViewSix.setAdapter(adapterSix);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.online_fragment,
                container, false);
        return view;
    }

    public void setText(String text) {
//        TextView view = (TextView) getView().findViewById(R.id.detailsText);
//        view.setText(text);
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void initSlider(){
        HashMap<String,String> url_maps = new HashMap<String, String>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");

        HashMap<String,Integer> file_maps = new HashMap<String, Integer>();
        file_maps.put("Hannibal",R.drawable.hannibal);
        file_maps.put("Big Bang Theory",R.drawable.bigbang);
        file_maps.put("House of Cards",R.drawable.house);
        file_maps.put("Game of Thrones", R.drawable.game_of_thrones);

        for(String name : file_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);

            mDemoSlider.addSlider(textSliderView);
        }

        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
        mDemoSlider.addOnPageChangeListener(this);

        mDemoSlider.setPresetTransformer("Default");


    }

    private void initComponents(View v){

        mDemoSlider = (SliderLayout)v.findViewById(R.id.slider);

        recyclerViewOne = (RecyclerView) v.findViewById(R.id.recycler_view_one);
        recyclerViewTwo = (RecyclerView) v.findViewById(R.id.recycler_view_two);
        recyclerViewThree = (RecyclerView)v. findViewById(R.id.recycler_view_three);
        recyclerViewFour = (RecyclerView) v.findViewById(R.id.recycler_view_four);
        recyclerViewFive = (RecyclerView) v.findViewById(R.id.recycler_view_five);
        recyclerViewSix = (RecyclerView)v.findViewById(R.id.recycler_view_six);

    }
}