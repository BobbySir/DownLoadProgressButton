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
 */

public class DownLoadProgressButton extends View {
    /**
     * download states (string)
     */
    private final String DOWNLOAD_FREE_STATE_STR = "下载";
    private final String DOWNLOAD_STATE_STR = "暂停";
    private final String DOWNLOAD_PAUSE_STATE_STR = "继续";
    private final String DOWNLOAD_WAITTING_STATE_STR = "等待中";
    private final String DOWNLOAD_COMPLETE_STR = "安装";
    private final String DOWNLOAD_COMPLETE_OPEN_STR = "打开";

    /**
     * downloading states (int)
     */
    public static final int DOWNLOAD_FREE_STATE = 0;
    public static final int DOWNLOADING_STATE = 1;
    public static final int DOWNLOAD_PAUSE_STATE = 2;
    public static final int DOWNLOAD_WAITTING_STATE = 3;
    public static final int DOWNLOAD_COMPLETE_STATE = 4;
    public static final int DOWNLOAD_COMPLETE_OPEN_STATE = 5;
    private RectF mDrawBgRectF;

    private Canvas mProgressCanvas;
    private Bitmap mProgressBitmap;


    /**
     * sp or px
     */
    public enum TextSizeType {
        SP, PX
    }

    /**
     * default text size
     */
    private final int DEFAULT_TEXT_SIZE = 14;

    private int mMeasureWidth;
    private int mMeasureHeight;


    private float mTextSize;
    /**
     * text
     */
    private String mStateTextCurrent;
    private Context mContext;

    /**
     * default wright width and height
     */
    private int mDefaultWidth;
    private int mDefaultHeight;

    /**
     * paint
     */
    private Paint mPaint = new Paint();

    private Rect mTextBounds = new Rect();

    private Path mPath = new Path();
    /**
     * progress and border color
     */
    private int mProgressColor;

    /**
     * current progress
     */
    private float mProgressCurrent;
    /**
     * max progress
     */
    private float mProgressMax;


    private int mCornerRadius;

    private int mBorderWidth;

    public DownLoadProgressButton(Context context) {
        this(context, null);
    }


    public DownLoadProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownLoadProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initParams();
    }

    /**
     * init default params
     */
    private void initParams() {
        mTextSize = sp2Px(DEFAULT_TEXT_SIZE);
        mBorderWidth = dp2Px(1f);
        //默认空闲状态
        mStateTextCurrent = DOWNLOAD_FREE_STATE_STR;
        //初始化圆角半径
        mCornerRadius = dp2Px(10);
        //默认进度条颜色
        mProgressColor = Color.parseColor("#f87908");
        initDefaultMeasureWidth();
        initPaint();
        //默认最大下载进度是100
        mProgressMax = 100;
        //初始化进度条Bitmap
    }

    private void initPaint() {
        mPaint.setAntiAlias(true);      //设置画笔是否抗锯齿（边缘柔化、消除混叠）
        mPaint.setStrokeJoin(Join.ROUND);
    }

    private void initDefaultMeasureWidth() {
        mTextBounds.setEmpty();
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(mStateTextCurrent, 0, mStateTextCurrent.length(), mTextBounds);

        int textWidth = mTextBounds.width();
        int textHeight = mTextBounds.height();

        // 设置默认控件大小，默认宽高为字体宽高二倍

        mDefaultWidth = textWidth * 2;
        mDefaultHeight = textHeight * 2;
    }


    private float sp2Px(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, mContext.getResources().getDisplayMetrics());
    }

    private int dp2Px(float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, mContext.getResources().getDisplayMetrics()) + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            mMeasureWidth = widthSize;
        } else {
            //非滚动这个种控件一般都是AT_MOST
            if(widthSize!=0){
                mMeasureWidth = Math.min(mDefaultWidth, widthSize);
            }else{
                mMeasureWidth=mDefaultWidth;
            }

        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mMeasureHeight = heightSize;
        } else {
            if(heightSize!=0){
                mMeasureHeight = Math.min(mDefaultHeight, heightSize);
            }else{
                mMeasureHeight =mDefaultHeight;
            }

        }
        mDrawBgRectF = new RectF(mBorderWidth, mBorderWidth, mMeasureWidth - mBorderWidth, mMeasureHeight - mBorderWidth);

        mProgressBitmap = Bitmap.createBitmap(mMeasureWidth - mBorderWidth, mMeasureHeight - mBorderWidth, Config.ARGB_4444);

        mProgressCanvas = new Canvas(mProgressBitmap);
        setMeasuredDimension(mMeasureWidth, mMeasureHeight);
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
        int tWidth = mTextBounds.width();
        float xCoordinate = (mMeasureWidth - tWidth) / 2;
        float baseline = (mMeasureHeight - fontMetricsInt.bottom + fontMetricsInt.top) / 2 - fontMetricsInt.top;
        float progressWidth = (mProgressCurrent / mProgressMax) * mMeasureWidth;
        if (progressWidth > xCoordinate) {
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
        int tWidth = mTextBounds.width();
        float xCoordinate = (mMeasureWidth - tWidth) / 2;
        float baseline = (mMeasureHeight - fontMetricsInt.bottom + fontMetricsInt.top) / 2 - fontMetricsInt.top;
        canvas.drawText(mStateTextCurrent, xCoordinate, baseline, mPaint);
    }

    private void drawProgress(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setStrokeWidth(0);
        mPaint.setColor(mProgressColor);

        mProgressCanvas.save(Canvas.CLIP_SAVE_FLAG);
        float right = (mProgressCurrent / mProgressMax) * mMeasureWidth;
        mProgressCanvas.clipRect(0, 0, right, mMeasureHeight);
        mProgressCanvas.drawColor(mProgressColor);
        mProgressCanvas.restore();
        mPaint.setShader(new BitmapShader(mProgressBitmap, TileMode.CLAMP, TileMode.CLAMP));
        canvas.drawRoundRect(mDrawBgRectF, mCornerRadius, mCornerRadius, mPaint);
        mPaint.setShader(null);

    }

    /**
     * draw the border
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
        canvas.drawRoundRect(mDrawBgRectF, mCornerRadius, mCornerRadius, mPaint);
    }

    /**
     * set progress
     */

    public void setProgress(float progress) {
        if (progress >= mProgressMax) {
            mProgressCurrent = mProgressMax;
        } else {
            mProgressCurrent = progress;
        }
        invalidate();
    }

    public void setMaxProgress(float progressMax) {
        mProgressMax = progressMax;
    }

    /**
     * set download button state
     * public static final int DOWNLOAD_FREE_STATE=0;         下载
     * public static final int DOWNLOADING_STATE=1;           暂停
     * public static final int DOWNLOAD_PAUSE_STATE =2;       继续
     * public static final int DOWNLOAD_WAITTING_STATE=3;     等待中状态
     * public static final int DOWNLOAD_COMPLETE_STATE=4;     下载完成等待安装
     */
    public void setState(int downLoadState) {
        switch (downLoadState) {
            case DOWNLOAD_FREE_STATE:
                mStateTextCurrent = DOWNLOAD_FREE_STATE_STR;
                break;
            case DOWNLOADING_STATE:
                mStateTextCurrent = DOWNLOAD_STATE_STR;
                break;
            case DOWNLOAD_PAUSE_STATE:
                mStateTextCurrent = DOWNLOAD_PAUSE_STATE_STR;
                break;
            case DOWNLOAD_WAITTING_STATE:
                mStateTextCurrent = DOWNLOAD_WAITTING_STATE_STR;
                break;
            case DOWNLOAD_COMPLETE_STATE:
                mStateTextCurrent = DOWNLOAD_COMPLETE_STR;
                break;
            case DOWNLOAD_COMPLETE_OPEN_STATE:
                mStateTextCurrent = DOWNLOAD_COMPLETE_OPEN_STR;
                break;
        }
        invalidate();
    }

    /**
     * set text color
     * parms textSize:text size
     * sizeType:type is sp or px?
     */
    public void setTextSize(int textSize, TextSizeType sizeType) {
        if (sizeType == TextSizeType.SP) {
            mTextSize = sp2Px(textSize);
        } else if (sizeType == TextSizeType.PX) {
            mTextSize = textSize;
        }
        initDefaultMeasureWidth();
        requestLayout();
    }


    /**
     * set corner radius
     */
    public void setCornerRadius(int radius) {
        mCornerRadius = radius;
        invalidate();
    }


    /**
     * set text
     */
    public void setText(String text) {
        mStateTextCurrent = text;
        initDefaultMeasureWidth();
        requestLayout();
    }

}
