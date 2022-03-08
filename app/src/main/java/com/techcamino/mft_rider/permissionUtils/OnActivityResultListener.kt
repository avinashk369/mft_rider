package com.techcamino.mft_rider.permissionUtils

import androidx.activity.result.ActivityResult

interface OnActivityResultListener{
        fun onActivityResult(
            result: ActivityResult,
            currentRequestCode: Int
        )
    }