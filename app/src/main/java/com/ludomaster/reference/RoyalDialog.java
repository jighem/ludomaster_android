package com.ludomaster.reference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** A single accessible royal blue/gold confirmation design for app and gameplay. */
public final class RoyalDialog {
    private RoyalDialog() {}
    public static void confirm(Activity activity,String title,String description,
                               String confirmLabel,Runnable onConfirm) {
        int gold=0xffffcd55;
        LinearLayout panel=new LinearLayout(activity);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(activity,26),dp(activity,24),dp(activity,26),dp(activity,24));
        GradientDrawable background=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xff122c58,0xff07142f});
        background.setCornerRadius(dp(activity,22));
        background.setStroke(dp(activity,3),gold);
        panel.setBackground(background);
        TextView heading=new TextView(activity);
        heading.setText(title);
        heading.setTextSize(24);
        heading.setTypeface(null,Typeface.BOLD);
        heading.setTextColor(gold);
        heading.setGravity(Gravity.CENTER);
        panel.addView(heading);
        TextView text=new TextView(activity);
        text.setText(description);
        text.setTextColor(Color.WHITE);
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        text.setPadding(0,dp(activity,14),0,dp(activity,20));
        panel.addView(text);
        Button cancel=new Button(activity);
        cancel.setText("CONTINUE PLAYING");
        cancel.setTextColor(Color.WHITE);
        cancel.setBackgroundTintList(ColorStateList.valueOf(0xff2259ad));
        panel.addView(cancel);
        Button exit=new Button(activity);
        exit.setText(confirmLabel);
        exit.setTextColor(0xff17233f);
        exit.setBackgroundTintList(ColorStateList.valueOf(gold));
        panel.addView(exit);
        AlertDialog dialog=new AlertDialog.Builder(activity).setView(panel).create();
        dialog.setOnShowListener(d -> {
            if(dialog.getWindow()!=null)
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        });
        cancel.setOnClickListener(v -> dialog.dismiss());
        exit.setOnClickListener(v -> { dialog.dismiss();onConfirm.run(); });
        dialog.show();
    }
    private static int dp(Activity activity,int value){
        return Math.round(value*activity.getResources().getDisplayMetrics().density);
    }
}
