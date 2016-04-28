package com.example.jc.personalaccount.Data;

import com.example.jc.personalaccount.MainActivity;
import com.example.jc.personalaccount.Utility;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by jc on 16/4/28.
 */
public class DetailItem {

    public static final String[] mDataColumnName = new String[]{"id","week","date","value","from","description",
            "sourcedate"};

    public int id;
    public String date;
    public int value;
    public String from;
    public String description;

    public DetailItem() { this.id = -1; }

    public Map<String, Object> mapValue() {

        Map<String, Object> map = new Hashtable<String, Object>();

        map.put(mDataColumnName[0],this.id);
        map.put(mDataColumnName[1], Utility.getWeek("yyyy-MM-dd",this.date));
        map.put(mDataColumnName[2],this.date.substring(5));
        map.put(mDataColumnName[3],Double.toString(this.value / 100.0));
        map.put(mDataColumnName[4],this.from);
        map.put(mDataColumnName[5],this.description);
        map.put(mDataColumnName[6],this.date);

        return map;
    }

    public DetailItem(Map<String, Object> mapValue) {
        try {
            this.id = Integer.parseInt(mapValue.get(mDataColumnName[0]).toString());
            this.date = mapValue.get(mDataColumnName[6]).toString();
            this.value = (int)(Double.parseDouble(mapValue.get(mDataColumnName[3]).toString()) * 100);
            this.from = mapValue.get(mDataColumnName[4]).toString();
            this.description = mapValue.get(mDataColumnName[5]).toString();
        } catch (Exception ex){
            this.id = -1;
        }
    }
}
