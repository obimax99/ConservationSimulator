package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Lumberjack extends Logger {
    Lumberjack(CSGame game, int gridX, int gridY) {
        super(game.am.get("lumberjack.png", Texture.class), gridX, gridY);
        this.walkDownAnimation = game.walkDownAnimation;
        this.walkLeftAnimation = game.walkLeftAnimation;
        this.walkRightAnimation = game.walkRightAnimation;
        this.walkUpAnimation = game.walkUpAnimation;
        health = 1;
        damage = 1;
    }

    public Lumberjack makeCopy(CSGame game, int gridX, int gridY) { return new Lumberjack(game, gridX, gridY); }
}
