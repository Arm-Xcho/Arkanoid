package com.example.arkanoid

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arkanoid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var ballX = 0f
    private var ballY = 0f

    private var ballSpeedX = 0f
    private var ballSpeedY = 0f

    private var score = 0

    private val brickRows = 10
    private val brickColumns = 10
    private val brickWidth = 100
    private val brickHeight = 40
    private val brickMargin = 4

    private var isBallLaunched = false

    private var lives = 3


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.newgame.setOnClickListener {
            binding.brickContainer.removeAllViews()
            initializeBricks()
            lives = 3
            start()
            //  movepaddle()
            binding.newgame.visibility = View.INVISIBLE
        }
    }

    private fun initializeBricks() {

        for (row in 0 until brickRows) {
            val rowLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = params

            for (col in 0 until brickColumns) {
                val brick = View(this)
                val brickParams = LinearLayout.LayoutParams(brickWidth, brickHeight)
                brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin)
                brick.layoutParams = brickParams
                brick.setBackgroundResource(R.drawable.brick_background)
                rowLayout.addView(brick)
            }
            binding.brickContainer.addView(rowLayout)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun checkCollision() {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        if (ballX <= 0 || ballX + binding.ball.width >= screenWidth) {
            ballSpeedX *= -1
        }

        if (ballY <= 0) {
            ballSpeedY *= -1
        }

        //paddle
        if (ballY + binding.ball.height >= binding.paddle.y && ballY + binding.ball.height <= binding.paddle.y + binding.paddle.height && ballX + binding.ball.width >= binding.paddle.x && ballX <= binding.paddle.x + binding.paddle.width) {
            ballSpeedY *= -1
        }

        //bricks
        for (row in 0 until brickRows) {
            val rowLayout = binding.brickContainer.getChildAt(row) as LinearLayout

            val rowTop = rowLayout.y + binding.brickContainer.y

            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View

                if (brick.visibility == View.VISIBLE) {
                    val brickLeft = brick.x + rowLayout.x
                    val brickRight = brickLeft + brick.width
                    val brickTop = brick.y + rowTop
                    val brickBottom = brickTop + brick.height

                    if (ballX + binding.ball.width >= brickLeft && ballX <= brickRight && ballY + binding.ball.height >= brickTop && ballY <= brickBottom) {
                        brick.visibility = View.INVISIBLE
                        ballSpeedY *= -1
                        score++
                        binding.scoreText.text = "Score: $score"
                        return  // Exit the function after finding a collision with a brick
                    }
                }
            }
        }

        // paddle misses the ball
        if (ballY + binding.ball.height >= screenHeight - 100) {
            lives--
            if (lives > 0) {
                Toast.makeText(this, "$lives balls left ", Toast.LENGTH_SHORT).show()
            }
            if (lives <= 0) {
                gameOver()
            } else {
                resetBallPosition()
                start()
            }
        }
    }

    private fun resetBallPosition() {
        val displayMetrics = resources.displayMetrics

        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        binding.ball.x = screenWidth
        binding.ball.y = screenHeight

        ballSpeedX = 0f
        ballSpeedY = 0f

        binding.paddle.x = screenWidth - binding.paddle.width

    }

    private fun gameOver() {
        binding.scoreText.text = "Game Over"
        score = 0
        binding.newgame.visibility = View.VISIBLE
    }

    private fun start() {
        movePaddle()
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density

        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        binding.paddle.x = screenWidth / 2 - binding.paddle.width / 2

        ballX = screenWidth / 2  // - binding.ball.width / 2
        ballY = screenHeight / 2 //  - binding.ball.height / 2

        ballSpeedX = 5 * screenDensity
        ballSpeedY = -5 * screenDensity

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = Long.MAX_VALUE
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { _ ->
            moveBall()
            checkCollision()
        }
        animator.start()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun movePaddle() {
        binding.paddle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    var x: Float = event.rawX
                    binding.paddle.x = x - binding.paddle.width / 2
                }

            }
            true
        }
    }

    private fun moveBall() {
        ballX += ballSpeedX
        ballY += ballSpeedY

        binding.ball.x = ballX
        binding.ball.y = ballY
    }
}

