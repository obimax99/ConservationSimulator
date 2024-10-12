package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;
import java.lang.Math;

import static wsuv.cs.Constants.*;

public class Tower extends Sprite {
    private int[] upgradeCosts;
    protected int gridY;
    protected int gridX;
    private int maxHealth;
    private int health;
    private final int healthIncreasePerUpgrade = 2;
    private int range;
    private final int rangeIncreasePerUpgrade = 1;
    private float timeBetweenAttacks;
    private final float atkSpdIncreasePerUpgrade = 0.2f;
    private float towerAttackTimer;

    public Tower(CSGame game, int gridX, int gridY) {
        super(game.am.get("frogTower.png", Texture.class));
        this.gridX = gridX;
        this.gridY = gridY;
        this.setX(gridX*TILE_SIZE);
        this.setY(gridY*TILE_SIZE);
        this.health = 5;
        this.maxHealth = health;
        this.range = 5;
        this.timeBetweenAttacks = 1.0f;
        this.towerAttackTimer = 0.0f;
        this.upgradeCosts = new int[] {
                3,
                3,
                3,
        };
    }

    /*
    * This is what will be displayed on the UI and what the player technically upgrades as it
    * is easier to understand; however, time_between_attacks is the value that matters as it
    * is easier to use in math equations and figuring out timing based on a single timer.
    * */
    public float getAttackSpeed() {
        return (1 / timeBetweenAttacks);
    }

    /*
    * Returns the logger that the tower is launching the attack at; PlayScreen will actually
    * instantiate the projectile and keep track of it, drawing it until it dies. The frog no
    * longer owns its spit once it's been fired.
    * */
    public Logger attack(ArrayList<Logger> liveLoggers) {
        if (towerAttackTimer < timeBetweenAttacks) { return null; }
        int minGridX = Math.max(gridX-range, 0);
        int minGridY = Math.max(gridY-range, 0);
        int maxGridX = Math.min(gridX+range, GRID_SIZE-1);
        int maxGridY = Math.min(gridY+range, GRID_SIZE-1);
        int closestDist = 2*range;
        Logger closestLogger = null;
        for (Logger logger : liveLoggers) {
            if (logger.gridY >= minGridY && logger.gridX >= minGridX && logger.gridY <= maxGridY && logger.gridX <= maxGridX) {
                int distFromTower = Math.abs(logger.gridX - gridX) + Math.abs(logger.gridY - gridY);
                if (distFromTower < closestDist) {
                    closestDist = distFromTower;
                    closestLogger = logger;
                }
            }
        }
        if (closestLogger != null) { towerAttackTimer = 0; }
        return closestLogger;
    }

    public void update(float delta) {
        towerAttackTimer+=delta;
    }

    // upgrade_health, upgrade_range, upgrade_attack_speed will be below
    public int upgradeHealth(int totalFertilizer) {
        if (totalFertilizer < upgradeCosts[0]) { return -1; }
        int remainingFertilizer = totalFertilizer - upgradeCosts[0];
        health += healthIncreasePerUpgrade;
        maxHealth += healthIncreasePerUpgrade;
        // maxHealth useless for now, but the hope is to get a UI element that shows health bar
        upgradeCosts[0]++;
        return remainingFertilizer;
    }

    public int upgradeRange(int totalFertilizer) {
        if (totalFertilizer < upgradeCosts[1]) { return -1; }
        int remainingFertilizer = totalFertilizer - upgradeCosts[1];
        range += rangeIncreasePerUpgrade;
        upgradeCosts[1]++;
        return remainingFertilizer;
    }

    public int upgradeAtkSpd(int totalFertilizer) {
        if (totalFertilizer < upgradeCosts[2]) { return -1; }
        int remainingFertilizer = totalFertilizer - upgradeCosts[2];
        timeBetweenAttacks = 1 / (getAttackSpeed() + atkSpdIncreasePerUpgrade);
        upgradeCosts[2]++;
        return remainingFertilizer;
    }

    public int getUpgradeCost(int buttonNum) {
        return upgradeCosts[buttonNum];
    }
}
