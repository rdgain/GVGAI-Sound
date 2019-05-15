package core.player;

import core.game.AudioStateObservation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;


/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 13:42
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 *
 * Subclass of Player for multi player games.
 * Implements single players act method, returns NULL.
 * Keeps track of playerID and disqualification flag.
 */

public abstract class AbstractMultiPlayer extends Player {

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player. The action returned must be contained in the
     * actions accessible from stateObs.getAvailableActions(), or no action
     * will be applied.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state.
     */
    @Override
    public final Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        return Types.ACTIONS.ACTION_NIL;
    }

    @Override
    public final Types.ACTIONS act(AudioStateObservation aso, ElapsedCpuTimer elapsedCpuTimer) {
        return null;
    }

    @Override
    public void reset() {}
}
