package org.astri.spitfire.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.xw.repo.BubbleSeekBar;

import org.astri.spitfire.R;
import org.astri.spitfire.util.LogUtil;

import java.util.Locale;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/07/09
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private Activity mActivity;

    // 一共四种算法
    private String[] algorithms = {
           "Learning Zone",
           "Awareness Zone",
           "Recovery Zone",
           "Relaxing Zone",
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, algorithms);
        ListView alglist = view.findViewById(R.id.algorithm_list);
        alglist.setAdapter(adapter);
        init(view);
        LogUtil.d(TAG, "onCreate");

        BubbleSeekBar bubbleSeekBar1 = view.findViewById(R.id.intense_seek_bar_1);

        bubbleSeekBar1.getConfigBuilder()
                .min(1)
                .max(5)
                .progress(2)
                .sectionCount(4)
                .trackColor(ContextCompat.getColor(mActivity, R.color.gray))
                .secondTrackColor(ContextCompat.getColor(mActivity, R.color.appmain))
                .thumbColor(ContextCompat.getColor(mActivity, R.color.appmain))
                .thumbRadius(10) // add by huguodong
                .showSectionText()
                .sectionTextColor(ContextCompat.getColor(mActivity, R.color.gray))
                .sectionTextSize(18)
                .showThumbText()
                .thumbTextColor(ContextCompat.getColor(mActivity, R.color.appmain))
                .thumbTextSize(18)
//                .bubbleColor(ContextCompat.getColor(mActivity, R.color.color_red))
//                .bubbleTextSize(18)
                .hideBubble()
                .showSectionMark()
                .seekStepSection()
                .touchToSeek()
                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .build();


        bubbleSeekBar1.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListenerAdapter() {

            // 监听值的改变

            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                String s = String.format(Locale.CHINA, "onChanged int:%d, float:%.1f", progress, progressFloat);

                LogUtil.d(TAG, s);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                String s = String.format(Locale.CHINA, "onActionUp int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                String s = String.format(Locale.CHINA, "onFinally int:%d, float:%.1f", progress, progressFloat);
                LogUtil.d(TAG, s);
            }
        });

//        bubbleSeekBar1.setProgress(2); // 设置进度


        return view;
    }

    private void init(View view) {

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (Activity) context;
    }
}
