package com.ludomaster.reference;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Glossy player-coloured die face. Rendering only; roll rules stay in MainActivity. */
public final class LhjDiceDrawable extends Drawable {
    private final String color;
    private final int face;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha=255;

    public LhjDiceDrawable(String color,int face){
        this.color=color==null?"blue":color;
        this.face=Math.max(1,Math.min(6,face));
    }
    private int[] palette(){
        switch(color){
            case "red":return new int[]{0xffff8190,0xfff22b4b,0xffa0102b};
            case "green":return new int[]{0xff9affc1,0xff1cc873,0xff08794b};
            case "yellow":return new int[]{0xffffee94,0xffffc627,0xffb7760a};
            default:return new int[]{0xff9cdaff,0xff218ffa,0xff06539f};
        }
    }
    private void pip(Canvas canvas,float x,float y,float radius){
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x5b0a2340);
        canvas.drawCircle(x+1.1f,y+1.2f,radius+0.25f,p);
        p.setColor(Color.WHITE);
        canvas.drawCircle(x,y,radius,p);
        p.setColor(0xffe1f4ff);
        canvas.drawCircle(x-radius*.2f,y-radius*.2f,radius*.50f,p);
    }
    @Override public void draw(Canvas canvas){
        Rect bounds=getBounds();
        if(bounds.isEmpty())return;
        int save=canvas.save();
        canvas.translate(bounds.left,bounds.top);
        canvas.scale(bounds.width()/80f,bounds.height()/80f);
        int[] palette=palette();
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setColor(0x68041b34);
        canvas.drawRoundRect(new RectF(8,9,76,77),18,18,p);
        p.setColor(palette[2]);
        canvas.drawRoundRect(new RectF(4,3,74,73),16,16,p);
        p.setShader(new LinearGradient(12,7,68,70,palette,null,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(7,4,71,68),14,14,p);
        p.setShader(null);
        p.setColor(0x86ffffff);
        canvas.drawRoundRect(new RectF(13,9,64,19),7,7,p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setColor(0xb8ffffff);
        canvas.drawRoundRect(new RectF(8,5,70,67),13,13,p);
        p.setStyle(Paint.Style.FILL);
        boolean[] cells=new boolean[9];
        switch(face){
            case 1: cells[4]=true;break;
            case 2:cells[0]=cells[8]=true;break;
            case 3:cells[0]=cells[4]=cells[8]=true;break;
            case 4:cells[0]=cells[2]=cells[6]=cells[8]=true;break;
            case 5:cells[0]=cells[2]=cells[4]=cells[6]=cells[8]=true;break;
            case 6:cells[0]=cells[2]=cells[3]=cells[5]=cells[6]=cells[8]=true;break;
        }
        for(int i=0;i<9;i++)if(cells[i])
            pip(canvas,22f+(i%3)*17f,20f+(i/3)*17f,5.6f);
        canvas.restoreToCount(save);
    }
    @Override public void setAlpha(int a){alpha=a;invalidateSelf();}
    @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    @Override public int getIntrinsicWidth(){return 80;}
    @Override public int getIntrinsicHeight(){return 80;}
}
