package core.player;

import core.game.AudioStateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 13:42
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 *
 * Subclass of Player, for Single Player games.
 * Implements multi player act method (returns NULL).
 */
public abstract class AbstractPlayer extends Player {

    @Override
    public final Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
        return Types.ACTIONS.ACTION_NIL;
    }

    @Override
    public final Types.ACTIONS act(AudioStateObservation aso, ElapsedCpuTimer elapsedCpuTimer) {
        return null;
    }

    @Override
    public final void reset() {}
}
