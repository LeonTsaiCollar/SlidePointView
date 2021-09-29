package com.leon.slidepointview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SlidePointView extends View {
    private Context mContext;
    private Paint line_paint, circle_paint;
    private TextPaint text_paint;
    private int scaleWidth;


    private Map<Integer, Integer> scaleInfos = new HashMap<>();
    private float mLastX;
    private float mPointXoff;

    private int circleX;
    private int circleY;

    private boolean isDrag;

    private Integer disX;
    private boolean isJump;
    private boolean isAnimStarting;


    public SlidePointView(Context context) {
        super(context);
    }

    public SlidePointView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        line_paint = new Paint();
//        line_paint.setColor(Color.parseColor("#FFEBEBEB"));
        line_paint.setColor(Color.parseColor("#000000"));
        line_paint.setStyle(Paint.Style.STROKE);
        line_paint.setAntiAlias(true);
        line_paint.setStrokeWidth(dp2px(mContext, 2));

        text_paint = new TextPaint();
        text_paint.setAntiAlias(true);
//        text_paint.setColor(Color.parseColor("#FFEBEBEB"));
        text_paint.setColor(Color.parseColor("#000000"));
        text_paint.setStyle(Paint.Style.STROKE);
        text_paint.setTextAlign(Paint.Align.CENTER);
        text_paint.setTextSize(sp2px(mContext, 11));

        circle_paint = new Paint();
        circle_paint.setAntiAlias(true);
        circle_paint.setColor(Color.parseColor("#FFFF761B"));
        text_paint.setStyle(Paint.Style.FILL);

        scaleWidth = dp2px(mContext, 41);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBottomLine(canvas);
        drawScaleLine(canvas);
        drawCircle(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        circleX = getMeasuredWidth() / 2;
        circleY = getMeasuredHeight() / 2;
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(circleX, circleY, dp2px(mContext, 8), circle_paint);
    }

    private void drawScaleLine(Canvas canvas) {
        canvas.drawLine(getMeasuredWidth() / 2, getMeasuredHeight() / 2 + dp2px(mContext, 7)
                , getMeasuredWidth() / 2, getMeasuredHeight() / 2 - dp2px(mContext, 7), line_paint);
        if (scaleInfos.size() == 0) {
            scaleInfos.put(0, getMeasuredWidth() / 2);
            for (int i = 1; i <= 4; i++) {
                scaleInfos.put(i * 10, getMeasuredWidth() / 2 + i * scaleWidth);
                scaleInfos.put(-i * 10, getMeasuredWidth() / 2 - i * scaleWidth);
            }
        }

        Paint.FontMetrics fontMetrics = text_paint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int baseLineY = (int) (getMeasuredHeight() / 2 + dp2px(mContext, 14) - top / 2 - bottom / 2);


        for (int i = 1; i <= 4; i++) {
            canvas.drawLine(getMeasuredWidth() / 2 + i * scaleWidth, getMeasuredHeight() / 2 + dp2px(mContext, 7)
                    , getMeasuredWidth() / 2 + i * scaleWidth, getMeasuredHeight() / 2 - dp2px(mContext, 7), line_paint);
            canvas.drawLine(getMeasuredWidth() / 2 - i * scaleWidth, getMeasuredHeight() / 2 + dp2px(mContext, 7)
                    , getMeasuredWidth() / 2 - i * scaleWidth, getMeasuredHeight() / 2 - dp2px(mContext, 7), line_paint);
            canvas.drawText(i * 10 + "°", getMeasuredWidth() / 2 + i * scaleWidth, baseLineY, text_paint);
            canvas.drawText(-i * 10 + "°", getMeasuredWidth() / 2 - i * scaleWidth, baseLineY, text_paint);
        }
    }

    private void drawBottomLine(Canvas canvas) {
        canvas.drawLine(0, getMeasuredHeight() / 2, getMeasuredWidth(), getMeasuredHeight() / 2, line_paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float mPointX = event.getX();
        float mPointY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (disFromCircle(mPointX, mPointY) < dp2px(mContext, 8)) {
                    isDrag = true;
                } else {
                    isDrag = false;
                }

                if (scaleInfos.size() > 0) {
                    for (Integer key : scaleInfos.keySet()) {
                        if (disBetween(mPointX, mPointY, scaleInfos.get(key), getMeasuredHeight() / 2) <= dp2px(mContext, 8)) {
                            isJump = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mPointXoff = mPointX - mLastX;
                if (isDrag) {
                    circleX += (int) mPointXoff;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                //拖动和点击都可以造成原点的移动,如果拖动条件满足,就不要进行点击事件了
                if (isDrag && scaleInfos.size() > 0) {
                    disX = scaleInfos.get(0);
                    int minDis = Math.abs(circleX - disX);
                    for (Integer key : scaleInfos.keySet()) {
                        if (Math.abs(scaleInfos.get(key) - circleX) < minDis) {
                            disX = scaleInfos.get(key);
                            minDis = Math.abs(scaleInfos.get(key) - circleX);
                        }
                    }
                    moveToX();
                    break;
                }

                if (isJump && scaleInfos.size() > 0) {
                    disX = scaleInfos.get(0);
                    int minDis = Math.abs((int) mPointX - disX);
                    for (Integer key : scaleInfos.keySet()) {
                        if (Math.abs(scaleInfos.get(key) - (int) mPointX) < minDis) {
                            disX = scaleInfos.get(key);
                            minDis = Math.abs(scaleInfos.get(key) - (int) mPointX);
                        }
                    }
                    moveToX();
                }
                break;
            default:
                break;
        }
        mLastX = mPointX;
        return true;
    }

    private int dp2px(Context context, int dpValue) {
        return (int) (context.getResources().getDisplayMetrics().density * dpValue + 0.5f);
    }

    public int sp2px(Context context, int spValue) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, displayMetrics) + 0.5f);
    }

    private float disFromCircle(float X, float Y) {
        float disX = Math.abs(X - circleX);
        float disY = Math.abs(Y - circleY);
        return (float) Math.sqrt(disX * disX + disY * disY);
    }


    private float disBetween(float X1, float Y1, float X2, float Y2) {
        float disX = Math.abs(X1 - X2);
        float disY = Math.abs(Y1 - Y2);
        return (float) Math.sqrt(disX * disX + disY * disY);
    }


    private ScrolleAnim mScrolleAnim;

    //差值器自定义动画
    private class ScrolleAnim extends Animation {

        float fromX = 0f;
        float desX = 0f;

        public ScrolleAnim(float f, float d) {
            fromX = f;
            desX = d;
        }


        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            circleX = (int) (fromX + (desX - fromX) * interpolatedTime);//计算动画每贞滑动的距离
            invalidate();
        }
    }


    private void moveToX() {
        if (isAnimStarting) {
            return;
        }
        if (mScrolleAnim != null) {
            clearAnimation();
        }
        mScrolleAnim = new ScrolleAnim(circleX, disX);

        mScrolleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimStarting = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimStarting = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mScrolleAnim.setDuration(500);
        startAnimation(mScrolleAnim);


        if (scaleInfos.size() > 0) {

            Integer result = 0;
            Set<Map.Entry<Integer, Integer>> set = scaleInfos.entrySet();

            for (Map.Entry<Integer, Integer> entry : set) {
                if (entry.getValue() == disX) {
                    result = entry.getKey();
                    break;
                }
            }

            if (isJump || isDrag) {
                if (onAngleListener != null) {
                    onAngleListener.onAngle(result);
                }
            }
        }
    }


    private OnAngleListener onAngleListener;

    public void setOnAngleListener(OnAngleListener onAngleListener) {
        this.onAngleListener = onAngleListener;
    }

    public interface OnAngleListener {
        void onAngle(int angle);
    }

    public void resetPosition() {
        isDrag = false;
        isJump = false;
        if (scaleInfos.size() > 0) {
            disX = scaleInfos.get(0);
            moveToX();
        }
    }

    public void setPosition(int destination) {
        isDrag = false;
        isJump = false;
        if (scaleInfos.size() > 0) {
            disX = scaleInfos.get(destination);
            moveToX();
        }
    }
}
