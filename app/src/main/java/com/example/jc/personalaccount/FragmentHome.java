package com.example.jc.personalaccount;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.jc.personalaccount.Data.HomeInfos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentHome extends Fragment {

    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = (ListView) view.findViewById(R.id.fragment_home_listview);

        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                getData(),
                R.layout.fragment_home_list_item,
                new String[]{"img", "title", "price", "description"},
                new int[]{R.id.fragment_home_list_item_img, R.id.fragment_home_list_item_title, R.id
                        .fragment_home_list_item_price, R.id.fragment_home_list_item_description});

        this.mListView.setAdapter(adapter);

        return view;
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_camera);
        map.put("title","Canoe 7D");
        map.put("price","7000");
        map.put("description", "数码相机，购于2012年底，配两个镜头，一个闪光灯，三脚架等物品。");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_watch);
        map.put("title", "Apple Watch");
        map.put("price","3000");
        map.put("description", "苹果手表，购于2016年初，测试使用。");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_garage_band);
        map.put("title", "Garage-Band");
        map.put("price","1000");
        map.put("description", "吉他，购于2015年中，测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！测试换行！");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.home_itunes_radio);
        map.put("title","Radio");
        map.put("price","800");
        map.put("description", "收音机，测试，蓝牙鼠标自动断开，唤醒比较慢，用着还不错。");
        list.add(map);

        HomeInfos[] infos = GlobalData.DataStoreHelper.getAllHomeInfos(GlobalData.CurrentUser);

        if (infos.length > 0) {
            map = new HashMap<String, Object>();
            map.put("img", R.drawable.home_itunes_radio);
            map.put("title",infos[0].title);
            map.put("price",infos[0].price);
            map.put("description", infos[0].description);
            list.add(map);
        }

        return list;
    }

}
