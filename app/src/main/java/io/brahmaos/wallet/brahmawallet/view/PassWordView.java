package io.brahmaos.wallet.brahmawallet.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

public class PassWordView extends View {

    private Paint mPaint;

    private Handler mHandler;

    private boolean isDrawText;

    private boolean isInputState = false;

    private boolean mDrawRemindLineState;

    private int mInputStateBoxColor;
    private int mNoInputStateBoxColor;

    private int mRemindLineColor;
    private int mInputStateTextColor;


    private int mBoxDrawType = 0;
    private int mShowPassType = 0;

    private boolean isShowRemindLine = true;

    private int mWidth = 40;
    private int mHeight = 40;

    private String mPassText = "";
    private Context mContext;

    private int mDrawTxtSize = 18;

    private int mDrawBoxLineSize = 4;

    public void setInputStateColor(int inputColor) {
        this.mInputStateBoxColor = inputColor;
    }

    public void setmPassText(String mPassText) {
        this.mPassText = mPassText;
    }

    public void setNoinputColor(int noinputColor) {
        this.mNoInputStateBoxColor = noinputColor;
    }

    public void setInputState(boolean input) {
        isInputState = input;
    }

    public void setRemindLineColor(int line_color) {
        this.mRemindLineColor = line_color;
    }


    public void setInputStateTextColor(int drc_color) {
        this.mInputStateTextColor = drc_color;
    }


    public PassWordView(Context context) {
        this(context, null);
    }

    public PassWordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setmBoxDrawType(int mBoxDrawType) {
        this.mBoxDrawType = mBoxDrawType;
    }


    public void setmShowPassType(int mShowPassType) {
        this.mShowPassType = mShowPassType;
    }

    public void setmDrawTxtSize(int mDrawTxtSize) {
        this.mDrawTxtSize = mDrawTxtSize;
    }


    public void setmIsShowRemindLine(boolean mIsShowShan) {
        this.isShowRemindLine = mIsShowShan;
    }

    public void setmDrawBoxLineSize(int mDrawBoxLineSize) {
        this.mDrawBoxLineSize = mDrawBoxLineSize;
    }

    public PassWordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mPaint.setStrokeWidth(mDrawBoxLineSize);
        mPaint.setPathEffect(new CornerPathEffect(1));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = 0;

        if (modeWidth == MeasureSpec.EXACTLY) {
            width = sizeWidth;
        } else {
            width = mWidth;
            if (modeWidth == MeasureSpec.AT_MOST) {
                width = Math.min(width, sizeWidth);
            }
        }

        if (modeHeight == MeasureSpec.EXACTLY) {
            height = sizeHeight;
        } else {
            height = mHeight;
            if (modeHeight == MeasureSpec.AT_MOST) {
                height = Math.min(height, sizeHeight);
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawInputBox(canvas);

        drawRemindLine(canvas);

        drawInputTextOrPicture(canvas);
    }

    private void drawInputTextOrPicture(Canvas canvas) {
        if (isDrawText) {

            mPaint.setColor(ContextCompat.getColor(mContext, mInputStateTextColor));
            mPaint.setStyle(Paint.Style.FILL);
            switch (mShowPassType) {
                case 0:
                    canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, 8, mPaint);
                    break;
                case 1:
                    mPaint.setTextSize(getMeasuredWidth() / 2 + 10);
                    float stringWidth = mPaint.measureText("*");

                    float baseY = (getMeasuredHeight() / 2 - ((mPaint.descent() + mPaint.ascent()) / 2)) + stringWidth / 3;  //实现y轴居中方法
                    float baseX = getMeasuredWidth() / 2 - stringWidth / 2;
                    canvas.drawText("*", baseX, baseY, mPaint);
                    break;
                case 2:
                    mPaint.setTextSize(mDrawTxtSize);
                    float stringWidth2 = mPaint.measureText(mPassText);

                    float baseY2 = (getMeasuredHeight() / 2 - ((mPaint.descent() + mPaint.ascent()) / 2)) + stringWidth2 / 5;  //实现y轴居中方法
                    float baseX2 = getMeasuredWidth() / 2 - stringWidth2 / 2;
                    canvas.drawText(mPassText, baseX2, baseY2, mPaint);
                    break;
            }
        }
    }

    private void drawRemindLine(Canvas canvas) {
        if (mDrawRemindLineState && isShowRemindLine) {
            int line_height = getMeasuredWidth() / 2 - 10;

            line_height = line_height < 0 ? getMeasuredWidth() / 2 : line_height;

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(ContextCompat.getColor(mContext, mRemindLineColor));
            canvas.drawLine(getMeasuredWidth() / 2, getMeasuredHeight() / 2 - line_height / 2, getMeasuredWidth() / 2, getMeasuredHeight() / 2 + line_height / 2, mPaint);
        }
    }

    private void drawInputBox(Canvas canvas) {
        if (isInputState) {
            mPaint.setColor(ContextCompat.getColor(mContext, mInputStateBoxColor));
        } else {
            mPaint.setColor(ContextCompat.getColor(mContext, mNoInputStateBoxColor));
        }
        mPaint.setStyle(Paint.Style.STROKE);
        switch (mBoxDrawType) {
            case 1:
                canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, getMeasuredWidth() / 2 - 5, mPaint);
                break;
            case 2:
                canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), mPaint);
                break;
            default:
                RectF rect = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
                canvas.drawRoundRect(rect, 6, 6, mPaint);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void updateInputState(boolean isinput) {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (isinput) {
            isInputState = true;
            isDrawText = true;
        } else {
            isInputState = false;
            isDrawText = false;
        }
        mDrawRemindLineState = false;
        invalidate();
    }

    public void startInputState() {
        isInputState = true;
        isDrawText = false;

        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.removeCallbacksAndMessages(null);

        if (!isShowRemindLine) {
            invalidate();
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDrawRemindLineState = !mDrawRemindLineState;
                invalidate();
                mHandler.postDelayed(this, 800);

            }
        });
    }

}
