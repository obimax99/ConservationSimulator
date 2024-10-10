package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;
import java.lang.Math;

import static wsuv.cs.Constants.*;

public class Tower extends Sprite {
    protected int row;
    protected int col;
    private int health;
    private int range;
    private float time_between_attacks;
    private float tower_attack_timer;

    public Tower(CSGame game, int row, int col) {
        super(game.am.get("frogTower.png", Texture.class));
        this.row = row;
        this.col = col;
        this.setX(col*TILE_SIZE);
        this.setY(row*TILE_SIZE);
        this.health = 5;
        this.range = 30;
        this.time_between_attacks = 1.0f;
        this.tower_attack_timer = 0.0f;
    }

    /*
    * This is what will be displayed on the UI and what the player technically upgrades as it
    * is easier to understand; however, time_between_attacks is the value that matters as it
    * is easier to use in math equations and figuring out timing based on a single timer.
    * */
    public float get_attack_speed() {
        return (1 / time_between_attacks);
    }

    /*
    * Returns the logger that the tower is launching the attack at; PlayScreen will actually
    * instantiate the projectile and keep track of it, drawing it until it dies. The frog no
    * longer owns its spit once it's been fired.
    * */
    public Logger attack(ArrayList<Logger> liveLoggers) {
        if (tower_attack_timer < time_between_attacks) { return null; }
        int minRow = Math.max(row-range, 0);
        int minCol = Math.max(col-range, 0);
        int maxRow = Math.min(row+range, GRID_SIZE-1);
        int maxCol = Math.min(col+range, GRID_SIZE-1);
        int closestDist = 2*range;
        Logger closestLogger = null;
        for (Logger logger : liveLoggers) {
            // might be able to get rid of this first if statement.
            if (logger.row >= minRow && logger.col >= minCol && logger.row <= maxRow && logger.col <= maxCol) {
                int distFromTower = Math.abs(logger.row - row) + Math.abs(logger.col - col);
                if (distFromTower < closestDist) {
                    closestLogger = logger;
                }
            }
        }
        if (closestLogger != null) { tower_attack_timer = 0; }
        return closestLogger;
    }

    public void update(float delta) {
        tower_attack_timer+=delta;
    }

    // upgrade_health, upgrade_range, upgrade_attack_speed will be below
}
