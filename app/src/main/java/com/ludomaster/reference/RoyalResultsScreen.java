package com.ludomaster.reference;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Complete replacement for the old post-match panel. */
public final class RoyalResultsScreen {
    private RoyalResultsScreen(){}
    private static int dp(Activity a,int value){return Math.round(value*a.getResources().getDisplayMetrics().density);}
    private static TextView text(Activity a,String value,int size,int color){
        TextView t=new TextView(a);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setGravity(Gravity.CENTER);t.setPadding(dp(a,12),dp(a,10),dp(a,12),dp(a,10));
        return t;
    }
    private static Button action(Activity a,String label){
        Button b=new Button(a);b.setText(label);b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff135cb1));return b;
    }
    public static void show(Activity a,String summary,String league,boolean canUpload,String uuid){
        Dialog dialog=new Dialog(a);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ScrollView scroller=new ScrollView(a);
        LinearLayout root=new LinearLayout(a);root.setOrientation(1);
        root.setPadding(dp(a,22),dp(a,25),dp(a,22),dp(a,20));
        GradientDrawable backdrop=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xff102344,0xff142f65,0xff06152e});
        root.setBackground(backdrop);
        TextView title=text(a,"LUDO HO JAYE",29,0xffffd579);
        title.setTypeface(null,Typeface.BOLD);root.addView(title);
        TextView subtitle=text(a,"MATCH COMPLETED",22,Color.WHITE);root.addView(subtitle);
        root.addView(text(a,summary==null?"Match finished":summary,15,0xffe8f0ff));
        if(league!=null && !league.trim().isEmpty())
            root.addView(text(a,"Selected league: "+league,16,0xffffd579));
        root.addView(text(a,"Powered by ludohojaye.com",13,0xffffd579));
        TextView receipt=text(a,"Saved locally — not submitted",14,0xffffd579);
        root.addView(receipt);
        Button submit=action(a,"SUBMIT SCORE TO LUDOHOJAYE");
        submit.setEnabled(canUpload);
        Runnable refresh=()->{
            String status=MatchUploader.localStatus(a,uuid);
            receipt.setText(status);
            boolean uploaded=status.startsWith("Uploaded");
            submit.setEnabled(canUpload && !uploaded);
            submit.setText(uploaded?"ALREADY SUBMITTED":"SUBMIT SCORE TO LUDOHOJAYE");
        };
        submit.setOnClickListener(v->MatchUploader.prompt(a,uuid,refresh));
        refresh.run();
        root.addView(submit);
        if(!canUpload)root.addView(text(a,"Match could not be saved locally. Submission is unavailable.",13,0xffffaaaa));
        Button done=action(a,"DONE");
        done.setOnClickListener(v->{dialog.dismiss();a.finish();});
        root.addView(done);
        scroller.addView(root);
        dialog.setContentView(scroller);
        Window window=dialog.getWindow();
        if(window!=null){window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);}
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        if(dialog.getWindow()!=null)dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
    }
}
