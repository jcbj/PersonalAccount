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
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.EditCommonOperType;
import com.example.jc.personalaccount.Data.FragmentID;
import com.example.jc.personalaccount.Data.SummaryItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentAccount extends Fragment implements IFragmentUI {

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
                switch (index) {
                    case 0:
                        //open
                        showEditAccountActivity(position,EditCommonOperType.EDIT);
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
                showEditAccountActivity(position, EditCommonOperType.VIEW);
            }
        });
    }

    private void showEditAccountActivity(int position, EditCommonOperType operType) {

        Intent intent = new Intent(mActivity, EditSummaryItemActivity.class);
        intent.putExtra(GlobalData.EXTRA_ACCOUNT_EDIT_TYPE, operType.value());

        if (-1 != position) {
            int iListItemsLength = this.mData.size();
            if (position < iListItemsLength) {
                GlobalData.EXTRA_Account_Edit_AI_Data = new AccountItem((Map<String, Object>) ((this.mData.toArray())[position]));
            }
        }

        mActivity.startActivityForResult(intent, FragmentID.ACCOUNT.value());
    }

    private void deleteClick(Map<String,Object> map) {

        int id = -1;
        try {
            id = Integer.parseInt(map.get(SummaryItem.mDataColumnName[0]).toString());
            if (id != -1) {
                if (GlobalData.DataStoreHelper.deleteAccountItem(GlobalData.CurrentUser,id)) {

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
                R.layout.fragment_account_list_item,
                new String[]{
                        AccountItem.mDataColumnName[1],
                        AccountItem.mDataColumnName[2],
                        AccountItem.mDataColumnName[3],
                        AccountItem.mDataColumnName[4],
                        AccountItem.mDataColumnName[5],
                        AccountItem.mDataColumnName[6],
                        AccountItem.mDataColumnName[7] },
                new int[]{
                        R.id.fragment_account_list_item_week,
                        R.id.fragment_account_list_item_date,
                        R.id.fragment_account_list_item_value,
                        R.id.fragment_account_list_item_from,
                        R.id.fragment_account_list_item_type_image,
                        R.id.fragment_account_list_item_to,
                        R.id.fragment_account_list_item_description});

        mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {

                if (view.getId() == R.id.fragment_account_list_item_value) {
                    if (null != data) {
                        String text = data.toString();
                        if (text.contains("-")) {
                            ((TextView)view).setText(text);
                            ((TextView)view).setTextColor(getResources().getColor(R.color.red));

                            return true;
                        }
                    }
                }

                return false;
            }
        });

        this.mListView.setAdapter(mAdapter);
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();

        AccountItem[] datas = GlobalData.DataStoreHelper.getAllAccountItemInfos(GlobalData.CurrentUser);
        if (null != datas) {
            for (int i = 0; i < datas.length; i++) {
                list.add(datas[i].mapValue());
            }
        }

        /*
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(AccountItem.mDataColumnName[1],"周日");
        map.put(AccountItem.mDataColumnName[2],"04-21");
        map.put(AccountItem.mDataColumnName[3],"-1234.56");
        map.put(AccountItem.mDataColumnName[4],"余额宝");
        map.put(AccountItem.mDataColumnName[5],R.drawable.fromto32);
        map.put(AccountItem.mDataColumnName[6],"交行卡");
        map.put(AccountItem.mDataColumnName[7],"从余额宝转出，转入交行卡中，还信用卡");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(AccountItem.mDataColumnName[1],"周一");
        map.put(AccountItem.mDataColumnName[2],"04-21");
        map.put(AccountItem.mDataColumnName[3],"-124.56");
        map.put(AccountItem.mDataColumnName[4],"交行卡");
        map.put(AccountItem.mDataColumnName[5],R.drawable.fromtovirtual32);
        map.put(AccountItem.mDataColumnName[6],"余额宝");
        map.put(AccountItem.mDataColumnName[7],"从交行卡转入余额宝");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(AccountItem.mDataColumnName[1],"周三");
        map.put(AccountItem.mDataColumnName[2],"04-21");
        map.put(AccountItem.mDataColumnName[3],"2354.56");
        map.put(AccountItem.mDataColumnName[4],"余额宝");
        map.put(AccountItem.mDataColumnName[5],R.drawable.tofrom32);
        map.put(AccountItem.mDataColumnName[6],"交行卡");
        map.put(AccountItem.mDataColumnName[7],"从余额宝转出，转入交行卡中，还信用卡");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(AccountItem.mDataColumnName[1],"周五");
        map.put(AccountItem.mDataColumnName[2],"04-21");
        map.put(AccountItem.mDataColumnName[3],"4.56");
        map.put(AccountItem.mDataColumnName[4],"余额宝");
        map.put(AccountItem.mDataColumnName[5],R.drawable.tofromvirtual32);
        map.put(AccountItem.mDataColumnName[6],"交行卡");
        map.put(AccountItem.mDataColumnName[7],"从余额宝转出，转入交行卡中，还信用卡");
        list.add(map);
        */

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
}
