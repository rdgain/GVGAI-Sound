package core.termination;

import core.content.TerminationContent;
import core.game.Game;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:48
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
@SuppressWarnings("WeakerAccess")
public class Timeout extends Termination
{
    public boolean use_counter = false;
    public boolean compare = false;
    public String limits = "0";

    public Timeout(){}

    public Timeout(TerminationContent cnt)
    {
        //Parse the arguments.
        this.parseParameters(cnt);
    }

    @Override
    public boolean isDone(Game game)
    {
        boolean ended = super.isFinished(game);
        if(ended) {
            return true;
        }

        if(game.getGameTick() >= limit) {
            countScore(game);

            if (use_counter) {
                //use the master game counter
                if (compare) {
                    //if comparing, the first player wins if they're not equal, the rest win otherwise
                    int first = game.getValueCounter(0);
                    boolean ok = true;
                    for (int i = 1; i < game.getNoCounters(); i++) {
                        if (game.getValueCounter(i) != first) {
                            ok = false;
                        }
                    }
                    if (ok) {
                        StringBuilder wintext = new StringBuilder("False,");
                        for (int i = 1; i < game.getNoPlayers(); i++) {
                            if (i == game.no_players - 1) {
                                wintext.append("True");
                            } else wintext.append("True,");
                        }
                        win = wintext.toString();
                    } else {
                        StringBuilder wintext = new StringBuilder("True,");
                        for (int i = 1; i < game.getNoPlayers(); i++) {
                            if (i == game.no_players - 1) {
                                wintext.append("False");
                            } else wintext.append("False,");
                        }
                        win = wintext.toString();
                    }
                } else {
                    //use the limits, split it and check each counter, idx corresponding to player ID
                    if (game.no_players != game.no_counters) {
                        StringBuilder wintext = new StringBuilder();
                        for (int i = 0; i < game.no_players; i++) {
                            if (i != game.no_players - 1) wintext.append("False,");
                            else wintext.append("False");
                        }
                        win = wintext.toString();
                    } else {
                        String[] split = limits.split(",");
                        int[] intlimits = new int[split.length];
                        for (int i = 0; i < intlimits.length;i++)
                            intlimits[i] = Integer.parseInt(split[i]);

                        StringBuilder wintext = new StringBuilder();
                        for (int i = 0; i < game.no_players; i++) {
                            if (game.getValueCounter(i) == intlimits[i]) {
                                wintext.append("True");
                            } else
                                wintext.append("False");
                            if (i != game.no_players - 1) wintext.append(",");
                        }
                        win = wintext.toString();
                    }
                }
            }

            return true;
        }

        return false;
    }
}
