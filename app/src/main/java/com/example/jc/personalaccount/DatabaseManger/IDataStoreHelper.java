package com.example.jc.personalaccount.DatabaseManger;

import android.content.Context;

import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.BalanceSheetItem;
import com.example.jc.personalaccount.Data.CarItem;
import com.example.jc.personalaccount.Data.DetailItem;
import com.example.jc.personalaccount.Data.SummaryItem;

/**
 * Created by jc on 16/3/28.
 */
public interface IDataStoreHelper {

    //连接启动数据库，初始化相关
    Boolean initDataStore(Context context);

    //关闭数据库连接，释放资源
    void closeDataStore();

    //登录
    Boolean login(String name, String password);

    void unlogin();

    //注册
    Boolean register(String name, String password, String email);

    //Home
    BalanceSheetItem[] getAllBalanceSheetItems(String user);

    Boolean editWorthItem(String user, BalanceSheetItem info, Boolean isAdd);

    Boolean deleteWorthItem(String user, int id);

    //Summary
    SummaryItem[] getAllSummaryItems(String user);

    Boolean editSummaryItem(String user, SummaryItem info, Boolean isAdd);

    Boolean deleteSummaryItem(String user, int id);

    String[] getAllAccountAlias(String user);

    //Account
    AccountItem[] getAllAccountItems(String user);

    Boolean editAccountItem(String user, AccountItem info, Boolean isAdd);

    Boolean deleteAccountItem(String user, int id);

    //Detail
    DetailItem[] getAllDetailItems(String user);

    Boolean editDetailItem(String user, DetailItem info, Boolean isAdd);

    Boolean deleteDetailItem(String user, int id);

    //Car
    CarItem[] getAllCarItems(String user);

    Boolean editCarItem(String user, CarItem info, Boolean isAdd);

    Boolean deleteCarItem(String user, int id);

    //Export
    Boolean exportDataStore();

    Boolean exportCSV(String user, int type);
}
