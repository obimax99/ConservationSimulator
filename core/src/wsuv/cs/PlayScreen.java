package wsuv.cs;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Iterator;

import static wsuv.cs.Constants.*;

public class PlayScreen extends ScreenAdapter {
    // things that don't require logic
    private enum SubState {READY, GAME_OVER, PLAYING}
    private CSGame csGame;
    private HUD hud;
    private SubState state;
    private int highScore;
    private int currentWave;
    private Terrain[] terrains;
    private Tile[][] grid;
    private int[][] setupGrid;

    private float timer;
    private float wave_time;
    private float time_between_spawns;
    private ArrayList<Logger> liveLoggers;
    private ArrayList<Logger> totalLoggers;
    private int loggerSpawnCount;

    // things that require logic
    private ArrayList<Tower> towers;


    public PlayScreen(CSGame game) {
        timer = 0;
        csGame = game;
        state = SubState.PLAYING;
        hud = new HUD(csGame.am.get(CSGame.RSC_MONO_FONT));
        FileHandle file = Gdx.files.internal("highscore.txt");
        highScore = Integer.parseInt(file.readString());
        terrains = new Terrain[NUM_TERRAINS];
        setTerrainTypes();
        getSetupGrid();
        grid = new Tile[GRID_SIZE][GRID_SIZE];
        towers = new ArrayList<>(1);
        setGrid();

        resetWaves();
        goNextWave(false);

        // the HUD will show FPS always, by default.  Here's how
        // to use the HUD interface to silence it (and other HUD Data)
        hud.setDataVisibility(HUDViewCommand.Visibility.WHEN_OPEN);

        // HUD Console Commands
        hud.registerAction("wave", new HUDActionCommand() {
            static final String help = "Usage: wave <x> ";

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

        // check to see which towers shoot
        for (Tower tower : towers) {
            // updates internal attack timers and maybe other stuff idk yet
            tower.update(delta);
            // if attack isn't ready OR nothing is in range, this method will return null
            Logger targetedLogger = tower.attack(liveLoggers);
            if (targetedLogger == null) { continue; }
            // if we actually can shoot, then shoot at that logger
            shootProjectile(tower.row, tower.col, targetedLogger.row, targetedLogger.col);
        }

        // shoot those arrows

        // move loggers and check if they're dead or not
        for (Iterator<Logger> loggerIterator = liveLoggers.iterator(); loggerIterator.hasNext();) {
            Logger logger = loggerIterator.next();
            // move loggers with pathfinding
            logger.update();
            // checking collisions here? loggers are responsible for taking damage from bees!
            if (logger.isDead()) { loggerIterator.remove(); }
        }
        // if all the loggers are dead (after they've all been spawned!), then the wave has been beaten.
        if (liveLoggers.isEmpty() && timer >= wave_time) { goNextWave(false); }

    }

    @Override
    public void render(float delta) {
        update(delta);

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
            }
        }

        // draw the entities
        for (Tower tower : towers) {
            tower.draw(csGame.batch);
        }

        for (Iterator<Logger> loggerIterator = liveLoggers.iterator(); loggerIterator.hasNext();) {
            Logger logger = loggerIterator.next();
            if (logger.isDead()) { loggerIterator.remove(); }
            else { logger.draw(csGame.batch); }
        }

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
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = new Tile(terrains[setupGrid[i][j]]);
                grid[i][j].setX(i * 32);
                grid[i][j].setY(j * 32);
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
                {0, 0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 4, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
    }

    public void spawnNextLogger() {
        Logger logger = totalLoggers.get(loggerSpawnCount);
        int spawnRow = 0;
        int spawnCol = 0;
        if(csGame.random.nextBoolean()) {
            // half the loggers spawn along the top and bottom rows
            spawnRow = csGame.random.nextInt(2) * (GRID_SIZE-1);
            spawnCol = csGame.random.nextInt(GRID_SIZE);
        }
        else {
            // and half along the left and right columns
            spawnCol = csGame.random.nextInt(2) * (GRID_SIZE-1);
            spawnRow = csGame.random.nextInt(GRID_SIZE);
        }
        loggerSpawnCount++;
        liveLoggers.add(logger.makeCopy(csGame, spawnRow, spawnCol));
        System.out.println(spawnRow + " " + spawnCol);
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

    public void shootProjectile(int towerRow, int towerCol, int loggerRow, int loggerCol) {
        System.out.println("shoot");
    }
}