package org.astri.spitfire.util;

import android.util.Log;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/01/23
 *     desc   : log
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class LogUtil {

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;

    public static int level = VERBOSE;

    public static void v(String tag, String msg){
        if(level <= VERBOSE){
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg){
        if(level <= DEBUG){
            Log.v(tag, msg);
        }
    }

    public static void i(String tag, String msg){
        if(level <= INFO){
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg){
        if(level <= WARN){
            Log.v(tag, msg);
        }
    }

    public static void e(String tag, String msg){
        if(level <= ERROR){
            Log.v(tag, msg);
        }
    }

}
