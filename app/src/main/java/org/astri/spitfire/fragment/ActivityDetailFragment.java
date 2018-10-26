package org.astri.spitfire.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.astri.spitfire.R;

import java.text.SimpleDateFormat;
import java.util.Date;

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
public class ActivityDetailFragment extends Fragment {

    public static final String TAG = ActivityDetailFragment.class.getSimpleName();

    private SeekBar seekBar;
    private TextView textView;
    private SeekBar seekBar1;
    private TextView textView2;
    private TextView warmUpTime;
    private TextView coolDownTime;

    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail_activity, container, false);
        Button update = view.findViewById(R.id.Update_bt);
        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new ActivitiesFragment())
                        .commit();
            }
        });

        Button back = view.findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new ActivitiesFragment())
                        .commit();
            }
        });

        seekBar = view.findViewById(R.id.seekBar);
        textView = view.findViewById(R.id.result);
        seekBar1 = view.findViewById(R.id.seekBar1);
        textView2 = view.findViewById(R.id.result2);
        warmUpTime = view.findViewById(R.id.Start_tv);
        coolDownTime = view.findViewById(R.id.End_tv);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");// HH:mm:ss

        //set current time
        Date date = new Date(System.currentTimeMillis());
        warmUpTime.setText("  Start              " + simpleDateFormat.format(date));
        coolDownTime.setText("  End                " + simpleDateFormat.format(date));

        //seekbar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("  Warmup：" + progress + " mins");
                Log.d(TAG, String.valueOf(seekBar.getId()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "按住seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "放开seekbar", Toast.LENGTH_SHORT).show();
            }
        });
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView2.setText("  Cooldown：" + progress + " mins");
                Log.d(TAG, String.valueOf(seekBar.getId()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "按住seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "放开seekbar", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
