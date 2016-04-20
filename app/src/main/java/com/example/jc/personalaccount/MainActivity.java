package com.example.jc.personalaccount;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends FragmentActivity {


    private static String CURFRAGMENTINDEX = "CURFRAGMENTINDEX";
    public static Fragment[] mFragments;
    private static int mCurFragmentIndex;
    private boolean mIsExit;

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
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURFRAGMENTINDEX,mCurFragmentIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_HOME:return true;
            case KeyEvent.KEYCODE_BACK:{

                if (!mIsExit) {
                    mIsExit = true;
                    Toast.makeText(getApplicationContext(),R.string.common_repeat_back_key_exit,Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsExit = false;
                        }
                    },2000);
                } else {
                    //连续两次点击，则退出
                    exit();
                }

                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        System.exit(0);
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
