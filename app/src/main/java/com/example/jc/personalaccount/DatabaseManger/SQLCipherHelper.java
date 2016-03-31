package com.example.jc.personalaccount.DatabaseManger;

import android.content.Context;

import com.example.jc.personalaccount.GlobalData;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;

import java.util.Objects;

/**
 * Created by jc on 16/3/28.
 */
public class SQLCipherHelper implements IDataStoreHelper {

    private static String ID = "SQLCipherHelper";

    private SQLCipherHelper() {

    }

    //单例模式
    private static class LazyHelper {
        private static final SQLCipherHelper INSTANCE = new SQLCipherHelper();
    }

    public static final SQLCipherHelper getInstance() {
        return LazyHelper.INSTANCE;
    }

    //Fields
    private static final String DATABASENAME = "PersonalAccount.db";
    private static final String KEYENCRYPT = "JCyoyo";
    private static final String SQLITE_MASTER = "sqlite_master";
    private static final String USERIDTABLENAME = "UserIDTable";

    private SQLiteDatabase database = null;

    //IDataStoreHelper
    public Boolean initDataStore(Context context) {

        String tag = ID + ".initDataStore";

        Boolean bIsSuccess = false;

        if (null == context) {
            return bIsSuccess;
        }

        try {
            SQLiteDatabase.loadLibs(context);
        } catch (Exception ex) {
            GlobalData.log(tag, GlobalData.LogType.eException,"loadLibs: " + ex.getMessage());
        }

        File file = null;
        try {
            file = context.getDatabasePath(DATABASENAME);

            if (null == file) {
                GlobalData.log(tag, GlobalData.LogType.eError, "Database get path is failed.");
                return bIsSuccess;
            }

            if (!file.exists()) {
                if (!file.mkdirs()) {
                    GlobalData.log(tag, GlobalData.LogType.eError, "Database path is mkdirs(" + file.getPath() + ") failed.");
                }

                if (!file.delete()) {
                    GlobalData.log(tag, GlobalData.LogType.eError, "Database path is delete() failed.");
                }
            }
        } catch (Exception ex) {
            GlobalData.log(tag, GlobalData.LogType.eException,"getDatabasePath: " + ex.getMessage());
        }

        try {
            this.database = SQLiteDatabase.openOrCreateDatabase(file, KEYENCRYPT, null);

            if (null != this.database) {
                bIsSuccess = this.createUserIDTable(USERIDTABLENAME);
            }
        } catch (Exception ex) {
            GlobalData.log(tag, GlobalData.LogType.eException,"openOrCreateDatabase: " + ex.getMessage());
        }

        return bIsSuccess;
    }

    public void closeDataStore() {
        try {
            this.database.close();
        } catch (Exception ex) {
            GlobalData.log(ID + ".closeDataStore", GlobalData.LogType.eException,ex.getMessage());
        }
    }

    public Boolean login(String name, String password) {
        String sql = "SELECT * FROM " + USERIDTABLENAME + " WHERE name = ? AND password = ?";
        Boolean bIsSuccess = this.checkIsExist(sql, new String[]{name, password});

        GlobalData.log(ID + ".login", GlobalData.LogType.eMessage,name + " is login " + (bIsSuccess?"success.":"failed."));

        return bIsSuccess;
    }

    public Boolean register(String name, String password, String email) {
        String sqlCheckExist = "SELECT * FROM " + USERIDTABLENAME + " WHERE name = ?";

        if (this.checkIsExist(sqlCheckExist,new String[] {name})) {

            GlobalData.log(ID + ".register", GlobalData.LogType.eMessage,name + " is register but name is exist");

            return false;
        }

        String  sql = "INSERT INTO " + USERIDTABLENAME + " (name,password,email) values('" + name + "','" + password + "','" + email +"')";
        this.execSQL(sql);

        if (this.checkIsExist(sqlCheckExist,new String[] {name})) {
            GlobalData.log(ID + ".register", GlobalData.LogType.eMessage,name + " is register success");
            return true;
        }

        if (this.login(name,password)) {
            return true;
        }

        GlobalData.log(ID + ".register", GlobalData.LogType.eMessage,name + " is register failed");

        return false;
    }

    //Database
    private void execSQL(String sql) {
        GlobalData.log(ID + ".execSQL",GlobalData.LogType.eMessage,sql);

        try {
            this.database.execSQL(sql);
        } catch (Exception ex) {
            GlobalData.log(ID + ".execSQL",GlobalData.LogType.eException,ex.getMessage());
        }
    }

    private void execSQL(String sql, Objects[] objs) {

        if (null == objs) {
            GlobalData.log(ID + ".execSQL1", GlobalData.LogType.eMessage,sql);
        } else {
            String[] selectionArgs = new String[objs.length];
            for (int i = 0; i < objs.length; i++) {
                selectionArgs[i] = objs[i].toString();
            }

            GlobalData.log(ID + ".execSQL1", GlobalData.LogType.eMessage,sql.replace("?","%s"),selectionArgs);
        }

        try {
            this.database.execSQL(sql,objs);
        } catch (Exception ex) {
            GlobalData.log(ID + ".execSQL1",GlobalData.LogType.eException,ex.getMessage());
        }
    }

    //selectionArgs: 只针对SQL语句中WHERE部分占位符替换，其它部分无效
    private Cursor querySQL(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        GlobalData.log(ID + ".querySQL",GlobalData.LogType.eMessage,sql.replace("?","%s"),selectionArgs);

        try {
            cursor = this.database.rawQuery(sql,selectionArgs);
        } catch (Exception ex) {
            GlobalData.log(ID + ".querySQL",GlobalData.LogType.eException,ex.getMessage());
        }

        return cursor;
    }

    //执行传人的查询条件，检查是否有记录返回
    private Boolean checkIsExist(String sql, String[] selectionArgs) {
        GlobalData.log(ID + ".CheckIsExist",GlobalData.LogType.eMessage, sql.replace("?","%s"),selectionArgs);

        try {
            Cursor cursor = this.querySQL(sql,selectionArgs);
            if ((null != cursor) && (cursor.getCount() > 0)) {
                cursor.close();

                return true;
            }
        } catch (Exception ex) {
            GlobalData.log(ID + ".CheckIsExist",GlobalData.LogType.eException,ex.getMessage());
        }

        return false;
    }

    //private
    //创建用户信息表
    //ID,UserNmae,Password,Email
    private boolean createUserIDTable(String tableName) {
        String sqlCheckExist = "SELECT * FROM " + SQLITE_MASTER + " WHERE type = 'table' and name = '" + USERIDTABLENAME + "'";
        if (!this.checkIsExist(sqlCheckExist,null)) {
            String sql = "CREATE TABLE " + USERIDTABLENAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, password TEXT, email TEXT)";
            this.execSQL(sql);

            if (this.checkIsExist(sqlCheckExist,null)) {
                GlobalData.log(ID + ".createUserIDTable", GlobalData.LogType.eMessage,"USERID TABLE is created success.");
                return true;
            } else {
                GlobalData.log(ID + ".createUserIDTable", GlobalData.LogType.eMessage,"USERID TABLE is created failed.");
                return false;
            }
        }

        return true;
    }
}
