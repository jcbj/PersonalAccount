package com.example.jc.personalaccount;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
    private ListView mListViewProperty;
    private ListView mListViewDebt;
    private Button mAddPropertyBtn;
    private Button mAddDebtBtn;
    private TextView mPropertyTV;
    private TextView mDebtTV;
    private TextView mNetAssetsPropertyTV;
    private TextView mNetAssetsDebtTV;
    private TextView mNetAssetsTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListViewProperty = (ListView) view.findViewById(R.id.fragment_home_listview_property);
        mListViewDebt = (ListView) view.findViewById(R.id.fragment_home_listview_debt);

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

        refresh();

        return view;
    }

    public void refresh() {
        mAuthTask = new RefreshTask();
        mAuthTask.execute((Void) null);
    }

    private void setData() {

        CalculateBalanceSheetData data = getData();

        SimpleAdapter adapterProperty = new SimpleAdapter(
                getActivity(),
                data.listPropertyItems,
                R.layout.fragment_home_list_item,
                new String[]{"img", "name", "worth", "description"},
                new int[]{R.id.fragment_home_list_item_img,
                        R.id.fragment_home_list_item_name,
                        R.id.fragment_home_list_item_worth,
                        R.id.fragment_home_list_item_description});
        this.mListViewProperty.setAdapter(adapterProperty);

        SimpleAdapter adapterDebt = new SimpleAdapter(
                getActivity(),
                data.listDebtItems,
                R.layout.fragment_home_list_item,
                new String[]{"img", "name", "worth", "description"},
                new int[]{R.id.fragment_home_list_item_img,
                        R.id.fragment_home_list_item_name,
                        R.id.fragment_home_list_item_worth,
                        R.id.fragment_home_list_item_description});
        this.mListViewDebt.setAdapter(adapterDebt);

        this.mPropertyTV.setText((data.dPropertyAll / 100.0 / 10000.0) + " 万");
        this.mDebtTV.setText((data.dDebtAll / 100.0 / 10000.0) + " 万");
        this.mNetAssetsPropertyTV.setText((data.dPropertyAll / 100.0 / 10000.0) + "");
        this.mNetAssetsDebtTV.setText((data.dDebtAll / 100.0 / 10000.0) + "");
        this.mNetAssetsTV.setText(((data.dPropertyAll - data.dDebtAll) / 100.0 / 10000.0) + " 万");
    }

    private CalculateBalanceSheetData getData() {
        CalculateBalanceSheetData result = new CalculateBalanceSheetData();
        result.listPropertyItems = new ArrayList<Map<String, Object>>();
        result.listDebtItems = new ArrayList<Map<String, Object>>();
        result.dDebtAll = result.dPropertyAll = 0.0;

        Map<String, Object> map = new HashMap<String, Object>();
        BalanceSheetItem[] infos = GlobalData.DataStoreHelper.getAllBalanceSheetInfos(GlobalData.CurrentUser);

        int[] images = new int[]{R.drawable.home_camera, R.drawable.home_garage_band, R.drawable.home_itunes_radio, R
                .drawable.home_watch};

        if (infos.length > 0) {
            for (int i = 0; i < infos.length; i++) {
                map = new HashMap<String, Object>();
                map.put("img", images[i % images.length]);
                map.put("name",infos[i].name.toString().trim());
                map.put("worth",Double.toString(infos[i].worth / 100.0));
                map.put("description", infos[i].description.toString());

                if (infos[i].worthType == BalanceSheetItem.WorthType.Property) {
                    result.listPropertyItems.add(map);
                    result.dPropertyAll += infos[i].worth;
                } else {
                    result.listDebtItems.add(map);
                    result.dDebtAll += infos[i].worth;
                }
            }
        }

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
