package org.astri.spitfire.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.astri.spitfire.Activities;
import org.astri.spitfire.ActivityDetail;
import org.astri.spitfire.HistoryActivity;
import org.astri.spitfire.HomeActivity;
import org.astri.spitfire.MeActivity;
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
    @Nullable

    private SeekBar seekBar;
    private TextView textView;
    private SeekBar seekBar1;
    private TextView textView2;
    TextView warmup_time;
    TextView cooldown_time;

    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_activity, container, false);

        Button update = (Button) view.findViewById(R.id.Update_bt);
        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new ActivitiesFragment())
                        .commit();
            }
        });

        Button back = (Button) view.findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new ActivitiesFragment())
                        .commit();
            }
        });
//        Button back = (Button) findViewById(R.id.Back_bt);
//        back.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(ActivityDetail.this,Activities.class);
//                startActivity(intent);
//            }
//        });
//        Button update = (Button) findViewById(R.id.Update_bt);
//        update.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(ActivityDetail.this,Activities.class);
//                startActivity(intent);
//            }
//        });

        //初始化Toolbar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //隐藏Actionbar后将toolbar设置上去替换Actionbar
//        setSupportActionBar(toolbar);
        //初始化seekbar，TextView
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        textView = (TextView) view.findViewById(R.id.result);
        seekBar1 = (SeekBar) view.findViewById(R.id.seekBar1);
        textView2 = (TextView) view.findViewById(R.id.result2);
        warmup_time = (TextView) view.findViewById(R.id.Start_tv);
        cooldown_time = (TextView) view.findViewById(R.id.End_tv);

//        dt = new Date();
//        str_time = dt.toLocaleString();
//        txt_time.setText(str_time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");// HH:mm:ss
//获取当前时间
        Date date = new Date(System.currentTimeMillis());
        warmup_time.setText("  Start              " + simpleDateFormat.format(date));
        cooldown_time.setText("  End                " + simpleDateFormat.format(date));

        //seekbar设置监听
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("  Warmup：" + progress + " mins");
                Log.d("debug", String.valueOf(seekBar.getId()));
            }

            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "按住seekbar", Toast.LENGTH_SHORT).show();
            }

            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "放开seekbar", Toast.LENGTH_SHORT).show();
            }
        });
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView2.setText("  Cooldown：" + progress + " mins");
                Log.d("debug", String.valueOf(seekBar.getId()));
            }

            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "按住seekbar", Toast.LENGTH_SHORT).show();
            }

            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "放开seekbar", Toast.LENGTH_SHORT).show();
            }
        });

//        //浮动button实例化
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        //设置点击监听
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        return view;
    }
}
