package com.techcamino.mft_rider.activity

import android.app.Dialog
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
import com.techcamino.mft_rider.databinding.ActivityOtpBinding
import com.techcamino.mft_rider.models.UserModel
import com.techcamino.mft_rider.utils.ProgressDialog
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response

class OtpActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityOtpBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    lateinit var phoneNumber: String
    private var coroutineJob: Job? = null
    private val gson = Gson()
    lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = ProgressDialog.progressDialog(this)
        phoneNumber = intent.getStringExtra("mobile")!!
        Log.d("phonenumber", phoneNumber)

        apiService = ApiClient.apiInterface
        shared =
            getSharedPreferences(this.resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        binding.otpVerify.setOnClickListener(this)
    }

    override fun findContentView(): Int {
        return R.layout.activity_otp
    }

    override fun bindViewWithViewBinding(view: View) {
        binding = ActivityOtpBinding.bind(view)
    }

    /**
     * validate if registration number has given or not
     * @return
     */
    private fun validateField(): Boolean {
        if (binding.otpNumber.text.toString().trim().length < 6) {
            binding.otpNumber.error = "Please enter otp"
            binding.otpNumber.requestFocus()
            return false
        }
        return true
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.otp_verify -> {
                if (!validateField()) return
                checkLogin(phoneNumber, binding.otpNumber.text.toString())
            }

        }
    }

    private fun checkLogin(mobile: String, otp: String) {
        dialog.show()
       val riderLogin =
            apiService.checkLogin(mobile, otp)
        riderLogin.enqueue(object : retrofit2.Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    val userModel: UserModel = response.body()!!
                    if (userModel.status) {
                        binding.verify.visibility=View.GONE
                        val edit = shared.edit()
                        edit.putString(
                            this@OtpActivity.resources.getString(R.string.access_token),
                            userModel.result.token
                        )
                        edit.putString(
                            "mobile",
                            userModel.result.mobile
                        )
                        edit.putString(
                            this@OtpActivity.resources.getString(R.string.user_name),
                            userModel.result.name
                        )
//                        Toast.makeText(this@OtpActivity, userModel.result.token, Toast.LENGTH_SHORT)
//                            .show()
                        edit.apply()
                        Intent(this@OtpActivity, HomeActivity::class.java).apply {
                            putExtra("mobile",userModel.result.mobile)
                            putExtra("name",userModel.result.name)
                        }.also {
                            startActivity(it)
                            finish()
                        }
                    } else {
                        binding.verify.visibility=View.VISIBLE
                        binding.verify.text =userModel.result.error.otp
                        Log.d("error",userModel.result.error.otp)
                    }
                } else {
                    Log.d("failed", response.errorBody()!!.toString())
                }
                if (dialog.isShowing)
                    dialog.dismiss()
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                Log.d("Success", t.toString())
                if (dialog.isShowing)
                    dialog.dismiss()
            }
        })

    }
}