package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

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
        // thankfully because 32 pixels is one tile and we know that, we can do shortcuts like this:
        // (at least until we do actual smooth movement haha)
        setX(row*32);
        setY(col*32);
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
