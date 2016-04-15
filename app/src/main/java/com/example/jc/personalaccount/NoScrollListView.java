package com.example.jc.personalaccount;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.baoyz.swipemenulistview.SwipeMenuListView;

/**
 * 自定义ListView,实现无滚动条模式
 * Created by jc on 16/4/13.
 */
public class NoScrollListView extends SwipeMenuListView {

    public NoScrollListView(Context context) {
        super(context);
    }

    public NoScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
