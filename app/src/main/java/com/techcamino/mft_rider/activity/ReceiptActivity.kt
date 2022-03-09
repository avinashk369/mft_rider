package com.techcamino.mft_rider.activity

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.apis.ApiClient
import com.techcamino.mft_rider.apis.ApiInterface
import com.techcamino.mft_rider.databinding.ActivityHomeBinding
import com.techcamino.mft_rider.databinding.ActivityReceiptBinding
import com.techcamino.mft_rider.models.orders.Order
import com.techcamino.mft_rider.models.orders.OrderDetail
import com.techcamino.mft_rider.utils.ProgressDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.lang.Exception


class ReceiptActivity : BaseActivity() {
    private lateinit var binding: ActivityReceiptBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    lateinit var phoneNumber: String
    lateinit var dialog: Dialog
    private lateinit var token: String
    private var order: Order.Result.Orders? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        dialog = ProgressDialog.progressDialog(this)
//        phoneNumber = intent.getStringExtra("mobile")!!


        apiService = ApiClient.apiInterface
        shared =
            getSharedPreferences(
                this@ReceiptActivity.resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
        token =
            shared.getString(this@ReceiptActivity.resources.getString(R.string.access_token), "")!!
        order = intent.getParcelableExtra<Order.Result.Orders>("order")
        Log.d("phonenumber", order?.address!!)
    }

    override fun onStart() {
        supportActionBar?.title = "#${order?.orderId}"
        getOrderDetail(token, order?.orderId!!)
        super.onStart()
    }

    override fun findContentView(): Int {
        return R.layout.activity_receipt
    }

    override fun bindViewWithViewBinding(view: View) {
        binding = ActivityReceiptBinding.bind(view)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun getOrderDetail(token: String, orderId: String) {
        val orderDetail = apiService.getOrderDetail("Bearer $token", orderId)
        orderDetail.enqueue(object : Callback<OrderDetail> {
            override fun onResponse(call: Call<OrderDetail>, response: Response<OrderDetail>) {
                if (response.isSuccessful) {
                    if (response.body()?.status!!) {
                        renderDetail(response.body()?.result?.orderInfo!!)
                    }
                    Log.d("data getting", response.body()?.result?.orderInfo?.shippingCity!!)
                }
            }

            override fun onFailure(call: Call<OrderDetail>, t: Throwable) {
                Log.d("data getting", "failed")
            }

        })
    }

    private fun renderDetail(orderInfo: OrderDetail.Result.OrderInfo) {
        binding.recName.text = orderInfo.shippingFirstname
        binding.delCity.text = orderInfo.shippingCity
        binding.recNum.text = orderInfo.shippingTelephone
        binding.altNum.text =
            if (orderInfo.shippingAlternateTelephone?.lowercase() == ("null")) "" else orderInfo.shippingAlternateTelephone
        binding.recAddress.text = orderInfo.shippingAddress1
        binding.addressType.text = orderInfo.shippingAddressType
    }
}