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

    Logger(Texture texture) {
        super(texture);
    }

    protected boolean takeDamage(int damage) {
        this.health -= damage;
        if (health <= 0) {
            return true;
        }
        return false;
    }

    public abstract Logger makeCopy(CSGame game, int row, int col);

}
