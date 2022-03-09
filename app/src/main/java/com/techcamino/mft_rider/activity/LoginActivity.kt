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
import com.techcamino.mft_rider.databinding.ActivityLoginBinding
import com.techcamino.mft_rider.models.MessageDetail
import com.techcamino.mft_rider.models.UserModel
import com.techcamino.mft_rider.models.orders.Data
import com.techcamino.mft_rider.models.orders.OrderHistory
import com.techcamino.mft_rider.utils.ProgressDialog
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

class LoginActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    private var coroutineJob: Job? = null
    private val gson = Gson()
    lateinit var dialog: Dialog


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
        dialog = ProgressDialog.progressDialog(this)
        apiService = ApiClient.apiInterface
        shared =
            getSharedPreferences(this.resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        binding.loginBtn.setOnClickListener(this)
    }

    /**
     * validate if registration number has given or not
     * @return
     */
    private fun validateField(): Boolean {
        if (binding.phoneNumber.text.toString().trim().length < 10) {
            binding.phoneNumber.error = "Please enter phone number"
            binding.phoneNumber.requestFocus()
            return false
        }
        return true
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.login_btn -> {
                if (!validateField()) return
                checkRider()
            }

        }
    }


    private fun checkRider() {
        try {
            dialog.show()
            var rider = apiService.checkRider(binding.phoneNumber.text.toString().trim())
            rider.enqueue(object : retrofit2.Callback<MessageDetail> {
                override fun onResponse(
                    call: Call<MessageDetail>,
                    response: Response<MessageDetail>
                ) {
                    if (response.isSuccessful) {
                        Intent(this@LoginActivity, OtpActivity::class.java).apply {
                            putExtra("mobile", binding.phoneNumber.text.trim().toString())
                        }.also {
                            startActivity(it)
                        }
                    } else {
                        try {
                            var messageDetails = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                MessageDetail::class.java
                            )
                            binding.verify.visibility = View.VISIBLE
                            binding.verify.text = messageDetails.result.error.mobile
                            Log.d("error", messageDetails.result.error.mobile)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace();
                        }
                    }
                    if (dialog.isShowing)
                        dialog.dismiss()
                }

                override fun onFailure(call: Call<MessageDetail>, t: Throwable) {
                    Log.d("OnFailure", "Something went wrong")
                    if (dialog.isShowing)
                        dialog.dismiss()
                }

            })
        } catch (e: Exception) {
        }

    }
}