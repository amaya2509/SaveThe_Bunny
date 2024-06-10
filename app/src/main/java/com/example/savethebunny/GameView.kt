package com.example.savethebunny

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import java.util.ArrayList
import java.util.Random

class GameView(context: Context) : View(context) {

    var background: Bitmap
    var ground: Bitmap
    var rabbit: Bitmap
    var rectBackground: Rect
    var rectGround: Rect
    var textPaint = Paint()
    var healthPaint = Paint()
    val UPDATE_MILLS = 30L
    val TEXT_SIZE = 120f
    var points = 0
    var life = 3
    companion object {
        var dWidth: Int = 0
        var dHeight: Int = 0
    }

    var random: Random
    var rabbitX: Float = 0f
    var rabbitY: Float = 0f
    var oldX: Float = 0f
    var oldRabbitX: Float = 0f
    var spikes = ArrayList<Spike>()
    var explosions = ArrayList<Explosion>()

    init {
        background = BitmapFactory.decodeResource(resources, R.drawable.background)
        ground = BitmapFactory.decodeResource(resources, R.drawable.ground)
        rabbit = BitmapFactory.decodeResource(resources, R.drawable.rabbit)
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        dWidth = size.x
        dHeight = size.y
        rectBackground = Rect(0, 0, dWidth, dHeight)
        rectGround = Rect(0, dHeight - ground.height, dWidth, dHeight)
        textPaint.color = Color.rgb(255, 165, 0)
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.kenny_blocks)
        healthPaint.color = Color.GREEN
        random = Random()
        rabbitX = (dWidth / 2 - rabbit.width / 2).toFloat()
        rabbitY = (dHeight - ground.height - rabbit.height).toFloat()

        for (i in 0 until 3) {
            val spike = Spike(context)
            spikes.add(spike)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(background, null, rectBackground, null)
        canvas.drawBitmap(ground, null, rectGround, null)
        canvas.drawBitmap(rabbit, rabbitX, rabbitY, null)
        for (i in spikes.indices) {
            spikes[i].getSpike(spikes[i].spikeFrame)?.let { canvas.drawBitmap(it, spikes[i].spikeX.toFloat(), spikes[i].spikeY.toFloat(), null) }
            spikes[i].spikeFrame++
            if (spikes[i].spikeFrame > 1) {
                spikes[i].spikeFrame = 0
            }
            spikes[i].spikeY += spikes[i].spikeVelocity
            if (spikes[i].spikeY + spikes[i].getSpikeHeight() >= dHeight - ground.height) {
                points += 10
                val explosion = Explosion(context)
                explosion.explosionX = spikes[i].spikeX
                explosion.explosionY = spikes[i].spikeY
                explosions.add(explosion)
                spikes[i].resetPosition()
            }
        }

        for (i in spikes.indices) {
            if (spikes[i].spikeX + spikes[i].getSpikeWidth() >= rabbitX
                && spikes[i].spikeX <= rabbitX + rabbit.width
                && spikes[i].spikeY + spikes[i].getSpikeHeight() >= rabbitY
                && spikes[i].spikeY + spikes[i].getSpikeHeight() <= rabbitY + rabbit.height) {
                life--
                spikes[i].resetPosition()
                if (life == 0) {
                    val intent = Intent(context, GameOver::class.java)
                    intent.putExtra("points", points)
                    context.startActivity(intent)
                    (context as Activity).finish()
                }
            }
        }

        for (i in explosions.indices) {
            explosions[i].getExplosion(explosions[i].explosionFrame)?.let {
                canvas.drawBitmap(
                    it, explosions[i].explosionX.toFloat(),
                    explosions[i].explosionY.toFloat(), null)
            }
            explosions[i].explosionFrame++
            if (explosions[i].explosionFrame > 1 ) {
                explosions.removeAt(i)
            }
        }

        if (life == 2) {
            healthPaint.color = Color.YELLOW
        } else if (life == 1) {
            healthPaint.color = Color.RED
        }

        canvas.drawRect(dWidth - 200.toFloat(), 30.toFloat(), dWidth - 200 + 60 * life.toFloat(), 80.toFloat(), healthPaint)
        canvas.drawText("" + points, 20.toFloat(), TEXT_SIZE, textPaint)
        postDelayed(runnable, UPDATE_MILLS)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (touchY >= rabbitY) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                oldX = event.x
                oldRabbitX = rabbitX
            }
            if (action == MotionEvent.ACTION_MOVE) {
                val shift = oldX - touchX
                val newAvatarX = oldRabbitX - shift
                rabbitX = when {
                    newAvatarX <= 0 -> 0f
                    newAvatarX >= dWidth - rabbit.width -> (dWidth - rabbit.width).toFloat()
                    else -> newAvatarX
                }
            }
        }
        return true
    }

    private val runnable = Runnable {
        invalidate()
    }
}
