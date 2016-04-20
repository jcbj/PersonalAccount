package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;

public class FragmentSummary extends Fragment {

    protected Activity mActivity;
    private RefreshTask mAuthTask;
    private SwipeMenuListView mListViewSummary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        this.mListViewSummary = (SwipeMenuListView)view.findViewById(R.id.fragment_summary_list_view);

        //设置没项滑动后的菜单
        this.mListViewSummary.setMenuCreator(GlobalData.buildSwipeMenuCreator(mActivity));

        this.bindingUIEvent();

        this.refreshUIData();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity)context;
    }

    private void refreshUIData() {
        if (null != mAuthTask) {
            return;
        }

        mAuthTask = new RefreshTask();
        mAuthTask.execute((Void) null);
    }

    private void bindingUIEvent() {

        this.mListViewSummary.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //open
                        break;
                    case 1:
                        //delete
                        break;
                }
                return false;
            }
        });

        this.mListViewSummary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    public class RefreshTask extends AsyncTask<Void, Void, Boolean> {

        RefreshTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {

            } else {

            }

            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}
