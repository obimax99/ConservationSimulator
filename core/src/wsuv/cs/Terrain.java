package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Terrain {
    public int cost;
    public Texture texture;

    public Terrain(int cost, Texture texture) {
        this.texture = texture;
        this.cost = cost;
    }

}
