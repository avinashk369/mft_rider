package com.techcamino.mft_rider.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.apis.ApiClient
import com.techcamino.mft_rider.apis.ApiInterface
import com.techcamino.mft_rider.models.UserModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.techcamino.mft_rider.models.MessageDetail
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    lateinit var apiService: ApiInterface
    private var coroutineJob: Job? = null
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiService = ApiClient.apiInterface
        //getQuot()
        riderLogin()

    }

    private fun riderLogin() {
        val riderLogin = apiService.checkLogin("8510074200", "123456")
        riderLogin.enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    Log.d("Success", response.body()!!.status.toString())
                } else {
                    Log.d("failed", response.errorBody()!!.toString())
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                Log.d("Success", t.toString())
            }
        })
    }

    private fun getQuotes() {
        val quotes = apiService.getQuotes()
        quotes.enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    val userModel: UserModel = response.body()!!
                    if (userModel.status) {
                    } else {
                        try {
                            var messageDetails = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                MessageDetail::class.java
                            )
                            Log.d("Avinash", messageDetails.result.error.otp)
                        } catch (e: Exception) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        var messageDetails = gson.fromJson(
                            response.errorBody()!!.charStream(),
                            MessageDetail::class.java
                        )
                        Log.d("Avinash", messageDetails.result.error.otp)
                    } catch (e: Exception) {
                        e.printStackTrace();
                    }
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                Log.d("Failed", "Something went wrong")
            }
        })
    }

    private fun getQuot() {
        coroutineJob = CoroutineScope(Dispatchers.IO).launch {
            val response = apiService.getQuote()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.d("ayush: ", response.body()!!.toString())
                } else {
                    try {
                        var messageDetails = gson.fromJson(
                            response.errorBody()!!.charStream(),
                            MessageDetail::class.java
                        )
                        Log.d("Avinash", messageDetails.result.error.otp)
                    } catch (e: Exception) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    override fun onPause() {
        coroutineJob?.cancel()
        super.onPause()
    }
}