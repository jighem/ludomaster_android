package com.ludomaster.reference;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Non-interactive 15x15 glossy tile bevel overlay.
 * Paints inside the existing board bounds only; game coordinates and
 * dynamically recoloured HomeShuffle bitmap remain the sole geometry source.
 */
public final class LhjBoardGlossDrawable extends Drawable {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha=255;
    @Override public void draw(Canvas canvas) {
        Rect r=getBounds();
        if(r.isEmpty())return;
        float w=r.width()/15f,h=r.height()/15f;
        float inset=Math.max(0.5f,Math.min(w,h)*0.015f);
        p.setStrokeWidth(Math.max(0.8f,Math.min(w,h)*0.024f));
        p.setStrokeCap(Paint.Cap.SQUARE);
        p.setStyle(Paint.Style.STROKE);
        for(int y=0;y<15;y++){
            float top=r.top+y*h,bot=top+h;
            for(int x=0;x<15;x++){
                float left=r.left+x*w,right=left+w;
                p.setColor(Color.argb(alpha*78/255,255,255,255));
                canvas.drawLine(left+inset,top+inset,right-inset,top+inset,p);
                canvas.drawLine(left+inset,top+inset,left+inset,bot-inset,p);
                p.setColor(Color.argb(alpha*29/255,4,22,48));
                canvas.drawLine(left+inset,bot-inset,right-inset,bot-inset,p);
                canvas.drawLine(right-inset,top+inset,right-inset,bot-inset,p);
            }
        }
    }
    @Override public void setAlpha(int a){alpha=Math.max(0,Math.min(255,a));invalidateSelf();}
    @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}