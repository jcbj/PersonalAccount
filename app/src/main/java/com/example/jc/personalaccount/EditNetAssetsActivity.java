package com.example.jc.personalaccount;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.jc.personalaccount.Data.BalanceSheetItem;

public class EditNetAssetsActivity extends AppCompatActivity {

    private int editCount = 0;
    private int type = 0;
    private EditText mETName;
    private EditText mETDescription;
    private EditText mETWorth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_net_assets);

        Intent intent = this.getIntent();
        type = intent.getIntExtra(GlobalData.EXTRA_HOME_EDIT_TYPE, 0);

        ArrayAdapter<String> typeSpinnerItems = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.fragment_home_property_title),getString(R.string
                        .fragment_home_debt_title)});
        final Spinner typeSpinner = (Spinner) findViewById(R.id.fragment_home_edit_type_spinner);
        typeSpinner.setAdapter(typeSpinnerItems);
        typeSpinner.setEnabled(false);
        //直接设置选中项，并立即生效
        typeSpinner.setSelection(type,true);

        mETName = (EditText)findViewById(R.id.fragment_home_edit_name_text);
        mETWorth = (EditText)findViewById(R.id.fragment_home_edit_worth_text);
        mETDescription = (EditText)findViewById(R.id.fragment_home_edit_description_text);

        Button mSaveBtn = (Button) findViewById(R.id.fragment_home_edit_save_button);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BalanceSheetItem item = new BalanceSheetItem();
                item.worthType = (0 == type) ? BalanceSheetItem.WorthType.Property : BalanceSheetItem.WorthType.Debt;
                item.name = mETName.getText().toString();
                item.worth = (int)(Double.parseDouble(mETWorth.getText().toString()) * 100);
                item.description = mETDescription.getText().toString();
                item.imageThumb = null;
                item.imagePath = null;

                if (GlobalData.DataStoreHelper.EditWorthItem(GlobalData.CurrentUser,item,true)) {
                    editCount++;

                    Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.common_save_success),Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,10);
                    toast.show();
                } else {
                    new AlertDialog.Builder(getApplicationContext()).setTitle(getString(R.string.common_str_information))
                            .setMessage(getString(R.string.common_save_failed))
                            .setPositiveButton(getString(R.string.common_btn_ok),null)
                            .show();
                }
            }
        });

        Button mBackBtn = (Button) findViewById(R.id.fragment_home_edit_back_button);
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(EditNetAssetsActivity.this,MainActivity.class);
                intent1.putExtra(GlobalData.EXTRA_WHO_HOME_TAGNAME,GlobalData.STRING_ACTIVITY_EDIT_NETASSETS);
                intent1.putExtra(GlobalData.EXTRA_EDIT_HOME_ISREFRESH,editCount);
                startActivity(intent1);
            }
        });
    }
}
