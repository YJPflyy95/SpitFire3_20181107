package org.astri.spitfire.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.astri.spitfire.GodActivity;
import org.astri.spitfire.LoginActivity;
import org.astri.spitfire.R;
import org.astri.spitfire.entities.UserDataManager;
import org.astri.spitfire.util.UserData;

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
public class ChangePasswordFragment extends Fragment {

    @Nullable
    private EditText mAccount;
    private EditText mPwd_old;                            //密码编辑
    private EditText mPwd_new;                            //密码编辑
    private EditText mPwdCheck;                       //密码编辑
    private Button mChangeButton;                       //更换按钮
    private Button mBackButton;                     //取消按钮
    private UserDataManager mUserDataManager;         //用户数据管理类
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        mAccount = (EditText) view.findViewById(R.id.EmailAddress_et);
        mPwd_old = (EditText) view.findViewById(R.id.OldPassword_et);
        mPwd_old.setTypeface(Typeface.DEFAULT);
        mPwd_old.setTransformationMethod(new PasswordTransformationMethod());
        mPwd_new = (EditText) view.findViewById(R.id.NewPassword_et);
        mPwd_new.setTypeface(Typeface.DEFAULT);
        mPwd_new.setTransformationMethod(new PasswordTransformationMethod());
        mPwdCheck = (EditText) view.findViewById(R.id.ConfirmNewPasssword_et);
        mPwdCheck.setTypeface(Typeface.DEFAULT);
        mPwdCheck.setTransformationMethod(new PasswordTransformationMethod());

        mBackButton = (Button) view.findViewById(R.id.Back_bt);
//        mBackButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v) {
//
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.beginTransaction()
//                        .replace(R.id.ll_content,new MeFragment())
//                        .commit();
//            }
//        });
        mChangeButton = (Button) view.findViewById(R.id.Change_bt);
//        mChangeButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v) {
//
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.beginTransaction()
//                        .replace(R.id.ll_content,new MeFragment())
//                        .commit();
//            }
//        });
        mBackButton.setOnClickListener(m_resetpwd_Listener);      //注册界面两个按钮的监听事件
        mChangeButton.setOnClickListener(m_resetpwd_Listener);

        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(getContext());
            mUserDataManager.openDataBase();                              //建立本地数据库
        }
        return view;
    }
    View.OnClickListener m_resetpwd_Listener = new View.OnClickListener() {    //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Change_bt:                       //确认按钮的监听事件
                    resetpwd_check();
                    mChangeButton.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v) {

                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            fm.beginTransaction()
                                    .replace(R.id.ll_content,new MeFragment())
                                    .commit();
                            Toast.makeText(getActivity(), getString(R.string.resetpwd_success),Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case R.id.Back_bt:

                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            fm.beginTransaction()
                                    .replace(R.id.ll_content,new MeFragment())
                                    .commit();

//                    Intent intent_Resetpwd_to_Login = new Intent(getActivity(),LoginActivity.class) ;    //切换Resetpwd Activity至Login Activity
//                    startActivity(intent_Resetpwd_to_Login);
//                    finish();
                    break;
            }
        }
    };
    public void resetpwd_check() {                                //确认按钮的监听事件
        if (isUserNameAndPwdValid()) {
            String userName = mAccount.getText().toString().trim();
            String userPwd_old = mPwd_old.getText().toString().trim();
            String userPwd_new = mPwd_new.getText().toString().trim();
            String userPwdCheck = mPwdCheck.getText().toString().trim();
            int result=mUserDataManager.findUserByNameAndPwd(userName, userPwd_old);
            if(result==1){                                             //返回1说明用户名和密码均正确,继续后续操作
                if(userPwd_new.equals(userPwdCheck)==false){           //两次密码输入不一样
                    Toast.makeText(getContext(), getString(R.string.pwd_not_the_same),Toast.LENGTH_SHORT).show();
                    return ;
                } else {
                    UserData mUser = new UserData(userName, userPwd_new);
                    mUserDataManager.openDataBase();
                    boolean flag = mUserDataManager.updateUserData(mUser);
                    if (flag == false) {
                        Toast.makeText(getContext(), getString(R.string.resetpwd_fail),Toast.LENGTH_SHORT).show();
                    }else{
//                        mChangeButton.setOnClickListener(new View.OnClickListener(){
//                            public void onClick(View v) {
//
//                                FragmentManager fm = getActivity().getSupportFragmentManager();
//                                fm.beginTransaction()
//                                        .replace(R.id.ll_content,new MeFragment())
//                                        .commit();
//                            }
//                        });
                        Toast.makeText(getContext(), getString(R.string.resetpwd_success),Toast.LENGTH_SHORT).show();

                        mUser.pwdresetFlag=1;

//                        Intent intent_Register_to_Login = new Intent(getActivity(),GodActivity.class) ;    //切换User Activity至Login Activity
//                        startActivity(intent_Register_to_Login);

//                        finish();
                    }
                }
            }else if(result==0){                                       //返回0说明用户名和密码不匹配，重新输入
                Toast.makeText(getContext(), getString(R.string.pwd_not_fit_user),Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
    public boolean isUserNameAndPwdValid() {
        String userName = mAccount.getText().toString().trim();
        //检查用户是否存在
        int count=mUserDataManager.findUserByName(userName);
        //用户不存在时返回，给出提示文字
        if(count<=0){
            Toast.makeText(getContext(), getString(R.string.name_not_exist, userName),Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mAccount.getText().toString().trim().equals("")) {
            Toast.makeText(getContext(), getString(R.string.account_empty),Toast.LENGTH_SHORT).show();
            return false;
        } else if (mPwd_old.getText().toString().trim().equals("")) {
            Toast.makeText(getContext(), getString(R.string.pwd_empty),Toast.LENGTH_SHORT).show();
            return false;
        } else if (mPwd_new.getText().toString().trim().equals("")) {
            Toast.makeText(getContext(), getString(R.string.pwd_new_empty),Toast.LENGTH_SHORT).show();
            return false;
        }else if(mPwdCheck.getText().toString().trim().equals("")) {
            Toast.makeText(getContext(), getString(R.string.pwd_check_empty),Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
