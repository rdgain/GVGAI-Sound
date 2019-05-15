package tracks;

import core.competition.CompetitionParameters;
import core.game.AudioStateObservation;
import core.game.Game;
import core.player.AudioPlayer;
import core.player.Player;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.StatSummary;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

public class AudioMachine {
    public static final boolean VERBOSE = false;

    /**
     * Reads and launches a game for a human to be played. Graphics always on.
     * 
     * @param game_file
     *            game description file.
     * @param level_file
     *            file with the level to be played.
     */
    public static double[] playOneGame(String game_file, String level_file, String actionFile, int randomSeed, boolean audio) {
		String agentName = "tracks.audioGames.controllers.human.Agent";
		return runOneGame(game_file, level_file, audio, true, agentName, actionFile, randomSeed);
    }

    /**
     * Reads and launches a game for a bot to be played. Graphics can be on or
     * off.
     * 
     * @param game_file
     *            game description file.
     * @param level_file
     *            file with the level to be played.
     * @param audio
     *            true to play sounds, false otherwise.
     * @param agentName
     *            name (inc. package) where the agent is.
     * @param actionFile
     *            filename of the files where the actions of these players, for
     *            this game, should be recorded.
     * @param randomSeed
     *            sampleRandom seed for the sampleRandom generator.
     */
    public static double[] runOneGame(String game_file, String level_file, boolean audio, boolean visuals,
									  String agentName, String actionFile, int randomSeed) {
		VGDLFactory.GetInstance().init(); // This always first thing to do.
		VGDLRegistry.GetInstance().init();

		if (VERBOSE)
			System.out.println(" ** Playing game " + game_file + ", level " + level_file + " **");

		if (CompetitionParameters.OS_WIN)
		{
			System.out.println(" * WARNING: Time limitations based on WALL TIME on Windows * ");
		}

		// First, we create the game to be played..
		Game toPlay = new VGDLParser().parseGame(game_file);
		toPlay.setAudio(audio);
		toPlay.buildLevel(level_file, randomSeed);

		// Warm the game up.
		AudioMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

		// Create players
		boolean anyHuman = isHuman(agentName);
		Player player = AudioMachine.createPlayer(agentName, actionFile, randomSeed, anyHuman);
		if (player == null) {
			// Something went wrong in the constructor, controller disqualified
			toPlay.disqualify();

			// Get the score for the result.
			toPlay.handleResult();
			toPlay.printResult();
			return toPlay.getFullResult();
		}

		// Then, play the game.
		if (visuals)
			toPlay.playAudioGame(new Player[]{player}, randomSeed, anyHuman, 0);
		else
			toPlay.runGame(new Player[]{player}, randomSeed);

		// Finally, when the game is over, we need to tear the players down.
		AudioMachine.tearPlayerDown(toPlay, player);

		// This, the last thing to do in this method, always:
		toPlay.handleResult();
		toPlay.printResult();

		return toPlay.getFullResult();
	}

    /**
     * Reads and launches a game for a bot to be played. It specifies which
     * levels to play and how many times. Filenames for saving actions can be
     * specified. Graphics always off.
     * 
     * @param game_file   game description file.
     * @param level_files  array of level file names to play.
     * @param level_times   how many times each level has to be played.
     * @param actionFiles names of the files where the actions of this player, for this
     *   game, should be recorded. Accepts null if no recording is desired. If not null,
     *   this array must contain as much String objects as level_files.length*level_times.
     */
    public static void runGames(String game_file, String[] level_files, int level_times, String agentName, String[] actionFiles) {
		VGDLFactory.GetInstance().init(); // This always first thing to do.
		VGDLRegistry.GetInstance().init();

		boolean recordActions = false;
		if (actionFiles != null) {
			recordActions = true;
			assert actionFiles.length >= level_files.length
				* level_times : "runGames (actionFiles.length<level_files.length*level_times): "
					+ "you must supply an action file for each game instance to be played, or null.";
		}

		Game toPlay = new VGDLParser().parseGame(game_file);
		StatSummary victories = new StatSummary();
		StatSummary scores = new StatSummary();

		// Determine the random seed for the player.
		int randomSeed = new Random().nextInt();
		Player player = AudioMachine.createPlayer(agentName, null, randomSeed, false);
		if (!(player instanceof AudioPlayer)) {
			// Something went wrong in the constructor, controller disqualified
			toPlay.getAvatar().disqualify(true);
			toPlay.handleResult();
			toPlay.printResult();
			System.out.println("Results in game " + game_file + ", " + -1 + " , " + 0);
			return;
		}

		for (String level_file : level_files) {
			for (int i = 0; i < level_times; ++i) {
				if (VERBOSE)
					System.out.println(" ** Playing game " + game_file + ", level " + level_file + " (" + (i + 1) + "/"
						+ level_times + ") **");

				// build the level in the game.
				toPlay.buildLevel(level_file, randomSeed);

				// Warm the game up.
				AudioMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

				// Play game
				double[] score = toPlay.runGame(new Player[]{player}, randomSeed);

				// Finally, when the game is over, we need to tear the player down.
				if (!AudioMachine.tearPlayerDown(toPlay, player)) {
					score = toPlay.handleResult();
					toPlay.printResult();
				} else {
					toPlay.printResult();
				}

				// Get players stats
				int id = player.getPlayerID();
				scores.add(score[0]);
				victories.add(toPlay.getWinner(id) == Types.WINNER.PLAYER_WINS ? 1 : 0);

				// Reset the game and player.
				toPlay.reset();
				player.reset();
			}
		}

		String vict = "" + victories.mean(), sc = "" + scores.mean();
		System.out.println("Results in game " + game_file + ", " + vict + " , " + sc);
    }

    /**
     * Creates a player given its name with package. This class calls the
     * constructor of the agent and initializes the action recording procedure.
     * PlayerID used is 0, default for single player games.
     * 
     * @param playerName
     *            name of the agent to create. It must be of the type
     *            "<agentPackage>.Agent".
     * @param actionFile
     *            filename of the file where the actions of this player, for
     *            this game, should be recorded.
     * @param randomSeed
     *            Seed for the sampleRandom generator of the game to be played.
     * @param isHuman
     *            Indicates if the player is human
     * @return the player, created and initialized, ready to start playing the
     *         game.
     */
    private static Player createPlayer(String playerName, String actionFile, int randomSeed, boolean isHuman) {
		Player player = null;

        try {
            // create the controller.
            player = createController(playerName);
            if (player != null)
            player.setup(actionFile, randomSeed, isHuman);
            // else System.out.println("No controller created.");

        } catch (Exception e) {
            // This probably happens because controller took too much time to be
            // created.
            e.printStackTrace();
            System.exit(1);
        }

        // System.out.println("Created player.");

        return player;
    }

    /**
    /**
     * Creates and initializes a new controller with the given name. Takes into
     * account the initialization time, calling the appropriate constructor with
     * the state observation and time due parameters.
     * 
     * @param playerName
     *            Name of the controller to instantiate.
     * @return the player if it could be created, null otherwise.
     */

    private static Player createController(String playerName) throws RuntimeException {
		Player player = null;
        try {

            // Determine the time due for the controller creation.
            ElapsedCpuTimer ect = new ElapsedCpuTimer();
            ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

			// Get the class and the constructor with arguments
			// (StateObservation, long).
			Class<? extends Player> controllerClass = Class.forName(playerName).asSubclass(Player.class);
			Class[] gameArgClass = new Class[] {};
			Constructor controllerArgsConstructor = controllerClass.getConstructor(gameArgClass);

			player = (Player) controllerArgsConstructor.newInstance();
			player.setPlayerID(0);

            // Check if we returned on time, and act in consequence.
            long timeTaken = ect.elapsedMillis();
            if (CompetitionParameters.TIME_CONSTRAINED && ect.exceededMaxTime()) {
				long exceeded = -ect.remainingTimeMillis();
				System.out.println("Controller initialization time out (" + exceeded + ").");

				return null;
            } else {
				if (VERBOSE)
					System.out.println("Controller initialization time: " + timeTaken + " ms.");
            }

            // This code can throw many exceptions (no time related):

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.err.println("Constructor " + playerName + "(StateObservation,long) not found in controller class:");
            System.exit(1);

        } catch (ClassNotFoundException e) {
            System.err.println("Class " + playerName + " not found for the controller:");
            e.printStackTrace();
            System.exit(1);

        } catch (InstantiationException e) {
            System.err.println("Exception instantiating " + playerName + ":");
            e.printStackTrace();
            System.exit(1);

        } catch (IllegalAccessException e) {
            System.err.println("Illegal access exception when instantiating " + playerName + ":");
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            System.err.println("Exception calling the constructor " + playerName + "(StateObservation,long):");
            e.printStackTrace();
            System.exit(1);
        }

        // System.out.println("Controller created. " + player.getPlayerID());

        return player;
    }



    /**
     * This methods takes the game and warms it up. This allows Java to finish
     * the runtime compilation process and optimize the code before the proper
     * game starts.
     * 
     * @param toPlay
     *            game to be warmed up.
     * @param howLong
     *            for how long the warming up process must last (in
     *            milliseconds).
     */
		@SuppressWarnings("unchecked")
	private static void warmUp(Game toPlay, long howLong) {
        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(howLong);
        int playoutLength = 10;
        int copyStats = 0;
        int advStats = 0;
        int no_players = toPlay.no_players;

        StatSummary ss1 = new StatSummary();
        StatSummary ss2 = new StatSummary();

        boolean finish = ect.exceededMaxTime();

        ArrayList<Types.ACTIONS>[] actions = new ArrayList[no_players];
        AudioStateObservation stateObs;

		stateObs = toPlay.getObservationAudio();
		actions[0] = stateObs.getAvailableActions();

        // while(!ect.exceededMaxTime())
        while (!finish) {
            for (int i = 0; i < no_players; i++) {
				for (Types.ACTIONS action : actions[i]) {

					AudioStateObservation stCopy = stateObs.copy();
					ElapsedCpuTimer ectAdv = new ElapsedCpuTimer();
					stCopy.advance(action);
					copyStats++;
					advStats++;

					if (ect.remainingTimeMillis() < CompetitionParameters.WARMUP_TIME * 0.5) {
						ss1.add(ectAdv.elapsedNanos());
					}

					for (int j = 0; j < playoutLength; j++) {
						ectAdv = new ElapsedCpuTimer();
						stCopy.advance(actions[i].get(new Random().nextInt(actions[i].size())));
						advStats++;

						if (ect.remainingTimeMillis() < CompetitionParameters.WARMUP_TIME * 0.5) {
							ss2.add(ectAdv.elapsedNanos());
						}
					}
				}

				finish = ect.exceededMaxTime()
					|| (copyStats > CompetitionParameters.WARMUP_CP && advStats > CompetitionParameters.WARMUP_ADV);

				// if(VERBOSE)
				// System.out.println("[WARM-UP] Remaining time: " +
				// ect.remainingTimeMillis() +
				// " ms, copy() calls: " + copyStats + ", advance() calls: " +
				// advStats);
			}
        }

        if (VERBOSE) {
            System.out.println("[WARM-UP] Finished, copy() calls: " + copyStats + ", advance() calls: " + advStats
                + ", time (s): " + ect.elapsedSeconds());
            // System.out.println(ss1);
            // System.out.println(ss2);
        }

        // Reset input to delete warm-up effects.
        Game.ki.resetAll();
    }

    /**
     * Tears the player down. This initiates the saving of actions to file. It
     * should be called when the game played is over.
     * 
     * @param toPlay
     *            game played.
     * @return false if there was a timeout from the players. true otherwise.
     */
    private static boolean tearPlayerDown(Game toPlay, Player player) {
        // This is finished, no more actions, close the writer.
        player.teardown(toPlay);

		// Determine the time due for the controller close up.
		ElapsedCpuTimer ect = new ElapsedCpuTimer();
		ect.setMaxTimeMillis(CompetitionParameters.TEAR_DOWN_TIME);

		// Inform about the result and the final game state.
		player.result(toPlay.getObservationAudio(), ect);

		// Check if we returned on time, and act in consequence.
		long timeTaken = ect.elapsedMillis();
		if (ect.exceededMaxTime()) {
			long exceeded = -ect.remainingTimeMillis();
			System.out.println("Controller tear down time out (" + exceeded + ").");
			toPlay.disqualify();
			return false;
		}

		if (VERBOSE)
			System.out.println("Controller tear down time: " + timeTaken + " ms.");

        return true;
    }

    private static boolean isHuman(String agentName) {
		return agentName.equalsIgnoreCase("tracks.multiPlayer.tools.human.Agent")
				|| agentName.equalsIgnoreCase("tracks.singlePlayer.tools.human.Agent")
				|| agentName.equalsIgnoreCase("tracks.audioGames.controllers.human.Agent");
	}

}
