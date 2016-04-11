package com.example.jc.personalaccount.DatabaseManger;

import android.content.Context;

import com.example.jc.personalaccount.Data.HomeInfos;

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

    //根据登录用户建表，如果已经存在则不用
    Boolean createdUserIDDataStore(String user);

    //Home
    HomeInfos[] getAllHomeInfos(String user);
}
