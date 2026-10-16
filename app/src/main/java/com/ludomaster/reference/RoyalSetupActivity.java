package com.ludomaster.reference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import java.util.ArrayList;

/** Native replacement for original token, player-count and game-mode popups. */
public final class RoyalSetupActivity extends Activity {
    private boolean computer;
    private int count=2;
    private final boolean[] rules={false,false,false};
    private final ArrayList<EditText> names=new ArrayList<>();
    private LinearLayout players;
    private TextView countLabel;
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        computer=getIntent().getBooleanExtra("computer",false);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root=new LinearLayout(this);
        root.setPadding(dp(26),dp(42),dp(26),dp(28));
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundResource(R.drawable.ludo_masters_game_background);
        scroll.addView(root);
        ImageView shield=new ImageView(this);
        shield.setImageResource(R.drawable.lm_shield);
        root.addView(shield,new LinearLayout.LayoutParams(dp(100),dp(110)));
        TextView heading=text(computer?"COMPUTER MATCH":"PASS N PLAY",27,0xffffd46a);
        root.addView(heading);
        countLabel=text("PLAYERS: 2",18,Color.WHITE);
        countLabel.setPadding(0,dp(14),0,dp(6));root.addView(countLabel);
        LinearLayout nums=new LinearLayout(this);root.addView(nums);
        for(int n=2;n<=4;n++){
            final int selected=n;
            Button btn=button(n+"P");
            nums.addView(btn,new LinearLayout.LayoutParams(0,dp(58),1));
            btn.setOnClickListener(v->{count=selected;updatePlayers();});
        }
        players=new LinearLayout(this);players.setOrientation(1);
        root.addView(players,new LinearLayout.LayoutParams(-1,-2));
        TextView note=text("OPTIONAL RULES",19,0xffffd46a);
        note.setPadding(0,dp(18),0,dp(8));root.addView(note);
        String[] labels={"Combat scoring (+5 kill / -5 death)",
                "Earn and use immunity shields","Dynamic Home Shuffle (5 minutes)"};
        for(int i=0;i<3;i++){
            final int idx=i;
            CheckBox cb=new CheckBox(this);
            cb.setText(labels[i]);cb.setTextColor(Color.WHITE);cb.setTextSize(15);
            cb.setButtonTintList(ColorStateList.valueOf(0xffffd46a));
            root.addView(cb);
            cb.setOnCheckedChangeListener((button,checked)->rules[idx]=checked);
        }
        Button play=button("START MATCH");
        root.addView(play,new LinearLayout.LayoutParams(-1,dp(64)));
        play.setOnClickListener(v->startMatch());
        Button back=button("BACK");
        root.addView(back,new LinearLayout.LayoutParams(-1,dp(54)));
        back.setOnClickListener(v->finish());
        updatePlayers();
        setContentView(scroll);
    }
    private TextView text(String label,int size,int color) {
        TextView t=new TextView(this);t.setText(label);t.setTextSize(size);t.setTextColor(color);
        t.setGravity(Gravity.CENTER);t.setTypeface(null,1);return t;
    }
    private Button button(String name){
        Button btn=new Button(this);btn.setText(name);btn.setTextColor(Color.WHITE);
        btn.setBackgroundTintList(ColorStateList.valueOf(0xff0757bf));return btn;
    }
    private void updatePlayers(){
        countLabel.setText("PLAYERS: "+count);
        players.removeAllViews();names.clear();
        for(int i=0;i<count;i++){
            EditText field=new EditText(this);
            field.setSingleLine(true);field.setTextColor(Color.WHITE);
            field.setHintTextColor(0xffb6cbea);
            field.setHint(computer?(i==0?"Your name":"Computer "+i):"Player "+(i+1));
            field.setText(computer?(i==0?"You":"Computer "+i):"Player "+(i+1));
            players.addView(field,new LinearLayout.LayoutParams(-1,dp(52)));
            names.add(field);
        }
    }
    private String name(int i){return names.get(i).getText().toString().trim();}
    private void startMatch(){
        for(int i=0;i<count;i++)if(name(i).isEmpty()){
            Toast.makeText(this,"Enter each player name",Toast.LENGTH_SHORT).show();return;
        }
        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra("nop",count);
        intent.putExtra("type",computer?4:1);
        intent.putExtra("normalPiece",true);
        // Preserve player/colour slot expectations of the imported game engine.
        if(count==2) {
            intent.putExtra("color","blue");
            intent.putExtra("player3name",name(0));
            intent.putExtra("player2name",name(1));
            intent.putExtra("player2bot",computer);
        } else if(count==3){
            intent.putExtra("color","yellow");
            intent.putExtra("player1name",name(0));
            intent.putExtra("player2name",name(1));
            intent.putExtra("player3name",name(2));
            intent.putExtra("player1color","blue");
            intent.putExtra("player2color","red");
            intent.putExtra("player3color","green");
            intent.putExtra("player2bot",computer);
            intent.putExtra("player3bot",computer);
        } else {
            intent.putExtra("color","blue");
            intent.putExtra("player1name",name(0));
            intent.putExtra("player2name",name(1));
            intent.putExtra("player3name",name(2));
            intent.putExtra("player4name",name(3));
            intent.putExtra("player2bot",computer);
            intent.putExtra("player3bot",computer);
            intent.putExtra("player4bot",computer);
        }
        intent.putExtra("rule_combat",rules[0]);
        intent.putExtra("rule_immunity",rules[1]);
        intent.putExtra("rule_shuffle",rules[2]);
        startActivity(intent);
    }
}