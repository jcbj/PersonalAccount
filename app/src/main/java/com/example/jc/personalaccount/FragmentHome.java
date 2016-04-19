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

public class FragmentHome extends Fragment {

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListViewProperty = (SwipeMenuListView) view.findViewById(R.id.fragment_home_listview_property);
        mListViewDebt = (SwipeMenuListView) view.findViewById(R.id.fragment_home_listview_debt);

//        mListViewDebt.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//
//                Toast toast = Toast.makeText(mActivity,"长按 " + position,Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
//                toast.show();
//
//                return false;
//            }
//        });

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

        mPropertyTV = (TextView)view.findViewById(R.id.fragment_home_property_value_text);
        mDebtTV = (TextView)view.findViewById(R.id.fragment_home_debt_value_text);
        mNetAssetsTV = (TextView)view.findViewById(R.id.fragment_home_net_assets_tv);

        mAddPropertyBtn = (Button) view.findViewById(R.id.fragment_home_property_add_button);
        mAddPropertyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mActivity, EditNetAssetsActivity.class);
                intent.putExtra(GlobalData.EXTRA_HOME_EDIT_TYPE, HomeEditOperType.HOME_EDIT_OPER_TYPE_ADDPROPERTY.value());
                startActivity(intent);
            }
        });

        mAddDebtBtn = (Button) view.findViewById(R.id.fragment_home_debt_add_button);
        mAddDebtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mActivity, EditNetAssetsActivity.class);
                intent.putExtra(GlobalData.EXTRA_HOME_EDIT_TYPE, HomeEditOperType.HOME_EDIT_OPER_TYPE_ADDDEBT.value());
                startActivity(intent);
            }
        });

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = this.buildSwipeMenuCreator(mActivity);
        mListViewProperty.setMenuCreator(creator);
        mListViewDebt.setMenuCreator(creator);

        // step 2. listener item click event
        mListViewProperty.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                switch (index) {
                    case 0:
                        // open
                        editClick(position,HomeEditOperType.HOME_EDIT_OPER_TYPE_EDITPROPERTY);
                        break;
                    case 1:
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
                        editClick(position,HomeEditOperType.HOME_EDIT_OPER_TYPE_EDITDEBT);
                        break;
                    case 1:
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
                editClick(position,HomeEditOperType.HOME_EDIT_OPER_TYPE_VIEWPROPERTY);
            }
        });

        mListViewDebt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editClick(position,HomeEditOperType.HOME_EDIT_OPER_TYPE_VIEWDEBT);
            }
        });

        refresh();

        return view;
    }

    public void refresh() {
        if (null != mAuthTask) {
            return;
        }

        mAuthTask = new RefreshTask();
        mAuthTask.execute((Void) null);
    }

    private void editClick(int position, HomeEditOperType type) {
        boolean bIsProperty = ((type == HomeEditOperType.HOME_EDIT_OPER_TYPE_EDITPROPERTY) || (type ==
                HomeEditOperType.HOME_EDIT_OPER_TYPE_VIEWPROPERTY));
        int iListItemsLength = (bIsProperty) ? this.mAdapterData.listPropertyItems.size() : this.mAdapterData.listDebtItems.size();
        if (position < iListItemsLength) {
            Intent intent = new Intent(mActivity, EditNetAssetsActivity.class);
            intent.putExtra(GlobalData.EXTRA_HOME_EDIT_TYPE, type.value());
            if (bIsProperty) {
                GlobalData.EXTRA_Home_Edit_BSI_Data = new BalanceSheetItem((Map<String, Object>) ((this.mAdapterData.listPropertyItems.toArray())[position]));
            } else {
                GlobalData.EXTRA_Home_Edit_BSI_Data = new BalanceSheetItem((Map<String, Object>) ((this.mAdapterData.listDebtItems.toArray())[position]));
            }

            mActivity.startActivity(intent);
        }
    }

    private void deleteClick(Map<String,Object> map) {

        int id = -1;
        try {
            id = Integer.parseInt(map.get(BalanceSheetItem.mDataColumnName[0]).toString());
            if (id != -1) {
                if (GlobalData.DataStoreHelper.deleteWorthItem(GlobalData.CurrentUser,id)) {

                    this.refresh();
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
                openItem.setTitle("Edit");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(context);//getApplicationContext()
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0x80, 0x80, 0x80)));
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
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if ((view instanceof ImageView) && (data instanceof Bitmap)) {
                    try {
                        ImageView imageView = (ImageView)view;
                        imageView.setImageBitmap((Bitmap)data);

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
                if ((view instanceof ImageView) && (data instanceof Bitmap)) {
                    ImageView imageView = (ImageView)view;
                    imageView.setImageBitmap((Bitmap)data);
                    return true;
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

        BalanceSheetItem[] infos = GlobalData.DataStoreHelper.getAllBalanceSheetInfos(GlobalData.CurrentUser);

        if (infos.length > 0) {
            for (int i = 0; i < infos.length; i++) {

                if (infos[i].worthType == BalanceSheetItem.WorthType.Property) {
                    result.listPropertyItems.add(infos[i].mapValue());
                    result.dPropertyAll += infos[i].worth;
                } else {
                    result.listDebtItems.add(infos[i].mapValue());
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
