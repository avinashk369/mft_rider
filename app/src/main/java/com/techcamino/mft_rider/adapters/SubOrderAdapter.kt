package com.techcamino.mft_rider.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.techcamino.mft_rider.R
import com.techcamino.mft_rider.models.orders.OrderDetail

class SubOrderAdapter(
    private val mList: List<OrderDetail.Result.OrderInfo.Detail>,
    private val context: Context,
    val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SubOrderAdapter.ViewHolder>() {


    interface OnItemClickListener {
        fun onItemClick(order: OrderDetail.Result.OrderInfo.Detail,image:ImageView)
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suborder_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val order = mList[position]


        // sets the image to the imageview from our itemHolder class
        //holder.imageView.setImageResource(order.images[0])
        Glide.with(context)
            .load(order.image)
            .fitCenter()
            .centerCrop()
            .error(R.drawable.logo)
            .into(holder.imageView);

        if(order.upImage.isNullOrEmpty()){
            holder.placeholder.visibility=View.VISIBLE
            holder.accept.visibility=View.GONE
        }else{
            holder.placeholder.visibility=View.GONE
            holder.accept.visibility=View.VISIBLE
        }
        // sets the text to the textview from our itemHolder class
        Glide.with(context)
            .load(order.upImage)
            .fitCenter()
            .centerCrop()
            .error(R.drawable.ic_baseline_camera_alt_24)
            .into(holder.uImageView)
        holder.orderId.text = "#${order.subOrderId}"


        holder.bind(order, itemClickListener)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val orderId: TextView = itemView.findViewById(R.id.suborder_id)
        val accept: CardView = itemView.findViewById(R.id.accept_btn)
        val placeholder: LinearLayout = itemView.findViewById(R.id.placeholder)
        val uImageView: ImageView = itemView.findViewById(R.id.suborder_image)
        fun bind(item: OrderDetail.Result.OrderInfo.Detail, listener: OnItemClickListener) {
            accept.setOnClickListener {
                listener.onItemClick(
                    item,uImageView
                )
            }
            placeholder.setOnClickListener{
                listener.onItemClick(item,imageView)
            }
        }
    }
}