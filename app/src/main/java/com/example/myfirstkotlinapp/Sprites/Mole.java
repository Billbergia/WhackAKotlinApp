package com.example.myfirstkotlinapp.Sprites;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Mole extends BaseSprite {

    private Vector2 startPosition;
    private boolean active = false;
    private int velocity = 3;

    private int testing = 0;

    private Paint paint;

    public Mole(Vector2 position, int width, int height) {
        super(position, width, height);

        this.startPosition = position;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
    }

    @Override
    public void draw(Canvas canvas) {
        if(!this.active)
            return;

        canvas.drawCircle(
                this.position.x * this.width + this.width / 2,
                this.position.y * this.height - testing + this.height / 2,
                this.width / 4, paint);

        testing += 10;
    }

    @Override
    public void update() {
        //if(this.active && this.position.y < this.position.y * 2) {
        //    this.position.y += 0.8f;
        //}
    }

    public void activate() {
        this.active = true;
        System.out.println("Mole activated.");
    }

    public void inActivate() {
        System.out.println("Mole deactivated.");
        this.active = false;
        position.y = startPosition.y;
    }

    public boolean isActive() {
        return this.active;
    }
}