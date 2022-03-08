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
import com.techcamino.mft_rider.utils.ProgressDialog

import java.lang.Exception


class ReceiptActivity : BaseActivity() {
    private lateinit var binding: ActivityReceiptBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    lateinit var phoneNumber: String
    lateinit var dialog: Dialog
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title="Receipt"

        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        dialog = ProgressDialog.progressDialog(this)
//        phoneNumber = intent.getStringExtra("mobile")!!
//        c

        apiService = ApiClient.apiInterface
        shared =
            getSharedPreferences(
                this@ReceiptActivity.resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
        token = shared.getString(this@ReceiptActivity.resources.getString(R.string.access_token), "")!!
        val order = intent.getParcelableExtra<Order.Result.Orders>("order")
        Log.d("phonenumber", order?.address!!)
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
}