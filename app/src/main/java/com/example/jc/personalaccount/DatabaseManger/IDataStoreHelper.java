package com.example.jc.personalaccount.DatabaseManger;

import android.content.Context;

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

    //注册
    Boolean register(String name, String password, String email);
}
