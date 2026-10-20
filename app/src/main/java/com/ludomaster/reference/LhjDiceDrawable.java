package com.ludomaster.reference;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Three-visible-face isometric die (top/front/right). Front face always
 * displays the actual rolled result; the other faces are visual context only.
 * All pips are white and no animation frame controls the dice result.
 */
public final class LhjDiceDrawable extends Drawable {
    private final String color;
    private final int face;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha=255;

    public LhjDiceDrawable(String color,int face) {
        this.color=color==null?"blue":color;
        this.face=Math.max(1,Math.min(6,face));
    }

    private int[] palette() {
        switch(color) {
            case "red":return new int[]{0xffffa3aa,0xfff42b4f,0xff9a1029};
            case "green":return new int[]{0xffb4ffce,0xff19c678,0xff086a43};
            case "yellow":return new int[]{0xfffff4ae,0xffffc42b,0xff9d6507};
            default:return new int[]{0xffbce9ff,0xff248efd,0xff064887};
        }
    }

    private static Path quad(float[] v) {
        Path path=new Path();
        path.moveTo(v[0],v[1]);path.lineTo(v[2],v[3]);
        path.lineTo(v[4],v[5]);path.lineTo(v[6],v[7]);
        path.close();
        return path;
    }

    private static float[] project(float[] q,float u,float v) {
        return new float[]{
            (1-u)*(1-v)*q[0]+u*(1-v)*q[2]+u*v*q[4]+(1-u)*v*q[6],
            (1-u)*(1-v)*q[1]+u*(1-v)*q[3]+u*v*q[5]+(1-u)*v*q[7]
        };
    }

    private void drawFace(Canvas c,float[] q,int start,int end) {
        p.setShader(new LinearGradient(q[0],q[1],q[4],q[5],
            start,end,Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL);
        c.drawPath(quad(q),p);
        p.setShader(null);
        p.setColor(0xa8ffffff);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.3f);
        c.drawPath(quad(q),p);
        p.setStyle(Paint.Style.FILL);
    }

    private void dot(Canvas c,float[] q,float u,float v,float radius) {
        float[] at=project(q,u,v);
        p.setShader(null);p.setColor(0x5c09213a);
        c.drawCircle(at[0]+.8f,at[1]+.8f,radius+.3f,p);
        p.setColor(Color.WHITE);
        c.drawCircle(at[0],at[1],radius,p);
        p.setColor(0x99d9f3ff);
        c.drawCircle(at[0]-radius*.22f,at[1]-radius*.22f,radius*.48f,p);
    }

    private void pips(Canvas c,float[] q,int n,float radius) {
        switch(n) {
            case 1: dot(c,q,.5f,.5f,radius);break;
            case 2: dot(c,q,.25f,.25f,radius);dot(c,q,.75f,.75f,radius);break;
            case 3:
                dot(c,q,.25f,.25f,radius);dot(c,q,.5f,.5f,radius);
                dot(c,q,.75f,.75f,radius);break;
            case 4:
                dot(c,q,.25f,.25f,radius);dot(c,q,.75f,.25f,radius);
                dot(c,q,.25f,.75f,radius);dot(c,q,.75f,.75f,radius);break;
            case 5:
                dot(c,q,.25f,.25f,radius);dot(c,q,.75f,.25f,radius);
                dot(c,q,.5f,.5f,radius);dot(c,q,.25f,.75f,radius);
                dot(c,q,.75f,.75f,radius);break;
            case 6:
                for(int col=0;col<2;col++)for(int row=0;row<3;row++)
                    dot(c,q,col==0?.25f:.75f,.22f+row*.28f,radius);break;
        }
    }

    @Override public void draw(Canvas canvas) {
        Rect r=getBounds();
        if(r.isEmpty())return;
        int state=canvas.save();
        canvas.translate(r.left,r.top);
        canvas.scale(r.width()/80f,r.height()/80f);
        int[] palette=palette();
        float[] top={9,25,32,9,73,21,50,37};
        float[] front={9,25,50,37,50,71,9,58};
        float[] right={50,37,73,21,73,55,50,71};
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setColor(0x4306192d);
        canvas.drawOval(10,66,76,79,p);
        drawFace(canvas,top,palette[0],palette[1]);
        drawFace(canvas,right,palette[1],palette[2]);
        drawFace(canvas,front,palette[0],palette[1]);
        // Real die orientation: adjacent faces cannot be opposing sides.
        int[] topValues={0,2,3,1,2,3,2};
        int[] rightValues={0,3,1,2,6,1,4};
        pips(canvas,top,topValues[face],2.45f);
        pips(canvas,right,rightValues[face],2.15f);
        pips(canvas,front,face,3.0f);
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.6f);
        p.setColor(0xc5ffffff);
        canvas.drawLine(10,26,32,11,p);
        canvas.drawLine(32,11,71,22,p);
        canvas.drawLine(10,26,10,57,p);
        p.setStyle(Paint.Style.FILL);
        canvas.restoreToCount(state);
    }
    @Override public void setAlpha(int value) {alpha=Math.max(0,Math.min(255,value));invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    @Override public int getIntrinsicWidth(){return 80;}
    @Override public int getIntrinsicHeight(){return 80;}
}
