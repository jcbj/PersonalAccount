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

import com.example.jc.personalaccount.Data.FragmentID;

import java.io.File;

public class MainActivity extends FragmentActivity {

    private static final int TABCOUNT = 6;
    private static String CURFRAGMENTINDEX = "CURFRAGMENTINDEX";
    public static Fragment[] mFragments;
    private static int mCurFragmentIndex;
    private static boolean[] mIsRefreshFragment;
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int editCount = data.getIntExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,0);

        if (0 == editCount) {
            return;
        }

        ((IFragmentUI)mFragments[resultCode]).refreshUIData();
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

        mIsRefreshFragment = new boolean[TABCOUNT];
        mFragments = new Fragment[TABCOUNT];
        mFragments[0] = fragmentManager.findFragmentById(R.id.fragment_home);
        mFragments[1] = fragmentManager.findFragmentById(R.id.fragment_summary);
        mFragments[2] = fragmentManager.findFragmentById(R.id.fragment_account);
        mFragments[3] = fragmentManager.findFragmentById(R.id.fragment_detail);
        mFragments[4] = fragmentManager.findFragmentById(R.id.fragment_car);
        mFragments[5] = fragmentManager.findFragmentById(R.id.fragment_setting);

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

        if (which < 0 || which >= TABCOUNT) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (!mIsRefreshFragment[which]) {
            ((IFragmentUI)mFragments[which]).refreshUIData();
            mIsRefreshFragment[which] = true;
        }

        for (int i = 0; i < mFragments.length; i++) {
            fragmentManager.beginTransaction().hide(mFragments[i]).commit();
        }
        fragmentManager.beginTransaction().show(mFragments[which]).commit();

        mCurFragmentIndex = which;
    }
}
