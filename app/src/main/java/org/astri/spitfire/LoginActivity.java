package org.astri.spitfire;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.event.BusManager;

import org.astri.spitfire.ble.common.BluetoothDeviceManager;
import org.astri.spitfire.entities.UserDataManager;

import static org.astri.spitfire.util.Constants.IS_PRODUCTION;
import static org.astri.spitfire.util.Constants.IS_USER_TESTING;

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
public class LoginActivity extends AppCompatActivity {

    private EditText mAccount;                         //用户名编辑
    private EditText password;                         //密码编辑
    private Button mRegisterButton;                   //注册按钮
    private Button mLoginButton;                      //登录按钮
    private Button mForgetPassword;
    private CheckBox mRememberCheck;

    private SharedPreferences login_sp;
    private TextView mChangepwdText;
    private UserDataManager mUserDataManager;         //用户数据管理类

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mAccount = (EditText) findViewById(R.id.EmailAddress_et);
        password = (EditText) findViewById(R.id.Password_et);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
        mRegisterButton = (Button) findViewById(R.id.Register_bt);
//        mRegisterButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
//                startActivity(intent);
//            }
//        });

        mForgetPassword = (Button) findViewById(R.id.ForgetPassword_bt);
        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        mLoginButton = (Button) findViewById(R.id.Login_bt);
//        mLoginButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(LoginActivity.this,GodActivity.class);
//                startActivity(intent);
//            }
//        });

        login_sp = getSharedPreferences("userInfo", 0);
        String name = login_sp.getString("USER_NAME", "");
        String pwd = login_sp.getString("PASSWORD", "");
        boolean choseRemember = login_sp.getBoolean("mRememberCheck", false);
        boolean choseAutoLogin = login_sp.getBoolean("mAutologinCheck", false);
        //如果上次选了记住密码，那进入登录页面也自动勾选记住密码，并填上用户名和密码
        if (choseRemember) {
            mAccount.setText(name);
            password.setText(pwd);
            mRememberCheck.setChecked(true);
        }
        mRegisterButton.setOnClickListener(mListener);                      //采用OnClickListener方法设置不同按钮按下之后的监听事件
        mLoginButton.setOnClickListener(mListener);

        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();                              //建立本地数据库
        }

        // 全局初始化一次即可
        ViseLog.getLogConfig().configAllowLog(true);//配置日志信息
        ViseLog.plant(new LogcatTree());//添加Logcat打印信息
        BluetoothDeviceManager.getInstance().init(this);
//        BusManager.getBus().register(this);

    }

    View.OnClickListener mListener = new View.OnClickListener() {                  //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Register_bt:                            //登录界面的注册按钮
                    Intent intent_Login_to_Register = new Intent(LoginActivity.this, RegisterActivity.class);    //切换Login Activity至User Activity
                    startActivity(intent_Login_to_Register);
                    finish();
                    break;
                case R.id.Login_bt:                              //登录界面的登录按钮
                    login();
                    break;
//                case R.id.login_text_change_pwd:                             //登录界面的注销按钮
//                    Intent intent_Login_to_reset = new Intent(Login.this,Resetpwd.class) ;    //切换Login Activity至User Activity
//                    startActivity(intent_Login_to_reset);
//                    finish();
//                    break;
            }
        }
    };

    public void login() {


        if (!IS_USER_TESTING) {
            //登录按钮监听事件
            if (isUserNameAndPwdValid()) {
                String userName = mAccount.getText().toString().trim();    //获取当前输入的用户名和密码信息
                String userPwd = password.getText().toString().trim();
                SharedPreferences.Editor editor = login_sp.edit();
                int result = mUserDataManager.findUserByNameAndPwd(userName, userName);
                if (result == 1) {                                             //返回1说明用户名和密码均正确
                    //保存用户名和密码
                    editor.putString("USER_NAME", userName);
                    editor.putString("PASSWORD", userPwd);

//                //是否记住密码
//                if(mRememberCheck.isChecked()){
//                    editor.putBoolean("mRememberCheck", true);
//                }else{
//                    editor.putBoolean("mRememberCheck", false);
//                }
                    editor.commit();

                    Intent intent = new Intent(LoginActivity.this, GodActivity.class);    //切换Login Activity至User Activity
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();//登录成功提示
                } else if (result == 0) {
                    Toast.makeText(this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();  //登录失败提示
                }
            }
        } else {
            // TODO: 修改此处，不判断用户名和密码
            Intent intent = new Intent(LoginActivity.this, GodActivity.class);    //切换Login Activity至User Activity
            startActivity(intent);
            finish();
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();//登录成功提示
        }
    }

    public boolean isUserNameAndPwdValid() {
        if (mAccount.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.account_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.pwd_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mUserDataManager != null) {
            mUserDataManager.closeDataBase();
            mUserDataManager = null;
        }
        super.onPause();
    }
}
