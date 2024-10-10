package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Bulldozer extends Logger {
    Bulldozer(CSGame game, int row, int col) {
        super(game.am.get("bulldozer.png", Texture.class));

        health = 2;
        damage = 2;
        this.row = row;
        this.col = col;
    }

    public Bulldozer makeCopy(CSGame game, int row, int col) {
        return new Bulldozer(game, row, col);
    }
}
