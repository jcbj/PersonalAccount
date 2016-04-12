package com.example.jc.personalaccount;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.jc.personalaccount.Data.BalanceSheetItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentHome extends Fragment {

    private ListView mListViewProperty;
    private ListView mListViewDebt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListViewProperty = (ListView) view.findViewById(R.id.fragment_home_listview_property);
        SimpleAdapter adapterProperty = new SimpleAdapter(
                getActivity(),
                getData(),
                R.layout.fragment_home_list_item,
                new String[]{"img", "name", "worth", "description"},
                new int[]{R.id.fragment_home_list_item_img,
                        R.id.fragment_home_list_item_name,
                        R.id.fragment_home_list_item_worth,
                        R.id.fragment_home_list_item_description});
        this.mListViewProperty.setAdapter(adapterProperty);
        this.setListViewHeightBasedOnChildren(this.mListViewProperty);

        mListViewDebt = (ListView) view.findViewById(R.id.fragment_home_listview_debt);
        SimpleAdapter adapterDebt = new SimpleAdapter(
                getActivity(),
                getData(),
                R.layout.fragment_home_list_item,
                new String[]{"img", "name", "worth", "description"},
                new int[]{R.id.fragment_home_list_item_img,
                        R.id.fragment_home_list_item_name,
                        R.id.fragment_home_list_item_worth,
                        R.id.fragment_home_list_item_description});
        this.mListViewDebt.setAdapter(adapterDebt);
        this.setListViewHeightBasedOnChildren(this.mListViewDebt);

        return view;
    }

    /***
     * 动态设置listview的高度
     *
     * @param listView
     */
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            //在还没有构建View 之前无法取得View的度宽。 在此之前我们必须选 measure 一下.
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // params.height += 5;
        // if without this statement,the listview will be
        // a
        // little short
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_camera);
        map.put("name","Canoe 7D");
        map.put("worth","7000");
        map.put("description", "数码相机，购于2012年底，配两个镜头，一个闪光灯，三脚架等物品。");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_watch);
        map.put("name", "Apple Watch");
        map.put("worth","3000");
        map.put("description", "苹果手表，购于2016年初，测试使用。");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_garage_band);
        map.put("name", "Garage-Band");
        map.put("worth","1000");
        map.put("description", "吉他，购于2015年中，测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_itunes_radio);
        map.put("name","Radio");
        map.put("worth","800");
        map.put("description", "收音机，测试，蓝牙鼠标自动断开，唤醒比较慢，用着还不错。");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_garage_band);
        map.put("name", "Garage-Band");
        map.put("worth","1000");
        map.put("description", "吉他，购于2015年中，测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！");
        list.add(map);

        BalanceSheetItem[] infos = GlobalData.DataStoreHelper.getAllHomeInfos(GlobalData.CurrentUser);

        if (infos.length > 0) {
            map = new HashMap<String, Object>();
            map.put("img", R.drawable.home_itunes_radio);
            map.put("name",infos[0].name);
            map.put("worth",infos[0].worth);
            map.put("description", infos[0].description);
            list.add(map);
        }

        return list;
    }

}
