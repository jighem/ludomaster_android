package com.ludomaster.reference;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Board-centre tap target; never rolls the dice or chooses a pawn. */
final class BombControlView extends LinearLayout {
    private ImageView icon;
    private TextView label;
    private ObjectAnimator pulse;
    private boolean lit;
    BombControlView(Context context) {
        super(context);setOrientation(VERTICAL);setGravity(Gravity.CENTER);
        setClipChildren(false);setClipToPadding(false);
        icon=new ImageView(context);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(icon,new LayoutParams(LayoutParams.MATCH_PARENT,0,1));
        label=new TextView(context);label.setTextSize(10);label.setTextColor(0xFF172B54);
        label.setTypeface(null,Typeface.BOLD);label.setGravity(Gravity.CENTER);
        label.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(label,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        setVisibility(GONE);
    }
    void bind(BombControlState state,boolean bot) {
        if(state==BombControlState.HIDDEN){setVisibility(GONE);stopPulse();return;}
        boolean nextLit=state.lit();
        if(nextLit!=lit || icon.getDrawable()==null) {
            lit=nextLit;icon.setImageDrawable(new BombIconDrawable(lit));
        }
        label.setText(lit?"Bomb Armed":state==BombControlState.UNAVAILABLE?"Unavailable":"Plant Bomb");
        setAlpha(state==BombControlState.UNAVAILABLE?.38f:1f);
        boolean toggle=state.canToggle(bot);
        setEnabled(toggle);setClickable(toggle);setFocusable(toggle);
        setContentDescription(lit?(toggle?"Bomb armed. Tap to cancel before rolling.":"Bomb armed for the selected move."):
            state==BombControlState.UNAVAILABLE?"Plant bomb unavailable. No pawn carrying a bomb can plant from its current square.":
            bot?"Computer bomb control":"Plant bomb with this roll. Tap before rolling to arm.");
        setVisibility(VISIBLE);refreshPulse();
    }
    private void refreshPulse() {
        if(icon==null)return;
        if(!lit || getVisibility()!=VISIBLE || getWindowVisibility()!=VISIBLE || !isShown()) { stopPulse();return; }
        if(pulse!=null && pulse.isStarted())return;
        pulse=ObjectAnimator.ofFloat(icon,View.ALPHA,1f,.68f,1f);
        pulse.setDuration(1000);pulse.setRepeatCount(ValueAnimator.INFINITE);pulse.start();
    }
    private void stopPulse() {
        if(pulse!=null){pulse.cancel();pulse=null;}
        if(icon!=null)icon.setAlpha(1f);
    }
    @Override protected void onVisibilityChanged(View changed,int visibility) {
        super.onVisibilityChanged(changed,visibility);refreshPulse();
    }
    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);refreshPulse();
    }
    @Override protected void onDetachedFromWindow() { stopPulse();super.onDetachedFromWindow(); }
}
