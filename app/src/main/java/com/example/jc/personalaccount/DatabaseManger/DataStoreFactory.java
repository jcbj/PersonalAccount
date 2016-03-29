package com.example.jc.personalaccount.DatabaseManger;

/**
 * Created by jc on 16/3/28.
 */
public class DataStoreFactory {

    //数据仓库类型
    public enum DataStoreType {
        SQLCipher
    }

    public static IDataStoreHelper getDataStoreHelper (DataStoreType storeType) {
        switch (storeType) {
            case SQLCipher:
                return SQLCipherHelper.getInstance();
            default:
                return SQLCipherHelper.getInstance();
        }
    }

}
