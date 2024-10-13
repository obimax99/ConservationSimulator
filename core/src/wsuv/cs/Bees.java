package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import static wsuv.cs.Constants.TILE_SIZE;

public class Bees extends Sprite {
    public int gridX;
    public int gridY;
    public int damage;
    public Bees(CSGame game, int gridX, int gridY) {
        super(game.am.get("bee.png", Texture.class));
        this.gridX = gridX;
        this.gridY = gridY;
        setX(gridX*TILE_SIZE);
        setY(gridY*TILE_SIZE);
        this.damage = 1;
    }
}
