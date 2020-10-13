package dev.hovel.counter;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.ArrayList;


public class Game {
    // list colors
    public int[] colors;
    public String[] colorNames;

    public boolean initialized = false;

    public enum GameStates {
        START,
        RUNNING,
        WON,
        LOST
    };

    public GameStates status;

    public int chosenColorIndex;
    public int colorChoiceCount;
    public int counted;
    private int[][] board;
    public int w, h, gridWidth, gridHeight;

    public long millisecondsToStart, millisecondsLeft;
    public long countDownInterval = 25;
    public CountDownTimer myTimer;

    public DrawView drawView;

    public int gapX, gapY;
    public int gapValue = -1;
    public boolean swapping;
    public int noswapdx = 0, noswapdy = 0;
    public int swapdx, swapdy;
    public long swappingCounter;
    public long swappingInterval = 100;
    public long swappingCooldown = 10;
    public long swappingCooldownCounter;

    public Game(DrawView view) {
        this.drawView = view;

        colors = new int[6];
        colorNames = new String[6];
        colors[0] = Color.RED; colorNames[0] = "Red";
        colors[1] = Color.BLUE; colorNames[1] = "Blue";
        colors[2] = Color.GREEN; colorNames[2] = "Green";
        colors[3] = Color.CYAN; colorNames[3] = "Cyan";
        colors[4] = Color.MAGENTA; colorNames[4] = "Magenta";
        colors[5] = Color.YELLOW; colorNames[5] = "Yellow";
    }

    public void restartGame(int gridWidth, int gridHeight) {
        Game g = this;

        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        initializeBoard(gridWidth, gridHeight);
        swapping = false;
        swapdx = 0;
        swapdy = 0;
        noswapdx = 0;
        noswapdy = 0;
        swappingCounter = 0;
        swappingCooldownCounter = 0;

        millisecondsToStart = 10000;
        millisecondsLeft = millisecondsToStart;
        // create timer
        myTimer = new CountDownTimer(millisecondsToStart, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisecondsLeft = millisUntilFinished;

                if ( swapping ) {
                    swappingCounter += countDownInterval;

                    if (swappingCounter >= swappingInterval) {
                        // we are done swapping
                        swapping = false;
                        swappingCounter = 0;

                        // don't just go back the way you came
                        noswapdx = -swapdx;
                        noswapdy = -swapdy;

                        swapBoardValues(gapX, gapY, gapX + swapdx, gapY + swapdy);

                        // update gaps
                        gapX = gapX + swapdx;
                        gapY = gapY + swapdy;


                    }
                } else {
                    // update swapping cooldown
                    swappingCooldownCounter += countDownInterval;

                    if (swappingCooldownCounter >= swappingCooldown) {
                        swappingCooldownCounter = 0;

                        swappingCounter = 0;
                        swapping = true;

                        // select swapdx and swapdy, avoid outofbounds, avoid just going backwards
                        ArrayList<Position> dv = new ArrayList<Position>();
                        if (noswapdx != -1 && gapX > 0) dv.add(new Position(-1, 0));
                        if (noswapdx != 1 && gapX < w - 1) dv.add(new Position(1, 0));
                        if (noswapdy != -1 && gapY > 0) dv.add(new Position(0, -1));
                        if (noswapdy != 1 && gapY < h - 1) dv.add(new Position(0, 1));

                        Position p = dv.get((int)Math.floor(Math.random() * dv.size()));
                        swapdx = p.x;
                        swapdy = p.y;

                        // now using the following:
                        // swapdx, swapdy, gapX, gapY, swappingCounter, swappingInterval
                        // can interpolate between board positions
                    }
                }

                drawView.invalidate();
            }

            @Override
            public void onFinish() {
                submitDone();
                millisecondsLeft = 0;
                drawView.invalidate();
            }
        };
    }
    public void initializeBoard(int w, int h) {
        initialized = true;
        status = GameStates.START;

        chosenColorIndex = this.chooseRandomColorIndex();
        colorChoiceCount = 0;
        counted = 0;

        this.w = w;
        this.h = h;
        board = new int[w][h];
        gapX = (int) Math.floor( Math.random() * this.w);
        gapY = (int) Math.floor( Math.random() * this.h);

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (i == gapX && j == gapY) {
                    board[i][j] = gapValue;
                    continue;
                }

                int cellColor = this.chooseRandomColorIndex();
                if (cellColor == chosenColorIndex) {
                    colorChoiceCount += 1;
                }
                board[i][j] = cellColor;
            }
        }
    }
    public void run() {
        status = GameStates.RUNNING;
        myTimer.start();
    }

    public void swapBoardValues(int x1, int y1, int x2, int y2) {

        int tmp = board[x1][y1];
        board[x1][y1] = board[x2][y2];
        board[x2][y2] = tmp;
    }

    public int getColorFromBoardValue(int value) {

        return colors[value];
    }
    public boolean isGap(int value) {
        return value == gapValue;
    }
    public int getBoardValue(int x, int y) {
        return board[x][y];
    }

    public String getChosenColorString() {
        return colorNames[chosenColorIndex];
    }
    public int getChosenColorValue() {
        return colors[chosenColorIndex];
    }
    public void incrementCount() {
        counted += 1;
    }

    private int chooseRandomColorIndex() {
        return (int) Math.floor( Math.random() * colors.length );
    }

    public void submitDone() {
        if (status != GameStates.RUNNING) throw new GameException("Game.submitDone: error status != RUNNING");

        if (colorChoiceCount == counted) {
            status = GameStates.WON;
        } else {
            status = GameStates.LOST;
        }
    }

    public long getTimeLeft() {
        return millisecondsLeft;
    }
    public static class GameException extends RuntimeException {
        public GameException(String message) {
            super(message);
        }
    }

    private class Position {
        public int x, y;
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
