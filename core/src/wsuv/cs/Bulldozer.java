package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Bulldozer extends Logger {
    Bulldozer(CSGame game, int gridX, int gridY) {
        super(game.am.get("bulldozer.png", Texture.class), gridX, gridY);
        this.walkDownAnimation = game.bulldozerWalkDownAnimation;
        this.walkLeftAnimation = game.bulldozerWalkLeftAnimation;
        this.walkRightAnimation = game.bulldozerWalkRightAnimation;
        this.walkUpAnimation = game.bulldozerWalkUpAnimation;
        health = 2;
        damage = 2;
    }

    public Bulldozer makeCopy(CSGame game, int gridX, int gridY) {
        return new Bulldozer(game, gridX, gridY);
    }
}
