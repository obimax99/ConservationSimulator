package wsuv.cs;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class PlayScreen extends ScreenAdapter {
    private enum SubState {READY, GAME_OVER, PLAYING}

    private CSGame csGame;
    private final int numBrickSlots = 128;
    private ArrayList<String> levels;
    private final int numLevels = 15;
    private HUD hud;
    private SubState state;
    private int lives;
    private float timer;
    private int score;
    private int highScore;
    private int currentLevel;
    private float levelChangeTimer;
    private boolean beatLevel;

    public PlayScreen(CSGame game) {
        timer = 0;
        csGame = game;
        state = SubState.PLAYING;
        hud = new HUD(csGame.am.get(CSGame.RSC_MONO_FONT));
        levels = new ArrayList<>(numLevels);
        currentLevel = 1;
        lives = 3;
        score = 0;
        FileHandle file = Gdx.files.internal("highscore.txt");
        highScore = Integer.parseInt(file.readString());

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
                return Integer.toString(currentLevel);
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
        // this logic could also be pushed into a method on SubState enum
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
        hud.draw(csGame.batch);
        csGame.batch.end();
    }
}