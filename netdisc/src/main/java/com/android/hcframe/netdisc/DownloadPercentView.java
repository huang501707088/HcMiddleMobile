package com.android.hcframe.netdisc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by pc on 2016/6/27.
 */
public class DownloadPercentView extends View {
    //状态设置
    /**
     * 下载失败
     */
    public final static int STATUS_NOBEGIN = 3;
    /**
     * 等待下载
     */
    public final static int STATUS_WAITING = 2;
    /**
     * 正在下载
     */
    public final static int STATUS_DOWNLOADING = 0;
    /**
     * 暂停
     */
    public final static int STATUS_PAUSED = 1;
    /**
     * 下载完成
     */
    public final static int STATUS_FINISHED = 4;
    /**
     * 圆形颜色
     */
    private int mCircleColor;
    /**
     * 画实心圆的画笔
     */
    private Paint mCirclePaint = null;
    /**
     * 画圆环的画笔
     */
    private Paint mRingPaint = null;
    // 绘制进度文字的画笔
//    private Paint mTxtPaint=null;
    /**
     * 圆环颜色
     */
    private int mRingColor;
    /**
     * 等待时显示的图片
     */
    private Bitmap mDownImg;
    //文字颜色
//    private int mTextColor;
    //文字大小
//    private int mTextSize;
    /**
     * 半径
     */
    private int mRadius;
    /**
     * 圆环宽度
     */
    private int mStrokeWidth = 1/10;
    /**
     * 圆心x坐标
     */
    private int mXCenter;
    /**
     * 圆心y坐标
     */
    private int mYCenter;
    /**
     * 总进度
     */
    private int mTotalProgress = 100;
    /**
     * 当前进度
     */
    private int mProgress;
    /**
     * 下载状态
     */
    private int mStatus = 1;
    /**
     * 默认显示的图片
     */
    private Bitmap mNotBeginImg;
    /**
     * 暂停时中间显示的图片
     */
    private Bitmap mPausedImg;
    /**
     * 等待时显示的图片
     */
    private Bitmap mWatiImg;
    /**
     * 下载完成时显示的图片
     */
    private Bitmap finishedImg;

    private final int SCALE = 2;

    public DownloadPercentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        /**
         * 获取自定义的属性
         * */
        initAttrs(context, attrs);
        /**
         * 初始值设置
         * */
        initVariable();
    }

    private void initVariable() {
        mCirclePaint = new Paint();
        mRingPaint = new Paint();
//        mTxtPaint = new Paint();
        /**
         * 初始化绘制灰色圆的画笔
         * */
        initPaint(mCirclePaint, mCircleColor);
        /**
         * 初始化绘制圆弧的画笔
         * */
        initPaint(mRingPaint, mRingColor);

        //初始化绘制文字的画笔
//        initTextPaint(mTxtPaint);
        //初始化要显示的图片
        initImage();
    }

    /**
     * 显示图片的处理
     */
    private void initImage() {
        mNotBeginImg = imageSize(mNotBeginImg, mRadius * SCALE, mRadius * SCALE);
        mPausedImg = imageSize(mPausedImg, mRadius * SCALE, mRadius * SCALE);
        mDownImg = imageSize(mDownImg, mRadius * SCALE, mRadius * SCALE);
        mWatiImg = imageSize(mWatiImg, mRadius * SCALE, mRadius * SCALE);
        finishedImg = imageSize(finishedImg, mRadius * SCALE, mRadius * SCALE);
    }

    public static Bitmap imageSize(Bitmap b, float x, float y) {
        int w = b.getWidth();//获取图片的宽高
        int h = b.getHeight();
        float sx = x / w;//要强制转换
        float sy = y / h;
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(b, 0, 0, w,
                h, matrix, true);
        return resizeBmp;
    }

    /**
     * 计算控件的宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) Math.ceil(mRadius)* SCALE;
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * 控件的中心坐标
         * */
        mXCenter = getWidth() / SCALE;
        mYCenter = getHeight() / SCALE;
        switch (mStatus) {
            case STATUS_DOWNLOADING://下载中0
                drawDownloadingView(canvas);
                break;
            case STATUS_PAUSED://暂停1
                drawPausedView(canvas);
                break;
            case STATUS_WAITING://等待下载状态2
                drawWatiView(canvas);
//                canvas.drawBitmap(mWatiImg, 0, 0, null);
                break;
            case STATUS_NOBEGIN://下载失败3
                drawFailView(canvas);
//                canvas.drawBitmap(mNotBeginImg, 0, 0, null);
                break;


            case STATUS_FINISHED://下载完成
                canvas.drawBitmap(finishedImg, 0, 0, null);
                break;
        }

    }

    /**
     * 绘制下载中的view
     *
     * @param canvas
     */
    private void drawDownloadingView(Canvas canvas) {
        /**
         * 绘制灰色圆环
         */
        canvas.drawCircle(mXCenter, mYCenter, mRadius - mStrokeWidth/ SCALE , mCirclePaint);
        /**
         * 绘制进度扇形圆环
         */
        RectF oval = new RectF();
        /**
         * 设置椭圆上下左右的坐标
         */
        oval.left = mXCenter - mRadius + mStrokeWidth / SCALE;
        oval.top = mYCenter - mRadius + mStrokeWidth / SCALE;
        oval.right = mXCenter + mRadius - mStrokeWidth / SCALE;
        oval.bottom = mYCenter + mRadius - mStrokeWidth / SCALE;
        canvas.drawArc(oval, -90, ((float) mProgress / mTotalProgress) * 360, false, mRingPaint);

        canvas.drawBitmap(mDownImg, 0, 0, null);
        //绘制中间百分比文字
//        String percentTxt = String.valueOf(mProgress);
        //计算文字垂直居中的baseline
//        Paint.FontMetricsInt fontMetrics = mTxtPaint.getFontMetricsInt();
//        float baseline = oval.top + (oval.bottom - oval.top - fontMetrics.bottom + fontMetrics.top) / SCALE - fontMetrics.top;
//        canvas.drawText(percentTxt+"%", mXCenter, baseline, mTxtPaint);
    }

    /**
     * 绘制暂停时的view
     *
     * @param canvas
     */
    private void drawPausedView(Canvas canvas) {
        /**
         * 绘制灰色圆环
         */
        canvas.drawCircle(mXCenter, mYCenter, mRadius - mStrokeWidth / SCALE, mCirclePaint);
        /**
         * 绘制进度扇形圆环
         */
        RectF oval = new RectF();
        /**
         * 设置椭圆上下左右的坐标
         */
        oval.left = mXCenter - mRadius + mStrokeWidth / SCALE;
        oval.top = mYCenter - mRadius + mStrokeWidth / SCALE;
        oval.right = mXCenter + mRadius - mStrokeWidth / SCALE;
        oval.bottom = mYCenter + mRadius - mStrokeWidth / SCALE;
        canvas.drawArc(oval, -90, ((float) mProgress / mTotalProgress) * 360, false, mRingPaint);
        /**
         * 绘制中间暂停图标
         */
        canvas.drawBitmap(mPausedImg, 0, 0, null);
    }

    /**
     * 绘制等待时的view
     *
     * @param canvas
     */
    private void drawWatiView(Canvas canvas) {
        /**
         * 绘制灰色圆环
         */
        canvas.drawCircle(mXCenter, mYCenter, mRadius - mStrokeWidth / SCALE, mCirclePaint);
        /**
         * 绘制进度扇形圆环
         */
        RectF oval = new RectF();
        /**
         * 设置椭圆上下左右的坐标
         */
        oval.left = mXCenter - mRadius + mStrokeWidth / SCALE;
        oval.top = mYCenter - mRadius + mStrokeWidth / SCALE;
        oval.right = mXCenter + mRadius - mStrokeWidth / SCALE;
        oval.bottom = mYCenter + mRadius - mStrokeWidth / SCALE;
        canvas.drawArc(oval, -90, ((float) mProgress / mTotalProgress) * 360, false, mRingPaint);
        /**
         * 绘制中间暂停图标
         */
        canvas.drawBitmap(mWatiImg, 0, 0, null);
    }

    /**
     * 绘制失败时的view
     *
     * @param canvas
     */
    private void drawFailView(Canvas canvas) {
        /**
         * 绘制灰色圆环
         */
        canvas.drawCircle(mXCenter, mYCenter, mRadius - mStrokeWidth / SCALE, mCirclePaint);
        /**
         * 绘制进度扇形圆环
         */
        RectF oval = new RectF();
        /**
         * 设置椭圆上下左右的坐标
         */
        oval.left = mXCenter - mRadius + mStrokeWidth / SCALE;
        oval.top = mYCenter - mRadius + mStrokeWidth / SCALE;
        oval.right = mXCenter + mRadius - mStrokeWidth / SCALE;
        oval.bottom = mYCenter + mRadius - mStrokeWidth / SCALE;
        canvas.drawArc(oval, -90, ((float) mProgress / mTotalProgress) * 360, false, mRingPaint);
        /**
         * 绘制中间暂停图标
         */
        canvas.drawBitmap(mNotBeginImg, 0, 0, null);
    }

    /**
     * 初始化圈和弧的画笔
     *
     * @param targetPaint 区分画笔
     * @param color       画笔颜色
     */
    private void initPaint(Paint targetPaint, int color) {
        targetPaint.setAntiAlias(true);
        targetPaint.setColor(color);
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeWidth(5);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.DownloadPercentView, 0, 0);
        mRadius = (int) typeArray.getDimension(R.styleable.DownloadPercentView_radius, 100);
        mStrokeWidth = (int) typeArray.getDimension(R.styleable.DownloadPercentView_strokeWidth, 2);
        mCircleColor = typeArray.getColor(R.styleable.DownloadPercentView_circleColor, 0xFFFFFFFF);
        mRingColor = typeArray.getColor(R.styleable.DownloadPercentView_ringColor, 0xFFFFFFFF);
//        mTextColor = typeArray.getColor(R.styleable.DownloadPercentView_textColor, 0xFFFFFFFF);
        mNotBeginImg = ((BitmapDrawable) typeArray.getDrawable(R.styleable.DownloadPercentView_notBeginImg)).getBitmap();
        mPausedImg = ((BitmapDrawable) typeArray.getDrawable(R.styleable.DownloadPercentView_pausedImg)).getBitmap();
        mWatiImg = ((BitmapDrawable) typeArray.getDrawable(R.styleable.DownloadPercentView_waitImg)).getBitmap();
        finishedImg = ((BitmapDrawable) typeArray.getDrawable(R.styleable.DownloadPercentView_finishedImg)).getBitmap();
        mDownImg = ((BitmapDrawable) typeArray.getDrawable(R.styleable.DownloadPercentView_downImg)).getBitmap();
//        mTextSize = (int)typeArray.getDimension(R.styleable.DownloadPercentView_textSize, 28);
        //记住recycle，并避免内存泄漏
        typeArray.recycle();
    }

    /**
     * 更新进度
     *
     * @param progress
     */
    public void setProgress(int progress) {
        mProgress = progress;
        postInvalidate();//刷新
    }

    /**
     * 设置下载状态
     *
     * @param status
     */
    public void setStatus(int status) {
        this.mStatus = status;
        postInvalidate();
    }

    /**
     * 获取下载状态
     *
     * @return
     */
    public int getStatus() {
        return mStatus;
    }

}
