package com.techcamino.mft_rider.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        lifecycleScope.launch {
            delay(3000L)

            Intent(this@SplashActivity,MainActivity::class.java).apply {
                putExtra("name","Avinash")
            }.also {
                startActivity(it)
                finish()
            }
        }
    }
}