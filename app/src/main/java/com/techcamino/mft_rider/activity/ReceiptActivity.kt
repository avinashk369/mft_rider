package com.techcamino.mft_rider.activity

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.adapters.SubOrderAdapter
import com.techcamino.mft_rider.apis.ApiClient
import com.techcamino.mft_rider.apis.ApiInterface
import com.techcamino.mft_rider.databinding.ActivityReceiptBinding
import com.techcamino.mft_rider.models.MessageDetail
import com.techcamino.mft_rider.models.orders.Order
import com.techcamino.mft_rider.models.orders.OrderDetail
import com.techcamino.mft_rider.permissionUtils.OnActivityResultListener
import com.techcamino.mft_rider.utils.ProgressDialog
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException


class ReceiptActivity : BaseActivity(), View.OnClickListener, OnActivityResultListener,
    SubOrderAdapter.OnItemClickListener {
    private lateinit var binding: ActivityReceiptBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    lateinit var phoneNumber: String
    lateinit var dialog: Dialog
    private lateinit var token: String
    private var order: Order.Result.Orders? = null
    private var subOrder: OrderDetail.Result.OrderInfo.Detail? = null
    private var pictureFilePath: String? = null
    private val MY_PERMISSIONS_REQUEST_CAMERA = 99
    private var isDelivered: Boolean = false
    private var imageView: ImageView? = null


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

        onActivityResultListener = this
        binding.deliveredBtn.setOnClickListener(this)
        binding.helpNumber.setOnClickListener(this)
        getOrderDetail(token, order?.orderId!!)

    }

    // check permission
    private fun checkPermissions(permission: kotlin.String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Required Camera Permission")
                    .setMessage("You have to give this permission to access camera")
                    .setPositiveButton("OK",
                        DialogInterface.OnClickListener { _, i ->
                            ActivityCompat.requestPermissions(
                                this, arrayOf(Manifest.permission.CAMERA),
                                MY_PERMISSIONS_REQUEST_CAMERA
                            )
                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                    .create()
                    .show()
            } else {
                // take permission
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)

            }
        } else {

            if (this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                clickPhoto(REQUEST_IMAGE_CAPTURE_WITHOUT_SCALE)
            } else {
                Toast.makeText(this, "No camera available on this device.", Toast.LENGTH_LONG)
                    .show()
            }
            //Toast.makeText(this,"Permission already granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun getPictureFile(fileName: kotlin.String, dirName: kotlin.String): File {

        val diren = this.resources.getString(R.string.app_name)
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imagePath = File(storageDir, diren)
        Log.d("avinash", "Find " + imagePath.absolutePath)
        if (!imagePath.exists()) {
            if (!imagePath.mkdirs()) {
                Log.d("CameraTestIntent", "failed to create directory");

            } else {
                Log.d("tag", "create new Tux folder");
            }
        }
        val imageName = subOrder?.subOrderId
        val image = File(imagePath, "$imageName$fileName.jpg")
        Log.d("image path", image.absolutePath)

        pictureFilePath = image.absolutePath

        return image
    }

    /**
     * testing image capture
     */
    private fun clickPhoto(requestCode: Int) {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.also {
            // Create the File where the photo should go
            val photoFile: File? = try {
                getPictureFile(subOrder?.subOrderId!!, "mft")
            } catch (ex: IOException) {
                Toast.makeText(this, "${ex.message}", Toast.LENGTH_LONG).show()
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "techcamino.mft_rider.provider",
                    it
                )

                mimeType = "image/*"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, getNewFileName(order?.orderId!!))
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        getImageDirectoryPath(this@ReceiptActivity.resources.getString(R.string.app_name))
                    )
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    Log.d("into if", photoURI.toString())
                } else {

                    val imageUri =
                        contentResolver.insert(
                            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                            values
                        )
                    Log.d("into elese", photoFile.toString())
                    if (imageUri != null) {

                        pictureFilePath = imageUri.toString()
                        shareUri = imageUri
                        Log.d("avinash", pictureFilePath!!)
                    }

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                }
                initRequestCode(takePictureIntent, requestCode)

            }
        }
    }

    private fun initRequestCode(takePictureIntent: Intent, requestImageCapture: Int) {
        currentRequestCode = requestImageCapture
        startActivityForResult.launch(takePictureIntent)
    }


    override fun onActivityResult(
        result: ActivityResult,
        currentRequestCode: Int
    ) {
        Log.d("testing file name", pictureFilePath!!)

        if (currentRequestCode == REQUEST_IMAGE_CAPTURE_WITHOUT_SCALE) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                val file = File(pictureFilePath!!)
                shareUri = FileProvider.getUriForFile(
                    this,
                    "techcamino.mft_rider.provider",
                    file
                )
            } else {


            }

            uploadImage(
                token,
                subOrder?.subOrderId!!,
                File(pictureFilePath!!)
            )
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.delivered_btn -> {
                if (isDelivered) {
                    markDelivered(token, order?.orderId!!)
                } else {
                    showSnack(R.string.upload_image_first)
                }
            }
            R.id.help_number -> {
                val num = this.resources.getString(R.string.help_number)
                Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$num")
                }.also { startActivity(it) }
            }
        }
    }

    override fun onStart() {
        supportActionBar?.title = "#${order?.orderId}"

        //binding.deliveredBtn.isEnabled=imageUploaded
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
                        if (response.body()?.result?.detail?.isEmpty()!!) {
                            binding.noData.visibility = View.VISIBLE
                        } else {
                            binding.noData.visibility = View.GONE
                            renderSubOrders(response.body()?.result?.detail!!)
                        }
                    }
                    Log.d("data getting", response.body()?.result?.orderInfo?.shippingCity!!)
                } else {
                    Intent(
                        this@ReceiptActivity,
                        LoginActivity::class.java
                    ).also {
                        startActivity(it)
                        finish()
                    }
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

    private fun markDelivered(token: String, orderId: String) {
        try {
            dialog.show()
            val markDeliver = apiService.markDelevered("Bearer $token", orderId)
            markDeliver.enqueue(object : Callback<MessageDetail> {
                override fun onResponse(
                    call: Call<MessageDetail>,
                    response: Response<MessageDetail>
                ) {
                    if (response.isSuccessful) {
                        isDelivered = false

                        Log.d("Order delivered", "Order delivered")
                        showSnack(R.string.order_delivered)
                    } else {
                        isDelivered = true
                    }
                    binding.deliveredBtn.isEnabled = isDelivered
                    if (dialog.isShowing)
                        dialog.dismiss()
                }

                override fun onFailure(call: Call<MessageDetail>, t: Throwable) {
                    Log.d("Failed", "Something went wrong")
                    if (dialog.isShowing)
                        dialog.dismiss()
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun uploadImage(
        token: String,
        orderId: String,
        imageUrl: File
    ) {
        try {

            dialog.show()
            Log.d("uploading", "uploading image started ${imageUrl.name}")

            // Parsing any Media type file
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)

            builder.addFormDataPart("sub_order_id", orderId)

            // Map is used to multipart the file using okhttp3.RequestBody
            // Multiple Images

            builder.addFormDataPart(
                "images",
                imageUrl.name,
                RequestBody.create(MediaType.parse("multipart/form-data"), imageUrl)
            )

            val requestBody = builder.build()

            val upload =
                apiService.uploadImage("Bearer $token", requestBody)
            upload.enqueue(object : Callback<MessageDetail> {
                override fun onResponse(
                    call: Call<MessageDetail>,
                    response: Response<MessageDetail>
                ) {
                    if (response.isSuccessful) {
                        isDelivered = true
                        showSnack(R.string.upload_image_success)
                        subOrder?.upImage = response.body()!!.result.vendorImage!!
                        //subOrder?.upImage = pictureFilePath

                        binding.suborders.adapter?.notifyDataSetChanged()

                    } else {
                        isDelivered = false
                        showSnack(R.string.upload_image_failed)
                        Log.d("Failed", "Image not uploaded")
                    }
                    if (dialog.isShowing)
                        dialog.dismiss()
                }

                override fun onFailure(call: Call<MessageDetail>, t: Throwable) {
                    Log.d("OnFailure", "Image not uploaded")
                    showSnack(R.string.upload_image_failed)
                    if (dialog.isShowing)
                        dialog.dismiss()
                }
            })
        } catch (e: Exception) {
            e.stackTrace
            if (dialog.isShowing)
                dialog.dismiss()
            Log.d("Exception", "Image upload failed" + e.printStackTrace())
        }
    }

    private fun showSnack(message: Int) {
        Snackbar.make(
            findViewById(R.id.context_view),
            message,
            Snackbar.LENGTH_LONG
        ).apply {
            setActionTextColor(
                Color.parseColor("#FFFFFF")
            )
        }
            .setAction("Ok", View.OnClickListener { // Request permission

            })
            .show()
    }

    private fun renderSubOrders(orders: ArrayList<OrderDetail.Result.OrderInfo.Detail>) {
        // this creates a vertical layout Manager
        binding.suborders.layoutManager =
            LinearLayoutManager(this@ReceiptActivity)
        // This will pass the ArrayList to our Adapter
        val adapter = SubOrderAdapter(orders, this@ReceiptActivity, this)
        adapter.setHasStableIds(true)
        // Setting the Adapter with the recyclerview
        binding.suborders.adapter = adapter
    }

    override fun onItemClick(order: OrderDetail.Result.OrderInfo.Detail, uImageView: ImageView) {
        Log.d("Suborder", order.subOrderId!!)
        subOrder = order
        imageView = uImageView
        checkPermissions(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA)
    }

    override fun onPause() {
        Log.d("pause", "Into on pause")
        super.onPause()
    }

    override fun onRestart() {
        Log.d("restart", "into restart")
        super.onRestart()
    }

    override fun onResume() {
        Log.d("resume", "in resume")
        super.onResume()
    }
}