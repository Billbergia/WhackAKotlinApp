package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class MoleHole extends BaseSprite {

    private Bitmap texture;

    public MoleHole(Vector2 position, int width, int height, Bitmap texture) {
        super(position, width, height);

        this.texture = texture;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.texture, this.position.x * this.width, this.position.y * this.height, null);
    }
}
