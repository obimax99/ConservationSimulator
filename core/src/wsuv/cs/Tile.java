package wsuv.cs;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.ArrayList;
import java.util.List;

import static wsuv.cs.Constants.GRID_SIZE;

public class Tile extends Sprite {
    protected Terrain terrain;
    private int currentCost;
    public int tileNum;
    public ArrayList<Integer> adjTileNums;

    public Tile(Terrain terrain, int tileNum) {
        super(terrain.texture);
        this.terrain = terrain;
        this.tileNum = tileNum;
        populateAdj();
    }

    public int getTerrainCost() { return terrain.cost; }

    public int getCurrentCost() { return currentCost; }

    public void setCurrentCost(int cost) { currentCost = cost; }

    private void populateAdj() {
        adjTileNums = new ArrayList<Integer>();
        // if tileNum is on the left edge (0-28) dont subtract 29
        if (tileNum > GRID_SIZE-1) {
            adjTileNums.add(tileNum-GRID_SIZE);
        }
        // if tileNum is on the right side (812-840) dont add 29
        if (tileNum < GRID_SIZE*GRID_SIZE-GRID_SIZE) {
            adjTileNums.add(tileNum+GRID_SIZE);
        }
        // if tileNum is on the bottom (0+29n) dont subtract 1
        if (tileNum % GRID_SIZE != 0) {
            adjTileNums.add(tileNum-1);
        }
        // if tileNum is on the top (28+29n) dont add 1
        if (tileNum % GRID_SIZE != GRID_SIZE-1) {
            adjTileNums.add(tileNum+1);
        }
        System.out.println(tileNum + ": " + adjTileNums);
    }
}
