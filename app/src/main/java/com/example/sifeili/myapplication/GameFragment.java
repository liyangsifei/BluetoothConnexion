package com.example.sifeili.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GameFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {

        //Charger le fragment Game
        View view = inflater.inflate(R.layout.fragment_game, null);

        return view;
    }
}