package org.astri.spitfire.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import org.astri.spitfire.R;

import java.util.Calendar;


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
public class NewActivityFragment extends Fragment implements View.OnTouchListener{
    @Nullable
    private SeekBar seekBar;
    private TextView textView;
    private SeekBar seekBar1;
    private TextView textView1;
    private EditText warmup_time;
    private EditText cooldown_time;

    private TimePicker tp = null;
    private DatePicker dp = null;
    private Calendar calendar = null;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_activity, container, false);

        Button save = (Button) view.findViewById(R.id.Save_bt);
        save.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new ActivitiesFragment())
                        .commit();
            }
        });
        Button cancel = (Button) view.findViewById(R.id.Cancel_bt);
        cancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new ActivitiesFragment())
                        .commit();
            }
        });
        //初始化Toolbar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //隐藏Actionbar后将toolbar设置上去替换Actionbar
//        setSupportActionBar(toolbar);
        //初始化seekbar，TextView
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        textView = (TextView) view.findViewById(R.id.result);
        seekBar1 = (SeekBar) view.findViewById(R.id.seekBar1);
        textView1 = (TextView) view.findViewById(R.id.result1);
        warmup_time = (EditText) view.findViewById(R.id.Start_tv);
        cooldown_time = (EditText) view.findViewById(R.id.End_tv);

//        dt = new Date();
//        str_time = dt.toLocaleString();
//        txt_time.setText(str_time);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");// HH:mm:ss
////获取当前时间
//        Date date = new Date(System.currentTimeMillis());
//        warmupTime.setText("  Start              "+simpleDateFormat.format(date));
//        cooldownTime.setText("  End                "+simpleDateFormat.format(date));

        //seekbar设置监听
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("  Warmup："+progress+" mins");
                Log.d("debug", String.valueOf(seekBar.getId()));
            }
            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(),"按住seekbar", Toast.LENGTH_SHORT).show();
            }
            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(),"放开seekbar", Toast.LENGTH_SHORT).show();
            }
        });
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView1.setText("  Cooldown："+progress+" mins");
                Log.d("debug", String.valueOf(seekBar.getId()));
            }
            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(),"按住seekbar", Toast.LENGTH_SHORT).show();
            }
            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(),"放开seekbar", Toast.LENGTH_SHORT).show();
            }
        });
        warmup_time.setOnTouchListener(this);
        cooldown_time.setOnTouchListener(this);
        return view;
    }
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = View.inflate(getActivity(), R.layout.date_time_dialog, null);
            final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
            final TimePicker timePicker = (android.widget.TimePicker) view.findViewById(R.id.time_picker);
            builder.setView(view);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(Calendar.MINUTE);
//            timePicker.init(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), null);

            if (v.getId() == R.id.Start_tv) {
                final int inType = warmup_time.getInputType();
                warmup_time.setInputType(InputType.TYPE_NULL);
                warmup_time.onTouchEvent(event);
                warmup_time.setInputType(inType);
                warmup_time.setSelection(warmup_time.getText().length());

//                builder.setTitle("选取起始时间");
                builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {


                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int which) {

                        StringBuffer sb = new StringBuffer();
                        sb.append("  Start              ");
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        sb.append("  ");
                        sb.append(timePicker.getHour()).append(":").append(timePicker.getMinute());
//                        sb.append(timePicker.getCurrentHour())
//                                .append(":").append(timePicker.getCurrentMinute());
//                        sb.append(String.format("%d:%02d",
//                                timePicker.getHour(),
//                                timePicker.getMinute(),
//                                timePicker.getDrawingTime()));
                        warmup_time.setText(sb);
                        cooldown_time.requestFocus();

                        dialog.cancel();
                    }
                });

            } else if (v.getId() == R.id.End_tv) {
                int inType = cooldown_time.getInputType();
                cooldown_time.setInputType(InputType.TYPE_NULL);
                cooldown_time.onTouchEvent(event);
                cooldown_time.setInputType(inType);
                cooldown_time.setSelection(cooldown_time.getText().length());

//                builder.setTitle("选取结束时间");
                builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {

                        StringBuffer sb = new StringBuffer();
                        sb.append("  End                ");
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        sb.append("  ");
                        sb.append(timePicker.getCurrentHour())
                                .append(":").append(timePicker.getCurrentMinute());
                        cooldown_time.setText(sb);

                        dialog.cancel();
                    }
                });
            }

            Dialog dialog = builder.create();
            dialog.show();
        }

        return true;
    }
}
