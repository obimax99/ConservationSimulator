package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;

public class Lumberjack extends Logger {
    Lumberjack(CSGame game, int row, int col) {
        super(game.am.get("lumberjack.png", Texture.class));

        health = 1;
        damage = 1;
        this.row = row;
        this.col = col;
    }

    public Lumberjack makeCopy(CSGame game, int row, int col) {
        return new Lumberjack(game, row, col);
    }
}
