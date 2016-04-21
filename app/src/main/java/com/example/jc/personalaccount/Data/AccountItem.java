package com.example.jc.personalaccount.Data;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by jc on 2016/4/21.
 */
public class AccountItem {

    public static final String[] mDataColumnName = new String[]{"id","week","date","value","from","image","to",
            "description"};

    public int id;
    //2016-04-21
    public String date;
    public int value;
    public String from;
    //0-实际支出，1-转账支出，2-实际收入，3-转账收入
    public int type;
    public String to;
    public String description;
}
