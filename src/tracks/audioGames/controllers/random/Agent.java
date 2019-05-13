package tracks.audioGames.controllers.random;

import core.game.AudioStateObservation;
import core.player.AudioPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AudioPlayer {
    /**
     * Random generator for the agent.
     */
    protected Random randomGenerator;

    /**
     * Public constructor with state observation and time due.
     */
    public Agent()
    {
        reset();
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(AudioStateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        int index = randomGenerator.nextInt(actions.size());
        return actions.get(index);
    }

    @Override
    public void reset() {
        randomGenerator = new Random();
    }

}
