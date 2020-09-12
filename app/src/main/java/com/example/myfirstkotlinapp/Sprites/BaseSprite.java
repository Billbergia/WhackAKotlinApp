package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Canvas;

public class BaseSprite implements IBaseSprite {
    public Vector2 position;

    public BaseSprite(Vector2 position) {
        this.position = position;
    }

    @Override
    public void draw(Canvas canvas) {

    }

    @Override
    public void update() {

    }
}
