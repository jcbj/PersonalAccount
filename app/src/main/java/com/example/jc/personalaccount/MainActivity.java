package com.example.jc.personalaccount;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
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
    protected void onStart() {
        super.onStart();

        Log.d("PA","MA:onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("PA","MA:onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("PA","MA:onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("PA","MA:onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("PA","MA:onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("PA","MA:onRestart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("PA","MA:onCreate");

        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //初始化
        GlobalData.ImagePath = getApplicationContext().getFilesDir().getAbsolutePath() + "/ImageData/" + GlobalData.CurrentUser;
        File file = new File(GlobalData.ImagePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        //默认显示第一项
        mCurFragmentIndex = 0;
        setFragmentIndicator(mCurFragmentIndex);
        //根据从不同Activity返回的，设置当前显示的项。
        Intent intent = this.getIntent();
        String sourceName = intent.getStringExtra(GlobalData.EXTRA_WHO_HOME_TAGNAME);
        int editCount = intent.getIntExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,0);

        if (null != sourceName) {

            if (sourceName.equals(GlobalData.STRING_ACTIVITY_EDIT_NETASSETS)) {
                mCurFragmentIndex = 0;
                showWhichFragment(mCurFragmentIndex);
                ViewIndicator.setIndicator(mCurFragmentIndex);
            } else if (sourceName.equals(GlobalData.STRING_ACTIVITY_EDIT_SUMMARY)) {
                mCurFragmentIndex = 1;
                showWhichFragment(mCurFragmentIndex);
                ViewIndicator.setIndicator(mCurFragmentIndex);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURFRAGMENTINDEX,mCurFragmentIndex);
        super.onSaveInstanceState(outState);

        Log.d("PA","MA:onSaveInstanceState");
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
