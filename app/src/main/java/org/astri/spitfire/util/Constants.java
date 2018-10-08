package org.astri.spitfire.util;

import java.text.SimpleDateFormat;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/01/31
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Constants {
    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static final SimpleDateFormat FMT_dd = new SimpleDateFormat("dd");
    public static final SimpleDateFormat FMT_d = new SimpleDateFormat("d");
    public static final SimpleDateFormat FMT_HMS = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat FMT_HM = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat FMT_MMddyyyy = new SimpleDateFormat("MM/dd/yyyy");

    // According to Production phase, set IS_PRODUCTION to show specific functions.
    public static final boolean IS_PRODUCTION = false;

    // According to is in User Testing Phase, close specific functions, such as user login check.
    public static final boolean IS_USER_TESTING = true;
}
