package com.example.jc.personalaccount;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.BoringLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.jc.personalaccount.Data.BalanceSheetItem;
import com.example.jc.personalaccount.Data.HomeEditOperType;

import java.io.File;
import java.util.Map;

public class EditNetAssetsActivity extends AppCompatActivity {

    private int mEditCount = 0;
    private HomeEditOperType mOperType = HomeEditOperType.HOME_EDIT_OPER_TYPE_ADDPROPERTY;
    private EditText mETName;
    private EditText mETDescription;
    private EditText mETWorth;
    private Spinner mTypeSpinner;
    private Button mSaveBtn;
    private Button mBackBtn;
    private Button mAddPictureBtn;
    private Button mRemovePictureBtn;
    private BalanceSheetItem mCurrentInfo;
    private ImageView mImageView;
    private int mWindowHeight;
    private int mWindowWidth;
    private boolean mImageIsChanged;
    private String mTempImagePath;
    private Bitmap mTempImageThumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_net_assets);

        //获取页面控件id
        final String[] spinnerItems = new String[]{getString(R.string.fragment_home_property_title),getString(R.string.fragment_home_debt_title)};
        final ArrayAdapter<String> typeSpinnerItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinnerItems);
        this.mTypeSpinner = (Spinner) findViewById(R.id.fragment_home_edit_type_spinner);
        this.mTypeSpinner.setAdapter(typeSpinnerItems);

        mETName = (EditText)findViewById(R.id.fragment_home_edit_name_text);
        mETWorth = (EditText)findViewById(R.id.fragment_home_edit_worth_text);
        mETDescription = (EditText)findViewById(R.id.fragment_home_edit_description_text);

        this.mSaveBtn = (Button) findViewById(R.id.fragment_home_edit_save_button);
        this.mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((TextUtils.isEmpty(mETName.getText().toString()))
                        || (TextUtils.isEmpty(mETWorth.getText().toString()))
                        || (TextUtils.isEmpty(mETDescription.getText().toString()))) {

                    Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.fragment_home_edit_check_message),Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();

                    return;
                }

                mCurrentInfo.worthType = (mTypeSpinner.getSelectedItem().toString() == spinnerItems[0]) ? BalanceSheetItem.WorthType.Property : BalanceSheetItem.WorthType.Debt;
                mCurrentInfo.name = mETName.getText().toString();
                mCurrentInfo.worth = (int)(Double.parseDouble(mETWorth.getText().toString()) * 100);
                mCurrentInfo.description = mETDescription.getText().toString();

                String tempPath = mCurrentInfo.imagePath;

                if (mImageIsChanged) {

                    mCurrentInfo.imagePath = null;
                    mCurrentInfo.imageThumb = null;

                    if ((null != mTempImagePath) && (!TextUtils.isEmpty(mTempImagePath))) {
                        //复制图片，保存缩略图
                        String curAppPath = GlobalData.ImagePath + "/" + Long.toString(System.currentTimeMillis());
                        if (Utility.copyFile(mTempImagePath,curAppPath)) {
                            mCurrentInfo.imagePath = curAppPath;
                        }
                    }

                    mCurrentInfo.imageThumb = mTempImageThumb;
                }

                if (GlobalData.DataStoreHelper.editWorthItem(GlobalData.CurrentUser,mCurrentInfo,(-1 == mCurrentInfo.id))) {
                    mEditCount++;

                    Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.common_save_success),Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
                    toast.show();

                    mETName.setText("");
                    mETWorth.setText("");
                    mETDescription.setText("");
                    mImageView.setImageBitmap(null);
                    mRemovePictureBtn.setEnabled(false);

                    if (-1 != mCurrentInfo.id) {
                        if (mImageIsChanged) {
                            //如果是编辑，则需要删除当前本地文件
                            if ((null != tempPath) && (!TextUtils.isEmpty(tempPath))) {
                                Utility.deleteFile(tempPath);
                            }
                        }
                    }
                } else {
                    new AlertDialog.Builder(getApplicationContext()).setTitle(getString(R.string.common_str_information))
                            .setMessage(getString(R.string.common_save_failed))
                            .setPositiveButton(getString(R.string.common_btn_ok),null)
                            .show();
                }
            }
        });

        this.mBackBtn = (Button) findViewById(R.id.fragment_home_edit_back_button);
        this.mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(EditNetAssetsActivity.this,MainActivity.class);
                intent1.putExtra(GlobalData.EXTRA_WHO_HOME_TAGNAME,GlobalData.STRING_ACTIVITY_EDIT_NETASSETS);
                intent1.putExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,mEditCount);
                startActivity(intent1);
            }
        });

        this.mAddPictureBtn = (Button)findViewById(R.id.fragment_home_edit_add_image_button);
        this.mAddPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,0);
            }
        });

        this.mRemovePictureBtn = (Button)findViewById(R.id.fragment_home_edit_remove_image_button);
        this.mRemovePictureBtn.setEnabled(false);
        this.mRemovePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageIsChanged = true;
                mTempImagePath = null;
                mTempImageThumb = null;
                mImageView.setImageBitmap(null);
                mRemovePictureBtn.setEnabled(false);
            }
        });

        this.mImageView = (ImageView)findViewById(R.id.fragment_home_edit_image_view);
        WindowManager manager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        manager.getDefaultDisplay().getSize(outSize);
        this.mWindowHeight = outSize.y;
        this.mWindowWidth = outSize.x;

        //初始化页面
        this.mCurrentInfo = new BalanceSheetItem();
        this.mImageIsChanged = false;

        Intent intent = this.getIntent();
        this.mOperType = HomeEditOperType.valueOf(intent.getIntExtra(GlobalData.EXTRA_HOME_EDIT_TYPE, 0));

        int iTypeSpinnerSelectIndex = 0;
        boolean isEdit = false;
        boolean isView = false;

        switch (this.mOperType) {
            case HOME_EDIT_OPER_TYPE_ADDPROPERTY: {
                this.setTitle(R.string.activity_title_home_edit_add);
                iTypeSpinnerSelectIndex = 0;

                break;
            }
            case HOME_EDIT_OPER_TYPE_ADDDEBT: {
                this.setTitle(R.string.activity_title_home_edit_add);
                iTypeSpinnerSelectIndex = 1;

                break;
            }
            case HOME_EDIT_OPER_TYPE_EDITPROPERTY: {
                this.setTitle(R.string.activity_title_home_edit);
                iTypeSpinnerSelectIndex = 0;
                isEdit = true;

                break;
            }
            case HOME_EDIT_OPER_TYPE_EDITDEBT: {
                this.setTitle(R.string.activity_title_home_edit);
                iTypeSpinnerSelectIndex = 1;
                isEdit = true;

                break;
            }
            case HOME_EDIT_OPER_TYPE_VIEWPROPERTY:{
                this.setTitle(R.string.activity_title_home_edit_view);
                iTypeSpinnerSelectIndex = 0;
                isView = true;

                this.setUIViewStatus();

                break;
            }
            case HOME_EDIT_OPER_TYPE_VIEWDEBT:{
                this.setTitle(R.string.activity_title_home_edit_view);
                iTypeSpinnerSelectIndex = 1;
                isView = true;

                this.setUIViewStatus();

                break;
            }

            default:{

                break;
            }
        }

        //直接设置选中项，并立即生效
        this.mTypeSpinner.setSelection(iTypeSpinnerSelectIndex,true);

        if (isEdit || isView) {
            this.mCurrentInfo = GlobalData.EXTRA_Home_Edit_BSI_Data;

            mETName.setText(this.mCurrentInfo.name);
            mETWorth.setText(Double.toString(this.mCurrentInfo.worth / 100.0));
            mETDescription.setText(this.mCurrentInfo.description);

            if ((!TextUtils.isEmpty(this.mCurrentInfo.imagePath)) && ((new File(this.mCurrentInfo.imagePath)).exists())) {
                Bitmap bitmap = Utility.extractMiniThumb(this.mCurrentInfo.imagePath,this.mWindowWidth,this.mWindowHeight,true);
                if (null != bitmap) {
                    this.mImageView.setImageBitmap(bitmap);
                    if (isEdit) {
                        mRemovePictureBtn.setEnabled(true);
                    }
                }
            } else if (null != this.mCurrentInfo.imageThumb) {
                this.mImageView.setImageBitmap(this.mCurrentInfo.imageThumb);
                if (isEdit) {
                    mRemovePictureBtn.setEnabled(true);
                }
            }
        }
    }

    private void setUIViewStatus() {
        this.mTypeSpinner.setEnabled(false);
        this.mETName.setEnabled(false);
        this.mETWorth.setEnabled(false);
        this.mETDescription.setEnabled(false);
        this.mRemovePictureBtn.setEnabled(false);
        this.mAddPictureBtn.setEnabled(false);
        this.mSaveBtn.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data) {
            mImageIsChanged = true;
            mRemovePictureBtn.setEnabled(true);

            Uri uri = data.getData();
            mTempImagePath = Utility.getPath(this,uri);

            Bitmap bitmap = Utility.extractMiniThumb(mTempImagePath,this.mWindowWidth,this.mWindowHeight,true);
            if (null != bitmap) {
                mTempImageThumb = Utility.extractMiniThumb(bitmap,40,40,false);
                this.mImageView.setImageBitmap(bitmap);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
