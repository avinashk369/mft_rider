package com.techcamino.mft_rider.models.orders

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("status") var status: Boolean? = null,
    @SerializedName("result") var result: Result? = Result(),
    @SerializedName("message") var message: String? = null
) {

    data class Result(

        @SerializedName("km_in") var kmIn: Int? = null,
        @SerializedName("km_out") var kmOut: Int? = null,
        @SerializedName("orders") var orders: ArrayList<Orders> = arrayListOf(),
        @SerializedName("total_orders") var totalOrders: Int? = null,
        @SerializedName("trip") var trip: String? = null

    ){
        data class Orders (

            @SerializedName("order_id"         ) var orderId          : String?           = null,
            @SerializedName("address"          ) var address          : String?           = null,
            @SerializedName("shippingmethod"   ) var shippingmethod   : String?           = null,
            @SerializedName("shippingtimeslot" ) var shippingtimeslot : String?           = null,
            @SerializedName("deliverydate"     ) var deliverydate     : String?           = null,
            @SerializedName("rider_status"     ) var riderStatus      : String?           = null,
            @SerializedName("images"           ) var images           : ArrayList<String> = arrayListOf()

        ): Parcelable {
            constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readArrayList( Orders::class.java.classLoader) as ArrayList<String>,
            ) {
            }

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(orderId)
                parcel.writeString(address)
                parcel.writeString(shippingmethod)
                parcel.writeString(shippingtimeslot)
                parcel.writeString(deliverydate)
                parcel.writeString(riderStatus)
                parcel.writeList(images)
//                if (Build.VERSION.SDK_INT >= 29) {
//                    parcel.writeParcelableList(images,flags)
//                }else{
//                    parcel.writeList(images)
//                }
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<Orders> {
                override fun createFromParcel(parcel: Parcel): Orders {
                    return Orders(parcel)
                }

                override fun newArray(size: Int): Array<Orders?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
