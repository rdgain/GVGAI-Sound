package core.game;

import ontology.Types;
import tools.KeyHandler;
import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class AudioStateObservation {

    /**
     * ID of the player that sees this observation
     */
    int playerID;

    /**
     * This is the model of the game, used to apply an action and
     * get to the next state. This model MUST NOT be public.
     */
    protected ForwardModel model;

    /**
     * Constructor for StateObservation. Requires a forward model
     *
     * @param a_model forward model of the game.
     */
    public AudioStateObservation(ForwardModel a_model, int playerID) {
        model = a_model;
        this.playerID = playerID;
    }

    /**
     * Returns an exact copy of the state observation object.
     *
     * @return a copy of the state observation.
     */
    public StateObservation copy() {
        return new StateObservation(model.copy(), this.playerID);
    }

    /**
     * Advances the state using the action passed as the move of the agent.
     * It updates all entities in the game. It modifies the object 'this' to
     * represent the next state after the action has been executed and all
     * entities have moved.
     * <p/>
     * Note: stochastic events will not be necessarily the same as in the real game.
     *
     * @param action agent action to execute in the next cycle.
     */
    public void advance(Types.ACTIONS action) {
        model.advance(action);
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar.
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions()
    {
        return model.getAvatarActions(false);
    }

    /**
     * Returns the actions that are available in this game for
     * the avatar. If the parameter 'includeNIL' is true, the array contains the (always available)
     * NIL action. If it is false, this is equivalent to calling getAvailableActions().
     * @param includeNIL true to include Types.ACTIONS.ACTION_NIL in the array of actions.
     * @return the available actions.
     */
    public ArrayList<Types.ACTIONS> getAvailableActions(boolean includeNIL)
    {
        return model.getAvatarActions(includeNIL);
    }

//    /**
//     * Returns the game tick of this particular observation.
//     * @return the game tick.
//     */
//    public int getGameTick()
//    {
//        return model.getGameTick();
//    }

    /**
     * Indicates if there is a game winner in the current observation.
     * Possible values are Types.WINNER.PLAYER_WINS, Types.WINNER.PLAYER_LOSES and
     * Types.WINNER.NO_WINNER.
     * @return the winner of the game.
     */
    public Types.WINNER getGameWinner()
    {
        return model.getGameWinner();
    }

    /**
     * Indicates if the game is over or if it hasn't finished yet.
     * @return true if the game is over.
     */
    public boolean isGameOver()
    {
        return model.isGameOver();
    }

    public ArrayList<AudioObservation> getAudioObservations() {
        return model.getAudioObservations();
    }


    /**
     * Returns key handler available to the player.
     * @param playerID ID of the player to query.
     * @return KeyHandler object.
     */
    public KeyHandler getKeyHandler(int playerID) { return model.avatars[playerID].getKeyHandler(); }

}
