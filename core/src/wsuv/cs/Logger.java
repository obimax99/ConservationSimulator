package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import static wsuv.cs.Constants.*;

public abstract class Logger extends Sprite {
    // movement stuff is shared
    // health stuff being lowered is shared
    protected int health;
    protected int damage;
    protected int gridX;
    protected int gridY;
    public final float MOVE_SPEED = 32;
    private boolean dead;
    private float xVelocity;
    private float yVelocity;

    Logger(Texture texture, int gridX, int gridY) {
        super(texture);
        dead = false;
        this.gridX = gridX;
        this.gridY = gridY;
        setX(gridX*TILE_SIZE);
        setY(gridY*TILE_SIZE);
    }

    public void update(float delta, char direction) {
        float x = getX();
        float y = getY();
        xVelocity = 0;
        yVelocity = 0;
        switch (direction) {
            case 'L':
                xVelocity = -1 * MOVE_SPEED;
                break;
            case 'R':
                xVelocity = MOVE_SPEED;
                break;
            case 'D':
                yVelocity = -1 * MOVE_SPEED;
                break;
            case 'U':
                yVelocity = MOVE_SPEED;
                break;
            default:
                break;
        }
        setX(x + delta * xVelocity);
        setY(y + delta * yVelocity);
    }

    public int getCurrGridNum() {
        // determine which grid tile the logger is actually in:
        // if the x value is greater than half of the gridX, update gridX.
        // if the x value is lower than half of the previous gridX, update gridX
        float x = getX();
        float y = getY();
        if (x > (gridX * TILE_SIZE) + ((float) TILE_SIZE / 2)) {
            gridX++;
        }
        else if (x < ((gridX-1) * TILE_SIZE) + ((float) TILE_SIZE / 2)) {
            gridX--;
        }
        if (y > (gridY * TILE_SIZE) + ((float) TILE_SIZE / 2)) {
            gridY++;
        }
        else if (y < ((gridY-1) * TILE_SIZE) + ((float) TILE_SIZE / 2)) {
            gridY--;
        }
        return gridY*GRID_SIZE+gridX;
    }

    protected void takeDamage(int damage) {
        if (isDead()) { return; }
        this.health -= damage;
        if (health <= 0) {
            dead = true;
        }
    }

    public boolean isDead() { return dead; }

    public abstract Logger makeCopy(CSGame game, int gridX, int gridY);

}
