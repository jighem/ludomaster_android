package com.ludomaster.board;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import com.ludomaster.game.ClassicGame;

public final class BoardView extends View {
    public interface PawnTap { void onPawn(int pawn); }
    private ClassicGame game;
    private PawnTap listener;
    private final Paint brush = new Paint(3);
    private float startX, startY, tile;
    public BoardView(Context context) { super(context); }
    public void bind(ClassicGame game,PawnTap listener) {this.game=game;this.listener=listener;invalidate();}
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (game==null) return;
        tile=Math.min(getWidth(),getHeight())/15f;
        startX=(getWidth()-15*tile)/2f;startY=(getHeight()-15*tile)/2f;
        c.drawColor(Color.rgb(245,247,250));
        for(int row=0;row<15;row++) for(int col=0;col<15;col++) {
            int color=Color.WHITE;
            if(row<6 && col<6) color=yardColor(game.colorAtQuadrant(0));
            if(row<6 && col>8) color=yardColor(game.colorAtQuadrant(1));
            if(row>8 && col>8) color=yardColor(game.colorAtQuadrant(2));
            if(row>8 && col<6) color=yardColor(game.colorAtQuadrant(3));
            if(row>=6 && row<=8 && col>=6 && col<=8) color=0xffeceff1;
            for(int k=0;k<4;k++) for(int[] lane:ClassicGame.LANES[k])
                if(lane[0]==row && lane[1]==col) color=ClassicGame.COLORS[game.colorAtQuadrant(k)];
            for(int k=0;k<4;k++) {
                int[] st=ClassicGame.TRACK[ClassicGame.START[k]];
                if(st[0]==row && st[1]==col) color=ClassicGame.COLORS[k];
            }
            brush.setColor(color);brush.setStyle(Paint.Style.FILL);
            c.drawRect(startX+col*tile,startY+row*tile,startX+(col+1)*tile,startY+(row+1)*tile,brush);
            brush.setColor(0xffb0bec5);brush.setStyle(Paint.Style.STROKE);brush.setStrokeWidth(0.7f);
            c.drawRect(startX+col*tile,startY+row*tile,startX+(col+1)*tile,startY+(row+1)*tile,brush);
            brush.setStyle(Paint.Style.FILL);
        }
        for(int color=0;color<4;color++) if(game.active[color])
            for(int pawn=0;pawn<4;pawn++) {
                int[] xy=game.position(color,pawn);
                float x=startX+(xy[1]+0.5f)*tile;
                float y=startY+(xy[0]+0.5f)*tile;
                // Offset stacked pawns so every pawn remains selectable.
                int stacked=0,slot=0;
                for(int q=0;q<4;q++) if(game.progress[color][q]!=57) {
                    int[] qxy=game.position(color,q);
                    if(qxy[0]==xy[0]&&qxy[1]==xy[1]) {if(q<pawn)slot++;stacked++;}
                }
                if(stacked>1){ x+=((slot%2==0)?-1:1)*tile*0.16f; y+=((slot/2==0)?-1:1)*tile*0.16f; }
                brush.setStyle(Paint.Style.FILL);brush.setColor(ClassicGame.COLORS[color]);
                c.drawCircle(x,y,tile*0.31f,brush);
                brush.setStyle(Paint.Style.STROKE);brush.setStrokeWidth(color==game.turn&&game.canMove(pawn)?tile*0.09f:tile*0.04f);
                brush.setColor(Color.BLACK);c.drawCircle(x,y,tile*0.31f,brush);brush.setStyle(Paint.Style.FILL);
                brush.setColor(color==2?Color.BLACK:Color.WHITE);
                brush.setTextSize(tile*0.31f);brush.setTextAlign(Paint.Align.CENTER);
                c.drawText(String.valueOf(pawn+1),x,y+tile*0.11f,brush);
            }
    }
    private static int yardColor(int color) {
        return new int[]{0xffef9a9a,0xffa5d6a7,0xfffff59d,0xff90caf9}[color];
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()!=MotionEvent.ACTION_UP || game==null || !game.awaitingPawn) return true;
        float x=event.getX(),y=event.getY();
        float nearest=Float.MAX_VALUE;int selected=-1;
        for(int p:game.legalPawns()){
            int[] xy=game.position(game.turn,p);
            float px=startX+(xy[1]+0.5f)*tile,py=startY+(xy[0]+0.5f)*tile;
            float d=(px-x)*(px-x)+(py-y)*(py-y);
            if(d<nearest){nearest=d;selected=p;}
        }
        if(selected>=0 && nearest<tile*tile*1.1f && listener!=null)listener.onPawn(selected);
        return true;
    }
}
