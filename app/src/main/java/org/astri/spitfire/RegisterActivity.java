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
import android.widget.Toast;

import org.astri.spitfire.entities.User;
import org.astri.spitfire.entities.UserDataManager;
import org.astri.spitfire.fragment.HomeFragment;
import org.astri.spitfire.service.UserService;
import org.astri.spitfire.util.UserData;

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
public class RegisterActivity extends AppCompatActivity{

    private EditText mAccount;                        //用户名编辑
    private EditText confirmpasssword;               //密码编辑
    private EditText password;                       //密码编辑
    private Button mSureButton;                       //注册按钮
    private Button mLoginButton;                     //登录按钮
    private UserDataManager mUserDataManager;         //用户数据管理类

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Utils.init(getApplication());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        confirmpasssword = (EditText) findViewById(R.id.ConfirmPasssword_et);
        confirmpasssword.setTypeface(Typeface.DEFAULT);
        confirmpasssword.setTransformationMethod(new PasswordTransformationMethod());
        password = (EditText) findViewById(R.id.Password_et);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
//        Button login = (Button) findViewById(R.id.Login_bt);
//        Button Register = (Button) findViewById(R.id.Register_bt);
        mAccount = (EditText) findViewById(R.id.EmailAddress_et);
//        mPwd = (EditText) findViewById(R.id.Password_et);
//        mPwdCheck = (EditText) findViewById(R.id.resetpwd_edit_pwd_new);

        mSureButton = (Button) findViewById(R.id.Register_bt);
        mLoginButton = (Button) findViewById(R.id.Login_bt);

        mSureButton.setOnClickListener(m_register_Listener);//注册界面两个按钮的监听事件
        mLoginButton.setOnClickListener(m_register_Listener);
        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();                              //建立本地数据库
        }
//        login.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
//                startActivity(intent);
//            }
//        });
//        mSureButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(RegisterActivity.this,GodActivity.class);
//                startActivity(intent);
//            }
//        });
    }
    View.OnClickListener m_register_Listener = new View.OnClickListener() {    //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Register_bt:                       //确认按钮的监听事件
                    register_check();
                    break;
                case R.id.Login_bt:                     //取消按钮的监听事件,由注册界面返回登录界面
//                    Intent intent = new Intent(RegisterActivity.this,LoginActivity.class) ;    //切换Register Activity至Login Activity
                    Intent intent = new Intent(RegisterActivity.this,HeartRate.class) ;    //切换Register Activity至Login Activity
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
    public void register_check() {                                //确认按钮的监听事件
        if (isUserNameAndPwdValid()) {
            String userName = mAccount.getText().toString().trim();
            String userPwd = password.getText().toString().trim();
            String userPwdCheck = confirmpasssword.getText().toString().trim();


            //guodongjia
//            User user = new User();
//            user.setUserName(userName);
//            user.setPwd(userPwd);
//
//            UserService.save(user);

            //检查用户是否存在
            int count=mUserDataManager.findUserByName(userName);
            //用户已经存在时返回，给出提示文字
            if(count>0){
                Toast.makeText(this, getString(R.string.name_already_exist, userName),Toast.LENGTH_SHORT).show();
                return ;
            }
            if(userPwd.equals(userPwdCheck)==false){     //两次密码输入不一样
                Toast.makeText(this, getString(R.string.pwd_not_the_same),Toast.LENGTH_SHORT).show();
                return ;
            } else {
                UserData mUser = new UserData(userName, userName);
                mUserDataManager.openDataBase();
                long flag = mUserDataManager.insertUserData(mUser); //新建用户信息
                if (flag == -1) {
                    Toast.makeText(this, getString(R.string.register_fail),Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, getString(R.string.register_success),Toast.LENGTH_SHORT).show();
                    Intent intent_Register_to_Login = new Intent(RegisterActivity.this,GodActivity.class) ;    //切换Register Activity至Login Activity
                    startActivity(intent_Register_to_Login);
                    finish();
                }
            }
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
        }else if(confirmpasssword.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.pwd_check_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
