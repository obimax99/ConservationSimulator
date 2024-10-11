package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Lumberjack extends Logger {
    Lumberjack(CSGame game, int gridX, int gridY) {
        super(game.am.get("lumberjack.png", Texture.class));

        health = 1;
        damage = 1;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public Lumberjack makeCopy(CSGame game, int gridX, int gridY) {
        return new Lumberjack(game, gridX, gridY);
    }
}
