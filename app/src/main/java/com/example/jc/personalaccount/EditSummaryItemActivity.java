package com.example.jc.personalaccount;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

public class EditSummaryItemActivity extends AppCompatActivity {

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


    }
}
