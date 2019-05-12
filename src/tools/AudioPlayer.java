package tools;

import com.musicg.wave.Wave;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("FieldCanBeLocal")
public class AudioPlayer {

    private static AudioPlayer audioPlayer = null;
    private static HashMap<String, Clip> clips;
    private static HashMap<String, Wave> waves;
    private static String path = "audio/";
    private static String extension = ".wav";

    public static AudioPlayer getInstance()
    {
        if (audioPlayer == null)
        {
            audioPlayer = new AudioPlayer();
        }
        return audioPlayer;
    }

    private AudioPlayer()
    {
        clips = new HashMap<>();
        waves = new HashMap<>();
    }

    private Clip getClip(String audio_file) {
        if(clips.containsKey(audio_file)) {
            return clips.get(audio_file);
        }

        try {
            String fullPath = path + audio_file + extension;
            if ((new File(fullPath).exists())) {
                // Create AudioInputStream object
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fullPath).getAbsoluteFile());
                // Create clip reference
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                clips.put(audio_file, clip);
                return clip;
            }
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Wave getWave(String audio_file) {
        if (waves.containsKey(audio_file)) {
            return waves.get(audio_file);
        }

        String fullPath = path + audio_file + extension;
        if ((new File(fullPath).exists())) {
            Wave wave = new Wave(audio_file);
            waves.put(audio_file, wave);
            return wave;
        }

        return null;
    }

    // Method to play the audio
    public void play(String audio_file, float volume) {
        Clip clip = getClip(audio_file);
        if (clip != null) {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            fc.setValue(volume);
            clip.start();
        }
    }

    // Method to restart the audio
    public void restart(String audio_file, float volume) {
        Clip clip = getClip(audio_file);
        if (clip != null) {
            clip.setMicrosecondPosition(0);
            this.play(audio_file, volume);
        }
    }

//    // Method to pause the audio
//    public void pause(String audio_file) {
//        this.currentFrame = this.clip.getMicrosecondPosition();
//        getClip(audio_file).stop();
//    }
//
//    // Method to resume the audio
//    public void resumeAudio(String audio_file) {
//        Clip clip = getClip(audio_file);
//        clip.close();
//        resetAudioStream(audio_file);
//        clip.setMicrosecondPosition(currentFrame);
//        this.play(audio_file);
//    }
//
//
//    // Method to stop the audio
//    public void stop(String audio_file) {
//        Clip clip = getClip(audio_file);
//        currentFrame = 0L;
//        clip.stop();
//        clip.close();
//    }
//
//    // Method to jump over a specific part
//    public void jump(String audio_file, long c)  {
//        Clip clip = getClip(audio_file);
//        if (c > 0 && c < clip.getMicrosecondLength()) {
//            clip.stop();
//            clip.close();
//            resetAudioStream(audio_file);
//            currentFrame = c;
//            clip.setMicrosecondPosition(c);
//            this.play(audio_file);
//        }
//    }
//
//    // Method to reset audio stream
//    public void resetAudioStream(String audio_file)  {
//        Clip clip = getClip(audio_file);
//        if (clip != null) {
//            clip.close();
//            String fullPath = path + audio_file + extension;
//            try {
//                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fullPath).getAbsoluteFile());
//                clip.open(audioInputStream);
//            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
