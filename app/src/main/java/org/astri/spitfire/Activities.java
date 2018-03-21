package org.astri.spitfire;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.astri.spitfire.component.SlideListView;

import java.util.ArrayList;
import java.util.List;

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
public class Activities extends AppCompatActivity {
    private SlideListView listView;
    private List<String> list=new ArrayList<String>();
    private ListViewSlideAdapter listViewSlideAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);
        getData();
        initView();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Button add = (Button) findViewById(R.id.Add_bt);
        add.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(Activities.this,NewActivity.class);
                startActivity(intent);
            }
        });
        ImageView Home_iv = (ImageView) findViewById(R.id.Homeiv);
        Home_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(Activities.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        TextView Home_tv = (TextView) findViewById(R.id.Hometv);
        Home_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(Activities.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        ImageView Me_iv = (ImageView) findViewById(R.id.Meiv);
        Me_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(Activities.this,MeActivity.class);
                startActivity(intent);
            }
        });
        TextView Me_tv = (TextView) findViewById(R.id.Metv);
        Me_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(Activities.this,MeActivity.class);
                startActivity(intent);
            }
        });
        ImageView historyBt = findViewById(R.id.Historyiv);
        historyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activities.this,HistoryActivity.class);
                startActivity(intent);
            }});
        TextView History_tv = (TextView) findViewById(R.id.Historytv);
        History_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(Activities.this,HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView(){
        listView=(SlideListView)findViewById(R.id.list);
        listViewSlideAdapter=new ListViewSlideAdapter(this,list);
        listView.setAdapter(listViewSlideAdapter);
        listViewSlideAdapter.setOnClickListenerEditOrDelete(new ListViewSlideAdapter.OnClickListenerEditOrDelete() {

            public void OnClickListenerEdit(int position) {
                Toast.makeText(Activities.this, "edit position: " + position, Toast.LENGTH_SHORT).show();
            }


            public void OnClickListenerDelete(int position) {
                Toast.makeText(Activities.this, "delete position: " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData(){
//        for (int i=0;i<2;i++){
//            list.add(new String("第"+i+"个item"));
//        }
//        list.add(new String("\"陕A666\", \"黑色\", \"奥迪A6\""));
//        list.add(3,"nihao");
        list.add("Jogging");
        list.add("Gym");
        list.add("Exam");
    }
//private void initDatas() {
//    List msg1 = new List("陕A666", "黑色", "奥迪A6");
//    List msg2 = new List("陕A888", "黑色", "奥迪A11");
//    List msg3 = new List("陕A999", "黑色", "奥迪A0");
//    List msg4 = new List("陕A0000", "黑色", "奥迪A9");
//
//    Vehicle vh01 = new Vehicle(R.drawable.tab_icon_home_my, msg1, R.drawable.tab_icon_my_back);
//    Vehicle vh02 = new Vehicle(R.drawable.tab_icon_home_my, msg2, R.drawable.tab_icon_my_back);
//    Vehicle vh03 = new Vehicle(R.drawable.tab_icon_home_my, msg3, R.drawable.tab_icon_my_back);
//    Vehicle vh04 = new Vehicle(R.drawable.tab_icon_home_my, msg4, R.drawable.tab_icon_my_back);
//
//    msg.add(vh01);
//    msg.add(vh02);
//    msg.add(vh03);
//    msg.add(vh04);
//}
}