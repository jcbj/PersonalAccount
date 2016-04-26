package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.BalanceSheetItem;
import com.example.jc.personalaccount.Data.FragmentID;
import com.example.jc.personalaccount.Data.HomeEditOperType;
import com.example.jc.personalaccount.Data.SummaryEditOperType;
import com.example.jc.personalaccount.Data.SummaryItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentSummary extends Fragment implements IFragmentUI {

    protected Activity mActivity;
    private RefreshTask mAuthTask;
    private SwipeMenuListView mListView;
    private List<Map<String, Object>> mData;
    private SimpleAdapter mAdapter;
    private Button mAddBtn;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity)context;
        Log.d("PA","FS:onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("PA","FS:onCreate");
    }

    @Override
    public void  onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("PA","FS:onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("PA","FS:onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("PA","FS:onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("PA","FS:onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("PA","FS:onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("PA","FS:onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("PA","FS:onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("PA","FS:onDetach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("PA","FS:onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        this.mListView = (SwipeMenuListView)view.findViewById(R.id.fragment_summary_list_view);
        this.mAddBtn = (Button)view.findViewById(R.id.fragment_summary_add_button);

        //设置没项滑动后的菜单
        this.mListView.setMenuCreator(GlobalData.buildSwipeMenuCreator(mActivity));

        this.bindingUIEvent();

        this.refreshUIData();

        return view;
    }

    private void bindingUIEvent() {

        this.mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNetAssetsActivity(-1, SummaryEditOperType.ADD);
            }
        });

        this.mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        showEditNetAssetsActivity(position,SummaryEditOperType.EDIT);
                        break;
                    case 1:
                        // delete
                        if (position < mData.size()) {
                            deleteClick((Map<String,Object>)((mData.toArray())[position]));
                        }
                        break;
                }
                return false;
            }
        });

        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                showEditNetAssetsActivity(position,SummaryEditOperType.VIEW);
            }
        });
    }

    private void showEditNetAssetsActivity(int position, SummaryEditOperType operType) {

        Intent intent = new Intent(mActivity, EditSummaryItemActivity.class);
        intent.putExtra(GlobalData.EXTRA_SUMMARY_EDIT_TYPE, operType.value());

        if (-1 != position) {
            int iListItemsLength = this.mData.size();
            if (position < iListItemsLength) {
                GlobalData.EXTRA_Summary_Edit_SI_Data = new SummaryItem((Map<String, Object>) ((this.mData.toArray())[position]));
            }
        }

        mActivity.startActivityForResult(intent, FragmentID.SUMMARY.value());
    }

    private void deleteClick(Map<String,Object> map) {

        int id = -1;
        try {
            id = Integer.parseInt(map.get(SummaryItem.mDataColumnName[0]).toString());
            if (id != -1) {
                if (GlobalData.DataStoreHelper.deleteSummaryItem(GlobalData.CurrentUser,id)) {

                    mData.remove(map);

                    mAdapter.notifyDataSetChanged();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setData() {

        mAdapter = new SimpleAdapter(
                mActivity,
                this.mData,
                R.layout.fragment_summary_list_item,
                new String[]{
                        SummaryItem.mDataColumnName[1],
                        SummaryItem.mDataColumnName[2],
                        SummaryItem.mDataColumnName[3],
                        SummaryItem.mDataColumnName[4],
                        SummaryItem.mDataColumnName[5],
                        SummaryItem.mDataColumnName[6]},
                new int[]{
                        R.id.fragment_summary_list_item_week,
                        R.id.fragment_summary_list_item_date,
                        R.id.fragment_summary_list_item_value,
                        R.id.fragment_summary_list_item_name,
                        R.id.fragment_summary_list_item_alias,
                        R.id.fragment_summary_list_item_description});

        this.mListView.setAdapter(mAdapter);
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();

        SummaryItem[] datas = GlobalData.DataStoreHelper.getAllSummaryItemInfos(GlobalData.CurrentUser);
        if (null != datas) {
            for (int i = 0; i < datas.length; i++) {
                list.add(datas[i].mapValue());
            }
        }

        /* Test
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SummaryItem.mDataColumnName[1],"周日");
        map.put(SummaryItem.mDataColumnName[2],"04-21");
        map.put(SummaryItem.mDataColumnName[3],"-1234.56");
        map.put(SummaryItem.mDataColumnName[4],"交通银行信用卡6236");
        map.put(SummaryItem.mDataColumnName[5],"交信卡");
        map.put(SummaryItem.mDataColumnName[6],"信用额度五万元，有效期2015年10月20日");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(SummaryItem.mDataColumnName[1],"周一");
        map.put(SummaryItem.mDataColumnName[2],"04-21");
        map.put(SummaryItem.mDataColumnName[3],"-124.56");
        map.put(SummaryItem.mDataColumnName[4],"中信银行信用卡6236");
        map.put(SummaryItem.mDataColumnName[5],"中信信");
        map.put(SummaryItem.mDataColumnName[6],"信用额度三万元，有效期2015年10月20日");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(SummaryItem.mDataColumnName[1],"周三");
        map.put(SummaryItem.mDataColumnName[2],"04-21");
        map.put(SummaryItem.mDataColumnName[3],"2354.56");
        map.put(SummaryItem.mDataColumnName[4],"阿里巴巴支付宝");
        map.put(SummaryItem.mDataColumnName[5],"余额宝");
        map.put(SummaryItem.mDataColumnName[6],"阿里巴巴金融业务");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(SummaryItem.mDataColumnName[1],"周五");
        map.put(SummaryItem.mDataColumnName[2],"04-21");
        map.put(SummaryItem.mDataColumnName[3],"4.56");
        map.put(SummaryItem.mDataColumnName[4],"中国银行信用卡2626");
        map.put(SummaryItem.mDataColumnName[5],"中行信");
        map.put(SummaryItem.mDataColumnName[6],"信用额度八千元，绑定ETC,缴纳通行费");
        list.add(map);
        */

        return list;
    }

    public void refreshUIData() {
        if (null != mAuthTask) {
            return;
        }

        Log.d("PA","FS:refreshUIData");

        mAuthTask = new RefreshTask();
        mAuthTask.execute((Void) null);
    }

    public class RefreshTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                mData = getData();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                setData();
            } else {
                Toast toast = Toast.makeText(mActivity,getString(R.string.common_load_failed),Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }

            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}
