package core.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import core.game.GameDescription.SpriteData;
import core.logging.Logger;
import core.logging.Message;

public class SLDescription {
	/**
	 * the keyword to encode the sprites in the game
	 */
	private String KEYWORD = "sprite";

	/**
	 * the current game object
	 */
	private Game currentGame;
	/**
	 * the current level
	 */
	private String[] level;
	/**
	 * the current game sprites
	 */
	private SpriteData[] gameSprites;
	/**
	 * the current encoded level
	 */
	private String[][] currentLevel;

	/**
	 * the seed value to encode the names
	 */
	private int shift;

	/**
	 * random object for random seeds
	 */
	private Random random;

	/**
	 * constructor for the SLDescription contains information about game sprites
	 * and the current level
	 *
	 * @param currentGame
	 *            the current game
	 * @param level
	 *            the current level
	 * @param shift
	 *            random seed to encode the sprites
	 * @throws Exception
	 *             if the level is empty
	 */
	public SLDescription(Game currentGame, String[] level, int shift) throws Exception {
		this.currentGame = currentGame;
		this.level = level;
		this.gameSprites = null;
		this.currentLevel = null;

		this.shift = shift;
		this.random = new Random(this.shift);

		this.reset(currentGame, level);
	}

	/**
	 * reset the current variables to a new current game and level
	 *
	 * @param currentGame the new game
	 * @param level the new level
	 * @throws Exception if the level is empty
	 */
	public void reset(Game currentGame, String[] level) throws Exception {
		this.currentGame = currentGame;
		this.level = level;
		if (this.currentGame == null) {
			return;
		}

		// Get all the game sprites in the game
		ArrayList<SpriteData> list = this.currentGame.getSpriteData();
		this.gameSprites = new SpriteData[list.size()];
		for (int i = 0; i < this.gameSprites.length; i++) {
			this.gameSprites[i] = list.get(i);
		}

		if (this.level == null) {
			throw new Exception("Level can't be null while game is not");
		}

		// Encode the level map
		HashMap<Character, ArrayList<String>> levelMapping = this.currentGame.getCharMapping();
		this.currentLevel = new String[level.length][getWidth(level)];
		for (int i = 0; i < this.currentLevel.length; i++) {
			for (int j = 0; j < this.currentLevel[i].length; j++) {
				if (j >= this.currentLevel[i].length) {
					// if the length of the line shorter than the maximum width
					this.currentLevel[i][j] = "";
				} else {
					ArrayList<String> tempSprites = levelMapping.get(level[i].charAt(j));
					if (tempSprites == null || tempSprites.size() == 0) {
						// empty location
						this.currentLevel[i][j] = "";
					} else {
						// encode the different sprites
						this.currentLevel[i][j] = this.encodeName(tempSprites.get(0), this.shift);
						for (int k = 1; k < tempSprites.size(); k++) {
							this.currentLevel[i][j] += ", " + this.encodeName(tempSprites.get(k), this.shift);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the width of the level
	 *
	 * @param level current level
	 * @return width of the level
	 */
	private int getWidth(String[] level) {
		int width = 0;
		for (String s : level) {
			if (s.length() > width) {
				width = s.length();
			}
		}

		return width;
	}

	/**
	 * encode the current sprite index to a new index
	 *
	 * @param index
	 *            current sprite index
	 * @return encoded sprite index
	 */
	private int encodeIndex(int index, int seed) {
		return index ^ seed;
	}

	/**
	 * encode sprite name to an encoded index
	 *
	 * @param name
	 *            the current sprite name to be encoded
	 * @return encoded index for a sprite name
	 */
	private String encodeName(String name, int seed) {
		for (int i = 0; i < this.gameSprites.length; i++) {
			if (this.gameSprites[i].name.toLowerCase().trim().equals(name.toLowerCase().trim())) {
				int result = encodeIndex(i, seed);
				return KEYWORD + "_" + result;
			}
		}
		return "";
	}

	/**
	 * decode the sprite index
	 *
	 * @param value current encoded sprite name
	 * @return correct sprite name
	 */
	private String decodeIndex(int value, int seed) {
		if((value ^ seed) < 0 || (value ^ seed) >= this.gameSprites.length){
			return "";
		}
		return this.gameSprites[value ^ seed].name;
	}

	/**
	 * decode the sprite name
	 *
	 * @param value current encoded sprite name
	 * @return correct sprite name
	 */
	public String decodeName(String value, int seed) {
		if(!value.contains(KEYWORD + "_")){
			return "";
		}
		int index = Integer.parseInt(value.split(KEYWORD + "_")[1]);
		return decodeIndex(index, seed);
	}

	/**
	 * get an array of game sprites
	 *
	 * @return array contain all the game sprites
	 */
	public SpriteData[] getGameSprites() {
		SpriteData[] result = new SpriteData[this.gameSprites.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (SpriteData) this.gameSprites[i].clone();
			result[i].changeSpriteName(result[i].name, this.encodeName(this.gameSprites[i].name, this.shift));
			for (int j = 0; j < result[i].sprites.size(); j++) {
				result[i].changeSpriteName(result[i].sprites.get(j), this.encodeName(this.gameSprites[i].sprites.get(j),
						this.shift));
			}
		}

		return result;
	}

	/**
	 * Return the current level as a comma separated 2D Array
	 *
	 * @return a comma separated 2D Array of Sprites
	 */
	public String[][] getCurrentLevel() {
		return this.currentLevel;
	}

	/**
	 * Decode the rules and strings based on the seed
	 *
	 * @param rules
	 *            current interaction rules to decode
	 * @param wins
	 *            current termination rules to decode
	 * @param seed
	 *            current encoding seed
	 * @return return decoded interaction and termination rules
	 */
	public String[][] modifyRules(String[] rules, String[] wins, int seed) {
		ArrayList<String> modifiedRules = _modify(rules, seed);
		ArrayList<String> modifiedWins = _modify(wins, seed);
		return new String[][] { modifiedRules.toArray(new String[0]), modifiedWins.toArray(new String[0]) };
	}

	private ArrayList<String> _modify(String[] rules, int seed) {
		ArrayList<String> modifiedRules = new ArrayList<>();
		for (String rule : rules) {
			String[] parts = rule.split(" ");
			modifiedRules.add("");
			for (String part : parts) {
				if (part.toLowerCase().contains(KEYWORD + "_")) {
					String[] temp = part.split(KEYWORD + "_");
					String spriteName = this.decodeIndex(Integer.parseInt(temp[1]), seed);
					if (spriteName.length() > 0) {
						modifiedRules.set(modifiedRules.size() - 1, modifiedRules.get(modifiedRules.size() - 1)
								+ temp[0] + spriteName + " ");
					} else {
						Logger.getInstance().addMessage(new Message(Message.WARNING, part
								+ " is undefined in the game."));
					}

				} else {
					modifiedRules.set(modifiedRules.size() - 1, modifiedRules.get(modifiedRules.size() - 1)
							+ part + " ");
				}
			}
		}
		return modifiedRules;
	}

	/**
	 * Get state observation based on the interaction rules and termination conditions
	 *
	 * @param rules current interaction rules
	 * @param wins current termination conditions
	 * @return state observation of the current game using the new interaction
	 *         rules and termination conditions return null when there is errors
	 */
	public StateObservation testRules(String[] rules, String[] wins){
		return this.testRules(rules, wins, null);
	}

	/**
	 * Get state observation based on the interaction rules and termination conditions
	 *
	 * @param rules current interaction rules
	 * @param wins current termination conditions
	 * @param spriteSetStructure current sprite set hierarchy
	 * @return state observation of the current game using the new interaction
	 *         rules and termination conditions return null when there is errors
	 */
	public StateObservation testRules(String[] rules, String[] wins, HashMap<String,
			ArrayList<String>> spriteSetStructure) {
		Logger.getInstance().flushMessages();

		String[][] rw = this.modifyRules(rules, wins, this.shift);
		HashMap<String, String> msprites = new HashMap<>();
		for (SpriteData gameSprite : this.gameSprites) {
			msprites.put(gameSprite.name, gameSprite.toString());
		}
		HashMap<String, ArrayList<String>> msetStructure = new HashMap<>();
		if(spriteSetStructure != null){
			for(String key:spriteSetStructure.keySet()){
				msetStructure.put(key, new ArrayList<>());
				for(int i=0; i<spriteSetStructure.get(key).size(); i++){
					if(spriteSetStructure.get(key).get(i).contains(KEYWORD + "_")){
						String[] parts = spriteSetStructure.get(key).get(i).split(KEYWORD + "_");
						msetStructure.get(key).add(this.decodeIndex(Integer.parseInt(parts[1]), this.shift));
					}
				}
			}
		}

		VGDLRegistry.GetInstance().init();
		this.currentGame.loadDefaultConstr();
		this.currentGame.clearInteractionTerminationData();

		new VGDLParser().parseSpriteSet(this.currentGame, msetStructure, msprites);
		new VGDLParser().parseInteractionTermination(this.currentGame, rw[0], rw[1]);

		this.currentGame.reset();
		this.currentGame.buildStringLevel(this.level, this.random.nextInt());
		if (Logger.getInstance().getMessageCount(Message.ERROR) > 0) {
			return null;
		}
		return this.currentGame.getObservation();
	}

	/**
	 * Disable/Enable the logger
	 * @param value		enable or disable
	 */
	public void enableLogger(boolean value){
	    Logger.getInstance().active = value;
	}
    /**
     * get list of errors from the system
     * 
     * @return a list of errors
     */
    public ArrayList<Message> getErrors() {
	return Logger.getInstance().getMessages(Message.ERROR);
    }
    
    /**
     * get list of warnings from the system
     * 
     * @return a list of warning
     */
    public ArrayList<Message> getWarnings() {
	return Logger.getInstance().getMessages(Message.WARNING);
    }

}
