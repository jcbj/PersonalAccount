package com.example.jc.personalaccount;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.jc.personalaccount.Data.DetailItem;
import com.example.jc.personalaccount.Data.EditCommonOperType;
import com.example.jc.personalaccount.Data.FragmentID;
import com.example.jc.personalaccount.Data.SummaryItem;

import java.util.Date;

public class EditDetailItemActivity extends AppCompatActivity {

    private int mEditCount;
    private DetailItem mCurrentItem;
    private EditCommonOperType mOperType;
    private DatePicker mDate;
    private EditText mETValue;
    private Spinner mSpinnerAlias;
    private EditText mETDescription;
    private Button mSaveBtn;
    private Button mBackBtn;
    private String[] mSpinnerItemsFromAndTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_detail_item);

        this.mDate = (DatePicker)findViewById(R.id.fragment_detail_edit_date);
        this.mETValue = (EditText)findViewById(R.id.fragment_detail_edit_value_text);
        this.mSpinnerAlias = (Spinner) findViewById(R.id.fragment_detail_edit_account_spinner);
        this.mETDescription = (EditText)findViewById(R.id.fragment_detail_edit_description_text);
        this.mSaveBtn = (Button)findViewById(R.id.fragment_detail_edit_save_button);
        this.mBackBtn = (Button)findViewById(R.id.fragment_detail_edit_back_button);

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

        this.mCurrentItem = new DetailItem();

        Intent intent = this.getIntent();
        this.mOperType = EditCommonOperType.valueOf(intent.getIntExtra(GlobalData.EXTRA_DETAIL_EDIT_TYPE, EditCommonOperType.ADD.value()));

        this.mSpinnerItemsFromAndTo = GlobalData.DataStoreHelper.getAllAccountAlias();
        this.mSpinnerAlias.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.mSpinnerItemsFromAndTo));

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
            this.mCurrentItem = GlobalData.EXTRA_Detail_Edit_Data;

            String[] lines = this.mCurrentItem.date.split("-");
            if ((null != lines) && (3 == lines.length)) {
                mDate.init(Integer.parseInt(lines[0]),Integer.parseInt(lines[1]) - 1,Integer.parseInt(lines[2]),null);
            }

            mETValue.setText(Double.toString(this.mCurrentItem.value / 100.0));
            int index = Utility.getFindArrayIndex(this.mSpinnerItemsFromAndTo,this.mCurrentItem.from);
            if (-1 == index) {
                index = 0;
            }
            mSpinnerAlias.setSelection(index,true);
            mETDescription.setText(this.mCurrentItem.description);
        }
    }

    private void saveClick() {

        if ((TextUtils.isEmpty(mETValue.getText().toString()))
                || (TextUtils.isEmpty(mETDescription.getText().toString()))) {

            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.fragment_account_edit_check_empty),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();

            return;
        }

        mCurrentItem.date = Utility.getFormatDate(GlobalData.DATEFORMAT, new Date(this.mDate.getYear() - 1900,this.mDate.getMonth(),this.mDate.getDayOfMonth()));
        mCurrentItem.from = mSpinnerAlias.getSelectedItem().toString();
        mCurrentItem.value = (int)(Double.parseDouble(mETValue.getText().toString()) * 100);
        mCurrentItem.description = mETDescription.getText().toString();

        boolean bIsSuccess = false;
        if (-1 == mCurrentItem.id) {
            bIsSuccess = GlobalData.DataStoreHelper.addDetailItem(new DetailItem[]{mCurrentItem});
        } else {
            bIsSuccess = GlobalData.DataStoreHelper.editDetailItem(mCurrentItem);
        }

        if (bIsSuccess) {
            mEditCount++;

            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.common_save_success),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
            toast.show();

            mETValue.setText("");
            mETDescription.setText("");
        } else {
            new AlertDialog.Builder(getApplicationContext()).setTitle(getString(R.string.common_str_information))
                    .setMessage(getString(R.string.common_save_failed))
                    .setPositiveButton(getString(R.string.common_btn_ok),null)
                    .show();
        }
    }

    private void backClick() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra(GlobalData.EXTRA_WHO_HOME_TAGNAME,GlobalData.STRING_ACTIVITY_EDIT_DETAIL);
        intent.putExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,mEditCount);
        setResult(FragmentID.DETAIL.value(),intent);
        finish();
    }

    private void setUIViewStatus() {
        //隐藏软键盘
        this.mBackBtn.setFocusable(true);
        this.mBackBtn.setFocusableInTouchMode(true);
        this.mBackBtn.requestFocus();
        this.mBackBtn.requestFocusFromTouch();

        this.mDate.setEnabled(false);
        this.mSpinnerAlias.setEnabled(false);
        this.mETValue.setEnabled(false);
        this.mETDescription.setEnabled(false);
        this.mSaveBtn.setEnabled(false);
    }
}
