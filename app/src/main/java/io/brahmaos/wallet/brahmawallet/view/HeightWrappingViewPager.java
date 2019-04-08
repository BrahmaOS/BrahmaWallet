package io.brahmaos.wallet.brahmawallet.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class HeightWrappingViewPager extends ViewPager {

    public HeightWrappingViewPager(Context context) {
        super(context);
        init();
    }

    public HeightWrappingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        this.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                HeightWrappingViewPager.this.getParent().requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View maxHeightView = getChildAt(0);
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                child.measure(widthMeasureSpec, heightMeasureSpec);
                if (child.getHeight() > maxHeightView.getHeight()) {
                    maxHeightView = child;
                }
            }
        }

        if (maxHeightView != null) {
            setMeasuredDimension(getMeasuredWidth(), measureHeight(heightMeasureSpec, maxHeightView));
        }
    }

    private int measureHeight(int measureSpec, View view) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            // set the height from the base view if available
            if (view != null) {
                result = view.getMeasuredHeight();
            }
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
}
