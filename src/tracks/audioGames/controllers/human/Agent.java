package tracks.audioGames.controllers.human;

import core.game.AudioStateObservation;
import core.game.Game;
import core.player.AudioPlayer;
import ontology.Types;
import tools.Direction;
import tools.ElapsedCpuTimer;
import tools.Utils;

import static ontology.Types.DEFAULT_SINGLE_PLAYER_KEYIDX;

/**
 * Created by diego on 06/02/14.
 */
public class Agent extends AudioPlayer
{
    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(AudioStateObservation so, ElapsedCpuTimer elapsedTimer) {}

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(AudioStateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        Direction move = Utils.processMovementActionKeys(Game.ki.getMask(), DEFAULT_SINGLE_PLAYER_KEYIDX);
        boolean useOn = Utils.processUseKey(Game.ki.getMask(), DEFAULT_SINGLE_PLAYER_KEYIDX);

        //In the keycontroller, move has preference.
        Types.ACTIONS action = Types.ACTIONS.fromVector(move);

        if (action == Types.ACTIONS.ACTION_NIL && useOn) {
            action = Types.ACTIONS.ACTION_USE;
        }

        return action;
    }

    @Override
    public void reset() {}
}
