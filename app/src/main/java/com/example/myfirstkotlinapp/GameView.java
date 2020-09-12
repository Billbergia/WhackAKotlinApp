package com.example.myfirstkotlinapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private int mGridSize = 3;
    private int gridSizeX = mGridSize;
    private int gridSizeY = mGridSize;
    private int mGridCellSizeX;
    private int mGridCellSizeY;

    private RectF mHole;
    private Paint mHolePaint;
    private Paint mMolePaint;

    private Random random = new Random();
    private List<Mole> moles = new ArrayList<Mole>();
    private List<Bitmap> mMoleTextures = new ArrayList<Bitmap>();
    private Bitmap mHoleTexture;

    private int mHoleTextureId = R.drawable.default_texture;
    private int[] mMoleTextureIds;

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
        if (set == null)
            return;

        getAndSetOptions(set);
        setupMap();
        setupDebugMode();
        activateRandomMole();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Only need it to be called one time, so we remove it after.
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupTextures();
                postInvalidate();
            }
        });
    }

    private void getAndSetOptions(AttributeSet set) {
        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.GameView);

        mGridSize = ta.getInt(R.styleable.GameView_grid_size, 3);
        gridSizeX = mGridSize;
        gridSizeY = mGridSize;

        mHoleTextureId = ta.getResourceId(R.styleable.GameView_hole_texture, R.drawable.default_texture);
        mMoleTextureIds = new int[]{
                ta.getResourceId(R.styleable.GameView_mole_texture_1, R.drawable.default_texture),
                ta.getResourceId(R.styleable.GameView_mole_texture_2, R.drawable.default_texture),
                ta.getResourceId(R.styleable.GameView_mole_texture_3, R.drawable.default_texture)
        };
    }

    private void setupTextures() {
        mGridCellSizeX = getWidth() / gridSizeX;
        mGridCellSizeY = getHeight() / gridSizeY;

        mHoleTexture = BitmapFactory.decodeResource(getResources(), R.drawable.wak_template);
        mHoleTexture = getResizedBitmap(mHoleTexture, mGridCellSizeX, mGridCellSizeY);
        for (int moleTextureId : mMoleTextureIds) {
            Bitmap texture = getResizedBitmap(BitmapFactory.decodeResource(getResources(), moleTextureId), mGridCellSizeX, mGridCellSizeY);
            mMoleTextures.add(texture);
        }
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        Matrix matrix = new Matrix();

        RectF src = new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
        RectF dst = new RectF(0,0,reqWidth,reqHeight);

        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);

        int posX = 0;
        int posY = 0;

        return Bitmap.createBitmap(bitmap, posX, posY , bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void setupDebugMode() {
        this.mHole = new RectF();
        this.mHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mHolePaint.setColor(Color.BLACK);

        this.mMolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mMolePaint.setColor(Color.GREEN);
    }

    private void setupMap() {
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

    private void debugDraw(Canvas canvas) {
        float holeWidth = mGridCellSizeX * 0.8f;
        float holeHeight = mGridCellSizeY * 0.4f;

        float marginX = (mGridCellSizeX - holeWidth) * 0.5f;
        float marginY = (mGridCellSizeY - holeHeight) * 0.8f;

        for (Mole mole : moles) {
            this.mHole.left = mole.coordinates.x * mGridCellSizeX + marginX;
            this.mHole.top = mole.coordinates.y * mGridCellSizeY + marginY;
            this.mHole.right = this.mHole.left + holeWidth;
            this.mHole.bottom = this.mHole.top + holeHeight;

            canvas.drawOval(this.mHole, this.mHolePaint);

            if(mole.active) {
                canvas.drawCircle(
                    mole.coordinates.x * mGridCellSizeX + mGridCellSizeX / 2,
                    mole.coordinates.y * mGridCellSizeY + mGridCellSizeY / 2,
                    mGridCellSizeX / 4, this.mMolePaint);
            }
        }

        for(int x = 0; x < gridSizeX; x++) {
            for(int y = 0; y < gridSizeY; y++) {
                canvas.drawLine(x, y, x * mGridCellSizeX, y * mGridCellSizeY, mHolePaint);
            }
        }
    }

    private void debugDrawLines(Canvas canvas) {
        for(int x = 0; x < gridSizeX; x++) {
            canvas.drawLine(x * mGridCellSizeX, 0, x * mGridCellSizeX, getHeight(), mHolePaint);
        }

        for(int y = 0; y < gridSizeY; y++) {
            canvas.drawLine(0, y * mGridCellSizeY, getWidth(), y * mGridCellSizeY, mHolePaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (Mole mole : moles) {
            canvas.drawBitmap(this.mHoleTexture, mole.coordinates.x * mGridCellSizeX, mole.coordinates.y * mGridCellSizeY, null);
            if(mole.active) {
                canvas.drawCircle(
                        mole.coordinates.x * mGridCellSizeX + mGridCellSizeX / 2,
                        mole.coordinates.y * mGridCellSizeY + mGridCellSizeY / 2,
                        mGridCellSizeX / 4, this.mMolePaint);
            }
        }

        debugDrawLines(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean value = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                float x = event.getX();
                float y = event.getY();

                int dx = (int)Math.floor(x / mGridCellSizeX);
                int dy = (int)Math.floor(y / mGridCellSizeY);

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