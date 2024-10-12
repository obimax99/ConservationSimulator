package wsuv.cs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CSButton extends Sprite {
    public float width;
    public float height;
    private boolean active;

    CSButton(Texture texture, float x, float y, float width, float height) {
        super(texture);
        setPosition(x, y);
        this.width = width;
        this.height = height;
        this.active = false;
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
