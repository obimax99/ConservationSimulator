package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import static wsuv.cs.Constants.*;

public abstract class Logger extends Sprite {
    // movement stuff is shared
    // health stuff being lowered is shared
    protected int health;
    protected int damage;
    protected int row;
    protected int col;
    public final float MOVE_SPEED = 32;
    private boolean dead;

    Logger(Texture texture) {
        super(texture);
        dead = false;
    }

    public void update() {
        // pathfinding
        // movement whatever who cares
        // (at least until we do actual smooth movement haha)
        setX(row*TILE_SIZE);
        setY(col*TILE_SIZE);
    }

    protected void takeDamage(int damage) {
        if (isDead()) { return; }
        this.health -= damage;
        if (health <= 0) {
            dead = true;
        }
    }

    public boolean isDead() { return dead; }

    public abstract Logger makeCopy(CSGame game, int row, int col);

}
