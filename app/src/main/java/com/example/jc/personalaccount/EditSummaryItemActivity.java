package com.example.jc.personalaccount;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jc.personalaccount.Data.FragmentID;
import com.example.jc.personalaccount.Data.EditCommonOperType;
import com.example.jc.personalaccount.Data.SummaryItem;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditSummaryItemActivity extends AppCompatActivity {

    private int mEditCount;
    private SummaryItem mCurrentInfo;
    private EditCommonOperType mOperType;
    private DatePicker mDate;
    private EditText mETValue;
    private EditText mETName;
    private EditText mETAlias;
    private EditText mETDescription;
    private Button mSaveBtn;
    private Button mBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_summary_item);

        this.mDate = (DatePicker)findViewById(R.id.fragment_summary_edit_date);
        this.mETName = (EditText)findViewById(R.id.fragment_summary_edit_account_name_text);
        this.mETAlias = (EditText)findViewById(R.id.fragment_summary_edit_account_name_alias_text);
        this.mETValue = (EditText)findViewById(R.id.fragment_summary_edit_value_text);
        this.mETDescription = (EditText)findViewById(R.id.fragment_summary_edit_description_text);
        this.mSaveBtn = (Button)findViewById(R.id.fragment_summary_edit_save_button);
        this.mBackBtn = (Button)findViewById(R.id.fragment_summary_edit_back_button);

        this.bindingUIEvent();

        this.initUI();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backClick();
            return true;
        }

        return super.onKeyDown(keyCode,keyEvent);
    }

    private void bindingUIEvent() {

        this.mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveClick();
            }
        });

        this.mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backClick();
            }
        });
    }

    /**
     * 初始化页面
     */
    private void initUI() {

        this.mCurrentInfo = new SummaryItem();

        Intent intent = this.getIntent();
        this.mOperType = EditCommonOperType.valueOf(intent.getIntExtra(GlobalData.EXTRA_SUMMARY_EDIT_TYPE, EditCommonOperType.ADD.value()));

        boolean isEdit = false;
        boolean isView = false;

        switch (this.mOperType) {
            case ADD:
                this.setTitle(R.string.activity_title_add);
                break;
            case EDIT:
                this.setTitle(R.string.activity_title_edit);
                isEdit = true;
                break;
            case VIEW:
                this.setTitle(R.string.activity_title_view);
                isView = true;
                this.setUIViewStatus();
                break;
            default:
                this.setTitle(R.string.activity_title_add);
        }

        if (isEdit || isView) {
            this.mCurrentInfo = GlobalData.EXTRA_Summary_Edit_Data;

            mETName.setText(this.mCurrentInfo.name);
            mETAlias.setText(this.mCurrentInfo.alias);
            mETValue.setText(Double.toString(this.mCurrentInfo.value / 100.0));
            mETDescription.setText(this.mCurrentInfo.description);
        }
    }

    private void saveClick() {

        if ((TextUtils.isEmpty(mETName.getText().toString()))
                || (TextUtils.isEmpty(mETAlias.getText().toString()))
                || (TextUtils.isEmpty(mETValue.getText().toString()))
                || (TextUtils.isEmpty(mETDescription.getText().toString()))) {

            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.fragment_summary_account_edit_check_empty),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();

            return;
        }

        mCurrentInfo.date = Utility.getFormatDate(GlobalData.DATEFORMAT, new Date(this.mDate.getYear() - 1900,this.mDate.getMonth(),this.mDate.getDayOfMonth()));
        mCurrentInfo.name = mETName.getText().toString();
        mCurrentInfo.alias = mETAlias.getText().toString();
        mCurrentInfo.value = (int)(Double.parseDouble(mETValue.getText().toString()) * 100);
        mCurrentInfo.description = mETDescription.getText().toString();

        if (GlobalData.DataStoreHelper.editSummaryItem(mCurrentInfo, (-1 == mCurrentInfo.id))) {
            mEditCount++;

            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.common_save_success),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
            toast.show();

            mETName.setText("");
            mETAlias.setText("");
            mETValue.setText("");
            mETDescription.setText("");

            if (-1 != mCurrentInfo.id) {
                this.backClick();
            }
        } else {
            new AlertDialog.Builder(getApplicationContext()).setTitle(getString(R.string.common_str_information))
                    .setMessage(getString(R.string.common_save_failed))
                    .setPositiveButton(getString(R.string.common_btn_ok),null)
                    .show();
        }
    }

    private void backClick() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra(GlobalData.EXTRA_WHO_HOME_TAGNAME,GlobalData.STRING_ACTIVITY_EDIT_SUMMARY);
        intent.putExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,mEditCount);
        setResult(FragmentID.SUMMARY.value(),intent);
        finish();
    }

    private void setUIViewStatus() {
        //隐藏软键盘
        this.mBackBtn.setFocusable(true);
        this.mBackBtn.setFocusableInTouchMode(true);
        this.mBackBtn.requestFocus();
        this.mBackBtn.requestFocusFromTouch();

        this.mETAlias.setEnabled(false);
        this.mETName.setEnabled(false);
        this.mETValue.setEnabled(false);
        this.mETDescription.setEnabled(false);
        this.mSaveBtn.setEnabled(false);
    }
}
