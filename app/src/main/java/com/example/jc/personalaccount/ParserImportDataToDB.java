package com.example.jc.personalaccount;

import android.text.TextUtils;

import com.example.jc.personalaccount.Data.DetailItem;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 解析导入的数据，并存入数据库中
 * Created by jc on 2016/5/11.
 */
public class ParserImportDataToDB {

    /**
     * 信用卡记录导入数据库
     * @param fileName
     * @return
     */
    public static Boolean saveCardAccountCSVToDataStore(String fileName) {

        try {
            File file = new File (fileName);
            FileReader fileReader=new FileReader(file);
            BufferedReader bufReader=new BufferedReader(fileReader);

            String strLine = bufReader.readLine(); //过滤掉标题行

            List<DetailItem> listData = new ArrayList<DetailItem>();
            while((strLine =  bufReader.readLine()) != null) {

                DetailItem item = parserCardAccountLine(strLine);
                if (null != item) {
                    listData.add(item);
                }
            }

            bufReader.close();
            fileReader.close();

            return GlobalData.DataStoreHelper.addDetailItem((DetailItem[]) listData.toArray(new DetailItem[listData.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 2016-05-10
     * 目前有如下两种格式，注意处理
     * 2016/4/25,RMB 16.60,欣交信,消费 支付宝 -taobao- 王冬雪
     * 20160430,223,中信信,昊良加油站中国石油化工ZHONGGUO???? CN?
     * @param line
     * @return
     */
    private static DetailItem parserCardAccountLine(String line) {
        DetailItem item = null;

        try {
            if (TextUtils.isEmpty(line)) {
                return item;
            }

            String[] items = line.split(",");
            if (4 == items.length) {

                //1,Date
                String date = getCardAccountDate(items[0]);
                if (null == date) {
                    return item;
                }

                item = new DetailItem();
                item.date = date;

                item.value = (int)(Float.parseFloat(items[1].replace("RMB ","").trim()) * 100);

                item.from = items[2];
                item.description = items[3];

                return item;
            }
        } catch (Exception e) {
            item = null;
            e.printStackTrace();
        }

        return item;
    }

    //传入字符串日期转换为:2016-05-11格式

    /**
     * 2016/4/25
     * 20160430
     * @param source
     * @return
     */
    private static String getCardAccountDate(String source) {

        if (TextUtils.isEmpty(source)) {
            return null;
        }

        if (source.contains("/")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            return Utility.getFormatDate(GlobalData.DATEFORMAT,sdf.parse(source,new ParsePosition(0)));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return Utility.getFormatDate(GlobalData.DATEFORMAT,sdf.parse(source,new ParsePosition(0)));
        }
    }
}
