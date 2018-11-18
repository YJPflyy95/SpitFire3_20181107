package org.astri.spitfire.ble.common;

import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/09/21
 *     desc   : Polling data from device
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PollingDevice {

    private Handler mHandler;
    private Map<Runnable, Runnable> mTaskMap = new HashMap<Runnable, Runnable>();

    public PollingDevice(Handler handler) {
        mHandler = handler;
    }

    public void startPolling(Runnable runnable, long interval)
    {
        startPolling(runnable, interval, false);
    }

    public void startPolling(final Runnable runnable, final long interval, boolean runImmediately)
    {
        if (runImmediately) {
            runnable.run();
        }
        Runnable task = mTaskMap.get(runnable);
        if (task == null)
        {
            task = new Runnable()
            {   @Override
                public void run()
                {
                    runnable.run();
                    post(runnable, interval);
                }
            };
            mTaskMap.put(runnable, task);
        }//if( )
        post(runnable, interval);
    }

    public void endPolling(Runnable runnable)
    {
        if (mTaskMap.containsKey(runnable))
        {
            mHandler.removeCallbacks(mTaskMap.get(runnable));
        }
    }

    private void post(Runnable runnable, long interval)
    {
        Runnable task = mTaskMap.get(runnable);
        mHandler.removeCallbacks(task);
        mHandler.postDelayed(task, interval);
    }
}
