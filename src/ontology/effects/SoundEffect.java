package ontology.effects;

import core.game.Game;
import core.vgdl.VGDLSprite;

public class SoundEffect extends Effect {

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game) {
        assert audio != null && !audio.equals("");
        super.execute(sprite1, sprite2, game);
    }
}
