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

    String getCurrentUserID();

    void unlogin();

    //注册
    Boolean register(String name, String password, String email);

    //Home
    BalanceSheetItem[] getAllBalanceSheetItems();

    Boolean editWorthItem(BalanceSheetItem info, Boolean isAdd);

    Boolean deleteWorthItem(int id);

    //Summary
    SummaryItem[] getAllSummaryItems();

    Boolean editSummaryItem(SummaryItem info, Boolean isAdd);

    Boolean deleteSummaryItem(int id);

    String[] getAllAccountAlias();

    //Account
    AccountItem[] getAllAccountItems();

    Boolean editAccountItem(AccountItem info, Boolean isAdd);

    Boolean deleteAccountItem(int id);

    //Detail
    DetailItem[] getAllDetailItems();

    Boolean editDetailItem(DetailItem info, Boolean isAdd);

    Boolean deleteDetailItem(int id);

    //Car
    CarItem[] getAllCarItems();

    Boolean editCarItem(CarItem info, Boolean isAdd);

    Boolean deleteCarItem(int id);

    //Export
    Boolean exportDataStore();

    Boolean exportCSV(int type);
}
