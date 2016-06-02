package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.DetailItem;
import com.example.jc.personalaccount.Data.EditCommonOperType;
import com.example.jc.personalaccount.Data.FragmentID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentDetail extends Fragment implements IFragmentUI {

    protected Activity mActivity;
    private RefreshTask mAuthTask;
    private SwipeMenuListView mListView;
    private Map<DetailItem, List<DetailItem>> mData;
    private List<DetailItem> mGroups;
    private DetailListAdapter mAdapter;
    private Button mAddBtn;
    private TextView mTVTotalValue;
    private Double mTotalValue;

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
        this.mTVTotalValue = (TextView)view.findViewById(R.id.fragment_detail_total_value);

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

                DetailItem curItem = (DetailItem) (mAdapter.getItem(position));
                if (curItem.listItemType == GlobalData.LISTGROUPTYPE) {
                    return false;
                }

                switch (index) {
                    case 0:
                        //open
                        showEditDetailActivity(position,EditCommonOperType.EDIT);
                        break;
                    case 2:
                        //delete
                        if (position < mAdapter.mListDatas.size()) {
                            deleteClick(curItem);
                        }
                        break;
                }
                return false;
            }
        });

        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                DetailItem curItem = (DetailItem) (mAdapter.getItem(position));
                if (curItem.listItemType == GlobalData.LISTGROUPTYPE) {
                    mAdapter.changedGroupItemUnfold(curItem);
                    mAdapter.notifyDataSetChanged();
                } else {
                    showEditDetailActivity(position, EditCommonOperType.VIEW);
                }
            }
        });
    }

    private void showEditDetailActivity(int position, EditCommonOperType operType) {

        Intent intent = new Intent(mActivity, EditDetailItemActivity.class);
        intent.putExtra(GlobalData.EXTRA_DETAIL_EDIT_TYPE, operType.value());

        if (-1 != position) {
            int iListItemsLength = mAdapter.mListDatas.size();
            if (position < iListItemsLength) {
                GlobalData.EXTRA_Detail_Edit_Data = mAdapter.mListDatas.get(position);
            }
        }

        mActivity.startActivityForResult(intent, FragmentID.DETAIL.value());
    }

    private void deleteClick(DetailItem curItem) {

        int id = -1;
        try {
            id = Integer.parseInt(curItem.mapValue().get(DetailItem.mDataColumnName[0]).toString());
            if (id != -1) {
                if (GlobalData.DataStoreHelper.deleteDetailItem(id)) {

                    mAdapter.deleteItemByUser(curItem);

                    mAdapter.notifyDataSetChanged();
                    this.refreshTotalValue(this.mTotalValue - curItem.value);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setData() {

        this.mAdapter = new DetailListAdapter(mActivity, this.mGroups, this.mData);

        this.mListView.setAdapter(this.mAdapter);

        this.refreshTotalValue(this.mTotalValue);
    }

    private void refreshTotalValue(Double value) {

        this.mTotalValue = value;
        this.mTVTotalValue.setText(Double.toString(value / 100.0) + " " + this.getString(R.string.common_value_unit_yuan));
    }

    private void getAllData() {

        this.mData = new HashMap<>();
        this.mGroups = new ArrayList<>();
        this.mTotalValue = 0.0;

        DetailItem[] datas = GlobalData.DataStoreHelper.getAllDetailItems();
        List<DetailItem> listData = new ArrayList<>();
        String lastYear = "";
        if (null != datas) {
            for (int i = 0; i < datas.length; i++) {
                String year = datas[i].date.substring(0,4);

                this.mTotalValue = this.mTotalValue + datas[i].value;
                if (0 != year.compareTo(lastYear)) {
                    DetailItem groupItem = new DetailItem();
                    groupItem.listItemType = GlobalData.LISTGROUPTYPE;
                    groupItem.date = year;
                    groupItem.bIsExpand = false;

                    this.mGroups.add(groupItem);

                    lastYear = year;

                    listData = new ArrayList<>();

                    this.mData.put(groupItem, listData);
                }

                listData.add(datas[i]);
            }
        }
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
                getAllData();
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

        private List<DetailItem> mListDatas = new ArrayList<>();
        private List<DetailItem> mListGroups = new ArrayList<>();
        private Map<DetailItem,List<DetailItem>> mMapAllData = new HashMap<>();
        private LayoutInflater mInflater;

        public DetailListAdapter(Context context, List<DetailItem> listGroups, Map<DetailItem,List<DetailItem>>
                mapDatas) {
            this.mInflater = LayoutInflater.from(context);
            this.mListGroups = listGroups;
            this.mMapAllData = mapDatas;

            this.getAllListItems();
        }

        @Override
        public int getCount() {
            return this.mListDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return this.mListDatas.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return this.mListDatas.get(position).listItemType;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DetailItem detailItem = this.mListDatas.get(position);

            GroupHolder groupHolder = null;
            DetailItemHolder detailItemHolder = null;

            if (null == convertView) {
                switch (detailItem.listItemType) {
                    case GlobalData.LISTGROUPTYPE:
                    {
                        convertView = this.mInflater.inflate(R.layout.group_list_item_tag,null);

                        groupHolder = new GroupHolder(convertView);

                        convertView.setTag(groupHolder);

                        break;
                    }
                    case GlobalData.LISTITEMTYPE:
                    {
                        convertView = this.mInflater.inflate(R.layout.fragment_detail_list_item,null);

                        detailItemHolder = new DetailItemHolder(convertView);

                        convertView.setTag(detailItemHolder);

                        break;
                    }
                    default:
                        break;
                }
            } else {
                switch (detailItem.listItemType) {
                    case GlobalData.LISTGROUPTYPE:
                    {
                        groupHolder = (GroupHolder)convertView.getTag();

                        break;
                    }
                    case GlobalData.LISTITEMTYPE:
                    {
                        detailItemHolder = (DetailItemHolder)convertView.getTag();

                        break;
                    }
                    default:
                        break;
                }
            }

            if (null != groupHolder) {
                groupHolder.resetData(detailItem);
            }

            if (null != detailItemHolder) {
                detailItemHolder.resetData(detailItem);
            }

            return convertView;
        }

        public void deleteItemByUser(DetailItem curItem) {

            for (DetailItem item : this.mMapAllData.keySet()) {
                if (0 == item.date.compareTo(curItem.date.substring(0,4))) {
                    this.mMapAllData.get(item).remove(curItem);
                    break;
                }
            }

            this.getAllListItems();
        }

        public void changedGroupItemUnfold(DetailItem curItem) {

            for (DetailItem item : this.mMapAllData.keySet()) {
                if (0 == item.date.compareTo(curItem.date)) {
                    item.bIsExpand = !curItem.bIsExpand;
                    break;
                }
            }

            this.getAllListItems();
        }

        private void getAllListItems() {
            this.mListDatas.clear();

            List<DetailItem> listRemoves = new ArrayList<>();
            for (DetailItem groupItem : this.mListGroups) {
                if (this.mMapAllData.get(groupItem).size() > 0) {
                    this.mListDatas.add(groupItem);
                    if (groupItem.bIsExpand) {
                        this.mListDatas.addAll(this.mMapAllData.get(groupItem));
                    }
                } else {
                    listRemoves.add(groupItem);
                }
            }

            for (DetailItem groupItem : listRemoves) {
                this.mListGroups.remove(groupItem);
            }
        }

        private class GroupHolder {
            TextView mTVDate;
            ImageView mImageUnfold;

            GroupHolder(View view) {
                this.mTVDate = (TextView)view.findViewById(R.id.group_list_item_text);
                this.mImageUnfold = (ImageView)view.findViewById(R.id.group_list_item_image);
            }

            public void resetData(DetailItem item) {
                this.mTVDate.setText(item.date);

                if (item.bIsExpand) {
                    this.mImageUnfold.setImageResource(R.drawable.arrow_down_gray);
                } else {
                    this.mImageUnfold.setImageResource(R.drawable.arrow_right_gray);
                }
            }
        }

        private class DetailItemHolder {

            TextView mTVWeek;
            TextView mTVDate;
            TextView mTVValue;
            TextView mTVFrom;
            TextView mTVDescription;

            DetailItemHolder(View view) {

                this.mTVWeek = (TextView) view.findViewById(R.id.fragment_detail_list_item_week);
                this.mTVDate = (TextView) view.findViewById(R.id.fragment_detail_list_item_date);
                this.mTVValue = (TextView) view.findViewById(R.id.fragment_detail_list_item_value);
                this.mTVFrom = (TextView) view.findViewById(R.id.fragment_detail_list_item_from);
                this.mTVDescription = (TextView) view.findViewById(R.id.fragment_detail_list_item_description);
            }

            public void resetData(DetailItem detailItem) {
                this.mTVWeek.setText(detailItem.mapValue().get(DetailItem.mDataColumnName[1]).toString());
                this.mTVDate.setText(detailItem.mapValue().get(DetailItem.mDataColumnName[2]).toString());
                this.mTVValue.setText(detailItem.mapValue().get(DetailItem.mDataColumnName[3]).toString());
                this.mTVFrom.setText(detailItem.mapValue().get(DetailItem.mDataColumnName[4]).toString());
                this.mTVDescription.setText(detailItem.mapValue().get(DetailItem.mDataColumnName[5]).toString());
            }
        }
    }
}
