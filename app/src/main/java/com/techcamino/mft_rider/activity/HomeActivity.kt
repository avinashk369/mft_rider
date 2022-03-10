package com.techcamino.mft_rider.activity


import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.apis.ApiClient
import com.techcamino.mft_rider.apis.ApiInterface
import com.techcamino.mft_rider.databinding.ActivityHomeBinding
import com.techcamino.mft_rider.utils.ProgressDialog
import com.techcamino.mft_rider.models.orders.OrderHistory

import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.techcamino.mft_rider.adapters.OrderAdapter
import com.techcamino.mft_rider.models.orders.Order
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


import androidx.core.view.MenuItemCompat

import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.techcamino.mft_rider.models.MessageDetail
import com.techcamino.mft_rider.models.orders.Data


class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener, OrderAdapter.OnItemClickListener {
    private lateinit var binding: ActivityHomeBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    lateinit var phoneNumber: String
    lateinit var name: String
    lateinit var dialog: Dialog
    lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var token: String
    private lateinit var orderId: String
    private lateinit var orderData: Order.Result.Orders
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = ProgressDialog.progressDialog(this)
        phoneNumber = intent.getStringExtra("mobile")!!
        name = intent.getStringExtra("name")!!
//        Log.d("phonenumber", phoneNumber)

        apiService = ApiClient.apiInterface
        shared =
            getSharedPreferences(
                this@HomeActivity.resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
            )

        binding.appBar.toolbar.title = ""
        setSupportActionBar(binding.appBar.toolbar)
        supportActionBar?.setIcon(R.drawable.bg)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        val toggle = ActionBarDrawerToggle(
            this@HomeActivity,
            binding.drawerLayout,
            binding.appBar.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        toggle.setHomeAsUpIndicator(R.drawable.ic_baseline_fiber_pin_24)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        binding.logoutLayout.setOnClickListener(this)
        val headerView = binding.navView.getHeaderView(0)

        // get user name and email textViews
        val userName = headerView.findViewById<View>(R.id.user_name) as TextView
        val mobileNumber = headerView.findViewById<View>(R.id.mobile) as TextView
        userName.text = name
        mobileNumber.text = phoneNumber

//        appIcon = headerView.findViewById<View>(R.id.imageView)
//        Glide.with(context).load(Constants.APP_ICON_URL)
//            .thumbnail(.5f)
//            .fitCenter()
//            .into<Target<Drawable>>(appIcon)
    }

    override fun onStart() {
        token = shared.getString(this@HomeActivity.resources.getString(R.string.access_token), "")!!
        // call api to get orders
        getOrders(token, "all")
        // get all order history
        getOrderHistory(token)
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val menuItem: MenuItem = menu!!.findItem(R.id.action_cart)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_cart -> {
                Log.d("Shopping cart", "Shopping cart items")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun findContentView(): Int {
        return R.layout.activity_home
    }

    override fun bindViewWithViewBinding(view: View) {
        binding = ActivityHomeBinding.bind(view)
    }

    private fun getOrderHistory(token: String) {
        try {
            val orderHistory = apiService.getOrderHistory("Bearer $token")
            orderHistory.enqueue(object : Callback<Data> {
                override fun onResponse(call: Call<Data>, response: Response<Data>) {
                    if (response.isSuccessful) {
                        if (response.body()!!.status!!) {
                            val orderHistory: OrderHistory = response.body()!!.result!!
                            Log.d("accepted order count", orderHistory.acceptedOrders.toString())
                            setMenuItemVal(orderHistory)

                        }

                    } else {
                        Log.d("accepted order count", "Whats going ons")
                        Intent(
                            this@HomeActivity,
                            LoginActivity::class.java
                        ).also {
                            startActivity(it)
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<Data>, t: Throwable) {
                    Log.d("Failed", "Order history failed here")
                }
            })
        } catch (e: Exception) {
            Log.d("Exception", "Something went wrong getting history")
        }
    }

    private fun setMenuItemVal(orderHistory: OrderHistory) {
        val item =
            (binding.navView.menu.findItem(R.id.delivered).actionView) as TextView
        item.gravity = Gravity.CENTER_VERTICAL
        item.setTypeface(null, Typeface.BOLD)
        item.text = orderHistory.deliveredOrders.toString()
        // pending order
        val pending =
            (binding.navView.menu.findItem(R.id.pending).actionView) as TextView
        pending.gravity = Gravity.CENTER_VERTICAL
        pending.setTypeface(null, Typeface.BOLD)
        pending.text = orderHistory.pendingOrders.toString()
        // accepted order
        val accepted =
            (binding.navView.menu.findItem(R.id.accepted).actionView) as TextView
        accepted.gravity = Gravity.CENTER_VERTICAL
        accepted.setTypeface(null, Typeface.BOLD)
        accepted.text = orderHistory.acceptedOrders.toString()
        // all order
        val all =
            (binding.navView.menu.findItem(R.id.all).actionView) as TextView
        all.gravity = Gravity.CENTER_VERTICAL
        all.setTypeface(null, Typeface.BOLD)
        all.text =
            (orderHistory.acceptedOrders!! + orderHistory.pendingOrders!! + orderHistory.deliveredOrders!!).toString()
    }

    private fun getOrders(token: String, type: String) {
        try {
            val orders = apiService.getAllOrders("Bearer $token", type, "1")
            orders.enqueue(object : Callback<Order> {
                override fun onResponse(call: Call<Order>, response: Response<Order>) {
                    if (response.isSuccessful) {
                        val orderList: Order = response.body()!!
                        Log.d("order limit", orderList.result?.orders?.size.toString())
                        if (orderList.result?.orders?.isEmpty()!!) {
                            binding.appBar.orderListView.dashboard.noData.visibility = View.VISIBLE
                        } else {
                            binding.appBar.orderListView.dashboard.noData.visibility = View.GONE
                        }
                        if (orderList.status!!) {
                            renderOrders(orderList.result?.orders!!)
                        }

                    } else {
                        Intent(
                            this@HomeActivity,
                            LoginActivity::class.java
                        ).also {
                            startActivity(it)
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<Order>, t: Throwable) {
                    Log.d("On Failure","Something went wrong")
                }

            })
        } catch (e: Exception) {
            Log.d("Exception", "Something went wrong getting order")
        }

    }

    private fun renderOrders(orders: ArrayList<Order.Result.Orders>) {
        // this creates a vertical layout Manager
        binding.appBar.orderListView.dashboard.orderList.layoutManager =
            LinearLayoutManager(this@HomeActivity)
//        binding.appBar.orderListView.dashboard.orderList.addItemDecoration(
//            androidx.recyclerview.widget.DividerItemDecoration(
//                this@HomeActivity,
//                androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
//            )
//        )


        // This will pass the ArrayList to our Adapter
        val adapter = OrderAdapter(orders, this@HomeActivity, this)

        // Setting the Adapter with the recyclerview
        binding.appBar.orderListView.dashboard.orderList.adapter = adapter
    }


    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
        finish()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delivered -> {
                Log.d("menu", "Deleivered")
                getOrders(token, "delivered_orders")
            }
            R.id.accepted -> {
                Log.d("menu", "accepted")
                getOrders(token, "accepted_orders")
            }
            R.id.pending -> {
                Log.d("menu", "pending")
                getOrders(token, "pending_orders")
            }
            R.id.all -> {
                Log.d("menu", "All")
                getOrders(token, "all")
            }

        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.logout_layout -> {
                shared.edit().clear().apply()
                val logout = Intent(this@HomeActivity, LoginActivity::class.java)
                startActivity(logout)
                finish()
            }
            R.id.submit_decline -> {
                val editText = bottomSheetDialog.findViewById<EditText>(R.id.reason)
                if (!validateField()) return
                Log.d("Reason", orderId)
                updateOrderStatus(token, orderId, "decline", editText?.text?.trim().toString())
                orderData.riderStatus = "declined_orders"
                binding.appBar.orderListView.dashboard.orderList.adapter?.notifyDataSetChanged()
                bottomSheetDialog.dismiss()
            }
        }
    }

    private fun validateField(): Boolean {
        val editText = bottomSheetDialog.findViewById<EditText>(R.id.reason)
        if (editText?.text.toString().trim().isEmpty()) {
            editText?.error = "Please enter a valid reason"
            editText?.requestFocus()
            return false
        }
        return true
    }

    override fun onItemClick(order: Order.Result.Orders) {
        Log.d("Order detail", order.address!!)
        Intent(
            this@HomeActivity,
            ReceiptActivity::class.java
        ).apply {
            putExtra("order", order)
        }.also {
            startActivity(it)
        }
    }

    override fun changeState(order: Order.Result.Orders, status: Boolean) {
        orderId = order.orderId!!
        orderData = order
        if (status && order.riderStatus?.lowercase() != "accepted_orders") {
            Log.d("state", "Order accepted $status")
            updateOrderStatus(token, order.orderId!!, "accept", "Accepted")
            order.riderStatus = "accepted_orders"
            binding.appBar.orderListView.dashboard.orderList.adapter?.notifyDataSetChanged()
        }
        if (!status && order.riderStatus?.lowercase() != "declined_orders") {
            Log.d("state", "Order declined $status")
            showBottomSheetDialog()
        }
    }

    override fun viewMap(order: Order.Result.Orders) {
        Log.d("Map", "Show map")
//        Intent(
//            this@HomeActivity,
//            MapActivity::class.java
//        ).apply {
//            putExtra("order", order)
//        }.also {
//            startActivity(it)
//        }
    }

    private fun updateOrderStatus(token: String, orderId: String, status: String, reason: String) {
        try {
            dialog.show()
            val orderStatus = apiService.updateOrderStatus("Bearer $token", orderId, status, reason)
            orderStatus.enqueue(object : Callback<MessageDetail> {
                override fun onResponse(
                    call: Call<MessageDetail>,
                    response: Response<MessageDetail>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()!!.status) {
                            Toast.makeText(
                                this@HomeActivity,
                                response.body()!!.message,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {

                        }
                    }
                    if (dialog.isShowing)
                        dialog.dismiss()
                }

                override fun onFailure(call: Call<MessageDetail>, t: Throwable) {
                    Log.d("Exception", "Something wrong with api")
                    if (dialog.isShowing)
                        dialog.dismiss()
                }
            })
        } catch (e: Exception) {

        }

    }

    override fun onResume() {
        Log.d("resume", "Onresume")
        super.onResume()
    }

    override fun onRestart() {
        Log.d("restart", "Onrestart")
        super.onRestart()
    }

    private fun showBottomSheetDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.bottom_sheet, null)

        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
        val submit = bottomSheetDialog.findViewById<CardView>(R.id.submit_decline)
        submit!!.setOnClickListener(this)
    }
}