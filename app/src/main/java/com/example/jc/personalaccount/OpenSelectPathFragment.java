package com.example.jc.personalaccount;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


/**
 * create an instance of this fragment.
 */
public class OpenSelectPathFragment extends DialogFragment {

    // 简单的Bundle参数回调接口
    public interface CallbackBundle {
        abstract void callback(Bundle bundle);
    }

    protected FragmentActivity mActivity;
    private TextView mTVPath;
    private Button mBtnOK;
    private Button mBtnBack;
    private ListView mLVPaths;

    public CallbackBundle mBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_open_select_path, container, false);

        mTVPath = (TextView)view.findViewById(R.id.fragment_open_select_path_text);
        mBtnBack = (Button)view.findViewById(R.id.fragment_open_select_path_btn_cancel);
        mBtnOK = (Button)view.findViewById(R.id.fragment_open_select_path_btn_ok);
        mLVPaths = (ListView)view.findViewById(R.id.fragment_open_select_path_list);

        this.bindingUIEvent();

        return view;
    }

    private void bindingUIEvent() {
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 设置回调的返回值
                Bundle bundle = new Bundle();
                bundle.putString("path", "test");
//                bundle.putString("name", fn);
                // 调用事先设置的回调函数
                mBundle.callback(bundle);

                dismiss();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mActivity = (FragmentActivity) context;
    }

}
