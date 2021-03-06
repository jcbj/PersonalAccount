package com.example.jc.personalaccount.Data;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.jc.personalaccount.GlobalData;
import com.example.jc.personalaccount.R;
import com.example.jc.personalaccount.Utility;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

/**
 * Created by jc on 2016/4/21.
 */
public class AccountItem {

    public static final String[] mDataColumnName = new String[]{"id","week","date","value","from","image","to",
            "description","sourcedate","sourcetype"};
    public static final int[] mTypeImageID = new int[]{
            R.drawable.arrow_from_red,
            R.drawable.arrow_from_gray,
            R.drawable.arrow_to_green,
            R.drawable.arrow_to_gray};
    public static final int[] mTypeTextColor = new int[]{
            Color.argb(0xFF,0xF2,0x8E,0x7F),
            Color.argb(0xFF,0x71,0x71,0x71),
            Color.argb(0xFF,0x57,0xC8,0xA3),
            Color.argb(0xFF,0x71,0x71,0x71)};

    public int id;
    //2016-04-21
    public String date;
    public int value;
    public String from;
    //0-实际支出，1-转账支出，2-实际收入，3-转账收入
    public int type;
    public String to;
    public String description;

    public int listItemType = GlobalData.LISTITEMTYPE;
    public boolean bIsExpand = false;

    public AccountItem() { this.id = -1; }

    public Map<String, Object> mapValue() {
        Map<String, Object> map = new Hashtable<String, Object>();

        map.put(mDataColumnName[0],this.id);
        map.put(mDataColumnName[1], Utility.getWeek(GlobalData.DATEFORMAT,this.date));
        map.put(mDataColumnName[2],this.date.substring(5));
        map.put(mDataColumnName[3],Double.toString(this.value / 100.0));
        map.put(mDataColumnName[4],this.from);
        map.put(mDataColumnName[5],mTypeImageID[this.type]);
        map.put(mDataColumnName[6],this.to);
        map.put(mDataColumnName[7],this.description);
        map.put(mDataColumnName[8],this.date);
        map.put(mDataColumnName[9],this.type);

        return map;
    }

    public AccountItem(Map<String,Object> mapValue) {
        try {
            this.id = Integer.parseInt(mapValue.get(mDataColumnName[0]).toString());
            this.date = mapValue.get(mDataColumnName[8]).toString();
            this.value = (int)(Double.parseDouble(mapValue.get(mDataColumnName[3]).toString()) * 100);
            this.from = mapValue.get(mDataColumnName[4]).toString();
            this.type = Integer.parseInt(mapValue.get(mDataColumnName[9]).toString());
            this.to = mapValue.get(mDataColumnName[6]).toString();
            this.description = mapValue.get(mDataColumnName[7]).toString();
        } catch (Exception ex){
            this.id = -1;
        }
    }
}
