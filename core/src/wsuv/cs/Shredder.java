package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Shredder extends Logger {
    Shredder(CSGame game, int gridX, int gridY) {
        super(game.am.get("shredder.png", Texture.class));

        health = 3;
        damage = 3;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public Shredder makeCopy(CSGame game, int gridX, int gridY) {
        return new Shredder(game, gridX, gridY);
    }
}
