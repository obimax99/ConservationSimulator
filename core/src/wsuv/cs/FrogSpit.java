package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.lang.Math;
import java.util.ArrayList;

import static wsuv.cs.Constants.*;

public class FrogSpit extends Sprite {
    float xVelocity;
    float yVelocity;
    private final float SPIT_SPEED = TILE_SIZE*16;
    private Logger target;
    int damage;

    public FrogSpit(CSGame game, int startGridX, int startGridY, Logger logger) {
        super(game.am.get("frogSpit.png", Texture.class));
        this.target = logger;
        this.damage = 1;
        setX(startGridX*TILE_SIZE);
        setY(startGridY*TILE_SIZE);

        // get normalized vector
        setVelocity();
    }

    // if it made it to its destination, return true so it can be removed from projectile list.
    public boolean update(float delta, ArrayList<Logger> liveLoggers) {
        float x = getX();
        float y = getY();
        if (target == null) {
            if (x < 0 || x > (GRID_SIZE-1)*TILE_SIZE || y < 0 || y > (GRID_SIZE-1)*TILE_SIZE) { return true; }
            else {
                for (Logger logger : liveLoggers) {
                    float loggerX = logger.getX();
                    float loggerY = logger.getY();
                    if (loggerX <= x+ (float) TILE_SIZE /2 && loggerX >= x-(float) TILE_SIZE /2 &&
                            loggerY <= y+(float) TILE_SIZE /2 && loggerY >= y-(float) TILE_SIZE /2) {
                        logger.takeDamage(damage);
                        return true;
                    }
                }
                setX(x + delta * xVelocity);
                setY(y + delta * yVelocity);
                return false;
            }
        }
        else if (target.isDead()) {
            target = null;
            return false; // LITTLE bit of an issue but only if framerate is extremely slow AND logger is on edge
        }
        float loggerX = target.getX();
        float loggerY = target.getY();
        if (loggerX <= x+ (float) TILE_SIZE /2 && loggerX >= x-(float) TILE_SIZE /2 &&
                loggerY <= y+(float) TILE_SIZE /2 && loggerY >= y-(float) TILE_SIZE /2) {
            target.takeDamage(damage);
            return true;
        }
        setVelocity();
        setX(x + delta * xVelocity);
        setY(y + delta * yVelocity);
        return false;
    }

    private void setVelocity() {
        float xDir = target.getX() - getX();
        float yDir = target.getY() - getY();
        double dotProduct = Math.sqrt(xDir*xDir + yDir*yDir);
        float normalizedXDir = (float) (xDir/dotProduct);
        float normalizedYDir = (float) (yDir/dotProduct);
        this.xVelocity = normalizedXDir * SPIT_SPEED;
        this.yVelocity = normalizedYDir * SPIT_SPEED;
    }
}
