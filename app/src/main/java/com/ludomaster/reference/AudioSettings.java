package com.ludomaster.reference;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
public final class AudioSettings {
    private AudioSettings(){}
    public static void show(Activity activity) {
        SharedPreferences prefs=activity.getSharedPreferences("LudoModUser",Activity.MODE_PRIVATE);
        // Same keys are read by the existing audio implementation.
        LinearLayout box=new LinearLayout(activity);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(42,20,42,20);
        CheckBox sound=new CheckBox(activity);sound.setText("Game sounds (dice, movement, captures)");
        sound.setChecked(prefs.getBoolean("sound",true));box.addView(sound);
        CheckBox music=new CheckBox(activity);music.setText("Background music");
        music.setChecked(prefs.getBoolean("music",false));box.addView(music);
        TextView hint=new TextView(activity);
        hint.setText("Changes are saved for the next game. Sound files can be replaced in app/src/main/res/raw.");
        box.addView(hint);
        new AlertDialog.Builder(activity).setTitle("Ludo Ho Jaye — Audio").setView(box)
          .setNegativeButton("Cancel",null)
          .setPositiveButton("Save",(d,w)->{
              prefs.edit().putBoolean("sound",sound.isChecked())
                   .putBoolean("music",music.isChecked()).apply();
              activity.recreate();
          }).show();
    }
}
