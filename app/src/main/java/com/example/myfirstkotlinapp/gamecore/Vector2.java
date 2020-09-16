package com.example.myfirstkotlinapp.gamecore;

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

    /**
     * Normalize this vector. This means that the vector is turned into a unit vector with the
     * magnitude of one.
     * @return The instance of this vector.
     */
    public Vector2 normalize() {
        double length = Math.sqrt(this.x * this.x + this.y * this.y);
        if (length != 0f) {
            float s = 1.0f / (float) length;
            this.x = this.x * s;
            this.y = this.y * s;
        }

        return this;
    }

    /**
     * Subtracts a vector from this vector.
     * @param other
     * @return The instance of this vector.
     */
    public Vector2 subtract(Vector2 other) {
        this.x -= other.x;
        this.y -= other.y;

        return this;
    }

    /**
     * Adds a vector to this vector.
     * @param other
     * @return The instance of this vector.
     */
    public Vector2 add(Vector2 other) {
        this.x += other.x;
        this.y += other.y;

        return this;
    }

    /**
     * Multiplies the vector x and y by a scalar.
     * @param scalar The scalar to multiply
     * @return The instance of this Vector2
     */
    public Vector2 multiply(float scalar) {
        this.x *= scalar;
        this.y *= scalar;

        return this;
    }

    /**
     * Compare x and y of this vector.
     * @param x
     * @param y
     * @return True if comparison is equal.
     */
    public boolean equals(float x, float y) {
        return x == this.x && y == this.y;
    }

    /**
     * Compare a vector to this vector.
     * @param other
     * @return True if comparison is equal.
     */
    public boolean equals(Vector2 other) {
        return this.equals(other.x, other.y);
    }

    /**
     * Set the x and y of this vector.
     * @param x
     * @param y
     * @return The instance of this vector.
     */
    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;

        return this;
    }

    /**
     * Create a copy of this vector.
     * @return A copy of this vector.
     */
    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }

    public String toString() {
        return String.format("x:%.1f y:%.1f", this.x, this.y);
    }
}