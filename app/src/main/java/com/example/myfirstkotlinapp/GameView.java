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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myfirstkotlinapp.Sprites.Mole;
import com.example.myfirstkotlinapp.Sprites.MoleHole;
import com.example.myfirstkotlinapp.Sprites.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;

    private int mGridSize = 3;
    private int gridSizeX = mGridSize;
    private int gridSizeY = mGridSize;
    private int mGridCellSizeX = 0;
    private int mGridCellSizeY = 0;

    private RectF mHole;
    private Paint mHolePaint;
    private Paint mMolePaint;

    private Random random = new Random();
    private List<Mole> mMoles = new ArrayList<Mole>();
    private List<MoleHole> mMoleHoles = new ArrayList<MoleHole>();

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

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

        mGridCellSizeX = getWidth() / gridSizeX;
        mGridCellSizeY = getHeight() / gridSizeY;

        setupTextures();
        setupMap();

        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // TODO: Not sure about this one?
            retry = false;
        }
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

                for (Mole mole : this.mMoles) {
                    System.out.println("x: " + dx + ", y: " + dy);
                    if(mole.position.Compare(dx, dy)) {
                        if(!mole.isActive())
                            break;

                        mole.inActivate();
                        this.activateRandomMole();

                        postInvalidate();

                        return true;
                    }
                }
            }
        }

        return value;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (canvas == null)
            return;

        for (MoleHole moleHole : mMoleHoles) {
            moleHole.draw(canvas);
        }

        for (Mole mole : mMoles) {
            mole.draw(canvas);
        }

        //debugDrawLines(canvas);
    }

    public void update() {
        for(Mole mole : mMoles) {
            mole.update();
        }
    }

    public void resetDraw() {
        activateRandomMole();
    }

    private void init(@Nullable AttributeSet set) {
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);

        if (set != null)
            getAndSetOptions(set);
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
        mHoleTexture = BitmapFactory.decodeResource(getResources(), R.drawable.wak_template);
        mHoleTexture = getResizedBitmap(mHoleTexture, mGridCellSizeX, mGridCellSizeY);
        for (int moleTextureId : mMoleTextureIds) {
            Bitmap texture = getResizedBitmap(BitmapFactory.decodeResource(getResources(), moleTextureId), mGridCellSizeX, mGridCellSizeY);
            mMoleTextures.add(texture);
        }
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        RectF src = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF dst = new RectF(0, 0, newWidth, newHeight);
        Matrix matrix = new Matrix();
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);

        Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0 , bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return resized;
    }

    private void setupMap() {
        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                mMoles.add(new Mole(new Vector2(x, y), mGridCellSizeX, mGridCellSizeY));
                mMoleHoles.add(new MoleHole(new Vector2(x, y), mGridCellSizeX, mGridCellSizeY, mHoleTexture));
            }
        }
    }

    private void activateRandomMole() {
        if (this.mMoles.size() == 0)
            return;

        boolean randomMoleActivated = false;
        while (!randomMoleActivated) {
            Mole mole = this.mMoles.get(random.nextInt(this.mMoles.size()));
            if(!mole.isActive()) {
                mole.activate();
                randomMoleActivated = true;
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

    private void debugDraw(Canvas canvas) {
        float holeWidth = mGridCellSizeX * 0.8f;
        float holeHeight = mGridCellSizeY * 0.4f;

        float marginX = (mGridCellSizeX - holeWidth) * 0.5f;
        float marginY = (mGridCellSizeY - holeHeight) * 0.8f;

        for (Mole mole : mMoles) {
            this.mHole.left = mole.position.x * mGridCellSizeX + marginX;
            this.mHole.top = mole.position.y * mGridCellSizeY + marginY;
            this.mHole.right = this.mHole.left + holeWidth;
            this.mHole.bottom = this.mHole.top + holeHeight;

            canvas.drawOval(this.mHole, this.mHolePaint);

            if(mole.isActive()) {
                canvas.drawCircle(
                        mole.position.x * mGridCellSizeX + mGridCellSizeX / 2,
                        mole.position.y * mGridCellSizeY + mGridCellSizeY / 2,
                        mGridCellSizeX / 4, this.mMolePaint);
            }
        }
    }
}