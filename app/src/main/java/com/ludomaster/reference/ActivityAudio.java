package com.ludomaster.reference;

import android.content.Context;
import android.media.MediaPlayer;
import java.util.IdentityHashMap;
import java.util.Map;

/** Owns every MediaPlayer created for one Activity; no static/global audio players. */
final class ActivityAudio {
    private final LifecycleAudio lifecycle=new LifecycleAudio();
    private final Map<MediaPlayer,LifecycleAudio.Track> owned=new IdentityHashMap<>();
    MediaPlayer create(Context context,int resource,boolean resumable) {
        MediaPlayer player=MediaPlayer.create(context,resource);
        if(player==null)return null;
        LifecycleAudio.Track track=new LifecycleAudio.Track() {
            @Override public void start() { try { player.start(); } catch(IllegalStateException ignored) { } }
            @Override public void pause() { try { player.pause(); } catch(IllegalStateException ignored) { } }
            @Override public void volume(float amount) { try { player.setVolume(amount,amount); } catch(IllegalStateException ignored) { } }
            @Override public void release() { player.setOnCompletionListener(null);player.release(); }
        };
        owned.put(player,track);lifecycle.register(track,resumable);
        return player;
    }
    void play(MediaPlayer player) { lifecycle.play(owned.get(player)); }
    void pause(MediaPlayer player) { lifecycle.pause(owned.get(player)); }
    void release(MediaPlayer player) { lifecycle.release(owned.remove(player)); }
    void foreground(boolean active) { lifecycle.foreground(active); }
    void muted(boolean muted) { lifecycle.muted(muted); }
    void effect(Context context,int resource) {
        if(!lifecycle.canPlayEffect())return;
        MediaPlayer player=create(context,resource,false);
        if(player==null)return;
        player.setOnCompletionListener(this::release);
        play(player);
    }
    void releaseAll() { lifecycle.releaseAll();owned.clear(); }
}
