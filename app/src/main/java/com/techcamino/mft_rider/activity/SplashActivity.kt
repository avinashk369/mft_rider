package com.techcamino.mft_rider.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.apis.ApiInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    lateinit var shared: SharedPreferences
    private lateinit var token: String
    private lateinit var mobile: String
    private lateinit var name: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shared =
            getSharedPreferences(
                this@SplashActivity.resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
        token =
            shared.getString(this@SplashActivity.resources.getString(R.string.access_token), "")!!
        mobile =
            shared.getString("mobile", "")!!
        name =
            shared.getString(this@SplashActivity.resources.getString(R.string.user_name), "")!!

        lifecycleScope.launch {
            delay(3000L)
            if (token != "") {
                Intent(
                    this@SplashActivity,
                    HomeActivity::class.java
                ).apply {
                    putExtra("mobile",mobile)
                    putExtra("name",name)
                }.also {
                    startActivity(it)
                    finish()
                }
            } else {
                Intent(
                    this@SplashActivity,
                    LoginActivity::class.java
                ).apply {
                    putExtra("name", "Avinash")
                }.also {
                    startActivity(it)
                    finish()
                }
            }

        }
    }
}