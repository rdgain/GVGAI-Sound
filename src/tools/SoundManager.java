package tools;

import com.musicg.wave.Wave;
import javax.sound.sampled.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("FieldCanBeLocal")
public class SoundManager {

    private static SoundManager audioPlayer = null;
    private static HashMap<String, Clip> clips;
    private static HashMap<String, Wave> waves;
    private static HashMap<String, int[][]> samples;
    private static String path = "audio/";
    private static String extension = ".wav";

    private BufferedWriter writer;

    public static SoundManager getInstance()
    {
        if (audioPlayer == null)
        {
            audioPlayer = new SoundManager();
        }
        return audioPlayer;
    }

    private SoundManager()
    {
        clips = new HashMap<>();
        waves = new HashMap<>();
        samples = new HashMap<>();
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

    private int[][] getSamples(String audio_file) {
        if(samples.containsKey(audio_file)) {
            return samples.get(audio_file);
        }

        try {
            String fullPath = path + audio_file + extension;
            if ((new File(fullPath).exists())) {
                // Create AudioInputStream object and get samples from file
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fullPath).getAbsoluteFile());
                int frameLength = (int) audioInputStream.getFrameLength();
                int frameSize = audioInputStream.getFormat().getFrameSize();
                byte[] eightBitByteArray = new byte[frameLength * frameSize];

                int result = audioInputStream.read(eightBitByteArray);
                int channels = audioInputStream.getFormat().getChannels();
                int[][] s = new int[channels][frameLength];

                int sampleIndex = 0;
                for (int t = 0; t < eightBitByteArray.length;) {
                    for (int channel = 0; channel < channels; channel++) {
                        int low = (int) eightBitByteArray[t];
                        t++;
                        int high = (int) eightBitByteArray[t];
                        t++;
                        int sample = getSixteenBitSample(high, low);
                        s[channel][sampleIndex] = sample;
                    }
                    sampleIndex++;
                }
                samples.put(audio_file, s);

                return s;
            }
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getSixteenBitSample(int high, int low) {
        return (high << 8) + (low & 0x00ff);
    }

    public Wave getWave(String audio_file) {
        if (waves.containsKey(audio_file)) {
            return waves.get(audio_file);
        }

        String fullPath = path + audio_file + extension;
        if ((new File(fullPath).exists())) {
            Wave wave = new Wave(fullPath);
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

    public void render(String audio_file, int gameTick, int idx) {
        int[][] sample = getSamples(audio_file);

        if (sample != null) {
            String file = path + "img/" + audio_file + "_" + gameTick;
            if (idx >= 0) {
                file += "_" + idx;
            }
            file += ".txt";
            try {
                writer = new BufferedWriter(new FileWriter(new File(file)));
                for (int[] ints : sample) {
                    for (int anInt : ints) {
                        writer.write(anInt + " ");
                    }
                    writer.write("\n");
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
