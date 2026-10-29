package com.ludomaster.reference;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Local profile hub. All league and match actions reuse existing APIs. */
public final class LhjProfileActivity extends Activity {
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView label(String value,int size){
        TextView v=new TextView(this);
        v.setText(value);v.setTextSize(size);v.setTextColor(0xff172B54);
        v.setGravity(Gravity.CENTER);
        return v;
    }
    private Button button(String label, boolean primary){
        Button b=new Button(this);
        b.setText(label);b.setTextSize(16);b.setAllCaps(false);
        b.setTextColor(primary?Color.WHITE:0xff172B54);
        b.setTypeface(null,Typeface.BOLD);
        b.setBackgroundResource(primary?R.drawable.lhj_button_blue:R.drawable.lhj_button_white);
        return b;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        if(SecureLogin.token(this)==null){startActivity(new Intent(this,WebLoginActivity.class));finish();return;}
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root=new LinearLayout(this);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24),dp(28),dp(24),dp(24));
        root.setBackgroundResource(R.drawable.lhj_menu_sky);
        scroll.addView(root);
        ImageView emblem=new ImageView(this);
        emblem.setImageResource(R.drawable.lhj_emblem_approved);
        root.addView(emblem,new LinearLayout.LayoutParams(dp(136),dp(136)));
        root.addView(label("Ludo Ho Jaye!!!",28));
        String name=getSharedPreferences("LudoModUser",MODE_PRIVATE).getString("username","Player");
        TextView welcome=label("Welcome, "+name,20);
        welcome.setPadding(0,dp(18),0,dp(18));
        root.addView(welcome);
        Button leagues=button("Choose League & Play",true);
        root.addView(leagues,new LinearLayout.LayoutParams(-1,dp(58)));
        leagues.setOnClickListener(v->{
            Intent intent=new Intent(this,RoyalSetupActivity.class);
            intent.putExtra("computer",false);
            startActivity(intent);
        });
        Button saved=button("Saved Matches & Upload Status",false);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));
        p.topMargin=dp(14);root.addView(saved,p);
        saved.setOnClickListener(v->MatchUploader.showSavedMatches(this));
        Button logout=button("Sign Out",false);
        LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,dp(58));
        q.topMargin=dp(14);root.addView(logout,q);
        logout.setOnClickListener(v->LhjDialog.confirm(this,"Sign out?",
            "You can still play offline. Sign in again to access your leagues.",
            "Sign Out",()->{SecureLogin.clear(this);finish();}));
        Button back=button("Back to Main Menu",false);
        LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(-1,dp(58));
        a.topMargin=dp(14);root.addView(back,a);
        back.setOnClickListener(v->finish());
        setContentView(scroll);
    }
}
