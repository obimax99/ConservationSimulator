package wsuv.cs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.lang.Math;
import java.util.HashMap;
import java.util.Map;

import static wsuv.cs.Constants.*;

public class Tower extends Sprite {
    private int[] upgradeCosts;
    int numUpgrades;
    protected int gridY;
    protected int gridX;
    private int maxHealth;
    private int health;
    public boolean isDead;
    private final int healthIncreasePerUpgrade = 1;
    private final int MAX_HEALTH_UPGRADES = 10;
    private int currentHealthUpgrades;
    private int range;
    private final int rangeIncreasePerUpgrade = 1;
    private final int MAX_RANGE_UPGRADES = 10;
    private int currentRangeUpgrades;
    private float timeBetweenAttacks;
    private final float atkSpdIncreasePerUpgrade = 0.2f;
    private float towerAttackTimer;
    private final int MAX_ATK_SPD_UPGRADES = 10;
    private int currentAtkSpdUpgrades;

    Map<String, Animation<TextureRegion>> greenAnimations;
    Map<String, Animation<TextureRegion>> blueAnimations;
    Map<String, Animation<TextureRegion>> purpleAnimations;
    Map<String, Animation<TextureRegion>> currentAnimations;
    Color currentColor;
    private float idleAnimationDuration;
    private float attackAnimationDuration;
    private float hurtAnimationDuration;
    private float deathAnimationDuration;
    private float animationTimer;
    private float deathTimer;
    private boolean attacking;
    private boolean hurting;
    private boolean dying;

    Animation<TextureRegion> greenFrogIdleAnimation;
    Animation<TextureRegion> greenFrogAttackAnimation;
    Animation<TextureRegion> greenFrogHurtAnimation;
    Animation<TextureRegion> greenFrogDeathAnimation;

    Animation<TextureRegion> blueFrogIdleAnimation;
    Animation<TextureRegion> blueFrogAttackAnimation;
    Animation<TextureRegion> blueFrogHurtAnimation;
    Animation<TextureRegion> blueFrogDeathAnimation;

    Animation<TextureRegion> purpleFrogIdleAnimation;
    Animation<TextureRegion> purpleFrogAttackAnimation;
    Animation<TextureRegion> purpleFrogHurtAnimation;
    Animation<TextureRegion> purpleFrogDeathAnimation;



    public Tower(CSGame game, int gridX, int gridY) {
        super(game.am.get("frogTower.png", Texture.class));
        this.setSize(TILE_SIZE, TILE_SIZE);
        this.setScale(2);
        this.gridX = gridX;
        this.gridY = gridY;
        this.setX(gridX*TILE_SIZE);
        this.setY(gridY*TILE_SIZE);
        this.health = 5;
        this.maxHealth = health;
        this.isDead = false;
        this.range = 4;
        this.timeBetweenAttacks = 1.66667f;
        this.towerAttackTimer = 0.0f;
        this.upgradeCosts = new int[] {
                3,
                7,
                3,
        };

        this.numUpgrades = 0;
        this.currentHealthUpgrades = 0;
        this.currentRangeUpgrades = 0;
        this.currentAtkSpdUpgrades = 0;

        this.greenFrogIdleAnimation = game.greenFrogIdleAnimation;
        this.greenFrogAttackAnimation = game.greenFrogAttackAnimation;
        this.greenFrogHurtAnimation = game.greenFrogHurtAnimation;
        this.greenFrogDeathAnimation = game.greenFrogDeathAnimation;

        this.blueFrogIdleAnimation = game.blueFrogIdleAnimation;
        this.blueFrogAttackAnimation = game.blueFrogAttackAnimation;
        this.blueFrogHurtAnimation = game.blueFrogHurtAnimation;
        this.blueFrogDeathAnimation = game.blueFrogDeathAnimation;

        this.purpleFrogIdleAnimation = game.purpleFrogIdleAnimation;
        this.purpleFrogAttackAnimation = game.purpleFrogAttackAnimation;
        this.purpleFrogHurtAnimation = game.purpleFrogHurtAnimation;
        this.purpleFrogDeathAnimation = game.purpleFrogDeathAnimation;

        this.idleAnimationDuration = greenFrogIdleAnimation.getAnimationDuration();
        this.attackAnimationDuration = greenFrogAttackAnimation.getAnimationDuration();
        this.hurtAnimationDuration = greenFrogHurtAnimation.getAnimationDuration();
        this.deathAnimationDuration = greenFrogDeathAnimation.getAnimationDuration();
        this.animationTimer = 0.0f;
        this.deathTimer = 0.0f;
        this.attacking = false;
        this.hurting = false;
        this.dying = false;

        greenAnimations = new HashMap<>(4);
        greenAnimations.put("idle", greenFrogIdleAnimation);
        greenAnimations.put("attack", greenFrogAttackAnimation);
        greenAnimations.put("hurt", greenFrogHurtAnimation);
        greenAnimations.put("death", greenFrogDeathAnimation);

        blueAnimations = new HashMap<>(4);
        blueAnimations.put("idle", blueFrogIdleAnimation);
        blueAnimations.put("attack", blueFrogAttackAnimation);
        blueAnimations.put("hurt", blueFrogHurtAnimation);
        blueAnimations.put("death", blueFrogDeathAnimation);

        purpleAnimations = new HashMap<>(4);
        purpleAnimations.put("idle", purpleFrogIdleAnimation);
        purpleAnimations.put("attack", purpleFrogAttackAnimation);
        purpleAnimations.put("hurt", purpleFrogHurtAnimation);
        purpleAnimations.put("death", purpleFrogDeathAnimation);

        currentAnimations = greenAnimations;
        currentColor = Color.GREEN;
    }

    /*
    * This is what will be displayed on the UI and what the player technically upgrades as it
    * is easier to understand; however, time_between_attacks is the value that matters as it
    * is easier to use in math equations and figuring out timing based on a single timer.
    * */
    public float getAttackSpeed() {
        return (1 / timeBetweenAttacks);
    }

    public int getHealth() {
        return health;
    }

    public int getRange() {
        return range;
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
        // if we attack
        if (closestLogger != null) {
            animationTimer = 0.0f;
            attacking = true;
            towerAttackTimer = 0;
        }
        return closestLogger;
    }

    public void update(float delta) {
        towerAttackTimer+=delta;
        animationTimer+=delta;
        TextureRegion currentFrame;
        if (dying) {
            deathTimer += delta;
            currentFrame = currentAnimations.get("death").getKeyFrame(deathTimer, false);
            if (deathTimer >= deathAnimationDuration) {
                dying = false;
                isDead = true;
            }
        }
        else if (hurting) {
            currentFrame = currentAnimations.get("hurt").getKeyFrame(animationTimer, false);
            if (animationTimer >= hurtAnimationDuration) {
                hurting = false;
            }
        }
        else if (attacking) {
            currentFrame = currentAnimations.get("attack").getKeyFrame(animationTimer, false);
            if (animationTimer >= attackAnimationDuration) {
                attacking = false;
            }
        }
        else {
            currentFrame = currentAnimations.get("idle").getKeyFrame(animationTimer, true);
        }
        setRegion(currentFrame);
    }

    private void upgradeCompleted() {
        numUpgrades++;
        if (numUpgrades >= 15 && numUpgrades < 30) {
            currentAnimations = blueAnimations;
            currentColor = Color.BLUE;
        }
        else if (numUpgrades == 30 ) {
            currentAnimations = purpleAnimations;
            currentColor = Color.PURPLE;
        }
        else {
            currentAnimations = greenAnimations;
            currentColor = Color.GREEN;
        }
    }

    public int upgradeHealth(int totalFertilizer) {
        if (totalFertilizer < upgradeCosts[0]) { return -1; }
        if (currentHealthUpgrades >= MAX_HEALTH_UPGRADES) { return -1; }
        int remainingFertilizer = totalFertilizer - upgradeCosts[0];
        health += healthIncreasePerUpgrade;
        maxHealth += healthIncreasePerUpgrade;
        // maxHealth useless for now, but the hope is to get a UI element that shows health bar
        upgradeCosts[0] = upgradeCosts[0] + 2;
        currentHealthUpgrades++;
        upgradeCompleted();
        return remainingFertilizer;
    }

    public int upgradeRange(int totalFertilizer) {
        if (totalFertilizer < upgradeCosts[1]) { return -1; }
        if (currentRangeUpgrades >= MAX_RANGE_UPGRADES) { return -1; }
        int remainingFertilizer = totalFertilizer - upgradeCosts[1];
        range += rangeIncreasePerUpgrade;
        upgradeCosts[1] = upgradeCosts[1] + 2;
        currentRangeUpgrades++;
        upgradeCompleted();
        return remainingFertilizer;
    }

    public int upgradeAtkSpd(int totalFertilizer) {
        if (totalFertilizer < upgradeCosts[2]) { return -1; }
        if (currentAtkSpdUpgrades >= MAX_ATK_SPD_UPGRADES) { return -1; }
        int remainingFertilizer = totalFertilizer - upgradeCosts[2];
        timeBetweenAttacks = 1 / (getAttackSpeed() + atkSpdIncreasePerUpgrade);
        upgradeCosts[2] = upgradeCosts[2] + 2;
        currentAtkSpdUpgrades++;
        upgradeCompleted();
        return remainingFertilizer;
    }

    public int getUpgradeCost(int buttonNum) {
        return upgradeCosts[buttonNum];
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            dying = true;
            deathTimer = 0.0f;
        }
        else {
            hurting = true;
            animationTimer = 0.0f;
        }
    }
}
