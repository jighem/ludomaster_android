package com.ludomaster.reference;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlertDialog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private boolean combatScoringEnabled;
    private boolean immunityEnabled;
    private boolean homeShuffleEnabled;
    private int shuffleIntervalSeconds = 300;
    private com.ludomaster.reference.shuffle.HomeShuffleEngine homeShuffleEngine;
    private long nextShuffleDueAtMs;
    private TextView shuffleCountdownView;
    private boolean shuffleMatchEnding;
    private volatile int shuffleRepaintVersion;
    private int shuffleTurnGeneration;
    private final Runnable shuffleTimerTick = new Runnable() {
        @Override public void run() {
            if (!homeShuffleEnabled || homeShuffleEngine == null || shuffleMatchEnding) return;
            long now = android.os.SystemClock.elapsedRealtime();
            if (now >= nextShuffleDueAtMs) {
                homeShuffleEngine.requestShuffle();
                nextShuffleDueAtMs = now + (shuffleIntervalSeconds * 1000L);
            }
            updateShuffleCountdown(now);
            globalHandler.postDelayed(this, 1000);
        }
    };
    private final java.util.Map<String, Integer> killCounts = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> deathCounts = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> immunityShields = new java.util.HashMap<>();
    private final java.util.Map<String, String> offlinePlayerNames = new java.util.LinkedHashMap<>();
    private final java.util.Map<String,Integer> selectedLeaguePlayerIds = new java.util.HashMap<>();
    private int selectedLeagueId=0;
    private String selectedLeagueName="";
    private final java.util.Map<String, Integer> offlineFinishRanks = new java.util.HashMap<>();
    private long offlineMatchStartedAt;
    private String offlineMatchId;
    private boolean offlineResultSaved;
    private LinearLayout statsHud;
    private final java.util.Map<String,TextView> statsCells = new java.util.HashMap<>();
    private final java.util.Map<String,ImageView> statsAvatarViews = new java.util.HashMap<>();
    private final java.util.Map<Integer,ImageView> seatDiceViews = new java.util.HashMap<>();


    DisplayMetrics displayMetrics;
    double dpHeight;
    double dpWidth;
    double pxWidth;
    double pxHeight;
    ImageView ludoBoard,redHomeBlink,red1activecircle,red2activecircle,red3activecircle,red4activecircle;
    View step1,step2,step3,step4,step5,step6;
    ConstraintLayout completeBackground,redpiece1,redpiece2,redpiece3,redpiece4,greenpiece1,greenpiece2,greenpiece3,greenpiece4,bluepiece1,bluepiece2,bluepiece3,bluepiece4,yellowpiece1,yellowpiece2,yellowpiece3,yellowpiece4;

    float onePercentWidth,onePercentHeight;

    TextView playername1,playername2,playername3,playername4;

    EditText editText;

    int gametype = 1;

    int nop;

    double[] dx = {53.32,53.32,53.32,53.32,53.32,60.04,66.7,73.36,80.02,86.68,93.34,93.34,93.34,86.68,80.02,73.36,66.7 ,60.04,53.32,53.32,53.32,53.32,53.32,53.32,46.66,40   ,40   ,40   ,40   ,40   ,40
                  ,33.34,26.68,20.02,13.36,6.7  ,0.04 ,0.04 ,0.04,6.7  ,13.36,20.02,26.68,33.34, 40  , 40  , 40  , 40  , 40  , 40  ,46.66,53.32,46.66,46.66,46.66,46.66,46.66,46.66,86.68,80.02,73.36,66.7 ,60.04,53.38,46.66,46.66,46.66,46.66,46.66,46.66,6.66 ,13.32,19.98,26.64,33.3 ,39.96};
    double[] dy = { 6.66,13.32,19.98,26.64,33.3 , 40  , 40 , 40  ,  40 ,  40 ,  40 ,46.66,53.32,53.32,53.32,53.32,53.32,53.32,60.04,66.7 ,73.36,80.02,86.68,93.34,93.34,93.34,86.68,80.02,73.36,66.7 ,60.04
                  ,53.32,53.32,53.32,53.32,53.32,53.32,46.66, 40 , 40  , 40  , 40  , 40  , 40  ,33.34,26.68,20.02,13.36,6.7  ,0.04 ,0.04 ,0.04 ,6.66 ,13.32,19.98,26.64,33.3 ,39.96,46.66,46.66,46.66,46.66,46.66,46.66,86.68,80.02,73.36,66.7 ,60.04,53.38,46.66,46.66,46.66,46.66,46.66,46.66};

    float[] x = new float[dy.length];
    float[] y = new float[dy.length];

    float pushXForPieces;
    float pushYForPieces;

    ImageView mainDiceImageView,hintArrow;

    int reddicevalue,greendicevalue,bluedicevalue,yellowdicevalue;

    float pos1[][],pos2[][],pos3[][],pos4[][];

    float pieceWidth,pieceHeight;
    float boardTokenScale = 1f;

    Drawable redCircle,greenCircle,blueCircle,yellowCircle;

    List<Player> players;

    static String currentPlayerColor = "";
    static  int currentPlayerPosition = 3;

    static int currentPlayerDice = 6;

    int currentPlayerIndex = 0;

    String currentPlayerName;

    int currentPlayerSelectedIndex = 0;

    List<Piece> rp,gp,bp,yp;

    int[] wBlocks52,wBlocks58,wBlocks64,wBlocks70;

    static boolean isDiceMovableExtraChance = false;
    Dice d;
    private final DiceRules diceRules = new DiceRules(new java.security.SecureRandom());
    private final BoardAwareDiceEngine boardAwareDiceEngine = new BoardAwareDiceEngine(60);
    private final java.util.Random botTieBreaker = new java.security.SecureRandom();
    private BoardAwareDiceEngine.Mode diceMode = BoardAwareDiceEngine.Mode.BALANCED_AGGRESSIVE;
    private final java.util.Map<String,Integer> homeRollMisses = new java.util.HashMap<>();
    private TextView centralDiceResultView;
    private final Runnable hideCentralDiceResult = () -> {
        if (centralDiceResultView != null) centralDiceResultView.setVisibility(View.GONE);
    };

    MediaPlayer gameStartSound,diceRollSound,stepSound,safeSound,deathSound,pantaSound,congratulationSound;
    int sizeOfBox;

    float blockSize;

    Piece autoMove;

    HashSet<Integer> safeSpots;

    boolean normalPiece = true;

    private AnimatorSet[] animatorSets;

    ImageView shieldIndex1,shieldIndex2,shieldIndex3,shieldIndex4;

    int currentWinnerPosition = 1;

    // ingame menu button and items
    ConstraintLayout ingamemenuitemslayout;
    ImageView ingamemenubtn;
    TextView menuremoveplayersbtn, menuexitbtn;

    // ingameexitgamelayout
    ConstraintLayout quitgamelayout;
    ImageView ingameyesbtn,ingamenobtn,ingamesoundbtn,ingamemusicbtn;

    // ingame rmp layout
    ConstraintLayout ingamermplayout,rmp1bg,rmp2bg,rmp3bg,rmp4bg;
    ImageView rmpp1piece,rmpp2piece,rmpp3piece,rmpp4piece,rmpp1removeicon,rmpp2removeicon,rmpp3removeicon,rmpp4removeicon,rmpbackbtn,rmpmenubtn;
    TextView rmpp1name, rmpp2name,rmpp3name,rmpp4name;

    // ingmae rmp sublayout confirmrmplayout
    ConstraintLayout confirmrmplayout;
    ImageView selectedrmppiece,confirmrmpyesbtn,confirmrmpnobtn;

    int selectedconfirmrmpplayerindex = -1;

    int rmpindexforp1=1,rmpindexforp2=2,rmpindexforp3=3,rmpindexforp4=4;

    // players exit boxes
    ImageView p1exitbox,p2exitbox,p3exitbox,p4exitbox;
    String player1color,player2color,player3color,player4color;
    String player1name,player2name,player3name,player4name;

    int currentWinnerPlayerIndex = -1;

    // Congratulations screen
    ConstraintLayout congratulationslayout;
    ImageView congratsmenubtn,congratssoundbtn,congratssharebtn,congratsreplaybtn;

    ConstraintLayout winnerlistp1layout,winnerlistp2layout,winnerlistp3layout,winnerlistp4layout;
    ImageView wlistshield1,wlistshield2,wlistshield3,wlistshield4,wlistpiece1,wlistpiece2,wlistpiece3,wlistpiece4,wlistwinorlose1,wlistwinorlose2,wlistwinorlose3,wlistwinorlose4;
    TextView wlistname1,wlistname2,wlistname3,wlistname4;

    boolean isbluegreenreadytowin=false,isredyellowreadytowin=false;

    Handler globalHandler;

    SharedPreferences sharedPreferences;

    boolean isSoundOn=true,isMusicOn=false;

    int botwins=0,botloses=0;

    ImageView gameStartImageView;

    class Piece
    {
        // managed by constructor
        String colour;
        ConstraintLayout piece;
        ImageView readyToPick;
        int startPosition;

        int numberOfSteps = 0;

        int endPosition;
        float defX;
        float defY;

        int diceValue;
        int stepsUntilShuffleEntrance = 50;

        // managed at runtime
        boolean isAlive = false;
        boolean isClickable = true;
        boolean isBotPiece;
        boolean isReadyToEnterWinnerZone = false;
        int currBlock = -1;

        boolean hasCompletedItsPurpose = false;

        int currWinnerBlock = 0;

        int[] winnerBlocks;

        ImageView pieceIcon,pieceStandIcon;
        boolean isThisPlayerWon = false;


        private final ObjectAnimator rotateAnimator;

        Piece(String color,ConstraintLayout piece,ImageView readyToPick,ImageView pieceIcon,ImageView pieceStandIcon,int startPosition,float defX,float defY,boolean isBotPiece)
        {
            this.colour = color;
            this.piece = piece;
            this.readyToPick = readyToPick;
            this.pieceIcon = pieceIcon;
            this.pieceStandIcon = pieceStandIcon;
            this.startPosition = startPosition;
            this.defX = defX;
            this.defY = defY;
            this.isBotPiece = isBotPiece;
            this.piece.setPivotX(pieceWidth / 2f);
            this.piece.setPivotY(pieceHeight / 2f);
            this.piece.setScaleX(boardTokenScale);
            this.piece.setScaleY(boardTokenScale);
            this.piece.setTranslationX(defX);
            this.piece.setTranslationY(defY);
            this.readyToPick.setVisibility(View.INVISIBLE);
            this.piece.setVisibility(View.VISIBLE);
            this.endPosition = startPosition!=0?startPosition-2:50;
            if(!isBotPiece) {
                this.piece.setOnClickListener(view -> chooseStackedPieceOrMove(this));
            }
            if(!normalPiece) {
                pieceIcon.setScaleX(0.8f);pieceIcon.setScaleY(0.8f);
                pieceIcon.setImageDrawable(getStylishIconDrawableByColor(this.colour));
                pieceStandIcon.setVisibility(GONE);
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) readyToPick.getLayoutParams();
                params.topMargin = 0;
                params.topToTop = pieceIcon.getId();
                params.leftToLeft = pieceIcon.getId();
                params.bottomToBottom = pieceIcon.getId();
                params.rightToRight = pieceIcon.getId();
                this.readyToPick.setLayoutParams(params);
                readyToPick.setScaleX(1.2f);readyToPick.setScaleY(1.2f);
            }

            /*piece.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    die();
                    return false;
                }
            });*/
            rotateAnimator = ObjectAnimator.ofFloat(readyToPick,"rotation",360,0);
            rotateAnimator.setDuration(900);
            rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
            rotateAnimator.setInterpolator(new LinearInterpolator());
            rotateAnimator.start();
        }

        void onClickForBot() {
            if (currentPlayerDice < 1 || !otherwiseLegal(this,currentPlayerDice)
                    || blockedByOwnPawn(this,currentPlayerDice)) return;
            if (isAlive && isClickable && currentPlayerColor.equals(colour)) {
                diceValue = currentPlayerDice;
                currentPlayerDice = -1;
                for (Piece p : getPiecesByColor(colour)) {
                    p.inactiveState();
                }
                checkAdjustments(currBlock);
                move(diceValue);
                //Toast.makeText(MainActivity.this, x+"", Toast.LENGTH_SHORT).show();
            } else if (!isAlive && currentPlayerColor.equals(colour) && currentPlayerDice == 6) {
                currentPlayerDice = -1;
                for (Piece p : getPiecesByColor(colour)) {
                    p.inactiveState();
                }
                makeAlive();
            }
        }

        void makeAlive()
        {
            isAlive = true;
            currBlock = startPosition;
            stepsUntilShuffleEntrance = com.ludomaster.reference.shuffle.HomeShuffleEngine.forwardDistance(
                startPosition, endPosition);
            piece.animate().translationX(x[startPosition]+pushXForPieces).translationY(y[startPosition]-pushYForPieces).setDuration(400).start(); // 16 75
            globalHandler.postDelayed(() -> {
                isDiceMovableExtraChance = true;
                if(!isBotPiece) { hintArrow.setVisibility(View.VISIBLE); }
                checkAdjustments(currBlock);
                if(currBlock>=24 && currBlock<=51) {
                    piece.setElevation(51-currBlock);
                } else if(currBlock>=64 && currBlock<=69) {
                    piece.setElevation(70-currBlock);
                } else {
                    piece.setElevation(currBlock);
                }
            },400);
            final String botColour = colour;
            final int expectedTurn = shuffleTurnGeneration;
            globalHandler.postDelayed(() -> {
                if (isBotPiece && !shuffleMatchEnding && d != null &&
                    expectedTurn == shuffleTurnGeneration &&
                    botColour.equals(currentPlayerColor)) d.roll();
            },550);
        }

        void die()
        {
            isAlive = false;
            isClickable = false;
            isReadyToEnterWinnerZone = false;
            checkAdjustments(currBlock);
            currWinnerBlock=0;
            numberOfSteps = 0;
            if (homeShuffleEnabled && homeShuffleEngine != null) {
                // Captured pawns must return to their colour's current home;
                // do not reverse-walk through an obsolete start square.
                currBlock = -1;
                stepsUntilShuffleEntrance = 50;
                piece.animate().cancel();
                piece.setScaleX(boardTokenScale);
                piece.setScaleY(boardTokenScale);
                piece.animate().translationX(defX).translationY(defY).setDuration(350).start();
                return;
            }
            if(isSoundOn) {
                deathSound = MediaPlayer.create(MainActivity.this, R.raw.death);
                deathSound.start();
                deathSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        deathSound.release();
                    }
                });
            }


            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if(currBlock!=startPosition)
                    {
                        if(currBlock==0) currBlock=51; else --currBlock;
                        piece.setTranslationX(x[currBlock]+pushXForPieces);
                        piece.setTranslationY(y[currBlock]-pushYForPieces);
                        globalHandler.postDelayed(this,20);
                    } else {
                        if(piece.getScaleX()<1.0f) {
                            piece.setScaleX(1.0f);
                            piece.setScaleY(1.0f);
                        }
                        piece.animate().translationX(defX).translationY(defY).setDuration(400).start();
                        globalHandler.removeCallbacks(this);
                    }
                }
            };
            globalHandler.post(r);
            //piece.animate().translationX(defX).translationY(defY).setDuration(400).start();
        }

        void activeState()
        {
            isClickable = true;
            piece.bringToFront();
            piece.setElevation(hudDp(18));
            piece.setClickable(true);
            rotateAnimator.start();
            readyToPick.setVisibility(View.VISIBLE);
            piece.setScaleX(boardTokenScale);
            piece.setScaleY(boardTokenScale);
            if (currBlock >= 0) layoutTokenSquare(currBlock);
        }

        void inactiveState()
        {
            isClickable = false;
            rotateAnimator.cancel();
            readyToPick.setVisibility(View.INVISIBLE);
            if(currBlock!=-1) {
                checkAdjustments(currBlock);
            }
        }

        void move(int n) {
            isClickable = false;
            if(stepSound!=null) {
                if(isSoundOn) {
                    if (!stepSound.isPlaying()) {
                        stepSound.seekTo(120);
                        stepSound.start();
                    } else if (stepSound.isPlaying()) {
                        stepSound.pause();
                        stepSound.seekTo(120);
                        stepSound.start();
                    }
                }
            }



            if (currBlock == -1) {
                currBlock = startPosition;
            }

            if (homeShuffleEnabled && homeShuffleEngine != null) {
                int previous = currBlock;
                if (isReadyToEnterWinnerZone) {
                    if (winnerBlocks == null || currWinnerBlock < 0 || currWinnerBlock >= 6)
                        throw new IllegalStateException("Invalid finishing lane step");
                    // Lane positions are always indexed using this pawn's CURRENT
                    // home assignment, never its pre-shuffle starting colour.
                    winnerBlocks = getWinnerBlocks(endPosition);
                    currBlock = winnerBlocks[currWinnerBlock++];
                } else {
                    currBlock = (currBlock + 1) % 52;
                    if (stepsUntilShuffleEntrance > 0) --stepsUntilShuffleEntrance;
                    if (stepsUntilShuffleEntrance == 0) {
                        isReadyToEnterWinnerZone = true;
                        winnerBlocks = getWinnerBlocks(endPosition);
                    }
                }
                if (previous >= 0 && previous < 52) checkAdjustments(previous);
            } else {
                if(currBlock>=51) currBlock = 0;
                else currBlock++;
                if(currBlock==0) { checkAdjustments(51); } else { checkAdjustments(currBlock-1); }
                if(isReadyToEnterWinnerZone) {
                    currBlock = winnerBlocks[currWinnerBlock];
                    currWinnerBlock++;
                }
                if(currBlock==endPosition) {
                    winnerBlocks = getWinnerBlocks(endPosition);
                    isReadyToEnterWinnerZone = true;
                }
            }

            piece.animate().translationX(x[currBlock] + pushXForPieces).translationY(y[currBlock] - pushYForPieces).setDuration(220).start();
            /*if(!isReadyToEnterWinnerZone)
            {
                piece.animate().translationX(x[currBlock] + pushXForPieces).translationY(y[currBlock] - pushYForPieces).setDuration(300).start();
            } else {
                piece.animate().translationX(x[winnerBlocks[currWinnerBlock]] + pushXForPieces).translationY(y[winnerBlocks[currWinnerBlock]] - pushYForPieces).setDuration(300).start();
            }*/

            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.4f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.4f);
            ObjectAnimator popUpAnimator = ObjectAnimator.ofPropertyValuesHolder(piece, scaleX, scaleY);
            popUpAnimator.setDuration(100);
            scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.4f, 1.0f);
            scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.4f, 1.0f);
            ObjectAnimator popDownAnimator = ObjectAnimator.ofPropertyValuesHolder(piece, scaleX, scaleY);
            popDownAnimator.setDuration(90);

            /*ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f);
            alphaAnimator.setDuration(duration);*/

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(popUpAnimator,popDownAnimator);
            animatorSet.start();




            globalHandler.postDelayed(() -> {
                if(n>1) {
                    int step = currBlock;
                    globalHandler.postDelayed( ()-> {
                        showStep(n,this.colour,x[step],y[step]);
                    },100);
                    numberOfSteps++;
                    move(n - 1);
                } else {
                    showStep(n,this.colour,x[currBlock],y[currBlock]);
                    isThisPlayerWon = false;
                    if(currBlock>=24 && currBlock<=51) {
                        piece.setElevation(51-currBlock);
                    } else if(currBlock>=64 && currBlock<=69) {
                        piece.setElevation(70-currBlock);
                    } else {
                        piece.setElevation(currBlock);
                    }
                    stepSound.pause();
                    numberOfSteps++;

                    boolean isDeadChanceAvailable = false;

                    if(safeSpots.contains(currBlock) && isSoundOn)
                    {
                        safeSound = MediaPlayer.create(MainActivity.this,R.raw.safe);
                        safeSound.start();
                        safeSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                safeSound.release();
                            }
                        });
                    } else if(currWinnerBlock>0) {
                        int temp = 0;
                        if(currWinnerBlock>5)
                        {
                            hasCompletedItsPurpose = true;
                            if(isSoundOn) {
                                pantaSound = MediaPlayer.create(MainActivity.this, R.raw.panta);
                                pantaSound.start();
                                pantaSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        pantaSound.release();
                                    }
                                });
                            }

                            List<Piece> pieces = getPiecesByColor(this.colour);
                            for(Piece p: pieces) { if(p.hasCompletedItsPurpose) { temp++; } }

                            int pantaValue = 4;
                            if(gametype==3) { pantaValue = 1; }

                            if(temp>=pantaValue) { //4
                                isThisPlayerWon = true;
                                isDeadChanceAvailable = isDiceMovableExtraChance = false;
                                if(players.size()==2) {
                                    int x;
                                    if(currentPlayerIndex==0) { x=players.size()-1; } else { x=currentPlayerIndex-1; }
                                    if(gametype!=3) {
                                        globalHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                checkPantaAdjustments(colour, currBlock);
                                            }
                                        }, 50);
                                    }
                                    redHomeBlink.clearAnimation();
                                    redHomeBlink.setVisibility(GONE);
                                    mainDiceImageView.setVisibility(GONE);
                                    players.remove(x);
                                    if(gametype==2) {
                                        for(Piece p: pieces) { p.piece.setVisibility(GONE); }
                                        if(colour.equals("red") || colour.equals("yellow")) {
                                            if(isredyellowreadytowin) {
                                                setRedYellowTeamAsWinners();
                                            } else { isredyellowreadytowin = true; }
                                        } else {
                                            if(isbluegreenreadytowin) {
                                                stopEverything();
                                                setBlueGreenTeamAsWinners();
                                                ((TextView)findViewById(R.id.team1name1)).setText(player3name);
                                                ((TextView)findViewById(R.id.team1name2)).setText(player2name);
                                                ((TextView)findViewById(R.id.team2name1)).setText(player1name);
                                                ((TextView)findViewById(R.id.team2name2)).setText(player4name);
                                                showGameOverScreen();
                                                mainDiceImageView.setVisibility(GONE);
                                                hintArrow.setVisibility(GONE);
                                            } else { isbluegreenreadytowin = true; }
                                        }
                                        return;
                                    } else {
                                        setCurrentPlayerAsWinnerAtCurrentPosition(currentPlayerSelectedIndex, colour, currentPlayerName);
                                    }
                                    String name = players.get(0).name,colour = players.get(0).color;
                                    int loserplayerselectedindex = players.get(0).index;

                                    if(gametype==4) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        if(loserplayerselectedindex!=0) {
                                            botwins += 1;
                                            editor.putInt("botwins",botwins);
                                        } else {
                                            botloses += 1;
                                            editor.putInt("botloses",botloses);
                                        }
                                        ((TextView)findViewById(R.id.userwins)).setText((botwins+""));
                                        ((TextView)findViewById(R.id.userloses)).setText((botloses+""));

                                        editor.apply();
                                    }

                                    hideThisPlayerDiceBg(loserplayerselectedindex);
                                    if(nop==2) {
                                        wlistshield2.setVisibility(View.INVISIBLE);
                                        wlistname2.setText(name);
                                        wlistpiece2.setImageDrawable(getPieceDrawableByColor(colour));
                                        winnerlistp3layout.setVisibility(GONE);
                                        winnerlistp4layout.setVisibility(GONE);
                                    } else if(nop==3) {
                                        //makeThisPlayerLoserBasedOnSelectedIndexNOP3(colour,loserplayerselectedindex);
                                        wlistshield3.setVisibility(View.INVISIBLE);
                                        wlistname3.setText(getLoserNameBasedOnSelectedIndex(loserplayerselectedindex));
                                        wlistpiece3.setImageDrawable(getPieceDrawableByColor(colour));
                                        winnerlistp4layout.setVisibility(GONE);
                                    } else {
                                        wlistshield4.setVisibility(View.INVISIBLE);
                                        wlistname4.setText(getLoserNameBasedOnSelectedIndex(loserplayerselectedindex));
                                        wlistpiece4.setImageDrawable(getPieceDrawableByColor(colour));
                                    }
                                    showGameOverScreen();
                                    return;
                                } else {
                                    int x;
                                    if (currentPlayerIndex == 0) {
                                        x = players.size() - 1;
                                    } else {
                                        x = currentPlayerIndex - 1;
                                    }
                                    currentWinnerPlayerIndex = currentPlayerIndex;
                                    //checkAdjustments(currBlock);
                                    if(gametype==2) {
                                        for(Piece p: pieces) { p.piece.setVisibility(GONE); }
                                        if(colour.equals("red") || colour.equals("yellow")) {
                                            if(isredyellowreadytowin) {
                                                setRedYellowTeamAsWinners();
                                            } else { isredyellowreadytowin = true; }
                                        } else {
                                            if(isbluegreenreadytowin) {
                                                stopEverything();
                                                setBlueGreenTeamAsWinners();
                                                ((TextView)findViewById(R.id.team1name1)).setText(player3name);
                                                ((TextView)findViewById(R.id.team1name2)).setText(player2name);
                                                ((TextView)findViewById(R.id.team2name1)).setText(player1name);
                                                ((TextView)findViewById(R.id.team2name2)).setText(player4name);
                                                showGameOverScreen();
                                                mainDiceImageView.setVisibility(GONE);
                                                hintArrow.setVisibility(GONE);
                                            } else { isbluegreenreadytowin = true; }
                                        }
                                    } else {
                                        setCurrentPlayerAsWinnerAtCurrentPosition(currentPlayerSelectedIndex, colour, currentPlayerName);
                                    }

                                    currentPlayerIndex = x;
                                    menuremoveplayersbtn.setVisibility(GONE);
                                    players.remove(currentPlayerIndex);
                                    if (currentPlayerIndex == players.size()) {
                                        currentPlayerIndex=0;
                                    }
                                    //Toast.makeText(MainActivity.this, currentPlayerIndex + "", Toast.LENGTH_SHORT).show();
                                    switchPlayers();
                                    if(gametype!=3) {
                                        globalHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                checkPantaAdjustments(colour,currBlock);
                                            }
                                        }, 50);
                                    }
                                    d.isDiceClickable = true;
                                    return;
                                }
                            }
                        }
                    } else {
                        // Delay turn settlement while the defender chooses whether
                        // to spend a shield. Captures are never settled in advance.
                        MainActivity.this.resolveCapture(this, currBlock,
                            captured -> MainActivity.this.finishPieceMove(this, captured));
                        return;
                    }
                    MainActivity.this.finishPieceMove(this, false);
                }
            }, 200); //325
        }



        public boolean check(int diceValue) {
            if (blockedByOwnPawn(this,diceValue)) {
                inactiveState();
                return false;
            }
            if (homeShuffleEnabled && homeShuffleEngine != null) {
                int remaining = isReadyToEnterWinnerZone ?
                    (6 - currWinnerBlock) : (stepsUntilShuffleEntrance + 6);
                // A six-step roll crossing the entry square continues into
                // the six-square finishing lane, provided exact finish is not overshot.
                boolean allowed = !hasCompletedItsPurpose && !isThisPlayerWon &&
                    (isAlive ? (diceValue <= remaining) : diceValue == 6);
                if (allowed) activeState(); else inactiveState();
                return allowed;
            }
            if(diceValue == 6)
            {
                if(!isAlive || (numberOfSteps+diceValue)<57)
                {
                    activeState();
                    return true;
                } return false;
            } else if(isAlive && (numberOfSteps+diceValue)<57 && !hasCompletedItsPurpose && !isThisPlayerWon) {
                activeState();
                return true;
            } else {
                inactiveState();
                return false;
            }
        }
    }

    private void setBlueGreenTeamAsWinners() {
        ((ImageView)findViewById(R.id.imageView132)).setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.team1header,null));

        ((ImageView)findViewById(R.id.imageView137)).setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.team2header,null));

        ((ImageView)findViewById(R.id.imageView133)).setImageDrawable(getPieceDrawableByColor("blue"));
        ((ImageView)findViewById(R.id.imageView135)).setImageDrawable(getPieceDrawableByColor("green"));
        ((ImageView)findViewById(R.id.imageView138)).setImageDrawable(getPieceDrawableByColor("red"));
        ((ImageView)findViewById(R.id.imageView140)).setImageDrawable(getPieceDrawableByColor("yellow"));

        ((TextView)findViewById(R.id.team1name1)).setText(player3name);
        ((TextView)findViewById(R.id.team1name2)).setText(player2name);
        ((TextView)findViewById(R.id.team2name1)).setText(player1name);
        ((TextView)findViewById(R.id.team2name2)).setText(player4name);
    }

    private void setRedYellowTeamAsWinners() {
        stopEverything();
        showGameOverScreen();
        mainDiceImageView.setVisibility(GONE);
        hintArrow.setVisibility(GONE);

        ((ImageView)findViewById(R.id.imageView132)).setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.team2header,null));

        ((ImageView)findViewById(R.id.imageView137)).setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.team1header,null));

        ((ImageView)findViewById(R.id.imageView133)).setImageDrawable(getPieceDrawableByColor("red"));
        ((ImageView)findViewById(R.id.imageView135)).setImageDrawable(getPieceDrawableByColor("yellow"));
        ((ImageView)findViewById(R.id.imageView138)).setImageDrawable(getPieceDrawableByColor("blue"));
        ((ImageView)findViewById(R.id.imageView140)).setImageDrawable(getPieceDrawableByColor("green"));

        ((TextView)findViewById(R.id.team1name1)).setText(player1name);
        ((TextView)findViewById(R.id.team1name2)).setText(player4name);
        ((TextView)findViewById(R.id.team2name1)).setText(player3name);
        ((TextView)findViewById(R.id.team2name2)).setText(player2name);
    }

    private String getLoserNameBasedOnSelectedIndex(int loserplayerselectedindex) {
        switch (loserplayerselectedindex+1) {
            case 1: return player3name;
            case 2: return player1name;
            case 3: return player2name;
            case 4: return player4name;
        }
        return player3name;
    }

    private void showGameOverScreen() {
        saveOfflineMatchResult();
        congratulationslayout.setVisibility(View.GONE);
        String winner = "Winner pending";
        for (java.util.Map.Entry<String,Integer> rank : offlineFinishRanks.entrySet()) {
            if (rank.getValue()==1) {
                winner="WINNER: "+offlinePlayerNames.getOrDefault(rank.getKey(),rank.getKey());
                break;
            }
        }
        RoyalResultsScreen.show(this,winner+"\n"+combatSummary(),selectedLeagueName,offlineResultSaved,offlineMatchId);
        // Result and upload controls stay on the non-dismissible RoyalResultsScreen.


        if(isSoundOn) {
            congratulationSound = MediaPlayer.create(this, R.raw.congratulations);
            congratulationSound.setLooping(true);
            congratulationSound.start();
        }

        ImageView imageView = findViewById(R.id.fireworksview);
        Glide.with(this)
                .asGif()
                .load(R.drawable.fireworksimg)
                .into(new DrawableImageViewTarget(imageView) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (resource instanceof GifDrawable) {
                            GifDrawable gifDrawable = (GifDrawable) resource;
                            gifDrawable.setLoopCount(GifDrawable.LOOP_FOREVER);
                            gifDrawable.start();
                        }
                    }
                }.getView());
    }

    private void makeThisPlayerLoserBasedOnSelectedIndexNOP3(String colour, int loserplayerselectedindex) {
        switch ((loserplayerselectedindex+1)) {
            case 1:
                if(nop==3) {
                    wlistshield3.setVisibility(View.INVISIBLE);
                    wlistpiece3.setImageDrawable(getPieceDrawableByColor(colour));
                    wlistname3.setText(player3name);
                    wlistwinorlose1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.loser1, null));
                }
                hideThisPlayerDiceBg(1);
                break;
            case 2:
                if(nop==3) {
                    wlistshield3.setVisibility(View.INVISIBLE);
                    wlistpiece3.setImageDrawable(getPieceDrawableByColor(colour));
                    wlistname3.setText(player1name);
                    wlistwinorlose1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.loser1, null));
                }
                hideThisPlayerDiceBg(2);
                break;
            case 3:
                if(nop==3) {
                    wlistshield3.setVisibility(View.INVISIBLE);
                    wlistpiece3.setImageDrawable(getPieceDrawableByColor(colour));
                    wlistname3.setText(player2name);
                    wlistwinorlose1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.loser1, null));
                }
                hideThisPlayerDiceBg(3);
                break;
            case 4:
                if(nop==3) {
                    wlistshield3.setVisibility(View.INVISIBLE);
                    wlistpiece3.setImageDrawable(getPieceDrawableByColor(colour));
                    wlistname3.setText(player4name);
                    wlistwinorlose1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.loser1, null));
                }
                hideThisPlayerDiceBg(4);
                break;
        }
    }

    private void setCurrentPlayerAsWinnerAtCurrentPosition(int currentPlayerSelectedIndex,String colour,String playername) {
        offlineFinishRanks.put(colour, currentWinnerPosition);
        switch ((currentPlayerSelectedIndex+1)) {
            case 1:
                shieldIndex1.setVisibility(View.VISIBLE);
                findViewById(R.id.winnerindexnumber1).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.winnerindexnumber1)).setImageDrawable(getWinnerNumberDrawableByNumber(currentWinnerPosition));
                if(nop!=2) {
                    addCurrentPlayerToCongratsList(colour, player3name, currentWinnerPosition);
                }
                else {
                    addCurrentPlayerToCongratsList(colour, playername, currentWinnerPosition);
                }

                hideThisPlayerDiceBg(1);
                break;
            case 2:
                shieldIndex2.setVisibility(View.VISIBLE);
                findViewById(R.id.winnerindexnumber2).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.winnerindexnumber2)).setImageDrawable(getWinnerNumberDrawableByNumber(currentWinnerPosition));
                if(nop!=2) {
                    addCurrentPlayerToCongratsList(colour, player1name, currentWinnerPosition);
                }
                else {
                    addCurrentPlayerToCongratsList(colour, playername, currentWinnerPosition);
                }
                hideThisPlayerDiceBg(2);
                break;
            case 3:
                shieldIndex3.setVisibility(View.VISIBLE);
                findViewById(R.id.winnerindexnumber3).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.winnerindexnumber3)).setImageDrawable(getWinnerNumberDrawableByNumber(currentWinnerPosition));
                if(nop!=2) {
                    addCurrentPlayerToCongratsList(colour, player2name, currentWinnerPosition);
                }
                else {
                    addCurrentPlayerToCongratsList(colour, playername, currentWinnerPosition);
                }
                hideThisPlayerDiceBg(3);
                break;
            case 4:
                shieldIndex4.setVisibility(View.VISIBLE);
                findViewById(R.id.winnerindexnumber4).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.winnerindexnumber4)).setImageDrawable(getWinnerNumberDrawableByNumber(currentWinnerPosition));
                addCurrentPlayerToCongratsList(colour,playername,currentWinnerPosition);
                hideThisPlayerDiceBg(4);
                break;
        }
        currentWinnerPosition++;
    }

    private void addCurrentPlayerToCongratsList(String color,String name,int currentWinnerPosition) {
        switch (currentWinnerPosition) {
            case 1:
                wlistshield1.setVisibility(View.VISIBLE);
                wlistshield2.setVisibility(View.INVISIBLE);
                wlistpiece1.setImageDrawable(getPieceDrawableByColor(color));
                wlistname1.setText(name);
                wlistwinorlose1.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.winner1,null));
                break;
            case 2:
                wlistshield2.setVisibility(View.VISIBLE);
                wlistshield3.setVisibility(View.INVISIBLE);
                wlistpiece2.setImageDrawable(getPieceDrawableByColor(color));
                wlistname2.setText(name);
                wlistwinorlose2.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.winner1,null));
                break;
            case 3:
                wlistshield3.setVisibility(View.VISIBLE);
                wlistshield4.setVisibility(View.INVISIBLE);
                wlistpiece3.setImageDrawable(getPieceDrawableByColor(color));
                wlistname3.setText(name);
                wlistwinorlose3.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.winner1,null));
                break;
            case 4:
                wlistshield4.setVisibility(View.VISIBLE);
                wlistshield4.setVisibility(View.INVISIBLE);
                wlistpiece1.setImageDrawable(getPieceDrawableByColor(color));
                wlistname1.setText(name);
                wlistwinorlose4.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.loser1,null));
                break;
        }
    }

    Drawable getWinnerNumberDrawableByNumber(int x) {
        switch (x) {
            case 1:
                return ResourcesCompat.getDrawable(getResources(),R.drawable.winner1number,null);
            case 2:
                return ResourcesCompat.getDrawable(getResources(),R.drawable.winner2number,null);
            case 3:
                return ResourcesCompat.getDrawable(getResources(),R.drawable.winner3number,null);
            case 4:
                return ResourcesCompat.getDrawable(getResources(),R.drawable.winner4number,null);
        }
        return null;
    }

    Drawable getWinnershieldDrawableByNumber(int x) {
        switch (x) {
            case 1:
                return ResourcesCompat.getDrawable(getResources(),R.drawable.lm_shield,null);
            case 2:
                return ResourcesCompat.getDrawable(getResources(),R.drawable.lm_shield,null);
            case 3:
                return ResourcesCompat.getDrawable(getResources(),R.drawable.lm_shield,null);
            case 4:
                return null;
        }
        return null;
    }

    private Drawable getStylishIconDrawableByColor(String colour) {
        switch(colour){
            case "red":
                return ResourcesCompat.getDrawable(getResources(),R.drawable.lhj_token_red,null);
            case "blue":
                return ResourcesCompat.getDrawable(getResources(),R.drawable.lhj_token_blue,null);
            case "green":
                return ResourcesCompat.getDrawable(getResources(),R.drawable.lhj_token_green,null);
            case "yellow":
                return ResourcesCompat.getDrawable(getResources(),R.drawable.lhj_token_yellow,null);
        }
        return null;
    }

    void checkPantaAdjustments(String color,int targetBox) {
        // Completed tokens use the same per-square layout as other tokens.
        layoutTokenSquare(targetBox);
    }

    /**
     * A visible pawn may cover other pawns on the same square. Offer every
     * selectable pawn explicitly instead of relying on z-order or tiny hitboxes.
     */
    private void chooseStackedPieceOrMove(Piece tapped) {
        if (currentPlayerDice < 1) return;
        if (tapped.currBlock < 0) {
            if (tapped.colour.equals(currentPlayerColor) && !tapped.isAlive && currentPlayerDice == 6)
                executeSelectedPiece(tapped);
            return;
        }
        // Resolve legal active-player pawns at this square even if another
        // colour's token is visually above them in a shared safe-square stack.
        List<Piece> selectable = new ArrayList<>();
        for (Piece candidate : getPiecesByColor(currentPlayerColor)) {
            if (candidate.currBlock == tapped.currBlock && candidate.isAlive &&
                    candidate.isClickable && otherwiseLegal(candidate,currentPlayerDice) &&
                    !blockedByOwnPawn(candidate,currentPlayerDice)) selectable.add(candidate);
        }
        if (selectable.isEmpty()) {
            if (tapped.colour.equals(currentPlayerColor)) executeSelectedPiece(tapped);
            return;
        }
        if (selectable.size() == 1) {
            executeSelectedPiece(selectable.get(0));
            return;
        }
        String[] labels = new String[selectable.size()];
        List<Piece> colorPieces = getPiecesByColor(currentPlayerColor);
        for (int i=0;i<selectable.size();i++)
            labels[i] = "Pawn " + (colorPieces.indexOf(selectable.get(i)) + 1);
        new AlertDialog.Builder(this)
                .setTitle("Choose a token")
                .setItems(labels,(dialog,which)->executeSelectedPiece(selectable.get(which)))
                .setNegativeButton("Cancel",null)
                .show();
    }

    /** Prioritise all movable current-player tokens over other colours after a dice roll. */
    private void prioritiseCurrentPlayerPieces() {
        if(currentPlayerColor==null) return;
        for(Piece p:getPiecesByColor(currentPlayerColor)) {
            if(p.isClickable && !p.hasCompletedItsPurpose && p.piece!=null) {
                p.piece.bringToFront();
                p.piece.setElevation(hudDp(15));
                p.piece.setClickable(true);
            }
        }
    }

    /**
     * Predict the final square using the same entrance and finishing-lane
     * transition rules as Piece.move(), without altering the live piece.
     * A pawn may pass through another pawn; only the landing square matters.
     */
    private int landingSquare(Piece p, int steps) {
        return LudoLandingRules.destination(
            p.currBlock,p.startPosition,p.isAlive,p.isReadyToEnterWinnerZone,
            p.currWinnerBlock,p.stepsUntilShuffleEntrance,p.endPosition,
            homeShuffleEnabled && homeShuffleEngine!=null,steps,
            getWinnerBlocks(p.endPosition));
    }

    /** Same colour may stack only on designated safe track squares. */
    private boolean blockedByOwnPawn(Piece moving, int face) {
        if (!moving.isAlive) return false; // All starts are safe squares.
        int destination=landingSquare(moving,face);
        if (destination<0 || safeSpots.contains(destination)) return false;
        java.util.ArrayList<Integer> occupied=new java.util.ArrayList<>();
        for(Piece other:getPiecesByColor(moving.colour)) {
            if(other!=moving && other.isAlive && !other.hasCompletedItsPurpose)
                occupied.add(other.currBlock);
        }
        int[] squares=new int[occupied.size()];
        for(int i=0;i<squares.length;i++)squares[i]=occupied.get(i);
        return LudoLandingRules.occupiedByFriend(destination,false,squares);
    }

    private boolean otherwiseLegal(Piece p,int face) {
        if(face<1 || face>6 || p.hasCompletedItsPurpose || p.isThisPlayerWon)return false;
        if(!p.isAlive)return face==6;
        if(homeShuffleEnabled && homeShuffleEngine!=null) {
            int remaining=p.isReadyToEnterWinnerZone?
                (6-p.currWinnerBlock):(p.stepsUntilShuffleEntrance+6);
            return face<=remaining;
        }
        return p.numberOfSteps+face<57;
    }

    /** No preference for any player; face excluded only if all viable pieces collide. */
    private boolean faceIsOwnCollisionBlocked(int face) {
        boolean hasNormallyLegal=false;
        for(Piece piece:getPiecesByColor(currentPlayerColor)) {
            if(!otherwiseLegal(piece,face))continue;
            hasNormallyLegal=true;
            if(!blockedByOwnPawn(piece,face))return false;
        }
        return hasNormallyLegal;
    }

    private void executeSelectedPiece(Piece selected) {
        if (!currentPlayerColor.equals(selected.colour) || currentPlayerDice < 1) return;
        if (!otherwiseLegal(selected,currentPlayerDice)
                || blockedByOwnPawn(selected,currentPlayerDice)) {
            Toast.makeText(this,"Choose a different pawn: landing blocked",
                Toast.LENGTH_SHORT).show();
            return;
        }
        if (selected.isAlive && selected.isClickable && !selected.hasCompletedItsPurpose) {
            int rolled = currentPlayerDice;
            currentPlayerDice = -1;
            for (Piece other : getPiecesByColor(selected.colour)) other.inactiveState();
            checkAdjustments(selected.currBlock);
            selected.diceValue = rolled;
            selected.move(rolled);
        } else if (!selected.isAlive && currentPlayerDice == 6) {
            currentPlayerDice = -1;
            for (Piece other : getPiecesByColor(selected.colour)) other.inactiveState();
            selected.makeAlive();
        }
    }

    private boolean isSquareThreatened(int square, String ownerColor) {
        if (square < 0 || square >= 52 || safeSpots.contains(square) || players == null) return false;
        for (Player player : players) {
            if (ownerColor.equals(player.color)) continue;
            for (Piece enemy : getPiecesByColor(player.color)) {
                if (!enemy.isAlive || enemy.hasCompletedItsPurpose ||
                        enemy.currBlock < 0 || enemy.currBlock >= 52 || safeSpots.contains(enemy.currBlock))
                    continue;
                int distance = (square - enemy.currBlock + 52) % 52;
                if (distance >= 1 && distance <= 6) return true;
            }
        }
        return false;
    }

    private Piece opponentOnSquare(Piece moving, int destination) {
        if (destination < 0 || destination >= 52 || safeSpots.contains(destination) || players == null)
            return null;
        for (Player player : players) {
            if (moving.colour.equals(player.color)) continue;
            for (Piece enemy : getPiecesByColor(player.color)) {
                if (enemy.isAlive && !enemy.hasCompletedItsPurpose && enemy.currBlock == destination)
                    return enemy;
            }
        }
        return null;
    }

    private int finishingProgress(Piece piece, int destination) {
        int[] lane = getWinnerBlocks(piece.endPosition);
        for (int i=0;i<lane.length;i++) if (lane[i] == destination) return i + 1;
        return 0;
    }

    private StrategicBot.Move assessBotMove(Piece piece, int face) {
        boolean legal = otherwiseLegal(piece,face) && !blockedByOwnPawn(piece,face);
        if (!legal) return new StrategicBot.Move(false,false,false,false,false,false,false,false,0);
        int destination = landingSquare(piece,face);
        Piece target = opponentOnSquare(piece,destination);
        boolean safe = destination >= 0 && safeSpots.contains(destination);
        boolean capture = target != null;
        boolean shielded = capture && immunityShields.getOrDefault(target.colour,0) > 0;
        boolean escaped = piece.isAlive && isSquareThreatened(piece.currBlock,piece.colour) &&
                (safe || !isSquareThreatened(destination,piece.colour));
        int laneProgress = finishingProgress(piece,destination);
        int[] lane = getWinnerBlocks(piece.endPosition);
        boolean finish = lane.length > 0 && destination == lane[lane.length-1];
        int progress = laneProgress > 0 ? laneProgress * 3 : piece.numberOfSteps / 2;
        return new StrategicBot.Move(true,capture,shielded,finish,escaped,safe,
                !piece.isAlive,isSquareThreatened(destination,piece.colour),progress);
    }

    private double tacticalScoreForFace(int face) {
        double best = 0d;
        for (Piece piece : getPiecesByColor(currentPlayerColor)) {
            if (!otherwiseLegal(piece,face) || blockedByOwnPawn(piece,face)) continue;
            int destination = landingSquare(piece,face);
            Piece target = opponentOnSquare(piece,destination);
            boolean safe = destination >= 0 && safeSpots.contains(destination);
            boolean escaped = piece.isAlive && isSquareThreatened(piece.currBlock,piece.colour) &&
                    (safe || !isSquareThreatened(destination,piece.colour));
            int laneProgress = finishingProgress(piece,destination);
            int[] lane = getWinnerBlocks(piece.endPosition);
            boolean finish = lane.length > 0 && destination == lane[lane.length-1];
            boolean shielded = target != null && immunityShields.getOrDefault(target.colour,0) > 0;
            double score = BoardAwareDiceEngine.opportunityScore(target != null,shielded,escaped,safe,
                    !piece.isAlive,laneProgress,finish);
            best = Math.max(best,score);
        }
        return best;
    }

    private double diceWeightForFace(int face) {
        if (diceMode == BoardAwareDiceEngine.Mode.FAIR_RANDOM) return 1d;
        double weight = boardAwareDiceEngine.weight(diceMode,tacticalScoreForFace(face));
        boolean allHome = true;
        for (Piece piece : getPiecesByColor(currentPlayerColor))
            if (piece.isAlive || piece.hasCompletedItsPurpose) allHome = false;
        if (face == 6 && allHome)
            weight += Math.min(4,homeRollMisses.getOrDefault(currentPlayerColor,0));
        else if (face == 6 && diceRules.consecutiveSixes() > 0) weight *= 0.35d;
        return weight;
    }

    private int chooseStrategicBotMove(List<Piece> candidates, int face) {
        List<StrategicBot.Move> assessed = new ArrayList<>();
        for (Piece candidate : candidates) assessed.add(assessBotMove(candidate,face));
        return StrategicBot.selectWeightedIndex(assessed,botTieBreaker);
    }

    private boolean isHorizontalTrackSquare(int targetBox) {
        return (targetBox >= 0 && targetBox <= 4) || (targetBox >= 18 && targetBox <= 30) ||
                (targetBox >= 44 && targetBox <= 57) || (targetBox >= 64 && targetBox <= 69);
    }

    /** Re-layout every visible owner on a square, including completed lane pawns. */
    private void layoutTokenSquare(int targetBox) {
        if (targetBox < 0 || targetBox >= x.length || players == null || pieceWidth <= 0f) return;
        List<Piece> occupants = new ArrayList<>();
        for (Player player : players) {
            for (Piece piece : getPiecesByColor(player.color)) {
                if (piece.currBlock == targetBox && (piece.isAlive || piece.hasCompletedItsPurpose))
                    occupants.add(piece);
            }
        }
        int count = occupants.size();
        boolean horizontal = isHorizontalTrackSquare(targetBox);
        float baseX = x[targetBox] + pushXForPieces;
        float baseY = y[targetBox] - pushYForPieces;
        float scale = boardTokenScale * TokenLayoutEngine.stackScale(count);
        for (int i=0;i<count;i++) {
            Piece piece = occupants.get(i);
            TokenLayoutEngine.Offset offset = TokenLayoutEngine.offset(i,count,horizontal,blockSize);
            piece.piece.setPivotX(pieceWidth / 2f);
            piece.piece.setPivotY(pieceHeight / 2f);
            piece.piece.setScaleX(scale);
            piece.piece.setScaleY(scale);
            piece.piece.setTranslationX(baseX + offset.x);
            piece.piece.setTranslationY(baseY + offset.y);
            piece.piece.setElevation(hudDp(10) + i);
        }
    }

    private void checkAdjustments(int targetBox) {
        layoutTokenSquare(targetBox);
    }

    /** Restore home pawns and rebuild track/lane groups after a home reassignment. */
    private void refreshAllTokenLayouts() {
        if (players == null) return;
        java.util.Set<Integer> occupiedSquares = new java.util.HashSet<>();
        for (Player player : players) {
            for (Piece piece : getPiecesByColor(player.color)) {
                if (piece.piece == null) continue;
                piece.piece.setPivotX(pieceWidth / 2f);
                piece.piece.setPivotY(pieceHeight / 2f);
                if (piece.currBlock >= 0 && (piece.isAlive || piece.hasCompletedItsPurpose)) {
                    occupiedSquares.add(piece.currBlock);
                } else if (!piece.isAlive && !piece.hasCompletedItsPurpose) {
                    piece.piece.setScaleX(boardTokenScale);
                    piece.piece.setScaleY(boardTokenScale);
                    piece.piece.setTranslationX(piece.defX);
                    piece.piece.setTranslationY(piece.defY);
                }
            }
        }
        for (Integer square : occupiedSquares) layoutTokenSquare(square);
    }

    private int hudDp(float amount) {
        return Math.round(amount * getResources().getDisplayMetrics().density);
    }

    private void addCentralDiceResultView() {
        RelativeLayout boardLayer = findViewById(R.id.relativeLayout);
        if (boardLayer == null || centralDiceResultView != null) return;
        centralDiceResultView = new TextView(this);
        centralDiceResultView.setTextSize(32);
        centralDiceResultView.setTextColor(Color.WHITE);
        centralDiceResultView.setTypeface(null,Typeface.BOLD);
        centralDiceResultView.setGravity(Gravity.CENTER);
        centralDiceResultView.setElevation(hudDp(12));
        centralDiceResultView.setClickable(false);
        centralDiceResultView.setFocusable(false);
        centralDiceResultView.setVisibility(View.GONE);
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(hudDp(14));
        centralDiceResultView.setBackground(background);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(hudDp(62),hudDp(62));

        boardLayer.addView(centralDiceResultView,params);
    }

    private void showCentralDiceResult(int result) {
        if (centralDiceResultView == null) return;
        globalHandler.removeCallbacks(hideCentralDiceResult);
        int color = 0xff248efd;
        if ("red".equals(currentPlayerColor)) color = 0xfff04452;
        else if ("green".equals(currentPlayerColor)) color = 0xff23bf75;
        else if ("yellow".equals(currentPlayerColor)) color = 0xffffc342;
        centralDiceResultView.setText(String.valueOf(result));
        centralDiceResultView.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(color));
        centralDiceResultView.setX(ludoBoard.getX() + (ludoBoard.getWidth()-hudDp(62))/2f);
        centralDiceResultView.setY(ludoBoard.getY() + (ludoBoard.getHeight()-hudDp(62))/2f);
        centralDiceResultView.setVisibility(View.VISIBLE);
        centralDiceResultView.bringToFront();
        globalHandler.postDelayed(hideCentralDiceResult,1500);
    }

    /** Always-visible, compact stats strip above the original board, without altering its tiles. */
    private void initStatsHud() {
        if (statsHud != null) return;
        ConstraintLayout root = findViewById(R.id.activitybg);
        statsHud = new LinearLayout(this);
        statsHud.setOrientation(LinearLayout.HORIZONTAL);
        statsHud.setPadding(hudDp(4), hudDp(4), hudDp(4), hudDp(4));
        statsHud.setClipChildren(false);
        ConstraintLayout.LayoutParams hudParams = new ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, hudDp(106));
        hudParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        hudParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        hudParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        hudParams.topMargin = hudDp(69);
        root.addView(statsHud, hudParams);

        for (String color : new String[]{"red","green","yellow","blue"}) {
            TextView cell = new TextView(this);
            cell.setTextSize(11);
            cell.setTextColor(0xff172B54);
            cell.setGravity(Gravity.CENTER);
            cell.setMaxLines(4);
            cell.setPadding(hudDp(2),hudDp(3),hudDp(2),hudDp(3));
            GradientDrawable background = new GradientDrawable();
            background.setColors(new int[]{0xffFFFFFF,0xffEBF5FF});
            background.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
            background.setCornerRadius(hudDp(16));
            int accent;
            switch (color) {
                case "red": accent=0xffe84248; break;
                case "green": accent=0xff28b66e; break;
                case "yellow": accent=0xffffca44; break;
                default: accent=0xff428aff;
            }
            background.setStroke(hudDp(2),accent);
            LinearLayout badge=new LinearLayout(this);
            badge.setOrientation(LinearLayout.VERTICAL);
            badge.setGravity(Gravity.CENTER);
            badge.setBackground(background);
            ImageView avatar=new ImageView(this);
            avatar.setImageResource(R.drawable.lhj_avatar_human);
            avatar.setContentDescription("Player avatar");
            badge.addView(avatar,new LinearLayout.LayoutParams(hudDp(34),hudDp(34)));
            badge.addView(cell,new LinearLayout.LayoutParams(-1,0,1f));
            LinearLayout.LayoutParams cellParams=new LinearLayout.LayoutParams(0,-1,1f);
            cellParams.setMargins(hudDp(2),0,hudDp(2),0);
            statsHud.addView(badge,cellParams);
            statsCells.put(color,cell);
            statsAvatarViews.put(color,avatar);
        }
        statsHud.bringToFront();
        addLiveAudioButton(root);
        refreshStatsHud();
    }

    private void addLiveAudioButton(ConstraintLayout root) {
        android.widget.Button audio=new android.widget.Button(this);
        audio.setText("SOUND");
        audio.setTextSize(10);
        audio.setTextColor(0xff172B54);
        audio.setAllCaps(false);
        audio.setBackgroundResource(R.drawable.lhj_button_white);
        ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(hudDp(108),hudDp(45));
        params.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
        params.topMargin=hudDp(16);
        root.addView(audio,params);
        audio.setOnClickListener(v->{
            isSoundOn=!isSoundOn;
            sharedPreferences.edit().putBoolean("sound",isSoundOn).apply();
            audio.setText(isSoundOn?"SOUND ON":"SOUND OFF");
            if(!isSoundOn && congratulationSound!=null && congratulationSound.isPlaying())
                congratulationSound.pause();
        });
        audio.setText(isSoundOn?"SOUND ON":"SOUND OFF");
        audio.bringToFront();
    }

    private void refreshStatsHud() {
        prioritiseCurrentPlayerPieces();
        if (statsHud == null) return;
        for (String color : new String[]{"red","green","yellow","blue"}) {
            TextView cell=statsCells.get(color);
            if (cell == null) continue;
            String name=offlinePlayerNames.get(color);
            if (name==null) {
                ((View)cell.getParent()).setVisibility(View.GONE);
                continue;
            }
            ((View)cell.getParent()).setVisibility(View.VISIBLE);
            ImageView avatar=statsAvatarViews.get(color);
            if(avatar!=null)avatar.setImageResource(
                name.toLowerCase(java.util.Locale.ROOT).startsWith("computer")
                    ?R.drawable.lhj_avatar_robot:R.drawable.lhj_avatar_human);
            if(name.length()>10) name=name.substring(0,9)+"…";
            int kills=getCount(killCounts,color);
            int deaths=getCount(deathCounts,color);
            StringBuilder display=new StringBuilder();
            display.append(name).append("\n");
            display.append("K:").append(kills).append("  D:").append(deaths).append("\n");
            display.append("Shield:").append(immunityEnabled?getCount(immunityShields,color):"OFF");
            if (combatScoringEnabled) display.append("\nPts:").append(5*(kills-deaths));
            cell.setText(display.toString());
            cell.setContentDescription(color+" player "+name+
                ", kills "+kills+", deaths "+deaths+
                ", shields "+(immunityEnabled?getCount(immunityShields,color):0));
            cell.setAlpha(color.equals(currentPlayerColor)?1f:0.84f);
        }
    }

    private int getCount(java.util.Map<String,Integer> values, String color) {
        Integer count = values.get(color);
        return count == null ? 0 : count;
    }

    private void incrementCount(java.util.Map<String,Integer> values, String color) {
        values.put(color, getCount(values,color) + 1);
    }

    private void settleCapture(Piece attacker, Piece defender,
                               java.util.function.Consumer<Boolean> continuation) {
        defender.die();
        incrementCount(killCounts, attacker.colour);
        incrementCount(deathCounts, defender.colour);
        if (immunityEnabled) incrementCount(immunityShields, attacker.colour);
        refreshStatsHud();
        String summary = "Great capture! " + attacker.colour + " takes " + defender.colour;
        if (combatScoringEnabled) summary += "  +5 points";
        if (immunityEnabled) summary += "  +1 shield";
        Toast.makeText(this, summary, Toast.LENGTH_SHORT).show();
        continuation.accept(true);
    }

    private void resolveCapture(Piece attacker, int targetBox,
                                java.util.function.Consumer<Boolean> continuation) {
        if (safeSpots.contains(targetBox)) { continuation.accept(false); return; }
        int index = Math.min(currentPlayerIndex, players.size() - 1);
        for (int i = 0; i < players.size(); i++) {
            String colour = players.get(index).getColor();
            if (!colour.equals(attacker.colour)) {
                for (Piece defender : getPiecesByColor(colour)) {
                    if (!defender.isAlive || defender.currBlock != targetBox) continue;
                    if (gametype == 2 &&
                        ((attacker.colour.equals("yellow") && colour.equals("red")) ||
                         (attacker.colour.equals("red") && colour.equals("yellow")) ||
                         (attacker.colour.equals("blue") && colour.equals("green")) ||
                         (attacker.colour.equals("green") && colour.equals("blue")))) continue;
                    if (immunityEnabled && getCount(immunityShields, colour) > 0) {
                        new AlertDialog.Builder(this)
                            .setTitle(colour.toUpperCase(java.util.Locale.ROOT) + " - Immunity")
                            .setMessage("Spend one immunity shield to protect this pawn? Hand the device to the defender.")
                            .setPositiveButton("Use Shield", (dialog, which) -> {
                                immunityShields.put(colour, getCount(immunityShields,colour) - 1);
                                refreshStatsHud();
                                Toast.makeText(this, "Capture blocked by immunity", Toast.LENGTH_SHORT).show();
                                continuation.accept(false);
                            })
                            .setNegativeButton("Accept Capture", (dialog, which) ->
                                settleCapture(attacker, defender, continuation))
                            .setCancelable(false).show();
                        return;
                    }
                    settleCapture(attacker, defender, continuation);
                    return;
                }
            }
            index = (index + 1) % players.size();
        }
        continuation.accept(false);
    }

    private void finishPieceMove(Piece piece, boolean captured) {
        refreshStatsHud();
        if (!captured) applyPendingHomeShuffle();
        globalHandler.postDelayed(() -> {
            if (!piece.isThisPlayerWon) checkAdjustments(piece.currBlock);
        },10);
        if (piece.diceValue == 6 || piece.currWinnerBlock > 5 || (captured && !piece.isThisPlayerWon)) {
            isDiceMovableExtraChance = true;
            if (!piece.isBotPiece) hintArrow.setVisibility(View.VISIBLE);
            else {
                final String expectedColour = piece.colour;
                final int expectedTurn = shuffleTurnGeneration;
                globalHandler.postDelayed(() -> {
                    if (!shuffleMatchEnding && d != null &&
                        expectedTurn == shuffleTurnGeneration &&
                        expectedColour.equals(currentPlayerColor)) d.roll();
                }, 150);
            }
        } else {
            switchPlayers();
            d.isDiceClickable = true;
        }
    }

    /** Persist match facts locally. No network is invoked here. */
    private void saveOfflineMatchResult() {
        if (offlineResultSaved || offlineMatchId == null || offlinePlayerNames.isEmpty()) return;
        try {
            JSONObject result = new JSONObject();
            result.put("schemaVersion", 1);
            result.put("clientMatchId", offlineMatchId);
            result.put("startedAtEpochMs", offlineMatchStartedAt);
            result.put("endedAtEpochMs", System.currentTimeMillis());
            result.put("gameType", gametype);
            result.put("syncStatus", "pending_player_mapping");
            JSONObject rules = new JSONObject();
            rules.put("combatScoring", combatScoringEnabled);
            rules.put("immunity", immunityEnabled);
            rules.put("dynamicHomeShuffle", homeShuffleEnabled);
            rules.put("shuffleIntervalSeconds", shuffleIntervalSeconds);
            result.put("rules", rules);
            result.put("leagueId", selectedLeagueId > 0 ? selectedLeagueId : JSONObject.NULL);
            result.put("leagueName",selectedLeagueName==null?"":selectedLeagueName);
            JSONArray slots = new JSONArray();
            java.util.Set<Integer> assignedRanks = new java.util.HashSet<>();
            java.util.Set<Integer> reservedRanks = new java.util.HashSet<>();
            for (Integer existingRank : offlineFinishRanks.values())
                if (existingRank != null && existingRank >= 1 && existingRank <= offlinePlayerNames.size()) reservedRanks.add(existingRank);
            int nextFallbackRank = 1;
            for (java.util.Map.Entry<String,String> player : offlinePlayerNames.entrySet()) {
                String color = player.getKey();
                JSONObject slot = new JSONObject();
                slot.put("color", color);
                slot.put("offlineName", player.getValue());
                boolean standardBot = gametype == 4 &&
                    ("Computer 1".equals(player.getValue()) ||
                     "Computer 2".equals(player.getValue()) ||
                     "Computer 3".equals(player.getValue()));
                slot.put("isBot",standardBot);
                Integer mapped = selectedLeaguePlayerIds.get(player.getValue());
                slot.put("leaguePlayerId", mapped==null ? JSONObject.NULL : mapped);
                Integer rank = offlineFinishRanks.get(color);
                if (rank == null || rank < 1 || rank > offlinePlayerNames.size() || assignedRanks.contains(rank)) {
                    while (assignedRanks.contains(nextFallbackRank) || reservedRanks.contains(nextFallbackRank)) nextFallbackRank++;
                    rank = nextFallbackRank++;
                }
                assignedRanks.add(rank);
                slot.put("finishRank", rank);
                slot.put("kills", getCount(killCounts,color));
                slot.put("deaths", getCount(deathCounts,color));
                slot.put("remainingShields", getCount(immunityShields,color));
                slot.put("combatPoints", combatScoringEnabled ?
                    5 * (getCount(killCounts,color)-getCount(deathCounts,color)) : 0);
                slots.put(slot);
            }
            result.put("players",slots);
            File directory = new File(getFilesDir(),"offline-matches");
            if (!directory.exists() && !directory.mkdirs())
                throw new java.io.IOException("Cannot create offline match directory");
            File target = new File(directory, offlineMatchId + ".json");
            try (FileOutputStream out = new FileOutputStream(target)) {
                out.write(result.toString(2).getBytes(StandardCharsets.UTF_8));
                out.getFD().sync();
            }
            offlineResultSaved = true;
        } catch (Exception exception) {
            android.util.Log.e("LudoMasters", "Failed saving offline match", exception);
            Toast.makeText(this,"Unable to save match history. Please report this error.",Toast.LENGTH_LONG).show();
        }
    }

    private String combatSummary() {
        StringBuilder result = new StringBuilder();
        for (String color : new String[]{"red","green","yellow","blue"}) {
            if (result.length() > 0) result.append(" | ");
            result.append(color).append(": ");
            result.append(getCount(killCounts,color)).append(" kills, ");
            result.append(getCount(deathCounts,color)).append(" deaths");
            if (combatScoringEnabled)
                result.append(", ").append(5 * (getCount(killCounts,color) - getCount(deathCounts,color))).append(" pts");
            if (immunityEnabled)
                result.append(", ").append(getCount(immunityShields,color)).append(" shields");
        }
        return result.toString();
    }

    private Drawable diceFaceDrawable(int face) {
        int resourceId;
        switch (face) {
            case 2: resourceId = R.drawable.dice2; break;
            case 3: resourceId = R.drawable.dice3; break;
            case 4: resourceId = R.drawable.dice4; break;
            case 5: resourceId = R.drawable.dice5; break;
            case 6: resourceId = R.drawable.dice6; break;
            default: resourceId = R.drawable.dice1;
        }
        return ResourcesCompat.getDrawable(getResources(),resourceId,getTheme());
    }

    private void setDiceFace(ImageView view,int face) {
        if (view != null) view.setImageDrawable(diceFaceDrawable(face));
    }

    private AnimationDrawable createDiceRollAnimationDrawable() {
        int[] frames = {R.drawable.dice0001,R.drawable.dice0002,R.drawable.dice0003,
                R.drawable.dice0004,R.drawable.dice0005,R.drawable.dice0006,
                R.drawable.dice0007,R.drawable.dice0008};
        AnimationDrawable animation = new AnimationDrawable();
        for (int frame : frames)
            animation.addFrame(ResourcesCompat.getDrawable(getResources(),frame,getTheme()),50);
        return animation;
    }

    class Dice
    {
        ImageView diceImgView,tLeftImgColor,tRightImgColor,bLeftImgColor,bRightImgColor;
        AnimationDrawable diceAnimationDrawable;

        Handler diceHandler;
        boolean isRolling, isDiceClickable;

        ConstraintLayout tLeftLayout,tRightLayout,bLeftLayout,bRightLayout;

        String mainColor;

        int player = 0,numberOfPlayers;
        Dice(ImageView diceImgView,int nop,String color)
        {
            this.diceImgView = diceImgView;
            this.numberOfPlayers = nop;
            diceAnimationDrawable = new AnimationDrawable();

            tLeftLayout = findViewById(R.id.tleftdicebg);
            tRightLayout = findViewById(R.id.trightdicebg);
            bLeftLayout = findViewById(R.id.bleftdicebg);
            bRightLayout = findViewById(R.id.brightdicebg);

            tLeftImgColor = findViewById(R.id.tleftpieceimage);
            tRightImgColor = findViewById(R.id.trightpieceimage);
            bLeftImgColor = findViewById(R.id.bleftpieceimage);
            bRightImgColor = findViewById(R.id.brightpieceimage);

            mainColor = color;
            setDiceFace(diceImgView,1);

            diceHandler = new Handler();
            isRolling = false;
            isDiceClickable = true;

            diceImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isRolling && (isDiceClickable || isDiceMovableExtraChance)) {
                        hintArrow.setVisibility(GONE);
                        roll();
                    }
                }
            });

            if(nop==2) {
                menuremoveplayersbtn.setVisibility(GONE);
                tRightLayout.setVisibility(View.VISIBLE);
                bLeftLayout.setVisibility(View.VISIBLE);
                tLeftLayout.setVisibility(GONE);
                bRightLayout.setVisibility(GONE);

                switch (mainColor) {
                    case "red":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("red"));
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("yellow"));
                        break;
                    case "green":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("green"));
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("blue"));
                        break;
                    case "blue":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("blue"));
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("green"));
                        break;
                    case "yellow":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("yellow"));
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("red"));
                        break;
                }
            }
            else if(nop == 3) {
                rmp4bg.setAlpha(0.5f);
                rmpp4removeicon.setOnClickListener(null);
                tRightLayout.setVisibility(View.VISIBLE);
                bLeftLayout.setVisibility(View.VISIBLE);
                tLeftLayout.setVisibility(View.VISIBLE);
                bRightLayout.setVisibility(GONE);
                switch (mainColor) {
                    case "red":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("green")); //3
                        tLeftImgColor.setImageDrawable(getPieceDrawableByColor("yellow")); //1
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("blue")); //2
                        break;
                    case "green":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("yellow")); //2
                        tLeftImgColor.setImageDrawable(getPieceDrawableByColor("blue")); //3
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("red")); //1

                        break;
                    case "blue":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("red")); //3
                        tLeftImgColor.setImageDrawable(getPieceDrawableByColor("green")); //2
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("yellow")); //1
                        break;
                    case "yellow":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("blue")); //2
                        tLeftImgColor.setImageDrawable(getPieceDrawableByColor("red")); //1
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("green")); //3
                        break;
                }
            }
            else {
                tRightLayout.setVisibility(View.VISIBLE);
                bLeftLayout.setVisibility(View.VISIBLE);
                tLeftLayout.setVisibility(View.VISIBLE);
                bRightLayout.setVisibility(View.VISIBLE);
                switch (mainColor) {
                    case "red":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("red")); //3
                        tLeftImgColor.setImageDrawable(getPieceDrawableByColor("green")); //2
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("yellow")); //1
                        bRightImgColor.setImageDrawable(getPieceDrawableByColor("blue")); //4
                        break;
                    case "green":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("green")); //3
                        tLeftImgColor.setImageDrawable(getPieceDrawableByColor("yellow")); //1
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("blue")); //2
                        bRightImgColor.setImageDrawable(getPieceDrawableByColor("red")); //4
                        break;
                    case "blue":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("blue")); //3
                        tLeftImgColor.setImageDrawable(getPieceDrawableByColor("red")); //2
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("green")); //1
                        bRightImgColor.setImageDrawable(getPieceDrawableByColor("yellow")); //4
                        break;
                    case "yellow":
                        bLeftImgColor.setImageDrawable(getPieceDrawableByColor("yellow")); //2
                        tLeftImgColor.setImageDrawable(getPieceDrawableByColor("blue")); //1
                        tRightImgColor.setImageDrawable(getPieceDrawableByColor("red")); //3
                        bRightImgColor.setImageDrawable(getPieceDrawableByColor("green")); //4
                        break;
                }
            }
        }

        void roll() {
            if (isRolling || shuffleMatchEnding || players == null || players.isEmpty()) return;
            isRolling = true;
            isDiceClickable = false;
            isDiceMovableExtraChance = false;
            if(isSoundOn) {
                if (diceRollSound.isPlaying()) {
                    diceRollSound.pause();
                }
                diceRollSound.seekTo(5);
                diceRollSound.start();
            }
            diceAnimationDrawable=createDiceRollAnimationDrawable();
            mainDiceImageView.setImageDrawable(diceAnimationDrawable);
            diceAnimationDrawable.setOneShot(true);
            diceAnimationDrawable.start();
            diceHandler.postDelayed(() -> {
                final boolean thirdRollLimited = diceRules.nextRollExcludesSix();
                int ch = diceRules.roll(face -> !faceIsOwnCollisionBlocked(face),MainActivity.this::diceWeightForFace);
                if(ch==0) {
                    isRolling=false;
                    setDiceFace(mainDiceImageView,1);
                    Toast.makeText(MainActivity.this,
                        "No free landing square. Turn passed!",Toast.LENGTH_SHORT).show();
                    switchPlayers();
                    isDiceClickable=true;
                    return;
                }
                if (thirdRollLimited) {
                    Toast.makeText(MainActivity.this,
                        "Hot streak! Third roll: 1 to 5", Toast.LENGTH_SHORT).show();
                } else if (ch == 6) {
                    Toast.makeText(MainActivity.this, "SIX! Roll again after your move!",
                        Toast.LENGTH_SHORT).show();
                }
                boolean allHome = true;
                for (Piece piece : getPiecesByColor(currentPlayerColor))
                    if (piece.isAlive || piece.hasCompletedItsPurpose) allHome = false;
                homeRollMisses.put(currentPlayerColor,allHome && ch != 6
                    ? homeRollMisses.getOrDefault(currentPlayerColor,0)+1 : 0);
                setDiceFace(mainDiceImageView,ch);
                isRolling = false;
                // Reward the result visually, never alter the random outcome.
                mainDiceImageView.animate().cancel();
                mainDiceImageView.setScaleX(0.89f);
                mainDiceImageView.setScaleY(0.89f);
                mainDiceImageView.animate().scaleX(1f).scaleY(1f)
                    .setDuration(210).setInterpolator(new OvershootInterpolator(1.15f)).start();

                currentPlayerDice = ch;
                showCentralDiceResult(ch);
                // Ensure active pieces are promoted when their roll is available.

                List<Piece> pieces = getPiecesByColor(currentPlayerColor);
                int x;
                if(currentPlayerIndex==0) { x=players.size()-1; } else { x=currentPlayerIndex-1; }
                Player currentPlayer = players.get(x);
                //Toast.makeText(MainActivity.this, currentPlayer.color+"", Toast.LENGTH_SHORT).show();

                 int chances = 0;
                 List<Piece> movablePieces = new ArrayList<>();
                 // An automatic choice must not leak across different dice rolls.
                 autoMove = null;

                for(Piece p : pieces)
                {
                    if(p.check(ch))
                    {
                        if(chances==0) autoMove = p;
                        else autoMove = null;
                        if(currentPlayer.isBot) { movablePieces.add(p); }
                        ++chances;
                    }
                }

                currentPlayer.chances = chances;

                if(autoMove!=null) {

                    if(autoMove.isAlive && autoMove.isClickable) {
                        autoMove.diceValue = currentPlayerDice;
                        currentPlayerDice = -1;
                        for(Piece p : pieces) { p.inactiveState(); }
                        checkAdjustments(autoMove.currBlock);
                        autoMove.move(ch);
                        autoMove=null;
                        //Toast.makeText(MainActivity.this, x+"", Toast.LENGTH_SHORT).show();
                    }
                    else if(!autoMove.isAlive && currentPlayerDice==6) {
                        currentPlayerDice = -1;
                        for(Piece p : pieces) { p.inactiveState(); }
                        autoMove.makeAlive();
                    }
                } else if(currentPlayer.isBot && chances>0) {
                    int selectedIndex = chooseStrategicBotMove(movablePieces,ch);
                    if (selectedIndex >= 0) {
                        movablePieces.get(selectedIndex).onClickForBot();
                    } else {
                        // Defensive fallback: the move validator remains authoritative.
                        switchPlayers();
                        isDiceClickable = true;
                    }
                } else if (chances == 0) {
                    // A six still grants its extra roll when no pawn can move.
                    // The third roll remains restricted to 1..5 by DiceRules.
                    final boolean extraRoll = ch == 6;
                    final String originalColour = currentPlayerColor;
                    final int expectedGeneration = shuffleTurnGeneration;
                    diceHandler.postDelayed(() -> {
                        if (shuffleMatchEnding || !originalColour.equals(currentPlayerColor)
                            || expectedGeneration != shuffleTurnGeneration) return;
                        if (extraRoll) {
                            isDiceMovableExtraChance = true;
                            if (currentPlayer.isBot) roll();
                            else hintArrow.setVisibility(View.VISIBLE);
                        } else {
                            switchPlayers();
                            isDiceClickable = true;
                        }
                    }, 500);
                }
            }, 420);
            }
    }

    private Drawable getPieceDrawableByColor(String color) {
        switch (color) {
            case "red":
                if (normalPiece) {
                    return ResourcesCompat.getDrawable(getResources(), R.drawable.lhj_token_red, null);
                } else {
                    return getStylishIconDrawableByColor(color);
                }
            case "green":
                if (normalPiece) {
                    return ResourcesCompat.getDrawable(getResources(), R.drawable.lhj_token_green, null);
                } else {
                    return getStylishIconDrawableByColor(color);
                }
            case "blue":
                if (normalPiece) {
                    return ResourcesCompat.getDrawable(getResources(), R.drawable.lhj_token_blue, null);
                } else {
                    return getStylishIconDrawableByColor(color);
                }
            case "yellow":
                if (normalPiece) {
                    return ResourcesCompat.getDrawable(getResources(), R.drawable.lhj_token_yellow, null);
                } else {
                    return getStylishIconDrawableByColor(color);
                }
        } return null;
    }

    private List<Piece> getPiecesByColor(String currentPlayerColor) {
        switch(currentPlayerColor)
        {
            case "red":
                return rp;
            case "green":
                return gp;
            case "blue":
                return bp;
            case "yellow":
                return yp;
        }
        return rp;
    }

    class Player {
        int position;
        String color;
        int index;
        String name;
        boolean isBot;
        TextView playerNameTextView;

        int chances = 0;

        public int getPosition() {
            return position;
        }

        public String getColor() {
            return color;
        }

        public int getDiceValue() {
            return diceValue;
        }

        int diceValue;

        public Player(int position, String color,String playerName,boolean isBot,int index) {
            this.position = position;
            this.color = color;
            this.name = playerName;
            this.index = index;
            this.isBot = isBot;
            playerNameTextView = getPlayerNameByPosition(this.position);
            if(this.isBot) {
                //name = "Bot";
                //playerNameTextView.setText(name);
            }
        }
        public int getIndex() {
            return index;
        }

        void setActive() {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(((int)(pxWidth*0.4)),((int)(pxWidth*0.4)));
            redHomeBlink.setLayoutParams(lp);

            //lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            if(position == 1)
            {    lp.addRule(RelativeLayout.ALIGN_TOP,R.id.imageView); lp.addRule(RelativeLayout.ALIGN_LEFT,R.id.imageView); }
            else if(position == 2)
            {    lp.addRule(RelativeLayout.ALIGN_TOP,R.id.imageView); lp.addRule(RelativeLayout.ALIGN_RIGHT,R.id.imageView); }
            else if(position == 3)
            {    lp.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.imageView); lp.addRule(RelativeLayout.ALIGN_LEFT,R.id.imageView);}
            else if(position == 4)
            {    lp.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.imageView); lp.addRule(RelativeLayout.ALIGN_RIGHT,R.id.imageView);}

            redHomeBlink.setLayoutParams(lp);

            Animation blink = AnimationUtils.loadAnimation(MainActivity.this, R.anim.blinkanimation);
            redHomeBlink.startAnimation(blink);
        }


    }

    private TextView getPlayerNameByPosition(int position) {
        switch (position)
        {
            case 1:
                return playername1;
            case 2:
                return playername2;
            case 3:
                return playername3;
            case 4:
                return playername4;
            default:
                return playername1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_main);
        //setContentView(new GameCanvas(this));
        Objects.requireNonNull(getSupportActionBar()).hide();

        initViews();
        initStatsHud();
        addCentralDiceResultView();

        globalHandler = new Handler();
        offlineMatchStartedAt = System.currentTimeMillis();
        offlineMatchId = UUID.randomUUID().toString();
        selectedLeagueId = getIntent().getIntExtra("league_id",0);
        // Competitive and casual games share the documented balanced dice mode.
        diceMode = BoardAwareDiceEngine.Mode.BALANCED_AGGRESSIVE;
        selectedLeagueName = getIntent().getStringExtra("league_name");
        int[] mappedIds = getIntent().getIntArrayExtra("league_player_ids");
        String[] mappedNames = getIntent().getStringArrayExtra("league_player_names");
        if(mappedIds!=null && mappedNames!=null) {
            for(int i=0;i<Math.min(mappedIds.length,mappedNames.length);i++)
                if(mappedIds[i]>0 && mappedNames[i]!=null)
                    selectedLeaguePlayerIds.put(mappedNames[i],mappedIds[i]);
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(hintArrow, "translationX", -20, 20);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(250);
        animator.start();


        boolean isPLayer1Bot, isPLayer2Bot, isPLayer3Bot, isPLayer4Bot;

        Bundle extras = getIntent().getExtras();
        combatScoringEnabled = extras != null && extras.getBoolean("rule_combat", false);
        immunityEnabled = extras != null && extras.getBoolean("rule_immunity", false);
        homeShuffleEnabled = extras != null && extras.getBoolean("rule_shuffle", false);
        shuffleIntervalSeconds = extras == null ? 300 : Math.max(60, Math.min(600, extras.getInt("shuffle_interval_seconds",300)));

        nop = extras.getInt("nop");
        String color = extras.getString("color");
        player1name = extras.getString("player1name");
        player2name = extras.getString("player2name");
        player3name = extras.getString("player3name");
        player4name = extras.getString("player4name");

        player1color = extras.getString("player1color");
        player2color = extras.getString("player2color");
        player3color = extras.getString("player3color");
        player4color = extras.getString("player4color");

        isPLayer1Bot = extras.getBoolean("player1bot");
        isPLayer2Bot = extras.getBoolean("player2bot");
        isPLayer3Bot = extras.getBoolean("player3bot");
        isPLayer4Bot = extras.getBoolean("player4bot");

        normalPiece = extras.getBoolean("normalPiece");

        gametype = extras.getInt("type"); // 1.classic , 2.teamup , 3.quick , 4.computer

        playername1.setText(player1name);
        playername2.setText(player2name);
        playername3.setText(player3name);
        playername4.setText(player4name);

        reddicevalue = greendicevalue = bluedicevalue = yellowdicevalue = 0;

        int length = dy.length;
        for (int i = 0; i < dy.length; i++) {
            x[i] = (float) ((pxWidth / 100) * dx[i]);
            y[i] = (float) ((pxWidth / 100) * dy[i]);
        }

        float total = onePercentWidth * 40;
        float homeCircleWidth = (float) ((total * 0.64) * 0.25);
        float pieceBlockSize = (float) (pxWidth * 0.065);
        float lifty = (41 * displayMetrics.density - homeCircleWidth) / 2f + 5f;
        pieceWidth = 34 * displayMetrics.density;
        pieceHeight = 41 * displayMetrics.density;
        boardTokenScale = TokenLayoutEngine.boardScale(blockSize, pieceWidth, pieceHeight);
        float pushx;
        if (homeCircleWidth >= pieceWidth) {
            pushx = (float) ((homeCircleWidth - pieceWidth) / 2);
        } else {
            pushx = (float) -((pieceWidth - homeCircleWidth) / 2);
        }
        if (pieceBlockSize >= pieceWidth) {
            pushXForPieces = (float) ((pieceBlockSize - pieceWidth) / 2);
        } else {
            pushXForPieces = (float) -((pieceWidth - pieceBlockSize) / 2);
        }
        pushYForPieces = (pieceHeight - blockSize) / 2f + 5f;
        if (!normalPiece) {
            pushx /= 1.5;
            lifty /= 3;
            pushXForPieces = pushx;
            pushYForPieces = lifty;
        }
        float base = (float) pxWidth - total;

        //

        shieldIndex1.getLayoutParams().height = (int) total;
        shieldIndex2.getLayoutParams().height = (int) total;
        shieldIndex3.getLayoutParams().height = (int) total;
        shieldIndex4.getLayoutParams().height = (int) total;

        p1exitbox.getLayoutParams().height = (int) (total * 0.985);
        p2exitbox.getLayoutParams().height = (int) (total * 0.985);
        p3exitbox.getLayoutParams().height = (int) (total * 0.985);
        p4exitbox.getLayoutParams().height = (int) (total * 0.985);
        p1exitbox.getLayoutParams().width = (int) (total * 0.985);
        p2exitbox.getLayoutParams().width = (int) (total * 0.985);
        p3exitbox.getLayoutParams().width = (int) (total * 0.985);
        p4exitbox.getLayoutParams().width = (int) (total * 0.985);

        if(gametype!=4) {
            playername1.getLayoutParams().width = (int) (total);
            playername1.getLayoutParams().height = (int) (total);
            playername1.setRotation(90);
            playername1.setGravity(Gravity.BOTTOM);
            playername1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            ((ConstraintLayout.LayoutParams)playername1.getLayoutParams()).topMargin = (int) (30 * displayMetrics.density);

            playername2.setRotation(0);
            ((ConstraintLayout.LayoutParams)playername2.getLayoutParams()).topMargin =  (int) (30 * displayMetrics.density);

            playername4.getLayoutParams().width = (int) (total);
            playername4.getLayoutParams().height = (int) (total);
            playername4.setRotation(-90);
            playername4.setGravity(Gravity.BOTTOM);
            playername4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }


        pos1[0][0] = (float) (total * 0.18 + ((total * 0.64) * 0.12)) + pushx;
        pos1[0][1] = (float) (total * 0.18 + ((total * 0.64) * 0.12) - lifty);
        double corDist = total * 0.18 + ((total * 0.64) * 0.12) + ((total * 0.64) * 0.25);
        pos1[1][0] = (float) (total - corDist + pushx);
        pos1[1][1] = (float) pos1[0][1];
        pos1[2][0] = (float) (total * 0.18 + ((total * 0.64) * 0.12) + pushx);
        pos1[2][1] = (float) (total - corDist - lifty);
        pos1[3][0] = (float) pos1[1][0];
        pos1[3][1] = (float) (total - corDist - lifty);

        pos2[0][0] = (float) base + pos1[2][0];
        pos2[0][1] = (float) pos1[0][1];
        pos2[1][0] = (float) base + (pos1[1][0]);
        pos2[1][1] = (float) pos1[0][1];
        pos2[2][0] = (float) base + pos1[2][0];
        pos2[2][1] = (float) (total - corDist - lifty);
        pos2[3][0] = (float) base + (pos1[1][0]);
        pos2[3][1] = (float) (total - corDist - lifty);

        pos3[0][0] = (float) pos1[2][0];
        pos3[0][1] = (float) base + pos1[0][1];
        pos3[1][0] = (float) pos1[1][0];
        pos3[1][1] = (float) base + pos1[0][1];
        pos3[2][0] = (float) pos1[2][0];
        pos3[2][1] = (float) (base + total - corDist - lifty);
        pos3[3][0] = (float) pos1[1][0];
        pos3[3][1] = (float) (base + total - corDist - lifty);

        pos4[0][0] = (float) base + pos1[2][0];
        pos4[0][1] = (float) base + pos1[0][1];
        pos4[1][0] = (float) base + pos1[1][0];
        pos4[1][1] = (float) base + pos1[0][1];
        pos4[2][0] = (float) base + pos1[2][0];
        pos4[2][1] = (float) (base + total - corDist - lifty);
        pos4[3][0] = (float) base + pos1[1][0];
        pos4[3][1] = (float) (base + total - corDist - lifty);

        if (nop == 2) {
            winnerlistp3layout.setVisibility(GONE);
            winnerlistp4layout.setVisibility(GONE);
            wlistwinorlose2.setImageDrawable(getResources().getDrawable(R.drawable.loser1));
            player1name = player3name;
            player2name = player2name;
            switch (color) {
                case "red":
                    ludoBoard.setRotation(0);
                    createPieces("red", 3, player1name, false);
                    createPieces("yellow", 2, player2name, isPLayer2Bot);
                    break;
                case "blue":
                    ludoBoard.setRotation(90);
                    createPieces("blue", 3, player1name, false);
                    createPieces("green", 2, player2name, isPLayer2Bot);
                    break;
                case "green":
                    ludoBoard.setRotation(270);
                    createPieces("green", 3, player1name, false);
                    createPieces("blue", 2, player2name, isPLayer2Bot);
                    break;
                case "yellow":
                    ludoBoard.setRotation(180);
                    createPieces("yellow", 3, player1name, false);
                    createPieces("red", 2, player2name, isPLayer2Bot);
                    break;
            }
        } else if (nop == 3) {
            addPiecesToRmpLayout(color, "Player 4", 4);
            player4color = color;
            switch (color) {
                case "red":
                    ludoBoard.setRotation(270);
                    createPieces("green", 3, player1name, isPLayer1Bot);
                    player1color = "green";
                    createPieces("yellow", 1, player2name, isPLayer2Bot);
                    player2color = "yellow";
                    createPieces("blue", 2, player3name, isPLayer3Bot);
                    player3color = "blue";
                    break;
                case "green":
                    ludoBoard.setRotation(180);
                    createPieces("yellow", 3, player1name, isPLayer1Bot);
                    player1color = "yellow";
                    createPieces("blue", 1, player2name, isPLayer2Bot);
                    player2color = "blue";
                    createPieces("red", 2, player3name, isPLayer3Bot);
                    player3color = "red";
                    break;
                case "blue": // 0
                    ludoBoard.setRotation(0);
                    createPieces("red", 3, player1name, isPLayer1Bot);
                    player1color = "red";
                    createPieces("green", 1, player2name, isPLayer2Bot);
                    player2color = "green";
                    createPieces("yellow", 2, player3name, isPLayer3Bot);
                    player3color = "yellow";
                    break;
                case "yellow":// 90
                    ludoBoard.setRotation(90);
                    createPieces("blue", 3, player1name, isPLayer1Bot);
                    player1color = "blue";
                    createPieces("red", 1, player2name, isPLayer2Bot);
                    player2color = "red";
                    createPieces("green", 2, player3name, isPLayer3Bot);
                    player3color = "green";
                    break;
            }
        } else {
            //Toast.makeText(this, "4", Toast.LENGTH_SHORT).show();
            switch (color) {
                case "red":
                    ludoBoard.setRotation(0);
                    createPieces("red", 3, player1name, isPLayer1Bot);
                    player1color = "red";
                    createPieces("green", 1, player2name, isPLayer2Bot);
                    player2color = "green";
                    createPieces("yellow", 2, player3name, isPLayer3Bot);
                    player3color = "yellow";
                    createPieces("blue", 4, player4name, isPLayer4Bot);
                    player4color = "blue";
                    break;
                case "blue":
                    ludoBoard.setRotation(90);
                    createPieces("blue", 3, player1name, isPLayer1Bot);
                    player1color = "blue";
                    createPieces("red", 1, player2name, isPLayer2Bot);
                    player2color = "red";
                    createPieces("green", 2, player3name, isPLayer3Bot);
                    player3color = "green";
                    createPieces("yellow", 4, player4name, isPLayer4Bot);
                    player4color = "yellow";
                    break;
                case "green":
                    ludoBoard.setRotation(270);
                    createPieces("green", 3, player1name, isPLayer1Bot);
                    player1color = "green";
                    createPieces("yellow", 1, player2name, isPLayer2Bot);
                    player2color = "yellow";
                    createPieces("blue", 2, player3name, isPLayer3Bot);
                    player3color = "blue";
                    createPieces("red", 4, player4name, isPLayer4Bot);
                    player4color = "red";
                    break;
                case "yellow":
                    ludoBoard.setRotation(180);
                    createPieces("yellow", 3, player1name, isPLayer1Bot);
                    player1color = "yellow";
                    createPieces("blue", 1, player2name, isPLayer2Bot);
                    player2color = "blue";
                    createPieces("red", 2, player3name, isPLayer3Bot);
                    player3color = "red";
                    createPieces("green", 4, player4name, isPLayer4Bot);
                    player4color = "green";
                    break;
                default:
                    ludoBoard.setRotation(0);
                    createPieces("blue", 3, player4name, isPLayer4Bot);
                    player1color = "blue";
                    createPieces("red", 1, player1name, isPLayer1Bot);
                    player2color = "red";
                    createPieces("green", 2, player2name, isPLayer2Bot);
                    player3color = "green";
                    createPieces("yellow", 4, player3name, isPLayer3Bot);
                    player4color = "yellow";
            }
        }
        View.OnTouchListener clickEffect = new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((ImageView) v).setColorFilter(ResourcesCompat.getColor(getResources(), R.color.dim_color, null), PorterDuff.Mode.SRC_ATOP);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        ((ImageView) v).clearColorFilter();
                        break;
                }
                return false;
            }
        };

        ingamemenubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ingamemenuitemslayout.getVisibility() == View.VISIBLE) {
                    ingamemenuitemslayout.setVisibility(GONE);
                } else {
                    ingamemenuitemslayout.setVisibility(View.VISIBLE);
                }
            }
        });

        menuremoveplayersbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingamemenuitemslayout.setVisibility(GONE);
                ingamermplayout.setScaleX(0.0f);
                ingamermplayout.setScaleY(0.0f);
                ingamermplayout.setVisibility(View.VISIBLE);
                ingamermplayout.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(new OvershootInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ingamermplayout.animate().setListener(null);
                    }
                }).start();
            }
        });

        menuexitbtn.setOnClickListener(view -> {
            ingamemenuitemslayout.setVisibility(GONE);
            RoyalDialog.confirm(MainActivity.this, "LEAVE MATCH?",
                "Your unfinished game will be lost if you leave.",
                "EXIT MATCH", () -> {
                    stopEverything();
                    Intent i = new Intent(MainActivity.this,HomeActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                });
        });

        // exit layout views
        quitgamelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        ingameyesbtn.setOnTouchListener(clickEffect);
        ingamenobtn.setOnTouchListener(clickEffect);
        ingamesoundbtn.setOnTouchListener(clickEffect);
        ingamemusicbtn.setOnTouchListener(clickEffect);

        ingameyesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopEverything();
                Intent i = new Intent(MainActivity.this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        ingamenobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitgamelayout.animate().scaleX(0.0f).scaleY(0.0f).setInterpolator(new AnticipateInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        quitgamelayout.setVisibility(GONE);
                    }
                }).start();
            }
        });

        ingamesoundbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSoundOn) {
                    isSoundOn = false;
                    sharedPreferences.edit().putBoolean("sound",false).apply();
                    ingamesoundbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.soundoff,null));
                    if(congratulationSound!=null) { if(congratulationSound.isPlaying()) { congratulationSound.pause(); } }
                } else {
                    isSoundOn = true;
                    sharedPreferences.edit().putBoolean("sound",true).apply();
                    ingamesoundbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.soundon,null));
                    if(congratulationSound!=null) { if(!congratulationSound.isPlaying()) { congratulationSound.start(); } }
                }
            }
        });

        ingamemusicbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMusicOn) {
                    isMusicOn = false;
                    sharedPreferences.edit().putBoolean("music",false).apply();
                    ingamemusicbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.musicoff,null));
                } else {
                    isMusicOn = true;
                    sharedPreferences.edit().putBoolean("music",true).apply();
                    ingamemusicbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.musicon,null));
                }
            }
        });

        // rmplayout views
        rmpbackbtn.setOnTouchListener(clickEffect);
        rmpmenubtn.setOnTouchListener(clickEffect);
        ingamermplayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        confirmrmplayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        String finalPlayer1color = player1color;
        rmpp1removeicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmrmplayout.setScaleX(0.0f);
                confirmrmplayout.setScaleY(0.0f);
                confirmrmplayout.setVisibility(View.VISIBLE);
                confirmrmplayout.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(new OvershootInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        confirmrmplayout.animate().setListener(null);
                    }
                }).start();
                selectedconfirmrmpplayerindex = 1;
                selectedrmppiece.setImageDrawable(getPieceDrawableByColor(finalPlayer1color));
            }
        });

        String finalPlayer2color = player2color;
        rmpp2removeicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmrmplayout.setScaleX(0.0f);
                confirmrmplayout.setScaleY(0.0f);
                confirmrmplayout.setVisibility(View.VISIBLE);
                confirmrmplayout.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(new OvershootInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        confirmrmplayout.animate().setListener(null);
                    }
                }).start();
                selectedconfirmrmpplayerindex = 2;

                selectedrmppiece.setImageDrawable(getPieceDrawableByColor(finalPlayer2color));
            }
        });

        String finalPlayer3color = player3color;
        rmpp3removeicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmrmplayout.setScaleX(0.0f);
                confirmrmplayout.setScaleY(0.0f);
                confirmrmplayout.setVisibility(View.VISIBLE);
                confirmrmplayout.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(new OvershootInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        confirmrmplayout.animate().setListener(null);
                    }
                }).start();
                selectedconfirmrmpplayerindex = 3;
                selectedrmppiece.setImageDrawable(getPieceDrawableByColor(finalPlayer3color));
            }
        });

        String finalPlayer4color = player4color;
        rmpp4removeicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmrmplayout.setScaleX(0.0f);
                confirmrmplayout.setScaleY(0.0f);
                confirmrmplayout.setVisibility(View.VISIBLE);
                confirmrmplayout.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(new OvershootInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        confirmrmplayout.animate().setListener(null);
                    }
                }).start();
                selectedconfirmrmpplayerindex = 4;
                selectedrmppiece.setImageDrawable(getPieceDrawableByColor(finalPlayer4color));
            }
        });

        if (nop == 3) {
            rmp4bg.setAlpha(0.5f);
            rmpp4removeicon.setOnClickListener(null);
        }

        rmpbackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingamermplayout.animate().scaleX(0.0f).scaleY(0.0f).setInterpolator(new AnticipateInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ingamermplayout.setVisibility(GONE);
                    }
                }).start();
            }
        });

        rmpmenubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuexitbtn.callOnClick();
            }
        });

        // confirm rmp layout views
        confirmrmpyesbtn.setOnTouchListener(clickEffect);
        confirmrmpnobtn.setOnTouchListener(clickEffect);
        confirmrmpyesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeThisPlayerByIndex(selectedconfirmrmpplayerindex);
                confirmrmplayout.setVisibility(GONE);
                ingamermplayout.setVisibility(GONE);
            }
        });

        confirmrmpnobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmrmplayout.animate().scaleX(0.0f).scaleY(0.0f).setInterpolator(new AnticipateInterpolator()).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        confirmrmplayout.setVisibility(GONE);
                    }
                }).start();
            }
        });

        // Congratulations screen
        congratulationslayout.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {}});

        congratsmenubtn.setOnTouchListener(clickEffect);
        congratsreplaybtn.setOnTouchListener(clickEffect);
        congratssoundbtn.setOnTouchListener(clickEffect);
        congratssharebtn.setOnTouchListener(clickEffect);
        congratsmenubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingameyesbtn.callOnClick();
            }
        });

        congratssoundbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSoundOn) {
                    isSoundOn = false;
                    sharedPreferences.edit().putBoolean("sound",false).apply();
                    congratssoundbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.soundoff,null));
                    if(congratulationSound!=null) { if(congratulationSound.isPlaying()) { congratulationSound.pause(); } }
                } else {
                    isSoundOn = true;
                    sharedPreferences.edit().putBoolean("sound",true).apply();
                    congratssoundbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.soundon,null));
                    if(congratulationSound!=null) { if(!congratulationSound.isPlaying()) { congratulationSound.start(); } }
                }
            }
        });

        congratssharebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        congratsreplaybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopEverything();
                Intent i = getIntent();
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtras(i.getExtras());
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        if(gametype==1) {
            findViewById(R.id.constraintLayout8).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView114).setVisibility(View.GONE);
            findViewById(R.id.teamslayout).setVisibility(View.GONE);
        }

        if(gametype==2) {
            findViewById(R.id.constraintLayout8).setVisibility(GONE);
            findViewById(R.id.imageView114).setVisibility(View.GONE);
            findViewById(R.id.teamslayout).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.imageView132)).setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.team1header,null));

            ((ImageView)findViewById(R.id.imageView137)).setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.team2header,null));

            ((ImageView)findViewById(R.id.imageView133)).setImageDrawable(getPieceDrawableByColor("blue"));
            ((ImageView)findViewById(R.id.imageView135)).setImageDrawable(getPieceDrawableByColor("green"));
            ((ImageView)findViewById(R.id.imageView138)).setImageDrawable(getPieceDrawableByColor("red"));
            ((ImageView)findViewById(R.id.imageView140)).setImageDrawable(getPieceDrawableByColor("yellow"));

            ((TextView)findViewById(R.id.team1name1)).setText(player3name);
            ((TextView)findViewById(R.id.team1name2)).setText(player2name);
            ((TextView)findViewById(R.id.team2name1)).setText(player1name);
            ((TextView)findViewById(R.id.team2name2)).setText(player4name);
        }

        if (gametype == 3) {
            findViewById(R.id.constraintLayout8).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView114).setVisibility(GONE);
            findViewById(R.id.teamslayout).setVisibility(GONE);
            for (Player p : players) {
                List<Piece> pieces = getPiecesByColor(p.getColor());
                for (int i = 0; i < 2; i++) {
                    pieces.get(i).makeAlive();
                }
                globalHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkAdjustments(pieces.get(0).startPosition);
                    }
                },420);
            }
        }

        if(gametype==4) {
            findViewById(R.id.constraintLayout8).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView114).setVisibility(View.VISIBLE);
            findViewById(R.id.teamslayout).setVisibility(GONE);
        }


            Runnable blinkAnim = new Runnable() {
                @Override
                public void run() {
                    if(gameStartImageView.getVisibility()==View.INVISIBLE) {
                        gameStartImageView.setVisibility(View.VISIBLE);
                    } else {
                        gameStartImageView.setVisibility(View.INVISIBLE);
                    }
                    globalHandler.postDelayed(this,220);
                }
            };

            globalHandler.post(blinkAnim);

            gameStartSound = MediaPlayer.create(this,R.raw.gamestartsound);
            gameStartSound.start();
            gameStartSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    globalHandler.removeCallbacks(blinkAnim);
                    gameStartImageView.setVisibility(GONE);
                    mainDiceImageView.setVisibility(View.VISIBLE);
                    hintArrow.setVisibility(View.VISIBLE);
                    switchPlayers();
                    gameStartSound.release();
                }
            });
            mainDiceImageView.setVisibility(GONE);
            hintArrow.setVisibility(GONE);
            d = new Dice(mainDiceImageView, nop, color);
            initializeSeatDiceViews();
            initialiseHomeShuffle();
            // Generate the same 15x15 home finish for standard and shuffle games.
            // The game's original tile positions and input controls are unchanged.
            android.graphics.Bitmap baseBoard=android.graphics.BitmapFactory.decodeResource(
                getResources(),R.drawable.ludofinalboard3);
            if(baseBoard!=null) {
                android.graphics.Bitmap premiumBoard=
                    com.ludomaster.reference.shuffle.LhjPremiumBoardRenderer.enhance(baseBoard);
                baseBoard.recycle();
                ludoBoard.setImageBitmap(premiumBoard);
            }
            if (homeShuffleEnabled) { refreshShuffleDicePanels(); addShuffleCountdown(); }
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(((int)(pxWidth*0.4)),((int)(pxWidth*0.4)));
        redHomeBlink.setLayoutParams(lp);
        lp.addRule(RelativeLayout.ALIGN_TOP,R.id.imageView); lp.addRule(RelativeLayout.ALIGN_LEFT,R.id.imageView);

        }



    private int shuffleColourIndex(String colour) {
        switch (colour) {
            case "red": return 0;
            case "green": return 1;
            case "blue": return 2;
            case "yellow": return 3;
            default: throw new IllegalArgumentException("colour");
        }
    }

    private int shuffleSeatForSlot(int slot) {
        return new int[]{2,4,3,1}[slot];
    }

    private java.util.List<Piece> shufflePiecesForColour(int index) {
        switch (index) {
            case 0: return rp;
            case 1: return gp;
            case 2: return bp;
            case 3: return yp;
            default: throw new IllegalArgumentException("colour index");
        }
    }

    private void initialiseHomeShuffle() {
        if (!homeShuffleEnabled) return;
        homeShuffleEngine = new com.ludomaster.reference.shuffle.HomeShuffleEngine(System.nanoTime());
        int[] existing = {-1,-1,-1,-1};
        boolean[] taken = new boolean[4];
        for (int colour=0; colour<4; colour++) {
            java.util.List<Piece> pieces = shufflePiecesForColour(colour);
            if (pieces == null || pieces.isEmpty()) continue;
            int start = pieces.get(0).startPosition;
            for (int slot=0; slot<4; slot++) {
                if (com.ludomaster.reference.shuffle.HomeShuffleEngine.START[slot] == start) {
                    if (taken[slot]) throw new IllegalStateException("Duplicate starting home");
                    existing[colour]=slot; taken[slot]=true; break;
                }
            }
            if (existing[colour] < 0) throw new IllegalStateException("Unknown physical start "+start);
        }
        for (int colour=0; colour<4; colour++) {
            if (existing[colour] >= 0) continue;
            for (int slot=0; slot<4; slot++) {
                if (!taken[slot]) { existing[colour]=slot; taken[slot]=true; break; }
            }
        }
        homeShuffleEngine.restoreAssignment(existing);
        nextShuffleDueAtMs = android.os.SystemClock.elapsedRealtime()+shuffleIntervalSeconds*1000L;
        // Start the scheduler when gameplay initializes; onResume alone runs too early.
        globalHandler.removeCallbacks(shuffleTimerTick);
        globalHandler.postDelayed(shuffleTimerTick, 1000);
    }

    /** Read-only countdown: pending shuffle applies after a completed move. */
    private void addShuffleCountdown() {
        ConstraintLayout root = findViewById(R.id.activitybg);
        shuffleCountdownView = new TextView(this);
        shuffleCountdownView.setTextColor(Color.WHITE);
        shuffleCountdownView.setTextSize(12);
        shuffleCountdownView.setGravity(Gravity.CENTER);
        shuffleCountdownView.setBackgroundColor(0xff8c398a);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(hudDp(126),hudDp(45));
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topMargin = hudDp(16);
        root.addView(shuffleCountdownView, params);
        updateShuffleCountdown(android.os.SystemClock.elapsedRealtime());
        shuffleCountdownView.bringToFront();
    }

    private void updateShuffleCountdown(long now) {
        if (shuffleCountdownView == null || homeShuffleEngine == null) return;
        if (homeShuffleEngine.isPending()) {
            shuffleCountdownView.setText("SHUFFLE PENDING");
        } else {
            long seconds = Math.max(0L, (nextShuffleDueAtMs - now + 999L) / 1000L);
            shuffleCountdownView.setText(String.format(java.util.Locale.US,
                "SHUFFLE %02d:%02d", seconds / 60L, seconds % 60L));
        }
    }

    /** In Shuffle mode any seat may become occupied, including originally unused seats. */

    /** Non-clickable player-specific dice, never replacing the active roll control. */
    private void initializeSeatDiceViews() {
        if(!seatDiceViews.isEmpty())return;
        int[] pads={R.id.tleftdicebg,R.id.trightdicebg,
                    R.id.bleftdicebg,R.id.brightdicebg};
        for(int i=0;i<4;i++){
            ConstraintLayout pad=findViewById(pads[i]);
            ImageView die=new ImageView(this);
            die.setScaleType(ImageView.ScaleType.FIT_CENTER);
            die.setClickable(false);
            die.setFocusable(false);
            ConstraintLayout.LayoutParams p=new ConstraintLayout.LayoutParams(hudDp(54.6f),hudDp(54.6f));
            p.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
            p.bottomToBottom=ConstraintLayout.LayoutParams.PARENT_ID;
            if(i%2==0) {
                p.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;
                p.setMarginEnd(hudDp(11));
            } else {
                p.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;
                p.setMarginStart(hudDp(9));
            }
            pad.addView(die,p);
            seatDiceViews.put(i+1,die);
        }
        refreshSeatDiceViews();
    }

    private void refreshSeatDiceViews() {
        for(ImageView die:seatDiceViews.values())die.setVisibility(View.GONE);
        for(Player player:players){
            ImageView die=seatDiceViews.get(player.position);
            if(die==null)continue;
            setDiceFace(die,1);
            die.setContentDescription(player.name+" "+player.color+" dice");
            die.setVisibility(player.color.equals(currentPlayerColor)?View.INVISIBLE:View.VISIBLE);
        }
    }

    private void refreshShuffleDicePanels() {
        if (!homeShuffleEnabled || d == null) return;
        d.tLeftLayout.setVisibility(View.GONE);
        d.tRightLayout.setVisibility(View.GONE);
        d.bLeftLayout.setVisibility(View.GONE);
        d.bRightLayout.setVisibility(View.GONE);
        d.tLeftImgColor.setVisibility(View.INVISIBLE);
        d.tRightImgColor.setVisibility(View.INVISIBLE);
        d.bLeftImgColor.setVisibility(View.INVISIBLE);
        d.bRightImgColor.setVisibility(View.INVISIBLE);
        for (Player player : players) {
            ImageView icon;
            // All dice icons are bound to the same seat map as pawn home yards.
            switch (player.position) {
                case 1: icon = d.tLeftImgColor; d.tLeftLayout.setVisibility(View.VISIBLE); break;
                case 2: icon = d.tRightImgColor; d.tRightLayout.setVisibility(View.VISIBLE); break;
                case 3: icon = d.bLeftImgColor; d.bLeftLayout.setVisibility(View.VISIBLE); break;
                case 4: icon = d.bRightImgColor; d.bRightLayout.setVisibility(View.VISIBLE); break;
                default: continue;
            }
            icon.setImageDrawable(getPieceDrawableByColor(player.color));
            icon.setVisibility(View.VISIBLE);
        }
        refreshSeatDiceViews();
    }

    private void applyPendingHomeShuffle() {
        if (!homeShuffleEnabled || homeShuffleEngine == null || shuffleMatchEnding ||
            !homeShuffleEngine.applyAtSafeBoundary(false)) return;
        for (int colour=0; colour<4; colour++) {
            int slot=homeShuffleEngine.homeSlot(colour);
            int newStart=homeShuffleEngine.startSquare(colour);
            int newEntrance=homeShuffleEngine.entranceSquare(colour);
            int seat = shuffleSeatForSlot(slot);
            float[][] yard = seat == 1 ? pos1 : seat == 2 ? pos2 : seat == 3 ? pos3 : pos4;
            java.util.List<Piece> pieces=shufflePiecesForColour(colour);
            for (int index=0; index<pieces.size(); index++) {
                Piece p=pieces.get(index);
                p.startPosition=newStart;
                p.endPosition=newEntrance;
                p.defX=yard[index][0]; p.defY=yard[index][1];
                if (p.hasCompletedItsPurpose) {
                    p.winnerBlocks = getWinnerBlocks(newEntrance);
                    p.currBlock = p.winnerBlocks[5];
                    p.piece.animate().translationX(x[p.currBlock]+pushXForPieces)
                        .translationY(y[p.currBlock]-pushYForPieces).setDuration(350).start();
                    continue;
                }
                if (!p.isAlive) {
                    p.piece.animate().translationX(p.defX).translationY(p.defY).setDuration(350).start();
                } else if (p.isReadyToEnterWinnerZone && p.currWinnerBlock > 0) {
                    p.winnerBlocks=getWinnerBlocks(newEntrance);
                    p.currBlock=p.winnerBlocks[p.currWinnerBlock-1];
                    p.piece.animate().translationX(x[p.currBlock]+pushXForPieces)
                        .translationY(y[p.currBlock]-pushYForPieces).setDuration(350).start();
                } else if(p.currBlock >= 0 && p.currBlock < 52) {
                    int steps=com.ludomaster.reference.shuffle.HomeShuffleEngine.forwardDistance(
                        p.currBlock,newEntrance);
                    p.stepsUntilShuffleEntrance = steps == 0 ? 52 : steps;
                    p.isReadyToEnterWinnerZone=false;
                    p.currWinnerBlock=0;
                }
            }
        }
        globalHandler.postDelayed(this::refreshAllTokenLayouts, 360);
        final int[] ownership = homeShuffleEngine.assignment();
        final float rotation = ludoBoard.getRotation();
        final int repaintVersion = ++shuffleRepaintVersion;
        new Thread(() -> {
            final android.graphics.Bitmap bitmap =
                com.ludomaster.reference.shuffle.HomeBoardPainter.render(
                    getResources(),R.drawable.ludofinalboard3,ownership,rotation);
            runOnUiThread(() -> {
                if (!shuffleMatchEnding && !isFinishing() && repaintVersion == shuffleRepaintVersion) {
                    ludoBoard.setImageBitmap(bitmap);
                } else bitmap.recycle();
            });
        }, "shuffle-board-repaint").start();
        ++shuffleTurnGeneration;
        for (Player player : players) {
            player.position = shuffleSeatForSlot(homeShuffleEngine.homeSlot(shuffleColourIndex(player.color)));
            player.playerNameTextView = getPlayerNameByPosition(player.position);
        }
        for (int seat = 1; seat <= 4; seat++) getPlayerNameByPosition(seat).setText("");
        for (Player player : players) {
            if (player.position >= 1 && player.position <= 4)
                getPlayerNameByPosition(player.position).setText(player.name);
        }
        refreshShuffleDicePanels();
        // Preserve the exact player who owned the dice when homes swapped.
        if (currentPlayerColor != null && !currentPlayerColor.isEmpty()) {
            currentPlayerPosition = shuffleSeatForSlot(
                homeShuffleEngine.homeSlot(shuffleColourIndex(currentPlayerColor)));
            moveDice(currentPlayerPosition);
        }
        updateShuffleCountdown(android.os.SystemClock.elapsedRealtime());
        Toast.makeText(this,"Home Shuffle! Every colour changed home.",Toast.LENGTH_SHORT).show();
    }

    private void stopEverything() {
        shuffleMatchEnding = true;
        globalHandler.removeCallbacksAndMessages(null);
        d.diceHandler.removeCallbacksAndMessages(null);
        if(congratulationSound!=null) {
            if(congratulationSound.isPlaying()) {
                congratulationSound.stop();
            }
            try { congratulationSound.release(); } catch (Exception e) {}
        }
    }

    @Override
    protected void onPause() {
        if (globalHandler != null) globalHandler.removeCallbacks(shuffleTimerTick);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        shuffleMatchEnding = true;
        if (globalHandler != null) globalHandler.removeCallbacks(shuffleTimerTick);
        if (globalHandler != null) globalHandler.removeCallbacks(hideCentralDiceResult);
        super.onDestroy();
    }

    private void hideThisPlayerDiceBg(int index) {
        switch (index) {
            case 1:
                findViewById(R.id.bleftdicebg).setVisibility(GONE);
                break;
            case 2:
                findViewById(R.id.tleftdicebg).setVisibility(GONE);
                break;
            case 3:
                findViewById(R.id.trightdicebg).setVisibility(GONE);
                break;
            case 4:
                findViewById(R.id.brightdicebg).setVisibility(GONE);
                break;
        }
    }

    private void removeThisPlayerByIndex(int index) {
        if(currentPlayerIndex!=0) { currentPlayerIndex--; }
        switch (index) {
            case 1:
                rmp1bg.setAlpha(0.5f);
                rmpp1removeicon.setOnClickListener(null);
                if(currentPlayerIndex==(rmpindexforp1-1)) {
                    players.remove(rmpindexforp1-1);
                    rmpindexforp2--;
                    rmpindexforp3--;
                    rmpindexforp4--;
                    switchPlayers();
                } else {
                    currentPlayerIndex++;
                    players.remove(rmpindexforp1-1);
                    rmpindexforp3--;
                    rmpindexforp4--;
                }
                hideThisPlayerDiceBg(1);
                for(Piece p : getPiecesByColor(player1color)) {
                    p.piece.setVisibility(GONE);
                }
                p1exitbox.setImageDrawable(getExitBoxByColor(player1color));
                p1exitbox.setVisibility(View.VISIBLE);
                break;
            case 2:
                rmp2bg.setAlpha(0.5f);
                rmpp2removeicon.setOnClickListener(null);
                if(currentPlayerIndex==(rmpindexforp2-1)) {
                    players.remove(rmpindexforp2-1);
                    rmpindexforp3--;
                    rmpindexforp4--;
                    switchPlayers();
                } else {
                    currentPlayerIndex++;
                    players.remove(rmpindexforp2-1);
                    rmpindexforp3--;
                    rmpindexforp4--;
                }
                hideThisPlayerDiceBg(2);
                for(Piece p : getPiecesByColor(player2color)) {
                    p.piece.setVisibility(GONE);
                }
                p2exitbox.setImageDrawable(getExitBoxByColor(player2color));
                p2exitbox.setVisibility(View.VISIBLE);
                break;
            case 3:
                rmp3bg.setAlpha(0.5f);
                rmpp3removeicon.setOnClickListener(null);
                if(currentPlayerIndex==(rmpindexforp3-1)) {
                    players.remove(rmpindexforp3-1);
                    rmpindexforp4--;
                    switchPlayers();
                } else {
                    currentPlayerIndex++;
                    players.remove(rmpindexforp3-1);
                    rmpindexforp4--;
                }
                hideThisPlayerDiceBg(3);
                for(Piece p : getPiecesByColor(player3color)) {
                    p.piece.setVisibility(GONE);
                }
                p3exitbox.setImageDrawable(getExitBoxByColor(player3color));
                p3exitbox.setVisibility(View.VISIBLE);
                break;
            case 4:
                rmp4bg.setAlpha(0.5f);
                rmpp4removeicon.setOnClickListener(null);
                if(currentPlayerIndex==(rmpindexforp4-1-1)) {
                    players.remove(rmpindexforp4-1);
                    switchPlayers();
                } else {
                    currentPlayerIndex++;
                    players.remove(rmpindexforp4-1);
                }
                hideThisPlayerDiceBg(4);
                for(Piece p : getPiecesByColor(player4color)) {
                    p.piece.setVisibility(GONE);
                }
                p4exitbox.setImageDrawable(getExitBoxByColor(player4color));
                p4exitbox.setVisibility(View.VISIBLE);
                break;
        }

        if(players.size()==2) {
            menuremoveplayersbtn.setVisibility(GONE);
        }

    }

    private Drawable getExitBoxByColor(String color) {
        switch (color)
        {
            case "red":
                return ResourcesCompat.getDrawable(getResources(),R.drawable.redexitbox,null);
            case "blue":
                return ResourcesCompat.getDrawable(getResources(),R.drawable.blueexitbox,null);
            case "green":
                return ResourcesCompat.getDrawable(getResources(),R.drawable.greenexitbox,null);
            case "yellow":
                return ResourcesCompat.getDrawable(getResources(),R.drawable.yellowexitbox,null);
        }
        return ResourcesCompat.getDrawable(getResources(),R.drawable.redexitbox,null);
    }

    void addPiecesToRmpLayout(String color,String name,int index) {
        switch (index) {
            case 1:
                rmpp1piece.setImageDrawable(getPieceDrawableByColor(color));
                rmpp1name.setText(name);
                break;
            case 2:
                rmpp2piece.setImageDrawable(getPieceDrawableByColor(color));
                rmpp2name.setText(name);
                break;
            case 3:
                rmpp3piece.setImageDrawable(getPieceDrawableByColor(color));
                rmpp3name.setText(name);
                break;
            case 4:
                rmpp4piece.setImageDrawable(getPieceDrawableByColor(color));
                rmpp4name.setText(name);
                break;
        }
    }


    void switchPlayers()
    {
        diceRules.startTurn();
        currentPlayerDice = -1;
        autoMove = null;
        ++shuffleTurnGeneration;
        Player currentPlayer = players.get(currentPlayerIndex);
        currentPlayerPosition = currentPlayer.getPosition();
        currentPlayerColor = currentPlayer.getColor();
        refreshSeatDiceViews();
        if(mainDiceImageView != null)
            setDiceFace(mainDiceImageView,1);
        currentPlayerSelectedIndex = currentPlayer.getIndex();
        currentPlayerName = currentPlayer.name;
        //Toast.makeText(this, currentPlayerColor+"", Toast.LENGTH_SHORT).show();
        currentPlayer.setActive();
        if(currentPlayer.isBot) {
            hintArrow.setVisibility(GONE);
            moveDice(currentPlayerPosition);
            final String expectedColour = currentPlayerColor;
            final int expectedTurn = ++shuffleTurnGeneration;
            globalHandler.postDelayed(() -> {
                if (!shuffleMatchEnding && expectedTurn == shuffleTurnGeneration &&
                    expectedColour.equals(currentPlayerColor) && d != null) d.roll();
            },150);
        } else {
            moveDice(currentPlayerPosition);
            hintArrow.setVisibility(View.VISIBLE);
        }

        if(currentPlayerIndex>=(players.size()-1)) { currentPlayerIndex = 0; } else { currentPlayerIndex++; }
    }

    void moveDice(int position)
    {
        // A reused LayoutParams object keeps old constraints across home swaps.
        // Clear every anchor before rebuilding the position for the active colour.
        ConstraintLayout.LayoutParams diceParams =
            (ConstraintLayout.LayoutParams) mainDiceImageView.getLayoutParams();
        diceParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
        diceParams.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        diceParams.endToStart = ConstraintLayout.LayoutParams.UNSET;
        diceParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
        diceParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
        diceParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        diceParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        diceParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        ConstraintLayout.LayoutParams hintParams =
            (ConstraintLayout.LayoutParams) hintArrow.getLayoutParams();
        hintParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
        hintParams.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        hintParams.endToStart = ConstraintLayout.LayoutParams.UNSET;
        hintParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
        hintParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
        hintParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        hintParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        hintParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        switch(position)
        {
            case 1:
                moveToTopLeft();
                break;
            case 2:
                moveToTopRight();
                break;
            case 3:
                moveToBottomLeft();
                break;
            case 4:
                moveToBottomRight();
                break;
        }

    }

    private void createPieces(String color, int position,String playername,boolean isBot) {
        offlinePlayerNames.put(color, playername == null ? color : playername);
        refreshStatsHud();
        if(nop==2) {
            if(players.size()==0)
            { players.add(new Player(position, color, playername, isBot, 0)); }
            else
            { players.add(new Player(position, color, playername, isBot, 2)); }
        }else {
            players.add(new Player(position, color, playername, isBot, players.size()));
            addPiecesToRmpLayout(color,getPlayerNameByPosition(position).getText().toString(),players.size());
        }
        float pos[][];
        int sPos[] = {39,0,26,13};
        switch (position) {case 1: pos = pos1; break; case 2: pos = pos2; break; case 3: pos = pos3; break; case 4: pos = pos4; break; default: pos = new float[4][2]; }
        switch (color) {
            case "red":
                rp.add(new Piece("red", redpiece1, findViewById(R.id.red1activecircle), findViewById(R.id.imageView6), findViewById(R.id.imageView7),  sPos[position - 1], pos[0][0], pos[0][1],isBot));
                rp.add(new Piece("red", redpiece2, findViewById(R.id.red2activecircle), findViewById(R.id.imageView62), findViewById(R.id.imageView72),  sPos[position - 1], pos[1][0], pos[1][1],isBot));
                rp.add(new Piece("red", redpiece3, findViewById(R.id.red3activecircle), findViewById(R.id.imageView63), findViewById(R.id.imageView73),  sPos[position - 1], pos[2][0], pos[2][1],isBot));
                rp.add(new Piece("red", redpiece4, findViewById(R.id.red4activecircle), findViewById(R.id.imageView64), findViewById(R.id.imageView74),  sPos[position - 1], pos[3][0], pos[3][1],isBot));
                break;
            case "green":
                gp.add(new Piece("green", greenpiece1, findViewById(R.id.green1activecircle), findViewById(R.id.imageView51), findViewById(R.id.imageView81),  sPos[position - 1], pos[0][0], pos[0][1],isBot));
                gp.add(new Piece("green", greenpiece2, findViewById(R.id.green2activecircle), findViewById(R.id.imageView52), findViewById(R.id.imageView82),  sPos[position - 1], pos[1][0], pos[1][1],isBot));
                gp.add(new Piece("green", greenpiece3, findViewById(R.id.green3activecircle), findViewById(R.id.imageView53), findViewById(R.id.imageView83),  sPos[position - 1], pos[2][0], pos[2][1],isBot));
                gp.add(new Piece("green", greenpiece4, findViewById(R.id.green4activecircle), findViewById(R.id.imageView54), findViewById(R.id.imageView84),  sPos[position - 1], pos[3][0], pos[3][1],isBot));
                break;
            case "blue":
                bp.add(new Piece("blue", bluepiece1, findViewById(R.id.blue1activecircle), findViewById(R.id.imageView41), findViewById(R.id.imageView91),  sPos[position - 1], pos[0][0], pos[0][1],isBot));
                bp.add(new Piece("blue", bluepiece2, findViewById(R.id.blue2activecircle), findViewById(R.id.imageView42), findViewById(R.id.imageView92),  sPos[position - 1], pos[1][0], pos[1][1],isBot));
                bp.add(new Piece("blue", bluepiece3, findViewById(R.id.blue3activecircle), findViewById(R.id.imageView43), findViewById(R.id.imageView93),  sPos[position - 1], pos[2][0], pos[2][1],isBot));
                bp.add(new Piece("blue", bluepiece4, findViewById(R.id.blue4activecircle), findViewById(R.id.imageView44), findViewById(R.id.imageView94),  sPos[position - 1], pos[3][0], pos[3][1],isBot));
                break;
            case "yellow":
                yp.add(new Piece("yellow", yellowpiece1, findViewById(R.id.yellow1activecircle), findViewById(R.id.imageView31), findViewById(R.id.imageView101),  sPos[position - 1], pos[0][0], pos[0][1],isBot));
                yp.add(new Piece("yellow", yellowpiece2, findViewById(R.id.yellow2activecircle), findViewById(R.id.imageView32), findViewById(R.id.imageView102),  sPos[position - 1], pos[1][0], pos[1][1],isBot));
                yp.add(new Piece("yellow", yellowpiece3, findViewById(R.id.yellow3activecircle), findViewById(R.id.imageView33), findViewById(R.id.imageView103),  sPos[position - 1], pos[2][0], pos[2][1],isBot));
                yp.add(new Piece("yellow", yellowpiece4, findViewById(R.id.yellow4activecircle), findViewById(R.id.imageView34), findViewById(R.id.imageView104),  sPos[position - 1], pos[3][0], pos[3][1],isBot));
                break;
        }
    }

    View getStepBubble(int n)
    {
        switch (n)
        {
            case 1:
                return step1;
            case 2:
                return step2;
            case 3:
                return step3;
            case 4:
                return step4;
            case 5:
                return step5;
            case 6:
                return step6;
        }
        return step1;
    }

    void initViews()
    {
        sharedPreferences = getSharedPreferences("LudoModUser",MODE_PRIVATE);

        botwins= sharedPreferences.getInt("botwins",0);
        botloses=sharedPreferences.getInt("botloses",0);
        ((TextView)findViewById(R.id.userwins)).setText((botwins+""));
        ((TextView)findViewById(R.id.userloses)).setText((botloses+""));

        isSoundOn = sharedPreferences.getBoolean("sound",true);
        isMusicOn = sharedPreferences.getBoolean("music",false);

        completeBackground = findViewById(R.id.activitybg);
        displayMetrics = getResources().getDisplayMetrics();
        dpHeight = displayMetrics.heightPixels/displayMetrics.density;
        dpWidth = displayMetrics.widthPixels/displayMetrics.density;
        pxWidth = displayMetrics.widthPixels;
        pxHeight = displayMetrics.heightPixels-getStatusBarHeight();
        ludoBoard = findViewById(R.id.imageView);
        // Keep original board markings unobstructed until exact-fit 3D board artwork is ready.
        red1activecircle = findViewById(R.id.red1activecircle);
        red2activecircle = findViewById(R.id.red2activecircle);
        red3activecircle = findViewById(R.id.red3activecircle);
        red4activecircle = findViewById(R.id.red4activecircle);
        redHomeBlink = findViewById(R.id.imageView3);
        redpiece1 = findViewById(R.id.redpiece1);
        redpiece2 = findViewById(R.id.redpiece2);
        redpiece3 = findViewById(R.id.redpiece3);
        redpiece4 = findViewById(R.id.redpiece4);

        greenpiece1 = findViewById(R.id.greenpiece1);
        greenpiece2 = findViewById(R.id.greenpiece2);
        greenpiece3 = findViewById(R.id.greenpiece3);
        greenpiece4 = findViewById(R.id.greenpiece4);

        bluepiece1 = findViewById(R.id.bluepiece1);
        bluepiece2 = findViewById(R.id.bluepiece2);
        bluepiece3 = findViewById(R.id.bluepiece3);
        bluepiece4 = findViewById(R.id.bluepiece4);

        yellowpiece1 = findViewById(R.id.yellowpiece1);
        yellowpiece2 = findViewById(R.id.yellowpiece2);
        yellowpiece3 = findViewById(R.id.yellowpiece3);
        yellowpiece4 = findViewById(R.id.yellowpiece4);

        // player names 1 to 4
        playername1 = findViewById(R.id.pname1);
        playername2 = findViewById(R.id.pname2);
        playername3 = findViewById(R.id.pname3);
        playername4 = findViewById(R.id.pname4);
        // Player names are displayed upright in the HUD badges. Hide the
        // legacy in-yard labels to avoid upside-down names and dice overlaps,
        // while retaining the TextView references used by seat assignment.
        playername1.setVisibility(View.INVISIBLE);
        playername2.setVisibility(View.INVISIBLE);
        playername3.setVisibility(View.INVISIBLE);
        playername4.setVisibility(View.INVISIBLE);

        mainDiceImageView = findViewById(R.id.maindiceimageview);
        mainDiceImageView.getLayoutParams().width = hudDp(54.6f);
        mainDiceImageView.getLayoutParams().height = hudDp(54.6f);
        hintArrow = findViewById(R.id.hintarrowleft);

        shieldIndex1 = findViewById(R.id.lm_shieldindex1);
        shieldIndex2 = findViewById(R.id.lm_shieldindex2);
        shieldIndex3 = findViewById(R.id.lm_shieldindex3);
        shieldIndex4 = findViewById(R.id.lm_shieldindex4);

        // ingame menu items
        ingamemenubtn = findViewById(R.id.ingamemenubtn);
        ingamemenuitemslayout = findViewById(R.id.ingamemenuitemslayout);
        menuremoveplayersbtn = findViewById(R.id.rmpitemmenu);
        menuexitbtn = findViewById(R.id.exititemmenu);

        // ingame exitlayout views
        quitgamelayout = findViewById(R.id.ingamequitlayout);
        ingameyesbtn = findViewById(R.id.ingamequitlayoutyesbtn);
        ingamenobtn = findViewById(R.id.ingamequitlayoutnobtn);
        ingamesoundbtn = findViewById(R.id.ingamequitlayoutsoundbtn);
        ingamemusicbtn = findViewById(R.id.ingamequitlayoutmusicbtn);

        //in game rmplayout
        ingamermplayout = findViewById(R.id.ingamermplayout);
        rmp1bg = findViewById(R.id.rmp1);
        rmp2bg = findViewById(R.id.rmp2);
        rmp3bg = findViewById(R.id.rmp3);
        rmp4bg = findViewById(R.id.rmp4);
        rmpp1piece = findViewById(R.id.rmp1piece);
        rmpp2piece = findViewById(R.id.rmp2piece);
        rmpp3piece = findViewById(R.id.rmp3piece);
        rmpp4piece = findViewById(R.id.rmp4piece);
        rmpp1removeicon = findViewById(R.id.rmp1btn);
        rmpp2removeicon = findViewById(R.id.rmp2btn);
        rmpp3removeicon = findViewById(R.id.rmp3btn);
        rmpp4removeicon = findViewById(R.id.rmp4btn);
        rmpbackbtn = findViewById(R.id.rmpbackbtn);
        rmpmenubtn = findViewById(R.id.rmpmenubtn);
        rmpp1name = findViewById(R.id.rmp1name);
        rmpp2name = findViewById(R.id.rmp2name);
        rmpp3name = findViewById(R.id.rmp3name);
        rmpp4name = findViewById(R.id.rmp4name);


        //in gmae rmp sublayout confirmrmplayout
        confirmrmplayout = findViewById(R.id.confirmrmplayout);
        selectedrmppiece = findViewById(R.id.confirmremovepieceview);
        confirmrmpyesbtn = findViewById(R.id.confirmrmpyesbtn);
        confirmrmpnobtn = findViewById(R.id.confirmrmpnobtn);

        // players exit boxes
        p1exitbox = findViewById(R.id.p1exitbox);
        p2exitbox = findViewById(R.id.p2exitbox);
        p3exitbox = findViewById(R.id.p3exitbox);
        p4exitbox = findViewById(R.id.p4exitbox);

        // congratulaions layout

        congratulationslayout = findViewById(R.id.congratulationsscreen);

        congratsmenubtn = findViewById(R.id.congratsscreenmenubtn);
        congratssoundbtn = findViewById(R.id.congratsscreensoundbtn);
        congratssharebtn = findViewById(R.id.congratssharebtn);
        congratsreplaybtn = findViewById(R.id.congratsscreenreplaybtn);

        winnerlistp1layout = findViewById(R.id.winnerpositionlayout1);
        winnerlistp2layout = findViewById(R.id.winnerpositionlayout2);
        winnerlistp3layout = findViewById(R.id.winnerpositionlayout3);
        winnerlistp4layout = findViewById(R.id.winnerpositionlayout4);

        wlistshield1 = findViewById(R.id.winnerlisshield1);
        wlistshield2 = findViewById(R.id.winnerlisshield2);
        wlistshield3 = findViewById(R.id.winnerlisshield3);
        wlistshield4 = findViewById(R.id.winnerlisshield4);
        wlistpiece1 = findViewById(R.id.winnerlistp1);
        wlistpiece2 = findViewById(R.id.winnerlistp2);
        wlistpiece3 = findViewById(R.id.winnerlistp3);
        wlistpiece4 = findViewById(R.id.winnerlistp4);
        wlistwinorlose1 = findViewById(R.id.winorlose1);
        wlistwinorlose2 = findViewById(R.id.winorlose2);
        wlistwinorlose3 = findViewById(R.id.winorlose3);
        wlistwinorlose4 = findViewById(R.id.winorlose4);

        wlistname1 = findViewById(R.id.winnerlistpname1);
        wlistname2 = findViewById(R.id.winnerlistpname2);
        wlistname3 = findViewById(R.id.winnerlistpname3);
        wlistname4 = findViewById(R.id.winnerlistpname4);

        gameStartImageView = findViewById(R.id.gamestartimageview);

        pos1 = new float[4][2];
        pos2 = new float[4][2];
        pos3 = new float[4][2];
        pos4 = new float[4][2];

        onePercentWidth = (float) pxWidth/100;
        onePercentHeight = (float) pxHeight/100;

        players = new ArrayList<>();

        rp = new ArrayList<>();
        gp = new ArrayList<>();
        bp = new ArrayList<>();
        yp = new ArrayList<>();

        safeSpots = new HashSet<>();

        safeSpots.add(0);safeSpots.add(8);safeSpots.add(13);safeSpots.add(21);safeSpots.add(26);safeSpots.add(34);safeSpots.add(39);safeSpots.add(47);
        //safeSpots.add(57);safeSpots.add(63);safeSpots.add(69);safeSpots.add(75);

        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);
        step5 = findViewById(R.id.step5);
        step6 = findViewById(R.id.step6);

        int boxSize = (int)(Math.ceil(pxWidth*0.0667)*1.3);
        sizeOfBox = (int)(Math.ceil(pxWidth*0.0667)*0.15);

        blockSize = (float) (pxWidth*0.0667);

        RelativeLayout.LayoutParams stepsParams = new RelativeLayout.LayoutParams(boxSize,boxSize);
        step1.setLayoutParams(stepsParams);
        step2.setLayoutParams(stepsParams);
        step3.setLayoutParams(stepsParams);
        step4.setLayoutParams(stepsParams);
        step5.setLayoutParams(stepsParams);
        step6.setLayoutParams(stepsParams);


        redCircle = AppCompatResources.getDrawable(this,R.drawable.red_circle_shaded);
        greenCircle = AppCompatResources.getDrawable(this,R.drawable.green_circle_shaded);
        blueCircle = AppCompatResources.getDrawable(this,R.drawable.blue_circle_shaded);
        yellowCircle = AppCompatResources.getDrawable(this,R.drawable.yellow_circle_shaded);

        wBlocks52 = new int[] {52, 53, 54, 55, 56, 57};
        wBlocks58 = new int[] {58, 59, 60, 61, 62, 63};
        wBlocks64 = new int[] {64, 65, 66, 67, 68, 69};
        wBlocks70 = new int[] {70, 71, 72, 73, 74, 75};

        diceRollSound = MediaPlayer.create(this, R.raw.diceroll);
        stepSound = MediaPlayer.create(this, R.raw.step);
//        safeSound = MediaPlayer.create(this, R.raw.safe);
//        deathSound = MediaPlayer.create(this, R.raw.death);
//        pantaSound = MediaPlayer.create(this, R.raw.panta);
//        congratulationSound = MediaPlayer.create(this, R.raw.congratulations);

        if(isSoundOn) {
            ingamesoundbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.soundon,null));
            congratssoundbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.soundon,null));
        } else { ingamesoundbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.soundoff,null));
            congratssoundbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.soundoff,null));}

        if(isMusicOn) {
            ingamemusicbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.musicon,null));
        } else { ingamemusicbtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.musicoff,null)); }


        editText = findViewById(R.id.editTextNumber);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(((int)(pxWidth)),((int)(pxWidth)));
        ludoBoard.setLayoutParams(layoutParams); // size of board set
    }

    void initializeAnimatorSets() {
        animatorSets = new AnimatorSet[6];

        for (int i = 0; i < animatorSets.length; i++) {
            View v = getStepBubble(i+1);
            animatorSets[i] = createAnimatorSet(v);
            animatorSets[i].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    void showStep(int viewIndex,String color, float x, float y) {
        if (animatorSets == null || animatorSets.length != 6) {
            initializeAnimatorSets();
        }

        View view = null;
        view = getStepBubble(viewIndex);

        if (view != null) {
            view.setBackground(getColorDrawable(color));
            view.setTranslationX(x-sizeOfBox);
            view.setTranslationY(y-sizeOfBox);

            AnimatorSet animatorSet = animatorSets[viewIndex - 1];
            view.setVisibility(View.VISIBLE);
            animatorSet.start();
        }
    }

    private AnimatorSet createAnimatorSet(View view) {
        AnimatorSet animatorSet = new AnimatorSet();

        float initialScale = 0.5f;
        float finalScale = 1.0f;
        long duration = 700;
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, initialScale, finalScale);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, initialScale, finalScale);
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY);
        scaleAnimator.setDuration(600);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f);
        alphaAnimator.setDuration(800);

        animatorSet.playTogether(scaleAnimator, alphaAnimator);

        return animatorSet;
    }

    Drawable getColorDrawable(String color)
    {
        switch (color)
        {
            case "red":
                return redCircle;
            case "green":
                return greenCircle;
            case "blue":
                return blueCircle;
            case "yellow":
                return yellowCircle;
        }
        return redCircle;
    }

    void setFullScreen() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View dview = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            dview.setSystemUiVisibility(uiOptions);
        } else {
            View dview = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            dview.setSystemUiVisibility(uiOptions);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                WindowInsetsController insetsController = getWindow().getInsetsController();
                if(insetsController !=null) {
                    insetsController.hide(WindowInsets.Type.navigationBars());
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }

        }
    }

    float getNavHeight()
    {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public float getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (homeShuffleEnabled && homeShuffleEngine != null && globalHandler != null && !shuffleMatchEnding) {
            globalHandler.removeCallbacks(shuffleTimerTick);
            globalHandler.postDelayed(shuffleTimerTick,1000);
        }
        View dview = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        dview.setSystemUiVisibility(uiOptions);
    }

    void moveToTopLeft()
    {
        ConstraintLayout.LayoutParams lps = (ConstraintLayout.LayoutParams) mainDiceImageView.getLayoutParams();
        lps.startToStart = ConstraintLayout.LayoutParams.UNSET;
        lps.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        lps.endToStart = ConstraintLayout.LayoutParams.UNSET;
        lps.endToEnd = R.id.tleftdicebg;
        lps.topToTop = R.id.tleftdicebg;
        lps.bottomToBottom = R.id.tleftdicebg;
        lps.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        lps.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        // The pink dice tray occupies the inner half of the legacy 115dp pad.
        // Offset from the outside edge to center a 42dp die in that tray.
        lps.setMarginStart(0);
        lps.setMarginEnd(hudDp(11));
        lps.topMargin = 0;
        lps.bottomMargin = 0;
        mainDiceImageView.setLayoutParams(lps);
        ConstraintLayout.LayoutParams arrowLps = (ConstraintLayout.LayoutParams) hintArrow.getLayoutParams();
        arrowLps.endToStart = ConstraintLayout.LayoutParams.UNSET;
        arrowLps.startToEnd = R.id.tleftdicebg;
        arrowLps.topToTop = R.id.tleftdicebg;
        arrowLps.bottomToBottom = R.id.tleftdicebg;
        hintArrow.setRotation(0);
        hintArrow.setLayoutParams(arrowLps);
    }

    void moveToTopRight()
    {
        ConstraintLayout.LayoutParams lps = (ConstraintLayout.LayoutParams) mainDiceImageView.getLayoutParams();
        lps.startToStart = R.id.trightdicebg;
        lps.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        lps.endToStart = ConstraintLayout.LayoutParams.UNSET;
        lps.endToEnd = ConstraintLayout.LayoutParams.UNSET;
        lps.topToTop = R.id.trightdicebg;
        lps.bottomToBottom = R.id.trightdicebg;
        lps.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        lps.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        // The pink dice tray occupies the inner half of the legacy 115dp pad.
        // Offset from the outside edge to center a 42dp die in that tray.
        lps.setMarginStart(hudDp(9));
        lps.setMarginEnd(0);
        lps.topMargin = 0;
        lps.bottomMargin = 0;
        mainDiceImageView.setLayoutParams(lps);
        ConstraintLayout.LayoutParams arrowLps = (ConstraintLayout.LayoutParams) hintArrow.getLayoutParams();
        arrowLps.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        arrowLps.endToStart = R.id.trightdicebg;
        arrowLps.topToTop = R.id.trightdicebg;
        arrowLps.bottomToBottom = R.id.trightdicebg;
        hintArrow.setRotation(180);
        hintArrow.setLayoutParams(arrowLps);
    }

    void moveToBottomRight()
    {
        ConstraintLayout.LayoutParams lps = (ConstraintLayout.LayoutParams) mainDiceImageView.getLayoutParams();
        lps.startToStart = R.id.brightdicebg;
        lps.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        lps.endToStart = ConstraintLayout.LayoutParams.UNSET;
        lps.endToEnd = ConstraintLayout.LayoutParams.UNSET;
        lps.topToTop = R.id.brightdicebg;
        lps.bottomToBottom = R.id.brightdicebg;
        lps.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        lps.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        // The pink dice tray occupies the inner half of the legacy 115dp pad.
        // Offset from the outside edge to center a 42dp die in that tray.
        lps.setMarginStart(hudDp(9));
        lps.setMarginEnd(0);
        lps.topMargin = 0;
        lps.bottomMargin = 0;
        mainDiceImageView.setLayoutParams(lps);
        ConstraintLayout.LayoutParams arrowLps = (ConstraintLayout.LayoutParams) hintArrow.getLayoutParams();
        arrowLps.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        arrowLps.endToStart = R.id.brightdicebg;
        arrowLps.topToTop = R.id.brightdicebg;
        arrowLps.bottomToBottom = R.id.brightdicebg;
        hintArrow.setRotation(180);
        hintArrow.setLayoutParams(arrowLps);
    }

    void moveToBottomLeft()
    {
        ConstraintLayout.LayoutParams lps = (ConstraintLayout.LayoutParams) mainDiceImageView.getLayoutParams();
        lps.startToStart = ConstraintLayout.LayoutParams.UNSET;
        lps.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        lps.endToStart = ConstraintLayout.LayoutParams.UNSET;
        lps.endToEnd = R.id.bleftdicebg;
        lps.topToTop = R.id.bleftdicebg;
        lps.bottomToBottom = R.id.bleftdicebg;
        lps.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        lps.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        // The pink dice tray occupies the inner half of the legacy 115dp pad.
        // Offset from the outside edge to center a 42dp die in that tray.
        lps.setMarginStart(0);
        lps.setMarginEnd(hudDp(11));
        lps.topMargin = 0;
        lps.bottomMargin = 0;
        mainDiceImageView.setLayoutParams(lps);
        ConstraintLayout.LayoutParams arrowLps = (ConstraintLayout.LayoutParams) hintArrow.getLayoutParams();
        arrowLps.endToStart = ConstraintLayout.LayoutParams.UNSET;
        arrowLps.startToEnd = R.id.bleftdicebg;
        arrowLps.topToTop = R.id.bleftdicebg;
        arrowLps.bottomToBottom = R.id.bleftdicebg;
        hintArrow.setRotation(0);
        hintArrow.setLayoutParams(arrowLps);
    }

    int[] getWinnerBlocks(int n)
    {
        switch (n)
        {
            case 0:
            case 50:
                return wBlocks52;
            case 11:
                return wBlocks58;
            case 24:
                return wBlocks64;
            case 37:
                return wBlocks70;
            default:
                return wBlocks52;
        }
    }

    @Override
    public void onBackPressed() {
        RoyalDialog.confirm(this, "Leave this match?", "Your current unsaved match progress may be lost.",
            "EXIT MATCH", () -> finish());
    }
}
