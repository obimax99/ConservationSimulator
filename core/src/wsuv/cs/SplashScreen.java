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
    float timer;
    boolean showingIntroText;

    public SplashScreen(CSGame game, boolean showIntroText) {
        csGame = game;
        timer = 0.0f;
        this.showingIntroText = showIntroText;
        if (!showIntroText) {
            timer = 30.0f;
        }
    }

    @Override
    public void show() {
        Gdx.app.log("SplashScreen", "show");
    }

    public void update(float delta) {
        timer += delta;
        if (showingIntroText) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || timer >= 30.0f) {
                showingIntroText = false;
                csGame.music.play();
            }
        }
        else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                csGame.setScreen(new PlayScreen(csGame));
            }
        }
    }

    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0, 0, 0, 1);
        font = csGame.am.get(CSGame.RSC_MONO_FONT);
        font.setColor(Color.WHITE);
        csGame.batch.begin();
        if (showingIntroText) {
            csGame.batch.draw(csGame.am.get(CSGame.RSC_INTRO_IMG, Texture.class), 0, Gdx.graphics.getHeight()/2-(csGame.am.get(CSGame.RSC_INTRO_IMG, Texture.class).getHeight()/2));
        }
        else {
            csGame.batch.draw(csGame.am.get(CSGame.RSC_LOGO_IMG, Texture.class), Gdx.graphics.getWidth()/2-(csGame.am.get(CSGame.RSC_LOGO_IMG, Texture.class).getWidth()/2), Gdx.graphics.getHeight()/2-(csGame.am.get(CSGame.RSC_LOGO_IMG, Texture.class).getHeight()/2));
            font.draw(csGame.batch, "By Max Greener", 100, 400);
            csGame.batch.draw(csGame.am.get(CSGame.RSC_PRESSAKEY_IMG, Texture.class), Gdx.graphics.getWidth()/2-(csGame.am.get(CSGame.RSC_PRESSAKEY_IMG, Texture.class).getWidth()/2), Gdx.graphics.getHeight()/4-(csGame.am.get(CSGame.RSC_PRESSAKEY_IMG, Texture.class).getHeight()/2));
        }
        csGame.batch.end();
    }
}
