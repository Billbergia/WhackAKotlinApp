package com.example.myfirstkotlinapp.Sprites;

public class Vector2 {
    public float x;
    public float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean compare(float x, float y) {
        return x == this.x && y == this.y;
    }

    public boolean compare(Vector2 vec) {
        return this.compare(vec.x, vec.y);
    }

    public Vector2 createCopy() {
        return new Vector2(this.x, this.y);
    }
}