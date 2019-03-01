package io.brahmaos.wallet.brahmawallet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import io.brahmaos.wallet.brahmawallet.R;

public class MyAutoLineLayout extends ViewGroup {
    private static final String TAG = "MyAutoLineLayout";

    private static final int CHILD_MARGIN = 20;
    private int mDefaultBottomMargin = 0;

    public MyAutoLineLayout(Context context){
        super(context);
        mDefaultBottomMargin = context.getResources().getDimensionPixelSize(R.dimen.space_larger);
    }

    public MyAutoLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefaultBottomMargin = context.getResources().getDimensionPixelSize(R.dimen.space_larger);
    }

    public MyAutoLineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDefaultBottomMargin = context.getResources().getDimensionPixelSize(R.dimen.space_larger);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "widthMeasureSpec = "+widthMeasureSpec+" heightMeasureSpec"+heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int usedWidth = CHILD_MARGIN;
        int remaining = 0;
        int totalHeight = CHILD_MARGIN;
        int lineHeight = 0;
        for (int index = 0; index < getChildCount(); index++) {
             final View childView = getChildAt(index);

            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            // calculate remaining width
            remaining = widthSize - usedWidth;

            // should start with a new line
            if (childView.getMeasuredWidth() > remaining) {
                usedWidth = CHILD_MARGIN;
                totalHeight += (lineHeight + CHILD_MARGIN);
            }

            usedWidth += (CHILD_MARGIN + childView.getMeasuredWidth());
            lineHeight = childView.getMeasuredHeight();
        }

        // If the layout height of the parent view group is wrap cotent, use the total height
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = totalHeight + CHILD_MARGIN + lineHeight;
        }
        setMeasuredDimension(widthSize, heightSize + mDefaultBottomMargin);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int mViewGroupWidth  = getMeasuredWidth();

        int mPainterPosX = CHILD_MARGIN;
        int mPainterPosY = CHILD_MARGIN;

        int childCount = getChildCount();
        for ( int i = 0; i < childCount; i++ ) {

            View childView = getChildAt(i);

            int width  = childView.getMeasuredWidth();
            int height = childView.getMeasuredHeight();

            if( mPainterPosX + width > mViewGroupWidth ) {
                mPainterPosX = CHILD_MARGIN;
                mPainterPosY += (height + CHILD_MARGIN);
            }

            childView.layout(mPainterPosX, mPainterPosY, mPainterPosX + width, mPainterPosY + height);

            mPainterPosX += (width + CHILD_MARGIN);
        }
    }
}
