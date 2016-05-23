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
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jc.personalaccount.Data.DetailItem;
import com.example.jc.personalaccount.Data.EditCommonOperType;
import com.example.jc.personalaccount.Data.FragmentID;

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

//        mAdapter = new SimpleAdapter(
//                mActivity,
//                this.mData,
//                R.layout.fragment_detail_list_item,
//                new String[]{
//                        DetailItem.mDataColumnName[1],
//                        DetailItem.mDataColumnName[2],
//                        DetailItem.mDataColumnName[3],
//                        DetailItem.mDataColumnName[4],
//                        DetailItem.mDataColumnName[5]},
//                new int[]{
//                        R.id.fragment_detail_list_item_week,
//                        R.id.fragment_detail_list_item_date,
//                        R.id.fragment_detail_list_item_value,
//                        R.id.fragment_detail_list_item_from,
//                        R.id.fragment_detail_list_item_description});

        DetailListAdapter adapter = new DetailListAdapter(mActivity,this.getAllData());

        this.mListView.setAdapter(adapter);
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

    private List<DetailItem> getAllData() {
        List<DetailItem> listData = new ArrayList<>();

        DetailItem[] datas = GlobalData.DataStoreHelper.getAllDetailItems();
        String lastYear = "";
        if (null != datas) {
            for (int i = 0; i < datas.length; i++) {
                String year = datas[i].date.substring(0,4);
                if (0 != year.compareTo(lastYear)) {
                    DetailItem detailItem = new DetailItem();
                    detailItem.listItemType = GlobalData.LISTGROUPTYPE;
                    detailItem.date = year;
                    lastYear = year;
                    listData.add(detailItem);
                }

                listData.add(datas[i]);
            }
        }

        return listData;
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

        private List<DetailItem> mListData = null;
        private LayoutInflater mInflater;

        public DetailListAdapter(Context context, List<DetailItem> objects) {
            this.mInflater = LayoutInflater.from(context);
            this.mListData = objects;
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
        public int getItemViewType(int position) {
            return this.mListData.get(position).listItemType;
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

            DetailItem detailItem = this.mListData.get(position);

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

        private class GroupHolder {
            TextView mTVDate;

            GroupHolder(View view) {
                this.mTVDate = (TextView)view.findViewById(R.id.group_list_item_text);
            }

            public void resetData(DetailItem detailItem) {
                this.mTVDate.setText(detailItem.date);
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
