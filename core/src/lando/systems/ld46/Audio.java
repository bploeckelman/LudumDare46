package lando.systems.ld46;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Sine;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public class Audio implements Disposable {
    public static final float MUSIC_VOLUME = 0.6f;
    public static final float SOUND_VOLUME = 0.6f;

    public static final boolean shutUpYourFace = false;
    public static final boolean shutUpYourTunes = false;

    // none should not have a sound
    public enum Sounds {
        none, sample_sound, doc_punch, doc_punch_land, doc_jump, doc_hurt, doc_death, zombie_punch,
        zombie_punch_land, zombie_punch_wall, zombie_jump, zombie_hurt, zombie_death
    }

    public enum Musics {
        barkMusic, ritzMusic
    }

    private Array<Musics> availableThemeTracks = new Array<>(new Musics[] {
            Musics.barkMusic, Musics.ritzMusic
    });

    private Musics getRandomThemeMusic() {
        return availableThemeTracks.get(MathUtils.random(0, availableThemeTracks.size - 1));
    }

    public ObjectMap<Sounds, SoundContainer> sounds = new ObjectMap<>();
    public ObjectMap<Musics, Music> musics = new ObjectMap<>();

    public Music currentMusic;
    public MutableFloat musicVolume;
    public Musics eCurrentMusic;
    public Music oldCurrentMusic;

    private Assets assets;
    private TweenManager tween;
    private boolean inGame = false;

    public Audio(Game game) {
        this(!shutUpYourTunes, game);
    }

    public Audio(boolean playMusic, Game game) {
        this.assets = game.assets;
        this.tween = game.tween;

        putSound(Sounds.sample_sound, assets.sampleSound);

        musics.put(Musics.barkMusic, assets.barkMusic);
        musics.put(Musics.ritzMusic, assets.ritzMusic);

        musicVolume = new MutableFloat(0);
        setMusicVolume(MUSIC_VOLUME, 2f);
        if (playMusic) {
            currentMusic = musics.get(Musics.ritzMusic);
            eCurrentMusic = Musics.ritzMusic;
            currentMusic.setLooping(true);
            currentMusic.setVolume(0f);
            currentMusic.play();

            // currentMusic.setOnCompletionListener(nextSong);
        }
    }

    public void update(float dt){
        if (currentMusic != null) {
//            if (musicVolume.floatValue() == 0f) {
//                if (oldCurrentMusic != null) oldCurrentMusic.stop();
//                setMusicVolume(MUSIC_VOLUME, 1f);
//                currentMusic.play();
//            }

            currentMusic.setVolume(musicVolume.floatValue());
        }

        if (oldCurrentMusic != null) {
            oldCurrentMusic.setVolume(musicVolume.floatValue());
        }
    }

    @Override
    public void dispose() {
        Sounds[] allSounds = Sounds.values();
        for (Sounds sound : allSounds) {
            if (sounds.get(sound) != null) {
                sounds.get(sound).dispose();
            }
        }
        Musics[] allMusics = Musics.values();
        for (Musics music : allMusics) {
            if (musics.get(music) != null) {
                musics.get(music).dispose();
            }
        }
        currentMusic = null;
    }

    public void setInGame() {
        if (!inGame){
            setMusicVolume(MUSIC_VOLUME, 2f);
        }
        inGame = true;
    }

    public void setInAttract() {
        if (inGame) setMusicVolume(0, 2f);
        inGame = false;
    }

    public void putSound(Sounds soundType, Sound sound) {
        SoundContainer soundCont = sounds.get(soundType);
        //Array<Sound> soundArr = sounds.get(soundType);
        if (soundCont == null) {
            soundCont = new SoundContainer();
        }

        soundCont.addSound(sound);
        sounds.put(soundType, soundCont);
    }

    public long playSound(Sounds soundOption) {
        return playSound(soundOption, false);
    }

    // this might be a mistake, gonna get real fiddly to have to tweak each sound's volume
    public long playSound(Sounds soundOption, float volume) {
        return playSound(soundOption, false,  volume);
    }

    public long playSound(Sounds soundOption, boolean override) {
        return playSound(soundOption, override, SOUND_VOLUME);
    }

    public long playSound(Sounds soundOption, boolean override, float volume) {
        if (shutUpYourFace) return -1;
        if (!inGame && !override) return -1;

        SoundContainer soundCont = sounds.get(soundOption);
        if (soundCont == null) {
            Gdx.app.log("NoSound", "No sound found for " + soundOption.toString());
            return 0;
        }

        Sound s = soundCont.getSound();
        return (s != null) ? s.play(volume) : 0;
    }

    public Music playMusic(Musics musicOptions) {
        return playMusic(musicOptions, false);
    }
    public Music playTheme() {
        Musics nextTheme = getRandomThemeMusic();
        Gdx.app.log("THEME", "next theme = " + nextTheme.name());
        Music theme = playMusic(nextTheme, false, false);
        theme.setOnCompletionListener(music -> playTheme());
        return theme;
    }

    public Music playMusic(Musics musicOptions, boolean playImmediately) {
        return playMusic(musicOptions, playImmediately, true);
    }

    public Music playMusic(Musics musicOptions, boolean playImmediately, boolean looping) {
        if (playImmediately) {
            if (currentMusic != null && currentMusic.isPlaying()) {
//                currentMusic.setLooping(false);
                currentMusic.stop();
                currentMusic = musics.get(musicOptions);
                currentMusic.setLooping(looping);
                currentMusic.play();
            }
        } else {
            if (currentMusic == null || !currentMusic.isPlaying()) {
                currentMusic = musics.get(musicOptions);
                currentMusic.setLooping(looping);
                currentMusic.play();
            } else {
                currentMusic.setLooping(false);
                currentMusic.setOnCompletionListener(music -> {
                    currentMusic = musics.get(musicOptions);
                    currentMusic.setLooping(looping);
                    currentMusic.play();
                });
            }
        }
        return currentMusic;
    }

    public void fadeMusic(Musics musicOption){
        if (eCurrentMusic == musicOption) return;
        Timeline.createSequence()
                .push(Tween.to(musicVolume, 1, 1).target(0).ease(Linear.INOUT))
                .push(Tween.call((type, source) -> {
                    if (currentMusic != null) currentMusic.stop();
                    eCurrentMusic = musicOption;
                    currentMusic = musics.get(musicOption);
                    currentMusic.setLooping(true);
                    currentMusic.play();
                }))
                .push(Tween.to(musicVolume, 1, 1).target(MUSIC_VOLUME).ease(Linear.INOUT))
                .start(tween);
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void stopSound(Sounds soundOption) {
        SoundContainer soundCont = sounds.get(soundOption);
        if (soundCont != null) {
            soundCont.stopSound();
        }
    }

    public void stopAllSounds() {
        for (SoundContainer soundCont : sounds.values()) {
            if (soundCont != null) {
                soundCont.stopSound();
            }
        }
    }

    public void setMusicVolume(float level, float duration) {
        Tween.to(musicVolume, 1, duration).target(level).ease(Sine.IN).start(tween);
    }
}

class SoundContainer {
    public Array<Sound> sounds;
    public Sound currentSound;

    public SoundContainer() {
        sounds = new Array<Sound>();
    }

    public void addSound(Sound s) {
        if (!sounds.contains(s, false)) {
            sounds.add(s);
        }
    }

    public Sound getSound() {
        if (sounds.size > 0) {
            int randIndex = MathUtils.random(0, sounds.size - 1);
            Sound s = sounds.get(randIndex);
            currentSound = s;
            return s;
        } else {
            System.out.println("No sounds found!");
            return null;
        }
    }

    public void stopSound() {
        if (currentSound != null) {
            currentSound.stop();
        }
    }

    public void dispose() {
        if (currentSound != null) {
            currentSound.dispose();
        }
    }
}
