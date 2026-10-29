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
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

/** Native replacement for original token, player-count and game-mode popups. */
public final class RoyalSetupActivity extends Activity {
    private boolean computer;
    private int count=2;
    private MatchVariant variant=MatchVariant.CLASSIC;
    private LinearLayout countPage,typePage,setupPage;
    private ScrollView setupScroll;
    private TextView variantTitle;
    private LinearLayout shuffleOptions;
    private int page;
    private boolean leaguePrompted;
    private final boolean[] rules={false,false,false};
    private final ArrayList<EditText> names=new ArrayList<>();
    private LinearLayout players;
    private int shuffleSeconds=300;
    private final ArrayList<Integer> playerIds=new ArrayList<>();
    private int selectedLeagueId=0;
    private String selectedLeagueName="";
    private JSONArray roster=new JSONArray();
    private Button leagueButton;
    private TextView countLabel;
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        computer=getIntent().getBooleanExtra("computer",false);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        setupScroll=scroll;
        LinearLayout root=new LinearLayout(this);
        root.setPadding(dp(26),dp(42),dp(26),dp(28));
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundResource(R.drawable.lhj_menu_sky);
        scroll.addView(root);
        ImageView shield=new ImageView(this);
        shield.setImageResource(R.drawable.lhj_emblem_approved);
        root.addView(shield,new LinearLayout.LayoutParams(dp(100),dp(110)));
        TextView heading=text(computer?"COMPUTER MATCH":"PASS & PLAY",27,0xff172B54);
        root.addView(heading);
        countPage=new LinearLayout(this);countPage.setOrientation(1);
        typePage=new LinearLayout(this);typePage.setOrientation(1);
        setupPage=new LinearLayout(this);setupPage.setOrientation(1);
        root.addView(countPage,new LinearLayout.LayoutParams(-1,-2));
        root.addView(typePage,new LinearLayout.LayoutParams(-1,-2));
        root.addView(setupPage,new LinearLayout.LayoutParams(-1,-2));
        countLabel=text("PLAYERS: 2",18,0xff172B54);
        countLabel.setPadding(0,dp(14),0,dp(6));countPage.addView(countLabel);
        LinearLayout nums=new LinearLayout(this);countPage.addView(nums);
        for(int n=2;n<=4;n++){
            final int selected=n;
            Button btn=button(n+"P");
            nums.addView(btn,new LinearLayout.LayoutParams(0,dp(58),1));
            btn.setOnClickListener(v->{count=selected;updatePlayers();});
        }
        Button next=button("CHOOSE GAME TYPE");
        countPage.addView(next,new LinearLayout.LayoutParams(-1,dp(58)));
        next.setOnClickListener(v->showPage(1));
        Button countBack=button("BACK");countPage.addView(countBack,new LinearLayout.LayoutParams(-1,dp(52)));
        countBack.setOnClickListener(v->finish());
        typePage.addView(text("CHOOSE GAME TYPE",22,0xff172B54));
        String[] descriptions={"The familiar race to bring all four pawns home.",
            "Homes, starts and finishing lanes swap on a timer.",
            "One bomb per pawn (four per player). Arm before rolling and leave one behind when moving. Any colour can trigger it. Hold a pawn to check its number and bombs."};
        for(MatchVariant choice:MatchVariant.values()) {
            Button option=button(choice.title);
            LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,dp(58));margin.topMargin=dp(14);
            typePage.addView(option,margin);
            typePage.addView(text(descriptions[choice.ordinal()],14,0xff172B54));
            option.setOnClickListener(v->{variant=choice;showPage(2);});
        }
        Button typeBack=button("BACK TO PLAYER COUNT");
        typePage.addView(typeBack,new LinearLayout.LayoutParams(-1,dp(52)));
        typeBack.setOnClickListener(v->showPage(0));
        variantTitle=text("",22,0xff172B54);setupPage.addView(variantTitle);
        leagueButton=button("SELECT LEAGUE PLAYERS (LOGIN REQUIRED)");
        setupPage.addView(leagueButton,new LinearLayout.LayoutParams(-1,dp(55)));
        leagueButton.setOnClickListener(v->chooseLeague());
        Button addPlayer=button("+ ADD PLAYER TO LEAGUE");
        setupPage.addView(addPlayer,new LinearLayout.LayoutParams(-1,dp(49)));
        addPlayer.setOnClickListener(v->LeaguePlayerCreator.show(this,selectedLeagueId,()->refreshRoster()));
        players=new LinearLayout(this);players.setOrientation(1);
        setupPage.addView(players,new LinearLayout.LayoutParams(-1,-2));
        TextView note=text("OPTIONAL RULES",19,0xff172B54);
        note.setPadding(0,dp(18),0,dp(8));setupPage.addView(note);
        String[] labels={"Combat scoring (+5 kill / -5 death)",
                "Earn and use immunity shields",
                "Guided Game (show legal moves and landing squares)"};
        for(int i=0;i<rules.length;i++){
            final int idx=i;
            CheckBox cb=new CheckBox(this);
            cb.setText(labels[i]);cb.setTextColor(0xff172B54);cb.setTextSize(15);
            cb.setButtonTintList(ColorStateList.valueOf(0xff2586F5));
            setupPage.addView(cb);
            cb.setOnCheckedChangeListener((button,checked)->rules[idx]=checked);
        }
        shuffleOptions=new LinearLayout(this);shuffleOptions.setOrientation(1);
        setupPage.addView(shuffleOptions,new LinearLayout.LayoutParams(-1,-2));
        shuffleOptions.addView(text("HOME SHUFFLE INTERVAL",14,0xff172B54));
        Spinner shuffleInterval=new Spinner(this);
        String[] intervals={"1 minute","2 minutes","3 minutes","5 minutes","10 minutes"};
        int[] values={60,120,180,300,600};
        ArrayAdapter<String> intervalAdapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,intervals);
        shuffleInterval.setAdapter(intervalAdapter);
        shuffleInterval.setSelection(3);
        shuffleInterval.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(android.widget.AdapterView<?> parent,android.view.View view,int position,long id){shuffleSeconds=values[position];}
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent){}
        });
        shuffleOptions.addView(shuffleInterval,new LinearLayout.LayoutParams(-1,dp(52)));
        shuffleOptions.addView(text("Each colour changes its home at a safe move boundary. Main-track pawns stay in place.",12,0xff172B54));
        Button play=button("START MATCH");
        setupPage.addView(play,new LinearLayout.LayoutParams(-1,dp(64)));
        play.setOnClickListener(v->startMatch());
        Button saved=button("SAVED MATCHES / RETRY UPLOAD");
        setupPage.addView(saved,new LinearLayout.LayoutParams(-1,dp(52)));
        saved.setOnClickListener(v->MatchUploader.showSavedMatches(this));
        Button back=button("BACK");
        setupPage.addView(back,new LinearLayout.LayoutParams(-1,dp(54)));
        setupPage.addView(text("Powered by ludohojaye.com",13,0xff172B54));
        back.setOnClickListener(v->showPage(1));
        updatePlayers();
        setContentView(scroll);
        // A signed-in player must pick the league before selecting its roster.
        // Offline guests can play without an account or league.
        showPage(0);
    }
    private void showPage(int selected) {
        page=selected;
        countPage.setVisibility(page==0?android.view.View.VISIBLE:android.view.View.GONE);
        typePage.setVisibility(page==1?android.view.View.VISIBLE:android.view.View.GONE);
        setupPage.setVisibility(page==2?android.view.View.VISIBLE:android.view.View.GONE);
        variantTitle.setText(variant.title+" • "+count+" players");
        shuffleOptions.setVisibility(variant==MatchVariant.HOME_SHUFFLE?
            android.view.View.VISIBLE:android.view.View.GONE);
        setupScroll.post(()->setupScroll.smoothScrollTo(0,0));
        if(page==2 && !leaguePrompted && SecureLogin.token(this)!=null) {
            leaguePrompted=true;setupScroll.post(this::chooseLeague);
        }
    }
    @Override public void onBackPressed() {
        if(page>0)showPage(page-1);else super.onBackPressed();
    }
    private TextView text(String label,int size,int color) {
        TextView t=new TextView(this);t.setText(label);t.setTextSize(size);t.setTextColor(color);
        t.setGravity(Gravity.CENTER);t.setTypeface(null,1);return t;
    }
    private Button button(String name){
        Button btn=new Button(this);btn.setText(name);btn.setTextColor(Color.WHITE);
        btn.setAllCaps(false);
        btn.setTypeface(null,android.graphics.Typeface.BOLD);
        btn.setBackgroundResource(R.drawable.lhj_button_blue);
        btn.setElevation(dp(3));
        return btn;
    }
    private void updatePlayers(){
        countLabel.setText("PLAYERS: "+count);
        players.removeAllViews();names.clear();playerIds.clear();
        for(int i=0;i<count;i++){
            LinearLayout playerCard=new LinearLayout(this);
            playerCard.setOrientation(LinearLayout.VERTICAL);
            playerCard.setPadding(dp(14),dp(12),dp(14),dp(12));
            playerCard.setBackgroundResource(R.drawable.lhj_card_white);
            playerCard.setElevation(dp(3));
            LinearLayout.LayoutParams cardParams=new LinearLayout.LayoutParams(-1,-2);
            cardParams.topMargin=dp(12);
            players.addView(playerCard,cardParams);
            TextView seatTitle=text("PLAYER "+(i+1)+(computer && i>0?"  -  COMPUTER":""),
                    14,0xff172B54);
            seatTitle.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            playerCard.addView(seatTitle,new LinearLayout.LayoutParams(-1,dp(30)));
            EditText field=new EditText(this);
            field.setSingleLine(true);field.setTextColor(0xff172B54);
            field.setHintTextColor(0xff64748B);
            field.setHint(computer?(i==0?"Your name":"Computer "+i):"Player "+(i+1));
            field.setText(computer?(i==0?"You":"Computer "+i):"Player "+(i+1));
            field.setBackgroundResource(R.drawable.lhj_button_white);
            field.setPadding(dp(12),0,dp(12),0);
            playerCard.addView(field,new LinearLayout.LayoutParams(-1,dp(52)));
            names.add(field);
            playerIds.add(0);
            if(SecureLogin.token(this)!=null && (!computer || i==0)) {
                final int seat=i;
                Button choose=button("Choose registered player");
                LinearLayout.LayoutParams pickParams=new LinearLayout.LayoutParams(-1,dp(48));
                pickParams.topMargin=dp(10);
                playerCard.addView(choose,pickParams);
                choose.setOnClickListener(v->choosePlayer(seat));
            }
        }
    }
    private void chooseLeague(){
        LeagueRoster.fetch(this,"leagues?status=active",new LeagueRoster.Callback(){
            @Override public void loaded(JSONArray data){
                if(data.length()==0){Toast.makeText(RoyalSetupActivity.this,"No accessible leagues",Toast.LENGTH_LONG).show();return;}
                String[] titles=new String[data.length()];
                for(int i=0;i<data.length();i++)titles[i]=data.optJSONObject(i).optString("name","League "+(i+1));
                LhjDialog.choose(RoyalSetupActivity.this,"Choose league",titles,(dialog,index)->{
                     JSONObject league=data.optJSONObject(index);
                     selectedLeagueId=league.optInt("id",0);
                     selectedLeagueName=league.optString("name","");
                     leagueButton.setText("LEAGUE: "+league.optString("name"));
                     roster=new JSONArray();
                     for(int j=0;j<playerIds.size();j++)playerIds.set(j,0);
                     // Discard identities from a previously selected league.
                     updatePlayers();
                     LeagueRoster.fetch(RoyalSetupActivity.this,"players?league_id="+selectedLeagueId+"&status=active&include_standard_computers=true",
                         new LeagueRoster.Callback(){
                            @Override public void loaded(JSONArray result){roster=result;Toast.makeText(RoyalSetupActivity.this,"Registered players loaded",Toast.LENGTH_SHORT).show();}
                            @Override public void failed(String error){Toast.makeText(RoyalSetupActivity.this,error,Toast.LENGTH_LONG).show();}
                         });
                  });
            }
            @Override public void failed(String error){Toast.makeText(RoyalSetupActivity.this,error,Toast.LENGTH_LONG).show();}
        });
    }
    private void refreshRoster() {
        if(selectedLeagueId<=0)return;
        LeagueRoster.fetch(this,"players?league_id="+selectedLeagueId+"&status=active",
            new LeagueRoster.Callback(){
                @Override public void loaded(JSONArray results){
                    roster=results;
                    Toast.makeText(RoyalSetupActivity.this,"League players refreshed",Toast.LENGTH_SHORT).show();
                }
                @Override public void failed(String message){
                    Toast.makeText(RoyalSetupActivity.this,message,Toast.LENGTH_LONG).show();
                }
            });
    }
    private void choosePlayer(int seat){
        if(selectedLeagueId<=0){chooseLeague();return;}
        ArrayList<JSONObject> eligible=new ArrayList<>();
        for(int i=0;i<roster.length();i++){
            JSONObject candidate=roster.optJSONObject(i);
            if(candidate==null || candidate.optInt("is_bot",0)==1 || candidate.optInt("league_id",-1)==0)continue;
            int id=candidate.optInt("id");
            if(id<=0)continue;
            boolean alreadyUsed=false;
            for(int j=0;j<playerIds.size();j++)if(j!=seat && playerIds.get(j)==id)alreadyUsed=true;
            if(!alreadyUsed)eligible.add(candidate);
        }
        if(eligible.isEmpty()){Toast.makeText(this,"No available registered players in this league",Toast.LENGTH_LONG).show();return;}
        String[] labels=new String[eligible.size()];
        for(int i=0;i<eligible.size();i++)labels[i]=eligible.get(i).optString("full_name","Player");
        LhjDialog.choose(this,"Select player for seat "+(seat+1),labels,(dialog,index)->{
                JSONObject selected=eligible.get(index);
                playerIds.set(seat,selected.optInt("id"));
                names.get(seat).setText(selected.optString("full_name"));
            });
    }
    private String name(int i){return names.get(i).getText().toString().trim();}
    private void startMatch(){
        if(SecureLogin.token(this)!=null && selectedLeagueId<=0){
            LhjDialog.confirm(this,"Choose league first",
                "Select the Ludo Ho Jaye league for this match before starting.",
                "Select League",this::chooseLeague);
            return;
        }
        for(int i=0;i<count;i++)if(name(i).isEmpty()){
            Toast.makeText(this,"Enter each player name",Toast.LENGTH_SHORT).show();return;
        }
        HashSet<Integer> mapped=new HashSet<>();
        for(int i=0;i<count;i++)if(playerIds.get(i)>0&&!mapped.add(playerIds.get(i))) {
            Toast.makeText(this,"Same league player cannot occupy two seats",Toast.LENGTH_LONG).show();return;
        }
        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra("league_id",selectedLeagueId);
        intent.putExtra("league_name",selectedLeagueName);
        int[] ids=new int[count];for(int i=0;i<count;i++)ids[i]=playerIds.get(i);
        intent.putExtra("league_player_ids",ids);
        String[] mappedNames=new String[count];for(int i=0;i<count;i++)mappedNames[i]=name(i);
        intent.putExtra("league_player_names",mappedNames);
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
        intent.putExtra("match_variant",variant.id);
        intent.putExtra("rule_shuffle",variant==MatchVariant.HOME_SHUFFLE);
        intent.putExtra("rule_guided",rules[2]);
        intent.putExtra("shuffle_interval_seconds",shuffleSeconds);
        startActivity(intent);
    }
}