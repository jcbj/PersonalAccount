package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import com.example.jc.personalaccount.Data.SummaryItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FragmentAccount extends Fragment implements IFragmentUI {

    protected Activity mActivity;
    private RefreshTask mAuthTask;
    private SwipeMenuListView mListView;
    private Map<AccountItem, List<AccountItem>> mData;
    private AccountListAdapter mAdapter;
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
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        this.mListView = (SwipeMenuListView)view.findViewById(R.id.fragment_account_list_view);
        this.mAddBtn = (Button)view.findViewById(R.id.fragment_account_add_button);

        //设置每项滑动后的菜单
        this.mListView.setMenuCreator(GlobalData.buildSwipeMenuCreator(mActivity));

        this.bindingUIEvent();

        return view;
    }

    private void bindingUIEvent() {

        this.mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditAccountActivity(-1,EditCommonOperType.ADD);
            }
        });

        this.mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                AccountItem curItem = (AccountItem) (mAdapter.getItem(position));
                if (curItem.listItemType == GlobalData.LISTGROUPTYPE) {
                    return false;
                }

                switch (index) {
                    case 0:
                        //open
                        showEditAccountActivity(position,EditCommonOperType.EDIT);
                        break;
                    case 1:
                        //delete
                        if (position < mData.size()) {
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

                AccountItem curItem = (AccountItem) (mAdapter.getItem(position));
                if (curItem.listItemType == GlobalData.LISTGROUPTYPE) {
                    mAdapter.changedGroupItemUnfold(curItem);
                    mAdapter.notifyDataSetChanged();
                } else {
                    showEditAccountActivity(position, EditCommonOperType.VIEW);
                }
            }
        });
    }

    private void showEditAccountActivity(int position, EditCommonOperType operType) {

        Intent intent = new Intent(mActivity, EditAccountItemActivity.class);
        intent.putExtra(GlobalData.EXTRA_ACCOUNT_EDIT_TYPE, operType.value());

        if (-1 != position) {
            int iListItemsLength = this.mData.size();
            if (position < iListItemsLength) {
                GlobalData.EXTRA_Account_Edit_Data = (AccountItem) mAdapter.getItem(position);
            }
        }

        mActivity.startActivityForResult(intent, FragmentID.ACCOUNT.value());
    }

    private void deleteClick(AccountItem curItem) {

        int id = -1;
        try {
            id = curItem.id;
            if (id != -1) {
                if (GlobalData.DataStoreHelper.deleteAccountItem(id)) {

                    mAdapter.deleteItemByUser(curItem);

                    mAdapter.notifyDataSetChanged();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setData() {

        mAdapter = new AccountListAdapter(mActivity,this.mData);

        this.mListView.setAdapter(mAdapter);
    }

    private Map<AccountItem, List<AccountItem>> getAllData() {

        Map<AccountItem, List<AccountItem>> mapAllData = new HashMap<>();

        AccountItem[] datas = GlobalData.DataStoreHelper.getAllAccountItems();
        List<AccountItem> listData = new ArrayList<>();
        String lastYear = "";
        if (null != datas) {
            for (int i = 0; i < datas.length; i++) {
                String year = datas[i].date.substring(0,4);

                if (0 != year.compareTo(lastYear)) {
                    AccountItem groupItem = new AccountItem();
                    groupItem.listItemType = GlobalData.LISTGROUPTYPE;
                    groupItem.date = year;
                    groupItem.bIsUnfold = false;

                    listData = new ArrayList<>();

                    mapAllData.put(groupItem, listData);
                }

                listData.add(datas[i]);
            }
        }

        return mapAllData;
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
                mData = getAllData();
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

    private class AccountListAdapter extends BaseAdapter {

        private Map<AccountItem,List<AccountItem>> mMapAllData;
        private List<AccountItem> mListData = null;
        private LayoutInflater mInflater;

        public AccountListAdapter(Context context, Map<AccountItem,List<AccountItem>> mapAllData) {
            this.mInflater = LayoutInflater.from(context);
            this.mMapAllData = mapAllData;

            this.getAllListItems();
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
        public boolean isEnabled(int position) {
            if (this.getItemViewType(position) == GlobalData.LISTGROUPTYPE) {
                return false;
            }

            return true;
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

            AccountItem curItem = this.mListData.get(position);

            GroupHolder groupHolder = null;
            AccountItemHolder itemHolder = null;

            if (null == convertView) {
                switch (curItem.listItemType) {
                    case GlobalData.LISTGROUPTYPE:
                    {
                        convertView = this.mInflater.inflate(R.layout.group_list_item_tag,null);

                        groupHolder = new GroupHolder(convertView);

                        convertView.setTag(groupHolder);

                        break;
                    }
                    case GlobalData.LISTITEMTYPE:
                    {
                        convertView = this.mInflater.inflate(R.layout.fragment_account_list_item,null);

                        itemHolder = new AccountItemHolder(convertView);

                        convertView.setTag(itemHolder);

                        break;
                    }
                    default:
                        break;
                }
            } else {
                switch (curItem.listItemType) {
                    case GlobalData.LISTGROUPTYPE:
                    {
                        groupHolder = (GroupHolder)convertView.getTag();

                        break;
                    }
                    case GlobalData.LISTITEMTYPE:
                    {
                        itemHolder = (AccountItemHolder) convertView.getTag();

                        break;
                    }
                    default:
                        break;
                }
            }

            if (null != groupHolder) {
                groupHolder.resetData(curItem);
            }

            if (null != itemHolder) {
                itemHolder.resetData(curItem);
            }

            return convertView;
        }

        public void deleteItemByUser(AccountItem curItem) {

            for (AccountItem item : this.mMapAllData.keySet()) {
                if (0 == item.date.compareTo(curItem.date.substring(0,4))) {
                    this.mMapAllData.get(item).remove(curItem);
                    break;
                }
            }

            this.getAllListItems();
        }

        public void changedGroupItemUnfold(AccountItem curItem) {

            for (AccountItem item : this.mMapAllData.keySet()) {
                if (0 == item.date.compareTo(curItem.date)) {
                    item.bIsUnfold = !curItem.bIsUnfold;
                    break;
                }
            }

            this.getAllListItems();
        }

        private void getAllListItems() {
            this.mListData.clear();

            for (AccountItem groupItem : this.mMapAllData.keySet()) {
                this.mListData.add(groupItem);
                if (groupItem.bIsUnfold) {
                    this.mListData.addAll(this.mMapAllData.get(groupItem));
                }
            }
        }

        private class GroupHolder {
            TextView mTVDate;

            GroupHolder(View view) {
                this.mTVDate = (TextView)view.findViewById(R.id.group_list_item_text);
            }

            public void resetData(AccountItem item) {
                this.mTVDate.setText(item.date);
            }
        }

        private class AccountItemHolder {

            TextView mTVWeek;
            TextView mTVDate;
            TextView mTVValue;
            TextView mTVFrom;
            ImageView mImageType;
            TextView mTVTo;
            TextView mTVDescription;

            AccountItemHolder(View view) {

                this.mTVWeek = (TextView) view.findViewById(R.id.fragment_account_list_item_week);
                this.mTVDate = (TextView) view.findViewById(R.id.fragment_account_list_item_date);
                this.mTVValue = (TextView) view.findViewById(R.id.fragment_account_list_item_value);
                this.mTVFrom = (TextView) view.findViewById(R.id.fragment_account_list_item_from);
                this.mImageType = (ImageView) view.findViewById(R.id.fragment_account_list_item_type_image);
                this.mTVTo = (TextView) view.findViewById(R.id.fragment_account_list_item_to);
                this.mTVDescription = (TextView) view.findViewById(R.id.fragment_account_list_item_description);
            }

            public void resetData(AccountItem item) {
                this.mTVWeek.setText(item.mapValue().get(AccountItem.mDataColumnName[1]).toString());
                this.mTVDate.setText(item.mapValue().get(AccountItem.mDataColumnName[2]).toString());
                String value = item.mapValue().get(AccountItem.mDataColumnName[3]).toString();
                this.mTVValue.setText(value);
                if (value.contains("-")) {
                    this.mTVValue.setTextColor(getResources().getColor(R.color.red));
                }

                this.mTVFrom.setText(item.mapValue().get(AccountItem.mDataColumnName[4]).toString());
                this.mImageType.setImageResource(AccountItem.mTypeImageID[item.type]);
                this.mTVTo.setText(item.mapValue().get(AccountItem.mDataColumnName[5]).toString());
                this.mTVDescription.setText(item.mapValue().get(AccountItem.mDataColumnName[6]).toString());
            }
        }
    }
}
