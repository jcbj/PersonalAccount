package com.example.jc.personalaccount.DatabaseManger;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.BalanceSheetItem;
import com.example.jc.personalaccount.Data.CarItem;
import com.example.jc.personalaccount.Data.DetailItem;
import com.example.jc.personalaccount.Data.SummaryItem;
import com.example.jc.personalaccount.GlobalData;
import com.example.jc.personalaccount.Utility;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileOutputStream;
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
            "fromname",
            "type",
            "toname",
            "description"};
    private static final String DETAILITEMTABLENAME = "DetailItemTable";
    private static final String[] DETAILITEMTABLECOLUMNNAME = new String[]{
            "id",
            "date",
            "value",
            "fromname",
            "description"};
    private static final String CARITEMTABLENAME = "CarItemTable";
    private static final String[] CARITEMTABLECOLUMNNAME = new String[]{
            "id",
            "date",
            "value",
            "type",
            "description"};

    //当前时间转换为文件名字符串
    private static final String CURRENTDATETOFILENAME = "yyyy-MM-dd_HH-mm-ss";

    private static final String LINEEND = "\r\n";

    private SQLiteDatabase database = null;
    private String mDatabasePath = null;
    private String mCurrentLoginUserID = null;

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
            mDatabasePath = file.getPath();

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

        if (!bIsSuccess) {
            mDatabasePath = null;
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

    public Boolean register(String name, String password, String email) {
        String sqlCheckExist = "SELECT * FROM " + USERIDTABLENAME + " WHERE name = ?";

        if (this.checkIsExist(sqlCheckExist,new String[] {name})) {

            GlobalData.log(ID + ".register", GlobalData.LogType.eMessage,name + " is register but name is exist");

            return false;
        }

        String  sql = "INSERT INTO " + USERIDTABLENAME + " (name,password,email) values('" + name + "','" + password + "','" + email +"')";
        this.execSQL(sql);

        if (this.checkIsExist(sqlCheckExist,new String[] {name})) {

            if (this.createdUserIDDataStore(name)) {

                GlobalData.log(ID + ".register", GlobalData.LogType.eMessage,name + " is register success");
                return true;
            }

            GlobalData.log(ID + ".register", GlobalData.LogType.eMessage,name + " is register failed, create table is failed.");
            return false;
        }

        GlobalData.log(ID + ".register", GlobalData.LogType.eMessage,name + " is register failed");

        return false;
    }

    public Boolean login(String name, String password) {
        String sql = "SELECT * FROM " + USERIDTABLENAME + " WHERE name = ? AND password = ?";
        Boolean bIsSuccess = this.checkIsExist(sql, new String[]{name, password});

        GlobalData.log(ID + ".login", GlobalData.LogType.eMessage,name + " is login " + (bIsSuccess?"success.":"failed."));

        if (bIsSuccess) {
            this.mCurrentLoginUserID = name;
        }

        return bIsSuccess;
    }

    public void logout() {
        this.mCurrentLoginUserID = null;
    }

    public String getCurrentUserID() {
        return this.mCurrentLoginUserID;
    }

    //Export
    public Boolean exportDataStore() {

        String destPath = this.getExportPath() + Utility.getFormatDate(CURRENTDATETOFILENAME) + DATABASENAME;

        return Utility.copyFile(this.mDatabasePath,destPath);
    }

    public Boolean exportCSV(int type) {

        switch (type) {
            case 0:
                return this.exportCSV_Home();
            case 1:
                return this.exportCSV_Summary();
            case 2:
                return this.exportCSV_Account();
            case 3:
                return this.exportCSV_Detail();
            case 4:
                return this.exportCSV_Car();
            default:
                return false;
        }
    }

    //Worth
    /**
     * 获取资产负债表中所有记录
     * @return
     */
    public BalanceSheetItem[] getAllBalanceSheetItems() {
        String tableName = this.mCurrentLoginUserID + "_" + BALANCESHEETTABLENAME;
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
                    infos.value = cursor.getInt(3);
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
     * @param info： 待保存的数据
     * @param isAdd：是新添加，还是编辑
     * @return： 是否成功
     */
    public Boolean editWorthItem(BalanceSheetItem info, Boolean isAdd) {

        if ((TextUtils.isEmpty(this.mCurrentLoginUserID)) || (null == info)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(BALANCESHEETTABLECOLUMNNAME[1],(info.worthType == BalanceSheetItem.WorthType.Property) ? 0 : 1);
        values.put(BALANCESHEETTABLECOLUMNNAME[2],info.name);
        values.put(BALANCESHEETTABLECOLUMNNAME[3],info.value);
        values.put(BALANCESHEETTABLECOLUMNNAME[4],info.imagePath);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (null != info.imageThumb) {
            info.imageThumb.compress(Bitmap.CompressFormat.PNG,100,os);
        }
        values.put(BALANCESHEETTABLECOLUMNNAME[5],os.toByteArray());
        values.put(BALANCESHEETTABLECOLUMNNAME[6],info.description);

        String tableName = this.mCurrentLoginUserID + "_" + BALANCESHEETTABLENAME;
        if (isAdd) {
            return this.insertSQL(tableName,BALANCESHEETTABLECOLUMNNAME[2],values);
        } else {
            return this.updateSQL(tableName, values, BALANCESHEETTABLECOLUMNNAME[0] + "=?", new String[]{String.valueOf
                    (info.id)});
        }
    }

    public Boolean deleteWorthItem(int id) {
        if (TextUtils.isEmpty(this.mCurrentLoginUserID)) {
            return false;
        }

        String sql = "DELETE FROM " + this.mCurrentLoginUserID + "_" + BALANCESHEETTABLENAME + " WHERE id='" + id + "'";
        return this.execSQL(sql);
    }

    //Summary
    /**
     * 获取帐户概要列表中所有记录
     * @return
     */
    public SummaryItem[] getAllSummaryItems() {
        String tableName = this.mCurrentLoginUserID + "_" + SUMMARYITEMTABLENAME;
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
     * @param info： 待保存的数据
     * @param isAdd：是新添加，还是编辑
     * @return： 是否成功
     */
    public Boolean editSummaryItem(SummaryItem info, Boolean isAdd) {

        if ((TextUtils.isEmpty(this.mCurrentLoginUserID)) || (null == info)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(SUMMARYITEMTABLECOLUMNNAME[1],info.date);
        values.put(SUMMARYITEMTABLECOLUMNNAME[2],info.value);
        values.put(SUMMARYITEMTABLECOLUMNNAME[3],info.name);
        values.put(SUMMARYITEMTABLECOLUMNNAME[4],info.alias);
        values.put(SUMMARYITEMTABLECOLUMNNAME[5],info.description);

        String tableName = this.mCurrentLoginUserID + "_" + SUMMARYITEMTABLENAME;
        if (isAdd) {
            return this.insertSQL(tableName,SUMMARYITEMTABLECOLUMNNAME[2],values);
        } else {
            return this.updateSQL(tableName, values, SUMMARYITEMTABLECOLUMNNAME[0] + "=?", new String[]{String.valueOf
                    (info.id)});
        }
    }

    public Boolean deleteSummaryItem(int id) {
        if (TextUtils.isEmpty(this.mCurrentLoginUserID)) {
            return false;
        }

        String sql = "DELETE FROM " + this.mCurrentLoginUserID + "_" + SUMMARYITEMTABLENAME + " WHERE id='" + id + "'";
        return this.execSQL(sql);
    }

    public String[] getAllAccountAlias() {
        String tableName = this.mCurrentLoginUserID + "_" + SUMMARYITEMTABLENAME;
        String sql = "SELECT * FROM " + tableName;
        ArrayList<String> list = new ArrayList<>();

        Cursor cursor = this.querySQL(sql,null);
        if (null != cursor) {
            try {
                cursor.moveToFirst();
                SummaryItem info;
                while (!cursor.isAfterLast()) {
                    list.add(cursor.getString(4));

                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return list.toArray(new String[list.size()]);
    }

    //Account
    /**
     * 获取总帐中所有记录
     * @return
     */
    public AccountItem[] getAllAccountItems() {
        String tableName = this.mCurrentLoginUserID + "_" + ACCOUNTITEMTABLENAME;
        String sql = "SELECT * FROM " + tableName;
        ArrayList<AccountItem> list = new ArrayList<>();

        Cursor cursor = this.querySQL(sql,null);
        if (null != cursor) {
            try {
                cursor.moveToFirst();
                AccountItem info;
                while (!cursor.isAfterLast()) {
                    info = new AccountItem();
                    info.id = cursor.getInt(0);
                    info.date = cursor.getString(1);
                    info.value = cursor.getInt(2);
                    info.from = cursor.getString(3);
                    info.type = cursor.getInt(4);
                    info.to = cursor.getString(5);
                    info.description = cursor.getString(6);

                    list.add(info);

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
     * @param info： 待保存的数据
     * @param isAdd：是新添加，还是编辑
     * @return： 是否成功
     */
    public Boolean editAccountItem(AccountItem info, Boolean isAdd) {

        if ((TextUtils.isEmpty(this.mCurrentLoginUserID)) || (null == info)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(ACCOUNTITEMTABLECOLUMNNAME[1],info.date);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[2],info.value);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[3],info.from);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[4],info.type);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[5],info.to);
        values.put(ACCOUNTITEMTABLECOLUMNNAME[6],info.description);

        String tableName = this.mCurrentLoginUserID + "_" + ACCOUNTITEMTABLENAME;
        if (isAdd) {
            return this.insertSQL(tableName,ACCOUNTITEMTABLECOLUMNNAME[2],values);
        } else {
            return this.updateSQL(tableName, values, ACCOUNTITEMTABLECOLUMNNAME[0] + "=?", new String[]{String.valueOf
                    (info.id)});
        }
    }

    public Boolean deleteAccountItem(int id) {
        if (TextUtils.isEmpty(this.mCurrentLoginUserID)) {
            return false;
        }

        String sql = "DELETE FROM " + this.mCurrentLoginUserID + "_" + ACCOUNTITEMTABLENAME + " WHERE id='" + id + "'";
        return this.execSQL(sql);
    }

    //Detail
    /**
     * 获取详细消费中所有记录
     * @return
     */
    public DetailItem[] getAllDetailItems() {
        String tableName = this.mCurrentLoginUserID + "_" + DETAILITEMTABLENAME;
        String sql = "SELECT * FROM " + tableName;
        ArrayList<DetailItem> list = new ArrayList<>();

        Cursor cursor = this.querySQL(sql,null);
        if (null != cursor) {
            try {
                cursor.moveToFirst();
                DetailItem info;
                while (!cursor.isAfterLast()) {
                    info = new DetailItem();
                    info.id = cursor.getInt(0);
                    info.date = cursor.getString(1);
                    info.value = cursor.getInt(2);
                    info.from = cursor.getString(3);
                    info.description = cursor.getString(4);

                    list.add(info);

                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return list.toArray(new DetailItem[list.size()]);
    }

    public Boolean addDetailItem(DetailItem[] infos) {
        try {
            if ((TextUtils.isEmpty(this.mCurrentLoginUserID)) || (null == infos)) {
                return false;
            }

            boolean bIsSuccess = false;

            String tableName = this.mCurrentLoginUserID + "_" + DETAILITEMTABLENAME;
            for (int i = 0; i < infos.length; i++) {

                ContentValues values = new ContentValues();
                values.put(DETAILITEMTABLECOLUMNNAME[1],infos[i].date);
                values.put(DETAILITEMTABLECOLUMNNAME[2],infos[i].value);
                values.put(DETAILITEMTABLECOLUMNNAME[3],infos[i].from);
                values.put(DETAILITEMTABLECOLUMNNAME[4],infos[i].description);

                bIsSuccess = this.insertSQL(tableName,DETAILITEMTABLECOLUMNNAME[2],values);
                if (!bIsSuccess) {
                    break;
                }
            }

            return bIsSuccess;
        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    /**
     * 添加或编辑详细消费表中记录
     * @param info： 待保存的数据
     * @return： 是否成功
     */
    public Boolean editDetailItem(DetailItem info) {

        if ((TextUtils.isEmpty(this.mCurrentLoginUserID)) || (null == info)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(DETAILITEMTABLECOLUMNNAME[1],info.date);
        values.put(DETAILITEMTABLECOLUMNNAME[2],info.value);
        values.put(DETAILITEMTABLECOLUMNNAME[3],info.from);
        values.put(DETAILITEMTABLECOLUMNNAME[4],info.description);

        String tableName = this.mCurrentLoginUserID + "_" + DETAILITEMTABLENAME;

        return this.updateSQL(tableName, values, DETAILITEMTABLECOLUMNNAME[0] + "=?", new String[]{String.valueOf
                    (info.id)});
    }

    public Boolean deleteDetailItem(int id) {
        if (TextUtils.isEmpty(this.mCurrentLoginUserID)) {
            return false;
        }

        String sql = "DELETE FROM " + this.mCurrentLoginUserID + "_" + DETAILITEMTABLENAME + " WHERE id='" + id + "'";
        return this.execSQL(sql);
    }

    //Account
    /**
     * 获取汽车费用中所有记录
     * @return
     */
    public CarItem[] getAllCarItems() {
        String tableName = this.mCurrentLoginUserID + "_" + CARITEMTABLENAME;
        String sql = "SELECT * FROM " + tableName;
        ArrayList<CarItem> list = new ArrayList<>();

        Cursor cursor = this.querySQL(sql,null);
        if (null != cursor) {
            try {
                cursor.moveToFirst();
                CarItem info;
                while (!cursor.isAfterLast()) {
                    info = new CarItem();
                    info.id = cursor.getInt(0);
                    info.date = cursor.getString(1);
                    info.value = cursor.getInt(2);
                    info.type = cursor.getString(3);
                    info.description = cursor.getString(4);

                    list.add(info);

                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return list.toArray(new CarItem[list.size()]);
    }

    /**
     * 添加或编辑汽车费用表中记录
     * @param info： 待保存的数据
     * @param isAdd：是新添加，还是编辑
     * @return： 是否成功
     */
    public Boolean editCarItem(CarItem info, Boolean isAdd) {

        if ((TextUtils.isEmpty(this.mCurrentLoginUserID)) || (null == info)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(CARITEMTABLECOLUMNNAME[1],info.date);
        values.put(CARITEMTABLECOLUMNNAME[2],info.value);
        values.put(CARITEMTABLECOLUMNNAME[3],info.type);
        values.put(CARITEMTABLECOLUMNNAME[4],info.description);

        String tableName = this.mCurrentLoginUserID + "_" + CARITEMTABLENAME;
        if (isAdd) {
            return this.insertSQL(tableName,CARITEMTABLECOLUMNNAME[2],values);
        } else {
            return this.updateSQL(tableName, values, CARITEMTABLECOLUMNNAME[0] + "=?", new String[]{String.valueOf
                    (info.id)});
        }
    }

    public Boolean deleteCarItem(int id) {
        if (TextUtils.isEmpty(this.mCurrentLoginUserID)) {
            return false;
        }

        String sql = "DELETE FROM " + this.mCurrentLoginUserID + "_" + CARITEMTABLENAME + " WHERE id='" + id + "'";
        return this.execSQL(sql);
    }

    //Database
    //每个注册用户，都有自己单独的表来存储数据；以注册用户名来区别
    private Boolean createdUserIDDataStore(String user) {
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

        //2,创建账户概要表
        tableName = user + "_" + SUMMARYITEMTABLENAME;
        sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" + SUMMARYITEMTABLECOLUMNNAME[0] + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SUMMARYITEMTABLECOLUMNNAME[1] + " TEXT," +
                SUMMARYITEMTABLECOLUMNNAME[2] + "  INT," +
                SUMMARYITEMTABLECOLUMNNAME[3] + " TEXT," +
                SUMMARYITEMTABLECOLUMNNAME[4] + " TEXT," +
                SUMMARYITEMTABLECOLUMNNAME[5] + " TEXT)";
        this.execSQL(sql);

        sql = "SELECT * FROM " + SQLITE_MASTER + " WHERE type = 'table' and name = '" + tableName + "'";
        if (!this.checkIsExist(sql,null)) {
            Log.e(ID + ".createdUserIDDataStore", "create " + tableName + " is failed.");
        }

        //3,创建账户表
        tableName = user + "_" + ACCOUNTITEMTABLENAME;
        sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" + ACCOUNTITEMTABLECOLUMNNAME[0] + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ACCOUNTITEMTABLECOLUMNNAME[1] + " TEXT," +
                ACCOUNTITEMTABLECOLUMNNAME[2] + "  INT," +
                ACCOUNTITEMTABLECOLUMNNAME[3] + " TEXT," +
                ACCOUNTITEMTABLECOLUMNNAME[4] + " INT," +
                ACCOUNTITEMTABLECOLUMNNAME[5] + " TEXT," +
                ACCOUNTITEMTABLECOLUMNNAME[6] + " TEXT)";
        this.execSQL(sql);

        sql = "SELECT * FROM " + SQLITE_MASTER + " WHERE type = 'table' and name = '" + tableName + "'";
        if (!this.checkIsExist(sql,null)) {
            Log.e(ID + ".createdUserIDDataStore", "create " + tableName + " is failed.");
        }

        //4,创建详细消费表
        tableName = user + "_" + DETAILITEMTABLENAME;
        sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" + DETAILITEMTABLECOLUMNNAME[0] + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DETAILITEMTABLECOLUMNNAME[1] + " TEXT," +
                DETAILITEMTABLECOLUMNNAME[2] + "  INT," +
                DETAILITEMTABLECOLUMNNAME[3] + " TEXT," +
                DETAILITEMTABLECOLUMNNAME[4] + " TEXT)";
        this.execSQL(sql);

        sql = "SELECT * FROM " + SQLITE_MASTER + " WHERE type = 'table' and name = '" + tableName + "'";
        if (!this.checkIsExist(sql,null)) {
            Log.e(ID + ".createdUserIDDataStore", "create " + tableName + " is failed.");
        }

        //3,创建汽车费用表
        tableName = user + "_" + CARITEMTABLENAME;
        sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" + CARITEMTABLECOLUMNNAME[0] + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CARITEMTABLECOLUMNNAME[1] + " TEXT," +
                CARITEMTABLECOLUMNNAME[2] + "  INT," +
                CARITEMTABLECOLUMNNAME[3] + " TEXT," +
                CARITEMTABLECOLUMNNAME[4] + " TEXT)";
        this.execSQL(sql);

        sql = "SELECT * FROM " + SQLITE_MASTER + " WHERE type = 'table' and name = '" + tableName + "'";
        if (!this.checkIsExist(sql,null)) {
            Log.e(ID + ".createdUserIDDataStore", "create " + tableName + " is failed.");
        }

        return false;
    }

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

    //./PersonalAccount/
    private String getExportPath() {

        String destPath = Environment.getExternalStorageDirectory().getPath();

        destPath = destPath + "/PersonalAccount/";
        File file = new File(destPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        return destPath;
    }

    private Boolean exportCSV_Home() {

        BalanceSheetItem[] items = this.getAllBalanceSheetItems();

        if (items.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("id,name,description,type,value" + LINEEND);
            for (int i = 0; i < items.length; i++) {
                sb.append(
                    items[i].id + ",'" +
                    items[i].name + "','" +
                    items[i].description + "'," +
                    ((BalanceSheetItem.WorthType.Property == items[i].worthType) ? 0 : 1) + "," +
                    items[i].value + LINEEND
                );
            }

            String destPath = this.getExportPath() + Utility.getFormatDate(CURRENTDATETOFILENAME) + BALANCESHEETTABLENAME + ".csv";
            return Utility.writeStringToFile (destPath,sb.toString(),false);
        }

        return false;
    }

    private Boolean exportCSV_Summary() {

        SummaryItem[] items = this.getAllSummaryItems();

        if (items.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("id,date,name,alias,description,value" + LINEEND);
            for (int i = 0; i < items.length; i++) {
                sb.append(
                        items[i].id + "," +
                        items[i].date + ",'" +
                        items[i].name + "','" +
                        items[i].alias + "','" +
                        items[i].description + "'," +
                        items[i].value + LINEEND
                );
            }

            String destPath = this.getExportPath() + Utility.getFormatDate(CURRENTDATETOFILENAME) + SUMMARYITEMTABLENAME + ".csv";
            return Utility.writeStringToFile (destPath,sb.toString(),false);
        }

        return false;
    }

    private Boolean exportCSV_Account() {

        AccountItem[] items = this.getAllAccountItems();

        if (items.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("id,date,type,value,from,to,description" + LINEEND);

            String destPath = this.getExportPath() + Utility.getFormatDate(CURRENTDATETOFILENAME) + ACCOUNTITEMTABLENAME + ".csv";

            for (int i = 0; i < items.length; i++) {
                sb.append(
                        items[i].id + "," +
                        items[i].date + "," +
                        items[i].type + "," +
                        items[i].value + ",'" +
                        items[i].from + "','" +
                        items[i].to + "','" +
                        items[i].description + "'" + LINEEND
                );

                if (0 == i % 500) {
                    if (Utility.writeStringToFile (destPath,sb.toString(),true)) {
                        sb = new StringBuilder();
                    } else {
                        return false;
                    }
                }
            }

            return Utility.writeStringToFile (destPath,sb.toString(),true);
        }

        return false;
    }

    private Boolean exportCSV_Detail() {

        DetailItem[] items = this.getAllDetailItems();

        if (items.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("id,date,value,from,description" + LINEEND);

            String destPath = this.getExportPath() + Utility.getFormatDate(CURRENTDATETOFILENAME) + DETAILITEMTABLENAME + ".csv";

            for (int i = 0; i < items.length; i++) {
                sb.append(
                        items[i].id + "," +
                                items[i].date + "," +
                                items[i].value + ",'" +
                                items[i].from + "','" +
                                items[i].description + "'" + LINEEND
                );

                if (0 == i % 500) {
                    if (Utility.writeStringToFile (destPath,sb.toString(),true)) {
                        sb = new StringBuilder();
                    } else {
                        return false;
                    }
                }
            }

            return Utility.writeStringToFile (destPath,sb.toString(),true);
        }

        return false;
    }

    private Boolean exportCSV_Car() {

        CarItem[] items = this.getAllCarItems();

        if (items.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("id,date,value,type,description" + LINEEND);

            String destPath = this.getExportPath() + Utility.getFormatDate(CURRENTDATETOFILENAME) + CARITEMTABLENAME + ".csv";

            for (int i = 0; i < items.length; i++) {
                sb.append(
                        items[i].id + "," +
                        items[i].date + "," +
                        items[i].value + ",'" +
                        items[i].type + "','" +
                        items[i].description + "'" + LINEEND
                );

                if (0 == i % 500) {
                    if (Utility.writeStringToFile (destPath,sb.toString(),true)) {
                        sb = new StringBuilder();
                    } else {
                        return false;
                    }
                }
            }

            return Utility.writeStringToFile (destPath,sb.toString(),true);
        }

        return false;
    }
}
