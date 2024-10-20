package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Lumberjack extends Logger {
    Lumberjack(CSGame game, int gridX, int gridY) {
        super(game.am.get("lumberjack.png", Texture.class), gridX, gridY);
        this.walkDownAnimation = game.lumberjackWalkDownAnimation;
        this.walkLeftAnimation = game.lumberjackWalkLeftAnimation;
        this.walkRightAnimation = game.lumberjackWalkRightAnimation;
        this.walkUpAnimation = game.lumberjackWalkUpAnimation;
        health = 1;
        damage = 1;
    }

    public Lumberjack makeCopy(CSGame game, int gridX, int gridY) { return new Lumberjack(game, gridX, gridY); }
}
