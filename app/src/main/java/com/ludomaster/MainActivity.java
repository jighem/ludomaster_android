package com.ludomaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.ludomaster.board.BoardView;
import com.ludomaster.game.ClassicGame;
import com.ludomaster.game.MatchOptions;

/** Offline pass-and-play prototype. No network access or external dependencies. */
public final class MainActivity extends Activity {
    private ClassicGame game;
    private BoardView board;
    private TextView status;
    private TextView dice;
    private Button rollButton;
    private final Button[] pawnButtons = new Button[4];
    private boolean combatEnabled, immunityEnabled, shieldPromptVisible, shuffleEnabled;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long nextShuffleAt;
    private final Runnable timerTick = new Runnable() {
        @Override public void run() {
            if (game != null && game.options.dynamicHomeShuffle && game.winner == -1) {
                long now = SystemClock.elapsedRealtime();
                if (now >= nextShuffleAt) {
                    game.requestHomeShuffle();
                    nextShuffleAt += ((now - nextShuffleAt) / 300000L + 1L) * 300000L;
                }
                render();
            }
            timerHandler.postDelayed(this, 1000L);
        }
    };
    @Override protected void onResume() { super.onResume(); timerHandler.removeCallbacks(timerTick); timerHandler.post(timerTick); }
    @Override protected void onPause() { timerHandler.removeCallbacks(timerTick); super.onPause(); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xfff3f6fa);
        root.setPadding(dp(12),dp(12),dp(12),dp(12));

        ImageView emblem = new ImageView(this);
        emblem.setImageResource(R.drawable.lm_shield);
        emblem.setContentDescription("Ludo Masters LM shield");
        LinearLayout.LayoutParams emblemLayout = new LinearLayout.LayoutParams(dp(72), dp(72));
        emblemLayout.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(emblem, emblemLayout);

        TextView title=new TextView(this);
        title.setText("LUDO MASTERS");
        title.setTextColor(0xff172b4d);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        status=new TextView(this);
        status.setTextColor(0xff243447);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0,dp(8),0,dp(8));
        root.addView(status);

        board=new BoardView(this);
        root.addView(board,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout pawnRow = new LinearLayout(this);
        pawnRow.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(pawnRow);
        for (int p=0;p<4;p++) {
            final int selected=p;
            Button button=new Button(this);
            button.setText("Pawn "+(p+1));
            pawnButtons[p]=button;
            pawnRow.addView(button,new LinearLayout.LayoutParams(0,dp(48),1));
            button.setOnClickListener(v->{if(game!=null && game.move(selected))render();});
        }

        dice=new TextView(this);
        dice.setGravity(Gravity.CENTER);
        dice.setTextSize(18);
        dice.setTextColor(Color.DKGRAY);
        root.addView(dice);

        rollButton=new Button(this);
        rollButton.setText("Roll dice");
        root.addView(rollButton);
        rollButton.setOnClickListener(v -> {
            if(game==null || game.winner!=-1)return;
            game.roll();render();
        });

        Button restart=new Button(this);
        restart.setText("New offline match");
        root.addView(restart);
        restart.setOnClickListener(v -> choosePlayers());

        setContentView(root);
        choosePlayers();
    }

    private void choosePlayers() {
        new AlertDialog.Builder(this)
            .setTitle("Start offline match")
            .setItems(new String[]{"2 players · Red vs Yellow","3 players · Red / Green / Yellow","4 players · All colours"},(dialog,choice)->{
                chooseRules(choice+2);
            })
            .setCancelable(game!=null)
            .show();
    }

    private void chooseRules(int playerCount) {
        boolean[] enabled = {combatEnabled, immunityEnabled, shuffleEnabled};
        new AlertDialog.Builder(this)
            .setTitle("Optional match rules")
            .setMultiChoiceItems(new String[]{"Combat scoring (+5 capture / -5 death)",
                "Earn immunity shields from captures",
                "Dynamic Home Shuffle (every 5 minutes)"}, enabled, (d, index, checked) -> enabled[index] = checked)
            .setPositiveButton("Start match", (d, which) -> {
                combatEnabled = enabled[0]; immunityEnabled = enabled[1]; shuffleEnabled = enabled[2];
                nextShuffleAt = SystemClock.elapsedRealtime() + 300000L;
                game = new ClassicGame(playerCount, new MatchOptions(combatEnabled, immunityEnabled, shuffleEnabled));
                board.bind(game, pawn -> {if (game.move(pawn)) render();});
                shieldPromptVisible = false;
                render();
            })
            .setNegativeButton("Back", (d, which) -> choosePlayers())
            .show();
    }

    private void render() {
        if(game==null)return;
        String turn=game.winner!=-1?"Winner: "+ClassicGame.NAMES[game.winner]:"Turn: "+ClassicGame.NAMES[game.turn];
        StringBuilder score = new StringBuilder();
        for (int c=0;c<4;c++) if (game.active[c]) {
            if (score.length()>0) score.append("  |  ");
            score.append(ClassicGame.NAMES[c]);
            if (game.options.combatScoring) score.append(" ").append(game.combatPoints(c)).append(" pts");
            if (game.options.immunity) score.append(" Shield:").append(game.shields(c));
        }
        String timerInfo = "";
        if(game.options.dynamicHomeShuffle && game.winner == -1) {
            long remaining = Math.max(0,(nextShuffleAt-SystemClock.elapsedRealtime()+999L)/1000L);
            timerInfo = "\nHome shuffle: "+(remaining/60)+":"+String.format(java.util.Locale.US,"%02d",remaining%60)+" · swaps: "+game.shuffleCount;
        }
        status.setText(turn+"\n"+game.message+"\n"+score+timerInfo);
        dice.setText(game.awaitingPawn?"Dice: "+game.die+" · Tap a highlighted pawn":game.winner!=-1?"Match finished":"Tap Roll dice");
        rollButton.setEnabled(!game.awaitingPawn && game.winner==-1);
        rollButton.setTextColor(Color.BLACK);
        for(int p=0;p<4;p++)pawnButtons[p].setEnabled(game.canMove(p));
        board.invalidate();
        if (game.pendingDefender != -1 && !shieldPromptVisible) {
            shieldPromptVisible = true;
            int defender = game.pendingDefender;
            new AlertDialog.Builder(this)
                .setTitle(ClassicGame.NAMES[defender] + " is under attack!")
                .setMessage("Use one immunity shield to prevent this capture? Pass the phone to the defender.")
                .setPositiveButton("Use shield", (d,which) -> {
                    shieldPromptVisible = false;
                    game.decideShield(true);
                    render();
                })
                .setNegativeButton("Accept capture", (d,which) -> {
                    shieldPromptVisible = false;
                    game.decideShield(false);
                    render();
                })
                .setCancelable(false).show();
        }
    }

    private int dp(float x) {return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
}
