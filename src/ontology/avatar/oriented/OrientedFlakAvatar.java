package ontology.avatar.oriented;

import core.content.SpriteContent;
import core.game.Game;
import core.vgdl.VGDLRegistry;
import core.vgdl.VGDLSprite;
import ontology.Types;
import tools.Direction;
import tools.SoundManager;
import tools.Utils;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 22/10/13
 * Time: 18:10
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class OrientedFlakAvatar extends ShootAvatar
{
    public OrientedFlakAvatar(){}

    public OrientedFlakAvatar(Vector2d position, Dimension size, SpriteContent cnt)
    {
        //Init the sprite
        this.init(position, size);

        //Specific class default parameter values.
        loadDefaults();

        //Parse the arguments.
        this.parseParameters(cnt);
    }


    public void postProcess()
    {
        //Define actions here first.
        if(actions.size()==0)
        {
            actions.add(Types.ACTIONS.ACTION_USE);
            actions.add(Types.ACTIONS.ACTION_LEFT);
            actions.add(Types.ACTIONS.ACTION_RIGHT);
        }

        super.postProcess();

        stypes = stype.split(",");
        itype = new int[stypes.length];

        for (int i = 0; i < itype.length; i++)
            itype[i] = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stypes[i]);
        if(ammo != null) {
            ammos = ammo.split(",");
            ammoId = new int[ammos.length];
            for (int i = 0; i < ammos.length; i++) {
                ammoId[i] = VGDLRegistry.GetInstance().getRegisteredSpriteValue(ammos[i]);
            }
        }
    }
}
