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
    public int nextTileNum;

    public Tile(CSGame game, Terrain terrain, int tileNum) {
        super(terrain.texture);
        this.terrain = terrain;
        this.tileNum = tileNum;
        nextTileNum = tileNum;
        populateAdj(game);
    }

    public int getTerrainCost() { return terrain.cost; }

    public int getCurrentCost() { return currentCost; }

    public void setCurrentCost(int cost) { currentCost = cost; }

    private void populateAdj(CSGame game) {
        adjTileNums = new ArrayList<Integer>();
        boolean addLeft = false;
        boolean addRight = false;
        boolean addBelow = false;
        boolean addAbove = false;
        // if tileNum is on the left edge (0-28) dont subtract 29
        if (tileNum > GRID_SIZE-1) {
            addLeft = true;
        }
        // if tileNum is on the right side (812-840) dont add 29
        if (tileNum < GRID_SIZE*GRID_SIZE-GRID_SIZE) {
            addRight = true;
        }
        // if tileNum is on the bottom (0+29n) dont subtract 1
        if (tileNum % GRID_SIZE != 0) {
            addBelow = true;
        }
        // if tileNum is on the top (28+29n) dont add 1
        if (tileNum % GRID_SIZE != GRID_SIZE-1) {
            addAbove = true;
        }

        // to randomize slightly the way the loggers will move (still towards best but sometimes
        // will pick up or left for example instead of always left each game).
        int randomNum = game.random.nextInt(4);
        if (randomNum == 0) {
            if (addLeft) adjTileNums.add(tileNum-GRID_SIZE);
            if (addRight) adjTileNums.add(tileNum+GRID_SIZE);
            if (addBelow) adjTileNums.add(tileNum-1);
            if (addAbove) adjTileNums.add(tileNum+1);
        }
        else if (randomNum == 1) {
            if (addBelow) adjTileNums.add(tileNum-1);
            if (addAbove) adjTileNums.add(tileNum+1);
            if (addRight) adjTileNums.add(tileNum+GRID_SIZE);
            if (addLeft) adjTileNums.add(tileNum-GRID_SIZE);
        }
        else if (randomNum == 2) {
            if (addRight) adjTileNums.add(tileNum+GRID_SIZE);
            if (addAbove) adjTileNums.add(tileNum+1);
            if (addLeft) adjTileNums.add(tileNum-GRID_SIZE);
            if (addBelow) adjTileNums.add(tileNum-1);
        }
        else if (randomNum == 3) {
            if (addAbove) adjTileNums.add(tileNum+1);
            if (addLeft) adjTileNums.add(tileNum-GRID_SIZE);
            if (addRight) adjTileNums.add(tileNum+GRID_SIZE);
            if (addBelow) adjTileNums.add(tileNum-1);
        }
    }
}
