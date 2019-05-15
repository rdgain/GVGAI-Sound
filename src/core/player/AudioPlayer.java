package core.player;

import core.game.AudioStateObservation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;

public abstract class AudioPlayer extends Player {
    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        return null;
    }
    @Override
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
        return null;
    }
    @Override
    public final void result(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer) {}
    @Override
    public final void resultMulti(StateObservationMulti stateObs, ElapsedCpuTimer elapsedCpuTimer) {}

    /**
     * Function called when the game is over. This method must finish before CompetitionParameters.TEAR_DOWN_TIME,
     *  or the agent will be DISQUALIFIED
     * @param aso the game state at the end of the game
     * @param elapsedCpuTimer timer when this method is meant to finish.
     */
    public void result(AudioStateObservation aso, ElapsedCpuTimer elapsedCpuTimer)
    {
    }
}
