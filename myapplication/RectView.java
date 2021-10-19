package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Myy on 2016/8/27.
 */
public class RectView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener{

    boolean canDraw = false;

    //移动过程的xy,imageView的canvas的宽高，点击事件的宽高,源图片的宽高。
    private float moveX,moveY,startWidth,startHeight,downX,downY,bitmapWidth,bitmapHeight;
    private Context context;
    //当前手机的像素密度
    private float density;
    //是否可以开始拖拽选择框
    private boolean isStart=true;
    //分别表示上线，下线，左线，右线
    private Line upLine,downLine,leftLine,rightLine;
    //是否是上线移动，是否是垂直移动，是否是左线移动，是否是水平移动
    //垂直移动分为上线移动和下线移动，垂直移动同理
    private boolean isMovingUpLine=false,isMovingVertical=false,isMovingLeftLine=false,isMovingHorizontal=false;
    //用来给点击点的坐标预留一些空间，便于判断移动时间的判断
    private float padding;
    private Paint paint = new Paint();
    public RectView(Context context) {
        super(context);
        init(context);
    }

    public RectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    private void init(Context context)
    {
        this.context=context;
        density = 1;
        //默认事件产生的波动距离是20个像素点
        padding = density * 180;
        //监听动作，产生移动事件
        this.setOnTouchListener(this);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isStart && moveY == 0) {
            //初次布局，绘画四周的边线
            initCanvas(canvas);
        } else {
            //重画边线和遮罩
            if(canDraw)
                reDraw(canvas);
            else
                initCanvas(canvas);
        }
    }

    /**
     * 重新绘制矩形区域
     * @param canvas
     */
    private void reDraw(Canvas canvas) {
        isStart=false;
        paint.setStrokeWidth(4);
        //style有三种模式fill,stroke,fill_and_stroke三种。
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);
        //画出上线和下线左线右线
        //画上线的时候，应该以左线作为x的起点的参考，以右线作为x的终点的参考。其余线都需要参考其他线，逻辑不列了
        canvas.drawLine(leftLine.getLeft(),upLine.getTop(),rightLine.getLeft(),upLine.getBottom(),paint);//上线
        canvas.drawLine(leftLine.getLeft(),downLine.getTop(),rightLine.getRight(),downLine.getBottom(),paint);//下线
        canvas.drawLine(leftLine.getLeft(),upLine.getTop(),leftLine.getRight(),downLine.getBottom(),paint);//左线
        canvas.drawLine(rightLine.getLeft(),upLine.getTop(),rightLine.getRight(),downLine.getBottom(),paint);//右线
        //画出遮罩
        paint.setColor(Color.BLACK);
        //设置透明度，以至于下边的内容不会完全被遮住导致不可见。
        paint.setAlpha(180);
        canvas.drawRect(0,0,upLine.getRight(),upLine.getBottom()-1,paint);//上边遮罩
        canvas.drawRect(downLine.getLeft(),downLine.getTop()+1,startWidth,startHeight,paint);//下边遮罩
        //画左右遮罩需要照顾到上下遮罩本
        //3为的是消除遮罩之间的缝隙
        canvas.drawRect(0,upLine.getTop()+3,leftLine.getRight()-1,downLine.getTop()-3,paint);//左边遮罩
        canvas.drawRect(rightLine.getLeft()+1,upLine.getTop()+3,startWidth,downLine.getTop()-3,paint);//右边遮罩
        Log.i("上线坐标",upLine.getLeft()+" "+upLine.getTop()+" "+upLine.getRight()+" "+upLine.getBottom() );
        Log.i("下线坐标",downLine.getLeft()+" "+downLine.getTop()+" "+downLine.getRight()+" "+downLine.getBottom() );
        Log.i("左线坐标",leftLine.getLeft()+" "+leftLine.getTop()+" "+leftLine.getRight()+" "+leftLine.getBottom());
        Log.i("右线坐标",rightLine.getLeft()+" "+rightLine.getTop()+" "+rightLine.getRight()+" "+rightLine.getBottom());
    }

    /**
     * 初始化布局
     * @param canvas
     */
    public void initCanvas(Canvas canvas) {
        //获取画布的宽高，用于初始化边线的距离
        startWidth = canvas.getWidth();
        startHeight = canvas.getHeight();
        upLine=new Line(0,0,startWidth,0);
        downLine=new Line(0, startHeight, startWidth, startHeight);
        leftLine=new Line(0,0,0,startHeight);
        rightLine=new Line(startWidth,0,startWidth,startHeight);
        Log.i("上线坐标",upLine.getLeft()+" "+upLine.getTop()+" "+upLine.getRight()+" "+upLine.getBottom() );
        Log.i("下线坐标",downLine.getLeft()+" "+downLine.getTop()+" "+downLine.getRight()+" "+downLine.getBottom() );
        Log.i("左线坐标",leftLine.getLeft()+" "+leftLine.getTop()+" "+leftLine.getRight()+" "+leftLine.getBottom());
        Log.i("右线坐标",rightLine.getLeft()+" "+rightLine.getTop()+" "+rightLine.getRight()+" "+rightLine.getBottom());
        //画出上下左右两条线
        paint.setStrokeWidth(4);
        paint.setColor(Color.WHITE);
        //0-255，越小越透明
        paint.setAlpha(255);
        //画边线
        canvas.drawLine(0, 0, startWidth, 0, paint);
        canvas.drawLine(0, startHeight, startWidth, startHeight, paint);
        canvas.drawLine(0,0,0,startHeight,paint);
        canvas.drawLine(startWidth,0,startWidth,startHeight,paint);
    }



    /**
     * 监听触摸事件，这里的逻辑是最复杂的
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(canDraw) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //记录当前点击的坐标，用于判断用户当前移动的是那条线以及用于产生移动距离
                downX = event.getX();
                downY = event.getY();
                //说明当前点击的是上线，波动距离为上线上下20个像素点的范围
                if ((downY <= upLine.getTop() + padding) && (downY >= upLine.getTop() - padding)) {
                    isMovingUpLine = true;//表示当前是上线被用户拖拽
                    isMovingVertical = true;//表示是垂直方向的移动
                    isMovingHorizontal = false;//表示不是水平方向的移动
                } else if ((downY <= downLine.getTop() + padding) && (downY >= downLine.getTop() - padding)) {
                    isMovingUpLine = false;
                    isMovingVertical = true;
                    isMovingHorizontal = false;
                } else if ((downX <= leftLine.getLeft() + padding) && (downX >= leftLine.getLeft() - padding)) {
                    isMovingLeftLine = true;
                    isMovingHorizontal = true;
                    isMovingVertical = false;
                } else if ((downX <= rightLine.getLeft() + padding) && (downX >= rightLine.getLeft() - padding)) {
                    isMovingLeftLine = false;
                    isMovingHorizontal = true;
                    isMovingVertical = false;
                } else {
                    //如果不在任何一条边线的拖动范围，对用户此次触摸事件忽略
                    isMovingHorizontal = false;
                    isMovingVertical = false;
                }
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                //产生移动距离，用于重新绘制边线和遮罩
                moveX = event.getX() - downX;
                moveY = event.getY() - downY;
                //必须保存这次的移动坐标，以便下一次移动的时候可以判断从上一次的移动坐标产生新的移动的距离
                downX = event.getX();
                downY = event.getY();
                if (isMovingUpLine && isMovingVertical) {
                    float detY = upLine.getTop() + moveY;
                    //当移动的距离超过下线20个像素范围或到达顶端的时候，需要进行特殊的处理。
                    //否则就使用移动的距离。
                    //上线必须处于下线上边的20个像素点以上
                    if (detY >= downLine.getTop() - padding)
                        detY = downLine.getTop() - padding;
                    //上线不可超过顶端
                    if (detY <= 0)
                        detY = 0;
                    upLine.setTop(detY);
                    upLine.setBottom(detY);
                }
                if (!isMovingUpLine && isMovingVertical) {
                    //下线的处理
                    float detY = downLine.getTop() + moveY;
                    if (detY <= upLine.getTop() + padding)//和上线保持20的像素点
                        detY = upLine.getTop() + padding;
                    if (detY >= startHeight)//不可超过边线
                        detY = startHeight;
                    downLine.setTop(detY);
                    downLine.setBottom(detY);
                }
                if (isMovingLeftLine && isMovingHorizontal) {
                    //左线的处理
                    float detX = leftLine.getLeft() + moveX;
                    if (detX >= rightLine.getLeft() - padding)
                        detX = rightLine.getLeft() - padding;
                    if (detX <= 0)
                        detX = 0;
                    leftLine.setLeft(detX);
                    leftLine.setRight(detX);
                }
                if (!isMovingLeftLine && isMovingHorizontal) {
                    //右线的处理
                    float detX = rightLine.getLeft() + moveX;
                    if (detX <= leftLine.getLeft() + padding)//和上线保持20的像素点
                        detX = leftLine.getLeft() + padding;
                    if (detX >= startWidth)//不可超过边线
                        detX = startWidth;
                    rightLine.setLeft(detX);
                    rightLine.setRight(detX);
                }
                if (isMovingVertical || isMovingHorizontal)
                    invalidate();
                Log.i("moveY", moveY + "");
                Log.i("isMovingVertical", isMovingVertical + "");
                Log.i("isMovingHorizontal", isMovingHorizontal + "");
                Log.i("上线", isMovingUpLine + "");
                Log.i("moveX", moveX + "");
                Log.i("左线", isMovingLeftLine + "");
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {

                return true;
            }
        }
        return false;
    }

    /**
     * 获取截图的范围
     * @return
     */
    public Line getClipLine(ImageView imageView)
    {
        Bitmap bitmap=((ItemDetailActivity)context).getTempBitmap();
        bitmapWidth=bitmap.getWidth();
        bitmapHeight=bitmap.getHeight();

        //计算当前显示的图片和原始图片的缩放比例
        float xRatio=bitmapWidth/(startWidth-getPaddingRight()-getPaddingLeft());
        float yRatio=bitmapHeight/(startHeight-getPaddingTop()-getPaddingBottom());
        //判断截图的XY坐标，如果小于0，则认为截取的图片XY是从0开始
        float clipX=(leftLine.getLeft()-getPaddingLeft())<=0?0:leftLine.getLeft()-getPaddingLeft();
        float clipY=(upLine.getTop()-getPaddingTop())<=0?0:upLine.getTop()-getPaddingTop();
        //获得截取的图片XY转换成原始图片的XY坐标
        float x=clipX*xRatio;
        float y=clipY*yRatio;
        //获得截取图片的宽高
        float clipWidth=(rightLine.getRight()-(startWidth-getPaddingRight())>=0?startWidth-getPaddingRight()-getPaddingLeft():rightLine.getRight())-clipX;
        float clipHeight=(downLine.getBottom()-(startHeight-getPaddingBottom())>=0?startHeight-getPaddingBottom()-getPaddingTop():downLine.getBottom())-clipY;
        //准换成原始图片的宽高
        float width=clipWidth*xRatio;
        float height=clipHeight*yRatio;

        Line line=new Line(x,y,width,height);
        return line;
    }

    public void setCanDraw(boolean canDraw){
        this.canDraw = canDraw;
    }

    public boolean getCanDraw(){
        return this.canDraw;
    }
}
