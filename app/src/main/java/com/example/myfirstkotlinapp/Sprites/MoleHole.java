package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class MoleHole extends BaseSprite {

    private Bitmap backgroundTexture;
    private Bitmap foregroundTexture;

    public MoleHole(Vector2 position, Bitmap backgroundTexture, Bitmap foregroundTexture) {
        super(position);

        this.backgroundTexture = backgroundTexture;
        this.foregroundTexture = foregroundTexture;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.backgroundTexture, this.position.x * this.backgroundTexture.getWidth(), this.position.y * this.backgroundTexture.getHeight(), null);
    }

    public void drawForeground(Canvas canvas) {
        canvas.drawBitmap(this.foregroundTexture, this.position.x * this.foregroundTexture.getWidth(), this.position.y * this.foregroundTexture.getHeight(), null);
    }
}
