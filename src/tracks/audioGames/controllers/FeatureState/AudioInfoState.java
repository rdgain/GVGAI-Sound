package tracks.audioGames.controllers.FeatureState;

import core.game.AudioObservation;
import core.game.AudioStateObservation;
import ontology.Types;

import java.util.ArrayList;

public class AudioInfoState implements LearningState {
    private Types.ACTIONS avatarLastAction;

    private AudioObservation[] obs;
    private int resultLength = 10;

    public AudioInfoState(AudioStateObservation aso)
    {
        obs = new AudioObservation[resultLength];
        this.avatarLastAction = aso.getAvatarLastAction();

        ArrayList<AudioObservation> observations = aso.getAudioObservations();
        int nObservations = observations.size();
        for (int i = 0; i < resultLength && i < nObservations; i++) {
            obs[i] = observations.get(i);
        }
    }

    @Override
    // This is super necessary to do the equals bit, or it won't even call the method
    public int hashCode(){
        return avatarLastAction.ordinal()*10;
    }

    @Override
    public boolean equals(Object state)
    {
        if (!(state instanceof AudioInfoState)) return false;

        // Consider two states are the same when all observations are the same
        AudioInfoState anotherState = (AudioInfoState)state;
        if (!avatarLastAction.equals(anotherState.avatarLastAction)) return false;
        for (int i = 0; i < resultLength; i++) {
            if (!obs[i].equals(anotherState.obs[i])) return false;
        }
        return true;
    }
}
