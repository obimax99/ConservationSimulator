package wsuv.cs;

public class ShrubTree {
    public int gridX;
    public int gridY;
    private float timeUntilGrowth;
    public boolean grown;

    ShrubTree(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        timeUntilGrowth = 8.0f;
    }

    public boolean update(float delta) {
        timeUntilGrowth -= delta;
        if (timeUntilGrowth <= 0.0f && !grown) { grown = true; return true;}
        return false;
    }
}
