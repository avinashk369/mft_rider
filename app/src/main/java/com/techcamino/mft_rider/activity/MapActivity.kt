package com.techcamino.mft_rider.activity

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.apis.ApiClient
import com.techcamino.mft_rider.apis.ApiInterface
import com.techcamino.mft_rider.databinding.ActivityMapBinding
import com.techcamino.mft_rider.databinding.ActivityReceiptBinding
import com.techcamino.mft_rider.utils.ProgressDialog
import java.lang.Exception

class MapActivity : BaseActivity(),OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    lateinit var dialog: Dialog
    private lateinit var token: String
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
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
                this@MapActivity.resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
        token =
            shared.getString(this@MapActivity.resources.getString(R.string.access_token), "")!!

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun findContentView(): Int {
        return R.layout.activity_map
    }

    override fun bindViewWithViewBinding(view: View) {
        binding = ActivityMapBinding.bind(view)
    }

    override fun onStart() {

        super.onStart()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(
            MarkerOptions()
            .position(sydney)
            .title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}