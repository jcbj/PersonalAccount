package com.example.jc.personalaccount.Data;

/**
 * Created by jc on 2016/4/25.
 */
public enum SummaryEditOperType {

    SUMMARY_EDIT_OPER_TYPE_ADD(0),
    SUMMARY_EDIT_OPER_TYPE_EDIT(1),
    SUMMARY_EDIT_OPER_TYPE_VIEW(2);

    private final int mValue;

    SummaryEditOperType(int value) {
        this.mValue = value;
    }

    public static SummaryEditOperType valueOf(int value) {
        switch (value) {
            case 0:
                return SUMMARY_EDIT_OPER_TYPE_ADD;
            case 1:
                return SUMMARY_EDIT_OPER_TYPE_EDIT;
            case 2:
                return SUMMARY_EDIT_OPER_TYPE_VIEW;
            default:
                return SUMMARY_EDIT_OPER_TYPE_ADD;
        }
    }

    public int value() {
        return this.mValue;
    }
}
