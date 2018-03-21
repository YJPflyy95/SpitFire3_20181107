package org.astri.spitfire;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.astri.spitfire.component.ConfirmDialog;

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
public class HomeActivity extends AppCompatActivity {
    private Activity activity;
    private View dialogBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_home);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initViews();
        initListeners();
        Button test = (Button) findViewById(R.id.Test_bt);
        test.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(HomeActivity.this,ReadinessReadingActivity.class);
                startActivity(intent);
            }
        });
        Button AddNow = (Button) findViewById(R.id.AddNow_bt);
        AddNow.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(HomeActivity.this,Activities.class);
                startActivity(intent);
            }
        });
        Button Completeyourprofilebt = (Button) findViewById(R.id.Completeyourprofilebt);
        Completeyourprofilebt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(HomeActivity.this,MeActivity.class);
                startActivity(intent);
            }
        });
//        ImageView Activities_iv = (ImageView) findViewById(R.id.Activitiesiv);
//        Activities_iv.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(HomeActivity.this,Activities.class);
//                startActivity(intent);
//            }
//        });
//        TextView Activities_tv = (TextView) findViewById(R.id.Activitiestv);
//        Activities_tv.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(HomeActivity.this,Activities.class);
//                startActivity(intent);
//            }
//        });
//        ImageView Me_iv = (ImageView) findViewById(R.id.Meiv);
//        Me_iv.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(HomeActivity.this,MeActivity.class);
//                startActivity(intent);
//            }
//        });
//        TextView Me_tv = (TextView) findViewById(R.id.Metv);
//        Me_tv.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(HomeActivity.this,MeActivity.class);
//                startActivity(intent);
//            }
//        });
//        ImageView historyBt = findViewById(R.id.Historyiv);
//        historyBt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(HomeActivity.this,HistoryActivity.class);
//                startActivity(intent);
//            }});
//        TextView History_tv = (TextView) findViewById(R.id.Historytv);
//        History_tv.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(HomeActivity.this,HistoryActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    private void initViews() {
        dialogBtn = findViewById(R.id.Home_tv);
    }
    private void initListeners(){

        dialogBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new ConfirmDialog(activity).show();
            }
        });
    }
}
