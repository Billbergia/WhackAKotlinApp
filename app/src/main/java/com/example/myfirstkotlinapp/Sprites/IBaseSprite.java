package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Canvas;

public interface IBaseSprite {
    void draw(Canvas canvas);
    void update(float elapsedTime);
}
