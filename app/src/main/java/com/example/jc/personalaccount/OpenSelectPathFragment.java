package com.example.jc.personalaccount;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    public CallbackBundle mCallbackBundle;

    private String mTempRootPath;
    private List<Map<String,Object>> mTempList;
    private String mSuffix = "";
    private Map<String, Integer> imageMap = null;
    private boolean mIsSelectFile = true;
    private int mInvokeType = -1;

    private static final String sOnErrorMsg = "No rights to access!";

    private static final String sRoot = "/";
    private static final String sParent = "..";
    private static final String sFolder = ".";
    private static final String sEmpty = "";

    public static final String EXTRA_ISSELECTFILE = "IsSelectFile";
    public static final String EXTRA_SELECTPATH = "SelectPath";
    public static final String EXTRA_INVOKETYPE = "InvokeType";

    public void setDialogSetting(boolean bIsSelectFile, String suffix, int type) {

        this.mInvokeType = type;
        this.mIsSelectFile = bIsSelectFile;
        if (!TextUtils.isEmpty(suffix)) {
            this.mSuffix = suffix;
        }
    }

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

        this.imageMap = new HashMap<>();
        this.imageMap.put(OpenSelectPathFragment.sRoot,R.drawable.filedialog_root);
        this.imageMap.put(OpenSelectPathFragment.sParent,R.drawable.filedialog_folder_up);
        this.imageMap.put(OpenSelectPathFragment.sFolder,R.drawable.filedialog_folder);
        this.imageMap.put(OpenSelectPathFragment.sEmpty,R.drawable.filedialog_file);

        mTVPath = (TextView)view.findViewById(R.id.fragment_open_select_path_text);
        mBtnBack = (Button)view.findViewById(R.id.fragment_open_select_path_btn_cancel);
        mBtnOK = (Button)view.findViewById(R.id.fragment_open_select_path_btn_ok);
        mLVPaths = (ListView)view.findViewById(R.id.fragment_open_select_path_list);

        this.mBtnOK.setEnabled(false);

        this.bindingUIEvent();

        this.mTempRootPath = sRoot;
        this.refreshFileList();

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
                bundle.putString(OpenSelectPathFragment.EXTRA_SELECTPATH, mTempRootPath);
                bundle.putInt(OpenSelectPathFragment.EXTRA_INVOKETYPE,mInvokeType);
                // 调用事先设置的回调函数
                mCallbackBundle.callback(bundle);

                dismiss();
            }
        });

        mLVPaths.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                folderItemClick(position);
            }
        });
    }

    private String getSuffix(String filename){
        int dix = filename.lastIndexOf('.');
        if(dix<0){
            return "";
        }
        else{
            return filename.substring(dix+1);
        }
    }

    private int getImageId(String s){
        if(null == this.imageMap){
            return 0;
        }
        else if(this.imageMap.containsKey(s)){
            return this.imageMap.get(s);
        }
        else if(this.imageMap.containsKey(sEmpty)){
            return this.imageMap.get(sEmpty);
        }
        else {
            return 0;
        }
    }

    private int refreshFileList()
    {
        // 刷新文件列表
        File[] files = null;
        try{
            files = new File(mTempRootPath).listFiles();
        }
        catch(Exception e){
            files = null;
        }
        if(files==null){
            // 访问出错
            Toast.makeText(getContext(), sOnErrorMsg,Toast.LENGTH_SHORT).show();
            return -1;
        }
        if(mTempList != null){
            mTempList.clear();
        }
        else{
            mTempList = new ArrayList<Map<String, Object>>(files.length);
        }

        // 用来先保存文件夹和文件夹的两个列表
        ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
        ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

        if(!this.mTempRootPath.equals(sRoot)){
            // 添加根目录 和 上一层目录
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", sRoot);
            map.put("path", sRoot);
            map.put("img", this.getImageId(sRoot));
            mTempList.add(map);

            map = new HashMap<String, Object>();
            map.put("name", sParent);
            map.put("path", mTempRootPath);
            map.put("img", this.getImageId(sParent));
            mTempList.add(map);
        }

        for(File file: files)
        {
            if(file.isDirectory() && file.listFiles()!=null){
                // 添加文件夹
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", file.getName());
                map.put("path", file.getPath());
                map.put("img", this.getImageId(sFolder));
                lfolders.add(map);
            }
            else if(file.isFile()){

                if (!this.mIsSelectFile) {
                    continue;
                }

                // 添加文件
                String sf = getSuffix(file.getName()).toLowerCase();
                if(mSuffix == null || mSuffix.length()==0 || (sf.length()>0 && mSuffix.indexOf("."+sf+";")>=0)){
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", file.getName());
                    map.put("path", file.getPath());
                    map.put("img", this.getImageId(sEmpty));
                    lfiles.add(map);
                }
            }
        }

        mTempList.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
        mTempList.addAll(lfiles);	//再添加文件

        SimpleAdapter adapter = new SimpleAdapter(getContext(), mTempList, R.layout.filedialogitem, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
        this.mLVPaths.setAdapter(adapter);
        return files.length;
    }

    private void folderItemClick(int position) {
        this.mBtnOK.setEnabled(false);
        // 条目选择
        String pt = (String) mTempList.get(position).get("path");
        String fn = (String) mTempList.get(position).get("name");
        if(fn.equals(sRoot) || fn.equals(sParent)){
            // 如果是更目录或者上一层
            File fl = new File(pt);
            String ppt = fl.getParent();
            if(ppt != null){
                // 返回上一层
                mTempRootPath = ppt;
            }
            else{
                // 返回更目录
                mTempRootPath = sRoot;
            }
        }
        else{
            File fl = new File(pt);
            if(fl.isFile()){

                this.mBtnOK.setEnabled(true);
            }
            else if(fl.isDirectory()){
                // 如果是文件夹
                // 那么进入选中的文件夹
                mTempRootPath = pt;
                mTVPath.setText(mTempRootPath);

                if (!this.mIsSelectFile) {
                    this.mBtnOK.setEnabled(true);
                }
            }
        }
        this.refreshFileList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mActivity = (FragmentActivity) context;
    }
}
