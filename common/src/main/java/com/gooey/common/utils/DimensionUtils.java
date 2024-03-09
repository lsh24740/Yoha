package com.gooey.common.utils;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.blankj.utilcode.util.Utils;

/**
 * @author lishihui01
 * @Date 2023/9/23
 * @Describe:
 */
public class DimensionUtils {
    public static final DisplayMetrics DISPLAY_METRICS;
    public static final float DENSITY;
    public static final int SCREEN_WIDTH_PORTRAIT;
    public static final int SCREEN_HEIGHT_PORTRAIT;

    static {
        Resources resources = Utils.getApp().getResources();
        boolean portrait = resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        DISPLAY_METRICS = resources.getDisplayMetrics();
        DENSITY = DISPLAY_METRICS.density;
        SCREEN_WIDTH_PORTRAIT = portrait ? DISPLAY_METRICS.widthPixels : DISPLAY_METRICS.heightPixels;
        SCREEN_HEIGHT_PORTRAIT = portrait ? DISPLAY_METRICS.heightPixels : DISPLAY_METRICS.widthPixels;
    }

    /**
     * 推荐使用Ui.kt中的dimenToPx()扩展方法
     */
    public static int getDimensionSize(int dimenid) {
        return Utils.getApp().getResources().getDimensionPixelSize(dimenid);
    }

    /**
     * 推荐使用Ui.kt中的dp()扩展方法
     */
    public static int dpToPx(float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, DISPLAY_METRICS) + 0.5);
    }

    /**
     * 推荐使用Ui.kt中的dpF()扩展方法
     */
    public static float dpToPxF(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, DISPLAY_METRICS);
    }

    /**
     * 推荐使用Ui.kt中的dimenToPx()扩展方法
     */
    public static int dimen2px(int dimen) {
        return Utils.getApp().getResources().getDimensionPixelSize(dimen);
    }

    public static int dimens2px(int... dimens) {
        int total = 0;
        for (int dimen : dimens) {
            total = total + dimen2px(dimen);
        }
        return total;
    }

    /**
     * 推荐使用Ui.kt中的spToPx()扩展方法
     */
    public static int spToPx(float spValue) {
        return (int) (spValue * DISPLAY_METRICS.scaledDensity + 0.5f);
    }

    public static float pxToDp(int px) {
        return px / DENSITY;
    }

    public static boolean isLandscape(Context context) {
        if (context == null) {
            return isLandscape(Utils.getApp().getResources());
        } else {
            return isLandscape(context.getResources());
        }
    }

    public static boolean isLandscape(Resources res) {
        return res.getConfiguration().orientation == ORIENTATION_LANDSCAPE;
    }


    /**
     * 这个比较准
     *
     * @return
     */
    public static int getScreenHeightWithCheckFullScreen(Context context) {
        return isFullScreen(context) ? getFullScreenHeight() : getScreenHeight(context);
    }

    public static int getFullScreenHeight() {
        WindowManager windowMgr = (WindowManager) Utils.getApp().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowMgr.getDefaultDisplay().getRealMetrics(dm);
        return dm.heightPixels;
    }

    public static int getFullScreenWidth() {
        WindowManager windowMgr = (WindowManager) Utils.getApp().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowMgr.getDefaultDisplay().getRealMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenWidth(Context context) {
        if (context == null) {
            context = Utils.getApp();
        }
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        if (context == null) {
            context = Utils.getApp();
        }
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static boolean isLandScape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    public static boolean isFullScreen(Context context) {
        return isFullScreen(context, 1.8f);
    }

    public static boolean isFullScreen(Context context, Float target) {
        int screenWidth = getScreenWidth(context);
        if (screenWidth != 0) {
            float pixels = getFullScreenHeight() / screenWidth;
            return pixels > target;
        }
        return false;
    }


    /**
     * @param textSize1 px
     * @param textSize2 px
     * @return 两个字号的基线差
     */
    public static float getTextBaselineDiff(float textSize1, float textSize2) {
        Paint p = new Paint();
        p.setTextSize(textSize1);
        float descent1 = p.getFontMetrics().descent;
        p.setTextSize(textSize2);
        return Math.abs(descent1 - p.getFontMetrics().descent);
    }
}

