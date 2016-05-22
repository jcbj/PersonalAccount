package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.DetailItem;
import com.example.jc.personalaccount.Data.EditCommonOperType;
import com.example.jc.personalaccount.Data.FragmentID;
import com.example.jc.personalaccount.Data.SummaryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FragmentDetail extends Fragment implements IFragmentUI {

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        this.mListView = (SwipeMenuListView)view.findViewById(R.id.fragment_detail_list_view);
        this.mAddBtn = (Button)view.findViewById(R.id.fragment_detail_add_button);

        //设置每项滑动后的菜单
        this.mListView.setMenuCreator(GlobalData.buildSwipeMenuCreator(mActivity));

        this.bindingUIEvent();

        return view;
    }

    private void bindingUIEvent() {

        this.mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDetailActivity(-1, EditCommonOperType.ADD);
            }
        });

        this.mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //open
                        showEditDetailActivity(position,EditCommonOperType.EDIT);
                        break;
                    case 1:
                        //delete
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
                showEditDetailActivity(position, EditCommonOperType.VIEW);
            }
        });
    }

    private void showEditDetailActivity(int position, EditCommonOperType operType) {

        Intent intent = new Intent(mActivity, EditDetailItemActivity.class);
        intent.putExtra(GlobalData.EXTRA_DETAIL_EDIT_TYPE, operType.value());

        if (-1 != position) {
            int iListItemsLength = this.mData.size();
            if (position < iListItemsLength) {
                GlobalData.EXTRA_Detail_Edit_Data = new DetailItem((Map<String, Object>) ((this.mData.toArray())
                        [position]));
            }
        }

        mActivity.startActivityForResult(intent, FragmentID.DETAIL.value());
    }

    private void deleteClick(Map<String,Object> map) {

        int id = -1;
        try {
            id = Integer.parseInt(map.get(DetailItem.mDataColumnName[0]).toString());
            if (id != -1) {
                if (GlobalData.DataStoreHelper.deleteDetailItem(id)) {

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
                R.layout.fragment_detail_list_item,
                new String[]{
                        DetailItem.mDataColumnName[1],
                        DetailItem.mDataColumnName[2],
                        DetailItem.mDataColumnName[3],
                        DetailItem.mDataColumnName[4],
                        DetailItem.mDataColumnName[5]},
                new int[]{
                        R.id.fragment_detail_list_item_week,
                        R.id.fragment_detail_list_item_date,
                        R.id.fragment_detail_list_item_value,
                        R.id.fragment_detail_list_item_from,
                        R.id.fragment_detail_list_item_description});

        this.mListView.setAdapter(mAdapter);
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();

        DetailItem[] datas = GlobalData.DataStoreHelper.getAllDetailItems();
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

    private class DetailListAdapter extends BaseAdapter {

        private List<String> mListTags = null;
        private List<DetailItem> mListData = null;

        public DetailListAdapter(Context context, List<DetailItem> objects, List<String> tags) {
            super();
            this.mListData = objects;
            this.mListTags = tags;
        }

        @Override
        public int getCount() {
            return this.mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return this.mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

    }
}
