package com.gooey.ui.util;

import android.view.View;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.gooey.ui.background.Builder;
import com.gooey.ui.background.Corner;
import com.gooey.ui.background.QR;
import com.gooey.ui.background.Stateful;

/**
 * @author lishihui01
 * @Date 2023/9/23
 * @Describe:
 */
public class BindingUtils {

    @BindingAdapter("fakeBoldText")
    public static void setFakeBoldText(TextView tv, boolean bold) {
        tv.getPaint().setFakeBoldText(bold);
        tv.invalidate();
    }

    @BindingAdapter(value = {"commonBackground", "commonCorner"},
            requireAll = false)
    public static void setCommonBackground(View v, Builder builder, Corner corner) {
        if (builder == null) {
            v.setBackground(null);
        } else if (corner == null) {
            v.setBackground(builder.build());
        } else {
            v.setBackground(QR.combine(builder, corner).build());
        }
    }

    @BindingAdapter(value = {"commonBackground", "commonRadius"},
            requireAll = false)
    public static void setCommonBackground(View v, Builder builder, float radius) {
        if (builder == null) {
            v.setBackground(null);
        } else if (radius <= 0f) {
            v.setBackground(builder.build());
        } else {
            v.setBackground(QR.combine(builder, Corner.only(radius)).build());
        }
    }

    @BindingAdapter(value = {
            "normalBackground",
            "pressedBackground",
            "disableBackground",
            "overlayBackground",
            "selectedBackground",
            "commonCorner"},
            requireAll = false)
    public static void setCommonBackground(
            View v,
            Builder normal,
            Builder pressed,
            Builder disable,
            Builder overlay,
            Builder selected,
            Corner corner) {
        Stateful stateful = new Stateful();
        stateful.setNormal(normal);
        stateful.setPressed(pressed);
        stateful.setDisable(disable);
        stateful.setOverlay(overlay);
        stateful.setSelected(selected);
        stateful.setCorner(corner);
        v.setBackground(stateful.build());
    }
}
