package org.astri.spitfire.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/01/23
 *     desc   : time util
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MyTimeUtils {

    public static String dateFormat_day = "HH:mm";
    public static String dateFormat_month = "MM-dd";

    /**
     * 时间转换成字符串,默认为"yyyy-MM-dd HH:mm:ss"
     *
     * @param time 时间
     */
    public static String dateToString(long time) {
        return dateToString(time, "yyyy.MM.dd HH:mm");
    }

    /**
     * 时间转换成字符串,指定格式
     *
     * @param time   时间
     * @param format 时间格式
     */
    public static String dateToString(long time, String format) {
        Date date = new Date(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    /**
     * return Chinese week of year
     * @param date
     * @return
     */
    public static int getWeekOfYearCN(final Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        // 得到星期,0-->星期天，1->星期一，6->星期六(外国人眼中一周的第一天为星期天，中国为星期一)
        if(dayOfWeek == 1){ // 周日，
            weekOfYear = weekOfYear - 1;
        }
        return weekOfYear;
    }

    /**
     * get Day of Month
     * @param date
     * @return
     */
    public static int getDayOfMonth(final Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

}
