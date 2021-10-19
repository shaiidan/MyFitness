/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class FitFragment extends Fragment {

    public FitFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fit, container, false);

        BottomNavigationView btnNavigationView = getActivity().findViewById(R.id.user_menu);
        btnNavigationView.setVisibility(View.VISIBLE);

        TextView tvStart = view.findViewById(R.id.fitFragment_start);
        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("fit show back")
                        .replace(
                                R.id.fragment_container_with_menu, new FitShowFragment()).commit();
            }
        });

        return view;
    }
}