package com.example.myfirstkotlinapp.sprites

import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.myfirstkotlinapp.gamecore.BaseSprite
import com.example.myfirstkotlinapp.gamecore.Vector2

class MoleHole(
    position: Vector2,
    private val backgroundTexture: Bitmap,
    private val foregroundTexture: Bitmap) : BaseSprite(position) {

    override fun draw(canvas: Canvas?) {
        canvas?.drawBitmap(
            backgroundTexture,
            position.x * backgroundTexture.width,
            position.y *backgroundTexture.height,
            null
        )
    }

    fun drawForeground(canvas: Canvas?) {
        canvas?.drawBitmap(
            foregroundTexture,
            position.x * foregroundTexture.width,
            position.y *foregroundTexture.height,
            null
        )
    }

    override fun update() {
        // Nothing needed here yet.
    }
}