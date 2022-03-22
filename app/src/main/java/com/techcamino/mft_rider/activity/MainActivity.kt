package com.techcamino.mft_rider.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.apis.ApiClient
import com.techcamino.mft_rider.apis.ApiInterface
import com.techcamino.mft_rider.models.MessageDetail
import com.techcamino.mft_rider.models.UserModel
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// github repo
class MainActivity : AppCompatActivity() {
    lateinit var apiService: ApiInterface
    private var coroutineJob: Job? = null
    private val gson = Gson()

    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiService = ApiClient.apiInterface
        //getQuot()
        //riderLogin()
        dispatchTakePictureIntent()

    }


    private fun riderLogin() {
        val riderLogin = apiService.checkLogin("8510074200", "123456")
        riderLogin.enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    Log.d("Success", response.body()!!.status.toString())
                } else {
                    Log.d("failed", response.errorBody()!!.toString())
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                Log.d("Success", t.toString())
            }
        })
    }

    private fun getQuotes() {
        val quotes = apiService.getQuotes()
        quotes.enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    val userModel: UserModel = response.body()!!
                    if (userModel.status) {
                    } else {
                        try {
                            var messageDetails = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                MessageDetail::class.java
                            )
                            Log.d("Avinash", messageDetails.result.error.otp)
                        } catch (e: Exception) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        var messageDetails = gson.fromJson(
                            response.errorBody()!!.charStream(),
                            MessageDetail::class.java
                        )
                        Log.d("Avinash", messageDetails.result.error.otp)
                    } catch (e: Exception) {
                        e.printStackTrace();
                    }
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                Log.d("Failed", "Something went wrong")
            }
        })
    }

    private fun getQuot() {
        coroutineJob = CoroutineScope(Dispatchers.IO).launch {
            val response = apiService.getQuote()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.d("ayush: ", response.body()!!.toString())
                } else {
                    try {
                        var messageDetails = gson.fromJson(
                            response.errorBody()!!.charStream(),
                            MessageDetail::class.java
                        )
                        Log.d("Avinash", messageDetails.result.error.otp)
                    } catch (e: Exception) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    lateinit var currentPhotoPath: String
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "techcamino.mft_rider.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("image detaol", currentPhotoPath)
            uploadImage("",File(currentPhotoPath),)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun uploadImage(
        token: String,
        imageUrl: File,
    ) {
        try {

            Log.d("uploading", "uploading image started ${imageUrl.name}")

            // Parsing any Media type file

            // Parsing any Media type file
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)

            builder.addFormDataPart("owner_id", "123")
            builder.addFormDataPart("owner_type", "1")

            // Map is used to multipart the file using okhttp3.RequestBody
            // Multiple Images

            builder.addFormDataPart(
                "image_url",
                imageUrl.name,
                RequestBody.create(MediaType.parse("multipart/form-data"), imageUrl)
            )

            val requestBody = builder.build()

            var upload =
                apiService.uploadImage("Bearer $token", requestBody)
            upload.enqueue(object : Callback<MessageDetail> {
                override fun onResponse(
                    call: Call<MessageDetail>,
                    response: Response<MessageDetail>
                ) {
                    if (response.isSuccessful) {
                        Log.d("Success", "Image uploaded")
                    } else {
                        Log.d("Failed", "Image not uploaded")
                    }

                }

                override fun onFailure(call: Call<MessageDetail>, t: Throwable) {
                    Log.d("OnFailure", "Image not uploaded")

                }
            })
        } catch (e: Exception) {
            e.stackTrace
            Log.d("Exception", "Image upload failed" + e.printStackTrace())
        }
    }


    override fun onPause() {
        coroutineJob?.cancel()
        super.onPause()
    }
}