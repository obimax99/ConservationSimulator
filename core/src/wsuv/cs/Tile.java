package wsuv.cs;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.Batch;

public class Tile extends Sprite {
    protected Terrain terrain;
    protected int currentCost;

    public Tile(Terrain terrain) {
        super(terrain.texture);
        this.terrain = terrain;
    }

    public int getTerrainCost() {
        return terrain.cost;
    }

}
