package com.example.jc.personalaccount;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

public class MainActivity extends FragmentActivity {

    public static Fragment[] mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setFragmentIndicator(0);

        Intent intent = getIntent();
        String tagName = intent.getStringExtra(GlobalData.EXTRA_WHO_HOME_TAGNAME);
        if (TextUtils.isEmpty(tagName)) {
            return;
        }
        if (tagName.equals(GlobalData.STRING_ACTIVITY_EDIT_NETASSETS)) {
            int isRefresh = intent.getIntExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,0);
            if (0 != isRefresh) {
                ((FragmentHome)mFragments[0]).refresh();
            }
        }
    }

    private void setFragmentIndicator(int whichIsDefault) {

        FragmentManager fragmentManager = getSupportFragmentManager();

        mFragments = new Fragment[5];
        mFragments[0] = fragmentManager.findFragmentById(R.id.fragment_home);
        mFragments[1] = fragmentManager.findFragmentById(R.id.fragment_summary);
        mFragments[2] = fragmentManager.findFragmentById(R.id.fragment_account);
        mFragments[3] = fragmentManager.findFragmentById(R.id.fragment_detail);
        mFragments[4] = fragmentManager.findFragmentById(R.id.fragment_car);

        showWhichFragment(whichIsDefault);

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

        FragmentManager fragmentManager = getSupportFragmentManager();

        for (int i = 0; i < mFragments.length; i++) {
            fragmentManager.beginTransaction().hide(mFragments[i]).commit();
        }
        fragmentManager.beginTransaction().show(mFragments[which]).commit();
    }
}
