package com.ludomaster.reference;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/** Child overlay moves/scales with the existing pawn and never intercepts touches. */
final class BombInventoryDotDrawable extends Drawable {
    private final ImageView owner;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean available=true;
    BombInventoryDotDrawable(ImageView owner) { this.owner=owner; }
    void available(boolean available) {
        if(this.available!=available){this.available=available;invalidateSelf();}
    }
    @Override public void draw(Canvas canvas) {
        if(!available || owner.getDrawable()==null)return;
        RectF artwork=new RectF(owner.getDrawable().getBounds());
        owner.getImageMatrix().mapRect(artwork);
        if(artwork.width()<=0 || artwork.height()<=0)return;
        float cx=owner.getPaddingLeft()+artwork.left+artwork.width()*.57f;
        float cy=owner.getPaddingTop()+artwork.top+artwork.height()*.55f;
        float radius=artwork.width()*.085f;
        paint.setStyle(Paint.Style.FILL);paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx,cy,radius*1.30f,paint);
        paint.setColor(0xFF090D13);canvas.drawCircle(cx,cy,radius,paint);
    }
    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(ColorFilter filter) { }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
