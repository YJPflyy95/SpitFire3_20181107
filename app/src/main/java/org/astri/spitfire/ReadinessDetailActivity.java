package org.astri.spitfire;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.TimeUtils;

import org.astri.spitfire.entities.History;

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
public class ReadinessDetailActivity extends AppCompatActivity {

    private TextView duration;
    private TextView time;
    private TextView date;
    private TextView hrv;
    private TextView avghr;
    private TextView minhr;
    private TextView maxhr;
    private TextView comment;
    private Button btDelete;
    private Button btSave;

    private SimpleDateFormat timefmt = new SimpleDateFormat("K:mm a");
    private SimpleDateFormat datefmt = new SimpleDateFormat("dd/MM/yyyy");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readiness_detail);

        initView();

        initHistoryDetail();

    }

    private void initHistoryDetail() {
        Intent intent = getIntent();
        History his = (History) intent.getSerializableExtra("history_data");
        if (his != null) {
            //让其不显示的
//            btDelete.setVisibility(View.GONE);
//            btSave.setVisibility(View.GONE);
            duration.setText("Duration: " + his.getDuration() + " mins");
            time.setText("Time: " + TimeUtils.date2String(new Date(his.getTestTime()), timefmt));
            date.setText("Date: " + TimeUtils.date2String(new Date(his.getTestTime()), datefmt));
            hrv.setText("HRV: " + his.getHRV());
            avghr.setText("Avg HR: " + his.getAvgHR());
            minhr.setText("Min HR: " + his.getMinHR()); // Min HR: 45.21 ?
            maxhr.setText("Max HR: " + his.getMaxHR());
        }
    }

    private void initView() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Button delete = (Button) findViewById(R.id.Delete_bt);
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ReadinessDetailActivity.this, GodActivity.class);
                startActivity(intent);
            }
        });
        Button save = (Button) findViewById(R.id.Save_bt);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ReadinessDetailActivity.this, GodActivity.class);
                startActivity(intent);
            }
        });

        duration = findViewById(R.id.Duration_tv);
        time = findViewById(R.id.Time_tv);
        date = findViewById(R.id.Date_tv);
        hrv = findViewById(R.id.HRVScores_tv);
        avghr = findViewById(R.id.AvgHR_tv);
        minhr = findViewById(R.id.MinHR_tv);
        maxhr = findViewById(R.id.MaxHR_tv);
        comment = findViewById(R.id.tx_comment);
        btDelete = findViewById(R.id.Delete_bt);
        btSave = findViewById(R.id.Save_bt);
    }
}
