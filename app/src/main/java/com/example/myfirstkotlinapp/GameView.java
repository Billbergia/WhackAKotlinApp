package com.example.myfirstkotlinapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myfirstkotlinapp.gamecore.BitmapHelper;
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

    private GameThread mThread;

    // Grid
    private int mGridSize = 3;
    private int mGridSizeX = mGridSize;
    private int mGridSizeY = mGridSize;
    private int mGridCellSizeX = 0;
    private int mGridCellSizeY = 0;

    // Game logic
    private boolean mRoundActive = false;
    private float mRoundStarted = 0f;
    private float mWaitTilNextRound = 0f;

    // Sprites
    private Random mRandom = new Random();
    private List<Mole> mMoles = new ArrayList<Mole>();
    private List<Mole> mActiveMoles = new ArrayList<Mole>();
    private List<MoleHole> mMoleHoles = new ArrayList<MoleHole>();

    // Textures
    private int mHoleTextureId;
    private int mHoleTextureForegroundId;
    private int[] mMoleTextureIds;
    private Bitmap mHoleBackgroundTexture;
    private Bitmap mHoleForegroundTexture;
    private List<Bitmap> mMoleTextures = new ArrayList<Bitmap>();

    // Input
    private Vector2 mPreviousTouch = new Vector2();
    private Vector2 mCurrentTouch = new Vector2();
    private long mPreviousTouchTimeMs = 0;

    // Debug
    private List<String> mDebutTextLines = new ArrayList<String>();
    private boolean mDebug = false;
    private boolean mDebugGridAndCoordinates = false;
    private int mDebugTextSize = 50;
    private Paint mDebugGridAndCoordinatesPaint;
    private Paint mDebugBackgroundPaint;
    private Paint mDebugTextPaint;

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
        mThread = new GameThread(getHolder(), this);

        if (set != null)
            getAttributesAndInitializeOptionFields(set);
    }

    private void getAttributesAndInitializeOptionFields(AttributeSet set) {
        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.GameView);

        mGridSize = ta.getInt(R.styleable.GameView_grid_size, 3);
        mGridSizeX = mGridSize;
        mGridSizeY = mGridSize;

        mHoleTextureId = ta.getResourceId(R.styleable.GameView_hole_background_texture, R.drawable.wak_default_hole);
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

        mThread.setRunning(true);
        mThread.start();
    }

    private void surfaceCreatedInit() {
        mGridCellSizeX = getWidth() / mGridSizeX;
        mGridCellSizeY = getHeight() / mGridSizeY;

        initializePaints();
        initializeTextures();
        createLevel();
    }

    private void initializePaints() {
        mDebugGridAndCoordinatesPaint = new Paint();
        mDebugGridAndCoordinatesPaint.setColor(Color.RED);
        mDebugGridAndCoordinatesPaint.setTextSize(mDebugTextSize);
        mDebugGridAndCoordinatesPaint.setStrokeWidth(4);

        mDebugBackgroundPaint = new Paint();
        mDebugBackgroundPaint.setColor(Color.BLACK);
        mDebugBackgroundPaint.setStyle(Paint.Style.FILL);
        mDebugBackgroundPaint.setAlpha(100);

        mDebugTextPaint = new Paint();
        mDebugTextPaint.setColor(Color.WHITE);
        mDebugTextPaint.setTextSize(mDebugTextSize);
    }

    private void initializeTextures() {
        mMoleTextures.clear();

        mHoleBackgroundTexture = BitmapHelper.resizeBitmap(getResources(), mHoleTextureId, mGridCellSizeX, mGridCellSizeY);
        mHoleForegroundTexture = BitmapHelper.resizeBitmap(getResources(), mHoleTextureForegroundId, mGridCellSizeX, mGridCellSizeY);

        for (int moleTextureId : mMoleTextureIds) {
            mMoleTextures.add(
                    BitmapHelper.resizeBitmap(getResources(), moleTextureId, mGridCellSizeX, mGridCellSizeY));
        }
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
                mMoleHoles.add(new MoleHole(new Vector2(x, y), mHoleBackgroundTexture, mHoleForegroundTexture));
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
                mThread.setRunning(false);
                mThread.join();
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
    * Also another downside is that for every single touch this will loop through all active moles.
    * */
    private void activeMoleTouched() {
        for (Mole mole : mActiveMoles) {
            if (mole.position.equals(mCurrentTouch)) {
                if (!mole.getActive())
                    break;

                mole.setActive(false);
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

        mDebugGridAndCoordinatesPaint.setTextSize(mDebugTextSize);
        mDebugGridAndCoordinatesPaint.setColor(Color.RED);
        mDebugGridAndCoordinatesPaint.setAlpha(255);

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
            mDebugGridAndCoordinatesPaint.setColor(Color.RED);
            mDebugGridAndCoordinatesPaint.setStrokeWidth(4);

            for(int x = 0; x < mGridSizeX; x++) {
                float startStopX = x * mGridCellSizeX - 2;
                canvas.drawLine(startStopX, 0, startStopX, getHeight(), mDebugGridAndCoordinatesPaint);

                for(int y = 0; y < mGridSizeY; y++) {
                    if (x == 0) {
                        // Only need to draw grid lines once.
                        float startStopY = y * mGridCellSizeY - 2;
                        canvas.drawLine(0, startStopY, getWidth(), startStopY, mDebugGridAndCoordinatesPaint);
                    }

                    canvas.drawText(
                            String.format("%d,%d", x, y),
                            x * mGridCellSizeX + 5,
                            y * mGridCellSizeY + mDebugGridAndCoordinatesPaint.getTextSize(),
                            mDebugGridAndCoordinatesPaint);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private void drawDebug(Canvas canvas) {
        try {
            mDebutTextLines.clear();
            mDebutTextLines.add(String.format("FPS: %d", GameGlobal.averageFps()));
            mDebutTextLines.add(String.format("DeltaTime: %f s", GameGlobal.deltaTime()));
            mDebutTextLines.add(String.format("CurrentTime: %.2f s", GameGlobal.currentTime()));
            mDebutTextLines.add(String.format("GridSize: %d x %d", mGridSizeX, mGridSizeY));
            mDebutTextLines.add(String.format("SpriteSize: %d x %d", mGridCellSizeX, mGridCellSizeY));
            mDebutTextLines.add(String.format("ActiveMoles: %d", mActiveMoles.size()));
            mDebutTextLines.add(String.format("CurrentTouch: %s", mCurrentTouch.toString()));

            drawDebutTextLines(canvas, mDebutTextLines);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /** Draws a debug overlay with the given text lines. */
    private void drawDebutTextLines(Canvas canvas, List<String> debugTextLines) {
        canvas.drawPaint(mDebugBackgroundPaint);

        if (debugTextLines.size() == 0)
            return;

        for (int i = 0; i < debugTextLines.size(); i++) {
            canvas.drawText(debugTextLines.get(i), 5, mDebugTextSize * (i + 1), mDebugTextPaint);
        }
    }

    /**
     * Update is the method where we calculate all the game logic such as
     * movement of the game objects etc. This is called before draw() once for every frame.
     */
    public void update() {
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
                activeMole.setActive(false);
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

            if(mole.getActive())
                continue;

            mole.setActive(true);
            mActiveMoles.add(mole);
            randomMoleActivated = true;
        }
    }

    public void startGame() {
        // TODO: Start game, currently it will automatically start.
    }
}