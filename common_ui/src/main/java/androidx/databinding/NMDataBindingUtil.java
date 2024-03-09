package androidx.databinding;

import android.view.View;

/**
 * @author lishihui
 */

public class NMDataBindingUtil {
    public static <T extends ViewDataBinding> T bind(View[] roots, int layoutId) {
        return DataBindingUtil.bind(DataBindingUtil.getDefaultComponent(), roots, layoutId);
    }
}
