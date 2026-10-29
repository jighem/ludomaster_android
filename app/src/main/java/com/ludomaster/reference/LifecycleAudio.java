package com.ludomaster.reference;

import java.util.IdentityHashMap;
import java.util.Map;

/** Foreground-only playback state, independent of Android for lifecycle tests. */
public final class LifecycleAudio {
    public interface Track {
        void start();
        void pause();
        void volume(float volume);
        void release();
    }
    private static final class State {
        final boolean resumable;
        boolean requested,playing;
        State(boolean resumable) { this.resumable=resumable; }
    }
    private final Map<Track,State> tracks=new IdentityHashMap<>();
    private boolean foreground,muted;
    public void register(Track track,boolean resumable) {
        if(track==null)return;
        tracks.put(track,new State(resumable));
        track.volume(muted?0f:1f);
    }
    public boolean canPlayEffect() { return foreground && !muted; }
    public void play(Track track) {
        State state=tracks.get(track);
        if(state==null)return;
        state.requested=foreground || state.resumable;
        // A reusable MediaPlayer may have completed since the previous call.
        // start() is also safe while already playing and does not rewind it.
        if(foreground) { track.start();state.playing=true; }
    }
    public void pause(Track track) {
        State state=tracks.get(track);
        if(state==null)return;
        state.requested=false;
        if(state.playing)track.pause();
        state.playing=false;
    }
    public void foreground(boolean active) {
        if(foreground==active)return;
        foreground=active;
        for(Map.Entry<Track,State> entry:tracks.entrySet()) {
            Track track=entry.getKey();State state=entry.getValue();
            if(!active) {
                if(state.playing)track.pause();
                state.playing=false;
                if(!state.resumable)state.requested=false;
            } else if(state.resumable && state.requested && !state.playing) {
                track.start();state.playing=true;
            }
        }
    }
    /** Muting preserves the intro's completion callback, which starts gameplay. */
    public void muted(boolean muted) {
        this.muted=muted;
        for(Track track:tracks.keySet())track.volume(muted?0f:1f);
    }
    public void release(Track track) {
        if(tracks.remove(track)!=null)track.release();
    }
    public void releaseAll() {
        for(Track track:tracks.keySet())track.release();
        tracks.clear();
    }
}
