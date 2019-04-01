package tracks.singlePlayer.tools.replayer;
import javax.sound.midi.*;

import java.util.ArrayList;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    /**
     * List of actions to execute. They must be loaded using loadActions().
     */
    private ArrayList<Types.ACTIONS> actions;

    /**
     * Current index of the action to be executed.
     */
    private int actionIdx;

    MidiChannel[] mChannels;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        actions = new ArrayList<>();

        try {
            Synthesizer midiSynth = MidiSystem.getSynthesizer();
            midiSynth.open();

            //get and load default instrument and channel lists
            Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
            mChannels = midiSynth.getChannels();

            midiSynth.loadInstrument(instr[0]);//load an instrument

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

    }

    /**
     * Loads the action from the contents of the object received as parameter.
     * @param actionsToLoad ArrayList of actions to execute.
     */
    public void setActions(ArrayList<Types.ACTIONS> actionsToLoad)
    {
        actionIdx = 0;
        this.actions = actionsToLoad;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
    {
        Types.ACTIONS action = actions.get(actionIdx);

        long remaining = elapsedTimer.remainingTimeMillis();

        // http://newt.phys.unsw.edu.au/jw/notes.html

        int noteNumber = 84;

        if (action.equals(Types.ACTIONS.ACTION_LEFT))
            noteNumber = 60;
        else if (action.equals(Types.ACTIONS.ACTION_RIGHT))
            noteNumber = 72;
        else if (action.equals(Types.ACTIONS.ACTION_UP))
            noteNumber = 64;
        else if (action.equals(Types.ACTIONS.ACTION_DOWN))
            noteNumber = 76;
        else if (action.equals(Types.ACTIONS.ACTION_USE))
            noteNumber = 55;

        if (actionIdx == 0 || !actions.get(actionIdx-1).equals(action))
            mChannels[0].noteOn(noteNumber, 100);//On channel 0, play note number 60 with velocity 100

        while(remaining > 1)
        {
            //This allows visualization of the replay.
            remaining = elapsedTimer.remainingTimeMillis();
        }

        if (actionIdx == actions.size()-1 || !actions.get(actionIdx+1).equals(action))
            mChannels[0].noteOff(noteNumber);//turn of the note

        actionIdx++;

        return action;
    }
}
