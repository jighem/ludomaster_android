package com.ludomaster.reference.shuffle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.content.res.Resources;

/**
 * Recolours the existing four coloured regions of the 15x15 board.
 * This preserves stars, dice paths, square outlines, and yard pawn circles
 * instead of painting opaque panels over the original artwork.
 *
 * Source board colour order (clockwise) is yellow, blue, red, green.
 * View rotation is respected when mapping physical home-slot ownership back
 * to the source bitmap.
 */
public final class HomeBoardPainter {
    private HomeBoardPainter() {}
    private static final float[] TARGET_HUE = {0f,139f,201f,53f}; // red green blue yellow

    /** Clockwise source bitmap and board slots share the same 0..3 order. */
    public static int rotatedPhysicalSlot(int sourceSlot, int clockwiseQuarterTurns) {
        if (sourceSlot < 0 || sourceSlot > 3) throw new IllegalArgumentException("slot");
        return (sourceSlot + ((clockwiseQuarterTurns % 4) + 4) % 4) % 4;
    }

    public static Bitmap render(Resources resources,int drawableId,int[] colourToSlot,float boardRotation) {
        if(colourToSlot==null || colourToSlot.length!=4)
            throw new IllegalArgumentException("Four home colours required");
        int[] ownerByPhysicalSlot={-1,-1,-1,-1};
        for(int colour=0;colour<4;colour++) {
            int slot=colourToSlot[colour];
            if(slot<0||slot>3||ownerByPhysicalSlot[slot]>=0)
                throw new IllegalArgumentException("Invalid home ownership");
            ownerByPhysicalSlot[slot]=colour;
        }
        int turns = ((Math.round(boardRotation/90f)%4)+4)%4;
        // Physical home slots from HomeShuffleEngine are clockwise:
        // 0=top-right (start 0), 1=bottom-right (13),
        // 2=bottom-left (26), 3=top-left (39).
        // Original source colours also appear in this clockwise order:
        // yellow, blue, red, green. Rotate source geometry clockwise to
        // find the *screen* slot, independent of colour ownership.
        int[] recolourSourceSlot = new int[4];
        for(int sourceSlot=0;sourceSlot<4;sourceSlot++) {
            int physicalSlot=rotatedPhysicalSlot(sourceSlot,turns);
            recolourSourceSlot[sourceSlot]=ownerByPhysicalSlot[physicalSlot];
        }

        Bitmap original=BitmapFactory.decodeResource(resources,drawableId);
        if(original==null)throw new IllegalStateException("Cannot load original board artwork");
        int w=original.getWidth(),h=original.getHeight();
        int[] pixels=new int[w*h];
        original.getPixels(pixels,0,w,0,0,w,h);
        original.recycle();
        float[] hsv=new float[3];
        for(int i=0;i<pixels.length;i++) {
            int originalColor=pixels[i];
            Color.colorToHSV(originalColor,hsv);
            if(hsv[1]<0.48f || hsv[2]<0.38f)continue; // preserve white, black, lines, stars
            int sourceSlot;
            float hue=hsv[0];
            if(hue<22f || hue>340f) sourceSlot=2;       // original red
            else if(hue>31f && hue<72f) sourceSlot=0;  // original yellow
            else if(hue>85f && hue<173f) sourceSlot=3; // original green
            else if(hue>180f && hue<237f) sourceSlot=1;// original blue
            else continue;
            int targetColour=recolourSourceSlot[sourceSlot];
            hsv[0]=TARGET_HUE[targetColour];
            hsv[1]=Math.max(0.75f,hsv[1]);
            hsv[2]=Math.max(0.85f,hsv[2]);
            pixels[i]=Color.HSVToColor(Color.alpha(originalColor),hsv);
        }
        Bitmap output=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        output.setPixels(pixels,0,w,0,0,w,h);
        return output;
    }

}
