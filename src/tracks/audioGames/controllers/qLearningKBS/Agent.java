package tracks.audioGames.controllers.qLearningKBS;

import core.game.AudioObservation;
import core.game.AudioStateObservation;
import core.player.AudioPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Pair;
import tracks.audioGames.controllers.FeatureState.AudioInfoState;
import tracks.audioGames.controllers.FeatureState.LearningState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Mike on 10.07.2017.
 *
 * Brief description of the controller
 *      Simple Q-learning using 'most' of the Avatar information.
 *      Limit health point was not used because it was difficult to scale.
 *      Avatar position was not used because the screen sizes can be different, so nothing to rely on in that case.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Agent extends AudioPlayer {

    private Random random;

    private HashMap<LearningState, HashMap<Types.ACTIONS,Double>> QValues;
    private static double ALPHA = 0.05;
    private static double GAMMA = 0.8;
    private static double EPSILON = 0.1;
    private LearningState previousState;
    private double previousReward;
    private double discount = 0.99;

    // Mapping from sounds to a list of pairs (intensity, value). The values for intensities are updated at the end
    // of each game based on the game's result.
    private HashMap<String, Double> soundKnowledge;
    private ArrayList<ArrayList<AudioObservation>> audioTrace;

    public Agent(){
        QValues = new HashMap<>();
        random = new Random();
        soundKnowledge = new HashMap<>();
        reset();
    }

    public void reset() {
        audioTrace = new ArrayList<>();
    }

    /**
     * Method used to determine the next move to be performed by the agent.
     * This method can be used to identify the current state of the game and all
     * relevant details, then to choose the desired course of action.
     *
     * @param aso Audio observation of the current state of the game to be used in deciding
     *            the next action to be taken by the agent.
     * @param elapsedCpuTimer Timer (40ms)
     * @return The action to be performed by the agent.
     */
    @Override
    public Types.ACTIONS act(AudioStateObservation aso, ElapsedCpuTimer elapsedCpuTimer) {
        ArrayList<Types.ACTIONS> actions = aso.getAvailableActions();
        int nActions = actions.size();

        LearningState currentState = new AudioInfoState(aso);
        ArrayList<AudioObservation> observations = aso.getAudioObservations();
        double stateReward = heuristicValue(observations);
        audioTrace.add(observations);

        // Update Q Value
        if (previousState != null) {
            HashMap<Types.ACTIONS,Double> mapper = QValues.get(previousState);
            Types.ACTIONS lastAction = aso.getAvatarLastAction();

            // Update the previous values if we have one stored
            if (mapper.containsKey(lastAction)) {
                double oldQ = mapper.get(lastAction);
                double plusReward = stateReward - previousReward;

                // Actual Q-learning equation
                double newQ = oldQ + ALPHA*(plusReward + GAMMA*(getMaxQNext(previousState))-oldQ);
                mapper.replace(lastAction,newQ);
            }

            // Or just put game score if we haven't found this before
            else
            {
                mapper.put(lastAction, stateReward);
            }
        }

        Types.ACTIONS toActAction;

        // Put new key in mapper if we don't have it
        if (!QValues.containsKey(currentState)) {
            HashMap<Types.ACTIONS,Double> mapper = new HashMap<>();
            QValues.put(currentState, mapper);
        }

        // Get the best action with probability 1-EPSILON, if we're still learning, if validating, just pick the best we know
        if(random.nextDouble() > EPSILON) {
            toActAction = getMaxAction(currentState, actions);
        }

        // otherwise pick randomly
        else toActAction = actions.get(random.nextInt(nActions));

        previousState = currentState;
        previousReward = stateReward;

        return toActAction;
    }

    private double heuristicValue(ArrayList<AudioObservation> observations) {
        double score = 0;
        int count = 0;

        // + for good sounds, - for bad sounds
        for (AudioObservation ao : observations) {
            // Look for current intensity of this sound in our knowledge and add its value if in there
            if (soundKnowledge.containsKey(ao.audioSrc)) {
                score += soundKnowledge.get(ao.audioSrc);
                count += 1;
            }
        }

        if (count > 0) return score/count;
        return -1;
    }

    @Override
    public void result(AudioStateObservation aso, ElapsedCpuTimer elapsedCpuTimer) {
        int result = aso.getGameWinner() == Types.WINNER.PLAYER_WINS ? 1 : -1;
        int finalTick = aso.getGameTick();
        // Update knowledge base at the end of the game
        int tick = 0;
        for (ArrayList<AudioObservation> l : audioTrace) {
            double weight = result * Math.pow(discount, finalTick - tick);

            for (AudioObservation ao : l) {
                String audioSrc = ao.audioSrc;

                // Try to find this sound in knowledge
                if (soundKnowledge.containsKey(audioSrc)) {
                    double currentWeight = soundKnowledge.get(audioSrc);
                    double newWeight = (currentWeight + weight) / 2.0;
                    double updateWeight = newWeight - currentWeight;
                    soundKnowledge.put(audioSrc, currentWeight + ALPHA * updateWeight);
                } else {
                    // Could not find sound, create new entry
                    soundKnowledge.put(audioSrc, weight);
                }
            }
            tick++;
        }
//        System.out.println(getKnowledgeBaseSize());
//        System.out.println(soundKnowledge);
    }

    private int getKnowledgeBaseSize() {
        return soundKnowledge.size();
    }

    private Types.ACTIONS getMaxAction(LearningState state, ArrayList<Types.ACTIONS> actions) {
        int index = random.nextInt(actions.size());

        if (!QValues.containsKey(state))
            return actions.get(index);

        HashMap<Types.ACTIONS, Double> mapper = QValues.get(state);
        if (mapper.keySet().size() == 0)
            return actions.get(index);

        Types.ACTIONS maxAction = mapper.keySet().iterator().next();
        for (Types.ACTIONS action : mapper.keySet()) {
            if (mapper.get(maxAction) < mapper.get(action))
                maxAction = action;
        }
        return maxAction;
    }

    // Another get max, but for the next possible states, given the current one
    private double getMaxQNext(LearningState state) {
        if (!QValues.containsKey(state))
            return 0;

        HashMap<Types.ACTIONS, Double> mapper = QValues.get(state);
        if (mapper.keySet().size() == 0)
            return 0;

        double maxAction = -Double.MAX_VALUE;
        for (Types.ACTIONS actions : mapper.keySet()) {
            if (maxAction < mapper.get(actions))
                maxAction = mapper.get(actions);
        }
        return maxAction;
    }
}
