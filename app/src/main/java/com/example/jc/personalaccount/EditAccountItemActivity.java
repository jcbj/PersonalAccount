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

import com.example.jc.personalaccount.Data.AccountItem;
import com.example.jc.personalaccount.Data.EditCommonOperType;
import com.example.jc.personalaccount.Data.FragmentID;
import com.example.jc.personalaccount.Data.SummaryItem;

import java.util.Date;
import java.util.GregorianCalendar;

public class EditAccountItemActivity extends AppCompatActivity {

    private int mEditCount;
    private AccountItem mCurrentItem;
    private EditCommonOperType mOperType;
    private DatePicker mDate;
    private EditText mETValue;
    private Spinner mSpinnerFrom;
    private Spinner mSpinnerType;
    private Spinner mSpinnerTo;
    private EditText mETDescription;
    private Button mSaveBtn;
    private Button mBackBtn;
    private String[] mSpinnerItemsType;
    private String[] mSpinnerItemsFromAndTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account_item);

        this.mDate = (DatePicker)findViewById(R.id.fragment_account_edit_date);
        this.mETValue = (EditText)findViewById(R.id.fragment_account_edit_value_text);
        this.mSpinnerFrom = (Spinner)findViewById(R.id.fragment_account_edit_from_spinner);
        this.mSpinnerType = (Spinner)findViewById(R.id.fragment_account_edit_type_spinner);
        this.mSpinnerTo = (Spinner)findViewById(R.id.fragment_account_edit_to_spinner);
        this.mETDescription = (EditText)findViewById(R.id.fragment_account_edit_description_text);
        this.mSaveBtn = (Button)findViewById(R.id.fragment_account_edit_save_button);
        this.mBackBtn = (Button)findViewById(R.id.fragment_account_edit_back_button);

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

        this.mCurrentItem = new AccountItem();

        this.mSpinnerItemsType = new String[]{
                this.getString(R.string.fragment_account_edit_type_fromto),
                this.getString(R.string.fragment_account_edit_type_fromto_virtual),
                this.getString(R.string.fragment_account_edit_type_tofrom),
                this.getString(R.string.fragment_account_edit_type_tofrom_virtual)
        };
        this.mSpinnerType.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.mSpinnerItemsType));

        this.mSpinnerItemsFromAndTo = GlobalData.DataStoreHelper.getAllAccountAlias();
        this.mSpinnerFrom.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.mSpinnerItemsFromAndTo));
        this.mSpinnerTo.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.mSpinnerItemsFromAndTo));

        this.mSaveBtn.setEnabled(this.mSpinnerItemsFromAndTo.length > 0);

        Intent intent = this.getIntent();
        this.mOperType = EditCommonOperType.valueOf(intent.getIntExtra(GlobalData.EXTRA_ACCOUNT_EDIT_TYPE, EditCommonOperType.ADD.value()));

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
            this.mCurrentItem = GlobalData.EXTRA_Account_Edit_Data;

            String[] lines = this.mCurrentItem.date.split("-");
            if ((null != lines) && (3 == lines.length)) {
                mDate.init(Integer.parseInt(lines[0]),Integer.parseInt(lines[1]) - 1,Integer.parseInt(lines[2]),null);
            }

            mETValue.setText(Double.toString(this.mCurrentItem.value / 100.0));
            int index = Utility.getFindArrayIndex(this.mSpinnerItemsFromAndTo,this.mCurrentItem.from);
            if (-1 == index) {
                index = 0;
            }
            mSpinnerFrom.setSelection(index,true);
            mSpinnerType.setSelection(this.mCurrentItem.type,true);
            index = Utility.getFindArrayIndex(this.mSpinnerItemsFromAndTo,this.mCurrentItem.from);
            if (-1 == index) {
                index = 0;
            }
            mSpinnerTo.setSelection(index,true);
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
        mCurrentItem.value = (int)(Double.parseDouble(mETValue.getText().toString()) * 100);
        mCurrentItem.from = mSpinnerFrom.getSelectedItem().toString();
        mCurrentItem.type = mSpinnerType.getSelectedItemPosition();
        mCurrentItem.to = mSpinnerTo.getSelectedItem().toString();
        mCurrentItem.description = mETDescription.getText().toString();

        if (GlobalData.DataStoreHelper.editAccountItem(mCurrentItem,(-1 == mCurrentItem.id))) {
            mEditCount++;

            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.common_save_success),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
            toast.show();

            mETValue.setText("");
            mSpinnerFrom.setSelection(0,true);
            mSpinnerType.setSelection(0,true);
            mSpinnerTo.setSelection(0,true);
            mETDescription.setText("");

            if (-1 != mCurrentItem.id) {
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
        intent.putExtra(GlobalData.EXTRA_WHO_HOME_TAGNAME,GlobalData.STRING_ACTIVITY_EDIT_ACCOUNT);
        intent.putExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,mEditCount);
        setResult(FragmentID.ACCOUNT.value(),intent);
        finish();
    }

    private void setUIViewStatus() {
        //隐藏软键盘
        this.mBackBtn.setFocusable(true);
        this.mBackBtn.setFocusableInTouchMode(true);
        this.mBackBtn.requestFocus();
        this.mBackBtn.requestFocusFromTouch();

        this.mDate.setEnabled(false);
        this.mSpinnerFrom.setEnabled(false);
        this.mSpinnerType.setEnabled(false);
        this.mSpinnerTo.setEnabled(false);
        this.mETValue.setEnabled(false);
        this.mETDescription.setEnabled(false);
        this.mSaveBtn.setEnabled(false);
    }
}
