package com.example.jc.personalaccount.Data;

/**
 * Created by jc on 16/4/21.
 */
public class SummaryItem {

    public static final String[] mDataColumnName = new String[]{"id","week","date","value","name","alias", "description"};

    public int id;
    public String date;
    public int value;
    public String name;
    //最长四个汉字
    public String alias;
    public String description;
}
