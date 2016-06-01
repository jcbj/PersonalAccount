package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.jc.personalaccount.Data.FragmentID;
import com.example.jc.personalaccount.Data.HomeEditOperType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

public class FragmentHome extends Fragment implements IFragmentUI {

    protected Activity mActivity;
    private RefreshTask mAuthTask;
    private SwipeMenuListView mListViewProperty;
    private SwipeMenuListView mListViewDebt;
    private Button mAddPropertyBtn;
    private Button mAddDebtBtn;
    private TextView mPropertyTV;
    private TextView mDebtTV;
    private TextView mNetAssetsTV;
    private CalculateBalanceSheetData mAdapterData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity)context;

        Log.d("PA","FH:onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("PA","FH:onCreate");
    }

    @Override
    public void  onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("PA","FH:onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("PA","FH:onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("PA","FH:onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("PA","FH:onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("PA","FH:onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("PA","FH:onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("PA","FH:onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("PA","FH:onDetach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("PA","FH:onCreateView");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        this.mListViewProperty = (SwipeMenuListView) view.findViewById(R.id.fragment_home_listview_property);
        this.mListViewDebt = (SwipeMenuListView) view.findViewById(R.id.fragment_home_listview_debt);
        this.mPropertyTV = (TextView)view.findViewById(R.id.fragment_home_property_value_text);
        this.mDebtTV = (TextView)view.findViewById(R.id.fragment_home_debt_value_text);
        this.mNetAssetsTV = (TextView)view.findViewById(R.id.fragment_home_net_assets_tv);
        this.mAddPropertyBtn = (Button) view.findViewById(R.id.fragment_home_property_add_button);
        this.mAddDebtBtn = (Button) view.findViewById(R.id.fragment_home_debt_add_button);

        // step 1. create a MenuCreator,设置每一项滑动后的菜单
        SwipeMenuCreator creator = GlobalData.buildSwipeMenuCreator(mActivity);
        mListViewProperty.setMenuCreator(creator);
        mListViewDebt.setMenuCreator(creator);

        this.bindingUIEvent();

        return view;
    }

    private void bindingUIEvent() {

        mListViewDebt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Toast toast = Toast.makeText(mActivity,"选中 " + position,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
                toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mAddPropertyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNetAssetsActivity(-1,HomeEditOperType.ADDPROPERTY);
            }
        });

        mAddDebtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNetAssetsActivity(-1,HomeEditOperType.ADDDEBT);
            }
        });

        // step 2. listener item click event
        mListViewProperty.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch (index) {
                    case 0:
                        // open
                        showEditNetAssetsActivity(position,HomeEditOperType.EDITPROPERTY);
                        break;
                    case 2:
                        // delete
                        if (position < mAdapterData.listPropertyItems.size()) {
                            deleteClick((Map<String,Object>)((mAdapterData.listPropertyItems.toArray())[position]));
                        }
                        break;
                }
                return false;
            }
        });

        mListViewDebt.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch (index) {
                    case 0:
                        // open
                        showEditNetAssetsActivity(position,HomeEditOperType.EDITDEBT);
                        break;
                    case 2:
                        // delete
                        if (position < mAdapterData.listDebtItems.size()) {
                            deleteClick((Map<String,Object>)((mAdapterData.listDebtItems.toArray())[position]));
                        }
                        break;
                }
                return false;
            }
        });

        mListViewProperty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditNetAssetsActivity(position,HomeEditOperType.VIEWPROPERTY);
            }
        });

        mListViewDebt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditNetAssetsActivity(position,HomeEditOperType.VIEWDEBT);
            }
        });
    }

    private void showEditNetAssetsActivity(int position, HomeEditOperType type) {

        Intent intent = new Intent(mActivity, EditNetAssetsActivity.class);
        intent.putExtra(GlobalData.EXTRA_HOME_EDIT_TYPE, type.value());

        if ((HomeEditOperType.ADDDEBT != type) && (HomeEditOperType.ADDPROPERTY != type)) {
            boolean bIsProperty = ((type == HomeEditOperType.EDITPROPERTY)
                    || (type == HomeEditOperType.VIEWPROPERTY));
            int iListItemsLength = (bIsProperty) ? this.mAdapterData.listPropertyItems.size() : this.mAdapterData.listDebtItems.size();
            if (position < iListItemsLength) {
                if (bIsProperty) {
                    GlobalData.EXTRA_Home_Edit_Data = new BalanceSheetItem((Map<String, Object>) ((this.mAdapterData.listPropertyItems.toArray())[position]));
                } else {
                    GlobalData.EXTRA_Home_Edit_Data = new BalanceSheetItem((Map<String, Object>) ((this.mAdapterData.listDebtItems.toArray())[position]));
                }
            }
        }

        mActivity.startActivityForResult(intent, FragmentID.HOME.value());
    }

    private void deleteClick(Map<String,Object> map) {

        int id = -1;
        try {
            id = Integer.parseInt(map.get(BalanceSheetItem.mDataColumnName[0]).toString());
            if (id != -1) {
                if (GlobalData.DataStoreHelper.deleteWorthItem(id)) {

                    this.refreshUIData();
                    Object path = map.get(BalanceSheetItem.mDataColumnName[5]);
                    if (null != path) {
                        Utility.deleteFile(path.toString());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setData() {

        SimpleAdapter adapterProperty = new SimpleAdapter(
                mActivity,
                this.mAdapterData.listPropertyItems,
                R.layout.fragment_home_list_item,
                new String[]{BalanceSheetItem.mDataColumnName[4],
                        BalanceSheetItem.mDataColumnName[1],
                        BalanceSheetItem.mDataColumnName[2],
                        BalanceSheetItem.mDataColumnName[3]},
                new int[]{R.id.fragment_home_list_item_img,
                        R.id.fragment_home_list_item_name,
                        R.id.fragment_home_list_item_worth,
                        R.id.fragment_home_list_item_description});
        adapterProperty.setViewBinder(new SimpleAdapter.ViewBinder() {
            //界面显示时，将Bitmap类型数据赋值给ImageView,如果是R.drawable中的资源ID，则不需要处理
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    try {
                        ImageView imageView = (ImageView)view;
                        if (data instanceof Bitmap) {
                            imageView.setImageBitmap((Bitmap)data);
                        }
                        else {
                            imageView.setImageBitmap(null);
                        }

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });
        this.mListViewProperty.setAdapter(adapterProperty);

        SimpleAdapter adapterDebt = new SimpleAdapter(
                mActivity,
                this.mAdapterData.listDebtItems,
                R.layout.fragment_home_list_item,
                new String[]{BalanceSheetItem.mDataColumnName[4],
                        BalanceSheetItem.mDataColumnName[1],
                        BalanceSheetItem.mDataColumnName[2],
                        BalanceSheetItem.mDataColumnName[3]},
                new int[]{R.id.fragment_home_list_item_img,
                        R.id.fragment_home_list_item_name,
                        R.id.fragment_home_list_item_worth,
                        R.id.fragment_home_list_item_description});
        adapterDebt.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    try {
                        ImageView imageView = (ImageView)view;
                        if (data instanceof Bitmap) {
                            imageView.setImageBitmap((Bitmap)data);
                        }
                        else {
                            imageView.setImageBitmap(null);
                        }

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });
        this.mListViewDebt.setAdapter(adapterDebt);

        this.mPropertyTV.setText((this.mAdapterData.dPropertyAll / 10000.0) + " 万");
        this.mDebtTV.setText((this.mAdapterData.dDebtAll / 10000.0) + " 万");
        this.mNetAssetsTV.setText(((this.mAdapterData.dPropertyAll - this.mAdapterData.dDebtAll) / 10000.0) + " 万");
    }

    private CalculateBalanceSheetData getData() {
        CalculateBalanceSheetData result = new CalculateBalanceSheetData();
        result.listPropertyItems = new ArrayList<Map<String, Object>>();
        result.listDebtItems = new ArrayList<Map<String, Object>>();
        result.dDebtAll = result.dPropertyAll = 0.0;

        BalanceSheetItem[] infos = GlobalData.DataStoreHelper.getAllBalanceSheetItems();

        if (null != infos) {
            for (int i = 0; i < infos.length; i++) {

                if (infos[i].worthType == BalanceSheetItem.WorthType.Property) {
                    result.listPropertyItems.add(infos[i].mapValue());
                    result.dPropertyAll += infos[i].value;
                } else {
                    result.listDebtItems.add(infos[i].mapValue());
                    result.dDebtAll += infos[i].value;
                }
            }
        }

        result.dPropertyAll = ((int)(result.dPropertyAll / 100)) * 1.0;
        result.dDebtAll = ((int)(result.dDebtAll / 100)) * 1.0;

        return result;
    }

    public void refreshUIData() {
        if (null != mAuthTask) {
            return;
        }

        Log.d("PA","FH:refreshUIData");

        mAuthTask = new RefreshTask();
        mAuthTask.execute((Void) null);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RefreshTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {


            try {
                //执行异步的代码，后台线程中执行，执行完调用onPostExecute
                mAdapterData = getData();

            } catch (Exception e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //UI线程中执行，不能执行耗时操作

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
