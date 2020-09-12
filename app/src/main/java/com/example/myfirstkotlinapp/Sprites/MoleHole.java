package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class MoleHole extends BaseSprite {

    private Bitmap texture;

    public MoleHole(Vector2 position, Bitmap texture) {
        super(position);

        this.texture = texture;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.texture, this.position.x * texture.getWidth(), this.position.y * this.texture.getHeight(), null);
    }
}
