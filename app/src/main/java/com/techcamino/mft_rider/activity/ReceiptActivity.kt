package com.techcamino.mft_rider.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.apis.ApiClient
import com.techcamino.mft_rider.apis.ApiInterface
import com.techcamino.mft_rider.databinding.ActivityHomeBinding
import com.techcamino.mft_rider.databinding.ActivityReceiptBinding
import com.techcamino.mft_rider.models.orders.Order
import com.techcamino.mft_rider.models.orders.OrderDetail
import com.techcamino.mft_rider.permissionUtils.OnActivityResultListener
import com.techcamino.mft_rider.utils.ProgressDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

import java.lang.Exception


class ReceiptActivity : BaseActivity(),View.OnClickListener, OnActivityResultListener {
    private lateinit var binding: ActivityReceiptBinding
    lateinit var shared: SharedPreferences
    lateinit var apiService: ApiInterface
    lateinit var phoneNumber: String
    lateinit var dialog: Dialog
    private lateinit var token: String
    private var order: Order.Result.Orders? = null

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var resultLauncher1: ActivityResultLauncher<Intent>
    private var pictureFilePath: String? = null
    private var imageName:String?=null
    private val MY_PERMISSIONS_REQUEST_CAMERA = 99
    private val MY_PERMISSIONS_REQUEST_STORAGE = 88
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

        //result of open camera
        // this is new way to handle intent
        // onActivityResult is deprecated now
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleCameraImage(result.data)
                }
            }
        // this is for setting intent result capture
        resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                print("success")
            }
        }
    }

    // check permission
    private fun checkPermissions(permission:kotlin.String, requestCode:Int){
        if(ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Required Camera Permission")
                    .setMessage("You have to give this permission to access camera")
                    .setPositiveButton("OK",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            ActivityCompat.requestPermissions(
                                this, arrayOf(Manifest.permission.CAMERA),
                                MY_PERMISSIONS_REQUEST_CAMERA
                            )
                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                    .create()
                    .show()
            }else{
                // take permission
                ActivityCompat.requestPermissions(this, arrayOf(permission),requestCode)

            }
        }else{
            //openCamera(editText.text.toString().trim(),binding.panchayatName.text.toString())
            if(this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
                clickPhoto(REQUEST_IMAGE_CAPTURE_WITHOUT_SCALE)
            }else{
                Toast.makeText(this,"No camera available on this device.", Toast.LENGTH_LONG).show()
            }
            //Toast.makeText(this,"Permission already granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun getPictureFile(fileName:kotlin.String,dirName:kotlin.String): File {
        val diren = this.resources.getString(R.string.app_name)
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imagePath = File(storageDir, diren)
        Log.d("avinash", "Find " + imagePath.absolutePath)
        if (! imagePath.exists()){
            if (! imagePath.mkdirs()){
                Log.d("CameraTestIntent", "failed to create directory");

            }else{
                Log.d("tag","create new Tux folder");
            }
        }
        val regNumberHint = order?.orderId
        val image = File(imagePath, "$regNumberHint$fileName.jpg")
        Log.d("image path",image.absolutePath)

        pictureFilePath = image.absolutePath

        return image
    }


    private fun handleCameraImage(intent: Intent?) {
        val spl = pictureFilePath?.split("/")
        val da = spl?.get(spl?.size-1)
        val imgFile = File(pictureFilePath)

        print(imgFile.absolutePath.toString())
        Log.d("Avinash kumar",imgFile.absolutePath)
        val splitted = imgFile.absolutePath.split("/")
        binding.uploadedImage.visibility=View.VISIBLE
        Glide.with(this).load(imgFile).into(binding.uploadedImage)
//        val bitmap = intent?.extras?.get("data") as Bitmap
    }


    /**
     * testing image capture
     */
    fun clickPhoto(requestCode: Int) {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.also {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile(this.resources.getString(R.string.app_name),order?.orderId!!)
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
                    Log.d("into if",photoURI.toString())
                } else {

                    val imageUri =
                        contentResolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), values)
                    Log.d("into elese",photoFile.toString())
                    if (imageUri != null) {
                        currentPhotoPath = imageUri.toString()
                        shareUri = imageUri
                    }
                    val splitted = photoFile.toString().split("/")
                    imageName = splitted[splitted.size-1]
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
        Log.d("testing file name",currentPhotoPath)
        if (currentRequestCode == REQUEST_IMAGE_CAPTURE_WITHOUT_SCALE) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                var file = File(currentPhotoPath)
                shareUri = FileProvider.getUriForFile(
                    this,
                    "techcamino.mft_rider.provider",
                    file
                )
                //binding.imageView.load(currentPhotoPath)
                val splitted = currentPhotoPath.split("/")
                binding.uploadedImage.visibility=View.VISIBLE
                Glide.with(this).load(currentPhotoPath).into(binding.uploadedImage)
            } else {
                var image: Bitmap = getBitmapFromContentResolver(Uri.parse(currentPhotoPath))
                Log.d("image path",currentPhotoPath)
                // binding.imageView.load(currentPhotoPath)
                val splitted = currentPhotoPath.split("/")
                binding.uploadedImage.visibility=View.VISIBLE
                Glide.with(this).load(currentPhotoPath).into(binding.uploadedImage)
            }
            //to show image in gallery
            addImageInGallery()
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.delivered_btn->{
                checkPermissions(Manifest.permission.CAMERA ,MY_PERMISSIONS_REQUEST_CAMERA)
            }
        }
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