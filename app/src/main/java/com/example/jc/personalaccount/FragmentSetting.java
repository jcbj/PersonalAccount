package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentSetting.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentSetting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSetting extends Fragment implements IFragmentUI {

    protected Activity mActivity;
    private Button mBtnExportDatabase;
    private Button mBtnExportCSV;
    private Spinner mSpinnerItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        this.mBtnExportDatabase = (Button)view.findViewById(R.id.fragment_setting_btn_export_database);
        this.mBtnExportCSV = (Button)view.findViewById(R.id.fragment_setting_btn_export_database_csv);
        this.mSpinnerItem = (Spinner)view.findViewById(R.id.fragment_setting_spinner_item);

        this.mSpinnerItem.setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, new String[]{
                this.getString(R.string.bottom_tab_home),
                this.getString(R.string.bottom_tab_summary),
                this.getString(R.string.bottom_tab_account),
                this.getString(R.string.bottom_tab_detail),
                this.getString(R.string.bottom_tab_car)
        }));

        this.bindingUIEvent();

        return view;
    }

    private void bindingUIEvent() {
        this.mBtnExportDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean bIsSuccess = GlobalData.DataStoreHelper.exportDataStore();

                Toast.makeText(mActivity, (bIsSuccess ? R.string.fragment_setting_export_success : R.string.fragment_setting_export_failed),Toast.LENGTH_SHORT).show() ;
            }
        });

        this.mBtnExportCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean bIsSuccess = GlobalData.DataStoreHelper.exportCSV(GlobalData.CurrentUser,mSpinnerItem.getSelectedItemPosition());

                Toast.makeText(mActivity, (bIsSuccess ? R.string.fragment_setting_export_success : R.string.fragment_setting_export_failed),Toast.LENGTH_SHORT).show() ;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity)context;
    }

    public void refreshUIData() {
        return;
    }
}
