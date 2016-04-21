package com.example.jc.personalaccount.DatabaseManger;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.BalanceSheetItem;
import com.example.jc.personalaccount.Data.SummaryItem;
import com.example.jc.personalaccount.GlobalData;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.util.ArrayList;
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
    private static final String BALANCESHEETTABLENAME = "BalanceSheetTable";
    private static final String[] BALANCESHEETTABLECOLUMNNAME = new String[]{
            "id",
            "worthtype",
            "name",
            "worth",
            "imagepath",
            "imagethumb",
            "description"};
    private static final String SUMMARYITEMTABLENAME = "SummaryItemTable";
    private static final String[] SUMMARYITEMTABLECOLUMNNAME = new String[]{
            "id",
            "date",
            "value",
            "name",
            "alias",
            "description"};
    private static final String ACCOUNTITEMTABLENAME = "AccountItemTable";
    private static final String[] ACCOUNTITEMTABLECOLUMNNAME = new String[]{
            "id",
            "date",
            "value",
            "from",
            "type",
            "to",
            "description"};


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

        if (bIsSuccess) {
            GlobalData.CurrentUser = name;
        }

        return bIsSuccess;
    }

    public void unlogin() {
        GlobalData.CurrentUser = null;
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

    //每个登录用户，都有自己单独的表来存储数据；以登录用户名来区别
    public Boolean createdUserIDDataStore(String user) {
        //1，创建资产负债表
        String tableName = user + "_" + BALANCESHEETTABLENAME;
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" + BALANCESHEETTABLECOLUMNNAME[0] + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BALANCESHEETTABLECOLUMNNAME[1] + " INT," +
                BALANCESHEETTABLECOLUMNNAME[2] + "  TEXT," +
                BALANCESHEETTABLECOLUMNNAME[3] + " INT," +
                BALANCESHEETTABLECOLUMNNAME[4] + " TEXT," +
                BALANCESHEETTABLECOLUMNNAME[5] + " BLOB," +
                BALANCESHEETTABLECOLUMNNAME[6] + " TEXT)";
        this.execSQL(sql);

        sql = "SELECT * FROM " + SQLITE_MASTER + " WHERE type = 'table' and name = '" + tableName + "'";
        if (!this.checkIsExist(sql,null)) {
            Log.e(ID + ".createdUserIDDataStore", "create " + tableName + " is failed.");
        }

        //2,

        return false;
    }

    //Worth
    /**
     * 获取资产负债表中所有记录
     * @param user:当前登录用户名
     * @return
     */
    public BalanceSheetItem[] getAllBalanceSheetInfos(String user) {
        String tableName = user + "_" + BALANCESHEETTABLENAME;
        String sql = "SELECT * FROM " + tableName;
        ArrayList<BalanceSheetItem> list = new ArrayList<>();

        Cursor cursor = this.querySQL(sql,null);
        if (null != cursor) {
            try {
                cursor.moveToFirst();
                BalanceSheetItem infos;
                while (!cursor.isAfterLast()) {
                    infos = new BalanceSheetItem();
                    infos.id = cursor.getInt(0);
                    infos.worthType = ((0 == cursor.getInt(1)) ? BalanceSheetItem.WorthType.Property : BalanceSheetItem.WorthType.Debt);
                    infos.name = cursor.getString(2);
                    infos.worth = cursor.getInt(3);
                    infos.imagePath = cursor.getString(4);
                    byte[] imageByte = cursor.getBlob(5);
                    if (null != imageByte) {
                        infos.imageThumb = BitmapFactory.decodeByteArray(imageByte,0,imageByte.length);
                    } else {
                        infos.imageThumb = null;
                    }
                    infos.description = cursor.getString(6);

                    list.add(infos);

                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return list.toArray(new BalanceSheetItem[list.size()]);
    }

    /**
     * 添加或编辑资产负债表中记录
     * @param user： 当前登录用户名
     * @param info： 待保存的数据
     * @param isAdd：是新添加，还是编辑
     * @return： 是否成功
     */
    public Boolean editWorthItem(String user, BalanceSheetItem info, Boolean isAdd) {

        if ((TextUtils.isEmpty(user)) || (null == info)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(BALANCESHEETTABLECOLUMNNAME[1],(info.worthType == BalanceSheetItem.WorthType.Property) ? 0 : 1);
        values.put(BALANCESHEETTABLECOLUMNNAME[2],info.name);
        values.put(BALANCESHEETTABLECOLUMNNAME[3],info.worth);
        values.put(BALANCESHEETTABLECOLUMNNAME[4],info.imagePath);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (null != info.imageThumb) {
            info.imageThumb.compress(Bitmap.CompressFormat.PNG,100,os);
        }
        values.put(BALANCESHEETTABLECOLUMNNAME[5],os.toByteArray());
        values.put(BALANCESHEETTABLECOLUMNNAME[6],info.description);

        String tableName = user + "_" + BALANCESHEETTABLENAME;
        if (isAdd) {
            return this.insertSQL(tableName,BALANCESHEETTABLECOLUMNNAME[2],values);
        } else {
            return this.updateSQL(tableName, values, BALANCESHEETTABLECOLUMNNAME[0] + "=?", new String[]{String.valueOf
                    (info.id)});
        }
    }

    public Boolean deleteWorthItem(String user, int id) {
        if (TextUtils.isEmpty(user)) {
            return false;
        }

        String sql = "DELETE FROM " + user + "_" + BALANCESHEETTABLENAME + " WHERE id='" + id + "'";
        return this.execSQL(sql);
    }

    //Summary
    /**
     * 获取帐户概要列表中所有记录
     * @param user:当前登录用户名
     * @return
     */
    public SummaryItem[] getAllSummaryItemInfos(String user) {
        String tableName = user + "_" + SUMMARYITEMTABLENAME;
        String sql = "SELECT * FROM " + tableName;
        ArrayList<SummaryItem> list = new ArrayList<>();

        Cursor cursor = this.querySQL(sql,null);
        if (null != cursor) {
            try {
                cursor.moveToFirst();
                SummaryItem info;
                while (!cursor.isAfterLast()) {
                    info = new SummaryItem();
                    info.id = cursor.getInt(0);
                    info.date = cursor.getString(1);
                    info.value = cursor.getInt(2);
                    info.name = cursor.getString(3);
                    info.alias = cursor.getString(4);
                    info.description = cursor.getString(5);

                    list.add(info);

                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return list.toArray(new SummaryItem[list.size()]);
    }

    /**
     * 添加或编辑帐户概要列表中记录
     * @param user： 当前登录用户名
     * @param info： 待保存的数据
     * @param isAdd：是新添加，还是编辑
     * @return： 是否成功
     */
    public Boolean editSummaryItem(String user, SummaryItem info, Boolean isAdd) {

        if ((TextUtils.isEmpty(user)) || (null == info)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(SUMMARYITEMTABLECOLUMNNAME[1],info.date);
        values.put(SUMMARYITEMTABLECOLUMNNAME[2],info.value);
        values.put(SUMMARYITEMTABLECOLUMNNAME[3],info.name);
        values.put(SUMMARYITEMTABLECOLUMNNAME[4],info.alias);
        values.put(SUMMARYITEMTABLECOLUMNNAME[5],info.description);

        String tableName = user + "_" + SUMMARYITEMTABLENAME;
        if (isAdd) {
            return this.insertSQL(tableName,SUMMARYITEMTABLECOLUMNNAME[2],values);
        } else {
            return this.updateSQL(tableName, values, SUMMARYITEMTABLECOLUMNNAME[0] + "=?", new String[]{String.valueOf
                    (info.id)});
        }
    }

    public Boolean deleteSummaryItem(String user, int id) {
        if (TextUtils.isEmpty(user)) {
            return false;
        }

        String sql = "DELETE FROM " + user + "_" + SUMMARYITEMTABLENAME + " WHERE id='" + id + "'";
        return this.execSQL(sql);
    }

    //Account
    /**
     * 获取总帐中所有记录
     * @param user:当前登录用户名
     * @return
     */
    public AccountItem[] getAllAccountItemInfos(String user) {
        String tableName = user + "_" + ACCOUNTITEMTABLENAME;
        String sql = "SELECT * FROM " + tableName;
        ArrayList<AccountItem> list = new ArrayList<>();

        Cursor cursor = this.querySQL(sql,null);
        if (null != cursor) {
            try {
                cursor.moveToFirst();
                AccountItem infos;
                while (!cursor.isAfterLast()) {
                    infos = new AccountItem();
                    infos.id = cursor.getInt(0);
                    infos.date = cursor.getString(1);
                    infos.rmb = cursor.getInt(2);
                    infos.from = cursor.getString(3);
                    infos.type = cursor.getInt(4);
                    infos.to = cursor.getString(5);
                    infos.description = cursor.getString(6);

                    list.add(infos);

                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return list.toArray(new AccountItem[list.size()]);
    }

    /**
     * 添加或编辑总帐表中记录
     * @param user： 当前登录用户名
     * @param info： 待保存的数据
     * @param isAdd：是新添加，还是编辑
     * @return： 是否成功
     */
    public Boolean editAccountItem(String user, AccountItem info, Boolean isAdd) {

        if ((TextUtils.isEmpty(user)) || (null == info)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ACCOUNTITEMTABLECOLUMNNAME[1],info.date);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[2],info.rmb);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[3],info.from);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[4],info.type);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[5],info.to);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[6],info.description);

        String tableName = user + "_" + ACCOUNTITEMTABLENAME;
        if (isAdd) {
            return this.insertSQL(tableName,ACCOUNTITEMTABLECOLUMNNAME[2],values);
        } else {
            return this.updateSQL(tableName, values, ACCOUNTITEMTABLECOLUMNNAME[0] + "=?", new String[]{String.valueOf
                    (info.id)});
        }
    }

    public Boolean deleteAccountItem(String user, int id) {
        if (TextUtils.isEmpty(user)) {
            return false;
        }

        String sql = "DELETE FROM " + user + "_" + ACCOUNTITEMTABLENAME + " WHERE id='" + id + "'";
        return this.execSQL(sql);
    }

    //Database
    private Boolean insertSQL(String table, String nullColumnHack, ContentValues values) {
        try {
            this.database.insert(table, nullColumnHack,values);
        } catch (Exception ex) {
            Log.e(ID + ".insertSQL",ex.getMessage());
            return false;
        }

        return true;
    }

    private Boolean updateSQL(String table, ContentValues values, String whereClause, String[] whereArgs) {
        try {
            this.database.update(table, values, whereClause, whereArgs);
        } catch (Exception ex) {
            Log.e(ID + ".updateSQL",ex.getMessage());
            return false;
        }

        return true;
    }

    private Boolean execSQL(String sql) {
        GlobalData.log(ID + ".execSQL",GlobalData.LogType.eMessage,sql);

        try {
            this.database.execSQL(sql);
            return true;
        } catch (Exception ex) {
            GlobalData.log(ID + ".execSQL",GlobalData.LogType.eException,ex.getMessage());
            return false;
        }
    }

    /*
    private Boolean execSQL(String sql, Objects[] objs) {

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
            return true;
        } catch (Exception ex) {
            GlobalData.log(ID + ".execSQL1",GlobalData.LogType.eException,ex.getMessage());
            return false;
        }
    }
    */

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
        //IF NOT EXISTS:创建表之前先判断，如果表存在，则此语句不执行
        String sql = "CREATE TABLE IF NOT EXISTS " + USERIDTABLENAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, password TEXT, email TEXT)";
        if (!this.execSQL(sql)) {
            return false;
        }

        String sqlCheckExist = "SELECT * FROM " + SQLITE_MASTER + " WHERE type = 'table' and name = '" + USERIDTABLENAME + "'";

        if (this.checkIsExist(sqlCheckExist,null)) {
            GlobalData.log(ID + ".createUserIDTable", GlobalData.LogType.eMessage, "USERID TABLE is created success.");
            return true;
        } else {
            GlobalData.log(ID + ".createUserIDTable", GlobalData.LogType.eMessage,"USERID TABLE is created failed.");
            return false;
        }
    }
}
