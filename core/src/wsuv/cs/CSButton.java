package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CSButton extends Sprite {
    private boolean active;
    public int buttonNum;

    CSButton(Texture texture, float x, float y, int buttonNum) {
        super(texture);
        setPosition(x, y);
        this.active = false;
        this.buttonNum = buttonNum;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
