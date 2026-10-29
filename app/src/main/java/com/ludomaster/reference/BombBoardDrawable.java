package com.ludomaster.reference;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import java.util.Collections;
import java.util.Map;

/** Non-interactive bomb markers, drawn in the same board coordinates as pawns. */
final class BombBoardDrawable extends Drawable {
    private final float[] x,y;
    private final float size,left,top;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Map<Integer,BomberRules.Bomb> bombs=Collections.emptyMap();
    private int blast=-1;
    private float progress;
    BombBoardDrawable(float[] x,float[] y,float size,float left,float top) {
        this.x=x;this.y=y;this.size=size;this.left=left;this.top=top;
    }
    void update(Map<Integer,BomberRules.Bomb> bombs) { this.bombs=bombs;invalidateSelf(); }
    void explosion(int square,float progress) { blast=square;this.progress=progress;invalidateSelf(); }
    static int color(String name) {
        if("red".equals(name))return 0xFFF04452;
        if("green".equals(name))return 0xFF20BE70;
        if("yellow".equals(name))return 0xFFFFC328;
        return 0xFF248EFD;
    }
    @Override public void draw(Canvas canvas) {
        for(BomberRules.Bomb bomb:bombs.values()) {
            float cx=left+x[bomb.square]+size*.5f,cy=top+y[bomb.square]+size*.55f;
            paint.setStyle(Paint.Style.FILL);paint.setColor(0xD9FFFFFF);
            canvas.drawCircle(cx,cy,size*.32f,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(size*.055f);
            paint.setColor(color(bomb.ownerColor));canvas.drawCircle(cx,cy,size*.32f,paint);
            paint.setStyle(Paint.Style.FILL);paint.setColor(0xFF172B3B);
            canvas.drawCircle(cx,cy,size*.22f,paint);
            canvas.drawRect(cx+size*.07f,cy-size*.29f,cx+size*.18f,cy-size*.13f,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(size*.045f);
            paint.setColor(0xFFFF8D18);
            canvas.drawLine(cx+size*.14f,cy-size*.29f,cx+size*.25f,cy-size*.38f,paint);
            paint.setStyle(Paint.Style.FILL);paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(cx-size*.07f,cy-size*.07f,size*.045f,paint);
        }
        if(blast>=0 && blast<x.length) {
            float cx=left+x[blast]+size*.5f,cy=top+y[blast]+size*.5f;
            paint.setStyle(Paint.Style.FILL);paint.setColor(0x99FF7A00);
            paint.setAlpha(Math.round(180*(1-progress)));
            canvas.drawCircle(cx,cy,size*(.2f+progress*.65f),paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(size*.08f);
            paint.setColor(0xFFFFD43B);paint.setAlpha(Math.round(255*(1-progress)));
            canvas.drawCircle(cx,cy,size*(.15f+progress*.7f),paint);
            paint.setAlpha(255);
        }
    }
    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(ColorFilter filter) { }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
