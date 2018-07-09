package org.astri.spitfire.searchbar;

import android.util.Log;

import com.mancj.materialsearchbar.SimpleOnSearchActionListener;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/02/03
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MySearchActionConfirmed extends SimpleOnSearchActionListener {
    private static final String TAG = "MySearchActionConfirmed";

    @Override
    public void onSearchStateChanged(boolean enabled) {
        super.onSearchStateChanged(enabled);
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        Log.d(TAG, "onSearchConfirmed: "+ text.toString());
        super.onSearchConfirmed(text);
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        super.onButtonClicked(buttonCode);
    }
}
