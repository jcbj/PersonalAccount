package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jc.personalaccount.Data.FragmentID;
import com.example.jc.personalaccount.Data.EditCommonOperType;
import com.example.jc.personalaccount.Data.SummaryItem;

import java.util.ArrayList;
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

        return view;
    }

    private void bindingUIEvent() {

        this.mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditSummaryActivity(-1, EditCommonOperType.ADD);
            }
        });

        this.mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        showEditSummaryActivity(position, EditCommonOperType.EDIT);
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

                showEditSummaryActivity(position, EditCommonOperType.VIEW);
            }
        });
    }

    private void showEditSummaryActivity(int position, EditCommonOperType operType) {

        Intent intent = new Intent(mActivity, EditSummaryItemActivity.class);
        intent.putExtra(GlobalData.EXTRA_SUMMARY_EDIT_TYPE, operType.value());

        if (-1 != position) {
            int iListItemsLength = this.mData.size();
            if (position < iListItemsLength) {
                GlobalData.EXTRA_Summary_Edit_Data = new SummaryItem((Map<String, Object>) ((this.mData.toArray())[position]));
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

        SummaryItem[] datas = GlobalData.DataStoreHelper.getAllSummaryItems(GlobalData.CurrentUser);
        if (null != datas) {
            for (int i = 0; i < datas.length; i++) {
                list.add(datas[i].mapValue());
            }
        }

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
