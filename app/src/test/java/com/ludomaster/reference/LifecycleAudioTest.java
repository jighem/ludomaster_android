package com.ludomaster.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public class LifecycleAudioTest {
    private static final class FakeTrack implements LifecycleAudio.Track {
        int starts,pauses,releases;
        float volume=-1;
        @Override public void start() { starts++; }
        @Override public void pause() { pauses++; }
        @Override public void volume(float value) { volume=value; }
        @Override public void release() { releases++; }
    }
    @Test public void leavingAppPausesAllAndReturnOnlyResumesEligibleAudio() {
        LifecycleAudio audio=new LifecycleAudio();FakeTrack effect=new FakeTrack(),music=new FakeTrack();
        audio.register(effect,false);audio.register(music,true);audio.foreground(true);
        audio.play(effect);audio.play(music);audio.foreground(false);
        assertEquals(1,effect.pauses);assertEquals(1,music.pauses);
        audio.foreground(true);
        assertEquals(1,effect.starts);assertEquals(2,music.starts);
    }
    @Test public void delayedCallbacksCannotStartEffectsInBackgroundOrReplayThemOnReturn() {
        LifecycleAudio audio=new LifecycleAudio();FakeTrack effect=new FakeTrack();
        audio.register(effect,false);audio.play(effect);
        assertEquals(0,effect.starts);assertFalse(audio.canPlayEffect());
        audio.foreground(true);assertEquals(0,effect.starts);
        audio.play(effect);assertEquals(1,effect.starts);
        audio.foreground(false);audio.play(effect);audio.play(effect);
        assertEquals(1,effect.starts);audio.foreground(true);assertEquals(1,effect.starts);
    }
    @Test public void introQueuedBeforeResumeStillStartsAndSurvivesBackgrounding() {
        LifecycleAudio audio=new LifecycleAudio();FakeTrack intro=new FakeTrack();
        audio.register(intro,true);audio.play(intro);assertEquals(0,intro.starts);
        audio.foreground(true);assertEquals(1,intro.starts);
        audio.foreground(false);assertEquals(1,intro.pauses);
        audio.foreground(true);assertEquals(2,intro.starts);
    }
    @Test public void mutedIntroCanFinishWithoutPlayingAudiblyOrBlockingMatchStart() {
        LifecycleAudio audio=new LifecycleAudio();FakeTrack intro=new FakeTrack();
        audio.muted(true);audio.register(intro,true);audio.play(intro);audio.foreground(true);
        assertEquals(0f,intro.volume,0f);assertEquals(1,intro.starts);
        assertFalse(audio.canPlayEffect());audio.muted(false);assertTrue(audio.canPlayEffect());
        assertEquals(1f,intro.volume,0f);assertEquals(1,intro.starts);
    }
    @Test public void manuallyPausedMusicDoesNotResume() {
        LifecycleAudio audio=new LifecycleAudio();FakeTrack music=new FakeTrack();
        audio.register(music,true);audio.foreground(true);audio.play(music);audio.pause(music);
        audio.foreground(false);audio.foreground(true);assertEquals(1,music.starts);
    }
    @Test public void exitReleasesEveryPlayerAndTheyCannotRestart() {
        LifecycleAudio audio=new LifecycleAudio();FakeTrack music=new FakeTrack(),effect=new FakeTrack();
        audio.register(music,true);audio.register(effect,false);audio.foreground(true);
        audio.play(music);audio.play(effect);audio.releaseAll();audio.releaseAll();
        assertEquals(1,music.releases);assertEquals(1,effect.releases);
        audio.foreground(false);audio.foreground(true);audio.play(music);
        assertEquals(1,music.starts);
    }
    @Test public void overlappingEffectsReleaseTheirOwnInstance() {
        LifecycleAudio audio=new LifecycleAudio();FakeTrack first=new FakeTrack(),second=new FakeTrack();
        audio.register(first,false);audio.register(second,false);audio.foreground(true);
        audio.play(first);audio.play(second);audio.release(first);audio.release(first);
        assertEquals(1,first.releases);assertEquals(0,second.releases);
        audio.foreground(false);assertEquals(1,second.pauses);
        audio.releaseAll();assertEquals(1,second.releases);
    }
    @Test public void reusableSoundCanPlayAgainAfterNaturalCompletion() {
        LifecycleAudio audio=new LifecycleAudio();FakeTrack effect=new FakeTrack();
        audio.register(effect,false);audio.foreground(true);
        audio.play(effect);audio.play(effect);
        assertEquals(2,effect.starts);
    }
}
