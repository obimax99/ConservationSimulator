package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Tower extends Sprite {
    int row;
    int col;

    public Tower(CSGame game, int row, int col) {
        super(game.am.get("frogTower.png", Texture.class));
        this.row = row;
        this.col = col;
        this.setX(col*32);
        this.setY(row*32);
    }
}
