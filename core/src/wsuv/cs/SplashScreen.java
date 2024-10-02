package wsuv.cs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class SplashScreen extends ScreenAdapter {
    CSGame csGame;
    BitmapFont font;

    public SplashScreen(CSGame game) {
        csGame = game;
    }

    @Override
    public void show() {
        Gdx.app.log("SplashScreen", "show");
    }

    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            csGame.setScreen(new PlayScreen(csGame));
        }
    }

    public void render(float delta) {
        update();
        ScreenUtils.clear(0, 0, 0, 1);
        font = csGame.am.get(CSGame.RSC_MONO_FONT);
        font.setColor(Color.BLACK);
        csGame.batch.begin();
//        csGame.batch.draw(csGame.am.get(CSGame.RSC_BREAKOUT_IMG, Texture.class), 0, Gdx.graphics.getHeight()-(csGame.am.get(CSGame.RSC_BREAKOUT_IMG, Texture.class).getHeight()));
        font.draw(csGame.batch, "By Max Greener", 20, 400);
        csGame.batch.draw(csGame.am.get(CSGame.RSC_PRESSAKEY_IMG, Texture.class), 200, 150);
        csGame.batch.end();
    }
}
