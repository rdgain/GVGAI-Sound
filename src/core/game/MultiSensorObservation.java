package core.game;

import java.applet.AudioClip;

/**
 * This class adds non-visual sensor observations:
 *  - Audio sensors
 */
public class MultiSensorObservation extends StateObservation {

    /**
     * Constructor for StateObservation. Requires a forward model
     *
     * @param a_model  forward model of the game.
     * @param playerID id of the player
     */
    public MultiSensorObservation(ForwardModel a_model, int playerID) {
        super(a_model, playerID);
    }

    public AudioClip[][] getAudioGrid() {
        return model.getAudioGrid();
    }
}
