package org.astri.spitfire;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <pre>
 *     author : ghf
 *     e-mail : 869862783@qq.com
 *     time   : 2018/1/23
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ActivityDetail extends AppCompatActivity {
    private SeekBar seekBar;
    private TextView textView;
    private SeekBar seekBar1;
    private TextView textView2;
    TextView warmup_time;
    TextView cooldown_time;
//    Date dt;
//    String str_time;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Button back = (Button) findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,Activities.class);
                startActivity(intent);
            }
        });
        Button update = (Button) findViewById(R.id.Update_bt);
        update.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,Activities.class);
                startActivity(intent);
            }
        });
        ImageView Activities_iv = (ImageView) findViewById(R.id.Activitiesiv);
        Activities_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,Activities.class);
                startActivity(intent);
            }
        });
        TextView Activities_tv = (TextView) findViewById(R.id.Activitiestv);
        Activities_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,Activities.class);
                startActivity(intent);
            }
        });
        ImageView Home_iv = (ImageView) findViewById(R.id.Homeiv);
        Home_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        TextView Home_tv = (TextView) findViewById(R.id.Hometv);
        Home_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        ImageView Me_iv = (ImageView) findViewById(R.id.Meiv);
        Me_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,MeActivity.class);
                startActivity(intent);
            }
        });
        TextView Me_tv = (TextView) findViewById(R.id.Metv);
        Me_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,MeActivity.class);
                startActivity(intent);
            }
        });
        ImageView historyBt = findViewById(R.id.Historyiv);
        historyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityDetail.this,HistoryActivity.class);
                startActivity(intent);
            }});
        TextView History_tv = (TextView) findViewById(R.id.Historytv);
        History_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityDetail.this,HistoryActivity.class);
                startActivity(intent);
            }
        });
        //初始化Toolbar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //隐藏Actionbar后将toolbar设置上去替换Actionbar
//        setSupportActionBar(toolbar);
        //初始化seekbar，TextView
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textView = (TextView) findViewById(R.id.result);
        seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
        textView2 = (TextView) findViewById(R.id.result2);
        warmup_time = (TextView) findViewById(R.id.Start_tv);
        cooldown_time = (TextView) findViewById(R.id.End_tv);

//        dt = new Date();
//        str_time = dt.toLocaleString();
//        txt_time.setText(str_time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");// HH:mm:ss
//获取当前时间
        Date date = new Date(System.currentTimeMillis());
        warmup_time.setText("  Start              "+simpleDateFormat.format(date));
        cooldown_time.setText("  End                "+simpleDateFormat.format(date));

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
                Toast.makeText(ActivityDetail.this,"按住seekbar", Toast.LENGTH_SHORT).show();
            }
            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(ActivityDetail.this,"放开seekbar", Toast.LENGTH_SHORT).show();
            }
        });
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView2.setText("  Cooldown："+progress+" mins");
                Log.d("debug", String.valueOf(seekBar.getId()));
            }
            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(ActivityDetail.this,"按住seekbar", Toast.LENGTH_SHORT).show();
            }
            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(ActivityDetail.this,"放开seekbar", Toast.LENGTH_SHORT).show();
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
    }
}