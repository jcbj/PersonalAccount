package com.example.jc.personalaccount;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.TypedValue;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.BalanceSheetItem;
import com.example.jc.personalaccount.Data.CarItem;
import com.example.jc.personalaccount.Data.DetailItem;
import com.example.jc.personalaccount.Data.SummaryItem;
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
    public static BalanceSheetItem EXTRA_Home_Edit_Data; 
    public static String EXTRA_HOME_EDIT_DATA = "HOME_EDIT_DATA";
    //从编辑页面返回资产负债表，是否需要刷新，[0]不需要刷新，[1]需要刷新
    public static String EXTRA_EDIT_HOME_ISREFRESH = "EDIT_HOME_ISREFRESH";
    //标记是从那个页面返回到main
    public static String EXTRA_WHO_HOME_TAGNAME = "WHO_HOME_TAGNAME";
    public static String STRING_ACTIVITY_EDIT_NETASSETS = "EDIT_NETASSETS";
    public static String STRING_ACTIVITY_EDIT_SUMMARY = "EDIT_SUMMARY";
    public static String STRING_ACTIVITY_EDIT_ACCOUNT = "EDIT_ACCOUNT";
    public static String STRING_ACTIVITY_EDIT_DETAIL = "EDIT_DETAIL";
    public static String STRING_ACTIVITY_EDIT_CAR = "EDIT_CAR";

    public static String EXTRA_SUMMARY_EDIT_TYPE = "SUMMARY_EDIT_TYPE";
    public static SummaryItem EXTRA_Summary_Edit_Data; 

    public static String EXTRA_ACCOUNT_EDIT_TYPE = "ACCOUNT_EDIT_TYPE";
    public static AccountItem EXTRA_Account_Edit_Data; 

    public static String EXTRA_DETAIL_EDIT_TYPE = "DETAIL_EDIT_TYPE";
    public static DetailItem EXTRA_Detail_Edit_Data; 

    public static String EXTRA_CAR_EDIT_TYPE = "CAR_EDIT_TYPE";
    public static CarItem EXTRA_Car_Edit_Data;

    //*****************

    //当前登录用户名
    public static String CurrentUser = "Admin";
    public static Boolean IsInitDatabase = false;
    public static String ImagePath;

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


    /**
     * 构建滑动以后的菜单项
     * @param context
     * @return
     */
    public static SwipeMenuCreator buildSwipeMenuCreator(final Context context) {
        return new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(context);
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(dp2px(90,context));
                // set item title
                openItem.setTitle("Edit");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(context);//getApplicationContext()
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0x80, 0x80, 0x80)));
                // set item width
                deleteItem.setWidth(dp2px(90,context));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
    }

    private static int dp2px(int dp, final Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

}
