package com.ludomaster.reference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Reusable native Ludo Ho Jaye modal: Bootstrap-inspired card, accessible
 * touch targets and no WebView or external dependency.
 */
public final class LhjDialog {
    private static final int NAVY = 0xff172B54;
    private static final int BLUE = 0xff2586F5;
    private static final int LIGHT = 0xffF1F7FF;
    private LhjDialog() {}

    private static int dp(Activity activity, int n) {
        return Math.round(n * activity.getResources().getDisplayMetrics().density);
    }

    private static GradientDrawable rounded(Activity activity, int color, int border, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(activity, radius));
        if (border != 0) drawable.setStroke(dp(activity, 1), border);
        return drawable;
    }

    private static TextView text(Activity activity, String label, int size, int color, boolean bold) {
        TextView view = new TextView(activity);
        view.setText(label);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private static LinearLayout panel(Activity activity, String title) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 20), dp(activity, 22), dp(activity, 20), dp(activity, 18));
        card.setBackground(rounded(activity, Color.WHITE, 0xffC9E2FF, 24));
        TextView heading = text(activity, title, 22, NAVY, true);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(heading, new LinearLayout.LayoutParams(-1, dp(activity, 48)));
        View line = new View(activity);
        line.setBackgroundColor(0xffE4EDF8);
        LinearLayout.LayoutParams rule = new LinearLayout.LayoutParams(-1, dp(activity, 1));
        rule.bottomMargin = dp(activity, 12);
        card.addView(line, rule);
        return card;
    }

    private static Button button(Activity activity, String label, boolean primary) {
        Button button = new Button(activity);
        button.setText(label);
        button.setAllCaps(false);
        button.setTypeface(null, Typeface.BOLD);
        button.setTextSize(15);
        button.setTextColor(primary ? Color.WHITE : NAVY);
        button.setBackground(rounded(activity, primary ? BLUE : LIGHT, primary ? 0 : 0xffC9E2FF, 14));
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        return button;
    }

    private static AlertDialog create(Activity activity, LinearLayout card) {
        AlertDialog dialog = new AlertDialog.Builder(activity).setView(card).create();
        dialog.setOnShowListener(unused -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setLayout(
                    activity.getResources().getDisplayMetrics().widthPixels - dp(activity, 32),
                    WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        return dialog;
    }

    public static void choose(Activity activity, String title, String[] labels,
                              DialogInterface.OnClickListener onChoice) {
        LinearLayout card = panel(activity, title);
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(false);
        LinearLayout rows = new LinearLayout(activity);
        rows.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(rows);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, -2);
        scrollParams.weight = 1;
        card.addView(scroll, scrollParams);
        AlertDialog dialog = create(activity, card);
        int maxHeight = Math.round(activity.getResources().getDisplayMetrics().heightPixels * 0.53f);
        scroll.setMaximumHeight(maxHeight);
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            TextView row = text(activity, labels[i], 16, NAVY, false);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(activity, 16), dp(activity, 8), dp(activity, 16), dp(activity, 8));
            row.setBackground(rounded(activity, LIGHT, 0xffDAE8F7, 12));
            row.setMinHeight(dp(activity, 54));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.bottomMargin = dp(activity, 8);
            rows.addView(row, p);
            row.setOnClickListener(v -> {
                dialog.dismiss();
                onChoice.onClick(dialog, index);
            });
        }
        Button cancel = button(activity, "Cancel", false);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(activity, 48));
        cp.topMargin = dp(activity, 8);
        card.addView(cancel, cp);
        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void confirm(Activity activity, String title, String message,
                               String actionLabel, Runnable onAction) {
        LinearLayout card = panel(activity, title);
        TextView body = text(activity, message, 16, 0xff516481, false);
        body.setPadding(0, dp(activity, 8), 0, dp(activity, 18));
        card.addView(body);
        LinearLayout actions = new LinearLayout(activity);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(actions);
        Button cancel = button(activity, "Cancel", false);
        Button action = button(activity, actionLabel, true);
        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, dp(activity, 52), 1);
        left.rightMargin = dp(activity, 10);
        actions.addView(cancel, left);
        actions.addView(action, new LinearLayout.LayoutParams(0, dp(activity, 52), 1));
        AlertDialog dialog = create(activity, card);
        cancel.setOnClickListener(v -> dialog.dismiss());
        action.setOnClickListener(v -> { dialog.dismiss(); onAction.run(); });
        dialog.show();
    }
}
