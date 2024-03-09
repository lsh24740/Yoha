package com.gooey.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.view.ViewCompat;

/**
 * @author lishihui01
 * @Date 2023/9/21
 * @Describe:
 */
public class StableSlidingLayout extends ClosableSlidingLayout {
    public StableSlidingLayout(Context context) {
        super(context);
    }

    public StableSlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StableSlidingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // layout的时候要把child还原回原来的位置
        View child = getChildAt(0);
        int oldLeft = child.getLeft();
        int oldTop = child.getTop();
        int oldRight = child.getRight();
        int oldBottom = child.getBottom();
        super.onLayout(changed, left, top, right, bottom);
        int height = child.getHeight();
        int width = child.getWidth();
        int newLeft = child.getLeft();
        int newTop = child.getTop();
        if (height == (oldBottom - oldTop)
                && width == (oldRight - oldLeft)
                && (oldLeft != newLeft || oldTop != newTop)
                && oldTop >= 0 && oldLeft >= 0) {
            int offsetX = oldLeft - newLeft;
            int offsetY = oldTop - newTop;
            ViewCompat.offsetTopAndBottom(child, offsetY);
            ViewCompat.offsetLeftAndRight(child, offsetX);
        }
    }
}
