package com.example.jc.personalaccount;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import java.io.File;

public class MainActivity extends FragmentActivity {

    private static String CURFRAGMENTINDEX = "CURFRAGMENTINDEX";
    public static Fragment[] mFragments;
    private static int mCurFragmentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if (null == savedInstanceState) {
            GlobalData.ImagePath = getApplicationContext().getFilesDir().getAbsolutePath() + "/ImageData/" + GlobalData.CurrentUser;
            File file = new File(GlobalData.ImagePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            mCurFragmentIndex = 0;
            setFragmentIndicator(mCurFragmentIndex);
        } else {
            mCurFragmentIndex = savedInstanceState.getInt(CURFRAGMENTINDEX);
            showWhichFragment(mCurFragmentIndex);

//            Intent intent = getIntent();
//            String tagName = intent.getStringExtra(GlobalData.EXTRA_WHO_HOME_TAGNAME);
//            if (TextUtils.isEmpty(tagName)) {
//                return;
//            }
//            if (tagName.equals(GlobalData.STRING_ACTIVITY_EDIT_NETASSETS)) {
//                int isRefresh = intent.getIntExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,0);
//                if (0 != isRefresh) {
//                    ((FragmentHome)mFragments[0]).refresh();
//                }
//            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURFRAGMENTINDEX,mCurFragmentIndex);
        super.onSaveInstanceState(outState);
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

        mCurFragmentIndex = which;
    }
}
