package com.example.myfirstkotlinapp.Sprites;

public class Vector2 {
    public float x;
    public float y;

    public Vector2() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 normalize() {
        double length = Math.sqrt(this.x * this.x + this.y * this.y);
        if (length != 0f) {
            float s = 1.0f / (float) length;
            this.x = this.x * s;
            this.y = this.y * s;
        }

        return this;
    }

    public Vector2 subtract(Vector2 other) {
        this.x -= other.x;
        this.y -= other.y;

        return this;
    }

    public Vector2 add(Vector2 other) {
        this.x += other.x;
        this.y += other.y;

        return this;
    }

    public Vector2 multiply(float scalar) {
        this.x *= scalar;
        this.y *= scalar;

        return this;
    }

    public boolean equals(float x, float y) {
        return x == this.x && y == this.y;
    }

    public boolean equals(Vector2 vec) {
        return this.equals(vec.x, vec.y);
    }

    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;

        return this;
    }

    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }
}