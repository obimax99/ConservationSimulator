package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Shredder extends Logger {
    Shredder(CSGame game, int row, int col) {
        super(game.am.get("shredder.png", Texture.class));

        health = 3;
        damage = 3;
        this.row = row;
        this.col = col;
    }

    public Shredder makeCopy(CSGame game, int row, int col) {
        return new Shredder(game, row, col);
    }
}
