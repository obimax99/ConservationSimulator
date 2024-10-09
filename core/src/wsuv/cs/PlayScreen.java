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

public class PlayScreen extends ScreenAdapter {
    // things that don't require logic
    private enum SubState {READY, GAME_OVER, PLAYING}
    private CSGame csGame;
    private ArrayList<String> waves;
    private final int NUM_WAVES = 150;
    private HUD hud;
    private SubState state;
    private float timer;
    private int highScore;
    private int currentWave;
    private Terrain[] terrains;
    private final int NUM_TERRAINS = 5;
    private final int GRID_SIZE = 29;
    private Tile[][] grid;
    private int[][] setupGrid;

    // things that require logic
    private ArrayList<Tower> towers;


    public PlayScreen(CSGame game) {
        timer = 0;
        csGame = game;
        state = SubState.PLAYING;
        hud = new HUD(csGame.am.get(CSGame.RSC_MONO_FONT));
        waves = new ArrayList<>(NUM_WAVES);
        currentWave = 1;
        FileHandle file = Gdx.files.internal("highscore.txt");
        highScore = Integer.parseInt(file.readString());
        terrains = new Terrain[NUM_TERRAINS];
        setTerrainTypes();
        getSetupGrid();
        grid = new Tile[GRID_SIZE][GRID_SIZE];
        towers = new ArrayList<>(1);
        setGrid();


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
                    if ((x > 20) || (x < 1)) return "Invalid Wave Number";
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
        // ignore key presses when console is open...
        if (!hud.isOpen()) {
            // clicking

        }
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
}