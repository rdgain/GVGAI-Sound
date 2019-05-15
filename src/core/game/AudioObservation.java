package core.game;

import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;
import tools.SoundManager;

/**
 * Created by Diego on 19/03/14.
 */
public class AudioObservation implements Comparable<AudioObservation>
{
    /**
     * Type of sprite of this observation.
     */
    public int itype;

    /**
     * Distance to avatar
     */
    public double intensity;

    public String audioSrc;
    private Wave wave;

    public AudioObservation() {
        // used for learning track
        itype = -1;
        intensity = -1; // Undefined
    }

    /**
     * New observation. It is the observation of a sprite, recording its ID and position.
     * @param itype type of the sprite of this observation
     * @param intensity intensity of this observation, based on distance to avatar
     */
    public AudioObservation(int itype, double intensity, String audio)
    {
        this.itype = itype;
        this.intensity = intensity;
        this.audioSrc = audio;
        wave = SoundManager.getInstance().getWave(audioSrc);
    }

    /**
     * Updates this observation
     * @param itype type of the sprite of this observation
     * @param intensity intensity of this observation.
     */
    public void update(int itype, double intensity, String audioSrc)
    {
        this.itype = itype;
        this.intensity = intensity;
        this.audioSrc = audioSrc;
        wave = SoundManager.getInstance().getWave(audioSrc);
    }

    public byte[] getBytes() {
        return wave.getBytes();
    }

    public byte[] getFingerprint() {
        return wave.getFingerprint();
    }

    public double[] getNormalizedAmplitudes() {
        return wave.getNormalizedAmplitudes();
    }

    public Spectrogram getSpectrogram() {
        return wave.getSpectrogram();
    }

    /**
     * Compares this observation to others, using distances to the reference position.
     * @param o other observation.
     * @return -1 if this precedes o, 1 if same distance or o is closer to reference.
     */
    @Override
    public int compareTo(AudioObservation o) {
        return Double.compare(intensity, o.intensity);
    }

    /**
     * Compares two Observations to check if they are equal. The reference attribute is NOT
     * compared in this object.
     * @param other the other observation.
     * @return true if both objects are the same Observation.
     */
    public boolean equals(Object other)
    {
        if(!(other instanceof AudioObservation))
            return false;

        AudioObservation o = (AudioObservation) other;
        if(this.itype != o.itype) return false;
        if(this.intensity != o.intensity) return false;
        if(!this.audioSrc.equals(o.audioSrc)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Observation{" +
                ", itype=" + itype +
                ", intensity=" + intensity +
                "}\n";
    }
}