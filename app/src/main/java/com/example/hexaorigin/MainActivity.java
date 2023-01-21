package com.example.hexaorigin;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity
        implements JGameLib.GameEvent {
    int rows=13, cols=7;
    JGameLib gameLib = null;
    JGameLib.Card[][] cellCards = new JGameLib.Card[rows][cols];
    Point poNewCell = new Point(cols/2,-1);
    ArrayList<Point> checkMadePoints = new ArrayList();
    HashSet<Integer> needDropCols = new HashSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameLib = findViewById(R.id.gameLib);
        initGame();
    }

    @Override
    protected void onDestroy() {
        if(gameLib != null)
            gameLib.clearMemory();
        super.onDestroy();
    }

    void initGame() {
        gameLib.listener(this);
        gameLib.setScreenGrid(cols,rows);
        for(int row=0; row < rows; row++) {
            for(int col=0; col < cols; col++) {
                JGameLib.Card cell = gameLib.addCard(R.drawable.hexa_cell0, col, row, 1, 1);
                cell.addImage(R.drawable.hexa_cell1);
                cell.addImage(R.drawable.hexa_cell2);
                cell.addImage(R.drawable.hexa_cell3);
                cell.addImage(R.drawable.hexa_cell4);
                cell.addImage(R.drawable.hexa_cell5);
                cell.addImage(R.drawable.hexa_cell6);
                cellCards[row][col] = cell;
            }
        }
    }

    void newGame() {
        setStateNewCell();
        for(int row=0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cellCards[row][col].imageChange(0);
            }
        }
        //addTestData();
    }

    void nextNewCell() {
        if(isEmptyCell(poNewCell.x, poNewCell.y + 1)) {
            moveNewCell(0, 1);
        } else {
            newCell2CheckMade();
            setStateCheckMade();
        }
    }

    void moveNewCell(int gapX, int gapY) {
        moveCol(poNewCell.x, poNewCell.y, gapX, gapY);
        if(poNewCell.y < 2) {
            int imageIndex = gameLib.random(1, 6);
            cellCards[0][poNewCell.x].imageChange(imageIndex);
        }
        poNewCell.x += gapX;
        poNewCell.y += gapY;
    }

    boolean isEmptyCell(int x, int y) {
        if(x < 0 || x >= cols || y < 0 || y >= rows || cellCards[y][x].imageIndex() > 0)
            return false;
        return true;
    }

    boolean moveCol(int x, int y, int gapX, int gapY) {
        int nextX = x + gapX;
        int nextY = y + gapY;
        if(x < 0 || x >= cols || y < 0 || y >= rows
            || nextX < 0 || nextX >= cols || nextY < 0 || nextY >= rows)
            return false;
        while(y >= 0 && cellCards[y][x].imageIndex() > 0 && nextX >= 0 && nextX < cols && nextY >= 0) {
            int imageIndex = cellCards[y][x].imageIndex();
            cellCards[y][x].imageChange(0);
            cellCards[nextY][nextX].imageChange(imageIndex);
            y --;
            nextY --;
        }
        return true;
    }

    void setStateNewCell() {
        checkMadePoints.clear();
        poNewCell.set(cols/2,-1);
    }

    void setStateCheckMade() {
        poNewCell.set(cols/2,-1);
    }

    void newCell2CheckMade() {
        if(poNewCell.y >= 2) {
            for(int i=0; i < 3; i++)
                checkMadePoints.add(new Point(poNewCell.x, poNewCell.y-i));
        }
    }

    void checkMade() {
        HashSet<Point> needRemove = new HashSet<>();
        for(Point po : checkMadePoints) {
            if(po.x < 0 || po.x >= cols || po.y < 0 || po.y >= rows || cellCards[po.y][po.x].imageIndex() == 0)
                continue;
            int gapX = 1, gapY = 1;
            while(gapX >= 0) {
                int branch1 = sameCellLength(po.x, po.y, gapX, gapY);
                int branch2 = sameCellLength(po.x, po.y, -gapX, -gapY);
                if(branch1 + branch2 >= 2) {
                    int nextX = po.x - (gapX*branch2);
                    int nextY = po.y - (gapY*branch2);
                    for(int j=0; j <= branch1 + branch2; j++) {
                        needRemove.add(new Point(nextX,nextY));
                        nextX += gapX;
                        nextY += gapY;
                    }
                }
                if(gapY >= 0)
                    gapY--;
                else
                    gapX--;
            }
        }
        for(Point po : needRemove) {
            cellCards[po.y][po.x].imageChange(0);
            needDropCols.add(po.x);
        }
        checkMadePoints.clear();
        checkDropCols();
    }

    int sameCellLength(int x, int y, int gapX, int gapY) {
        int nextX = x + gapX;
        int nextY = y + gapY;
        if(nextX < 0 || nextX >= cols || nextY < 0 || nextY >= rows ||
                cellCards[nextY][nextX].imageIndex() != cellCards[y][x].imageIndex())
            return 0;
        return sameCellLength(nextX, nextY, gapX, gapY) + 1;
    }

    void checkDropCols() {
        for(int col : (HashSet<Integer>)needDropCols.clone()) {
            checkDropCol(col);
            needDropCols.remove(col);
        }
    }

    void checkDropCol(int col) {
        int slow = rows-1;
        for(int fast=rows-2; fast >= 0; fast --) {
            if(cellCards[fast][col].imageIndex() == 0) continue;
            while(slow > fast+1 && cellCards[slow][col].imageIndex() > 0)
                slow --;
            if(slow > fast && cellCards[slow][col].imageIndex() == 0) {
                moveCell(col, fast, col, slow);
                checkMadePoints.add(new Point(col, slow));
                slow --;
            }
        }
    }

    void moveCell(int fromX, int fromY, int toX, int toY) {
        int imageIndex = cellCards[fromY][fromX].imageIndex();
        cellCards[fromY][fromX].imageChange(0);
        cellCards[toY][toX].imageChange(imageIndex);
    }

    // User Event start ====================================

    public void onBtnStart(View v) {
        gameLib.stopTimer();
        gameLib.stopAllWork();
        newGame();
        gameLib.startTimer(0.9);
    }

    public void onBtnArrow(View v) {
        if(poNewCell.y < 2) return;
        int gapX = -1;
        if(v.getId() == R.id.btnRight)
            gapX = 1;
        if(!isEmptyCell(poNewCell.x+gapX, poNewCell.y))
            return;
        moveNewCell(gapX, 0);
    }

    public void onBtnRotate(View v) {
        if(poNewCell.y < 2) return;
        int imageIndex = cellCards[poNewCell.y][poNewCell.x].imageIndex();
        moveCol(poNewCell.x, poNewCell.y-1, 0, 1);
        cellCards[poNewCell.y-2][poNewCell.x].imageChange(imageIndex);
    }

    public void onBtnDrop(View v) {
        if(poNewCell.y < 2) return;
        checkDropCol(poNewCell.x);
        setStateCheckMade();
    }

    // User Event end ====================================

    // Game Event start ====================================

    @Override
    public void onGameWorkEnded(JGameLib.Card card, JGameLib.WorkType workType) {}

    @Override
    public void onGameTouchEvent(JGameLib.Card card, int action, float blockX, float blockY) {}

    @Override
    public void onGameSensor(int sensorType, float x, float y, float z) {}

    @Override
    public void onGameCollision(JGameLib.Card card1, JGameLib.Card card2) {}

    @Override
    public void onGameTimer(int what) {
        if(checkMadePoints.isEmpty()) {
            nextNewCell();
        } else {
            checkMade();
        }
    }

    // Game Event end ====================================

    void addTestData() {
        int x=1, y=10;
        cellCards[y][x].imageChange(2);
        cellCards[y][x+1].imageChange(2);
        cellCards[y+1][x].imageChange(1);
        cellCards[y+1][x+1].imageChange(1);
        cellCards[y+2][x].imageChange(3);
        cellCards[y+2][x+1].imageChange(3);
        cellCards[y+1][x+2].imageChange(4);
        cellCards[y+2][x+2].imageChange(4);
        cellCards[y+1][x+3].imageChange(2);
        cellCards[y+2][x+3].imageChange(1);
        cellCards[y+2][x+4].imageChange(2);

        cellCards[0][3].imageChange(6);
        cellCards[1][3].imageChange(5);
        cellCards[2][3].imageChange(2);
        poNewCell.y = 2;
    }

}