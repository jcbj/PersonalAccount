package com.example.jc.personalaccount.Data;

/**
 * Created by jc on 2016/4/25.
 */
public enum EditCommonOperType {

    ADD(0),
    EDIT(1),
    VIEW(2);

    private final int mValue;

    EditCommonOperType(int value) {
        this.mValue = value;
    }

    public static EditCommonOperType valueOf(int value) {
        switch (value) {
            case 0:
                return ADD;
            case 1:
                return EDIT;
            case 2:
                return VIEW;
            default:
                return ADD;
        }
    }

    public int value() {
        return this.mValue;
    }
}
