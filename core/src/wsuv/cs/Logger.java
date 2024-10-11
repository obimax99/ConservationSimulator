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
    public final float MOVE_SPEED = 64;
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
        switch (direction) {
            case 'L':
                if (notThereYetY(y)) break;
                xVelocity = -1 * MOVE_SPEED;
                break;
            case 'R':
                if (notThereYetY(y)) break;
                xVelocity = MOVE_SPEED;
                break;
            case 'D':
                if (notThereYetX(x)) break;
                yVelocity = -1 * MOVE_SPEED;
                break;
            case 'U':
                if (notThereYetX(x)) break;
                yVelocity = MOVE_SPEED;
                break;
            default:
                break;
        }
        setX(getX() + delta * xVelocity);
        setY(getY() + delta * yVelocity);
    }

    private boolean notThereYetY(float y) {
        // if we're going up, we need to arrive fully at our destination before turning
        if (yVelocity > 0) {
            if (y <= gridY*TILE_SIZE) {
                return true;
            } // keep going up!
            else {
                setY(gridY*TILE_SIZE); // snap to grid
                yVelocity = 0;
            }
        }
        // same if we're going down
        else if (yVelocity < 0) {
            if (y >= gridY*TILE_SIZE) {
                return true;
            } // keep going down!
            else {
                setY(gridY*TILE_SIZE); // snap to grid
                yVelocity = 0;
            }
        }
        return false;
    }

    private boolean notThereYetX(float x) {
        // if we're going right, we need to arrive fully at our destination before turning
        if (xVelocity > 0) {
            if (x <= gridX*TILE_SIZE) {
                return true;
            } // keep going right!
            else {
                setX(gridX*TILE_SIZE); // snap to grid
                xVelocity = 0;
            }
        }
        // same if we're going left
        else if (xVelocity < 0) {
            if (x >= gridX*TILE_SIZE) {
                return true;
            } // keep going left!
            else {
                setX(gridX*TILE_SIZE); // snap to grid
                xVelocity = 0;
            }
        }
        return false;
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
        else if (x < (gridX * TILE_SIZE) - ((float) TILE_SIZE / 2)) {
            gridX--;
        }
        if (y > (gridY * TILE_SIZE) + ((float) TILE_SIZE / 2)) {
            gridY++;
        }
        else if (y < (gridY * TILE_SIZE) - ((float) TILE_SIZE / 2)) {
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
