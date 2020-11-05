package com.example.myfirstkotlinapp.sprites

import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.myfirstkotlinapp.gamecore.BaseSprite
import com.example.myfirstkotlinapp.gamecore.GameGlobal
import com.example.myfirstkotlinapp.gamecore.Vector2
import java.util.*

class Mole(position: Vector2, var textures: List<Bitmap>) : BaseSprite(position) {

    var active: Boolean = false
    set(value) {
        if (value) {
            setRandomTexture()
        }
        field = value
    }

    private val random = Random()
    private val speed: Float = 5.0f

    private val startPosition: Vector2 = position.copy()
    private val targetPosition: Vector2 = Vector2(position.x, position.y - 1)

    private lateinit var currentTexture: Bitmap

    init {
        setRandomTexture()
    }

    override fun draw(canvas: Canvas?) {
        if(!active && position.y >= startPosition.y)
            return;

        canvas?.drawBitmap(
            currentTexture,
            position.x * currentTexture.width,
            position.y * currentTexture.height,
            null)
    }

    override fun update() {
        if(active && position.y > targetPosition.y) {
            position.y -= speed * GameGlobal.deltaTime()
            if(position.y < targetPosition.y)
                position.y = targetPosition.y
        } else if (!active && position.y < startPosition.y) {
            position.y += speed * GameGlobal.deltaTime() * 2
            if(position.y > startPosition.y)
                position.y = startPosition.y
        }
    }

    private fun setRandomTexture() {
        val randomIndex = random.nextInt(textures.size)
        currentTexture = textures[randomIndex]
    }
}