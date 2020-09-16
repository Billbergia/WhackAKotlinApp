package com.example.myfirstkotlinapp.gamecore;

import android.graphics.Canvas;

/**
 *
 */
public abstract class BaseSprite implements IBaseSprite {
    public Vector2 position;

    public BaseSprite(Vector2 position) {
        this.position = position;
    }

    /**
     * This method should be used for drawing the sprite on the canvas.
     * @param canvas The canvas to draw on.
     */
    @Override
    public abstract void draw(Canvas canvas);

    /**
     * This method should be used for game logic, such as controlling movement of the sprite.
     */
    @Override
    public abstract void update();
}
