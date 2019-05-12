package core.game;

import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;
import tools.AudioPlayer;

/**
 * Created by Diego on 19/03/14.
 */
public class AudioObservation implements Comparable<AudioObservation>
{

    /**
     * Category of this observation (static, resource, npc, etc.).
     */
    public int category;

    /**
     * Type of sprite of this observation.
     */
    public int itype;

    /**
     * unique ID for this observation
     */
    public int obsID;

    /**
     * Distance to avatar
     */
    public int intensity;

    private String audioSrc;
    private Wave wave;

    public AudioObservation() {
        // used for learning track
        category = -1;
        itype = -1;
        obsID = -1;
        intensity = -1; // Undefined
    }

    /**
     * New observation. It is the observation of a sprite, recording its ID and position.
     * @param itype type of the sprite of this observation
     * @param id ID of the observation.
     * @param category category of this observation (NPC, static, resource, etc.)
     * @param intensity intensity of this observation, based on distance to avatar
     */
    public AudioObservation(int itype, int id, int category, int intensity, String audio)
    {
        this.itype = itype;
        this.obsID = id;
        this.category = category;
        this.intensity = intensity;
        this.audioSrc = audio;
        wave = AudioPlayer.getInstance().getWave(audioSrc);
    }

    /**
     * Updates this observation
     * @param itype type of the sprite of this observation
     * @param id ID of the observation.
     * @param intensity intensity of this observation.
     * @param category category of this observation (NPC, static, resource, etc.)
     */
    public void update(int itype, int id, int category, int intensity, String audioSrc)
    {
        this.itype = itype;
        this.obsID = id;
        this.category = category;
        this.intensity = intensity;
        this.audioSrc = audioSrc;
        wave = AudioPlayer.getInstance().getWave(audioSrc);
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
        return Integer.compare(intensity, o.intensity);
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
        if(this.obsID != o.obsID) return false;
        if(this.intensity != o.intensity) return false;
        if(this.category != o.category) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Observation{" +
                "category=" + category +
                ", itype=" + itype +
                ", obsID=" + obsID +
                ", intensity=" + intensity +
                "}\n";
    }
}