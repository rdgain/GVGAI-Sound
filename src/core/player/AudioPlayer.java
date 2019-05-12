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

    public abstract Types.ACTIONS act(AudioStateObservation aso, ElapsedCpuTimer elapsedCpuTimer);
}
