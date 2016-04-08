package com.example.jc.personalaccount;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends FragmentActivity {

    public static Fragment[] mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setFragmentIndicator(0);
    }

    private void setFragmentIndicator(int whichIsDefault) {

        mFragments = new Fragment[5];
        mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_home);
        mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_summary);
        mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_account);
        mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_detail);
        mFragments[4] = getSupportFragmentManager().findFragmentById(R.id.fragment_car);


        getSupportFragmentManager().beginTransaction().hide(mFragments[0])
                .hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]).show(mFragments[whichIsDefault])
                .commit();

//        showWhichFragment(whichIsDefault);

        ViewIndicator mIndicator = (ViewIndicator) findViewById(R.id.indicator);
        ViewIndicator.setIndicator(whichIsDefault);
        mIndicator.setOnIndicateListener(new ViewIndicator.OnIndicateListener() {
            @Override
            public void onIndicate(View v, int which) {
                showWhichFragment(which);
            }
        });
    }

    private void showWhichFragment(int which) {

        getSupportFragmentManager().beginTransaction().hide(mFragments[0])
                .hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]).show(mFragments[which])
                .commit();

//        FragmentManager fragmentManager = getSupportFragmentManager();
//
//        for (int i = 0; i < mFragments.length; i++) {
//            fragmentManager.beginTransaction().hide(mFragments[i]).commit();
//        }
//        fragmentManager.beginTransaction().show(mFragments[which]).commit();
    }
}
