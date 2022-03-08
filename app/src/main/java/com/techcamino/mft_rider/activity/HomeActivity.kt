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
import androidx.recyclerview.widget.LinearLayoutManager
import com.techcamino.mft_rider.adapters.OrderAdapter
import com.techcamino.mft_rider.models.orders.Order
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


import androidx.core.view.MenuItemCompat

import android.widget.TextView
import com.techcamino.mft_rider.models.orders.Data


class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener, OrderAdapter.OnItemClickListener {
    private lateinit var binding: ActivityHomeBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    lateinit var phoneNumber: String
    lateinit var dialog: Dialog
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = ProgressDialog.progressDialog(this)
//        phoneNumber = intent.getStringExtra("mobile")!!
//        Log.d("phonenumber", phoneNumber)

        apiService = ApiClient.apiInterface
        shared =
            getSharedPreferences(
                this@HomeActivity.resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
        token = shared.getString(this@HomeActivity.resources.getString(R.string.access_token), "")!!
        // call api to get orders
        getOrders(token)
        // get all order history
        getOrderHistory(token)
        //binding.appBar.toolbar.title = this@HomeActivity.resources.getString(R.string.app_name)
        setSupportActionBar(binding.appBar.toolbar)
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


        // get user name and email textViews
//        userName = headerView.findViewById<View>(R.id.user_name)
//        mobileNumber = headerView.findViewById<View>(R.id.mobile)
//        appIcon = headerView.findViewById<View>(R.id.imageView)
//        Glide.with(context).load(Constants.APP_ICON_URL)
//            .thumbnail(.5f)
//            .fitCenter()
//            .into<Target<Drawable>>(appIcon)
    }

    override fun findContentView(): Int {
        return R.layout.activity_home
    }

    override fun bindViewWithViewBinding(view: View) {
        binding = ActivityHomeBinding.bind(view)
    }

    private fun getOrderHistory(token:String){
        val orderHistory = apiService.getOrderHistory("Bearer $token")
        orderHistory.enqueue(object :Callback<Data>{
            override fun onResponse(call: Call<Data>, response: Response<Data>) {
                if(response.isSuccessful){
                    if(response.body()!!.status!!) {
                        val orderHistory: OrderHistory = response.body()!!.result!!
                        Log.d("accepted order count",orderHistory.acceptedOrders.toString())
                        val item = (binding.navView.menu.findItem(R.id.nav_profile).actionView) as TextView
                        item.gravity= Gravity.CENTER_VERTICAL
                        item.setTypeface(null, Typeface.BOLD)
                        item.text=orderHistory.acceptedOrders.toString()

                    }else{

                    }

                }
            }

            override fun onFailure(call: Call<Data>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun getOrders(token: String) {
        val orders = apiService.getAllOrders("Bearer $token", "All", "1")
        orders.enqueue(object : Callback<Order> {
            override fun onResponse(call: Call<Order>, response: Response<Order>) {
                if (response.isSuccessful) {
                    val orderList: Order = response.body()!!
                    Log.d("order limit", orderList.result?.orders?.size.toString())
                    if(orderList.status!!){
                        renderOrders(orderList.result?.orders!!)
                    }

                }
            }

            override fun onFailure(call: Call<Order>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun renderOrders(orders: ArrayList<Order.Result.Orders>) {
        // this creates a vertical layout Manager
        binding.appBar.orderListView.dashboard.orderList.layoutManager =
            LinearLayoutManager(this@HomeActivity)

        // This will pass the ArrayList to our Adapter
        val adapter = OrderAdapter(orders,this@HomeActivity,this)

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
        val id = item.itemId
//       if (id == R.id.nav_wish_list) {
//            val wishList = Intent(context, WishList::class.java)
//            wishList.putExtra(Constants.USER, userDetails)
//            startActivity(wishList)
//        } else if (id == R.id.nav_order_history) {
//            val orderHistory = Intent(context, OrderHistory::class.java)
//            orderHistory.putExtra(Constants.USER, userDetails)
//            startActivity(orderHistory)
//        }
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
        }
    }

    override fun onItemClick(order: Order.Result.Orders) {
        Log.d("Order detail",order.address!!)
        Intent(
            this@HomeActivity,
            ReceiptActivity::class.java
        ).apply {
            putExtra("order", order)
        }.also {
            startActivity(it)
            finish()
        }
    }
}