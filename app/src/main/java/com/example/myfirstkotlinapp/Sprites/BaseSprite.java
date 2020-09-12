package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Canvas;

public class BaseSprite implements IBaseSprite {
    public Vector2 position;
    public int width;
    public int height;

    public BaseSprite(Vector2 position, int width, int height) {
        this.position = position;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Canvas canvas) {

    }

    @Override
    public void update() {

    }
}
