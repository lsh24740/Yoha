package com.gooey.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import java.util.HashSet;
import java.util.Set;

/**
 *@author lishihui01
 *@Date 2023/9/21
 *@Describe:
 */

public class ClosableSlidingLayout extends FrameLayout {
    private static final String TAG = "ClosableSlidingLayout";
    private static final boolean DEBUG = false;
    private Set<View> ignoreViews = new HashSet<View>();
    private Set<View> requestDisallowInterceptTouchEventViews = new HashSet<View>();

    public void addIgnoreScrollView(View view) {
        ignoreViews.add(view);
    }

    public void addRequestDisallowInterceptTouchEventViews(View view) {
        if (!requestDisallowInterceptTouchEventViews.contains(view)) {
            requestDisallowInterceptTouchEventViews.add(view);
        }
    }

    private final float MINVEL;
    private ViewDragHelper mDragHelper;
    private SlideListener mListener;
    private int distance;
    private int start;
    private int mActivePointerId;
    private boolean mIsBeingDragged;
    private float mInitialMotionX, mInitialMotionY;
    private static final int INVALID_POINTER = -1;
    public View mTarget;

    private boolean collapsible = false;
    private boolean vertical = true;
    private boolean isStart = false;
    private float xDiff, yDiff;

    private boolean swipeable = true;
    private boolean realSwipeable = true; // 实际上外部设置的swipeable，不受webview逻辑影响
    private boolean canSloveConflict = false; // 是否能解决webview的滑动冲突
    private OnTouchListener mPreTouchListener;
    private WebView webview = null;

    public ClosableSlidingLayout(Context context) {
        this(context, null);
    }

    public ClosableSlidingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ClosableSlidingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDragHelper = ViewDragHelper.create(this, 0.8f, new ViewDragCallback());
        MINVEL = getResources().getDisplayMetrics().density * 400;
    }

    private boolean isInIgnoreViews(@NonNull MotionEvent event, Set<View> views) {
        for (View view : views) {
            int[] locationPosition = new int[2];
            int[] location = new int[4];
            view.getLocationOnScreen(locationPosition);
            location[0] = locationPosition[0];
            location[1] = locationPosition[1];
            location[2] = view.getWidth() + location[0];
            location[3] = view.getHeight() + location[1];
            if (event.getRawY() > location[1]
                    && event.getRawY() < location[3]
                    && event.getRawX() > location[0]
                    && event.getRawX() < location[2]) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        distance = vertical ? getChildAt(0).getHeight() : getChildAt(0).getWidth();
    }

    private void findWebView(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        if (count == 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof WebView) {
                webview = (WebView) view;
                break;
            } else {
                if (view instanceof ViewGroup) {
                    findWebView((ViewGroup) view);
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mPreTouchListener != null) {
            mPreTouchListener.onTouch(this, ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        //        if (!isEnabled() || canChildScrollUp()) {
//            return super.onTouchEvent(ev);
//        }

        // todo 运行效率
        if (webview == null) {
            findWebView(this);  // 寻找子view里面有没有webview
            if (webview != null) {
                webview.evaluateJavascript("javascript: typeof window.getWebViewContentHeight === 'function'", value -> {
                    try {
                        canSloveConflict = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        canSloveConflict = false;
                    }
                });
            }
        }

        if (webview != null && canSloveConflict) {
            /**
             * webview内部在滚动的时候 Android 拿不到它的滚动状态，scrolly取出来一直是0
             * 所以通过执行js 方法去获取值
             * 如果js端给的高度大于0，则说明不能滑动，swipeable为false
             */
            webview.evaluateJavascript("javascript:window.getWebViewContentHeight()", value -> {
                try {
                    float height = Float.parseFloat(value);
                    if (height > 0) {
                        swipeable = false;
                    } else {
                        swipeable = realSwipeable;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    swipeable = false;
                }
            });
        }

        if (DEBUG) {
            Log.d(TAG, "onInterceptTouchEvent e = " + event);
        }
        if (isInIgnoreViews(event, requestDisallowInterceptTouchEventViews)) {
            return false;
        }
        if ((action == MotionEvent.ACTION_MOVE && !isInIgnoreViews(event, ignoreViews))
                && (!isEnabled() || canChildScrollUp((int) event.getX(), (int) event.getY()))) {
            return false;
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mActivePointerId = INVALID_POINTER;
            mIsBeingDragged = false;
            if (collapsible && Math.abs(xDiff) < Math.abs(yDiff)
                    && -yDiff > mDragHelper.getTouchSlop()) {
                expand(mDragHelper.getCapturedView(), 0);
            }
            mDragHelper.cancel();
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                distance = vertical ? getChildAt(0).getHeight() : getChildAt(0).getWidth();
                start = vertical ? getChildAt(0).getTop() : getChildAt(0).getLeft();
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(event, mActivePointerId);
                final float initialMotionX = getMotionEventX(event, mActivePointerId);
                if (initialMotionY == -1 || initialMotionX == -1) {
                    return false;
                }
                mInitialMotionX = initialMotionX;
                mInitialMotionY = initialMotionY;
                xDiff = 0;
                yDiff = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(event, mActivePointerId);
                final float x = getMotionEventX(event, mActivePointerId);
                if (y == -1 || x == -1) {
                    return false;
                }
                xDiff = x - mInitialMotionX;
                yDiff = y - mInitialMotionY;
                if (swipeable
                        && (vertical && Math.abs(xDiff) < Math.abs(yDiff) && (isStart ? -1 : 1) * yDiff > mDragHelper.getTouchSlop()
                        || !vertical && Math.abs(yDiff) < Math.abs(xDiff) && xDiff > mDragHelper.getTouchSlop())
                        && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                    mDragHelper.captureChildView(getChildAt(0), 0);
                }
                break;
        }
        try {
            if (swipeable) {
                mDragHelper.shouldInterceptTouchEvent(event);
            }
        } catch (Exception ignore) {
        }
        return mIsBeingDragged;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }

    public boolean getSwipeable() {
        return swipeable;
    }

    public void setSwipeable(boolean enable) {
        swipeable = enable;
        realSwipeable = enable;
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    private boolean canChildScrollUp(int x, int y) {
        return canScroll(this, false, vertical ? -1 : -1, x, y, vertical);
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y, boolean vertical) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                // This will not work for transformed views in Honeycomb+
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight()
                        && y + scrollY >= child.getTop() && y + scrollY < child.getBottom()
                        && canScroll(child, true, dx, x + scrollX - child.getLeft(),
                        y + scrollY - child.getTop(), vertical)) {
                    return true;
                }
            }
        }
        return checkV && vertical ? v.canScrollVertically(dx) : v.canScrollHorizontally(dx);
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private float getMotionEventX(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getX(ev, index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        if (!isEnabled() || canChildScrollUp()) {
//            return super.onTouchEvent(ev);
//        }
        if (DEBUG) {
            Log.d(TAG, "onTouchEvent e = " + ev);
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        if ((!isEnabled() || canChildScrollUp((int) ev.getX(), (int) ev.getY()))
                && (action == MotionEvent.ACTION_MOVE && !isInIgnoreViews(ev, ignoreViews))) {
            return false;
        }
        try {
            if (swipeable)
                mDragHelper.processTouchEvent(ev);
        } catch (Exception ignored) {
        }
        return super.onTouchEvent(ev) || swipeable;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setSlideListener(SlideListener listener) {
        mListener = listener;
    }

    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
    }

    public void setVertical(boolean vertical, boolean start) {
        this.vertical = vertical;
        this.isStart = start;
    }

    /**
     * Callback
     */
    private class ViewDragCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (DEBUG) {
                Log.d(TAG, "child = " + releasedChild + ", currentTop = "
                        + releasedChild.getTop() + ", start = " + start
                        + ", x = " + xvel + ", y = " + yvel);
            }
            if (vertical) {
                if (isStart) {
                    if (yvel < -MINVEL) {
                        dismiss(releasedChild, yvel);
                    } else {
                        if (releasedChild.getBottom() <= start + distance / 2) {
                            dismiss(releasedChild, yvel);
                        } else {
                            mDragHelper.smoothSlideViewTo(releasedChild, 0, start);
                        }
                    }
                } else {
                    if (yvel > MINVEL) {
                        dismiss(releasedChild, yvel);
                    } else {
                        if (releasedChild.getTop() >= start + distance / 2) {
                            dismiss(releasedChild, yvel);
                        } else {
                            mDragHelper.smoothSlideViewTo(releasedChild, 0, start);
                        }
                    }
                }
            } else {
                if (isStart) {
                    if (xvel < -MINVEL) {
                        dismiss(releasedChild, yvel);
                    } else {
                        if (releasedChild.getRight() <= start + distance / 2) {
                            dismiss(releasedChild, yvel);
                        } else {
                            mDragHelper.smoothSlideViewTo(releasedChild, start, 0);
                        }
                    }
                } else {
                    if (xvel > MINVEL) {
                        dismiss(releasedChild, yvel);
                    } else {
                        if (releasedChild.getLeft() >= start + distance / 2) {
                            dismiss(releasedChild, yvel);
                        } else {
                            mDragHelper.smoothSlideViewTo(releasedChild, start, 0);
                        }
                    }
                }
            }
            ViewCompat.postInvalidateOnAnimation(ClosableSlidingLayout.this);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (vertical) {
                if (isStart) {
                    mListener.onSlided((float) Math.abs(Math.min(top, 0)) / (float) distance);
                    if (distance + top < 1 && mListener != null) {
                        mListener.onClosed();
                    }
                } else {
                    mListener.onSlided((float) Math.max(top, 0) / (float) distance);
                    if (distance - top < 1 && mListener != null) {
                        mListener.onClosed();
                    }
                }
            } else {
                if (isStart) {
                    mListener.onSlided((float) Math.abs(Math.min(left, 0)) / (float) distance);
                    if (distance + left < 1 && mListener != null) {
                        mListener.onClosed();
                    }
                } else {
                    mListener.onSlided((float) Math.max(left, 0) / (float) distance);
                    if (distance - left < 1 && mListener != null) {
                        mListener.onClosed();
                    }
                }
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (vertical) {
                if (isStart) {
                    return Math.min(top, ClosableSlidingLayout.this.start);
                } else {
                    return Math.max(top, ClosableSlidingLayout.this.start);
                }
            } else {
                return 0;
            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (vertical) {
                return 0;
            } else {
                if (isStart) {
                    return Math.min(left, ClosableSlidingLayout.this.start);
                } else {
                    return Math.max(left, ClosableSlidingLayout.this.start);
                }
            }
        }
    }

    private void expand(View releasedChild, float yvel) {
        if (mListener != null) {
            mListener.onOpened();
        }
    }

    public void dismiss(View view, float yvel) {
        if (DEBUG) {
            Log.d(TAG, "dismiss, y = " + yvel);
        }
        mDragHelper.cancel();
        mDragHelper.abort();
        if (vertical) {
            if (isStart) {
                mDragHelper.smoothSlideViewTo(view, view.getLeft(), -distance);
            } else {
                mDragHelper.smoothSlideViewTo(view, view.getLeft(), start + distance);
            }
        } else {
            if (isStart) {
                mDragHelper.smoothSlideViewTo(view, -distance, view.getTop());
            } else {
                mDragHelper.smoothSlideViewTo(view, start + distance, view.getTop());
            }


        }
        ViewCompat.postInvalidateOnAnimation(ClosableSlidingLayout.this);
    }

    public void show(View view) {
        if (DEBUG) {
            Log.d(TAG, "show");
        }
        mDragHelper.cancel();
        mDragHelper.abort();
        if (vertical) {
            mDragHelper.smoothSlideViewTo(view, view.getLeft(), 0);
        } else {
            mDragHelper.smoothSlideViewTo(view, 0, 0);
        }

        ViewCompat.postInvalidateOnAnimation(ClosableSlidingLayout.this);
    }

    public void abort() {
        mDragHelper.cancel();
        mDragHelper.abort();
    }

    public void setPreTouchListener(OnTouchListener preTouchListener) {
        this.mPreTouchListener = preTouchListener;
    }

    /**
     * set listener
     */
    public interface SlideListener {
        void onClosed();

        void onOpened();

        void onSlided(float rate);
    }
}