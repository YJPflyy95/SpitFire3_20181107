package org.astri.spitfire;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        EditText old_passsword = (EditText) findViewById(R.id.OldPassword_et);
        old_passsword.setTypeface(Typeface.DEFAULT);
        old_passsword.setTransformationMethod(new PasswordTransformationMethod());
        EditText new_passsword = (EditText) findViewById(R.id.NewPassword_et);
        new_passsword.setTypeface(Typeface.DEFAULT);
        new_passsword.setTransformationMethod(new PasswordTransformationMethod());
        EditText confirm_new_passsword = (EditText) findViewById(R.id.ConfirmNewPasssword_et);
        confirm_new_passsword.setTypeface(Typeface.DEFAULT);
        confirm_new_passsword.setTransformationMethod(new PasswordTransformationMethod());
        Button back = (Button) findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,MeActivity.class);
                startActivity(intent);
            }
        });
        Button save = (Button) findViewById(R.id.Save_bt);
        save.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,MeActivity.class);
                startActivity(intent);
            }
        });
        ImageView Me_iv = (ImageView) findViewById(R.id.Meiv);
        Me_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,MeActivity.class);
                startActivity(intent);
            }
        });
        TextView Me_tv = (TextView) findViewById(R.id.Metv);
        Me_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,MeActivity.class);
                startActivity(intent);
            }
        });
        ImageView Home_iv = (ImageView) findViewById(R.id.Homeiv);
        Home_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        TextView Home_tv = (TextView) findViewById(R.id.Hometv);
        Home_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        ImageView Activities_iv = (ImageView) findViewById(R.id.Activitiesiv);
        Activities_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,Activities.class);
                startActivity(intent);
            }
        });
        TextView Activities_tv = (TextView) findViewById(R.id.Activitiestv);
        Activities_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,Activities.class);
                startActivity(intent);
            }
        });
        ImageView historyBt = findViewById(R.id.Historyiv);
        historyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangePasswordActivity.this,HistoryActivity.class);
                startActivity(intent);
            }});
        TextView History_tv = (TextView) findViewById(R.id.Historytv);
        History_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(ChangePasswordActivity.this,HistoryActivity.class);
                startActivity(intent);
            }
        });
    }
}
