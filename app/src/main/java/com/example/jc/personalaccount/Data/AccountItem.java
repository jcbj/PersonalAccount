package com.example.jc.personalaccount.Data;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by jc on 2016/4/21.
 */
public class AccountItem {

    public static final String[] mDataColumnName = new String[]{"id","week","date","rmb","from","image","to","description"};

    public int id;
    public Date date;
    public int rmb;
    public String from;
    public int imageid;
    public String to;
    public String description;
}
