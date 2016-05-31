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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
    private Button mBtnOpenExportCSVPath;
    private EditText mETExportCSVPath;
    private Button mBtnOpenExportDBPath;
    private EditText mETExportDBPath;
    private Button mBtnExit;
    private Button mBtnImportCardAccount;
    private Button mBtnOpenCardAccountFile;
    private EditText mETCardAccountPath;
    private OpenSelectPathFragment mOpenPathFragmentOpen;

    private static final int INVOKE_TYPE_DB = 0;
    private static final int INVOKE_TYPE_CSV = 1;
    private static final int INVOKE_TYPE_IMPORT_ACCOUNT_CSV = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        this.mBtnExportDatabase = (Button)view.findViewById(R.id.fragment_setting_btn_export_database);
        this.mBtnExportCSV = (Button)view.findViewById(R.id.fragment_setting_btn_export_database_csv);
        this.mSpinnerItem = (Spinner)view.findViewById(R.id.fragment_setting_spinner_item);
        this.mBtnOpenExportCSVPath = (Button)view.findViewById(R.id.fragment_setting_btn_export_folder_csv);
        this.mETExportCSVPath = (EditText)view.findViewById(R.id.fragment_setting_text_path_export_csv);
        this.mBtnOpenExportDBPath = (Button)view.findViewById(R.id.fragment_setting_btn_open_export_database_path);
        this.mETExportDBPath = (EditText)view.findViewById(R.id.fragment_setting_export_database_path_text);
        this.mBtnExit = (Button)view.findViewById(R.id.fragment_setting_btn_exit);
        this.mBtnImportCardAccount = (Button)view.findViewById(R.id.fragment_setting_btn_import_cardaccount);
        this.mBtnOpenCardAccountFile = (Button)view.findViewById(R.id.fragment_setting_btn_open_file);
        this.mETCardAccountPath = (EditText)view.findViewById(R.id.fragment_setting_text_open_file_card_account);

        mOpenPathFragmentOpen = new OpenSelectPathFragment();
        mOpenPathFragmentOpen.mCallbackBundle = this;

        this.mSpinnerItem.setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, new String[]{
                this.getString(R.string.bottom_tab_home),
                this.getString(R.string.bottom_tab_summary),
                this.getString(R.string.bottom_tab_account),
                this.getString(R.string.bottom_tab_detail),
                this.getString(R.string.bottom_tab_car),
                this.getString(R.string.common_all)
        }));

        this.bindingUIEvent();

        return view;
    }

    private void bindingUIEvent() {
        this.mBtnExportDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean bIsSuccess = false;

                String path = mETExportDBPath.getText().toString();
                if (!TextUtils.isEmpty(path)) {
                    bIsSuccess = GlobalData.DataStoreHelper.exportDataStore(path);
                }

                Toast.makeText(mActivity, (bIsSuccess ? R.string.fragment_setting_export_success : R.string.fragment_setting_export_failed),Toast.LENGTH_SHORT).show() ;
            }
        });

        this.mETExportDBPath.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String path = mETExportDBPath.getText().toString();
                if (!TextUtils.isEmpty(path) && Utility.isValidFileName(path)) {

                    mBtnExportDatabase.setEnabled(true);
                } else {
                    mBtnExportDatabase.setEnabled(false);
                }
            }
        });

        this.mBtnExportCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean bIsSuccess = false;

                String path = mETExportCSVPath.getText().toString();
                if (!TextUtils.isEmpty(path)) {
                    bIsSuccess = GlobalData.DataStoreHelper.exportCSV(mSpinnerItem.getSelectedItemPosition(),mETExportCSVPath.getText().toString());
                }

                Toast.makeText(mActivity, (bIsSuccess ? R.string.fragment_setting_export_success : R.string.fragment_setting_export_failed),Toast.LENGTH_SHORT).show() ;
            }
        });

        this.mETExportCSVPath.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String path = mETExportCSVPath.getText().toString();
                if (!TextUtils.isEmpty(path) && Utility.isValidFileName(path)) {

                    mBtnExportCSV.setEnabled(true);
                } else {
                    mBtnExportCSV.setEnabled(false);
                }
            }
        });

        this.mETCardAccountPath.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String path = mETCardAccountPath.getText().toString();
                if (!TextUtils.isEmpty(path) && Utility.isValidFileName(path)) {

                    mBtnImportCardAccount.setEnabled(true);
                } else {
                    mBtnImportCardAccount.setEnabled(false);
                }
            }
        });

        this.mBtnOpenExportDBPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenPathFragmentOpen.setDialogSetting(false,"",INVOKE_TYPE_DB);
                mOpenPathFragmentOpen.show(mActivity.getSupportFragmentManager(),getString(R.string.fragment_setting_open_folder));
            }
        });

        this.mBtnOpenExportCSVPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenPathFragmentOpen.setDialogSetting(false,"",INVOKE_TYPE_CSV);
                mOpenPathFragmentOpen.show(mActivity.getSupportFragmentManager(),getString(R.string.fragment_setting_open_folder));
            }
        });

        this.mBtnOpenCardAccountFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenPathFragmentOpen.setDialogSetting(true,".csv;",INVOKE_TYPE_IMPORT_ACCOUNT_CSV);
                mOpenPathFragmentOpen.show(mActivity.getSupportFragmentManager(),getString(R.string.fragment_setting_open_file));
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

                mActivity.finish();
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
        int type = bundle.getInt(OpenSelectPathFragment.EXTRA_INVOKETYPE);
        if (INVOKE_TYPE_IMPORT_ACCOUNT_CSV == type) {
            mETCardAccountPath.setText(bundle.getString(OpenSelectPathFragment.EXTRA_SELECTPATH));
        } else if(INVOKE_TYPE_CSV == type) {
            mETExportCSVPath.setText(bundle.getString(OpenSelectPathFragment.EXTRA_SELECTPATH));
        } else if (INVOKE_TYPE_DB == type) {
            mETExportDBPath.setText(bundle.getString(OpenSelectPathFragment.EXTRA_SELECTPATH));
        }
    }
}
