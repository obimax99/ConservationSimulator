package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Bulldozer extends Logger {
    Bulldozer(CSGame game, int gridX, int gridY) {
        super(game.am.get("bulldozer.png", Texture.class));

        health = 2;
        damage = 2;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public Bulldozer makeCopy(CSGame game, int gridX, int gridY) {
        return new Bulldozer(game, gridX, gridY);
    }
}
