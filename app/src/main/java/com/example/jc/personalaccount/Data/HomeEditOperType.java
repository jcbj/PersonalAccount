package com.example.jc.personalaccount.Data;

/**
 * Created by jc on 2016/4/18.
 */
public enum HomeEditOperType {
    ADDPROPERTY(0),
    ADDDEBT(1),
    EDITPROPERTY(2),
    EDITDEBT(3),
    VIEWPROPERTY(4),
    VIEWDEBT(5);

    private final int mValue;
    //必须private,否则编译错误
    HomeEditOperType(int value) {
        this.mValue = value;
    }

    //自定义从int到enum
    public static HomeEditOperType valueOf(int value) {
        switch (value) {
            case 0:
                return ADDPROPERTY;
            case 1:
                return ADDDEBT;
            case 2:
                return EDITPROPERTY;
            case 3:
                return EDITDEBT;
            case 4:
                return VIEWPROPERTY;
            case 5:
                return VIEWDEBT;
            default:
                return ADDPROPERTY;
        }
    }

    //从enum到int
    public int value() {
        return this.mValue;
    }
}
