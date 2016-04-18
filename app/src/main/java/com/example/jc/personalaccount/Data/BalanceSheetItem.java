package com.example.jc.personalaccount.Data;

import android.graphics.Bitmap;

import com.example.jc.personalaccount.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 资产负债表中数据信息
 * Created by jc on 16/4/11.
 */
public class BalanceSheetItem {

    public static final String[] mDataColumnName = new String[]{"id","name","worth","description","imageThumb","imagePath","type"};

    public enum WorthType {
        Property,
        Debt
    }

    public int id;
    public WorthType worthType;     //资产，负债
    public String imagePath;        //图片路径
    public Bitmap imageThumb;       //缩略图
    public String name;             //名称
    public int worth;               //价值 double = worth / 100.0
    public String description;      //描述

    public Map<String,Object> mapValue() {

        Map<String,Object> map = new HashMap<String, Object>();

        map.put(mDataColumnName[0],this.id);
        map.put(mDataColumnName[1],this.name);
        map.put(mDataColumnName[2],Double.toString(this.worth / 100.0));
        map.put(mDataColumnName[3],this.description);
        map.put(mDataColumnName[4],this.imageThumb);
        map.put(mDataColumnName[5],this.imagePath);
        map.put(mDataColumnName[6],this.worthType);

        return map;
    }

    public BalanceSheetItem() {
        this.id = -1;
    }

    public BalanceSheetItem(Map<String,Object> mapValue) {
        try {
            this.id = Integer.parseInt(mapValue.get(mDataColumnName[0]).toString());
            this.name = mapValue.get(mDataColumnName[1]).toString();
            this.worth = (int)(Double.parseDouble(mapValue.get(mDataColumnName[2]).toString()) * 100);
            this.description = mapValue.get(mDataColumnName[3]).toString();
            this.imageThumb = (Bitmap)(mapValue.get(mDataColumnName[4]));
            if (null != mapValue.get(mDataColumnName[5])) {
                this.imagePath = mapValue.get(mDataColumnName[5]).toString();
            }
            this.worthType = (WorthType)(mapValue.get(mDataColumnName[6]));
        } catch (Exception ex){
            this.id = -1;
        }
    }
}
