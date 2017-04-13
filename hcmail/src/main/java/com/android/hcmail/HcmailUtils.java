package com.android.hcmail;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhujiabin on 2017/3/16.
 */

public class HcmailUtils {

     /**
     * 时间戳转换成字符串
     */
    public static String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日");
        return sf.format(d);
    }

    public static boolean isSameWeek(String str) {
        Date date = HcmailUtils.stringToDate(str);
        Boolean isSameWeek = HcmailUtils.isSameWeekWithToday(date);
        return isSameWeek;
    }

    public static boolean isSameLastWeek(String str) {
        Date date = HcmailUtils.stringToDate(str);
        Boolean isSameWeek = HcmailUtils.isSameWeekWithLast(date);
        return isSameWeek;
    }

    /**
     * <pre>
     * 判断date和当前日期是否在同一周内
     * 注:
     * Calendar类提供了一个获取日期在所属年份中是第几周的方法，对于上一年末的某一天
     * 和新年初的某一天在同一周内也一样可以处理，例如2012-12-31和2013-01-01虽然在
     * 不同的年份中，但是使用此方法依然判断二者属于同一周内
     * </pre>
     *
     * @param date
     * @return
     */
    public static boolean isSameWeekWithToday(Date date) {

        if (date == null) {
            return false;
        }

        // 0.先把Date类型的对象转换Calendar类型的对象
        Calendar todayCal = Calendar.getInstance();
        Calendar dateCal = Calendar.getInstance();

        todayCal.setTime(new Date());
        dateCal.setTime(date);

        // 1.比较当前日期在年份中的周数是否相同
        if (todayCal.get(Calendar.WEEK_OF_YEAR) == dateCal.get(Calendar.WEEK_OF_YEAR)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSameWeekWithLast(Date date) {
        if (date == null) {
            return false;
        }

        // 0.先把Date类型的对象转换Calendar类型的对象
        Calendar todayCal = Calendar.getInstance();
        Calendar dateCal = Calendar.getInstance();
        todayCal.add(Calendar.DATE, -7);
        dateCal.setTime(date);
        // 1.比较当前日期在年份中的周数是否相同
        if (todayCal.get(Calendar.WEEK_OF_YEAR) == dateCal.get(Calendar.WEEK_OF_YEAR)) {
            return true;
        } else {
            return false;
        }
    }

    public static Date stringToDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = null;
        try {
            date = sdf.parse(str);//提取格式中的日期
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
