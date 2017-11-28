package com.zhuoyi.progressbuttondemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by wubo on 2017/11/27.
 * 全新开发的下载进度按钮控件
 */

public class DownLoadProgressButton extends View {
    /**
     *  下载中的几种状态文字
     * */
    private final String FREE_STATE_STR ="下载";
    private final String DOWNLOADING_STATE_STR ="暂停";
    private final String PAUSE_STATE_STR ="继续";
    private final String WAITTING_STATE_STR ="等待中";
    private final String DOWNLOAD_COMPLETE_STR ="安装";

    /**
     *  下载中状态
     */
    public static final int FREE_STATE=0;
    public static final int DOWNLOADING_STATE=1;
    public static final int PAUSE_STATE =2;
    public static final int WAITTING_STATE=3;
    public static final int DOWNLOAD_COMPLETE=4;
    private RectF mDrawBgRectF;

    private Canvas mProgressCanvas;
    private Bitmap mProgressBitmap;


    /**
     *  textSize类型是sp还是px
     */
    public enum TextSizeType{
        SP,PX
    }

    /**
     *  默认text size大小是sp
     */
    private final int DEFAULT_TEXT_SIZE=14;

    private int mMeasureWidth;
    private int mMeasureHeight;


    private float mTextSize;
    /**
     * 当前状态的text文字
     * */
    private String mStateTextCurrent;
    private Context mContext;

    /**
     *  控件默认大小
     * */
    private int mDefaultWidth;
    private int mDefaultHeight;

    /**
     *  默认画笔
     * */
    private Paint mPaint=new Paint();

    private Rect mTextBounds = new Rect();

    private Path mPath=new Path();
    /**
     * 控件边框以及进度条的颜色
     */
    private int mProgressColor;

    /**
     *  当前下载进度
     * */
    private float mProgressCurrent;
    /**
     *  总下载进度
     * */
    private float mProgressMax;



    /**
     *  定义按钮圆角的弧度
     */
    private int mCornerRadius;

    private int mBorderWidth;

    public DownLoadProgressButton(Context context) {
        this(context,null);
    }
    /**
     *  一般我们在布局中设置的控件会调用这个构造, attrs可以拿到我们设置的自定义attrs属性
     */
    public DownLoadProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public DownLoadProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initParams();
    }

    /**
     *  初始化默认参数
     * */
    private void initParams() {
        mTextSize =sp2Px(DEFAULT_TEXT_SIZE);
        mBorderWidth=dp2Px(1f);
        //默认空闲状态
        mStateTextCurrent= FREE_STATE_STR;
        //初始化圆角半径
        mCornerRadius= dp2Px(10);
        //默认进度条颜色
        mProgressColor= Color.parseColor("#f87908");
        initDefaultMeasureWidth();
        initPaint();
        //默认最大下载进度是100
        mProgressMax=100;
        //初始化进度条Bitmap
    }

    private void initPaint() {
        mPaint.setAntiAlias(true);      //设置画笔是否抗锯齿（边缘柔化、消除混叠）
        mPaint.setStrokeJoin(Join.ROUND);
    }

    private void initDefaultMeasureWidth() {
        mTextBounds.setEmpty();
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(WAITTING_STATE_STR,0, WAITTING_STATE_STR.length(),mTextBounds);

        int textWidth = mTextBounds.width();
        int textHeight = mTextBounds.height();

        // 设置默认控件大小，默认宽高为字体宽高二倍

        mDefaultWidth=textWidth*2;
        mDefaultHeight=textHeight*2;
    }


    private float sp2Px(float sp){
       return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, mContext.getResources().getDisplayMetrics());
    }

    private int dp2Px(float dp){
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, mContext.getResources().getDisplayMetrics())+0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);

        if(widthMode==MeasureSpec.EXACTLY){
            mMeasureWidth=widthSize;
        }else{
            //非滚动这个种控件一般都是AT_MOST
            mMeasureWidth=Math.min(mDefaultWidth,widthSize);
        }

        if(heightMode==MeasureSpec.EXACTLY){
            mMeasureHeight=heightSize;
        }else{
            mMeasureHeight=Math.min(mDefaultHeight,heightSize);
        }
        mDrawBgRectF = new RectF(mBorderWidth, mBorderWidth, mMeasureWidth - mBorderWidth, mMeasureHeight - mBorderWidth);

        mProgressBitmap = Bitmap.createBitmap(mMeasureWidth - mBorderWidth, mMeasureHeight - mBorderWidth, Config.ARGB_4444);

        mProgressCanvas = new Canvas(mProgressBitmap);
        setMeasuredDimension(mMeasureWidth,mMeasureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       //  1.绘制外围边框
        drawBorder(canvas);
        // 2.绘制进度条
        drawProgress(canvas);
        // 3.默认时候文本显示
        drawText(canvas);
        // 4.绘制进度条遮罩文本显示时
        drawProgressShadeText(canvas);
    }

    private void drawProgressShadeText(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        int tWidth =mTextBounds.width();
        float xCoordinate = (mMeasureWidth - tWidth) / 2;
        float baseline  = (mMeasureHeight -fontMetricsInt.bottom+fontMetricsInt.top) / 2-fontMetricsInt.top;
        float progressWidth = (mProgressCurrent / mProgressMax) *mMeasureWidth;
        if(progressWidth > xCoordinate){
            canvas.save(Canvas.CLIP_SAVE_FLAG);
            float right = Math.min(progressWidth, xCoordinate + tWidth * 1.1f);
            canvas.clipRect(xCoordinate, 0, right, mMeasureHeight);
            canvas.drawText(mStateTextCurrent, xCoordinate, baseline, mPaint);
            canvas.restore();
        }
    }

    private void drawText(Canvas canvas) {
        mTextBounds.setEmpty();
        mPaint.setColor(mProgressColor);
        mPaint.getTextBounds(mStateTextCurrent, 0, mStateTextCurrent.length(), mTextBounds);
        FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        int tWidth =mTextBounds.width();
        float xCoordinate = (mMeasureWidth - tWidth) / 2;
        float baseline  = (mMeasureHeight -fontMetricsInt.bottom+fontMetricsInt.top) / 2-fontMetricsInt.top;
        canvas.drawText(mStateTextCurrent, xCoordinate, baseline, mPaint);
    }

    private void drawProgress(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setStrokeWidth(0);
        mPaint.setColor(mProgressColor);

        mProgressCanvas.save(Canvas.CLIP_SAVE_FLAG);
        float right = (mProgressCurrent / mProgressMax) * mMeasureWidth;
        mProgressCanvas.clipRect(0, 0, right, mMeasureHeight);
        mProgressCanvas.drawColor(mProgressColor );
        mProgressCanvas.restore();
        mPaint.setShader(new BitmapShader(mProgressBitmap, TileMode.CLAMP,TileMode.CLAMP));
        canvas.drawRoundRect(mDrawBgRectF,mCornerRadius, mCornerRadius, mPaint);
        mPaint.setShader(null);

    }

    /**
     *  绘制边框
     */
    private void drawBorder(Canvas canvas) {
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(mProgressColor);
        mPaint.setStrokeWidth(dp2Px(0.5f));
       // mPath.moveTo(mCornerRadius,0);
   /*     mPath.moveTo(mCornerRadius,0);
        mPath.lineTo(mMeasureWidth-mCornerRadius,0);
        mPath.moveTo(mMeasureWidth-mCornerRadius,0);
        mPath.quadTo(mMeasureWidth,0,mMeasureWidth,mCornerRadius);
        mPath.moveTo(mMeasureWidth,mCornerRadius);
        mPath.lineTo(mMeasureWidth,mMeasureHeight-mCornerRadius);
        mPath.moveTo(mMeasureWidth,mMeasureHeight-mCornerRadius);
        mPath.quadTo(mMeasureWidth,mMeasureHeight,mMeasureWidth-mCornerRadius,mMeasureHeight);
        mPath.moveTo(mMeasureWidth-mCornerRadius,mMeasureHeight);
        mPath.lineTo(mCornerRadius,mMeasureHeight);
        mPath.moveTo(mCornerRadius,mMeasureHeight);
        mPath.quadTo(0,mMeasureHeight,0,mMeasureHeight-mCornerRadius);
        mPath.moveTo(0,mMeasureHeight-mCornerRadius);
        mPath.lineTo(0,mCornerRadius);
        mPath.moveTo(0,mCornerRadius);
        mPath.quadTo(0,0,mCornerRadius,0);
*/
        canvas.drawRoundRect(mDrawBgRectF,mCornerRadius,mCornerRadius, mPaint);
    }

    /**
     *   暴露的api提供下载、暂停、等待、继续、设置下载进度、设置最大下载进度等
     */

    public void setProgress(float progress){
        if(progress>=mProgressMax){
            mProgressCurrent=mProgressMax;
        }else{
            mProgressCurrent=progress;
        }
        invalidate();
    }

    public void setMaxProgress(float progressMax){
        mProgressMax=progressMax;
    }

    /**
     *  设置下载状态按钮显示,请设置默认提供的几种状态字段
     *   public static final int FREE_STATE=0;         下载
     *   public static final int DOWNLOADING_STATE=1;  暂停
     *   public static final int PAUSE_STATE =2;       继续
     *   public static final int WAITTING_STATE=3;     等待中状态
     *   public static final int DOWNLOAD_COMPLETE=4;  下载完成等待安装
     */
    public void  setState(int downLoadState){
        switch (downLoadState){
            case FREE_STATE:
                mStateTextCurrent=FREE_STATE_STR;
                break;
            case DOWNLOADING_STATE:
                mStateTextCurrent=DOWNLOADING_STATE_STR;
                break;
            case PAUSE_STATE:
                mStateTextCurrent=PAUSE_STATE_STR;
                break;
            case WAITTING_STATE:
                mStateTextCurrent=WAITTING_STATE_STR;
                break;
            case DOWNLOAD_COMPLETE:
                mStateTextCurrent=DOWNLOAD_COMPLETE_STR;
                break;
        }
        invalidate();
    }

    /**
     *   设置字体颜色
     *   parms textSize:字体颜色大小
     *         sizeType:字体大小类型是sp还是px
     */
    public void setTextSize(int textSize,TextSizeType sizeType){
       if(sizeType==TextSizeType.SP){
           mTextSize =sp2Px(textSize);
       }else if(sizeType==TextSizeType.PX){
           mTextSize=textSize;
       }
        initDefaultMeasureWidth();
        requestLayout();
    }



    /**
     *  设置边框圆角弧度大小
     */
    public void setCornerRadius(int radius){
        mCornerRadius=radius;
        invalidate();
    }
}
