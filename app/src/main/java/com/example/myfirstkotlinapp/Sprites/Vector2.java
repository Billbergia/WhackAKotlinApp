package com.example.myfirstkotlinapp.Sprites;

public class Vector2 {
    public int x;
    public int y;

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean Compare(int x, int y) {
        if (x == this.x && y == this.y) {
            return true;
        }

        return false;
    }

    public boolean Compare(Vector2 vec) {
        return this.Compare(vec.x, vec.y);
    }
}