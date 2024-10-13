package wsuv.cs;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

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

    private ArrayList<CSButton> upgradeButtons;
    private String[] upgradeButtonFuncs;
    private Tower towerBeingUpgraded;
    private ArrayList<CSButton> summonButtons;
    private int[] summonCosts;
    private String[] summonButtonFuncs;
    private int fertilizerCount;
    private boolean summoningTree;
    private ArrayList<ShrubTree> shrubTrees;
    private boolean summoningBees;
    private boolean summoningTower;

    public PlayScreen(CSGame game) {
        timer = 0;
        csGame = game;
        state = SubState.PLAYING;
        DEBUG_pathfinding = true;
        DEBUG_borders = true;
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
        fertilizerCount = 500; // for now
        shrubTrees = new ArrayList<ShrubTree>(16);

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

        // we're adding an input processor AFTER the HUD has been created,
        // so we need to be a bit careful here and make sure not to clobber
        // the HUD's input controls. Do that by using an InputMultiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        // let the HUD's input processor handle things first....
        multiplexer.addProcessor(Gdx.input.getInputProcessor());
        // idk if this is necessary if im not using keyboard input but whatever who cares
        multiplexer.addProcessor(new InputAdapter() {
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
                if (buttonHit != null) { buttonFunc(func); }
                // if this fails, perhaps we make it a boolean and then it will play a sound.
                // also probably a success would make a sound
                else if (towerHit != null) { activateUpgradeButtons(); towerBeingUpgraded = towerHit; }
                // there's another option: we clicked a space after hitting a summon button to summon!
                else if (summoningTree) { createTree(gridX, gridY); }
                else if (summoningBees) { createBees(gridX, gridY); }
                else if (summoningTower) { createTower(gridX, gridY); }
                else {
                    activateSummonButtons();
                    towerBeingUpgraded = null;
                    summoningTree = false;
                    summoningBees = false;
                    summoningTower = false;
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
        if ((loggerSpawnCount < currentWave+2) && (timer >= (loggerSpawnCount * time_between_spawns))) {
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
                System.out.println(grid[shrubTree.gridX][shrubTree.gridY].terrain);
                grid[shrubTree.gridX][shrubTree.gridY].setNewTerrain(terrains[3]);
                doPathfinding();
            }
        }


        // check to see which towers shoot
        for (Tower tower : towers) {
            // updates internal attack timers and maybe other stuff idk yet
            tower.update(delta);
            // if attack isn't ready OR nothing is in range, this method will return null
            Logger targetedLogger = tower.attack(liveLoggers);
            if (targetedLogger == null) { continue; }
            // if we actually can shoot, then shoot at that logger
            shootProjectile(tower.gridX, tower.gridY, targetedLogger);
        }

        // shoot those arrows
        for (Iterator<FrogSpit> frogSpitIterator = frogSpits.iterator(); frogSpitIterator.hasNext();) {
            FrogSpit frogSpit = frogSpitIterator.next();
            if (frogSpit.update(delta)) { frogSpitIterator.remove(); }
        }

        // move loggers and check if they're dead or not
        for (Iterator<Logger> loggerIterator = liveLoggers.iterator(); loggerIterator.hasNext();) {
            Logger logger = loggerIterator.next();
            int loggerTileNum = logger.getCurrGridNum();
            // if the logger steps on a bee, ruh roh!
            Bees possibleBee = beeGrid[iVal(loggerTileNum)][jVal(loggerTileNum)];
            if (possibleBee != null) {
                logger.takeDamage(possibleBee.damage);
                // bee dies too though :(
                beeGrid[iVal(loggerTileNum)][jVal(loggerTileNum)] = null;
            }
            char direction = getCheapestDirection(loggerTileNum);
            logger.update(delta, direction);
            // checking collisions here? loggers are responsible for taking damage from bees!
            if (logger.isDead()) {
                fertilizerCount = fertilizerCount + logger.damage; // stronger loggers drop more fertilizer!
                loggerIterator.remove();
            }
        }
        // if all the loggers are dead (after they've all been spawned!), then the wave has been beaten.
        if (liveLoggers.isEmpty() && timer >= wave_time) { goNextWave(false); }

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
                csGame.setScreen(new SplashScreen(csGame));
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
        for (CSButton upgradeButton : upgradeButtons) {
            if (upgradeButton.isActive()) {
                upgradeButton.draw(csGame.batch);
                font.draw(csGame.batch, Integer.toString(towerBeingUpgraded.getUpgradeCost(upgradeButton.buttonNum)), upgradeButton.getX() + 100, upgradeButton.getY()+20);
            }
        }
        for (CSButton summonButton : summonButtons) {
            if (summonButton.isActive()) {
                summonButton.draw(csGame.batch);
                font.draw(csGame.batch, Integer.toString(summonCosts[summonButton.buttonNum]), summonButton.getX() + 100, summonButton.getY()+20);
            }
        }
        if (towerBeingUpgraded != null) {
            font.draw(csGame.batch, "Current Health: " + towerBeingUpgraded.getHealth(), 956,162 );
            font.draw(csGame.batch, "Current Range: " + towerBeingUpgraded.getRange(), 960,414 );
            font.draw(csGame.batch, "Current AtkSpd: " + formatter.format(towerBeingUpgraded.getAttackSpeed()), 944,666 );
        }

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
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0},
                {0, 0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 4, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0},
                {0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0},
                {0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
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
        wave_time = 10;
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
        wave_time++;
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

    public void upgradeHealth() {
        int newFertilizerCount = towerBeingUpgraded.upgradeHealth(fertilizerCount);
        if (newFertilizerCount >= 0) {
            fertilizerCount = newFertilizerCount;
        }
    }

    public void upgradeRange() {
        int newFertilizerCount = towerBeingUpgraded.upgradeRange(fertilizerCount);
        if (newFertilizerCount >= 0) {
            fertilizerCount = newFertilizerCount;
        }
    }

    public void upgradeAtkSpd() {
        int newFertilizerCount = towerBeingUpgraded.upgradeAtkSpd(fertilizerCount);
        if (newFertilizerCount >= 0) {
            fertilizerCount = newFertilizerCount;
        }
    }

    public void summonTree() {
        int newFertilizerCount = fertilizerCount-summonCosts[0];
        if (newFertilizerCount < 0) return;
        summoningTree = true;
        summoningBees = false;
        summoningTower = false;
        fertilizerCount = newFertilizerCount;
    }

    public void summonBees() {
        int newFertilizerCount = fertilizerCount-summonCosts[1];
        if (newFertilizerCount < 0) return;
        summoningTree = false;
        summoningBees = true;
        summoningTower = false;
        fertilizerCount = newFertilizerCount;
    }

    public void summonTower() {
        int newFertilizerCount = fertilizerCount-summonCosts[2];
        if (newFertilizerCount < 0) return;
        summoningTree = false;
        summoningBees = false;
        summoningTower = true;
        fertilizerCount = newFertilizerCount;
    }

    public void createTree(int gridX, int gridY) {
        summoningTree = false;
        if (!validateGridSpace(gridX, gridY)) { return; }
        if (grid[gridX][gridY].terrain == terrains[1]) { fertilizerCount++; } // get a little refund if planting on roots
        shrubTrees.add(new ShrubTree(gridX, gridY));
        grid[gridX][gridY].setNewTerrain(terrains[2]);
        doPathfinding();
    }

    public void createBees(int gridX, int gridY) {
        summoningBees = false;
        if (!validateGridSpace(gridX, gridY)) { return; }
        beeGrid[gridX][gridY] = new Bees(csGame, gridX, gridY);
    }

    public void createTower(int gridX, int gridY) {
        summoningTower = false;
        if (!validateGridSpace(gridX, gridY)) { return; }
        towers.add(new Tower(csGame, gridX, gridY));
        doPathfinding();
    }

    public boolean validateGridSpace(int gridX, int gridY) {
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
    public void buttonFunc(String func) {
        switch (func) {
            case "upgradeHealth":
                upgradeHealth();
                break;
            case "upgradeRange":
                upgradeRange();
                break;
            case "upgradeAtkSpd":
                upgradeAtkSpd();
                break;
            case "summonTree":
                summonTree();
                break;
            case "summonBees":
                summonBees();
                break;
            case "summonTower":
                summonTower();
                break;
            default:
                System.out.println("you just hit a fake button!");
                break;
        }
    }



}