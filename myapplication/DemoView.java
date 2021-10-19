package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class DemoView extends androidx.appcompat.widget.AppCompatImageView {

    private Paint mPaint;
    private Path mPath;
    private boolean canDraw = false;
    private boolean drawEnd = false;
    private int count = 0;
    private ItemDetailActivity currentActivity;

    public DemoView(Context context) {
        super(context);
        init();
    }

    public DemoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DemoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCurrentActivity(ItemDetailActivity current){
        currentActivity = current;
    }

    public void setCanDraw(boolean can){
        canDraw = can;
    }

    public void resetPath(){
        mPath.reset();
        count = 0;
        if(canDraw)
            Toast.makeText(currentActivity, "取消绘制", Toast.LENGTH_LONG).show();
        canDraw = false;
        postInvalidate();
    }

    private void init() {
        //初始化画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(50);
        mPaint.setAntiAlias(true);

        //初始化Path。
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制路径
        if(drawEnd) {
            canvas.clipPath(mPath);
            canvas.drawBitmap(currentActivity.getTempBitmap(), 0, 0, mPaint);
            canvas.save();
            canvas.restore();
            Toast.makeText(currentActivity, "绘制结束", Toast.LENGTH_LONG).show();
            drawEnd = false;
        }
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(canDraw) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //按下时设置Path起点
                    if(count == 0){
                        mPath.reset();
                        mPath.moveTo(event.getX(), event.getY());
                        count += 2;
                    }
                    else if (count >= 5) {
                        mPath.lineTo(event.getX(), event.getY());
                        mPath.close();
                        canDraw = false;
                        drawEnd = true;
                        count = 0;

                    } else {
                        mPath.lineTo(event.getX(), event.getY());
                        count += 2;
                    }
                    postInvalidate();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }
}
