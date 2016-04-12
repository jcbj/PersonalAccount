package com.example.jc.personalaccount.Data;

import android.graphics.Bitmap;

/**
 * 资产负债表中数据信息
 * Created by jc on 16/4/11.
 */
public class BalanceSheetItem {

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
}
