package com.example.jc.personalaccount.Data;

/**
 * Created by jc on 2016/4/18.
 */
public enum HomeEditOperType {
    HOME_EDIT_OPER_TYPE_ADDPROPERTY(0),
    HOME_EDIT_OPER_TYPE_ADDDEBT(1),
    HOME_EDIT_OPER_TYPE_EDITPROPERTY(2),
    HOME_EDIT_OPER_TYPE_EDITDEBT(3);

    private final int mValue;
    //必须private,否则编译错误
    HomeEditOperType(int value) {
        this.mValue = value;
    }

    //自定义从int到enum
    public static HomeEditOperType valueOf(int value) {
        switch (value) {
            case 0:
                return HOME_EDIT_OPER_TYPE_ADDPROPERTY;
            case 1:
                return HOME_EDIT_OPER_TYPE_ADDDEBT;
            case 2:
                return HOME_EDIT_OPER_TYPE_EDITPROPERTY;
            case 3:
                return HOME_EDIT_OPER_TYPE_EDITDEBT;
            default:
                return HOME_EDIT_OPER_TYPE_ADDPROPERTY;
        }
    }

    //从enum到int
    public int value() {
        return this.mValue;
    }
}
