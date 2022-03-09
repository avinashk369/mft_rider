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
    lateinit var name: String
    lateinit var dialog: Dialog
    private lateinit var token: String
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
        userName.text=name
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
        getOrders(token,"All")
        // get all order history
        getOrderHistory(token)
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main,menu)
        val menuItem:MenuItem= menu!!.findItem(R.id.action_cart)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_cart->{
                Log.d("Shopping cart","Shopping cart items")
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
        val orderHistory = apiService.getOrderHistory("Bearer $token")
        orderHistory.enqueue(object : Callback<Data> {
            override fun onResponse(call: Call<Data>, response: Response<Data>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status!!) {
                        val orderHistory: OrderHistory = response.body()!!.result!!
                        Log.d("accepted order count", orderHistory.acceptedOrders.toString())
                        setMenuItemVal(orderHistory)

                    } else {

                    }

                }
            }

            override fun onFailure(call: Call<Data>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setMenuItemVal(orderHistory: OrderHistory): Unit {
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
        all.text = (orderHistory.acceptedOrders!!+orderHistory.pendingOrders!!+orderHistory.deliveredOrders!!).toString()
    }

    private fun getOrders(token: String,type:String) {
        val orders = apiService.getAllOrders("Bearer $token", type, "1")
        orders.enqueue(object : Callback<Order> {
            override fun onResponse(call: Call<Order>, response: Response<Order>) {
                if (response.isSuccessful) {
                    val orderList: Order = response.body()!!
                    Log.d("order limit", orderList.result?.orders?.size.toString())
                    if(orderList.result?.orders?.isEmpty()!!){
                        binding.appBar.orderListView.dashboard.noData.visibility=View.VISIBLE
                    }else{
                        binding.appBar.orderListView.dashboard.noData.visibility=View.GONE
                    }
                    if (orderList.status!!) {
                        renderOrders(orderList.result?.orders!!)
                    }else{
                        Intent(
                            this@HomeActivity,
                            LoginActivity::class.java
                        ).also {
                            startActivity(it)
                            finish()
                        }
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
        when(item.itemId){
            R.id.delivered->{
                Log.d("menu", "Deleivered")
                getOrders(token,"delivered_orders")
            }
            R.id.accepted->{
                Log.d("menu", "accepted")
                getOrders(token,"accepted_orders")
            }
            R.id.pending->{
                Log.d("menu", "pending")
                getOrders(token,"pending_orders")
            }
            R.id.all->{
                Log.d("menu", "All")
                getOrders(token,"all")
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
        }
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
        if (status) {
            Log.d("state", "Order accepted $status")
        } else {
            Log.d("state", "Order decline $status")
        }
    }

    override fun viewMap(order: Order.Result.Orders) {
        Log.d("Map","Show map")
//        Intent(
//            this@HomeActivity,
//            MapActivity::class.java
//        ).apply {
//            putExtra("order", order)
//        }.also {
//            startActivity(it)
//        }
    }
}