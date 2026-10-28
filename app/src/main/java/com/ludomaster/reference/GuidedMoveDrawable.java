package com.ludomaster.reference;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.List;

/** Presentation-only board overlay; never receives touches or changes match state. */
final class GuidedMoveDrawable extends Drawable {
    private static final class Mark {
        final float x,y,size;
        final int step,color;
        final boolean destination;
        Mark(float x,float y,float size,int step,int color,boolean destination) {
            this.x=x;this.y=y;this.size=size;this.step=step;
            this.color=color;this.destination=destination;
        }
    }
    private final List<Mark> marks=new ArrayList<>();
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    void add(float x,float y,float size,int step,int color,boolean destination) {
        for (Mark m:marks)
            if(m.x==x && m.y==y && m.step==step && m.destination==destination) return;
        marks.add(new Mark(x,y,size,step,color,destination));
    }
    @Override public void draw(Canvas canvas) {
        // Draw destinations first, so all step labels remain readable on shared paths.
        for(Mark m:marks) if(m.destination) {
            paint.setStyle(Paint.Style.FILL);paint.setColor((m.color & 0xFFFFFF)|0x55000000);
            canvas.drawRect(m.x+2,m.y+2,m.x+m.size-2,m.y+m.size-2,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(2,m.size*.06f));
            paint.setColor(m.color);
            canvas.drawRect(m.x+2,m.y+2,m.x+m.size-2,m.y+m.size-2,paint);
        }
        java.util.Map<String,Integer> labels=new java.util.HashMap<>();
        java.util.Set<String> drawn=new java.util.HashSet<>();
        for(Mark m:marks) {
            String square=m.x+":"+m.y;
            if(!drawn.add(square+":"+m.step)) continue;
            int slot=labels.getOrDefault(square,0);
            labels.put(square,slot+1);
            float radius=m.size*.13f;
            float cx=m.x+m.size*(.17f+(slot%3)*.33f);
            float cy=m.y+m.size*(slot<3?.17f:.83f);
            paint.setStyle(Paint.Style.FILL);paint.setColor(0xF5FFFFFF);
            canvas.drawCircle(cx,cy,radius,paint);
            paint.setColor(0xFF172B54);paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(m.size*.22f);paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            canvas.drawText(Integer.toString(m.step),cx,cy-(paint.ascent()+paint.descent())/2,paint);
        }
    }
    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(ColorFilter filter) { }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
