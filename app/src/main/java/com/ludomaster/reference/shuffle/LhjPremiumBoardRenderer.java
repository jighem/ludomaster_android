package com.ludomaster.reference.shuffle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * A coordinate-locked premium finish for the validated 15x15 Ludo board.
 *
 * No cells, stars, arrow glyphs, center triangles, entrance paths, or track
 * ownership are moved. Only the four 6x6 home panels are redrawn, and thin
 * bevels are painted inside path cells. The source board still owns every
 * playable coordinate, both initially and after a Dynamic Home Shuffle.
 */
public final class LhjPremiumBoardRenderer {
    private LhjPremiumBoardRenderer() {}

    /** Returns a new mutable bitmap. Caller continues owning the source bitmap. */
    public static Bitmap enhance(Bitmap source) {
        if (source == null || source.getWidth() < 150 || source.getHeight() < 150)
            throw new IllegalArgumentException("Invalid 15x15 source board");
        Bitmap result = source.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        float cellX = result.getWidth() / 15f;
        float cellY = result.getHeight() / 15f;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        int[][] homes = {{0,0}, {9,0}, {0,9}, {9,9}};
        for (int[] h : homes) {
            float left = h[0] * cellX, top = h[1] * cellY;
            // Source colours are sampled *after* shuffle recolouring. This
            // preserves the assigned home colour without a second seat map.
            int sampled = source.getPixel(
                    Math.min(source.getWidth()-1, Math.round(left + cellX * .45f)),
                    Math.min(source.getHeight()-1, Math.round(top + cellY * .45f)));
            drawHome(canvas, paint, left, top, cellX, cellY, sampled);
        }
        drawPaths(canvas, paint, cellX, cellY);
        drawBoardRim(canvas, paint, cellX, cellY);
        return result;
    }

    private static int brighten(int color, float amount) {
        return Color.rgb(
            Math.min(255, Math.round(Color.red(color)+(255-Color.red(color))*amount)),
            Math.min(255, Math.round(Color.green(color)+(255-Color.green(color))*amount)),
            Math.min(255, Math.round(Color.blue(color)+(255-Color.blue(color))*amount)));
    }
    private static int darken(int color, float amount) {
        return Color.rgb(
            Math.round(Color.red(color)*(1-amount)),
            Math.round(Color.green(color)*(1-amount)),
            Math.round(Color.blue(color)*(1-amount)));
    }
    private static void box(Canvas canvas, Paint p, RectF r, float rad, int color) {
        p.setStyle(Paint.Style.FILL); p.setShader(null); p.setColor(color);
        canvas.drawRoundRect(r, rad, rad, p);
    }
    private static void drawHome(Canvas canvas, Paint p, float left, float top,
                                 float sx, float sy, int sampled) {
        float sideX=6*sx, sideY=6*sy, radius=Math.min(sx,sy)*.24f;
        // Slightly inset the bevel so it never bleeds into an adjacent lane.
        RectF outer=new RectF(left,top,left+sideX,top+sideY);
        box(canvas,p,outer,radius,darken(sampled,.32f));
        RectF gloss=new RectF(left+sx*.08f,top+sy*.06f,
                              left+sideX-sx*.08f,top+sideY-sy*.11f);
        p.setShader(new LinearGradient(gloss.left,gloss.top,gloss.right,gloss.bottom,
            new int[]{brighten(sampled,.38f),sampled,darken(sampled,.17f)},null,
            Shader.TileMode.CLAMP));
        canvas.drawRoundRect(gloss,radius,radius,p);
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f,sx*.085f));
        p.setColor(0x98FFFFFF);
        canvas.drawRoundRect(new RectF(gloss.left+sx*.04f,gloss.top+sy*.04f,
            gloss.right-sx*.04f,gloss.bottom-sy*.04f),radius,radius,p);
        p.setStyle(Paint.Style.FILL);

        // Exact old playable home area: columns/rows 1..5 of each 6x6 block.
        RectF shadow=new RectF(left+sx*.97f,top+sy*1.08f,
            left+sx*5.06f,top+sy*5.12f);
        box(canvas,p,shadow,sx*.18f,0x4B092642);
        RectF inner=new RectF(left+sx,top+sy,left+sx*5,top+sy*5);
        box(canvas,p,inner,sx*.18f,0xFFFDFEFF);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f,sx*.065f));
        p.setColor(brighten(sampled,.53f));
        canvas.drawRoundRect(new RectF(inner.left+sx*.055f,inner.top+sy*.055f,
            inner.right-sx*.055f,inner.bottom-sy*.055f),sx*.16f,sx*.16f,p);
        p.setStyle(Paint.Style.FILL);

        float r=Math.min(sx,sy)*.47f;
        for(int yy=2;yy<=4;yy+=2)for(int xx=2;xx<=4;xx+=2){
            float cx=left+xx*sx,cy=top+yy*sy;
            p.setColor(0x35061E39);
            canvas.drawCircle(cx,cy+sy*.07f,r*1.08f,p);
            p.setColor(darken(sampled,.20f));
            canvas.drawCircle(cx,cy,r,p);
            p.setShader(new LinearGradient(cx-r,cy-r,cx+r,cy+r,
                new int[]{brighten(sampled,.52f),sampled,darken(sampled,.08f)},
                null,Shader.TileMode.CLAMP));
            canvas.drawCircle(cx,cy,r*.87f,p);
            p.setShader(null);
            p.setColor(0xA0FFFFFF);
            canvas.drawArc(new RectF(cx-r*.59f,cy-r*.68f,cx+r*.49f,cy+r*.31f),
                204,110,false,p);
        }
    }

    private static void drawPaths(Canvas canvas, Paint p, float sx, float sy) {
        float line=Math.max(.65f,Math.min(sx,sy)*.02f);
        p.setStrokeWidth(line);p.setStyle(Paint.Style.STROKE);
        for(int y=0;y<15;y++)for(int x=0;x<15;x++){
            boolean path=(x>=6 && x<=8)||(y>=6 && y<=8);
            boolean center=(x>=6 && x<=8 && y>=6 && y<=8);
            if(!path || center)continue;
            float l=x*sx,t=y*sy,r=l+sx,b=t+sy;
            p.setColor(0x44FFFFFF);
            canvas.drawLine(l+1,t+1,r-1,t+1,p);
            canvas.drawLine(l+1,t+1,l+1,b-1,p);
            p.setColor(0x1B0C2347);
            canvas.drawLine(l+1,b-1,r-1,b-1,p);
            canvas.drawLine(r-1,t+1,r-1,b-1,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private static void drawBoardRim(Canvas canvas,Paint p,float sx,float sy) {
        float width=15*sx,height=15*sy;
        float stroke=Math.max(1f,Math.min(sx,sy)*.08f);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(stroke);
        p.setColor(0xB2FFFFFF);
        canvas.drawRect(stroke/2,stroke/2,width-stroke/2,height-stroke/2,p);
        p.setColor(0x41052243);
        canvas.drawLine(stroke,height-stroke,width-stroke,height-stroke,p);
        canvas.drawLine(width-stroke,stroke,width-stroke,height-stroke,p);
        p.setStyle(Paint.Style.FILL);
    }
}
