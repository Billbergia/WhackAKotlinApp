package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class MoleHole extends BaseSprite {

    private Bitmap texture;
    private Bitmap foregroundTexture;

    public MoleHole(Vector2 position, Bitmap texture, Bitmap foregroundTexture) {
        super(position);

        this.texture = texture;
        this.foregroundTexture = foregroundTexture;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.texture, this.position.x * this.texture.getWidth(), this.position.y * this.texture.getHeight(), null);
    }

    public void drawForeground(Canvas canvas) {
        canvas.drawBitmap(this.foregroundTexture, this.position.x * this.foregroundTexture.getWidth(), this.position.y * this.foregroundTexture.getHeight(), null);
    }
}
