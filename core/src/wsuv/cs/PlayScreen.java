package wsuv.cs;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ScreenUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static wsuv.cs.Constants.*;

public class PlayScreen extends ScreenAdapter {
    private boolean DEBUG_pathfinding;
    private boolean DEBUG_borders;
    NumberFormat formatter = new DecimalFormat("0.00");

    private enum SubState {READY, GAME_OVER, PLAYING}
    private CSGame csGame;
    private HUD hud;
    private SubState state;
    private BitmapFont font;

    private int highScore;
    private int currentWave;

    private Terrain[] terrains;
    private Tile[][] grid;
    private Bees[][] beeGrid;
    private int[][] setupGrid;
    private float timer;
    private float wave_time;
    private float time_between_spawns;
    private ArrayList<Logger> liveLoggers;
    private ArrayList<Logger> totalLoggers;
    private int loggerSpawnCount;

    private ArrayList<Tower> towers;
    private ArrayList<FrogSpit> frogSpits;

    private String[] buttonKeyBinds;
    private ArrayList<CSButton> upgradeButtons;
    private String[] upgradeButtonFuncs;
    private Tower towerBeingUpgraded;
    private ArrayList<CSButton> summonButtons;
    private int[] summonCosts;
    private String[] summonButtonFuncs;
    private int fertilizerCount;
    private boolean summoningTree;
    private Sprite treeHolo;
    private ArrayList<ShrubTree> shrubTrees;
    private boolean summoningBees;
    private Sprite beesHolo;
    private boolean summoningTower;
    private Sprite towerHolo;
    private Sprite validateHolo;

    private ArrayList<Sound> spitSfx;
    private Sound frogHurtSfx;
    private Sound frogDieSfx;
    private Sound notAllowedSfx;
    private Sound confirmedSfx;
    private Sound chopSfx;
    private Sound footstepSfx;
    private Sound beesSfx;
    private Sound loggerDieSfx;
    private Sound growSfx;

    public PlayScreen(CSGame game) {
        timer = 0;
        csGame = game;
        spitSfx = new ArrayList<Sound>(4);
        spitSfx.add(game.am.get(CSGame.RSC_SPIT1_SFX)); spitSfx.add(game.am.get(CSGame.RSC_SPIT2_SFX)); spitSfx.add(game.am.get(CSGame.RSC_SPIT3_SFX)); spitSfx.add(game.am.get(CSGame.RSC_SPIT4_SFX));
        frogHurtSfx = game.am.get(CSGame.RSC_FROG_HURT_SFX);
        frogDieSfx = game.am.get(CSGame.RSC_FROG_DIE_SFX);
        notAllowedSfx = game.am.get(CSGame.RSC_NOT_ALLOWED_SFX);
        confirmedSfx = game.am.get(CSGame.RSC_CONFIRMED_SFX);
        chopSfx = game.am.get(CSGame.RSC_CHOP_SFX);
        footstepSfx = game.am.get(CSGame.RSC_FOOTSTEP_SFX);
        beesSfx = game.am.get(CSGame.RSC_BEES_SFX);
        loggerDieSfx = game.am.get(CSGame.RSC_LOGGER_DIE_SFX);
        growSfx = game.am.get(CSGame.RSC_GROW_SFX);
        state = SubState.PLAYING;
        DEBUG_pathfinding = false;
        DEBUG_borders = false;
        hud = new HUD(csGame.am.get(CSGame.RSC_MONO_FONT));
        FileHandle file = Gdx.files.internal("highscore.txt");
        highScore = Integer.parseInt(file.readString());
        terrains = new Terrain[NUM_TERRAINS];
        setTerrainTypes();
        getSetupGrid();
        grid = new Tile[GRID_SIZE][GRID_SIZE];
        beeGrid = new Bees[GRID_SIZE][GRID_SIZE];
        towers = new ArrayList<>(1);
        frogSpits = new ArrayList<>(5);
        setGrid();
        doPathfinding();
        font = csGame.am.get(CSGame.RSC_MONO_FONT);

        resetWaves();
        goNextWave(false);

        bindButtons();
        towerBeingUpgraded = null;
        fertilizerCount = 0;
        shrubTrees = new ArrayList<ShrubTree>(16);
        treeHolo = new Sprite(game.am.get("trees.png", Texture.class));
        treeHolo.setAlpha(0.7f);
        beesHolo = new Sprite(game.am.get("bee.png", Texture.class));
        beesHolo.setAlpha(0.7f);
        towerHolo = new Sprite(game.greenFrogIdleAnimation.getKeyFrame(0.05f));
        towerHolo.setSize(TILE_SIZE, TILE_SIZE);
        //towerHolo.setScale(2);
        towerHolo.setAlpha(0.85f);
        validateHolo = new Sprite(game.am.get("whiteTile.png", Texture.class));

        // the HUD will show FPS always, by default.  Here's how
        // to use the HUD interface to silence it (and other HUD Data)
        hud.setDataVisibility(HUDViewCommand.Visibility.WHEN_OPEN);

        // HUD Console Commands
        hud.registerAction("wave", new HUDActionCommand() {
            static final String help = "Switch to a specific wave. Usage: wave <x> ";

            @Override
            public String execute(String[] cmd) {
                try {
                    int x = Integer.parseInt(cmd[1]);
                    if ((x > NUM_WAVES) || (x < 1)) return "Invalid Wave Number";
                    resetWaves();
                    for (int i = 0; i < (x-1); i++) {
                        goNextWave(true);
                    }
                    goNextWave(false);
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("pathfinding", new HUDActionCommand() {
            static final String help = "Toggle pathfinding debug. Usage: pathfinding ";

            @Override
            public String execute(String[] cmd) {
                try {
                    DEBUG_pathfinding = !DEBUG_pathfinding;
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("borders", new HUDActionCommand() {
            static final String help = "Toggle borders debug. Usage: borders ";

            @Override
            public String execute(String[] cmd) {
                try {
                    DEBUG_borders = !DEBUG_borders;
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("endGame", new HUDActionCommand() {
            static final String help = "End game. Usage: endGame ";

            @Override
            public String execute(String[] cmd) {
                try {
                    endGame();
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("fertilizer", new HUDActionCommand() {
            static final String help = "Set fertilizer amount. Usage: fertilizer <x> ";

            @Override
            public String execute(String[] cmd) {
                try {
                    int x = Integer.parseInt(cmd[1]);
                    if (x < 0) return "Fertilizer can't be negative";
                    fertilizerCount = x;
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("debug", new HUDActionCommand() {
            static final String help = "Toggle debug views on or off. Usage: debug on || debug off ";
            @Override
            public String execute(String[] cmd) {
                try {
                    String x = cmd[1];
                    if (!(Objects.equals(x, "on") || Objects.equals(x, "off"))) return help;
                    if (x.equals("on")) { DEBUG_borders = true; DEBUG_pathfinding = true; }
                    else if (x.equals("off")) { DEBUG_borders = false; DEBUG_pathfinding = false; }
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        // HUD Data
        hud.registerView("High Score:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(highScore);
            }
        });
        hud.registerView("Current Wave:", new HUDViewCommand(HUDViewCommand.Visibility.ALWAYS) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(currentWave);
            }
        });
        hud.registerView("Enemies Remaining:", new HUDViewCommand(HUDViewCommand.Visibility.ALWAYS) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString((currentWave+2-loggerSpawnCount+liveLoggers.size()));
            }
        });

        // we're adding an input processor AFTER the HUD has been created,
        // so we need to be a bit careful here and make sure not to clobber
        // the HUD's input controls. Do that by using an InputMultiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        // let the HUD's input processor handle things first....
        multiplexer.addProcessor(Gdx.input.getInputProcessor());
        // idk if this is necessary if im not using keyboard input but whatever who cares
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (hud.isOpen()) { return false; }
                if (keycode == Input.Keys.ESCAPE) {
                    activateSummonButtons();
                    towerBeingUpgraded = null;
                    cancelSummonBooleans();
                    return true;
                }
                else if (keycode == Input.Keys.Q) {
                    // if its an upgrade
                    cancelSummonBooleans();
                    if (towerBeingUpgraded != null) {
                        if (buttonFunc(upgradeButtonFuncs[0])) {
                            // success, make the happy sound :)
                            confirmedSfx.play();
                        }
                        else {
                            // then there isn't enough resources to do this command, play the out of fertilizer sound
                            notAllowedSfx.play();
                        }
                    }
                    // else its a summon
                    else {
                        if (buttonFunc(summonButtonFuncs[0])) {
                            // success, make the happy sound :)
                            confirmedSfx.play();
                        }
                        else {
                            // then there isn't enough resources to do this command, play the out of fertilizer sound

                            notAllowedSfx.play();
                        }
                    }
                }
                else if (keycode == Input.Keys.W) {
                    // if its an upgrade
                    cancelSummonBooleans();
                    if (towerBeingUpgraded != null) {
                        if (buttonFunc(upgradeButtonFuncs[1])) {
                            // success, make the happy sound :)

                            confirmedSfx.play();
                        }
                        else {
                            // then there isn't enough resources to do this command, play the out of fertilizer sound

                            notAllowedSfx.play();
                        }
                    }
                    // else its a summon
                    else {
                        if (buttonFunc(summonButtonFuncs[1])) {
                            // success, make the happy sound :)

                            confirmedSfx.play();
                        }
                        else {
                            // then there isn't enough resources to do this command, play the out of fertilizer sound

                            notAllowedSfx.play();
                        }
                    }
                }
                else if (keycode == Input.Keys.E) {
                    // if its an upgrade
                    cancelSummonBooleans();
                    if (towerBeingUpgraded != null) {
                        if (buttonFunc(upgradeButtonFuncs[2])) {
                            // success, make the happy sound :)

                            confirmedSfx.play();
                        }
                        else {
                            // then there isn't enough resources to do this command, play the out of fertilizer sound

                            notAllowedSfx.play();
                        }
                    }
                    // else its a summon
                    else {
                        if (buttonFunc(summonButtonFuncs[2])) {
                            // success, make the happy sound :)

                            confirmedSfx.play();
                        }
                        else {
                            // then there isn't enough resources to do this command, play the out of fertilizer sound

                            notAllowedSfx.play();
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // first, see which button it hit (if any). towers are kind of buttons!
                // if we click somewhere that isn't a button (and we aren't placing anything)
                // then we should swap ensure we activate summon buttons.
                // stupid coordinate system is backwards so lets fix that
                screenY = 928-screenY;
                int gridX = screenX / TILE_SIZE;
                int gridY = screenY / TILE_SIZE;
                CSButton buttonHit = null;
                Tower towerHit = null;
                String func = null;
                for (CSButton upgradeButton : upgradeButtons) {
                    if (upgradeButton.isActive() && screenX > upgradeButton.getX() && screenX < upgradeButton.getX() + upgradeButton.getWidth() && screenY > upgradeButton.getY() && screenY < upgradeButton.getY() + upgradeButton.getHeight()) {
                        buttonHit = upgradeButton;
                        func = upgradeButtonFuncs[buttonHit.buttonNum];
                    }
                }
                for (CSButton summonButton : summonButtons) {
                    if (summonButton.isActive() && screenX > summonButton.getX() && screenX < summonButton.getX() + summonButton.getWidth() && screenY > summonButton.getY() && screenY < summonButton.getY() + summonButton.getHeight()) {
                        buttonHit = summonButton;
                        func = summonButtonFuncs[buttonHit.buttonNum];
                    }
                }
                for (Tower tower: towers) {
                    if (screenX > tower.getX() && screenX < tower.getX() + tower.getWidth() && screenY > tower.getY() && screenY < tower.getY() + tower.getHeight()) {
                        towerHit = tower;
                    }
                }

                // if a button was clicked, do the function
                // if a tower was clicked, go to upgrade screen and set that tower to the one being upgraded
                // if nothing was clicked, go back to summon screen and set towerBeingUpgraded to null
                if (buttonHit != null) {
                    if (buttonFunc(func)) {
                        // success, make the happy sound :)

                        confirmedSfx.play();
                    }
                    else {
                        // then there isn't enough resources to do this command, play the out of fertilizer sound

                        notAllowedSfx.play();
                    }
                }
                // if this fails, perhaps we make it a boolean and then it will play a sound.
                // also probably a success would make a sound
                else if (towerHit != null) { activateUpgradeButtons(); towerBeingUpgraded = towerHit; cancelSummonBooleans(); }
                else if (gridX >= GRID_SIZE) {
                    // hitting nothing is the same as hitting escape
                    activateSummonButtons();
                    towerBeingUpgraded = null;
                    cancelSummonBooleans();
                    return false;
                }
                // there's another option: we clicked a space after hitting a summon button to summon!
                else if (summoningTree) {
                    if (createTree(gridX, gridY)) {

                        confirmedSfx.play();
                    }
                    else {
                        // clicked on an invalid space!

                        notAllowedSfx.play();
                    }
                }
                else if (summoningBees) {
                    if (createBees(gridX, gridY)) {

                        confirmedSfx.play();
                    }
                    else {

                        notAllowedSfx.play();
                    }
                }
                else if (summoningTower) {
                    if (createTower(gridX, gridY)) {

                        confirmedSfx.play();
                    }
                    else {

                        notAllowedSfx.play();
                    }
                }
                else {
                    // hitting nothing is the same as hitting escape
                    activateSummonButtons();
                    towerBeingUpgraded = null;
                    cancelSummonBooleans();
                    return false;
                }
                return true;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
    }

    public void update(float delta) {
        if (hud.isOpen()) return;
        timer += delta;
        if (state == SubState.GAME_OVER && timer > 3.0f) {
            state = SubState.READY;
        }

        // spawn next logger if more remain to be spawned
        if ((loggerSpawnCount < currentWave+2) && (timer >= (loggerSpawnCount * time_between_spawns + time_between_spawns))) {
            spawnNextLogger();
        }

        // ignore key presses when console is open...
        if (!hud.isOpen()) {
            // clicking
        }
        // above is where upgrade and summons will go. If we summon something other than bees,
        // then that means we need to call our pathfinding function!
        // doPathfinding();
        // also if trees grow or get cut down we need to redo pathfinding immediately.
        for (ShrubTree shrubTree : shrubTrees) {
            if (shrubTree.update(delta)) {
                grid[shrubTree.gridX][shrubTree.gridY].setNewTerrain(terrains[3]);
                growSfx.play();
                doPathfinding();
            }
        }


        // check to see which towers shoot
        for (Iterator<Tower> towerIterator = towers.iterator(); towerIterator.hasNext();) {
            Tower tower = towerIterator.next();
            // check if loggers are on top of it
            for (Logger logger : liveLoggers) {
                if (logger.gridX == tower.gridX && logger.gridY == tower.gridY) {
                    // if the logger is already on top of the tower, then the tower takes damage and the logger dies
                    tower.takeDamage(logger.damage);
                    frogHurtSfx.play();
                    if (tower.getHealth() <= 0) { frogDieSfx.play(); } // play death sound before isDead true
                    logger.takeDamage(logger.damage); //guaranteed to kill logger
                }
            }
            if (tower.isDead) { towerIterator.remove(); doPathfinding(); continue; }
            // updates internal attack timers and maybe other stuff idk yet
            tower.update(delta);
            // if attack isn't ready OR nothing is in range, this method will return null
            Logger targetedLogger = tower.attack(liveLoggers);
            if (targetedLogger == null) { continue; }
            // if we actually can shoot, then shoot at that logger
            shootProjectile(tower.gridX, tower.gridY, targetedLogger);
            spitSfx.get(csGame.random.nextInt(4)).play();
        }

        // if all towers have been destroyed, RIP-- that's the game.
        if (towers.isEmpty()) { endGame(); }

        // shoot those arrows
        for (Iterator<FrogSpit> frogSpitIterator = frogSpits.iterator(); frogSpitIterator.hasNext();) {
            FrogSpit frogSpit = frogSpitIterator.next();
            if (frogSpit.update(delta, liveLoggers)) { frogSpitIterator.remove(); }
        }

        // move loggers and check if they're dead or not
        for (Iterator<Logger> loggerIterator = liveLoggers.iterator(); loggerIterator.hasNext();) {
            Logger logger = loggerIterator.next();
            int preUpdateTileNum = logger.getGridNumBeforeUpdating();
            int loggerTileNum = logger.getCurrGridNum();
            logger.changeMoveSpeed(grid[iVal(preUpdateTileNum)][jVal(preUpdateTileNum)].getTerrainCost());
            if (preUpdateTileNum != loggerTileNum) { // then we literally just left that tile!
                footstepSfx.play();
                // if we left shrub or tree, then chop it down and leave roots
                for (Iterator<ShrubTree> shrubTreeIterator = shrubTrees.iterator(); shrubTreeIterator.hasNext();) {
                    ShrubTree shrubTree = shrubTreeIterator.next();
                    if (shrubTree.gridX == iVal(preUpdateTileNum) && shrubTree.gridY == jVal(preUpdateTileNum)) {
                        shrubTreeIterator.remove();
                        chopSfx.play();
                        grid[iVal(preUpdateTileNum)][jVal(preUpdateTileNum)].setNewTerrain(terrains[1]);
                    }
                }
            }
            // if the logger steps on a bee, ruh roh!
            Bees possibleBee = beeGrid[iVal(loggerTileNum)][jVal(loggerTileNum)];
            if (possibleBee != null) {
                logger.takeDamage(possibleBee.damage);
                // bee dies too though :(
                beesSfx.play();
                beeGrid[iVal(loggerTileNum)][jVal(loggerTileNum)] = null;
            }
            char direction = getCheapestDirection(loggerTileNum);
            logger.update(delta, direction);
            if (logger.isDead()) {
                fertilizerCount = fertilizerCount + logger.damage; // stronger loggers drop more fertilizer!
                loggerDieSfx.play();
                loggerIterator.remove();
            }
        }
        // if all the loggers are dead (after they've all been spawned!), then the wave has been beaten.
        if (liveLoggers.isEmpty() && timer >= wave_time) { goNextWave(false); }

        // holograms for summoning

        if (summoningTree || summoningBees || summoningTower) {
            int xMousePos = Gdx.input.getX();
            int yMousePos = 928 - Gdx.input.getY();
            int gridX = xMousePos / TILE_SIZE;
            int gridY = yMousePos / TILE_SIZE;
            if (validateGridSpace(gridX, gridY)) {
                int summonCost = 9999;
                if (summoningTree && grid[gridX][gridY].terrain == terrains[1]) {
                    summonCost = summonCosts[0]-1;
                }
                else if (summoningTree) {
                    summonCost = summonCosts[0];
                }
                else if (summoningBees) {
                    summonCost = summonCosts[1];
                }
                else if (summoningTower) {
                    summonCost = summonCosts[2];
                }

                if (summonCost <= fertilizerCount) {
                    validateHolo.setColor(Color.GREEN);
                }
                else {
                    validateHolo.setColor(Color.RED);
                }
            } else {
                validateHolo.setColor(Color.RED);
            }
            validateHolo.setAlpha(0.5f);
            validateHolo.setPosition(gridX*TILE_SIZE, gridY*TILE_SIZE);
            if (summoningTree) { treeHolo.setPosition(gridX*TILE_SIZE, gridY*TILE_SIZE); }
            else { treeHolo.setPosition(-TILE_SIZE, -TILE_SIZE); }
            if (summoningBees) { beesHolo.setPosition(gridX*TILE_SIZE, gridY*TILE_SIZE); }
            else { beesHolo.setPosition(-TILE_SIZE, -TILE_SIZE); }
            if (summoningTower) { towerHolo.setPosition(gridX*TILE_SIZE, gridY*TILE_SIZE); }
            else { towerHolo.setPosition(-TILE_SIZE, -TILE_SIZE); }
        }
        else {
            validateHolo.setPosition(-TILE_SIZE, -TILE_SIZE);
            treeHolo.setPosition(-TILE_SIZE, -TILE_SIZE);
            beesHolo.setPosition(-TILE_SIZE, -TILE_SIZE);
            towerHolo.setPosition(-TILE_SIZE, -TILE_SIZE);
        }



    }


    @Override
    public void render(float delta) {
        update(delta);
        font.setColor(Color.WHITE);
        ScreenUtils.clear(0, 0, 0, 1);
        csGame.batch.begin();
        switch (state) {
            case GAME_OVER:
                csGame.batch.draw(csGame.am.get(CSGame.RSC_GAMEOVER_IMG, Texture.class), 200, 200);
                break;
            case READY:
                csGame.setScreen(new SplashScreen(csGame, false));
                break;
            case PLAYING:
                break;
        }

        // draw the tiles
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j].draw(csGame.batch);
                if (beeGrid[i][j] != null) {beeGrid[i][j].draw(csGame.batch); }
                if (DEBUG_pathfinding) {
                    font.draw(csGame.batch, Integer.toString(grid[i][j].getCurrentCost()), i*TILE_SIZE+10, j*TILE_SIZE+((float) TILE_SIZE /2));
                }
            }
        }

        // draw the entities
        for (Tower tower : towers) {
            tower.draw(csGame.batch);
        }
        for (FrogSpit frogSpit : frogSpits) {
            frogSpit.draw(csGame.batch);
        }

        for (Iterator<Logger> loggerIterator = liveLoggers.iterator(); loggerIterator.hasNext();) {
            Logger logger = loggerIterator.next();
            if (logger.isDead()) { loggerIterator.remove(); }
            else { logger.draw(csGame.batch); }
        }

        // draw UI elements
        if (DEBUG_borders) {
            csGame.batch.draw(csGame.am.get(CSGame.RSC_BORDERS_IMG, Texture.class), 0, 0);
        }
        csGame.batch.draw(csGame.am.get(CSGame.RSC_BACKGROUNDUI_IMG, Texture.class), 928, 0);
        font.draw(csGame.batch, Integer.toString(fertilizerCount), 1024, 28);
        int ii = -1;
        for (CSButton upgradeButton : upgradeButtons) {
            if (upgradeButton.isActive()) {
                ii++;
                upgradeButton.draw(csGame.batch);
                font.draw(csGame.batch, Integer.toString(towerBeingUpgraded.getUpgradeCost(upgradeButton.buttonNum)), upgradeButton.getX() + 100, upgradeButton.getY()+20);
                font.draw(csGame.batch, buttonKeyBinds[ii], upgradeButton.getX()-20, upgradeButton.getY()+20);
            }
        }
        for (CSButton summonButton : summonButtons) {
            if (summonButton.isActive()) {
                ii++;
                summonButton.draw(csGame.batch);
                font.draw(csGame.batch, Integer.toString(summonCosts[summonButton.buttonNum]), summonButton.getX() + 100, summonButton.getY()+20);
                font.draw(csGame.batch, buttonKeyBinds[ii], summonButton.getX()-20, summonButton.getY()+20);
            }
        }
        if (towerBeingUpgraded != null) {
            font.draw(csGame.batch, "Current Health: " + towerBeingUpgraded.getHealth(), 956,162 );
            font.draw(csGame.batch, "Current Range: " + towerBeingUpgraded.getRange(), 960,414 );
            font.draw(csGame.batch, "Current AtkSpd: " + formatter.format(towerBeingUpgraded.getAttackSpeed()), 944,666 );
        }

        // draw holograms
        validateHolo.draw(csGame.batch);
        treeHolo.draw(csGame.batch);
        beesHolo.draw(csGame.batch);
        towerHolo.draw(csGame.batch);

        font.setColor(Color.BLACK);
        hud.draw(csGame.batch);
        csGame.batch.end();
    }

    public void setTerrainTypes() {
        /*
         * 0 = Grass
         * 1 = Roots
         * 2 = Shrubs
         * 3 = Trees
         * 4 = Rocks
         * */
        terrains[0] = new Terrain(1, csGame.am.get(CSGame.RSC_GRASS_IMG, Texture.class));
        terrains[1] = new Terrain(2, csGame.am.get(CSGame.RSC_ROOTS_IMG, Texture.class));
        terrains[2] = new Terrain(3, csGame.am.get(CSGame.RSC_SHRUBS_IMG, Texture.class));
        terrains[3] = new Terrain(10, csGame.am.get(CSGame.RSC_TREES_IMG, Texture.class));
        terrains[4] = new Terrain(Integer.MAX_VALUE, csGame.am.get(CSGame.RSC_ROCKS_IMG, Texture.class));
    }

    public void setGrid() {
        // tiles
        for (int j = 0; j < GRID_SIZE; j++) {
            for (int i = 0; i < GRID_SIZE; i++) {
                grid[i][j] = new Tile(csGame, terrains[setupGrid[i][j]], j*GRID_SIZE+i);
                grid[i][j].setX(i * TILE_SIZE);
                grid[i][j].setY(j * TILE_SIZE);
            }
        }
        Tower tower = new Tower(csGame, GRID_SIZE/2, GRID_SIZE/2);
        towers.add(tower);
    }

    public void getSetupGrid() {
        /*
        * 0 = Grass
        * 1 = Roots
        * 2 = Shrubs
        * 3 = Trees
        * 4 = Rocks
        * */
        setupGrid = new int[][]{
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
                {0, 1, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 4, 4, 0, 0, 0},
                {0, 0, 0, 2, 0, 0, 4, 0, 0, 0, 0, 0, 4, 0, 4, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 0, 0, 0, 4, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 4, 0, 4, 0, 0, 0, 0, 3, 4, 0, 4, 0, 0, 0, 0, 0, 4, 0, 4, 0, 0, 1, 1, 2, 0},
                {0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0},
                {0, 0, 0, 3, 0, 0, 0, 0, 4, 4, 0, 0, 4, 0, 4, 0, 1, 4, 0, 0, 2, 1, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 4, 0, 0, 0, 0, 4, 4, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 3, 0},
                {0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0},
                {0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 3, 0, 0, 0, 0, 0, 4, 4, 4, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 4, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 4, 0},
                {0, 4, 4, 0, 3, 0, 0, 0, 4, 4, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 4, 0},
                {0, 0, 4, 0, 0, 0, 0, 0, 0, 4, 0, 4, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 4, 1, 0, 0, 0, 0, 0},
                {0, 0, 4, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 4, 4, 0, 0, 4, 0, 0, 0},
                {0, 0, 0, 0, 4, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 1, 0, 0, 4, 4, 4, 0},
                {0, 0, 0, 0, 0, 0, 4, 4, 0, 2, 0, 0, 0, 4, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 4, 4, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0},
                {0, 4, 4, 4, 0, 3, 0, 0, 2, 1, 0, 0, 0, 0, 0, 2, 0, 0, 4, 0, 4, 0, 0, 0, 4, 0, 0, 0, 0},
                {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 1, 0, 0, 0, 4, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 4, 4, 4, 0, 0, 0, 1, 0, 0, 0, 0, 1, 4, 0, 4, 0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 0},
                {0, 0, 0, 1, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 1, 0, 4, 0},
                {0, 0, 0, 0, 1, 4, 4, 0, 4, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 4, 0, 4, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 4, 4, 4, 0, 4, 0},
                {0, 0, 0, 3, 0, 4, 4, 0, 0, 0, 4, 0, 0, 0, 4, 3, 1, 0, 4, 0, 4, 0, 0, 0, 4, 4, 0, 4, 0},
                {0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 4, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0},
        };
    }

    public void spawnNextLogger() {
        Logger logger = totalLoggers.get(loggerSpawnCount);
        int spawnGridY = 0;
        int spawnGridX = 0;
        if(csGame.random.nextBoolean()) {
            // half the loggers spawn along the top and bottom rows
            spawnGridX = csGame.random.nextInt(GRID_SIZE);
            spawnGridY = csGame.random.nextInt(2) * (GRID_SIZE-1);
        }
        else {
            // and half along the left and right columns
            spawnGridY = csGame.random.nextInt(GRID_SIZE);
            spawnGridX = csGame.random.nextInt(2) * (GRID_SIZE-1);
        }
        loggerSpawnCount++;
        liveLoggers.add(logger.makeCopy(csGame, spawnGridX, spawnGridY));
    }

    public void resetWaves() {
        currentWave = 0;
        totalLoggers = new ArrayList<Logger>(3+NUM_WAVES);
        // wave 1 will start with 3 total lumberjacks, so add two immediately
        totalLoggers.add(new Lumberjack(csGame, 0, 0));
        totalLoggers.add(new Lumberjack(csGame, 0, 0));

        killAllLoggers();
        wave_time = 6;
    }

    public void killAllLoggers() {
        liveLoggers = new ArrayList<Logger>(3+NUM_WAVES);
    }

    public void goNextWave(boolean skipWave) {
        currentWave++;
        // Add the correct type of logger based on the current wave
        // Every wave adds a logger: usually a lumberjack, but every fifth
        // wave is a bulldozer and every eighth wave is a shredder.
        // Additionally, every wave increases the total time of spawning by 1 second.
        // This means that the time between logger spawns slowly decreases, approaching but never hitting 1 per second.
        wave_time = wave_time+0.5f;
        timer = 0;
        loggerSpawnCount = 0;
        time_between_spawns = wave_time / totalLoggers.size();
        if (currentWave % 8 == 0) {
            totalLoggers.add(new Shredder(csGame, 0, 0));
        }
        else if (currentWave % 5 == 0) {
            totalLoggers.add(new Bulldozer(csGame, 0, 0));
        }
        else {
            totalLoggers.add(new Lumberjack(csGame, 0, 0));
        }
        if (skipWave) { timer = wave_time + 1; loggerSpawnCount =  currentWave+2; }
    }

    public void shootProjectile(int towerGridX, int towerGridY, Logger logger) {
        frogSpits.add(new FrogSpit(csGame, towerGridX, towerGridY, logger));
    }

    private char getCheapestDirection(int loggerTileNum) {
        int cheapestDirectionTileNum = grid[iVal(loggerTileNum)][jVal(loggerTileNum)].nextTileNum;
        if (cheapestDirectionTileNum == loggerTileNum-1) { return 'L'; }
        else if (cheapestDirectionTileNum == loggerTileNum+1) { return 'R'; }
        else if (cheapestDirectionTileNum == loggerTileNum-GRID_SIZE) { return 'D'; }
        else if (cheapestDirectionTileNum == loggerTileNum+GRID_SIZE) { return 'U'; }
        return 'X'; // this should never happen
    }

    public void doPathfinding() {
        Queue<Integer> vertexQueue = new LinkedList<Integer>();
        for (int j = 0; j < GRID_SIZE; j++) {
            for (int i = 0; i < GRID_SIZE; i++) {
                grid[i][j].setCurrentCost(Integer.MAX_VALUE);
            }
        }

        // this will repeat for every tower but for now lets just do one!
        for (Tower tower : towers) {
            boolean[] visited = new boolean[GRID_SIZE*GRID_SIZE];
            grid[tower.gridX][tower.gridY].setCurrentCost(0);
            vertexQueue.add(grid[tower.gridX][tower.gridY].tileNum);
            while (!vertexQueue.isEmpty()) {
                int vertexNum = vertexQueue.remove();
                ArrayList<Integer> adjTileNums = grid[iVal(vertexNum)][jVal(vertexNum)].adjTileNums;
                for (Integer wTileNum : adjTileNums) {
                    if (grid[iVal(wTileNum)][jVal(wTileNum)].getTerrainCost() == Integer.MAX_VALUE) {
                        continue;
                    } // this is to prevent overflow
                    if (grid[iVal(vertexNum)][jVal(vertexNum)].getCurrentCost() +
                            grid[iVal(wTileNum)][jVal(wTileNum)].getTerrainCost() <
                            grid[iVal(wTileNum)][jVal(wTileNum)].getCurrentCost()) {
                        grid[iVal(wTileNum)][jVal(wTileNum)].setCurrentCost(grid[iVal(vertexNum)][jVal(vertexNum)].getCurrentCost() +
                                grid[iVal(wTileNum)][jVal(wTileNum)].getTerrainCost());
                        grid[iVal(wTileNum)][jVal(wTileNum)].nextTileNum = vertexNum;
                    }
                    if (!visited[wTileNum]) {
                        visited[wTileNum] = true;
                        vertexQueue.add(wTileNum);
                    }
                }
            }
        }
    }
    private int iVal(int tileNum) {return tileNum % GRID_SIZE; }
    private int jVal(int tileNum) {return tileNum / GRID_SIZE; }


    /**
     * UPGRADES:
     * <p>
     * 0 = HealthUpButton
     * <p>
     * 1 = RangeUpButton
     * <p>
     * 2 = AtkSpdUpButton
     * <p>
     * SUMMONS:
     * <p>
     * 0 = SummonTreeButton
     * <p>
     * 1 = SummonBeesButton
     * <p>
     * 2 = SummonTowerButton
     * */
    public void bindButtons() {

        final int NUM_BUTTONS_ON_UPGRADE_SCREEN = 3;
        final Texture[] UPGRADE_TEXTURES = new Texture[] {
                csGame.am.get(CSGame.RSC_HEALTHUPBUTTON_IMG, Texture.class),
                csGame.am.get(CSGame.RSC_RANGEUPBUTTON_IMG, Texture.class),
                csGame.am.get(CSGame.RSC_ATKSPDUPBUTTON_IMG, Texture.class),
        };
        upgradeButtonFuncs = new String[] {
                "upgradeHealth",
                "upgradeRange",
                "upgradeAtkSpd",
        };
        // upgrade costs are based on each individual tower
        final int NUM_BUTTONS_ON_SUMMON_SCREEN = 3;
        final Texture[] SUMMON_TEXTURES = new Texture[] {
                csGame.am.get(CSGame.RSC_SUMMONTREEBUTTON_IMG, Texture.class),
                csGame.am.get(CSGame.RSC_SUMMONBEESBUTTON_IMG, Texture.class),
                csGame.am.get(CSGame.RSC_SUMMONTOWERBUTTON_IMG, Texture.class),
        };
        summonButtonFuncs = new String[] {
                "summonTree",
                "summonBees",
                "summonTower",
        };
        summonCosts = new int[] {
                4,
                3,
                12,
        };
        buttonKeyBinds = new String[] {
                "Q",
                "W",
                "E",
        };

        final float WIDTH_OF_BUTTONS = 128;
        final float HEIGHT_OF_BUTTONS = 128;
        final float WIDTH_OF_TOTAL_BUTTON_AREA = 178; // -7 on each side
        final float HEIGHT_OF_TOTAL_BUTTON_AREA = 882; // -7 on each side -32 fertilizer count
        final float X_OF_BUTTON_AREA = 935; // game area + 7
        final float Y_OF_BUTTON_AREA = 39; // 7 black bar + 32 fertilizer count
        final float X_OF_BUTTONS = (X_OF_BUTTON_AREA + (WIDTH_OF_TOTAL_BUTTON_AREA/2) - (WIDTH_OF_BUTTONS/2));
        float yBetweenButtons;
        float yOfFirstButton;
        float spaceBetweenButtons;

        upgradeButtons = new ArrayList<CSButton>(NUM_BUTTONS_ON_UPGRADE_SCREEN);
        spaceBetweenButtons = (HEIGHT_OF_TOTAL_BUTTON_AREA - (NUM_BUTTONS_ON_UPGRADE_SCREEN * HEIGHT_OF_BUTTONS)) / (NUM_BUTTONS_ON_UPGRADE_SCREEN + 1);
        yBetweenButtons = spaceBetweenButtons + HEIGHT_OF_BUTTONS;
        yOfFirstButton = spaceBetweenButtons + Y_OF_BUTTON_AREA;
        for (int ii = 0; ii < NUM_BUTTONS_ON_UPGRADE_SCREEN; ii++) {
            upgradeButtons.add(new CSButton(UPGRADE_TEXTURES[ii], X_OF_BUTTONS, yOfFirstButton +(yBetweenButtons *ii), ii));
        }

        summonButtons = new ArrayList<CSButton>(NUM_BUTTONS_ON_SUMMON_SCREEN);
        spaceBetweenButtons = (HEIGHT_OF_TOTAL_BUTTON_AREA - (NUM_BUTTONS_ON_SUMMON_SCREEN * HEIGHT_OF_BUTTONS)) / (NUM_BUTTONS_ON_SUMMON_SCREEN + 1);
        yBetweenButtons = spaceBetweenButtons + HEIGHT_OF_BUTTONS;
        yOfFirstButton = spaceBetweenButtons + Y_OF_BUTTON_AREA;
        for (int ii = 0; ii < NUM_BUTTONS_ON_SUMMON_SCREEN; ii++) {
            summonButtons.add(new CSButton(SUMMON_TEXTURES[ii], X_OF_BUTTONS, yOfFirstButton +(yBetweenButtons *ii), ii));
        }

        activateSummonButtons(); // these are on by default; clicking a tower should swap it to upgrade

    }

    public void activateSummonButtons() {
        for (CSButton button : summonButtons) {
            button.activate();
        }
        for (CSButton button : upgradeButtons) {
            button.deactivate();
        }
    }

    public void activateUpgradeButtons() {
        for (CSButton button : upgradeButtons) {
            button.activate();
        }
        for (CSButton button : summonButtons) {
            button.deactivate();
        }
    }

    public boolean upgradeHealth() {
        int newFertilizerCount = towerBeingUpgraded.upgradeHealth(fertilizerCount);
        if (newFertilizerCount >= 0) {
            fertilizerCount = newFertilizerCount;
            return true;
        }
        else { return false; }
    }

    public boolean upgradeRange() {
        int newFertilizerCount = towerBeingUpgraded.upgradeRange(fertilizerCount);
        if (newFertilizerCount >= 0) {
            fertilizerCount = newFertilizerCount;
            return true;
        }
        else { return false; }
    }

    public boolean upgradeAtkSpd() {
        int newFertilizerCount = towerBeingUpgraded.upgradeAtkSpd(fertilizerCount);
        if (newFertilizerCount >= 0) {
            fertilizerCount = newFertilizerCount;
            return true;
        }
        else { return false; }
    }

    public boolean summonTree() {
        cancelSummonBooleans();
        summoningTree = true;
        return true;
    }

    public boolean summonBees() {
        cancelSummonBooleans();
        summoningBees = true;
        return true;
    }

    public boolean summonTower() {
        cancelSummonBooleans();
        summoningTower = true;
        return true;
    }

    public boolean createTree(int gridX, int gridY) {
        if (!validateGridSpace(gridX, gridY)) { return false; }
        int summonTreeCost = summonCosts[0];
        if (grid[gridX][gridY].terrain == terrains[1]) { summonTreeCost--; } // get a little refund if planting on roots
        int newFertilizerCount = fertilizerCount-summonTreeCost;
        if (newFertilizerCount < 0) return false;
        shrubTrees.add(new ShrubTree(gridX, gridY));
        grid[gridX][gridY].setNewTerrain(terrains[2]);
        doPathfinding();
        fertilizerCount -= summonTreeCost;
        return true;
    }

    public boolean createBees(int gridX, int gridY) {
        int newFertilizerCount = fertilizerCount-summonCosts[1];
        if (newFertilizerCount < 0) return false;
        if (!validateGridSpace(gridX, gridY)) { return false; }
        beeGrid[gridX][gridY] = new Bees(csGame, gridX, gridY);
        beesSfx.play();
        fertilizerCount -= summonCosts[1];
        return true;
    }

    public boolean createTower(int gridX, int gridY) {
        int newFertilizerCount = fertilizerCount-summonCosts[2];
        if (newFertilizerCount < 0) return false;
        if (!validateGridSpace(gridX, gridY)) { return false; }
        towers.add(new Tower(csGame, gridX, gridY));
        doPathfinding();
        fertilizerCount -= summonCosts[2];
        summonCosts[2] += 4;
        return true;
    }

    public boolean validateGridSpace(int gridX, int gridY) {
        // can't summon unless it's on the actual grid!
        if (gridX >= GRID_SIZE || gridY >= GRID_SIZE || gridX < 0 || gridY < 0) return false;
        // can't summon on shrubs or trees or rocks:
        if (grid[gridX][gridY].terrain == terrains[2] || grid[gridX][gridY].terrain == terrains[3] || grid[gridX][gridY].terrain == terrains[4]) { return false; }
        // cannot summon on top of bees or loggers or towers-- not sure how to do bees just yet!
        // loggers:
        for (Logger logger : liveLoggers) {
            if (logger.gridX == gridX && logger.gridY == gridY) { return false; }
        }
        // towers:
        if (grid[gridX][gridY].getCurrentCost() == 0) { return false; }
        // bees:
        if (beeGrid[gridX][gridY] != null) { return false; }
        // otherwise, this grid is fine to go on!
        return true;
    }
    public boolean buttonFunc(String func) {
        switch (func) {
            case "upgradeHealth":
                return upgradeHealth();
            case "upgradeRange":
                return upgradeRange();
            case "upgradeAtkSpd":
                return upgradeAtkSpd();
            case "summonTree":
                return summonTree();
            case "summonBees":
                return summonBees();
            case "summonTower":
                return summonTower();
            default:
                System.out.println("you just hit a fake button!");
                return false;
        }
    }

    public void cancelSummonBooleans() {
        summoningTree = false;
        summoningBees = false;
        summoningTower = false;
    }

    public void endGame() {
        if (fertilizerCount > highScore) {
            FileHandle file = Gdx.files.local("highscore.txt");
            file.writeString(Integer.toString(currentWave-1), false);  // write the high score
        }
        csGame.setScreen(new VictoryScreen(csGame, currentWave-1));
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
    }

}