package com.example.jc.personalaccount;

import android.support.v4.app.FragmentActivity;
import android.app.Dialog;
//import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.example.jc.personalaccount.Data.DetailItem;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class FragmentSetting extends Fragment implements IFragmentUI,OpenSelectPathFragment.CallbackBundle {

    protected FragmentActivity mActivity;
    private Button mBtnExportDatabase;
    private Button mBtnExportCSV;
    private Spinner mSpinnerItem;
    private Button mBtnExit;
    private Button mBtnImportCardAccount;
    private Button mBtnOpenCardAccountFile;
    private EditText mETCardAccountPath;
    private OpenSelectPathFragment mOpenPathFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        this.mBtnExportDatabase = (Button)view.findViewById(R.id.fragment_setting_btn_export_database);
        this.mBtnExportCSV = (Button)view.findViewById(R.id.fragment_setting_btn_export_database_csv);
        this.mSpinnerItem = (Spinner)view.findViewById(R.id.fragment_setting_spinner_item);
        this.mBtnExit = (Button)view.findViewById(R.id.fragment_setting_btn_exit);
        this.mBtnImportCardAccount = (Button)view.findViewById(R.id.fragment_setting_btn_import_cardaccount);
        this.mBtnOpenCardAccountFile = (Button)view.findViewById(R.id.fragment_setting_btn_open_file);
        this.mETCardAccountPath = (EditText)view.findViewById(R.id.fragment_setting_text_open_file_card_account);

        mOpenPathFragment = new OpenSelectPathFragment();
        mOpenPathFragment.mBundle = this;

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

                Boolean bIsSuccess = GlobalData.DataStoreHelper.exportCSV(mSpinnerItem.getSelectedItemPosition());

                Toast.makeText(mActivity, (bIsSuccess ? R.string.fragment_setting_export_success : R.string.fragment_setting_export_failed),Toast.LENGTH_SHORT).show() ;
            }
        });

        this.mBtnOpenCardAccountFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenPathFragment.show(mActivity.getSupportFragmentManager(),getString(R.string.fragment_setting_open_file));
            }
        });

        this.mBtnImportCardAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = mETCardAccountPath.getText().toString();

                if (TextUtils.isEmpty(fileName)) {
                    return;
                }

                Boolean bIsSuccess = ParserImportDataToDB.saveCardAccountCSVToDataStore(fileName);

                Toast.makeText(mActivity, (bIsSuccess ? R.string.fragment_setting_import_success : R.string.fragment_setting_import_failed),Toast.LENGTH_SHORT).show() ;
            }
        });

        this.mBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GlobalData.DataStoreHelper.logout();

                Intent intent = new Intent(mActivity,LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (FragmentActivity) context;
    }

    public void refreshUIData() {
        return;
    }

    @Override
    public void callback(Bundle bundle) {
        mETCardAccountPath.setText(bundle.getString("path"));
    }
}
