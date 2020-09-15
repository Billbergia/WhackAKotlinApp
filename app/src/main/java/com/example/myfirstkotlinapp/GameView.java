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

import com.example.myfirstkotlinapp.Sprites.Mole;
import com.example.myfirstkotlinapp.Sprites.MoleHole;
import com.example.myfirstkotlinapp.Sprites.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;

    private int mGridSize = 3;
    private int mGridSizeX = mGridSize;
    private int mGridSizeY = mGridSize;

    // Will be set in surfaceCreated()
    private int mGridCellSizeX = 0;
    private int mGridCellSizeY = 0;

    private boolean mRoundActive = false;
    private long mRoundStarted = 0;
    private long mWaitTilNextRound = 0;

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

    private Vector2 mLastDebugPress = new Vector2(-1,-1);
    private long mLastDebugPressTimeMs = 0;
    private boolean mDebug;
    private Paint mDebugPaint;

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
            getAttributesAndSetOptionFields(set);
    }

    private void getAttributesAndSetOptionFields(AttributeSet set) {
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

    private void createLevel() {
        // Consider below grid 3 x 3, this will be our default play field.
        // We'll span the mole underneath it's hole, see below grid example.
        // o = hole and x = mole

        //     0   1   2
        // 0 |   |   |   |
        // 1 |   | o |   |
        // 2 |   | x |   |

        mActiveMoles.clear();
        mMoleHoles.clear();
        mMoles.clear();

        for (int x = 0; x < mGridSizeX; x++) {
            for (int y = 0; y < mGridSizeY; y++) {
                mMoleHoles.add(new MoleHole(new Vector2(x, y), mHoleTexture, mHoleTextureForeground));
                mMoles.add(new Mole(new Vector2(x, y + 1), mMoleTextures));
            }
        }
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

                toggleDebugMode(dx, dy, event.getDownTime());

                // Only check if active mole was touched.
                // Downside with this is that won't be possible to
                // score on moles that are going down in the hole.

                // Also another downside is that for every single touch
                // this will loop through all active moles.
                for (Mole mole : mActiveMoles) {
                    if(mole.position.equals(dx, dy)) {
                        if(!mole.isActive())
                            break;

                        mole.inActivate();
                        mActiveMoles.remove(mole);
                        break;
                    }
                }
            }
        }

        return value;
    }

    private void toggleDebugMode(int dx, int dy, long eventDownTime) {
        if (mLastDebugPress.equals(dx, dy) && mLastDebugPressTimeMs + 200 > eventDownTime) {
            if (mLastDebugPress.equals(0, 0)) {
                mDebug = !mDebug;
            } else if (mDebug && mLastDebugPress.equals(0, mGridSizeY - 1)) {
                mGridSize -= 1;
                mGridSizeX = mGridSize;
                mGridSizeY = mGridSize;
                surfaceCreatedInit();
            } else if (mDebug && mLastDebugPress.equals(mGridSizeX - 1, mGridSizeY - 1)) {
                mGridSize += 1;
                mGridSizeX = mGridSize;
                mGridSizeY = mGridSize;
                surfaceCreatedInit();
            }

            mLastDebugPress.set(-1, -1);
        } else {
            mLastDebugPressTimeMs = eventDownTime;
            mLastDebugPress.set(dx, dy);
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (canvas == null)
            return;

        // A small hack that makes it so the mole is hidden behind below hole background texture.
        for (int i = 0; i < mMoleHoles.size(); i++) {
            mMoleHoles.get(i).draw(canvas);
            mMoles.get(i).draw(canvas);
            mMoleHoles.get(i).drawForeground(canvas);
        }

        if (mDebug)
            drawDebug(canvas);
    }

    @SuppressLint("DefaultLocale")
    private void drawDebug(Canvas canvas) {
        try {
            mDebugPaint.setColor(Color.RED);
            mDebugPaint.setStrokeWidth(4);
            for(int x = 0; x < mGridSizeX; x++) {
                canvas.drawLine(x * mGridCellSizeX - 2, 0, x * mGridCellSizeX - 2, getHeight(), mDebugPaint);
            }

            for(int y = 0; y < mGridSizeY; y++) {
                canvas.drawLine(0, y * mGridCellSizeY - 2, getWidth(), y * mGridCellSizeY - 2, mDebugPaint);
            }

            List<String> debugTextLines = new ArrayList<String>();
            debugTextLines.add(String.format("FPS: %d", GameGlobal.averageFps()));
            debugTextLines.add(String.format("GridSize: %d", mGridSize));
            debugTextLines.add(String.format("ActiveMoles: %d", mActiveMoles.size()));
            debugTextLines.add(String.format("CellWidth: %d", mGridCellSizeX));
            debugTextLines.add(String.format("CellHeight: %d", mGridCellSizeY));
            drawDebutTextLines(canvas, debugTextLines);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void drawDebutTextLines(Canvas canvas, List<String> debugTextLines) {
        if (debugTextLines.size() == 0)
            return;

        int textSize = 50;
        int strokeWidth = textSize * debugTextLines.size() + 20;

        mDebugPaint.setColor(Color.BLACK);
        mDebugPaint.setAlpha(100);
        mDebugPaint.setStrokeWidth(strokeWidth);
        canvas.drawLine(0, strokeWidth / 2, getWidth() / 2, strokeWidth / 2, mDebugPaint);

        int increment = 0;
        mDebugPaint.setColor(Color.WHITE);
        mDebugPaint.setTextSize(textSize);
        mDebugPaint.setAlpha(255);
        for (String textLine : debugTextLines) {
            increment++;
            canvas.drawText(textLine, 5, textSize * increment, mDebugPaint);
        }
    }

    public void update(float deltaTime) {
        // Overall game-logic should run first so sprite logic
        // benefit from the updates before next frame.
        logic();

        // Sprite logic
        /*
        for(MoleHole hole: mMoleHoles) {
            hole.update(elapsedTime);
        }
        */

        for(Mole mole : mMoles) {
            mole.update(deltaTime);
        }
    }

    private void logic() {
        long currentTime = System.currentTimeMillis();
        // TODO: Consider adding this as view attribute.
        long waitTime = 2000l;

        if (!mRoundActive && currentTime > mWaitTilNextRound) {
            mRoundActive = true;
            mRoundStarted = currentTime;

            int numberOfMolesToActivate = mRandom.nextInt((mGridSizeX * mGridSizeY / 2) - 1) + 1;
            while (numberOfMolesToActivate > 0) {
                activateRandomMole();
                numberOfMolesToActivate--;
            }
        }

        if (mRoundActive && (currentTime > mRoundStarted + waitTime || mActiveMoles.size() == 0)) {
            mRoundActive = false;

            for (Mole mole : mActiveMoles) {
                mole.inActivate();
            }

            mWaitTilNextRound = currentTime + waitTime;
            mActiveMoles.clear();
        }
    }

    private void activateRandomMole() {
        if (this.mMoles.size() == 0)
            return;

        boolean randomMoleActivated = false;
        while (!randomMoleActivated) {
            Mole mole = mMoles.get(mRandom.nextInt(mMoles.size()));
            if(!mole.isActive()) {
                mole.activate();
                mActiveMoles.add(mole);
                randomMoleActivated = true;
            }
        }
    }

    public void resetDraw() {
        // TODO: Start game, currently it will automatically start.
    }
}