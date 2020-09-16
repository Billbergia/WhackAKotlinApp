package com.example.myfirstkotlinapp;

import android.annotation.SuppressLint;
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

import com.example.myfirstkotlinapp.gamecore.GameGlobal;
import com.example.myfirstkotlinapp.gamecore.GameThread;
import com.example.myfirstkotlinapp.sprites.Mole;
import com.example.myfirstkotlinapp.sprites.MoleHole;
import com.example.myfirstkotlinapp.gamecore.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
* TODO: Probably a good idea to try to split this up, perhaps create a base class which can be used
*       for creating other games.
* */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;

    private int mGridSize = 3;
    private int mGridSizeX = mGridSize;
    private int mGridSizeY = mGridSize;

    // Will be set in surfaceCreated()
    private int mGridCellSizeX = 0;
    private int mGridCellSizeY = 0;

    private boolean mRoundActive = false;
    private float mRoundStarted = 0f;
    private float mWaitTilNextRound = 0f;

    private Random mRandom = new Random();
    private List<Mole> mMoles = new ArrayList<Mole>();
    private List<Mole> mActiveMoles = new ArrayList<Mole>();
    private List<MoleHole> mMoleHoles = new ArrayList<MoleHole>();

    private List<Bitmap> mMoleTextures = new ArrayList<Bitmap>();
    private Bitmap mHoleTexture;
    private Bitmap mHoleTextureForeground;

    private int mHoleTextureId;
    private int mHoleTextureForegroundId;
    private int[] mMoleTextureIds;

    private Vector2 mPreviousTouch = new Vector2();
    private Vector2 mCurrentTouch = new Vector2();
    private long mPreviousTouchTimeMs = 0;

    private boolean mDebug = false;
    private boolean mDebugGridAndCoordinates = false;
    private Paint mDebugPaint;
    private int mDebugTextSize = 50;

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
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);

        if (set != null)
            getAttributesAndInitializeOptionFields(set);
    }

    private void getAttributesAndInitializeOptionFields(AttributeSet set) {
        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.GameView);

        mGridSize = ta.getInt(R.styleable.GameView_grid_size, 3);
        mGridSizeX = mGridSize;
        mGridSizeY = mGridSize;

        mHoleTextureId = ta.getResourceId(R.styleable.GameView_hole_texture, R.drawable.wak_default_hole);
        mHoleTextureForegroundId = ta.getResourceId(R.styleable.GameView_hole_foreground_texture, R.drawable.wak_default_hole_foreground);
        mMoleTextureIds = new int[]{
                ta.getResourceId(R.styleable.GameView_mole_texture_1, R.drawable.wak_default_mole_1),
                ta.getResourceId(R.styleable.GameView_mole_texture_2, R.drawable.wak_default_mole_2),
                ta.getResourceId(R.styleable.GameView_mole_texture_3, R.drawable.wak_default_mole_3)
        };

        mDebug = ta.getBoolean(R.styleable.GameView_debug, false);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        surfaceCreatedInit();

        thread.setRunning(true);
        thread.start();
    }

    private void surfaceCreatedInit() {
        mGridCellSizeX = getWidth() / mGridSizeX;
        mGridCellSizeY = getHeight() / mGridSizeY;

        initializeTextures();
        createLevel();

        mDebugPaint = new Paint();
        mDebugPaint.setColor(Color.RED);
        mDebugPaint.setStrokeWidth(4);
        mDebugPaint.setTextSize(50);
    }

    private void initializeTextures() {
        mMoleTextures.clear();

        mHoleTexture = BitmapFactory.decodeResource(getResources(), mHoleTextureId);
        mHoleTexture = getResizedBitmap(mHoleTexture, mGridCellSizeX, mGridCellSizeY);
        mHoleTextureForeground = BitmapFactory.decodeResource(getResources(), mHoleTextureForegroundId);
        mHoleTextureForeground = getResizedBitmap(mHoleTextureForeground, mGridCellSizeX, mGridCellSizeY);

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

    /*
    * Consider below grid 3 x 3, this will be our default play field.
    * We'll span the mole underneath it's hole, see below grid example.
    *
    *       0   1   2
    *   0 |   |   |   |
    *   1 |   | o |   |      o = hole
    *   2 |   | x |   |      x = mole
    *
    * */
    private void createLevel() {
        mMoles.clear();
        mMoleHoles.clear();
        mActiveMoles.clear();

        for (int x = 0; x < mGridSizeX; x++) {
            for (int y = 0; y < mGridSizeY; y++) {
                mMoleHoles.add(new MoleHole(new Vector2(x, y), mHoleTexture, mHoleTextureForeground));
                mMoles.add(new Mole(new Vector2(x, y + 1), mMoleTextures));
            }
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // TODO: do we need to do anything here?
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
                mCurrentTouch.set(dx, dy);

                debugOptionsTouched(event.getDownTime());
                activeMoleTouched();

                mPreviousTouch.set(dx, dy);
                mPreviousTouchTimeMs = event.getDownTime();
            }
        }

        return value;
    }

    private void debugOptionsTouched(long eventDownTime) {
        if (!mPreviousTouch.equals(mCurrentTouch) || mPreviousTouchTimeMs + 200 < eventDownTime)
            return;

        if (mPreviousTouch.equals(0, 0)) {
            mDebug = !mDebug;
        } else if (mDebug && mPreviousTouch.equals(mGridSizeX - 1, 0)) {
            mDebugGridAndCoordinates = !mDebugGridAndCoordinates;
        } else if (mDebug && mPreviousTouch.equals(0, mGridSizeY - 1) && mGridSize > 2) {
            mGridSize -= 1;
            mGridSizeX = mGridSize;
            mGridSizeY = mGridSize;
            surfaceCreatedInit();
        } else if (mDebug && mPreviousTouch.equals(mGridSizeX - 1, mGridSizeY - 1)) {
            mGridSize += 1;
            mGridSizeX = mGridSize;
            mGridSizeY = mGridSize;
            surfaceCreatedInit();
        }
    }

    /*
    * Only check if active mole was touched. Downside with this is that won't be possible to
    * score on moles that are going down in the hole.
    *
    * Also another downside is that for every single touch this will loop through all active moles.
    * */
    private void activeMoleTouched() {
        for (Mole mole : mActiveMoles) {
            if (mole.position.equals(mCurrentTouch)) {
                if (!mole.isActive())
                    break;

                mole.deactivate();
                mActiveMoles.remove(mole);
                break;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (canvas == null)
            return;

        mDebugPaint.setTextSize(mDebugTextSize);
        mDebugPaint.setColor(Color.RED);
        mDebugPaint.setAlpha(255);

        // A small hack that makes it so the mole is hidden behind below hole background texture.
        for (int i = 0; i < mMoleHoles.size(); i++) {
            mMoleHoles.get(i).draw(canvas);
            mMoles.get(i).draw(canvas);
            mMoleHoles.get(i).drawForeground(canvas);
        }

        if (mDebugGridAndCoordinates)
            drawDebugGridAndCoordinates(canvas);

        if (mDebug)
            drawDebug(canvas);
    }

    private void drawDebugGridAndCoordinates(Canvas canvas) {
        try {
            mDebugPaint.setColor(Color.RED);
            mDebugPaint.setStrokeWidth(4);

            for(int x = 0; x < mGridSizeX; x++) {
                float startStopX = x * mGridCellSizeX - 2;
                canvas.drawLine(startStopX, 0, startStopX, getHeight(), mDebugPaint);

                for(int y = 0; y < mGridSizeY; y++) {
                    if (x == 0) {
                        // Only need to draw grid lines once.
                        float startStopY = y * mGridCellSizeY - 2;
                        canvas.drawLine(0, startStopY, getWidth(), startStopY, mDebugPaint);
                    }

                    canvas.drawText(
                            String.format("%d,%d", x, y),
                            x * mGridCellSizeX + 5,
                            y * mGridCellSizeY + mDebugPaint.getTextSize(),
                            mDebugPaint);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private void drawDebug(Canvas canvas) {
        try {
            mDebugPaint.setColor(Color.BLACK);
            mDebugPaint.setAlpha(100);
            mDebugPaint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(mDebugPaint);

            List<String> debugTextLines = new ArrayList<String>();
            debugTextLines.add(String.format("FPS: %d", GameGlobal.averageFps()));
            debugTextLines.add(String.format("DeltaTime: %f s", GameGlobal.deltaTime()));
            debugTextLines.add(String.format("CurrentTime: %.2f s", GameGlobal.currentTime()));
            debugTextLines.add(String.format("GridSize: %d x %d", mGridSizeX, mGridSizeY));
            debugTextLines.add(String.format("SpriteSize: %d x %d", mGridCellSizeX, mGridCellSizeY));
            debugTextLines.add(String.format("ActiveMoles: %d", mActiveMoles.size()));
            debugTextLines.add(String.format("CurrentTouch: %s", mCurrentTouch.toString()));

            drawDebutTextLines(canvas, debugTextLines);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void drawDebutTextLines(Canvas canvas, List<String> debugTextLines) {
        if (debugTextLines.size() == 0)
            return;

        mDebugPaint.setTextSize(mDebugTextSize);
        mDebugPaint.setAlpha(255);
        mDebugPaint.setColor(Color.WHITE);

        int increment = 0;
        for (String textLine : debugTextLines) {
            increment++;
            canvas.drawText(textLine, 5, mDebugTextSize * increment, mDebugPaint);
        }
    }

    public void update(float deltaTime) {
        // Overall game-logic should run first so sprite logic
        // benefit from the updates before next frame.
        logic();

        // Sprite logic
        for(Mole mole : mMoles) {
            mole.update();
        }
    }

    private void logic() {
        // TODO: Consider adding these as view attribute.
        int maxNumberOfActivatedMoles = mGridSizeX * mGridSizeY / 2;
        int minNumberOfActiveMoles = 1;
        float waitTime = 2f;

        if (!mRoundActive && GameGlobal.currentTime() > mWaitTilNextRound) {
            mRoundActive = true;
            mRoundStarted = GameGlobal.currentTime();

            int numberOfMolesToActivate = mRandom.nextInt(maxNumberOfActivatedMoles - minNumberOfActiveMoles) + minNumberOfActiveMoles;
            while (numberOfMolesToActivate > 0) {
                activateRandomMole();
                numberOfMolesToActivate--;
            }
        }

        if (mRoundActive && (GameGlobal.currentTime() > mRoundStarted + waitTime || mActiveMoles.size() == 0)) {
            mRoundActive = false;

            for (Mole activeMole : mActiveMoles) {
                activeMole.deactivate();
            }

            mWaitTilNextRound = GameGlobal.currentTime() + waitTime;
            mActiveMoles.clear();
        }
    }

    private void activateRandomMole() {
        if (this.mMoles.size() == 0)
            return;

        boolean randomMoleActivated = false;
        while (!randomMoleActivated) {
            Mole mole = mMoles.get(mRandom.nextInt(mMoles.size()));

            if(mole.isActive())
                continue;

            mole.activate();
            mActiveMoles.add(mole);
            randomMoleActivated = true;
        }
    }

    public void startGame() {
        // TODO: Start game, currently it will automatically start.
    }
}