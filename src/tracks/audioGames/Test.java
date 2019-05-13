package tracks.audioGames;

import tools.Utils;
import tracks.ArcadeMachine;
import tracks.AudioMachine;

import java.util.Random;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test {

    public static void main(String[] args) {

		// Available controllers:
		String sampleQLearningSimple = "tracks.audioGames.controllers.qLearningKBS.Agent";
		String sampleQLearningIntensity = "tracks.audioGames.controllers.qLearningKBI.Agent";
		String sampleQLearning = "tracks.audioGames.controllers.simple.Agent";
		String sampleRandomController = "tracks.audioGames.controllers.random.Agent";

		//Load available games
		String audioGamesCollection =  "examples/all_games_audio.csv";
		String[][] games = Utils.readGames(audioGamesCollection);

		//Game settings
		int seed = new Random().nextInt();
		int gameIdx = 0;
		int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);

		String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
						// + levelIdx + "_" + seed + ".txt";
						// where to record the actions
						// executed. null if not to save.

		// 1. This starts a game, in a level, played by a human.
//		AudioMachine.playOneGame(game, level1, recordActionsFile, seed, true);

		// 2. This plays a game in a level by the controller.
//		AudioMachine.runOneGame(game, level1, true, true, sampleQLearningIntensity, recordActionsFile, seed);

		// 3. This plays a single game, in one level, M times :
//		int M = 10;
//		AudioMachine.runGames(game, new String[]{level1}, M, sampleQLearningSimple, null);

		//5. This plays a single game, in the first L levels, M times each. Actions to file optional (set saveActions to true).
		int L = 5, M = 20;
		boolean saveActions = false;
		String[] levels = new String[L];
		String[] actionFiles = new String[L*M];

		int actionIdx = 0;
		for(int j = 0; j < L; ++j){
			levels[j] = game.replace(gameName, gameName + "_lvl" + j);
			if(saveActions) for(int k = 0; k < M; ++k)
				actionFiles[actionIdx++] = "actions_game_" + gameIdx + "_level_" + j + "_" + k + ".txt";
		}
		AudioMachine.runGames(game, levels, M, sampleRandomController, saveActions ? actionFiles : null);
    }
}
