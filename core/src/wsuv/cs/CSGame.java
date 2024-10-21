package wsuv.cs;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.Random;

public class CSGame extends Game {
    public static final String RSC_GAMEOVER_IMG = "gameover.png";
    public static final String RSC_PRESSAKEY_IMG = "pressakey.png";
    public static final String RSC_MONO_FONT_FILE = "JetBrainsMono-Regular.ttf";
    public static final String RSC_MONO_FONT = "JBM.ttf";

    public static final String RSC_FROGTOWER_IMG = "frogTower.png";
    public static final String RSC_FROGSPIT_IMG = "frogSpit.png";
    public static final String RSC_BEE_IMG = "bee.png";

    public static final String RSC_LUMBERJACK_IMG = "lumberjack.png";
    public static final String RSC_BULLDOZER_IMG = "bulldozer.png";
    public static final String RSC_SHREDDER_IMG = "shredder.png";
    public static Texture lumberjackWalkSheet;
    public static Texture bulldozerWalkSheet;
    public static Texture shredderWalkSheet;

    public static final String RSC_BORDERS_IMG = "borders.png";
    public static final String RSC_BACKGROUNDUI_IMG = "backgroundUI.png";
    public static final String RSC_HEALTHUPBUTTON_IMG = "healthUpButton.png";
    public static final String RSC_RANGEUPBUTTON_IMG = "rangeUpButton.png";
    public static final String RSC_ATKSPDUPBUTTON_IMG = "atkSpdUpButton.png";
    public static final String RSC_SUMMONTREEBUTTON_IMG = "summonTreeButton.png";
    public static final String RSC_SUMMONBEESBUTTON_IMG = "summonBeesButton.png";
    public static final String RSC_SUMMONTOWERBUTTON_IMG = "summonTowerButton.png";

    public static final String RSC_GRASS_IMG = "grass.png";
    public static final String RSC_ROOTS_IMG = "roots.png";
    public static final String RSC_SHRUBS_IMG = "shrubs.png";
    public static final String RSC_TREES_IMG = "trees.png";
    public static final String RSC_ROCKS_IMG = "rocks.png";


    AssetManager am;  // AssetManager provides a single source for loaded resources
    SpriteBatch batch;
    Random random = new Random();

    Animation<TextureRegion> lumberjackWalkDownAnimation;
    Animation<TextureRegion> lumberjackWalkLeftAnimation;
    Animation<TextureRegion> lumberjackWalkRightAnimation;
    Animation<TextureRegion> lumberjackWalkUpAnimation;

    Animation<TextureRegion> bulldozerWalkDownAnimation;
    Animation<TextureRegion> bulldozerWalkLeftAnimation;
    Animation<TextureRegion> bulldozerWalkRightAnimation;
    Animation<TextureRegion> bulldozerWalkUpAnimation;

    Animation<TextureRegion> shredderWalkDownAnimation;
    Animation<TextureRegion> shredderWalkLeftAnimation;
    Animation<TextureRegion> shredderWalkRightAnimation;
    Animation<TextureRegion> shredderWalkUpAnimation;

    @Override
    public void create() {
        am = new AssetManager();

        lumberjackWalkSheet = new Texture(Gdx.files.internal("lumberjackSprites.png"));
        TextureRegion[][] lumberjackTmp = TextureRegion.split(lumberjackWalkSheet,
                lumberjackWalkSheet.getWidth() / 4,
                lumberjackWalkSheet.getHeight() / 4);

        TextureRegion[] lumberjackWalkDownFrames = setTextureRegion(lumberjackTmp, 0);
        TextureRegion[] lumberjackWalkLeftFrames = setTextureRegion(lumberjackTmp, 1);
        TextureRegion[] lumberjackWalkRightFrames = setTextureRegion(lumberjackTmp, 2);
        TextureRegion[] lumberjackWalkUpFrames = setTextureRegion(lumberjackTmp, 3);

        lumberjackWalkDownAnimation = new Animation<TextureRegion>(0.1f, lumberjackWalkDownFrames);
        lumberjackWalkLeftAnimation = new Animation<TextureRegion>(0.1f, lumberjackWalkLeftFrames);
        lumberjackWalkRightAnimation = new Animation<TextureRegion>(0.1f, lumberjackWalkRightFrames);
        lumberjackWalkUpAnimation = new Animation<TextureRegion>(0.1f, lumberjackWalkUpFrames);


        bulldozerWalkSheet = new Texture(Gdx.files.internal("bulldozerSprites.png"));
        TextureRegion[][] bulldozerTmp = TextureRegion.split(bulldozerWalkSheet,
                bulldozerWalkSheet.getWidth() / 4,
                bulldozerWalkSheet.getHeight() / 4);

        TextureRegion[] bulldozerWalkDownFrames = setTextureRegion(bulldozerTmp, 0);
        TextureRegion[] bulldozerWalkLeftFrames = setTextureRegion(bulldozerTmp, 1);
        TextureRegion[] bulldozerWalkRightFrames = setTextureRegion(bulldozerTmp, 2);
        TextureRegion[] bulldozerWalkUpFrames = setTextureRegion(bulldozerTmp, 3);

        bulldozerWalkDownAnimation = new Animation<TextureRegion>(0.1f, bulldozerWalkDownFrames);
        bulldozerWalkLeftAnimation = new Animation<TextureRegion>(0.1f, bulldozerWalkLeftFrames);
        bulldozerWalkRightAnimation = new Animation<TextureRegion>(0.1f, bulldozerWalkRightFrames);
        bulldozerWalkUpAnimation = new Animation<TextureRegion>(0.1f, bulldozerWalkUpFrames);


        shredderWalkSheet = new Texture(Gdx.files.internal("shredderSprites.png"));
        TextureRegion[][] shredderTmp = TextureRegion.split(shredderWalkSheet,
                shredderWalkSheet.getWidth() / 4,
                shredderWalkSheet.getHeight() / 4);

        TextureRegion[] shredderWalkDownFrames = setTextureRegion(shredderTmp, 0);
        TextureRegion[] shredderWalkLeftFrames = setTextureRegion(shredderTmp, 1);
        TextureRegion[] shredderWalkRightFrames = setTextureRegion(shredderTmp, 2);
        TextureRegion[] shredderWalkUpFrames = setTextureRegion(shredderTmp, 3);

        shredderWalkDownAnimation = new Animation<TextureRegion>(0.1f, shredderWalkDownFrames);
        shredderWalkLeftAnimation = new Animation<TextureRegion>(0.1f, shredderWalkLeftFrames);
        shredderWalkRightAnimation = new Animation<TextureRegion>(0.1f, shredderWalkRightFrames);
        shredderWalkUpAnimation = new Animation<TextureRegion>(0.1f, shredderWalkUpFrames);

		/* True Type Fonts are a bit of a pain. We need to tell the AssetManager
           a bit more than simply the file name in order to get them into an
           easily usable (BitMap) form...
		 */
        FileHandleResolver resolver = new InternalFileHandleResolver();
        am.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        am.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
        FreetypeFontLoader.FreeTypeFontLoaderParameter myFont = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFont.fontFileName = RSC_MONO_FONT_FILE;
        myFont.fontParameters.size = 14;
        am.load(RSC_MONO_FONT, BitmapFont.class, myFont);

        // Load Textures after the font...
        am.load(RSC_GAMEOVER_IMG, Texture.class);
        am.load(RSC_PRESSAKEY_IMG, Texture.class);

        am.load(RSC_FROGTOWER_IMG, Texture.class);
        am.load(RSC_FROGSPIT_IMG, Texture.class);
        am.load(RSC_BEE_IMG, Texture.class);

        am.load(RSC_LUMBERJACK_IMG, Texture.class);
        am.load(RSC_BULLDOZER_IMG, Texture.class);
        am.load(RSC_SHREDDER_IMG, Texture.class);

        am.load(RSC_BORDERS_IMG, Texture.class);
        am.load(RSC_BACKGROUNDUI_IMG, Texture.class);
        am.load(RSC_HEALTHUPBUTTON_IMG, Texture.class);
        am.load(RSC_RANGEUPBUTTON_IMG, Texture.class);
        am.load(RSC_ATKSPDUPBUTTON_IMG, Texture.class);
        am.load(RSC_SUMMONTREEBUTTON_IMG, Texture.class);
        am.load(RSC_SUMMONBEESBUTTON_IMG, Texture.class);
        am.load(RSC_SUMMONTOWERBUTTON_IMG, Texture.class);

        am.load(RSC_GRASS_IMG, Texture.class);
        am.load(RSC_ROOTS_IMG, Texture.class);
        am.load(RSC_SHRUBS_IMG, Texture.class);
        am.load(RSC_TREES_IMG, Texture.class);
        am.load(RSC_ROCKS_IMG, Texture.class);

        // Load Sounds


        batch = new SpriteBatch();
        setScreen(new LoadScreen(this));

        // start the music right away.
        // this one we'll only reference via the GameInstance, and it's streamed
        // so, no need to add it to the AssetManager...

    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
        lumberjackWalkSheet.dispose();
        bulldozerWalkSheet.dispose();
        shredderWalkSheet.dispose();
    }

    @Override
    public void setScreen(Screen screen) {
        if (this.screen != null) {
            this.screen.hide();
            this.screen.dispose();
        }

        this.screen = screen;
        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

    }

    public TextureRegion[] setTextureRegion(TextureRegion[][] tmp, int row) {
        TextureRegion[] frames = new TextureRegion[4];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            frames[index++] = tmp[row][i];
        }
        return frames;
    }
}
