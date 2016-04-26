package com.example.jc.personalaccount.Data;

/**
 * Created by jc on 2016/4/26.
 */
public enum FragmentID {
    HOME(0),
    SUMMARY(1),
    ACCOUNT(2),
    DETAIL(3),
    CAR(4);

    private final int mValue;

    FragmentID(int value) {
        this.mValue = value;
    }

    public static FragmentID valueOf(int value) {
        switch (value) {
            case 0:
                return HOME;
            case 1:
                return SUMMARY;
            case 2:
                return ACCOUNT;
            case 3:
                return DETAIL;
            case 4:
                return CAR;
            default:
                return HOME;
        }
    }

    public int value() {
        return this.mValue;
    }
}
