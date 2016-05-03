package com.example.jc.personalaccount;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by jc on 16/4/7.
 *
 * @author jc
 * @功能描述：自定义底部工具栏
 */
public class ViewIndicator extends LinearLayout implements View.OnClickListener {
    private int mDefaultIndicator = 0; // 默认的选定View

    private static int mCurIndicator; // 当前选定View

    private static View[] mIndicators; // View集合

    private OnIndicateListener mOnIndicateListener; // 对应的监听器
    // 对应的图标Tag
    private static final String[] mTAG_ICON = new String[]{"icon_tag_0","icon_tag_1","icon_tag_2","icon_tag_3",
            "icon_tag_4","icon_tag_5"};
    // 对应的文字Tag
    private static final String[] mTAG_TEXT = new String[]{"text_tag_0","text_tag_1","text_tag_2","text_tag_3",
            "text_tag_4","text_tag_4"};

    private static final int[] mAllIconResID_Normal = new int[]{
            R.drawable.main_tab_item_home_normal,
            R.drawable.main_tab_item_summary_normal,
            R.drawable.main_tab_item_account_normal,
            R.drawable.main_tab_item_detail_normal,
            R.drawable.main_tab_item_car_normal,
            R.drawable.main_tab_item_setting_normal
    };

    private static final int[] mAllIconResID_Focus = new int[]{
            R.drawable.main_tab_item_home_focus,
            R.drawable.main_tab_item_summary_focus,
            R.drawable.main_tab_item_account_focus,
            R.drawable.main_tab_item_detail_focus,
            R.drawable.main_tab_item_car_focus,
            R.drawable.main_tab_item_setting_focus
    };

    private static final int[] mAllXmlResID = new int[]{
            R.drawable.main_tab_item_home,
            R.drawable.main_tab_item_summary,
            R.drawable.main_tab_item_account,
            R.drawable.main_tab_item_detail,
            R.drawable.main_tab_item_car,
            R.drawable.main_tab_item_setting
    };

    private static final int[] mAllStringResID = new int[]{
            R.string.bottom_tab_home,
            R.string.bottom_tab_summary,
            R.string.bottom_tab_account,
            R.string.bottom_tab_detail,
            R.string.bottom_tab_car,
            R.string.bottom_tab_setting
    };

    // 未选中状态
    private static final int COLOR_UNSELECT = Color.argb(100, 0xff, 0xff, 0xff);
    // 选中状态
    private static final int COLOR_SELECT = Color.WHITE;

    //构造函数
    public ViewIndicator(Context context) {
        super(context);
    }

    public ViewIndicator(Context context, AttributeSet attrs) {
        super(context,attrs);
        mCurIndicator = mDefaultIndicator;
        setOrientation(LinearLayout.HORIZONTAL);
        init();
    }

    /**
     * 菜单视图布局
     *
     * @param iconResID
     *            图片资源ID
     * @param stringResID
     *            文字资源ID
     * @param stringColor
     *            颜色资源ID
     * @param iconTag
     *            图片标签
     * @param textTag
     *            文字标签
     * @return
     */
    private View createIndicator(int iconResID, int stringResID,
                                 int stringColor, String iconTag, String textTag) {
        // 实例一个LinearLayout
        LinearLayout view = new LinearLayout(getContext());
        view.setOrientation(LinearLayout.VERTICAL);// 垂直布局
        // 设置宽高和权重
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        view.setBackgroundResource(R.drawable.main_tab_item_bg_normal);
        // 实例一个ImageView
        ImageView iconView = new ImageView(getContext());
        // 设置与该ImageView视图相关联的标记
        iconView.setTag(iconTag);
        // 设置宽高和权重
        iconView.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
        iconView.setImageResource(iconResID);// 设置图片资源
        // 实例一个TextView
        TextView textView = new TextView(getContext());
        // 设置与该TextView视图相关联的标记
        textView.setTag(textTag);
        // 设置宽高和权重
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
        // 设置文字颜色
        textView.setTextColor(stringColor);
        // 设置文字大小
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        // 设置文字资源
        textView.setText(stringResID);
        // 添加视图到布局中
        view.addView(iconView);
        view.addView(textView);
        // 返回布局视图
        return view;
    }

    /**
     * 初始化视图
     */
    private void init() {

        int count = mAllStringResID.length;

        mIndicators = new View[count];      //View

        for (int i = 0; i < count; i++) {
            mIndicators[i] = createIndicator(
                    ((0 == i) ? mAllIconResID_Focus[i] : mAllIconResID_Normal[i]),
                    mAllStringResID[i],
                    ((0 == i) ? COLOR_SELECT : COLOR_UNSELECT),     // 第一个为默认选中的
                    mTAG_ICON[i],
                    mTAG_TEXT[i]
                    );
            mIndicators[i].setBackgroundResource(R.drawable.main_tab_item_bg);
            mIndicators[i].setTag(Integer.valueOf(i));
            mIndicators[i].setOnClickListener(this);
            addView(mIndicators[i]);
        }
    }

    public static void setIndicator(int which) {
        // /////////////////清除之前的状态/////////////////////////////////

        ImageView prevIcon = (ImageView) mIndicators[mCurIndicator].findViewWithTag(mTAG_ICON[mCurIndicator]);
        prevIcon.setImageResource(mAllIconResID_Normal[mCurIndicator]);
        TextView prevText = (TextView) mIndicators[mCurIndicator].findViewWithTag(mTAG_TEXT[mCurIndicator]);
        prevText.setTextColor(COLOR_UNSELECT);

        // /////////////////更新前状态/////////////////////////////////
        /**
         * 设置选中状态
         */
        ImageView currIcon = (ImageView) mIndicators[which].findViewWithTag(mTAG_ICON[which]);
        currIcon.setImageResource(mAllIconResID_Focus[which]);
        TextView currText = (TextView) mIndicators[which].findViewWithTag(mTAG_TEXT[which]);
        currText.setTextColor(COLOR_SELECT);

        mCurIndicator = which;
    }

    public interface OnIndicateListener {
        void onIndicate(View v, int which);
    }

    public void setOnIndicateListener(OnIndicateListener listener) {
        mOnIndicateListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (null != mOnIndicateListener) {
            int tag = (Integer) v.getTag();
            if (tag != mCurIndicator) {
                mOnIndicateListener.onIndicate(v,tag);
                setIndicator(tag);
            }
        }
    }



}
