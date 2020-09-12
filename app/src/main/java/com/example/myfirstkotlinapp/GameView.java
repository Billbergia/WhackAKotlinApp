package com.example.myfirstkotlinapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private static final int gridSize = 3;
    private static final int gridSizeX = gridSize;
    private static final int gridSizeY = gridSize;

    private RectF mHole;
    private Paint mHolePaint;
    private Paint mMolePaint;

    private List<Mole> moles = new ArrayList<Mole>();
    private Random random = new Random();;

    public GameView(Context context) {
        super(context);
        init(null);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set) {
        this.mHole = new RectF();
        this.mHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mHolePaint.setColor(Color.BLACK);

        this.mMolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mMolePaint.setColor(Color.GREEN);

        this.SetupMap();
        this.activateRandomMole();
    }

    private void SetupMap() {
        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                moles.add(new Mole(x, y));
            }
        }
    }

    public void resetDraw() {
        postInvalidate();
    }

    private void activateRandomMole() {
        boolean randomMoleActivated = false;
        while (!randomMoleActivated) {
            Mole mole = this.moles.get(random.nextInt(this.moles.size()));
            if(!mole.active) {
                mole.active = true;
                randomMoleActivated = true;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cellWidth = (float)getWidth() / gridSizeX;
        float cellHeight = (float)getHeight() / gridSizeY;

        float holeWidth = cellWidth * 0.8f;
        float holeHeight = cellHeight * 0.4f;

        float marginX = (cellWidth - holeWidth) * 0.5f;
        float marginY = (cellHeight - holeHeight) * 0.8f;

        for (Mole mole : moles) {
            this.mHole.left = mole.coordinates.x * cellWidth + marginX;
            this.mHole.top = mole.coordinates.y * cellHeight + marginY;
            this.mHole.right = this.mHole.left + holeWidth;
            this.mHole.bottom = this.mHole.top + holeHeight;

            canvas.drawOval(this.mHole, this.mHolePaint);

            if(mole.active) {
                canvas.drawCircle(
                        mole.coordinates.x * cellWidth + cellWidth / 2,
                        mole.coordinates.y * cellHeight + cellHeight / 2,
                        cellWidth / 4, this.mMolePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean value = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                float x = event.getX();
                float y = event.getY();

                float cellWidth = getWidth() / (float)gridSizeX;
                float cellHeight = getHeight() / (float)gridSizeY;

                int dx = (int)Math.floor(x / cellWidth);
                int dy = (int)Math.floor(y / cellHeight);

                for (Mole mole : this.moles) {
                    System.out.println("x: " + dx + ", y: " + dy);
                    if(mole.coordinates.Compare(dx, dy)) {
                        if(!mole.active)
                            break;

                        mole.active = false;
                        this.activateRandomMole();

                        postInvalidate();

                        return true;
                    }
                }
            }
        }

        return value;
    }

    public class Vector2 {
        public int x;
        public int y;

        Vector2(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean Compare(int x, int y) {
            if (x == this.x && y == this.y) {
                return true;
            }

            return false;
        }

        public boolean Compare(Vector2 vec) {
            if (vec.x == this.x && vec.y == this.y) {
                return true;
            }

            return false;
        }
    }

    public class Mole {
        public Vector2 coordinates;
        public boolean active = false;

        Mole(Vector2 coordinates) {
            this.coordinates = coordinates;
        }

        Mole(int x, int y) {
            this.coordinates = new Vector2(x, y);
        }
    }
}