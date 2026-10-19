package com.ludomaster.reference;

import android.app.Activity;

/** Consistent Ludo Ho Jaye confirmation UI used by game and setup screens. */
public final class RoyalDialog {
    private RoyalDialog() {}

    public static void confirm(Activity activity, String title, String description,
                               String confirmLabel, Runnable onConfirm) {
        LhjDialog.confirm(activity, title, description,
                "Continue Playing", confirmLabel, onConfirm);
    }
}
