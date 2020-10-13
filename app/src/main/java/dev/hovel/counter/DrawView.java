package dev.hovel.counter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;

public class DrawView extends View {

    Game g;

    public int viewWidth, viewHeight;

    public int gridAreaLeft, gridAreaTop, gridAreaRight, gridAreaBottom;

    public int buttonAreaLeft, buttonAreaTop, buttonAreaRight, buttonAreaBottom;

    public int gridWidth, gridHeight;

    public Typeface myFont;
    private Paint p;
    private String timerText;

    MediaPlayer mediaPlayer;
    private boolean firstPlay = true;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mediaPlayer = MediaPlayer.create(context, R.raw.till);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.seekTo(600);
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(MediaPlayer mp) {
                if (firstPlay) {
                    firstPlay = false;
                    // goofy logic to get ready for next prepared state durr
                    mp.start();
                    mp.stop();
                    return;
                }
                mp.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
            }
        });

        myFont = ResourcesCompat.getFont(context, R.font.roboto);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);

        gridWidth = 15;
        gridHeight = 15;

        g = new Game(this);
        g.restartGame(gridWidth, gridHeight);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getAction();
        int pointerId = event.getPointerId(index);

        if (action != MotionEvent.ACTION_UP) return true;

        float x = event.getX(pointerId);
        float y = event.getY(pointerId);

        if (g.status == Game.GameStates.START) {
            g.run();
        }
        else if (g.status == Game.GameStates.RUNNING) {
            g.incrementCount();

            if (mediaPlayer.isPlaying() ) {
                mediaPlayer.stop();
            }
            mediaPlayer.prepareAsync();

        } else {
            g.restartGame(gridWidth, gridHeight);
        }

        this.invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setDimensions();
        blackOut(canvas);
        drawGridArea(canvas);
        drawButtonArea(canvas);
    }
    private void blackOut(Canvas canvas) {
        p.setColor(Color.BLACK);
        canvas.drawRect(0, 0, viewWidth, viewHeight, p);

    }

    private void drawGridArea(Canvas canvas) {

        int widthDiff = gridAreaRight - gridAreaLeft;
        int heightDiff = gridAreaBottom - gridAreaTop;

        int cellWidth = widthDiff / gridWidth;
        int cellHeight = heightDiff / gridHeight;

        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {

                if (g.swapping && i == g.gapX + g.swapdx && j == g.gapY + g.swapdy) {
                    int value = g.getBoardValue(i, j);
                    if (g.isGap(value)) continue;
                    int color = g.getColorFromBoardValue(value);
                    p.setStyle(Paint.Style.FILL);
                    if (g.status == Game.GameStates.START) p.setColor(Color.BLACK);
                    else p.setColor(color);

                    // cells
                    int halfLength = (int)(cellWidth / 2 * 0.90);
                    int cx =  (int) ( interpolate(g.gapX + g.swapdx, g.gapX, g.swappingCounter / (float) g.swappingInterval) * cellWidth + cellWidth / 2 );
                    int cy = (int) ( interpolate(g.gapY + g.swapdy, g.gapY, g.swappingCounter / (float) g.swappingInterval) * cellHeight + cellHeight / 2);
                    int radc = (int) (halfLength - halfLength * 0.6);
                    centerRoundedSquare(canvas, cx, cy, halfLength, radc, p);

                    // outlines
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth( cellWidth * 0.05f );
                    p.setColor(Color.WHITE);
                    centerRoundedSquare(canvas, cx, cy, halfLength, radc, p);

                    continue;
                }

                int value = g.getBoardValue(i, j);
                if (g.isGap(value)) continue;
                int color = g.getColorFromBoardValue(value);
                p.setStyle(Paint.Style.FILL);
                if (g.status == Game.GameStates.START) p.setColor(Color.BLACK);
                else p.setColor(color);

                // cells
                int halfLength = (int)(cellWidth / 2 * 0.90);
                int cx = i * cellWidth + cellWidth / 2;
                int cy = j * cellHeight + cellHeight / 2;
                int radc = (int) (halfLength - halfLength * 0.6);
                centerRoundedSquare(canvas, cx, cy, halfLength, radc, p);

                // outlines
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth( cellWidth * 0.05f );
                p.setColor(Color.WHITE);
                centerRoundedSquare(canvas, cx, cy, halfLength, radc, p);
            }
        }
    }

    private void centerRoundedSquare(Canvas canvas, int cx, int cy, int halfLength, int radc, Paint p) {
        canvas.drawRoundRect(cx - halfLength, cy - halfLength, cx + halfLength, cy + halfLength, radc, radc, p);
    }

    private void drawButtonArea(Canvas canvas) {

        // draw the text
        String counted = Integer.toString(g.counted);
        String targetCount = Integer.toString(g.colorChoiceCount);
        String targetColor = g.getChosenColorString();
        int targetColorVal = g.getChosenColorValue();

        int offsetX = buttonAreaLeft;
        int offsetY = buttonAreaTop;

        int areaWidth = (buttonAreaRight - buttonAreaLeft);
        int areaHeight = (buttonAreaBottom - buttonAreaTop);

        int row1 = (int) (offsetY + areaHeight * 0.2);
        int row2 = (int) (offsetY + areaHeight * 0.5);
        int row3 = (int) (offsetY + areaHeight * 0.8);

        int col1 = (int) (offsetX + areaWidth * 0.2);
        int col2 = (int) (offsetX + areaWidth * 0.4);
        int col3 = (int) (offsetX + areaWidth * 0.8);
        int colCenter = (int) (offsetX + areaWidth * 0.5);

        // Style information
        p.setStyle(Paint.Style.FILL);
        p.setColor(targetColorVal);
        p.setTypeface(myFont);
        p.setTextSize((float) (areaHeight * 0.2));

        // draw target text
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(targetColor, colCenter, row1, p);

        // draw the timer
        p.setColor(Color.WHITE);
        p.setTextAlign(Paint.Align.CENTER);
        // timer position and text
        setTimerText();
        canvas.drawText(timerText, colCenter, row2, p);

        // draw the counter
        p.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(counted, col1, row2, p);

        if (g.status == Game.GameStates.LOST || g.status == Game.GameStates.WON) {

            // draw the target count
            p.setColor(targetColorVal);
            p.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(targetCount, col3, row2, p);

            // game message style and position
            p.setColor(Color.WHITE);
            p.setTextAlign(Paint.Align.CENTER);
            // draw the losing text
            if (g.status == Game.GameStates.LOST) {
                canvas.drawText("You Lost", colCenter, row3, p);
            }
            // draw the winning text
            else if (g.status == Game.GameStates.WON) {
                canvas.drawText("You Won!", colCenter, row3, p);
            }
        }
    }

    private void setTimerText() {
        long millisecondsLeft = g.getTimeLeft();
        StringBuilder sb = new StringBuilder();
        int seconds = (int) millisecondsLeft / 1000;
        if (seconds >= 10)
            sb.append(seconds);
        else {
            sb.append("0");
            sb.append(seconds);
        }
        sb.append(".");

        int hundredths = (int) ((millisecondsLeft/10 - seconds * 100));
        if (hundredths >= 10) {
            sb.append(hundredths);
        } else {
            sb.append("0");
            sb.append(hundredths);
        }
        timerText = sb.toString();
    }
    private void setDimensions() {
        viewWidth = getWidth();
        viewHeight = getHeight();

        gridAreaLeft = 0;
        gridAreaTop = 0;
        gridAreaRight = viewWidth;
        gridAreaBottom = gridAreaRight;

        buttonAreaLeft = 0;
        buttonAreaTop = gridAreaBottom;

        buttonAreaRight = viewWidth;
        buttonAreaBottom = viewHeight;

    }

    private float interpolate(float start, float end, float t) {
        return start + (end - start) * t;
    }
}
