package org.astri.spitfire;

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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.astri.spitfire.ble.activity.BLEMainActivity;
import org.astri.spitfire.component.CircleImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * <pre>
 *     author : ghf
 *     e-mail : 869862783@qq.com
 *     time   : 2018/1/31
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MeActivity extends AppCompatActivity {
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initView();
        final Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
        dateDisplay = (TextView) findViewById(R.id.dateDisplay);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textView = (TextView) findViewById(R.id.result);
        imgBoy = (ImageView)this.findViewById(R.id.boy_ib);
        TextView changepassword = (TextView) findViewById(R.id.ChangePassword_tv);
        changepassword.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
        ImageView forwardchangepassword = (ImageView) findViewById(R.id.forwardChangePassword_ib);
        forwardchangepassword.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
        TextView languages = (TextView) findViewById(R.id.Languages_tv);
        languages.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,LanguagesActivity.class);
                startActivity(intent);
            }
        });
        ImageView forwardlanguages = (ImageView) findViewById(R.id.forwardLanguages_ib);
        forwardlanguages.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,LanguagesActivity.class);
                startActivity(intent);
            }
        });
        TextView connecteddevice = (TextView) findViewById(R.id.ConnectedDevice_tv);
        connecteddevice.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,BLEMainActivity.class);
                startActivity(intent);
            }
        });
        ImageView forwardconnecteddevice = (ImageView) findViewById(R.id.forwardConnectedDevice_ib);
        forwardconnecteddevice.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,BLEMainActivity.class);
                startActivity(intent);
            }
        });
        ImageView Home_iv = (ImageView) findViewById(R.id.Homeiv);
        Home_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        TextView Home_tv = (TextView) findViewById(R.id.Hometv);
        Home_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        ImageView Activities_iv = (ImageView) findViewById(R.id.Activitiesiv);
        Activities_iv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,Activities.class);
                startActivity(intent);
            }
        });
        TextView Activities_tv = (TextView) findViewById(R.id.Activitiestv);
        Activities_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,Activities.class);
                startActivity(intent);
            }
        });
        ImageView historyBt = findViewById(R.id.Historyiv);
        historyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeActivity.this,HistoryActivity.class);
                startActivity(intent);
            }});
        TextView History_tv = (TextView) findViewById(R.id.Historytv);
        History_tv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(MeActivity.this,HistoryActivity.class);
                startActivity(intent);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("  Morning Readiness Duration   "+progress+" mins");
                Log.d("debug", String.valueOf(seekBar.getId()));
            }
            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MeActivity.this,"按住seekbar", Toast.LENGTH_SHORT).show();
            }
            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MeActivity.this,"放开seekbar", Toast.LENGTH_SHORT).show();
            }
        });
        imgBoy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.boy_ib){
                    imgBoy.setImageDrawable(getResources().getDrawable(image[1]));
            }
        }});
        imgGirl = (ImageView)this.findViewById(R.id.girl_ib);
        imgGirl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.girl_ib){
                    imgGirl.setImageDrawable(getResources().getDrawable(image[3]));
                }
            }});
        dateDisplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG:
                return new DatePickerDialog(this, mdateListener, mYear, mMonth, mDay);
        }
        return null;
    }

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
    private void initView() {

        headIcon = (CircleImageView) findViewById(R.id.headIcon);
        headIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                changeHeadIcon();
            }
        });
        changeTheme();
        File file = new File(MeActivity.this.getFilesDir(), "_head_icon.jpg");
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
        AlertDialog dlg = new AlertDialog.Builder(MeActivity.this).setTitle("选择图片").setItems(items, new DialogInterface.OnClickListener() {
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
                        Toast.makeText(MeActivity.this, "未找到存储卡，无法存储照片！",
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
                Toast.makeText(MeActivity.this, "未找到存储卡，无法存储照片！",
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
                    outputStream = MeActivity.this.openFileOutput("_head_icon.jpg", Context.MODE_PRIVATE);
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

}
