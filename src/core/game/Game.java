package core.game;

import core.competition.CompetitionParameters;
import core.content.Content;
import core.content.GameContent;
import core.content.ParameterContent;
import core.content.SpriteContent;
import core.game.GameDescription.InteractionData;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.logging.Logger;
import core.logging.Message;
import core.player.Player;
import core.termination.Termination;
import core.vgdl.*;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import ontology.effects.Effect;
import ontology.effects.TimeEffect;
import ontology.sprites.Resource;
import tools.*;
import tools.pathfinder.Node;
import tools.pathfinder.PathFinder;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 17/10/13 Time: 13:42 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
@SuppressWarnings("unchecked")
public abstract class Game {

	/**
	 * indicates if player i is human or not
	 */
	public boolean[] humanPlayer;

	/**
	 * z-level of sprite types (in case of overlap)
	 */
	int[] spriteOrder;

	/**
	 * Singletons of the game.
	 */
	boolean[] singletons;

	/**
	 * Content objects for the different sprite types.. The index is the type of
	 * object Content encloses information about the class of the object and its
	 * parameters.
	 */
	Content[] classConst;

	/**
	 * Parameters for a Game Space. Unused in normal games.
	 */
	public HashMap<String, ParameterContent> parameters;

	/**
	 * List of template sprites, one for each object in the above "classConst"
	 * array.
	 */
	VGDLSprite[] templateSprites;

	/**
	 * Groups of sprites in the level. Each element of the array is a collection
	 * of sprites of a given type, which is also the index of the array.
	 */
	protected SpriteGroup[] spriteGroups;

	/**
	 * Relationships for collisions: double array of (list of) effects.
	 * Interaction between two sprites can trigger more than one effect.
	 * collisionEffects[] -> int id of the FIRST element taking part on the
	 * effects. collisionEffects[][] -> int id of the SECOND element taking part
	 * on the effects.
	 *
	 */
	ArrayList<Effect>[][] collisionEffects;

	/**
	 * Pairs of all defined effects in the game.
	 */
	ArrayList<Pair<Integer, Integer>> definedEffects;

	/**
	 * List of EOS effects
	 */
	ArrayList<Effect>[] eosEffects;

	/**
	 * List of TIME effects
	 */
	TreeSet<TimeEffect> timeEffects;

	/**
	 * List of types that can trigger an EOS effect.
	 */
	ArrayList<Integer> definedEOSEffects;

	/**
	 * Historic of events related to the avatar happened during the game. The
	 * entries are ordered asc. by game step.
	 */
	TreeSet<Event> historicEvents;

	/**
	 * For each entry, int identifier of sprite type, a list with all the itypes
	 * this sprite belongs to.
	 */
	ArrayList<Integer>[] iSubTypes;

	/**
	 * For each entry, int identifier of sprite type, a list with all the itypes
	 * this sprite belongs to.
	 */
	ArrayList<Pair<Integer, Long>>[] shieldedEffects;

	/**
	 * Arraylist to hold collisions between objects in every frame
	 */
	Bucket[] bucketList;

	/**
	 * Mapping between characters in the level and the entities they represent.
	 */
	protected HashMap<Character, ArrayList<String>> charMapping;

	/**
	 * Termination set conditions to finish the game.
	 */
	protected ArrayList<Termination> terminations;

	/**
	 * List of sprites killed in the game.
	 */
	public ArrayList<VGDLSprite> kill_list;

	/**
	 * Limit number of each resource type
	 */
	int[] resources_limits;

	/**
	 * Color for each resource
	 */
	private Color[] resources_colors;

	/**
	 * Screen size.
	 */
	Dimension screenSize;

	/**
	 * Dimensions of the game.
	 */
	protected Dimension size;

	/**
	 * Indicates if the game is stochastic.
	 */
	protected boolean is_stochastic;

	/**
	 * Number of sprites this game has.
	 */
	int num_sprites;

	/**
	 * Game tick
	 */
	protected int gameTick;

	/**
	 * Handling when the window is closed
	 */
	public static WindowInput wi = new WindowInput();

	/**
	 * Quick reference to the gamer
	 */
	MovingAvatar[] avatars;

	/**
	 * Indicates if the game is ended.
	 */
	boolean isEnded;

	/**
	 * State observation for this game.
	 */
	private ForwardModel fwdModel;

	/**
	 * Maximum number of sprites in a game.
	 */
	static int MAX_SPRITES;

	/**
	 * Random number generator for this game. It can only be received when the
	 * game is started.
	 */
	private Random random;

	/**
	 * Id of the sprite type "avatar".
	 */
	private int avatarId;

	/**
	 * Id of the sprite type "wall".
	 */
	private int wallId;

	/**
	 * Next ID to generate for sprites;
	 */
	int nextSpriteID;

	/**
	 * Key Handler for human play. The default is
	 * CompetitionParameters.KEY_INPUT
	 */
	public String key_handler;

	/**
	 * Pathfinder.
	 */
	PathFinder pathf;

	/**
	 * Avatars last actions. Array for all avatars in the game. Index in array
	 * corresponds to playerID.
	 */
	Types.ACTIONS[] avatarLastAction;

	boolean playAudio;
	public boolean audio_game;

	/**
	 * VGDL variables, can be set at the top of the VGDL description.
	 */
	public int block_size = 10; // Size of the block in pixels.
	public int no_players = 1; // default to single player
	public int no_counters = 0; // default no counters

	// Sets vision range for the avatars, the value is the grid cell's distance from the avatar, if lower, the cell
	// would be observed; otherwise nothing would be observed there.
	public int vision_range = -1; // default full observability

	// Similar to $vision_range$, but indicates the range for receiving observations from audio sensors.
	public int hearing_range = -1;  // default full observability

	// Sets vision mode for avatars, either off (0) or on (1). If off, the system would only check the vision range.
	// If on, grid cells previously observed but not currently in vision range would be included in the observation,
	// as well as the current cells in vision range.
	public int fog_of_war = 0; // default fog of war is off

	/**
	 * Vars for VGDL variables.
	 */

	// Array of counters, one for each avatar
	public int[] counter;

	// Grid the same size as observation grid, indicates for each cell if observable. Each grid cell is an array
	// with one boolean per avatar, indicating the avatar's observability
	public boolean[][][] fog_grid;

	public static KeyHandler ki;

	/**
	 * Default constructor.
	 */
	public Game() {
		// data structures to hold the game definition.
		definedEffects = new ArrayList<>();
		definedEOSEffects = new ArrayList<>();
		charMapping = new HashMap<>();
		terminations = new ArrayList<>();
		historicEvents = new TreeSet<>();
		timeEffects = new TreeSet<>();

		// Game attributes:
		size = new Dimension();
		is_stochastic = false;
		num_sprites = 0;
		nextSpriteID = 0;

		loadDefaultConstr();
	}

	/**
	 * Loads the constructor information for default objects (walls, avatar).
	 */
	void loadDefaultConstr() {
		// If more elements are added here, initSprites() must be modified accordingly!
		VGDLRegistry.GetInstance().registerSprite("wall");
		VGDLRegistry.GetInstance().registerSprite("avatar");
	}

	/**
	 * Initialisation after the game is parsed.
	 */
	public void initMulti() {
		avatars = new MovingAvatar[no_players];
		avatarLastAction = new Types.ACTIONS[no_players];
		for (int i = 0; i < no_players; i++)
			avatarLastAction[i] = Types.ACTIONS.ACTION_NIL;

		counter = new int[no_counters];
		humanPlayer = new boolean[no_players];
	}

	/**
	 * Modify the sprite order for the renderer of the GVG-AI
	 * @param spOrder	the request order
	 */
	public void changeSpriteOrder(ArrayList<Integer> spOrder){
		spriteOrder = new int[spOrder.size()];
		// We need here the default 2 sprites:
		avatarId = VGDLRegistry.GetInstance().getRegisteredSpriteValue("avatar");
		wallId = VGDLRegistry.GetInstance().getRegisteredSpriteValue("wall");

		// 1. "avatar" ALWAYS at the end of the array.
		for (int i = 0; i < no_players; i++) {
			spriteOrder[spriteOrder.length - 1 - i] = avatarId;
		}
		// 2. Other sprite types are sorted using spOrder
		int i = 0;
		for (Integer intId : spOrder) {
			if (intId != avatarId) {
				spriteOrder[i++] = intId;
			}
		}
	}

	/**
	 * Initializes the sprite structures that hold the game.
	 *
	 * @param spOrder
	 *            order of sprite types to be drawn on the screen.
	 * @param sings
	 *            sprites that are marked as singletons.
	 * @param constructors
	 *            map of sprite constructor's information.
	 */
	public void initSprites(ArrayList<Integer> spOrder, ArrayList<Integer> sings,
							HashMap<Integer, SpriteContent> constructors) {
		ArrayList<Resource> resources = new ArrayList<>();

		// We need here the default 2 sprites:
		avatarId = VGDLRegistry.GetInstance().getRegisteredSpriteValue("avatar");
		wallId = VGDLRegistry.GetInstance().getRegisteredSpriteValue("wall");

		// Initialize the sprite render order.
		this.changeSpriteOrder(spOrder);

		// Singletons
		singletons = new boolean[VGDLRegistry.GetInstance().numSpriteTypes()];
		for (Integer intId : sings) {
			singletons[intId] = true;
		}

		// Constructors, as many as number of sprite types, so they are accessed by its id:
		classConst = new Content[VGDLRegistry.GetInstance().numSpriteTypes()];
		templateSprites = new VGDLSprite[classConst.length];

		// By default, we have 2 constructors:
		SpriteContent wallConst = new SpriteContent("wall", "Immovable");
		wallConst.parameters.put("color", "DARKGRAY");
		wallConst.parameters.put("solid", "True");
		wallConst.itypes.add(wallId);
		classConst[wallId] = wallConst;

		SpriteContent avatarConst = new SpriteContent("avatar", "MovingAvatar");
		avatarConst.itypes.add(avatarId);
		classConst[avatarId] = avatarConst;

		// Now, the other constructors.
		Set<Map.Entry<Integer, SpriteContent>> entries = constructors.entrySet();
		for (Map.Entry<Integer, SpriteContent> entry : entries) {
			classConst[entry.getKey()] = entry.getValue();

			// Special case: we create a dummy Resource sprite of each resource type.
			String refClass = entry.getValue().referenceClass;
			if (refClass != null && refClass.equals("Resource")) {
				VGDLSprite resourceTest = VGDLFactory.GetInstance().createSprite(this, entry.getValue(),
						new Vector2d(0, 0), new Dimension(1, 1));
				resources.add((Resource) resourceTest);
			}
		}

		// Structures to hold game sprites, as many as number of sprite types, so they are accessed by its id:
		spriteGroups = new SpriteGroup[classConst.length];
		shieldedEffects = new ArrayList[classConst.length];
		collisionEffects = new ArrayList[classConst.length][classConst.length];
		eosEffects = new ArrayList[classConst.length];
		iSubTypes = new ArrayList[classConst.length];
		bucketList = new Bucket[classConst.length];
		resources_limits = new int[classConst.length];
		resources_colors = new Color[classConst.length];

		// For each sprite type...
		for (int j = 0; j < spriteGroups.length; ++j) {
			// Create the space for the sprites and effects of this type.
			spriteGroups[j] = new SpriteGroup(j);
			shieldedEffects[j] = new ArrayList<>();
			eosEffects[j] = new ArrayList<>();
			timeEffects = new TreeSet<>();
			bucketList[j] = new Bucket();

			// Declare the extended types list of this sprite type.
			iSubTypes[j] = (ArrayList<Integer>) ((SpriteContent) classConst[j]).subtypes.clone();

			for (int k = 0; k < spriteGroups.length; ++k) {
				// Create the array list of collision effects for each pair of sprite types.
				collisionEffects[j][k] = new ArrayList<>();
			}

		}

		// Add walls and avatars to the subtypes list.
		if (!iSubTypes[wallId].contains(wallId))
			iSubTypes[wallId].add(wallId);

		if (!iSubTypes[avatarId].contains(avatarId))
			iSubTypes[avatarId].add(avatarId);

		// Resources: use the list of resources created before to store limit and color of each resource.
		for (Resource r : resources) {
			resources_limits[r.resource_type] = r.limit;
			resources_colors[r.resource_type] = r.color;
		}
	}

	/**
	 * Check if the current itype has no children nodes
	 *
	 * @param itype
	 *            sprite index
	 * @return true if its lead node, false otherwise
	 */
	private boolean isLeafNode(int itype) {
		SpriteContent sc = (SpriteContent) classConst[itype];

		return sc.subtypes.size() <= 1 || sc.subtypes.get(sc.subtypes.size() - 1) == itype;
	}

	/**
	 * Get all parent sprites for a certain sprite
	 *
	 * @param itype
	 *            id for the current node
	 * @return a list of all parent nodes' ids
	 */
	private ArrayList<Integer> parentNodes(int itype) {
		SpriteContent sc = (SpriteContent) classConst[itype];

		ArrayList<Integer> parents = new ArrayList<>(sc.itypes);
		parents.remove(parents.size() - 1);

		return parents;
	}

	/**
	 * Expand a non leaf node using its children
	 *
	 * @param itype sprite index
	 * @return a list of all leaf children under the hierarchy of itype sprite
	 */
	private ArrayList<String> expandNonLeafNode(int itype) {
		ArrayList<String> result = new ArrayList<>();
		boolean[] visited = new boolean[classConst.length];
		ArrayList<Integer> queue = new ArrayList<>();
		queue.add(itype);

		while (!queue.isEmpty()) {
			int current = queue.remove(0);
			if (visited[current]) {
				continue;
			}

			if (isLeafNode(current)) {
				result.add(VGDLRegistry.GetInstance().getRegisteredSpriteKey(current));
			} else {
				SpriteContent sc = (SpriteContent) classConst[current];
				for(int s:sc.subtypes){
				    if(!sc.itypes.contains(s)){
					queue.add(s);
				    }
				}
			}
			visited[current] = true;
		}

		return result;
	}

	/**
	 * Method used to access the number of players in a game.
	 *
	 * @return number of players.
	 */
	public int getNoPlayers() {
		return no_players;
	}

	public int getNoCounters() {
		return no_counters;
	}

	public int getValueCounter(int idx) {
		return counter[idx];
	}

	/**
	 * return sprite type of certain sprite
	 *
	 * @param sp
	 *            sprite object
	 * @return sprite type (avatar, resource, portal, npc, static, moving)
	 */
	private int getSpriteCategory(VGDLSprite sp) {
		if (sp.is_avatar)
			return Types.TYPE_AVATAR;

		// Is it a resource?
		if (sp.is_resource)
			return Types.TYPE_RESOURCE;

		// Is it a portal?
		if (sp.portal)
			return Types.TYPE_PORTAL;

		// Is it npc?
		if (sp.is_npc)
			return Types.TYPE_NPC;

		// Is it immovable?
		if (sp.is_static)
			return Types.TYPE_STATIC;

		// is it created by the avatar?
		if (sp.is_from_avatar)
			return Types.TYPE_FROMAVATAR;

		return Types.TYPE_MOVABLE;
	}

	/**
	 * Convert a sprite content object to Sprite Data object
	 *
	 * @param sc
	 *            sprite content object for a certain sprite
	 * @return sprite data object for the current sprite content
	 */
	private SpriteData initializeSpriteData(SpriteContent sc) {
		SpriteData data = new SpriteData(sc.parameters);
		data.name = sc.identifier;
		data.type = sc.referenceClass;
		for(int pIndex:sc.itypes){
		    if( VGDLRegistry.GetInstance().getRegisteredSpriteValue(data.name) != pIndex){
			data.parents.add(VGDLRegistry.GetInstance().getRegisteredSpriteKey(pIndex));
		    }
		}

		VGDLSprite sprite = VGDLFactory.GetInstance().createSprite(this, sc, new Vector2d(),
				new Dimension(1, 1));
		switch (getSpriteCategory(sprite)) {
			case Types.TYPE_NPC:
				data.isNPC = true;
				break;
			case Types.TYPE_AVATAR:
				data.isAvatar = true;
				break;
			case Types.TYPE_PORTAL:
				data.isPortal = true;
				break;
			case Types.TYPE_RESOURCE:
				data.isResource = true;
				break;
			case Types.TYPE_STATIC:
				data.isStatic = true;
				break;
		}

		ArrayList<String> dependentSprites = sprite.getDependentSprites();
		for (String s : dependentSprites) {
			ArrayList<String> expandedSprites = expandNonLeafNode(
					VGDLRegistry.GetInstance().getRegisteredSpriteValue(s));
			data.sprites.addAll(expandedSprites);
		}

		return data;
	}

	/**
	 * Get an array of sprite data objects for all leaf sprite nodes.
	 *
	 * @return Array of sprite data
	 */
	ArrayList<SpriteData> getSpriteData() {
		ArrayList<SpriteData> result = new ArrayList<>();

		for (int i = 0; i < classConst.length; i++) {
			SpriteContent sc = (SpriteContent) classConst[i];
			if (isLeafNode(i)) {
				result.add(initializeSpriteData(sc));
			}
		}

		return result;
	}

	/**
	 * Construct and return a temporary avatar sprite
	 *
	 * @return a temproary avatar sprite
	 */
	VGDLSprite getTempAvatar(SpriteData sprite) {
		avatarId = VGDLRegistry.GetInstance().getRegisteredSpriteValue(sprite.name);
		if (((SpriteContent) classConst[avatarId]).referenceClass != null) {
			VGDLSprite result = VGDLFactory.GetInstance().createSprite(this, (SpriteContent) classConst[avatarId],
					new Vector2d(), new Dimension(1, 1));
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	/**
	 * Return an array of termination data objects. These objects represents the
	 * termination conditions for the game
	 *
	 * @return array of Termination Data objects
	 */
	ArrayList<TerminationData> getTerminationData() {
		ArrayList<TerminationData> result = new ArrayList<>();

		TerminationData td;
		for (Termination tr : terminations) {
			td = new TerminationData();
			int lastDot = tr.getClass().getName().lastIndexOf('.');
			td.type = tr.getClass().getName().substring(lastDot + 1);
			td.limit = tr.limit;
			td.win = tr.win;

			ArrayList<String> sprites = tr.getTerminationSprites();
			for (String s : sprites) {
				int itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(s);
				if (isLeafNode(itype)) {
					td.sprites.add(s);
				} else {
					td.sprites.addAll(expandNonLeafNode(itype));
				}
			}

			result.add(td);
		}

		return result;
	}

	/**
	 * Get a list of interaction data objects between two sprite types. These
	 * objects represents the effect happened to the first sprite type.
	 *
	 * @param itype1 The first sprite type object
	 * @param itype2 The second sprite type object
	 * @return array of interaction data objects.
	 */
	ArrayList<InteractionData> getInteractionData(int itype1, int itype2) {
		ArrayList<InteractionData> results = new ArrayList<>();

		ArrayList<Integer> parent1 = new ArrayList<>();
		ArrayList<Integer> parent2 = new ArrayList<>();

		if (itype1 != -1) {
			parent1.addAll(parentNodes(itype1));
			parent1.add(itype1);
		}

		if (itype2 != -1) {
			parent2.addAll(parentNodes(itype2));
			parent2.add(itype2);
		}

		ArrayList<Effect> effects = new ArrayList<>();
		if (parent1.size() > 0 && parent2.size() > 0) {
			for (int p1 : parent1) {
				for (int p2 : parent2) {
					effects.addAll(getCollisionEffects(p1, p2));
				}
			}
		} else if (parent1.size() > 0) {
			for (int p1 : parent1) {
				effects.addAll(getEosEffects(p1));

			}
		} else if (parent2.size() > 0) {
			for (int p2 : parent2) {
				effects.addAll(getEosEffects(p2));
			}
		}

		InteractionData temp;
		for (Effect e : effects) {
			temp = new InteractionData();
			temp.type = e.getClass().getName();
			temp.type = temp.type.substring(temp.type.lastIndexOf('.') + 1);
			temp.scoreChange = e.scoreChange;
			temp.sprites.addAll(e.getEffectSprites());

			results.add(temp);
		}

		return results;
	}

	/**
	 * Sets the game back to the state prior to load a level.
	 */
	public void reset() {
		num_sprites = 0;

		for (int i = 0; i < no_players; i++) {
			avatars[i] = null;
		}
		for (int i = 0; i < no_counters; i++) {
			counter[i] = 0;
		}
		isEnded = false;
		gameTick = -1;
		avatarLastAction = new Types.ACTIONS[no_players];
		for (int i = 0; i < no_players; i++)
			avatarLastAction[i] = Types.ACTIONS.ACTION_NIL;

		// For each sprite type...
		for (SpriteGroup spriteGroup : spriteGroups) {
			// Create the space for the sprites and effects of this type.
			spriteGroup.clear();
		}

		if (kill_list != null) {
			kill_list.clear();
		}
		for (int j = 0; j < spriteGroups.length; ++j) {
			bucketList[j].clear();
		}

		for (int i = 0; i < templateSprites.length; ++i) {
			templateSprites[i] = null;
		}

		historicEvents.clear();

		resetShieldEffects();
	}

	/**
	 * Starts the forward model for the game.
	 */
	void initForwardModel() {
		fwdModel = new ForwardModel(this, 0);
		fwdModel.update(this);
	}

	/**
	 * Reads the parameters of a game type.
	 *
	 * @param content list of parameter-value pairs.
	 */
	protected void parseParameters(GameContent content) {
		VGDLFactory factory = VGDLFactory.GetInstance();
		Class refClass = VGDLFactory.registeredGames.get(content.referenceClass);
		// System.out.inn("refClass" + refClass.toString());
		if (!this.getClass().equals(refClass)) {
			System.out.println("Error: Game subclass instance not the same as content.referenceClass" + " "
					+ this.getClass() + " " + refClass);
			return;
		}

		factory.parseParameters(content, this);

		// taking care of the key handler parameter:

		if (key_handler != null && key_handler.equalsIgnoreCase("Pulse"))
			CompetitionParameters.KEY_HANDLER = CompetitionParameters.KEY_PULSE;

		ki = CompetitionParameters.KEY_HANDLER == CompetitionParameters.KEY_INPUT ? new KeyInput()
				: new KeyPulse(no_players);
	}

	/**
	 * Adds a new sprite to the pool of sprites of the game. Increments the
	 * sprite counter and also modifies is_stochastic and the avatar
	 * accordingly.
	 *
	 * @param sprite the new sprite to add.
	 * @param itype main int type of this sprite (leaf of the hierarchy of types).
	 */
	protected void addSprite(VGDLSprite sprite, int itype) {
		sprite.spriteID = nextSpriteID;
		nextSpriteID++;
		spriteGroups[itype].addSprite(sprite);
		num_sprites++;

		if (sprite.is_stochastic)
			this.is_stochastic = true;
	}

	/**
	 * Returns the number of sprites of the type given by parameter, and all its
	 * subtypes
	 *
	 * @param itype parent itype requested.
	 * @return the number of sprites of the type and subtypes.
	 */
	public int getNumSprites(int itype) {
		int acum = 0;
		for (Integer subtype : this.iSubTypes[itype]) {
			acum += spriteGroups[subtype].numSprites();
		}
		return acum;
	}

	/**
	 * Returns an arraylist of subtypes of the given parent type.
	 *
	 * @param itype parent itype requested.
	 */
	public ArrayList<Integer> getSubTypes(int itype) {
		return this.iSubTypes[itype];
	}

	/**
	 * Returns the number of sprites disabled of the type given by parameter and
	 * all its subtypes
	 *
	 * @param itype parent itype requested.
	 * @return the number of disabled sprites of the type and subtypes.
	 */
	public int getNumDisabledSprites(int itype) {
		int acum = 0;
		for (Integer subtype : this.iSubTypes[itype]) {
			acum += spriteGroups[subtype].numDisabledSprites();
		}
		return acum;
	}

	/**
	 * Runs a game, without graphics.
	 *
	 * @param players Players that play this game.
	 * @param randomSeed sampleRandom seed for the whole game.
	 * @return the score of the game played.
	 */
	public double[] runGame(Player[] players, int randomSeed) {
		// Prepare some structures and references for this game.
		prepareGame(players, randomSeed);

		// Play until the game is ended
		while (!isEnded) {
			this.gameCycle(); // Execute a game cycle.
		}

		// Update the forward model for the game state sent to the controller.
		fwdModel.update(this);

		return handleResult();
	}

	/**
	 * Plays the game, graphics enabled.
	 *
	 * @param players Players that play this game.
	 * @param randomSeed sampleRandom seed for the whole game.
	 * @param isHuman indicates if a human is playing the game.
	 * @param humanID ID of the human player
	 * @return the score of the game played.
	 */

	public double[] playGame(Player[] players, int randomSeed, boolean isHuman, int humanID) {
		// Prepare some structures and references for this game.
		prepareGame(players, randomSeed);
		// Create and initialize the panel for the graphics.
		VGDLViewer view = new VGDLViewer(this, players[humanID]);
		JEasyFrame frame = new JEasyFrame(view, "Java-VGDL");
		return playGame(frame, view, players, isHuman, humanID);
	}

	public double[] playAudioGame(Player[] players, int randomSeed, boolean isHuman, int humanID) {
		// Prepare some structures and references for this game.
		prepareGame(players, randomSeed);
		// Create and initialize the panel for the graphics.
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(300, 150));
		JEasyFrame frame = new JEasyFrame(panel, "Java-VGDL");
		return playGame(frame, null, players, isHuman, humanID);
	}

	private double[] playGame(JEasyFrame frame, VGDLViewer view, Player[] players, boolean isHuman, int humanID) {
		frame.addKeyListener(ki);
		frame.addWindowListener(wi);
		wi.windowClosed = false;

		// Determine the delay for playing with a good fps.
		double delay = CompetitionParameters.LONG_DELAY;
		for (Player player : players)
			if (player instanceof tracks.singlePlayer.tools.human.Agent) {
				delay = 1000.0 / CompetitionParameters.DELAY; // in milliseconds
				break;
			}

		boolean firstRun = true;

		// Play until the game is ended
		while (!isEnded && !wi.windowClosed) {
			// Determine the time to adjust framerate.
			long then = System.currentTimeMillis();

			this.gameCycle(); // Execute a game cycle.

			// Get the remaining time to keep fps.
			long now = System.currentTimeMillis();
			int remaining = (int) Math.max(0, delay - (now - then));

			// Wait until de next cycle.
			waitStep(remaining);

			// Draw all sprites in the panel.
			if (view != null) {
				view.paint(this.spriteGroups);
			}

			// Update the frame title to reflect current score and tick.
			this.setTitle(frame);

			if (firstRun && isHuman) {
				if (CompetitionParameters.dialogBoxOnStartAndEnd) {
					JOptionPane.showMessageDialog(frame, "Click OK to start.");
				}

				firstRun = false;
			}
		}

		if (isHuman && !wi.windowClosed && CompetitionParameters.killWindowOnEnd) {
			if (CompetitionParameters.dialogBoxOnStartAndEnd) {
				if (no_players == 1) {
					String sb = "GAMEOVER: YOU LOSE.";
					if (avatars[humanID] != null) {
						sb = "GAMEOVER: YOU "
								+ ((avatars[humanID].getWinState() == Types.WINNER.PLAYER_WINS) ? "WIN." : "LOSE.");
					}
					JOptionPane.showMessageDialog(frame, sb);
				} else {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < no_players; i++) {
						if (avatars[i] != null && avatars[i].getWinState() == Types.WINNER.PLAYER_WINS) {
							sb.append("Player ").append(i).append("; ");
						}
					}
					if (sb.toString().equals(""))
						sb = new StringBuilder("NONE");
					JOptionPane.showMessageDialog(frame, "GAMEOVER - WINNER: " + sb);
				}
			}
			frame.dispose();
		}

		// Update the forward model for the game state sent to the controller.
		fwdModel.update(this);

		return handleResult();
	}

	public double[] playOnlineGame(Player[] players, int randomSeed, boolean isHuman, int humanID) {
		// Prepare some structures and references for this game.
		prepareGame(players, randomSeed);

		// Create and initialize the panel for the graphics.
		VGDLViewer view = new VGDLViewer(this, players[humanID]);
		view.justImage = true;
		wi.windowClosed = false;

		// Determine the delay for playing with a good fps.
		double delay = CompetitionParameters.LONG_DELAY;
		for (Player player : players)
			if (player instanceof tracks.singlePlayer.tools.human.Agent) {
				delay = 1000.0 / CompetitionParameters.DELAY; // in milliseconds
				break;
			}

		boolean firstRun = true;

		// Play until the game is ended
		while (!isEnded && !wi.windowClosed) {
			// Determine the time to adjust framerate.
			long then = System.currentTimeMillis();

			this.gameCycle(); // Execute a game cycle.

			// Get the remaining time to keep fps.
			long now = System.currentTimeMillis();
			int remaining = (int) Math.max(0, delay - (now - then));

			// Wait until de next cycle.
			waitStep(remaining);

			// Draw all sprites in the panel.
			view.paint(this.spriteGroups);

			if (firstRun && isHuman) {
				firstRun = false;
			}
		}


		// Update the forward model for the game state sent to the controller.
		fwdModel.update(this);

		return handleResult();
	}

	/**
	 * Sets the title of the game screen, depending on the game ending state.
	 *
	 * @param frame The frame whose title needs to be set.
	 */
	private void setTitle(JEasyFrame frame) {
		StringBuilder sb = new StringBuilder();
		sb.append("Java-VGDL: ");
		for (int i = 0; i < no_players; i++) {
			if (avatars[i] != null) {
				sb.append("Player").append(i).append("-Score:").append(avatars[i].getScore()).append(". ");
			}
		}
		sb.append("Tick:").append(this.getGameTick());

		// sb += " --Counter:";
		// for (int i = 0; i < no_counters; i++) {
		// sb += counter[i] + ", ";
		// }

		if (!isEnded)
			frame.setTitle(sb.toString());
		else {
			for (int i = 0; i < no_players; i++) {
				if (avatars[i] != null && avatars[i].getWinState() == Types.WINNER.PLAYER_WINS)
					sb.append(" [Player ").append(i).append(" WINS!]");
				else
					sb.append(" [Player ").append(i).append(" LOSES!]");
			}
		}

		frame.setTitle(sb.toString());

	}

	/**
	 * Initializes some variables for the game to be played, such as the game
	 * tick, sampleRandom number generator, forward model and assigns the player
	 * to the avatar.
	 *
	 * @param players Players that play this game.
	 * @param randomSeed sampleRandom seed for the whole game.
	 */
	private void prepareGame(Player[] players, int randomSeed) {
		// Start tick counter.
		gameTick = -1;

		// Create the sampleRandom generator.
		random = new Random(randomSeed);

		// Assigns the player to the avatar of the game.
		createAvatars();
		assignPlayer(players);

		// Initialize state observation (sets all non-volatile references).
		initForwardModel();
	}

	/**
	 * This is a standard game cycle in J-VGDL. It advances the game tick,
	 * updates the forward model and rolls an action in all entities, handling
	 * collisions and end game situations.
	 */
	private void gameCycle() {
		gameTick++; // next game tick.

		// Update our state observation (forward model) with the information of the current game state.
		fwdModel.update(this);
		// System.out.println(avatars[0].rect);

		// Execute a game cycle:
		this.tick(); // update for all entities.
		this.eventHandling(); // handle events such collisions.
		this.clearAll(fwdModel); // clear all additional data, including dead sprites.
		this.terminationHandling(); // check for game termination.
		this.checkTimeOut(); // Check for end of game by time steps.

		// if(gameTick == 0 || isEnded)
		// fwdModel.printObservationGrid(); //uncomment this to show the observation grid.
	}

	/**
	 * Handles the result for the game, considering disqualifications. Prints
	 * the result (score, time and winner) and returns the score of the game.
	 * Default player ID used 0 for single player games.
	 *
	 * @return the result of the game.
	 */
	public double[] handleResult() {
		// check all players disqualified and set scores
		for (MovingAvatar avatar : avatars) {
			if (avatar != null) {
				if (avatar.is_disqualified()) {
					avatar.setWinState(Types.WINNER.PLAYER_DISQ);
					avatar.setScore(Types.SCORE_DISQ);
				}
				// For sanity: winning a game always gives a positive score
				else if (avatar.getWinState() == Types.WINNER.PLAYER_WINS)
					if (avatar.getScore() <= 0)
						avatar.setScore(1);
			}
		}

		// Prints the result: score, time and winner.
		// printResult();

		double[] scores = new double[no_players];
		for (int i = 0; i < no_players; i++) {
			if (avatars[i] == null) {
				scores[i] = Types.SCORE_DISQ;
			} else {
				scores[i] = avatars[i].getScore();
			}
		}

		return scores;
	}

	/**
	 * Checks if the game must finish because of number of cycles played. This
	 * is a value stored in CompetitionParameters.MAX_TIMESTEPS. If the game is
	 * due to end, the winner is determined and the flag isEnded is set to true.
	 */
	void checkTimeOut() {
		if (gameTick >= CompetitionParameters.MAX_TIMESTEPS) {
			isEnded = true;
			for (int i = 0; i < no_players; i++) {
				if (avatars[i].getWinState() != Types.WINNER.PLAYER_WINS)
					avatars[i].setWinState(Types.WINNER.PLAYER_LOSES);
			}
		}
	}

	/**
	 * Prints the result of the game, indicating the winner, the score and the
	 * number of game ticks played, in this order.
	 */
	public void printResult() {
		Pair sb = getResultPrint();
		System.out.println("Result (1->win; 0->lose): " + sb.first + sb.second + "timesteps:" + this.getGameTick());
	}

	/**
	 * Prints the result of the game, indicating the game id, level id, winner, the score and the
	 * number of game ticks played, in this order.
	 */
	public void printLearningResult(int levelIdx, boolean isValidation) {
		Pair sb = getResultPrint();
		String s = " Result (1->win; 0->lose): level:" + levelIdx + ", " + sb.first + sb.second + "timesteps:" + this.getGameTick();
		if (isValidation) {
			System.out.println("[VALIDATION]" + s);
		} else {
			System.out.println("[TRAINING]" + s);
		}
	}

	private Pair getResultPrint() {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < no_players; i++) {
			if (avatars[i] != null) {
				sb1.append("Player").append(i).append(":").append(avatars[i].getWinState().key()).append(", ");
				sb2.append("Player").append(i).append("-Score:").append(avatars[i].getScore()).append(", ");
			} else {
				sb1.append("Player").append(i).append(":-100, ");
				sb2.append("Player").append(i).append("-Score:").append(Types.SCORE_DISQ).append(", ");
			}
		}
		return new Pair(sb1, sb2);
	}

	/**
	 * Returns the complete result of the game (victory, score, timestep).
	 * Indicated in triplets, one per player.
	 *
	 * @return [w0,s0,t0,w1,s1,t1,...]
	 */
	public double[] getFullResult() {
		int result_dims = 3;
		double[] allRes = new double[no_players * result_dims];
		for (int i = 0; i < no_players; i++) {

			allRes[i * result_dims] = avatars[i].getWinState().key();
			allRes[i * result_dims + 1] = avatars[i].getScore();
			allRes[i * result_dims + 2] = this.getGameTick();
		}
		return allRes;
	}

	/**
	 * Disqualifies the player in the game, and also sets the isEnded flag to true.
	 */
	// Comment this method out to check if mistakes in method overloading anywhere
	public void disqualify() {
		isEnded = true;
	}

	/**
	 * Overloaded method for multiplayer games. Same functionality as above.
	 *
	 * @param id - id of the player that was disqualified
	 */
	public void disqualify(int id) {
		if (id >= 0)
			avatars[id].disqualify(true);
		isEnded = true;
	}


	/**
	 * Aborts a game. Game is lost and game over.
	 */
	public void abort() {
		isEnded = true;
	}



	/**
	 * Method to create the array of avatars from the sprites.
	 */
	void createAvatars() {

		// Avatars will usually be the first elements, starting from the end.

		// Find avatar sprites
		ArrayList<MovingAvatar> avSprites = new ArrayList<>();
		int idx = spriteOrder.length;
		int numAvatarSprites;
		while (true) {
			idx--;
			if (idx > 0) {
				int spriteTypeId = spriteOrder[idx];
				int num = spriteGroups[spriteTypeId].numSprites();
				if (num > 0) {
					// There should be just one sprite in the avatar's group in single player games.
					// Could be more than one avatar in multiplayer games
					for (int j = 0; j < num; j++) {
						VGDLSprite thisSprite = spriteGroups[spriteTypeId].getSpriteByIdx(j);
						if (thisSprite.is_avatar) {
							avSprites.add((MovingAvatar) thisSprite);
						}
					}
				}
			} else {
				numAvatarSprites = avSprites.size();
				// System.out.println("Done finding avatars: " + numAvatarSprites);
				break;
			}
		}

		Collections.reverse(avSprites); // Read in reverse order
		if (!avSprites.isEmpty()) {
			for (int i = 0; i < no_players; i++) {
				if (numAvatarSprites > i) { // Check if there's enough avatars just in case
					avatars[i] = avSprites.get(i);
					avatars[i].setKeyHandler(ki);
					avatars[i].setPlayerID(i);
				}
			}
		} else {
			Logger.getInstance().addMessage(new Message(Message.WARNING, "No avatars found."));
		}
	}

	/**
	 * Looks for the avatar of the game in the existing sprites. If the player
	 * received as a parameter is not null, it is assigned to it.
	 *
	 * @param players the players that will play the game (only 1 in single player games).
	 */
	private void assignPlayer(Player[] players) {
		// Iterate through all avatars and assign their players
		if (players.length == no_players) {
			for (int i = 0; i < no_players; i++) {
				if (players[i] != null && avatars[i] != null) {
					avatars[i].player = players[i];
					avatars[i].setPlayerID(i);
				} else {
					System.out.println("Null player.");
				}
			}
		} else {
			System.out.println("Not enough players.");
		}
	}

	/**
	 * Holds the game for the specified duration milliseconds
	 *
	 * @param duration time to wait.
	 */
	private void waitStep(int duration) {

		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs one tick for the game: calling update(this) in all sprites. It
	 * follows the opposite order of the drawing order (inverse spriteOrder[]).
	 * Avatar is always updated first. Doesn't update disabled sprites.
	 */
	protected void tick() {
		// Now, do all of the avatars.
		for (int i = 0; i < no_players; i++) {
			if (avatars[i] != null && !avatars[i].is_disabled()) {
				avatars[i].preMovement();
				avatars[i].updateAvatar(this, true, null);
			} else if (avatars[i] == null) {
				System.out.println(gameTick + ": Something went wrong, no avatar, ID = " + i);
			}
		}
		// random = new Random(this.gameTick * 100); //uncomment this for testing a new rnd generator after avatar move

		int spriteOrderCount = spriteOrder.length;
		for (int i = spriteOrderCount - 1; i >= 0; --i) {
			int spriteTypeInt = spriteOrder[i];
			ArrayList<VGDLSprite> spritesList = spriteGroups[spriteTypeInt].getSprites();
			if (spritesList != null)
				for (VGDLSprite sp : spritesList) {
					if (!(sp instanceof MovingAvatar) && !sp.is_disabled()) {
						sp.preMovement();
						sp.update(this);
					}
				}

		}
	}

	/**
	 * Handles collisions and triggers events.
	 */
	void eventHandling() {
		// Array to indicate that the sprite type has no representative in collisions.

		// First, check the effects that are triggered in a timely manner.
		while (timeEffects.size() > 0 && timeEffects.first().nextExecution <= gameTick) {
			TimeEffect ef = timeEffects.pollFirst();
			assert ef != null;
			if (ef.enabled) {
				int intId = ef.itype;
				boolean exec = false;

				// if intId==-1, we have no sprite
				if (intId == -1) {
					// With no sprite, the effect is independent from particular sprites.
					ef.execute(null, null, this);
					exec = true;

					// Affect score for all players:
					if (ef.applyScore) {
						for (int i = 0; i < no_players; i++) {
							avatars[i].addScore(ef.getScoreChange(i));
						}
					}

				} else {

					ArrayList<Integer> allTypes = iSubTypes[intId];
					for (Integer itype : allTypes) {
						// Find all sprites of this subtype.
						Collection<VGDLSprite> sprites = this.getSprites(itype);
						for (VGDLSprite sp : sprites) {
							// Check that they are not dead (could happen in this same cycle).
							if (!kill_list.contains(sp) && !sp.is_disabled()) {
								executeEffect(ef, sp, null);
								exec = true;
							}
						}
					}
				}

				// If the time effect is repetitive, need to reinsert in the list of effects
				if (ef.repeating) {
					if (!exec)
						ef.planExecution(this);
					this.addTimeEffect(ef);
				}
			}

		}

		// Secondly, we handle single sprite events (EOS). Take each sprite itype that has a EOS effect defined.
		for (Integer intId : definedEOSEffects) {
			// For each effect that this sprite has assigned.
			for (Effect ef : eosEffects[intId]) {
				// Take all the subtypes in the hierarchy of this sprite.
				ArrayList<Integer> allTypes = iSubTypes[intId];
				if (ef.enabled)
					for (Integer itype : allTypes) {
						// Add all sprites of this subtype to the list of sprites.
						// These are sprites that could potentially collide with EOS
						Collection<VGDLSprite> sprites = this.getSprites(itype);
						try{
							for (VGDLSprite sp : sprites) {
								// Check if they are at the edge to trigger the effect. Also check that they
								// are not dead (could happen in this same cycle).
								if (isAtEdge(sp.rect) && !kill_list.contains(sp) && !sp.is_disabled()) {
									executeEffect(ef, sp, null);
								}
							}
						}
						catch(ConcurrentModificationException e){
							Logger.getInstance().addMessage(new Message(Message.WARNING,
									"you can't spawn sprites outside of the screen."));
						}
					}
			}

		}

		// Now, we handle events between pairs of sprites, for each pair of sprites that have a paired effect defined:
		for (Pair<Integer, Integer> p : definedEffects) {
			// We iterate over the (potential) multiple effects that these
			// two sprites could have defined between them.
			for (Effect ef : collisionEffects[p.first][p.second]) {
				if (ef.enabled) {

					if (shieldedEffects[p.first].size() > 0) {
						if (shieldedEffects[p.first].contains(new Pair(p.second, ef.hashCode)))
							continue;
					}

					ArrayList<VGDLSprite> firstx = new ArrayList<>();
					ArrayList<VGDLSprite> secondx = new ArrayList<>();

					ArrayList<Integer> allTypes1 = iSubTypes[p.first];
					for (int i : allTypes1) {
						firstx.addAll(getSprites(i));
					}
					ArrayList<Integer> allTypes2 = iSubTypes[p.second];
					for (int j : allTypes2) {
						secondx.addAll(getSprites(j));
					}

					ArrayList<VGDLSprite> new_secondx;

					for (VGDLSprite s1 : firstx) {
						new_secondx = new ArrayList<>();

						for (VGDLSprite s2 : secondx) {
							if ((s1 != s2 && s1.intersects(s2))) {
								new_secondx.add(s2);
							}
						}

						if(new_secondx.size() > 0) {
							if (ef.inBatch) {
								executeEffectBatch(ef, s1, new_secondx);
							} else {

								for (VGDLSprite new_secondx1 : new_secondx) {
									if (!kill_list.contains(s1) && s1 != new_secondx1 && s1.intersects(new_secondx1)) {
										executeEffect(ef, s1, new_secondx1);
									}
								}
							}
						}
					}
				}
			}
		}

	}


	private void executeEffectBatch(Effect ef, VGDLSprite s1, ArrayList<VGDLSprite> s2list) {
		// There is a collision. Apply the effect.
		int batchCount = ef.executeBatch(s1, s2list, this);
		if(batchCount == -1)
		{
			System.out.println("WARNING: Batch collision not or bad implemented (batchCount == -1)");
			batchCount = 0; //So the game keeps making better sense.
		}

		// Affect score:
		if (ef.applyScore) {
			// apply scores for all avatars
			for (int i = 0; i < no_players; i++) {
				double multScore = ef.getScoreChange(i) * batchCount;
				avatars[i].addScore(multScore);
			}
		}

		// Add to events history.
		if (s1 != null && s2list != null)
			for(VGDLSprite s2 : s2list)
				addEvent(s1, s2, ef.audio);

		if (ef.count) {
			for (int i = 0; i < no_counters; i++) {
				double multCounter = ef.getCounter(i) * batchCount;
				this.counter[i] += multCounter;
			}
		}

		if (ef.countElse) {
			for (int i = 0; i < no_counters; i++) {
				double multElseCounter = ef.getCounterElse(i) * batchCount;
				this.counter[i] += multElseCounter;
			}
		}
	}

	private void executeEffect(Effect ef, VGDLSprite s1, VGDLSprite s2) {
		// There is a collision. Apply the effect.
		ef.execute(s1, s2, this);

		// Affect score:
		if (ef.applyScore) {
			// apply scores for all avatars
			for (int i = 0; i < no_players; i++) {
				avatars[i].addScore(ef.getScoreChange(i));
			}
		}

		// Add to events history.
		if (s1 != null && s2 != null)
			addEvent(s1, s2, ef.audio);

		if (ef.count) {
			for (int i = 0; i < no_counters; i++) {
				this.counter[i] += ef.getCounter(i);
			}
		}

		if (ef.countElse) {
			for (int i = 0; i < no_counters; i++) {
				this.counter[i] += ef.getCounterElse(i);
			}
		}
	}

	private void addEvent(VGDLSprite s1, VGDLSprite s2, String audioSrc) {
		if (s1.is_avatar)
			historicEvents.add(
					new Event(gameTick, false, s1.getType(), s2.getType(), s1.spriteID, s2.spriteID,
							s1.getPosition(), audioSrc));

		else if (s1.is_from_avatar)
			historicEvents.add(
					new Event(gameTick, true, s1.getType(), s2.getType(), s1.spriteID, s2.spriteID,
							s1.getPosition(), audioSrc));

		else if (s2.is_avatar)
			historicEvents.add(
					new Event(gameTick, false, s2.getType(), s1.getType(), s2.spriteID, s1.spriteID,
							s2.getPosition(), audioSrc));

		else if (s2.is_from_avatar)
			historicEvents.add(
					new Event(gameTick, true, s2.getType(), s1.getType(), s2.spriteID, s1.spriteID,
							s2.getPosition(), audioSrc));
	}

	/**
	 * Checks if a given rectangle is at the edge of the screen.
	 *
	 * @param rect the rectangle to check
	 * @return true if rect is at the edge of the screen.
	 */
	private boolean isAtEdge(Rectangle rect) {
		Rectangle r = new Rectangle(screenSize);
		return !r.contains(rect);
	}

	/**
	 * Handles termination conditions, for every termination defined in
	 * 'terminations' array.
	 */
	void terminationHandling() {
		int numTerminations = terminations.size();
		for (int i = 0; !isEnded && i < numTerminations; ++i) {
			Termination t = terminations.get(i);
			if (t.isDone(this)) {
				isEnded = true;
				for (int j = 0; j < no_players; j++) {
					if (avatars[j] != null) {
						avatars[j].setWinState(t.win(j) ? Types.WINNER.PLAYER_WINS : Types.WINNER.PLAYER_LOSES);
					}
				}
			}
		}
		if(Logger.getInstance().getMessageCount() > CompetitionParameters.MAX_ALLOWED_WARNINGS){
			System.out.println("Finishing the game due to number of warnings: " + Logger.getInstance().getMessageCount()
					+ ". Messages will be flushed.");
			Logger.getInstance().printMessages();
		    isEnded = true;
		    Logger.getInstance().flushMessages();
		}
	}

	/**
	 * Deletes all the sprites killed in the previous step. Also, clears the
	 * array of collisions from the last step.
	 *
	 * @param fm Forward model where we are cleaning sprites.
	 */
	void clearAll(ForwardModel fm) {
		for (VGDLSprite sprite : kill_list) {
			int spriteType = sprite.getType();
			this.spriteGroups[spriteType].removeSprite(sprite);
			if (fm != null) {
				fm.removeSpriteObservation(sprite);
			}

			if (sprite.is_avatar)
				// Go through all avatars to see which avatar is dead
				for (int i = 0; i < no_players; i++)
					if (sprite == avatars[i])
						avatars[i] = null;

			num_sprites--;

		}
		kill_list.clear();

		for (int j = 0; j < spriteGroups.length; ++j) {
			bucketList[j].clear();
		}

		resetShieldEffects();
	}

	/**
	 * Cleans the array of shielded effects.
	 */
	private void resetShieldEffects() {
		for (ArrayList<Pair<Integer, Long>> shieldedEffect : shieldedEffects) shieldedEffect.clear();
	}

	/**
	 * Adds a new Shield effect to the scene.
	 *
	 * @param type1 Recipient of the effect (sprite ID)
	 * @param type2 Second sprite ID
	 * @param functHash Hash of the effect name to shield.
	 */
	public void addShield(int type1, int type2, long functHash) {
		Pair newShield = new Pair(type2, functHash);
		shieldedEffects[type1].add(newShield);
	}

	/**
	 * Adds a sprite given a content and position.
	 *
	 * @param itype
	 *            integer that identifies the definition of the sprite to add
	 * @param position
	 *            where the sprite has to be placed.
	 */
	public VGDLSprite addSprite(int itype, Vector2d position) {
		return this.addSprite((SpriteContent) classConst[itype], position, itype, false);
	}

	/**
	 * Adds a sprite given a content and position.
	 *
	 * @param itype integer that identifies the definition of the sprite to add
	 * @param position where the sprite has to be placed.
	 * @param force if true, ignores the singleton restrictions and creates it anyway.
	 */
	public VGDLSprite addSprite(int itype, Vector2d position, boolean force) {
		return this.addSprite((SpriteContent) classConst[itype], position, itype, force);
	}

	/**
	 * Adds a sprite given a content and position. It checks for possible
	 * singletons.
	 *
	 * @param content definition of the sprite to add
	 * @param position where the sprite has to be placed.
	 * @param itype integer identifier of this type of sprite.
	 * @param force If true, forces the creation ignoring singleton restrictions
	 */
	public VGDLSprite addSprite(SpriteContent content, Vector2d position, int itype, boolean force) {
		if (num_sprites > MAX_SPRITES) {
			Logger.getInstance().addMessage(new Message(Message.WARNING, "Sprite limit reached."));
			return null;
		}

		// Check for singleton Sprites
		boolean anyother = false;
		if (!force) {
			for (Integer typeInt : content.itypes) {
				// If this type is a singleton and we have one already
				if (singletons[typeInt] && getNumSprites(typeInt) > 0) {
					// that's it, no more creations of this type.
				    anyother = true;
				    break;
				}
			}
		}

		// Only create the sprite if there is not any other sprite that blocks it.
		if (!anyother) {
			VGDLSprite newSprite;

			Dimension spriteDim = new Dimension(block_size, block_size);
			if (templateSprites[itype] == null) // don't have a template yet, so
			// need to create one
			{
				newSprite = VGDLFactory.GetInstance().createSprite(this, content, position, spriteDim);

				// Assign its types and add it to the collection of sprites.
				newSprite.itypes = (ArrayList<Integer>) content.itypes.clone();

				// save a copy as template object
				templateSprites[itype] = newSprite.copy();
			} else // we already have a template, so simply copy that one
			{
				newSprite = templateSprites[itype].copy();

				// make sure the copy is moved to the correct position
				newSprite.setRect(position, spriteDim);

				// Set last rect
				newSprite.lastrect = new Rectangle(newSprite.rect);
			}

			// add the sprite to the collection of sprites in the game
			this.addSprite(newSprite, itype);
			return newSprite;
		}
		
		return null;
	}

	/**
	 * Returns the game score.
	 */
	public double getScore() {
		return getScore(0);
	}

	/**
	 * Method overloaded for multi player games. Returns the game score of the specified player.
	 *
	 * @param playerID ID of the player.
	 */
	public double getScore(int playerID) {
		return avatars[playerID].getScore();
	}

	/**
	 * Reverses the direction of a given sprite.
	 *
	 * @param sprite sprite to reverse.
	 */
	public void reverseDirection(VGDLSprite sprite) {
		sprite.orientation = new Direction(-sprite.orientation.x(), -sprite.orientation.y());
	}

	/**
	 * Kills a given sprite, adding it to the list of sprites killed at this step.
	 *
	 * @param sprite the sprite to kill.
	 * @param transformed - indicates if the sprite was transformed (necessary to kill sprite even if avatar,
	 *                       instead of disabling it).
	 */
	public void killSprite(VGDLSprite sprite, boolean transformed) {
		if (sprite instanceof MovingAvatar && !transformed) { // if avatar, just
			// disable
			sprite.setDisabled(true);
		} else {
			kill_list.add(sprite);
		}
	}

	/**
	 * Gets an iterator for the collection of sprites for a particular sprite type.
	 *
	 * @param spriteItype type of the sprite to retrieve.
	 * @return sprite collection of the specified type.
	 */
	public Iterator<VGDLSprite> getSpriteGroup(int spriteItype) {
		return spriteGroups[spriteItype].getSpriteIterator();
	}

	/**
	 * Gets an iterator for the collection of sprites for a particular sprite type, AND all subtypes.
	 *
	 * @param spriteItype  type of the sprite to retrieve.
	 * @return sprite collection of the specified type and subtypes.
	 */
	public Iterator<VGDLSprite> getSubSpritesGroup(int spriteItype) {
		// Create a sprite group for all the sprites
		SpriteGroup allSprites = new SpriteGroup(spriteItype);
		// Get all the subtypes
		ArrayList<Integer> allTypes = iSubTypes[spriteItype];

		// Add sprites of this type, and all subtypes.
		allSprites.addAllSprites(this.getSprites(spriteItype));
		for (Integer itype : allTypes) {
			allSprites.addAllSprites(this.getSprites(itype));
		}

		// Return the iterator.
		return allSprites.getSpriteIterator();
	}

	/**
	 * Gets the collection of sprites for a particular sprite type.
	 *
	 * @param spriteItype type of the sprite to retrieve.
	 * @return sprite collection of the specified type.
	 */
	public ArrayList<VGDLSprite> getSprites(int spriteItype) {
		return spriteGroups[spriteItype].getSprites();
	}

	/**
	 * Gets the array of collisions defined for two types of sprites.
	 *
	 * @param spriteItype1 type of the first sprite.
	 * @param spriteItype2 type of the second sprite.
	 * @return the collection of the effects defined between the two sprite
	 *         types.
	 */
	public ArrayList<Effect> getCollisionEffects(int spriteItype1, int spriteItype2) {
		return collisionEffects[spriteItype1][spriteItype2];
	}

	/**
	 * Returns all paired effects defined in the game.
	 *
	 * @return all paired effects defined in the game.
	 */
	public ArrayList<Pair<Integer, Integer>> getDefinedEffects() {
		return definedEffects;
	}

	/**
	 * Returns the list of sprite type with at least one EOS effect defined.
	 *
	 * @return the list of sprite type with at least one EOS effect defined.
	 */
	public ArrayList<Integer> getDefinedEosEffects() {
		return definedEOSEffects;
	}

	/**
	 * Returns all EOS effects defined in the game.
	 *
	 * @return all EOS effects defined in the game.
	 */
	public ArrayList<Effect> getEosEffects(int obj1) {
		return eosEffects[obj1];
	}

	/**
	 * Adds a time effect to the game.
	 */
	public void addTimeEffect(TimeEffect ef) {
		timeEffects.add(ef);
	}

	/**
	 * Returns the char mapping of this array, that relates characters in the
	 * level with sprite names that it references.
	 *
	 * @return the char mapping of this array. For each character, there is a
	 *         list of N sprite names.
	 */
	public HashMap<Character, ArrayList<String>> getCharMapping() {
		return charMapping;
	}

	/**
	 * Set the char mapping that is used to parse loaded levels
	 *
	 * @param charMapping new character mapping
	 */
	public void setCharMapping(HashMap<Character, ArrayList<String>> charMapping) {
		this.charMapping = charMapping;
	}

	/**
	 * Gets the array of termination conditions for this game.
	 *
	 * @return the array of termination conditions.
	 */
	public ArrayList<Termination> getTerminations() {
		return terminations;
	}

	/**
	 * Gets the maximum amount of resources of type resourceId that are allowed
	 * by entities in the game.
	 *
	 * @param resourceId the id of the resource to query for.
	 * @return maximum amount of resources of type resourceId.
	 */
	public int getResourceLimit(int resourceId) {
		return resources_limits[resourceId];
	}

	/**
	 * Gets the color of the resource of type resourceId
	 *
	 * @param resourceId id of the resource to query for.
	 * @return Color assigned to this resource.
	 */
	public Color getResourceColor(int resourceId) {
		return resources_colors[resourceId];
	}

	/**
	 * Gets the dimensions of the screen.
	 *
	 * @return the dimensions of the screen.
	 */
	public Dimension getScreenSize() {
		return screenSize;
	}

	/**
	 * Defines this game as stochastic (or not) depending on the parameter passed.
	 *
	 * @param stoch true if the game is stochastic.
	 */
	public void setStochastic(boolean stoch) {
		is_stochastic = stoch;
	}

	/**
	 * Returns the avatar of the game in single player games.
	 *
	 * @return the avatar of the game.
	 */
	public MovingAvatar getAvatar() {
		return getAvatar(0);
	}

	/**
	 * Overloaded method, returns the avatar of the player specified (for multi player games).
	 *
	 * @param playerID ID of the player desired.
	 * @return the corresponding avatar.
	 */
	public MovingAvatar getAvatar(int playerID) {
		return avatars[playerID];
	}

	/**
	 * Returns an array of all avatars in the game.
	 *
	 * @return array of avatars.
	 */
	public MovingAvatar[] getAvatars() {
		return avatars;
	}

	/**
	 * Sets the avatar of the game.
	 *
	 * @param newAvatar the avatar of the game.
	 */
	public void setAvatar(MovingAvatar newAvatar) {
		avatars[0] = newAvatar;
	}

	/**
	 * Overloaded method, sets the avatar specified.
	 *
	 * @param newAvatar the avatar of the game.
	 * @param playerID the ID of the player desired.
	 */
	public void setAvatar(MovingAvatar newAvatar, int playerID) {
		avatars[playerID] = newAvatar;
	}

	/**
	 * Sets the last action executed by the avatar. It could be NIL in case of time overspent.
	 *
	 * @param action the action to set.
	 */
	void setAvatarLastAction(Types.ACTIONS action) {
		setAvatarLastAction(action, 0);
	}

	/**
	 * Overloaded method for multi player games. Sets the last action executed
	 * by the avatar with the corresponding player ID. It could be NIL in case
	 * of time overspent.
	 *
	 * @param action the action to set.
	 * @param playerID the ID of the player.
	 */
	public void setAvatarLastAction(Types.ACTIONS action, int playerID) {
		this.avatarLastAction[playerID] = action;
	}

	/**
	 * Indicates if the game is over, or if it is still being played.
	 *
	 * @return true if the game is over, false if it is still being played.
	 */
	public abstract boolean isGameOver();

	/**
	 * Clear all the interactions and termination in the current game
	 */
	void clearInteractionTerminationData() {
		this.setStochastic(false);
		this.terminations.clear();

		this.definedEffects.clear();
		for (ArrayList<Effect>[] collisionEffect : this.collisionEffects) {
			for (ArrayList<Effect> effects : collisionEffect) {
				effects.clear();
			}
		}

		this.definedEOSEffects.clear();
		for (ArrayList<Effect> eosEffect : this.eosEffects) {
			eosEffect.clear();
		}

		this.timeEffects.clear();
	}

	/**
	 * Retuns the observation of this state.
	 *
	 * @return the observation.
	 */
	public StateObservation getObservation() {
		return new StateObservation(fwdModel.copy(), 0);
	}

	/**
	 * Retuns the observation of this state.
	 *
	 * @return the observation.
	 */
	public AudioStateObservation getObservationAudio() {
		return new AudioStateObservation(this, fwdModel.copy(), 0);
	}

	ArrayList<AudioObservation> getAudioObservations() {
		ArrayList<AudioObservation> obs = new ArrayList<>();

		// Sprites:
		for (SpriteGroup spriteGroup : spriteGroups) {
			for (VGDLSprite sp : spriteGroup.getSprites()) {
				if (sp.audioMove != null && !sp.audioMove.equals("") && !sp.rect.equals(sp.lastrect)) {
					obs.add(createAudioObservation(sp, sp.audioMove));
				}
				if (sp.audioUse != null && !sp.audioUse.equals("") && sp.used) {
					obs.add(createAudioObservation(sp, sp.audioUse));
					sp.used = false;
				}
				if (sp.beacon != null && !sp.beacon.equals("")) {
					obs.add(createAudioObservation(sp, sp.beacon));
				}
			}
		}

		// Events:
		for (Event historicEvent : historicEvents) {
			if (historicEvent.gameStep >= getGameTick() - 1) {
				obs.add(createAudioObservation(historicEvent));
			}
		}

		Collections.sort(obs);
		return obs;
	}

	private AudioObservation createAudioObservation(VGDLSprite sp, String audioSrc) {
		double dist = sp.getPosition().dist(fwdModel.getAvatarPosition()) / block_size;
		double intensity = 1/(dist + 1);
		return new AudioObservation(sp.spriteID, intensity, audioSrc);
	}

	private AudioObservation createAudioObservation(Event e) {
		double dist = e.position.dist(fwdModel.getAvatarPosition()) / block_size;
		double intensity = 1/(dist + 1);
		return new AudioObservation(e.activeTypeId, intensity, e.audioSrc);
	}

	/**
	 * Retuns the observation of this state (for multiplayer).
	 *
	 * @return the observation.
	 */
	public StateObservationMulti getObservationMulti(int playerID) {
		return new StateObservationMulti(fwdModel.copy(), playerID);
	}

	/**
	 * Returns the sampleRandom object
	 *
	 * @return the sampleRandom generator.
	 */
	public Random getRandomGenerator() {
		return random;
	}

	/**
	 * Returns the current game tick of this game.
	 *
	 * @return the current game tick of this game.
	 */
	public int getGameTick() {
		return gameTick;
	}

	/**
	 * Returns the winner of this game. A value from Types.WINNER.
	 *
	 * @return the winner of this game.
	 */
	public Types.WINNER getWinner() {
		return getWinner(0);
	}

	/**
	 * Overloaded method for multi player games. Returns the win state of the
	 * specified player.
	 *
	 * @param playerID
	 *            ID of the player.
	 * @return the win state of the specified player.
	 */
	public Types.WINNER getWinner(int playerID) {
		return avatars[playerID].getWinState();
	}

	/**
	 * Gets the order in which the sprites are drawn.
	 *
	 * @return the order of the sprites.
	 */
	public int[] getSpriteOrder() {
		return spriteOrder;
	}

	/**
	 * Returns the number of sprites
	 */
	public static int getMaxSprites() {
		return MAX_SPRITES;
	}

	/**
	 * Indicates how many pixels form a block in the game.
	 *
	 * @return how many pixels form a block in the game.
	 */
	public int getBlockSize() {
		return block_size;
	}

	public abstract void buildStringLevel(String[] levelString, int randomSeed);

	/**
	 * Builds a level, receiving a file name.
	 *
	 * @param gamelvl file name containing the level.
	 */
	public void buildLevel(String gamelvl, int randomSeed) {
	}

	public ArrayList<Node> getPath(Vector2d start, Vector2d end) {
		Vector2d pathStart = new Vector2d(start);
		Vector2d pathEnd = new Vector2d(end);

		pathStart.mul(1.0 / (double) block_size);
		pathEnd.mul(1.0 / (double) block_size);

		return pathf.getPath(pathStart, pathEnd);
	}

	public HashMap<String, ParameterContent> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, ParameterContent> parameters) {
		this.parameters = parameters;
	}

    public boolean playAudio() {
		return playAudio;
    }

	public void setAudio(boolean b) {
		playAudio = b;
	}

    /**
	 * Class for helping collision detection.
	 */
	protected class Bucket {
		ArrayList<VGDLSprite> allSprites;
		HashMap<Integer, ArrayList<VGDLSprite>> spriteLists;
		int totalNumSprites;

		public Bucket() {
			allSprites = new ArrayList<>();
			spriteLists = new HashMap<>();
			totalNumSprites = 0;
		}

		public void clear() {
			allSprites.clear();
			spriteLists.clear();
			totalNumSprites = 0;
		}

		public void add(VGDLSprite sp) {
			int bucket = sp.bucket;
			ArrayList<VGDLSprite> sprites = spriteLists.computeIfAbsent(bucket, k -> new ArrayList<>());
			sprites.add(sp);
			allSprites.add(sp);
			totalNumSprites++;
		}

		public int size() {
			return totalNumSprites;
		}

		public int size(int bucket) {
			ArrayList<VGDLSprite> sprites = spriteLists.get(bucket);
			if (sprites == null)
				return 0;
			return sprites.size();
		}

		public ArrayList<VGDLSprite> getAllSprites() {
			return allSprites;
		}

		public HashMap<Integer, ArrayList<VGDLSprite>> getSpriteList() {
			return spriteLists;
		}

	}

}
