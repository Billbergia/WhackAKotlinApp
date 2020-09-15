package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.myfirstkotlinapp.GameGlobal;

import java.util.List;
import java.util.Random;

public class Mole extends BaseSprite {

    private Vector2 startPosition;
    private Vector2 targetPosition;
    private List<Bitmap> textures;
    private Bitmap currentTexture;
    private float speed = 5.0f;

    private boolean active = false;
    private Random random = new Random();

    public Mole(Vector2 position, List<Bitmap> textures) {
        super(position);

        this.startPosition = position.copy();
        this.targetPosition = new Vector2(position.x, position.y - 1);
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
    public void update(float elapsedTime) {
        if(this.active && this.position.y > this.targetPosition.y) {
            this.position.y -= speed * GameGlobal.deltaTime();
            if (this.position.y < this.targetPosition.y)
                this.position.y = this.targetPosition.y;
        } else if (!this.active && this.position.y < this.startPosition.y) {
            this.position.y += speed * GameGlobal.deltaTime() * 2;
            if (this.position.y > this.startPosition.y)
                this.position.y = this.startPosition.y;
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