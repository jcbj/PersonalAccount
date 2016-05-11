package com.example.jc.personalaccount.Data;

import android.graphics.Bitmap;
import android.media.MediaActionSound;

import com.example.jc.personalaccount.GlobalData;
import com.example.jc.personalaccount.Utility;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jc on 16/4/21.
 */
public class SummaryItem {

    public static final String[] mDataColumnName = new String[]{"id","week","date","value","name","alias", "description","sourcedate"};

    public int id;
    public String date;
    public int value;
    public String name;
    //最长四个汉字
    public String alias;
    public String description;

    public SummaryItem() {
        this.id = -1;
    }

    public Map<String,Object> mapValue() {

        Map<String,Object> map = new HashMap<String, Object>();

        map.put(mDataColumnName[0],this.id);
        map.put(mDataColumnName[1], Utility.getWeek(GlobalData.DATEFORMAT,this.date));
        map.put(mDataColumnName[2],this.date.substring(5));
        map.put(mDataColumnName[3],Double.toString(this.value / 100.0));
        map.put(mDataColumnName[4],this.name);
        map.put(mDataColumnName[5],this.alias);
        map.put(mDataColumnName[6],this.description);
        map.put(mDataColumnName[7],this.date);

        return map;
    }

    public SummaryItem(Map<String,Object> mapValue) {
        try {
            this.id = Integer.parseInt(mapValue.get(mDataColumnName[0]).toString());
            this.date = mapValue.get(mDataColumnName[7]).toString();
            this.value = (int)(Double.parseDouble(mapValue.get(mDataColumnName[3]).toString()) * 100);
            this.name = mapValue.get(mDataColumnName[4]).toString();
            this.alias = mapValue.get(mDataColumnName[5]).toString();
            this.description = mapValue.get(mDataColumnName[6]).toString();
        } catch (Exception ex){
            this.id = -1;
        }
    }
}
