package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Shredder extends Logger {
    Shredder(CSGame game, int gridX, int gridY) {
        super(game.am.get("shredder.png", Texture.class), gridX, gridY);
        this.walkDownAnimation = game.shredderWalkDownAnimation;
        this.walkLeftAnimation = game.shredderWalkLeftAnimation;
        this.walkRightAnimation = game.shredderWalkRightAnimation;
        this.walkUpAnimation = game.shredderWalkUpAnimation;
        health = 3;
        damage = 3;
    }

    public Shredder makeCopy(CSGame game, int gridX, int gridY) {
        return new Shredder(game, gridX, gridY);
    }
}
