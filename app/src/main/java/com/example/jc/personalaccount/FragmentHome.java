package com.example.jc.personalaccount;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jc.personalaccount.Data.BalanceSheetItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

public class FragmentHome extends Fragment {

    private RefreshTask mAuthTask;
    private SwipeMenuListView mListViewProperty;
    private SwipeMenuListView mListViewDebt;
    private Button mAddPropertyBtn;
    private Button mAddDebtBtn;
    private TextView mPropertyTV;
    private TextView mDebtTV;
    private TextView mNetAssetsPropertyTV;
    private TextView mNetAssetsDebtTV;
    private TextView mNetAssetsTV;
    private CalculateBalanceSheetData mAdapterDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListViewProperty = (SwipeMenuListView) view.findViewById(R.id.fragment_home_listview_property);
        mListViewDebt = (SwipeMenuListView) view.findViewById(R.id.fragment_home_listview_debt);

        mListViewDebt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast toast = Toast.makeText(getActivity(),"点击 " + position,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
                toast.show();
            }
        });

        mListViewDebt.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Toast toast = Toast.makeText(getActivity(),"长按 " + position,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
                toast.show();

                return false;
            }
        });

        mListViewDebt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Toast toast = Toast.makeText(getActivity(),"选中 " + position,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
                toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mPropertyTV = (TextView)view.findViewById(R.id.fragment_home_property_value_text);
        mDebtTV = (TextView)view.findViewById(R.id.fragment_home_debt_value_text);
        mNetAssetsPropertyTV = (TextView)view.findViewById(R.id.fragment_home_net_assets_property_tv);
        mNetAssetsDebtTV = (TextView)view.findViewById(R.id.fragment_home_net_assets_debt_tv);
        mNetAssetsTV = (TextView)view.findViewById(R.id.fragment_home_net_assets_tv);

        mAddPropertyBtn = (Button) view.findViewById(R.id.fragment_home_property_add_button);
        mAddPropertyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), EditNetAssetsActivity.class);
                intent.putExtra(GlobalData.EXTRA_HOME_EDIT_TYPE, 0);
                startActivity(intent);
            }
        });

        mAddDebtBtn = (Button) view.findViewById(R.id.fragment_home_debt_add_button);
        mAddDebtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), EditNetAssetsActivity.class);
                intent.putExtra(GlobalData.EXTRA_HOME_EDIT_TYPE, 1);
                startActivity(intent);
            }
        });

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = this.buildSwipeMenuCreator(getActivity());
        mListViewDebt.setMenuCreator(creator);

        // step 2. listener item click event
        mListViewDebt.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch (index) {
                    case 0:
                        // open
                        Toast toast = Toast.makeText(getActivity(),"Open click: " + position ,Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();

                        editClick(position,2);
                        break;
                    case 1:
                        // delete
                        Toast toast1 = Toast.makeText(getActivity(),"Delete click: " + position,Toast.LENGTH_SHORT);
                        toast1.setGravity(Gravity.CENTER,0,0);
                        toast1.show();

                        deleteClick(position,2);
                        break;
                }
                return false;
            }
        });

        refresh();

        return view;
    }

    public void refresh() {
        mAuthTask = new RefreshTask();
        mAuthTask.execute((Void) null);
    }

    private void editClick(int position, int type) {
        Intent intent = new Intent(getActivity(),EditNetAssetsActivity.class);
        intent.putExtra(GlobalData.EXTRA_HOME_EDIT_TYPE,type);
        getActivity().startActivity(intent);
    }

    private void deleteClick(int position, int type) {
        if (position < this.mAdapterDate.listDebtItems.size()) {

            Map<String,Object> temp = (Map<String,Object>)((this.mAdapterDate.listDebtItems.toArray())[position]);
            int id = -1;
            try {
                id = Integer.parseInt(temp.get("id").toString());
                if (id != -1) {
                    GlobalData.DataStoreHelper.deleteWorthItem(GlobalData.CurrentUser,id);
                }
            } catch (Exception ex) {
                return;
            }
        }
    }

    private SwipeMenuCreator buildSwipeMenuCreator(final Context context) {
        return new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(context); //getApplicationContext()
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(context);//getApplicationContext()
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void setData() {

        this.mAdapterDate = getData();

        SimpleAdapter adapterProperty = new SimpleAdapter(
                getActivity(),
                this.mAdapterDate.listPropertyItems,
                R.layout.fragment_home_list_item,
                new String[]{"img", "name", "worth", "description"},
                new int[]{R.id.fragment_home_list_item_img,
                        R.id.fragment_home_list_item_name,
                        R.id.fragment_home_list_item_worth,
                        R.id.fragment_home_list_item_description});
        this.mListViewProperty.setAdapter(adapterProperty);

        SimpleAdapter adapterDebt = new SimpleAdapter(
                getActivity(),
                this.mAdapterDate.listDebtItems,
                R.layout.fragment_home_list_item,
                new String[]{"img", "name", "worth", "description"},
                new int[]{R.id.fragment_home_list_item_img,
                        R.id.fragment_home_list_item_name,
                        R.id.fragment_home_list_item_worth,
                        R.id.fragment_home_list_item_description});
        this.mListViewDebt.setAdapter(adapterDebt);

        this.mPropertyTV.setText((this.mAdapterDate.dPropertyAll / 10000.0) + " 万");
        this.mDebtTV.setText((this.mAdapterDate.dDebtAll / 10000.0) + " 万");
        this.mNetAssetsPropertyTV.setText((this.mAdapterDate.dPropertyAll / 10000.0) + "");
        this.mNetAssetsDebtTV.setText((this.mAdapterDate.dDebtAll / 10000.0) + "");
        this.mNetAssetsTV.setText(((this.mAdapterDate.dPropertyAll - this.mAdapterDate.dDebtAll) / 10000.0) + " 万");
    }

    private CalculateBalanceSheetData getData() {
        CalculateBalanceSheetData result = new CalculateBalanceSheetData();
        result.listPropertyItems = new ArrayList<Map<String, Object>>();
        result.listDebtItems = new ArrayList<Map<String, Object>>();
        result.dDebtAll = result.dPropertyAll = 0.0;

        Map<String, Object> map = new HashMap<String, Object>();
        BalanceSheetItem[] infos = GlobalData.DataStoreHelper.getAllBalanceSheetInfos(GlobalData.CurrentUser);

        int[] images = new int[]{
                R.drawable.home_camera,
                R.drawable.home_garage_band,
                R.drawable.home_itunes_radio,
                R.drawable.home_watch};

        if (infos.length > 0) {
            for (int i = 0; i < infos.length; i++) {
                map = new HashMap<String, Object>();
                map.put("img", images[i % images.length]);
                map.put("name",infos[i].name.toString().trim());
                map.put("worth",Double.toString(infos[i].worth / 100.0));
                map.put("description", infos[i].description.toString());
                map.put("id",infos[i].id);
                map.put("imgpath",infos[i].imagePath);

                if (infos[i].worthType == BalanceSheetItem.WorthType.Property) {
                    result.listPropertyItems.add(map);
                    result.dPropertyAll += infos[i].worth;
                } else {
                    result.listDebtItems.add(map);
                    result.dDebtAll += infos[i].worth;
                }
            }
        }

        result.dPropertyAll = ((int)(result.dPropertyAll / 100)) * 1.0;
        result.dDebtAll = ((int)(result.dDebtAll / 100)) * 1.0;

        return result;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RefreshTask extends AsyncTask<Void, Void, Boolean> {

        RefreshTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                //执行异步的代码，后台线程中执行，执行完调用onPostExecute
                setData();
            } catch (Exception e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //UI线程中执行，不能执行耗时操作

            if (!success) {
                Toast toast = Toast.makeText(getActivity(),getString(R.string.common_load_failed),Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    /**
     * 从数据库获取数据以后处理玩，供界面显示
     */
    public class CalculateBalanceSheetData {
        public List<Map<String, Object>> listPropertyItems;
        public Double dPropertyAll;

        public List<Map<String, Object>> listDebtItems;
        public Double dDebtAll;
    }
}
