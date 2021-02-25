package com.ilya.voice;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import static android.content.Context.MODE_PRIVATE;

public class GuideFragment extends Fragment {

    LinearLayout linearLayout_guide1, linearLayout_guide2, linearLayout_guide3, linearLayout_guide4,
            linearLayout_guide5, linearLayout_guide6;
    Button button_next;
    public static int COUNT_NEXT = 0;

    PhrasesFragment.onSomeEventListenerMain someEventListenerMain;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListenerMain = (PhrasesFragment.onSomeEventListenerMain) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListenerMain");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide, container, false);
        linearLayout_guide1 = (LinearLayout) view.findViewById(R.id.ll_guide_1);
        linearLayout_guide2 = (LinearLayout) view.findViewById(R.id.ll_guide_2);
        linearLayout_guide3 = (LinearLayout) view.findViewById(R.id.ll_guide_3);
        linearLayout_guide4 = (LinearLayout) view.findViewById(R.id.ll_guide_4);
        linearLayout_guide5 = (LinearLayout) view.findViewById(R.id.ll_guide_5);
        linearLayout_guide6 = (LinearLayout) view.findViewById(R.id.ll_guide_6);
        button_next = (Button)view.findViewById(R.id.btn_next);
        next();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_next:
                        next();
                        break;
                }
            }
        };
        button_next.setOnClickListener(listener);
        return view;
    }
    public void next(){
        switch (COUNT_NEXT){
            case 0:
                linearLayout_guide2.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide3.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide4.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide5.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide6.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));

                linearLayout_guide1.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1));
                break;
            case 1:
                linearLayout_guide1.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide2.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1));
                break;
            case 2:
                linearLayout_guide2.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide3.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1));
                break;
            case 3:
                linearLayout_guide3.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide4.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1));
                break;
            case 4:
                linearLayout_guide4.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide5.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1));
                break;
            case 5:
                linearLayout_guide5.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                linearLayout_guide6.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1));
                break;
            default:
                //закрываю гайд
                someEventListenerMain.someEvent("cloSe_guiDe");
                break;
        }
        COUNT_NEXT++;
    }
}