package com.example.jc.personalaccount;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jc on 16/4/12.
 */
public class Utility {

    // 可用于生成缩略图。
    /**
     * Creates a centered bitmap of the desired size. Recycles the input.
     *
     * @param source
     */
    public static Bitmap extractMiniThumb(Bitmap source, int width, int height) {
        return extractMiniThumb(source, width, height, true);
    }

    public static Bitmap extractMiniThumb(Bitmap source, int width, int height, boolean recycle) {
        if (source == null) {
            return null;
        }

        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = width / (float) source.getWidth();
        } else {
            scale = height / (float) source.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap miniThumbnail = transform(matrix, source, width, height, false);

        if (recycle && miniThumbnail != source) {
            source.recycle();
        }
        return miniThumbnail;
    }

    private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, boolean scaleUp) {
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
             * In this case the bitmap is smaller, at least in one dimension,
             * than the target. Transform it by placing as much of the image as
             * possible into the target and leaving the top/bottom or left/right
             * (or both) black.
             */
            Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b2);

            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
                    + Math.min(targetWidth, source.getWidth()), deltaYHalf
                    + Math.min(targetHeight, source.getHeight()));
            int dstX = (targetWidth - src.width()) / 2;
            int dstY = (targetHeight - src.height()) / 2;
            Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
                    - dstY);
            c.drawBitmap(source, src, dst, null);
            return b2;
        }
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();

        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect = (float) targetWidth / targetHeight;

        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        }

        Bitmap b1;
        if (scaler != null) {
            // this is used for minithumb and crop, so we want to filter here.
            b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                    source.getHeight(), scaler, true);
        } else {
            b1 = source;
        }

        int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        int dy1 = Math.max(0, b1.getHeight() - targetHeight);

        Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
                targetHeight);

        if (b1 != source) {
            b1.recycle();
        }

        return b2;
    }

    public static Bitmap extractMiniThumb(String path, int width, int height, boolean isScale) {

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();

            if (isScale) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path,options);
                int imageWidth = options.outWidth;
                int imageHeight = options.outHeight;

                int scaleX = imageWidth / width;
                int scaleY = imageHeight / height;
                int scale = 1;
                if ((scaleX >= scaleY) && (scaleX >= 1)) {
                    scale = scaleX;
                } else if ((scaleY >= scaleX) && (scaleY >= 1)) {
                    scale = scaleY;
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;
            } else {
                options.inJustDecodeBounds = false;
                options.outWidth = width;
                options.outHeight = height;
            }

            return BitmapFactory.decodeFile(path,options);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    //**********************

    //从图库获取图片选中信息转为Bitmap
    public Bitmap getInsertedImage(Context context, Uri uri) {
        ContentResolver cr = context.getContentResolver();
        InputStream imgIS = null;
        try {
            imgIS = cr.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(imgIS);
    }

    //从图库获取图片选中信息转换为图片真实路径
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    //**********************

    //保存图片到指定路径
    public static boolean saveBitmapToFile(String path, Bitmap bitmap) {

        if (TextUtils.isEmpty(path)) {
            return false;
        }

        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //**********************
    /**
     *
     * @param oldPath String 原文件路径
     * @param newPath String 复制后路径
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件

                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();

                return true;
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }

        return false;
    }

    public static Boolean writeStringToFile(String path, String content, Boolean append) {
        try {
            FileOutputStream fOutputStream = new FileOutputStream(path,append);
            fOutputStream.write(content.getBytes());
            fOutputStream.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    //**********************
    /**
     * 删除文件，如果是文件夹，遍历删除所有文件
     *
     */
    public static void deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        deleteFile(new File(path));
    }

    public static void deleteFile(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 你应该知道 是删除的意思;
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        }
    }

    //**********************
    /**
     * 切换软键盘的状态
     * 如当前为收起变为弹出,若当前为弹出变为收起
     */
    public static void toggleInput(Context context){
        InputMethodManager inputMethodManager =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 强制隐藏输入法键盘
     */
    public static void hideInput(Context context,View view){
        InputMethodManager inputMethodManager =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 获取指定日期对应的星期
     * @param format 传入指定日期的格式， eg: yyyy-mm-dd
     * @param date 指定如期  eg: 2016-04-21
     * @return
     */
    public static String getWeek(String format, String date) {
        String Week = "周";
        SimpleDateFormat sdFormat = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
        switch(c.get(Calendar.DAY_OF_WEEK)){
            case 1:
                Week += "日";
                break;
            case 2:
                Week += "一";
                break;
            case 3:
                Week += "二";
                break;
            case 4:
                Week += "三";
                break;
            case 5:
                Week += "四";
                break;
            case 6:
                Week += "五";
                break;
            case 7:
                Week += "六";
                break;
            default:
                Week = "N/A";
                break;
        }
        return Week;
    }

    /**
     * 返回指定格式的日期字符串
     * @param format
     * @return 2016-04-25
     */
    public static String getFormatDate(String format) {
        return getFormatDate(format,new Date(System.currentTimeMillis()));
    }

    public static String getFormatDate(String format, Date date) {

        SimpleDateFormat formatter = new SimpleDateFormat (format);
        return formatter.format(date);
    }

    /**
     * 返回给定字符串在数组中的索引，没有返回-1
     * @param datas
     * @param text
     * @return
     */
    public static int getFindArrayIndex(String[] datas, String text) {
        for (int i = 0; i < datas.length; i++) {
            if (datas[i].equals(text)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 获取内置 SD卡路径
     * @return
     */
    public static String getInnerSDCardPath () {
        return Environment.getExternalStorageDirectory().getPath() ;
    }

    /**
     * 获取外置 SD卡路径:2016-05-08,小米手机无效
     * @return  应该就一条记录或空
     */
    public static List<String> getExtSDCardPath ()
    {
        List<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime ();
            Process proc = rt.exec( "mount");
            InputStream is = proc.getInputStream() ;
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line ;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSdCard"))
                {
                    String [] arr = line.split( " ");
                    String path = arr[1 ];
                    File file = new File(path);
                    if (file.isDirectory())
                    {
                        lResult.add(path) ;
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
            lResult.clear();
        }
        return lResult;
    }

    // 判断一个字符串是否都为数字
    public static boolean isDigit(String strNum) {
        return strNum.matches("[0-9]{1,}");
    }

    // 判断一个字符串是否都为数字
    public static boolean isDigit2(String strNum) {
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        Matcher matcher = pattern.matcher((CharSequence) strNum);
        return matcher.matches();
    }

    //截取数字
    public static String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    // 截取非数字
    public static String getNotNumber(String content) {
        Pattern pattern = Pattern.compile("\\D+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * Created by jc on 2016/5/9.
     */
    public static class MyOpenFileDialog {

        // 简单的Bundle参数回调接口
        public interface CallbackBundle {
            abstract void callback(Bundle bundle);
        }

        public static String tag = "OpenFileDialog";
        static final public String sRoot = "/";
        static final public String sParent = "..";
        static final public String sFolder = ".";
        static final public String sEmpty = "";
        static final private String sOnErrorMsg = "No rights to access!";

        // 参数说明
        // context:上下文
        // dialogid:对话框ID
        // title:对话框标题
        // callback:一个传递Bundle参数的回调接口
        // suffix:需要选择的文件后缀，比如需要选择wav、mp3文件的时候设置为".wav;.mp3;"，注意最后需要一个分号(;)
        // images:用来根据后缀显示的图标资源ID。
        //	根目录图标的索引为sRoot;
        //	父目录的索引为sParent;
        //	文件夹的索引为sFolder;
        //	默认图标的索引为sEmpty;
        //	其他的直接根据后缀进行索引，比如.wav文件图标的索引为"wav"
        public static Dialog createDialog(int id, Context context, String title, CallbackBundle callback, String suffix, Map<String, Integer> images){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(new FileSelectView(context, id, callback, suffix, images));
            Dialog dialog = builder.create();
            //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setTitle(title);
            return dialog;
        }

        static class FileSelectView extends ListView implements AdapterView.OnItemClickListener {


            private CallbackBundle callback = null;
            private String path = sRoot;
            private List<Map<String, Object>> list = null;
            private int dialogid = 0;

            private String suffix = null;

            private Map<String, Integer> imagemap = null;

            public FileSelectView(Context context, int dialogid, CallbackBundle callback, String suffix, Map<String, Integer> images) {
                super(context);
                this.imagemap = images;
                this.suffix = suffix==null?"":suffix.toLowerCase();
                this.callback = callback;
                this.dialogid = dialogid;
                this.setOnItemClickListener(this);
                refreshFileList();
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
                if(imagemap == null){
                    return 0;
                }
                else if(imagemap.containsKey(s)){
                    return imagemap.get(s);
                }
                else if(imagemap.containsKey(sEmpty)){
                    return imagemap.get(sEmpty);
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
                    files = new File(path).listFiles();
                }
                catch(Exception e){
                    files = null;
                }
                if(files==null){
                    // 访问出错
                    Toast.makeText(getContext(), sOnErrorMsg,Toast.LENGTH_SHORT).show();
                    return -1;
                }
                if(list != null){
                    list.clear();
                }
                else{
                    list = new ArrayList<Map<String, Object>>(files.length);
                }

                // 用来先保存文件夹和文件夹的两个列表
                ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
                ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

                if(!this.path.equals(sRoot)){
                    // 添加根目录 和 上一层目录
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", sRoot);
                    map.put("path", sRoot);
                    map.put("img", getImageId(sRoot));
                    list.add(map);

                    map = new HashMap<String, Object>();
                    map.put("name", sParent);
                    map.put("path", path);
                    map.put("img", getImageId(sParent));
                    list.add(map);
                }

                for(File file: files)
                {
                    if(file.isDirectory() && file.listFiles()!=null){
                        // 添加文件夹
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("name", file.getName());
                        map.put("path", file.getPath());
                        map.put("img", getImageId(sFolder));
                        lfolders.add(map);
                    }
                    else if(file.isFile()){
                        // 添加文件
                        String sf = getSuffix(file.getName()).toLowerCase();
                        if(suffix == null || suffix.length()==0 || (sf.length()>0 && suffix.indexOf("."+sf+";")>=0)){
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("name", file.getName());
                            map.put("path", file.getPath());
                            map.put("img", getImageId(sf));
                            lfiles.add(map);
                        }
                    }
                }

                list.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
                list.addAll(lfiles);	//再添加文件


                SimpleAdapter adapter = new SimpleAdapter(getContext(), list, R.layout.filedialogitem, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
                this.setAdapter(adapter);
                return files.length;
            }
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // 条目选择
                String pt = (String) list.get(position).get("path");
                String fn = (String) list.get(position).get("name");
                if(fn.equals(sRoot) || fn.equals(sParent)){
                    // 如果是更目录或者上一层
                    File fl = new File(pt);
                    String ppt = fl.getParent();
                    if(ppt != null){
                        // 返回上一层
                        path = ppt;
                    }
                    else{
                        // 返回更目录
                        path = sRoot;
                    }
                }
                else{
                    File fl = new File(pt);
                    if(fl.isFile()){
                        // 如果是文件
                        ((Activity)getContext()).dismissDialog(this.dialogid); // 让文件夹对话框消失

                        // 设置回调的返回值
                        Bundle bundle = new Bundle();
                        bundle.putString("path", pt);
                        bundle.putString("name", fn);
                        // 调用事先设置的回调函数
                        this.callback.callback(bundle);
                        return;
                    }
                    else if(fl.isDirectory()){
                        // 如果是文件夹
                        // 那么进入选中的文件夹
                        path = pt;
                    }
                }
                this.refreshFileList();
            }
        }
    }

    /**
     * 判断文件名是否合法
     * @param fileName
     * @return
     */
    public static boolean isValidFileName(String fileName) {

        if (fileName == null || fileName.length() > 255) {
            return false;
        } else {
            return true;//fileName.matches("/^([\\/] [\\w-]+)*$/");
        }
    }
}
