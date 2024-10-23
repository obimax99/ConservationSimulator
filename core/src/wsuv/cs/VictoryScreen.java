package wsuv.cs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class VictoryScreen extends ScreenAdapter {
    CSGame csGame;
    BitmapFont font;
    float timer;
    int score;

    public VictoryScreen(CSGame game, int playerScore) {
        csGame = game;
        font = csGame.am.get(CSGame.RSC_MONO_FONT);
        font.setColor(Color.WHITE);
        timer = 0;
        score = playerScore;
    }

    @Override
    public void show() {
        Gdx.app.log("VictoryScreen", "show");
    }

    public void update(float delta) {
        timer += delta;
        if ((timer >= 20.0) || (timer >= 5.0 && Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY))) {
            csGame.setScreen(new SplashScreen(csGame, false));
        }
    }

    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0, 0, 0, 1);
        csGame.batch.begin();
        font.setColor(Color.WHITE);
        font.draw(csGame.batch, "Final Score: ", 500, 200);
        font.draw(csGame.batch, Integer.toString(score), 600, 200);
        csGame.batch.draw(csGame.am.get(CSGame.RSC_GAMEOVER_IMG, Texture.class), Gdx.graphics.getWidth()/2-(csGame.am.get(CSGame.RSC_GAMEOVER_IMG, Texture.class).getWidth()/2), Gdx.graphics.getHeight()/2-(csGame.am.get(CSGame.RSC_GAMEOVER_IMG, Texture.class).getHeight()/2));
        font.draw(csGame.batch, "Your forest of friends lives on in our hearts <3", 375, 150);
        csGame.batch.end();
    }
}
