package org.odk.share.views.ui.about;


import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * AboutItem: Data Wrapper for About Page Item.
 *
 * @author huangyz0918 (huangyz0918@gmail.com)
 */
public class AboutItem {

    private @StringRes
    int titleRes;
    private @DrawableRes
    int iconRes;

    public AboutItem(@StringRes int titleRes, @DrawableRes int iconRes) {
        this.titleRes = titleRes;
        this.iconRes = iconRes;
    }

    public int getTitle() {
        return titleRes;
    }

    public void setTitle(@StringRes int titleRes) {
        this.titleRes = titleRes;
    }

    public int getIcon() {
        return iconRes;
    }

    public void setIcon(@DrawableRes int iconRes) {
        this.iconRes = iconRes;
    }
}