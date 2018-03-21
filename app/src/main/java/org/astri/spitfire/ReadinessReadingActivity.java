package org.astri.spitfire;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
public class ReadinessReadingActivity extends AppCompatActivity {
    private final static int COUNT = 1;
    private TextView countDown;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_readiness_reading);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Button Cancel = (Button) findViewById(R.id.Cancel_bt);
        Cancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ReadinessReadingActivity.this,GodActivity.class);
                startActivity(intent);
            }
        });
        initView();
    }

    //sehedule的第而个参数是第一次启动延时的时间，第三个是每隔多长时间执行一次。单位都是ms。
//因此这里是每一秒发送一次消息给handler更新UI。
    //然后三秒后时间到了，在timer的第二个sehedule中进行跳转到另外一个界面
    private void initView() {
        countDown =  (TextView) findViewById(R.id.count_down);
        final Timer timer = new Timer();
        final long end = System.currentTimeMillis() + 1000*10;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(COUNT);

            }
        }, 0, 1000);
        //这里的schedule的第二个参数意义是到了这个时间尽快运行run里面的方法
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Intent i = new Intent(ReadinessReadingActivity.this, ReadinessDetailActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                timer.cancel();
            }
        }, new Date(end));

    }

    private Handler handler = new Handler(){
        int num = 120;
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case COUNT:
                    countDown.setText(String.valueOf(num));
                    num--;
                    break;

                default:
                    break;
            }
        };
    };
}
