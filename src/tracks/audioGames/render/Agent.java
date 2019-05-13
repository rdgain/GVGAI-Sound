package tracks.audioGames.render;

import core.competition.CompetitionParameters;
import core.game.AudioObservation;
import core.game.AudioStateObservation;
import core.player.AudioPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.SoundManager;

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
    private int idx;

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
        int tick = stateObs.getGameTick();

        if (stateObs.getGameTick() < 50) {
            ArrayList<AudioObservation> observations = stateObs.getAudioObservations();
            for (AudioObservation ao : observations) {
                SoundManager.getInstance().render(ao.audioSrc, tick, idx);
                idx++;
            }
        }
        Types.ACTIONS action = actions.get(index);
        System.out.println(tick + " " + action);
        return action;
    }

    @Override
    public void reset() {
        randomGenerator = new Random();
        idx = 0;
    }

}
