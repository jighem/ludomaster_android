package com.ludomaster.reference;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Matching transparent, scalable unlit/lit bomb artwork for the centre control. */
final class BombIconDrawable extends Drawable {
    private final boolean lit;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    BombIconDrawable(boolean lit) { this.lit=lit; }
    @Override public void draw(Canvas canvas) {
        int save=canvas.save();
        canvas.translate(getBounds().left,getBounds().top);
        canvas.scale(getBounds().width()/64f,getBounds().height()/80f);
        paint.setStyle(Paint.Style.FILL);paint.setShader(null);paint.setColor(0x24000000);
        canvas.drawOval(new RectF(6,74,49,79),paint);
        paint.setShader(new RadialGradient(17,37,43,
            lit?new int[]{0xFF8194B0,0xFF1B2B45,0xFF040B17}:new int[]{0xFFAAAAAA,0xFF42474C,0xFF080C10},
            new float[]{0f,.35f,1f},Shader.TileMode.CLAMP));
        canvas.drawCircle(27,51,23,paint);
        paint.setShader(null);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);
        paint.setColor(0xFF152235);canvas.drawCircle(27,51,23,paint);
        paint.setStyle(Paint.Style.FILL);paint.setColor(lit?0xFF152239:0xFF30363C);
        canvas.drawRoundRect(new RectF(21,25,38,39),3,3,paint);
        paint.setColor(0xFF070C14);canvas.drawOval(new RectF(21,23,38,29),paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);
        Path fuse=new Path();fuse.moveTo(29,26);fuse.cubicTo(28,8,45,26,52,14);
        paint.setColor(0xFF58401F);paint.setStrokeWidth(6);canvas.drawPath(fuse,paint);
        paint.setColor(lit?0xFFDDB779:0xFFBDA879);paint.setStrokeWidth(3.5f);canvas.drawPath(fuse,paint);
        if(lit) {
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new RadialGradient(52,14,12,
                new int[]{0xF5FFFFFF,0xDDFFE13A,0xA0FF7800,0x00FF4500},
                new float[]{0,.2f,.48f,1},Shader.TileMode.CLAMP));
            canvas.drawCircle(52,14,12,paint);paint.setShader(null);
            Path spark=new Path();
            for(int i=0;i<16;i++) {
                double angle=i*Math.PI/8;
                float radius=i%2==0?7f:2.4f;
                float x=52+(float)Math.cos(angle)*radius,y=14+(float)Math.sin(angle)*radius;
                if(i==0)spark.moveTo(x,y);else spark.lineTo(x,y);
            }
            spark.close();paint.setColor(0xFFFFCE35);canvas.drawPath(spark,paint);
            paint.setColor(0xFFFFFFFF);canvas.drawCircle(52,14,2.4f,paint);
        }
        canvas.restoreToCount(save);
    }
    @Override public int getIntrinsicWidth() { return 64; }
    @Override public int getIntrinsicHeight() { return 80; }
    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(ColorFilter filter) { }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
