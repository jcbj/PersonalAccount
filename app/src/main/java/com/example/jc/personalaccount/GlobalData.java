package com.example.jc.personalaccount;

import android.util.Log;

import com.example.jc.personalaccount.DatabaseManger.DataStoreFactory;
import com.example.jc.personalaccount.DatabaseManger.IDataStoreHelper;

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;

/**
 * Created by jc on 16/3/28.
 */
public class GlobalData {

    public static DataStoreFactory.DataStoreType StoreType = DataStoreFactory.DataStoreType.SQLCipher;

    public static IDataStoreHelper DataStoreHelper = DataStoreFactory.getDataStoreHelper(StoreType);

    //Intent Parameter
    public static String EXTRA_USERNAME = "UserName";
    //资产负债表弹出编辑页面时传如参数：编辑资产[0]或编辑负债[1]
    public static String EXTRA_HOME_EDIT_TYPE = "HOME_EDIT_TYPE";
    public static Map<String, Object> Home_Edit_data;
    public static String EXTRA_HOME_EDIT_DATA = "HOME_EDIT_DATA";
    //从编辑页面返回资产负债表，是否需要刷新，[0]不需要刷新，[1]需要刷新
    public static String EXTRA_EDIT_HOME_ISREFRESH = "EDIT_HOME_ISREFRESH";
    //标记是从那个页面返回到main
    public static String EXTRA_WHO_HOME_TAGNAME = "WHO_HOME_TAGNAME";
    public static String STRING_ACTIVITY_EDIT_NETASSETS = "EDIT_NETASSETS";


    //*****************

    //当前登录用户名
    public static String CurrentUser = "Admin";
    public static Boolean IsInitDatabase = false;

    //Log
    public static enum LogType {
        eMessage,           //普通提示信息
        eException,         //异常信息
        eError              //不应该出现的情况
    }

    public static void log(String tag, LogType logType, String log) {
        log(tag,logType,log,null);
    }

    public static void log(String tag, LogType logType, String format, String[] args) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        if (format.isEmpty()) {
            return;
        }

        String log = new String();

        if ((null == args) || (0 == args.length)) {
            log = format;
        } else {
            char[] formatChar = format.toCharArray();
            int j = 0;
            for (int i = 0; i < formatChar.length - 1; i++) {
                if (('%' == formatChar[i]) && ('s' == formatChar[i + 1])) {
                    if (j < args.length) {
                        log += args[j++];
                    }
                    i++;
                } else {
                    log += formatChar[i];
                }
            }
        }

        switch (logType) {
            case eMessage:
                Log.v(tag,log);
                break;
            case eException:
                Log.d(tag,log);
                break;
            case eError:
                Log.e(tag,log);
                break;
            default:
                Log.v(tag,log);
        }
    }
}
