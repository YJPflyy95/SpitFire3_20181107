package org.astri.spitfire.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import org.astri.spitfire.ChangePasswordActivity;
import org.astri.spitfire.LanguagesActivity;
import org.astri.spitfire.MeActivity;
import org.astri.spitfire.R;
import org.astri.spitfire.ble.Fragment.BLEMainFragment;
import org.astri.spitfire.ble.Fragment.DeviceControlFragment;
import org.astri.spitfire.ble.activity.BLEMainActivity;
import org.astri.spitfire.component.CircleImageView;
import org.astri.spitfire.util.Constants;
import org.astri.spitfire.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
public class MeFragment extends Fragment {

    private static final String TAG = "MeFragment";

    @Nullable

    private static final int PHOTO_REQUEST_CAREMA = 1;
    private static final int PHOTO_REQUEST_GALLERY = 2;
    private static final int PHOTO_REQUEST_CUT = 3;
    private static final String PHOTO_FILE_NAME = "my.png";
    private File tempFile;
    private CircleImageView headIcon;
    int mYear, mMonth, mDay;
    TextView dateDisplay;
    final int DATE_DIALOG = 1;
    private ImageView imgBoy;
    private ImageView imgGirl;
    private SeekBar seekBar;
    private TextView textView;
    int[] image = {
            R.drawable.boy,
            R.drawable.boyfilled,
            R.drawable.girl,
            R.drawable.girlfilled
    };



    private Fragment changePasswordFragment = new ChangePasswordFragment();
    private Fragment languagesFragment = new LanguagesFragment();
    private Fragment deviceControlFragment = new DeviceControlFragment();

    private Fragment settingFragment = new SettingsFragment();



    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        initView(view);
        int flag = 0;
        final Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
        dateDisplay = (TextView) view.findViewById(R.id.dateDisplay);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        textView = (TextView) view.findViewById(R.id.result);

        TextView changepassword = (TextView) view.findViewById(R.id.ChangePassword_tv);
        changepassword.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
//                switchContent(changePasswordFragment);
                replaceFragment(changePasswordFragment);
            }
        });
        ImageView forwardchangepassword = (ImageView) view.findViewById(R.id.forwardChangePassword_ib);
        forwardchangepassword.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
//                switchContent(changePasswordFragment);
                replaceFragment(changePasswordFragment);
            }
        });
        TextView languages = (TextView) view.findViewById(R.id.Languages_tv);
        languages.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
//                switchContent(languagesFragment);
                replaceFragment(languagesFragment);
            }
        });
        ImageView forwardlanguages = (ImageView) view.findViewById(R.id.forwardLanguages_ib);
        forwardlanguages.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
//                switchContent(languagesFragment);
                replaceFragment(languagesFragment);
            }
        });
        TextView connecteddevice = (TextView) view.findViewById(R.id.ConnectedDevice_tv);
        connecteddevice.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
//                switchContent(deviceControlFragment);
                replaceFragment(deviceControlFragment);
            }
        });
        ImageView forwardconnecteddevice = (ImageView) view.findViewById(R.id.forwardConnectedDevice_ib);
        forwardconnecteddevice.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
//                switchContent(deviceControlFragment);
                replaceFragment(deviceControlFragment);
            }
        });


        // 设置： 算法
        TextView settings = view.findViewById(R.id.forwardSetting_tv);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(settingFragment);
            }
        });

//        ImageView forwardSettings = view.findViewById(R.id.forwardSetting_ib);
//        forwardSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                replaceFragment(settingFragment);
//            }
//        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(" Morning Readiness Duration "+progress+" mins");
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
        imgBoy = (ImageView)view.findViewById(R.id.boy_ib);
        imgBoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.boy_ib){
                    imgBoy.setImageDrawable(getResources().getDrawable(image[1]));
                    imgGirl.setImageDrawable(getResources().getDrawable(image[2]));
                }
            }});
        imgGirl = (ImageView)view.findViewById(R.id.girl_ib);
        imgGirl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.girl_ib){
                    imgGirl.setImageDrawable(getResources().getDrawable(image[3]));
                    imgBoy.setImageDrawable(getResources().getDrawable(image[0]));
                }

            }});

        dateDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), onSetDOBListener, mYear,mMonth,mDay).show();
            }
        });
        return view;
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ll_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



    /**
     * choose DOB(date of birth).
     */
    private DatePickerDialog.OnDateSetListener onSetDOBListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            String date = new StringBuffer().append(mMonth + 1).append("-").append(mDay).append("-").append(mYear).append(" ").toString();
            dateDisplay.setText(date);
        }
    };

    /**
     * 设置日期 利用StringBuffer追加
     */
    public void display() {
        dateDisplay.setText(new StringBuffer().append(mMonth + 1).append("-").append(mDay).append("-").append(mYear).append(" "));
    }

    private DatePickerDialog.OnDateSetListener mdateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            display();
        }
    };
    private void initView(View view) {

        headIcon = (CircleImageView) view.findViewById(R.id.headIcon);
        headIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                changeHeadIcon();
            }
        });
        changeTheme();
        File file = new File(getContext().getFilesDir(), "_head_icon.jpg");
        if (file.exists()) {
            headIcon.setImageURI(Uri.fromFile(file));
        }
    }
    private void changeTheme() {
        Calendar c = Calendar.getInstance();
        System.out.println(c.get(Calendar.HOUR_OF_DAY));
        if (c.get(Calendar.HOUR_OF_DAY) < 18 && c.get(Calendar.HOUR_OF_DAY) >= 6) {
            headIcon.setImageResource(R.drawable.live);
        } else {
            headIcon.setImageResource(R.drawable.my);
        }
    }
    private void changeHeadIcon() {
        final CharSequence[] items = { "相册", "拍照" };
        AlertDialog dlg = new AlertDialog.Builder(getContext()).setTitle("选择图片").setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // 这里item是根据选择的方式，
                if (item == 0) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent,
                            PHOTO_REQUEST_GALLERY);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        tempFile = new File(Environment.getExternalStorageDirectory(),
                                PHOTO_FILE_NAME);
                        Uri uri = Uri.fromFile(tempFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent,PHOTO_REQUEST_CAREMA);
                    } else {
                        Toast.makeText(getContext(), "未找到存储卡，无法存储照片！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).create();
        dlg.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            if (data != null) {
                Uri uri = data.getData();
                Log.e("图片路径？", data.getData() + "");
                crop(uri);
            }

        } else if (requestCode == PHOTO_REQUEST_CAREMA) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                crop(Uri.fromFile(tempFile));
            } else {
                Toast.makeText(getContext(), "未找到存储卡，无法存储照片！",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == PHOTO_REQUEST_CUT) {
            if (data != null) {
                final Bitmap bitmap = data.getParcelableExtra("data");
                headIcon.setImageBitmap(bitmap);
                // 保存图片到internal storage
                FileOutputStream outputStream;
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    // out.close();
                    // final byte[] buffer = out.toByteArray();
                    // outputStream.write(buffer);
                    outputStream = getActivity().openFileOutput("_head_icon.jpg", Context.MODE_PRIVATE);
                    out.writeTo(outputStream);
                    out.close();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (tempFile != null && tempFile.exists())
                    tempFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void crop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    /**
     * 切换内容
     * @param to
     */
    public void switchContent(Fragment to) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        LogUtil.d(TAG, "isadd: "+to.isAdded());

        if (!to.isAdded()) {	// 先判断是否被add过
            transaction.add(R.id.ll_content, to).show(to).commit(); // 隐藏当前的fragment，add下一个到Activity中
        } else {
            transaction.show(to).commit(); // 隐藏当前的fragment，显示下一个
        }
    }

    /**
     * 隐藏所有fragment防止显示多个
     */
    private void hideFragments(FragmentTransaction transaction) {

        LogUtil.d(TAG, "hideFragments");


        if (changePasswordFragment != null) {
            transaction.hide(changePasswordFragment);
        }
        if (languagesFragment != null) {
            transaction.hide(languagesFragment);
        }
        if (deviceControlFragment != null) {
            transaction.hide(deviceControlFragment);
        }
        if(this !=null){
            transaction.hide(this);
        }

    }

}
