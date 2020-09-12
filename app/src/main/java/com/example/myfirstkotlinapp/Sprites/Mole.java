package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.List;
import java.util.Random;

public class Mole extends BaseSprite {

    private Vector2 startPosition;
    private List<Bitmap> textures;
    private Bitmap currentTexture;

    private boolean active = false;
    private Random random = new Random();

    public Mole(Vector2 position, List<Bitmap> textures) {
        super(position);

        this.startPosition = position.createCopy();
        this.textures = textures;

        setRandomTexture();
    }

    @Override
    public void draw(Canvas canvas) {
        if(!this.active && position.y >= startPosition.y)
            return;

        canvas.drawBitmap(currentTexture, position.x * currentTexture.getWidth(), position.y * currentTexture.getHeight(), null);
    }

    @Override
    public void update() {
        if(this.active && this.position.y > this.startPosition.y - 1) {
            this.position.y -= 0.25f;
        } else if (!this.active && this.position.y <= this.startPosition.y) {
            this.position.y += 0.25f;
        }
    }

    public void activate() {
        this.active = true;
        setRandomTexture();
    }

    public void inActivate() {
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }

    private void setRandomTexture() {
        int randomIndex = random.nextInt(textures.size());
        currentTexture = textures.get(randomIndex);
    }
}