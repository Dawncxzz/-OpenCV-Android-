package com.example.myapplication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class ItemDetailActivity extends AppCompatActivity{

    private int ImageHeight;

    private String filePath;
    private Bitmap tempBitmap;
    private Line line;

    private Path path = new Path();
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView imageView = (ImageView) findViewById(R.id.Image);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                Log.e("高度",":" + imageView.getHeight());
                ImageHeight = imageView.getHeight();
            }
        });


        Button button = (Button)findViewById(R.id.getImage);
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
                intent.setType("image/*");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setAction(Intent.ACTION_GET_CONTENT);
                /* 取得相片后返回本画面 */
                startActivityForResult(intent, 1);
            }
        });
        Button button1 = (Button)findViewById(R.id.photo);
        button1.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                String SDState = Environment.getExternalStorageState();
                if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                    if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT){
                        requestPermissions(new String[]{
                                "android.permission.CAMERA",
                                "android.permission.READ_EXTERNAL_STORAGE",
                                "android.permission.WRITE_EXTERNAL_STORAGE"
                        },102);
                    }
                    // 给拍摄的照片指定存储位置
                    String f = System.currentTimeMillis()+".jpg"; // 指定名字
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), f); // 指定文件
                    filePath = file.getAbsolutePath();
                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri fileUri = FileProvider.getUriForFile(ItemDetailActivity.this, getPackageName() + ".fileProvider", file); // 路径转换
                        //拍照结果输出到这个uri对应的file中
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); //指定图片存放位置，指定后，在onActivityResult里得到的Data将为null
                        //对这个uri进行授权
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        //拍照结果输出到这个uri对应的file中
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    }
                    startActivityForResult(intent, 102);
                }else {
                    Toast.makeText(ItemDetailActivity.this, "内存卡不存在", Toast.LENGTH_LONG).show();
                }
            }
        });
        Button button2 = (Button)findViewById(R.id.draw);
        button2.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                RectView imageView = (RectView) findViewById(R.id.Image);
                if(tempBitmap == null) {
                    imageView.setDrawingCacheEnabled(true);
                    tempBitmap = imageView.getDrawingCache();
                }
                if(imageView.getCanDraw()){
                    imageView.setCanDraw(false);
                    sure(imageView);
                    imageView.setDrawingCacheEnabled(false);
                    Toast.makeText(ItemDetailActivity.this, "裁剪结束", Toast.LENGTH_LONG).show();
                }else{
                    imageView.setCanDraw(true);
                    Toast.makeText(ItemDetailActivity.this, "开始裁剪（拖拽图片边缘进行移动）", Toast.LENGTH_LONG).show();
                }
            }
        });
        Button button3 = (Button)findViewById(R.id.calculate);
        button3.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                ImageView imageView = (ImageView) findViewById(R.id.Image);
                if(tempBitmap == null) {
                    imageView.setDrawingCacheEnabled(true);
                    tempBitmap = imageView.getDrawingCache();
                }
                Results result = OpenCV.houghLines(tempBitmap);

                imageView.setImageBitmap(result.getRotateBitmap());

                double Brightness = result.getResult() * 100;

                EditText slope=(EditText)findViewById(R.id.slope);
                EditText intercept=(EditText)findViewById(R.id.intercept);
                if(slope.getText().toString() == "" || slope.getText().toString() == null){
                    slope.setText("1");
                }
                if(slope.getText().toString() == "" || slope.getText().toString() == null){
                    intercept.setText("1");
                }

                double Trombin = Double.valueOf(slope.getText().toString()) * Brightness + Double.valueOf(intercept.getText().toString());

                TextView brightness = (TextView)findViewById(R.id.brightness);
                TextView trombin = (TextView)findViewById(R.id.thrombin);
                brightness.setText(String.valueOf(Brightness * 0.01));
                trombin.setText(String.valueOf(Trombin));
            }
        });
    }

    public void sure(View view) {
        RectView imageView = (RectView) findViewById(R.id.Image);
        line = imageView.getClipLine(imageView);
        tempBitmap = tempBitmap.createBitmap(tempBitmap, (int)line.getLeft(),(int)line.getTop(),(int)line.getRight(),(int)line.getBottom());//截取图片
        tempBitmap = changeBitmapSize(tempBitmap);
        imageView.setImageBitmap(tempBitmap);
        setResult(RESULT_OK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView imageView = (ImageView) findViewById(R.id.Image);
        ContentResolver cr = this.getContentResolver();
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(ItemDetailActivity.this, "取消", Toast.LENGTH_SHORT).show();
            return;
        }
        if (resultCode == RESULT_OK){
            switch (requestCode) {
                case 1:
                    try {
                        Uri uri = data.getData();
                        Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                        bitmap = changeBitmapSize(bitmap);
                        tempBitmap = bitmap;
                        imageView.setBackgroundResource(0);
                        imageView.setImageBitmap(bitmap);
//                        imageView.resetPath();
                    } catch (FileNotFoundException e) {
                        Log.e("Exception", e.getMessage(), e);
                    }
                    break;
                case 102:
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    bitmap = changeBitmapSize(bitmap);
                    tempBitmap = bitmap;
                    imageView.setBackgroundResource(0);
                    imageView.setImageBitmap(bitmap);
//                    imageView.resetPath();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filePath", filePath);
        Log.d("TakePhoto", "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (TextUtils.isEmpty(filePath)) {
            filePath = savedInstanceState.getString("filePath");
        }
        Log.d("TakePhoto", "onRestoreInstanceState");
    }

    private Bitmap changeBitmapSize(Bitmap bitmap) {
        Bitmap newbm = null;
        if (bitmap!=null){
            // 获得图片的宽高
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            // 计算缩放比例
            float scale = (float)width / height;
            float scaleWidth = ((float) ImageHeight * scale) / width;
            float scaleHeight = ((float) ImageHeight) / height;
            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 得到新的图片
            newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        return newbm;
    }

    public Bitmap getTempBitmap(){
        return tempBitmap;
    }
}
