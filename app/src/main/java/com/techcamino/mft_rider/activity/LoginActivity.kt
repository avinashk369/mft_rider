package com.techcamino.mft_rider.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.apis.ApiClient
import com.techcamino.mft_rider.apis.ApiInterface
import com.techcamino.mft_rider.databinding.ActivityLoginBinding
import com.techcamino.mft_rider.models.UserModel
import com.techcamino.mft_rider.models.orders.Data
import com.techcamino.mft_rider.models.orders.OrderHistory
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

class LoginActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    lateinit var shared : SharedPreferences
    lateinit var apiService: ApiInterface
    private var coroutineJob: Job? = null
    private val gson = Gson()

    override fun findContentView(): Int {
        return R.layout.activity_login
    }

    override fun bindViewWithViewBinding(view: View) {
        binding = ActivityLoginBinding.bind(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiClient.apiInterface
        shared = getSharedPreferences(this.resources.getString(R.string.app_name) , Context.MODE_PRIVATE)
        binding.loginBtn.setOnClickListener(this)
    }

    /**
     * validate if registration number has given or not
     * @return
     */
    private fun validateField(): Boolean {
        if (binding.phoneNumber.text.toString().trim().length<10) {
            binding.phoneNumber.error = "Please enter phone number"
            binding.phoneNumber.requestFocus()
            return false
        }
        return true
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.login_btn -> {
                if(!validateField())return

                Intent(this@LoginActivity,OtpActivity::class.java).apply {
                    putExtra("mobile",binding.phoneNumber.text.trim().toString())
                }.also {
                    startActivity(it)
                }
            }

        }
    }

    private fun saveToken( text:String){
        val edit = shared.edit()
        //edit.putString(this.resources.getString(R.string.access_token) , text)
        Toast.makeText(this , "Was Saved" , Toast.LENGTH_SHORT).show()
        edit.apply()
    }

    private fun getOrderHistory(){
        val riderLogin = apiService.getOrderHistory("Bearer AbwMx5s0pudOiHYEpEO1zFcpw8WCKCMd_6227059a818fa",)
        riderLogin.enqueue(object: retrofit2.Callback<Data> {
            override fun onResponse(call: Call<Data>, response: Response<Data>) {
                if(response.isSuccessful){
                    Log.d("Success",response.body()!!.result!!.acceptedOrders.toString())
                }else{
                    Log.d("failed",response.errorBody()!!.toString())
                }
            }

            override fun onFailure(call: Call<Data>, t: Throwable) {
                Log.d("Success",t.toString())
            }
        })
    }
}